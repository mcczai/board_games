package net.mcczai.cardduel.duel;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 单个玩家在一场对局中的所有数据（存于 DuelTableBlockEntity）。
 * 服务端权威，客户端仅通过同步包获得视图。
 */
public class DuelPlayerData {

    public static final int MAX_HAND = 8;
    public static final int MAX_DECK = 27;
    public static final int BOARD_SIZE = 7;

    /** 手牌（≤8） */
    private final List<ItemStack> hand = new ArrayList<>(MAX_HAND);
    /** 抽牌堆（开局时洗乱，≤27） */
    private final List<ItemStack> deck = new ArrayList<>(MAX_DECK);
    /** 弃牌堆 */
    private final List<ItemStack> discard = new ArrayList<>();
    /** 战场槽位（固定 7 个，空位为 EMPTY） */
    private final ItemStack[] board = new ItemStack[BOARD_SIZE];
    /** 每个槽位附着的装备卡（每个召唤物最多 1 件，新装备替换旧装备） */
    private final ItemStack[] equipped = new ItemStack[BOARD_SIZE];
    /** 秘密区（陷阱卡，背对对手，上限见 DuelConfig.TRAP_ZONE_LIMIT） */
    private final List<ItemStack> trapZone = new ArrayList<>();
    /** 每个槽位的召唤回合号（判定召唤失调） */
    private final int[] summonTurn = new int[BOARD_SIZE];
    /** 每个槽位最近一次攻击时的回合号（判定每回合只能攻击一次） */
    private final int[] attackTurn = new int[BOARD_SIZE];

    private int hp;
    private int mp;
    private int mpMax;
    /** 该玩家的回合计数（决定法力上限 = min(回合数, 封顶)） */
    private int turnCount;
    /** 疲劳计数：第 n 次牌库抽空扣 n 点血 */
    private int fatigue;

    /** 是否已提交牌组 */
    private boolean deckReady;
    /** 是否已完成换牌 */
    private boolean mulliganDone;
    /** 不死图腾是否生效（抵挡下一次致命伤害并回复 5 生命） */
    private boolean totemActive;

    public DuelPlayerData() {
        Arrays.fill(this.board, ItemStack.EMPTY);
        Arrays.fill(this.equipped, ItemStack.EMPTY);
    }

    // ==================== 集合访问 ====================

    public List<ItemStack> getHand() {
        return hand;
    }

    public List<ItemStack> getDeck() {
        return deck;
    }

    public List<ItemStack> getDiscard() {
        return discard;
    }

    public ItemStack[] getBoard() {
        return board;
    }

    public ItemStack[] getEquipped() {
        return equipped;
    }

    public List<ItemStack> getTrapZone() {
        return trapZone;
    }

    public boolean isTotemActive() {
        return totemActive;
    }

    public void setTotemActive(boolean totemActive) {
        this.totemActive = totemActive;
    }

    public int[] getSummonTurn() {
        return summonTurn;
    }

    public int[] getAttackTurn() {
        return attackTurn;
    }

    public void setDeck(List<ItemStack> cards) {
        this.deck.clear();
        this.deck.addAll(cards.stream().limit(MAX_DECK).map(ItemStack::copy).toList());
    }

    public void setDeckReady(boolean deckReady) {
        this.deckReady = deckReady;
    }

    public boolean isDeckReady() {
        return deckReady;
    }

    public boolean isMulliganDone() {
        return mulliganDone;
    }

    public void setMulliganDone(boolean mulliganDone) {
        this.mulliganDone = mulliganDone;
    }

    // ==================== 数值 ====================

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(hp, 0);
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = Math.max(mp, 0);
    }

    public int getMpMax() {
        return mpMax;
    }

    public void setMpMax(int mpMax) {
        this.mpMax = Math.max(mpMax, 1);
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = Math.max(turnCount, 0);
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = Math.max(fatigue, 0);
    }

    // ==================== 重置 ====================

    public void reset() {
        this.hand.clear();
        this.deck.clear();
        this.discard.clear();
        this.trapZone.clear();
        Arrays.fill(this.board, ItemStack.EMPTY);
        Arrays.fill(this.equipped, ItemStack.EMPTY);
        Arrays.fill(this.summonTurn, 0);
        Arrays.fill(this.attackTurn, 0);
        this.hp = 0;
        this.mp = 0;
        this.mpMax = 0;
        this.turnCount = 0;
        this.fatigue = 0;
        this.deckReady = false;
        this.mulliganDone = false;
        this.totemActive = false;
    }

    // ==================== NBT ====================

    public void save(CompoundTag tag, HolderLookup.Provider provider) {
        savePublic(tag, provider, true);
        tag.put("Hand", saveStackList(this.hand, provider));
        // 秘密区内容为隐藏信息：只落盘，不进公开同步包
        tag.put("Trap", saveStackList(this.trapZone, provider));
        tag.putBoolean("TotemActive", this.totemActive);
    }

    /**
     * 只写公开数据（不含手牌/陷阱内容），用于方块实体同步包与存档。
     * 装备为公开信息（对手可见挂载的装备），随 Board 一并公开。
     *
     * @param includeDeckContents true=写 deck/discard 完整内容（存档、对局前牌组预览同步）；
     *                            false=仅写 DeckCount/DiscardCount 数量（对局中公开同步瘦身，
     *                            弃牌堆/牌库内容永不进对局中公开同步包）
     */
    public void savePublic(CompoundTag tag, HolderLookup.Provider provider, boolean includeDeckContents) {
        if (includeDeckContents) {
            tag.put("Deck", saveStackList(this.deck, provider));
            tag.put("Discard", saveStackList(this.discard, provider));
        } else {
            tag.putInt("DeckCount", this.deck.size());
            tag.putInt("DiscardCount", this.discard.size());
        }

        ListTag boardTag = new ListTag();
        for (ItemStack stack : this.board) {
            boardTag.add(stack.saveOptional(provider));
        }
        tag.put("Board", boardTag);

        ListTag equipTag = new ListTag();
        for (ItemStack stack : this.equipped) {
            equipTag.add(stack.saveOptional(provider));
        }
        tag.put("Equipped", equipTag);

        tag.putIntArray("SummonTurn", this.summonTurn);
        tag.putIntArray("AttackTurn", this.attackTurn);

        tag.putInt("Hp", this.hp);
        tag.putInt("Mp", this.mp);
        tag.putInt("MpMax", this.mpMax);
        tag.putInt("TurnCount", this.turnCount);
        tag.putInt("Fatigue", this.fatigue);
        tag.putBoolean("DeckReady", this.deckReady);
        tag.putBoolean("MulliganDone", this.mulliganDone);
    }

    public void load(CompoundTag tag, HolderLookup.Provider provider) {
        this.hand.clear();
        this.deck.clear();
        this.discard.clear();
        this.trapZone.clear();
        loadStackList(this.hand, tag.getList("Hand", Tag.TAG_COMPOUND), provider);
        loadStackList(this.deck, tag.getList("Deck", Tag.TAG_COMPOUND), provider);
        loadStackList(this.discard, tag.getList("Discard", Tag.TAG_COMPOUND), provider);
        loadStackList(this.trapZone, tag.getList("Trap", Tag.TAG_COMPOUND), provider);

        ListTag boardTag = tag.getList("Board", Tag.TAG_COMPOUND);
        Arrays.fill(this.board, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(boardTag.size(), BOARD_SIZE); i++) {
            ItemStack stack = ItemStack.parseOptional(provider, boardTag.getCompound(i));
            this.board[i] = stack;
        }

        ListTag equipTag = tag.getList("Equipped", Tag.TAG_COMPOUND);
        Arrays.fill(this.equipped, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(equipTag.size(), BOARD_SIZE); i++) {
            ItemStack stack = ItemStack.parseOptional(provider, equipTag.getCompound(i));
            this.equipped[i] = stack;
        }

        Arrays.fill(this.summonTurn, 0);
        int[] turns = tag.getIntArray("SummonTurn");
        System.arraycopy(turns, 0, this.summonTurn, 0, Math.min(turns.length, BOARD_SIZE));

        Arrays.fill(this.attackTurn, 0);
        int[] attacks = tag.getIntArray("AttackTurn");
        System.arraycopy(attacks, 0, this.attackTurn, 0, Math.min(attacks.length, BOARD_SIZE));

        this.hp = tag.getInt("Hp");
        this.mp = tag.getInt("Mp");
        this.mpMax = tag.getInt("MpMax");
        this.turnCount = tag.getInt("TurnCount");
        this.fatigue = tag.getInt("Fatigue");
        this.deckReady = tag.getBoolean("DeckReady");
        this.mulliganDone = tag.getBoolean("MulliganDone");
        this.totemActive = tag.getBoolean("TotemActive");
    }

    private static ListTag saveStackList(List<ItemStack> stacks, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (ItemStack stack : stacks) {
            list.add(stack.saveOptional(provider));
        }
        return list;
    }

    private static void loadStackList(List<ItemStack> target, ListTag list, HolderLookup.Provider provider) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(provider, list.getCompound(i));
            if (!stack.isEmpty()) {
                target.add(stack);
            }
        }
    }
}
