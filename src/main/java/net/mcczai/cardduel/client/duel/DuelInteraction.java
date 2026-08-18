package net.mcczai.cardduel.client.duel;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

/**
 * 对局中的客户端选中状态：
 *  - selectedHand：选中的手牌索引（出牌）
 *  - selectedBoard：选中的己方战场槽位（攻击）
 *  - mulliganSelection：换牌阶段选中的手牌索引（≤2）
 */
@OnlyIn(Dist.CLIENT)
public final class DuelInteraction {

    private static int selectedHand = -1;
    private static int selectedBoard = -1;
    private static final Set<Integer> MULLIGAN_SELECTION = new HashSet<>();

    private DuelInteraction() {
    }

    public static int getSelectedHand() {
        return selectedHand;
    }

    public static void toggleHand(int index) {
        selectedHand = selectedHand == index ? -1 : index;
        selectedBoard = -1;
    }

    public static int getSelectedBoard() {
        return selectedBoard;
    }

    public static void toggleBoard(int slot) {
        selectedBoard = selectedBoard == slot ? -1 : slot;
        selectedHand = -1;
    }

    public static Set<Integer> getMulliganSelection() {
        return MULLIGAN_SELECTION;
    }

    public static void toggleMulligan(int index) {
        if (MULLIGAN_SELECTION.contains(index)) {
            MULLIGAN_SELECTION.remove(index);
        } else if (MULLIGAN_SELECTION.size() < 2) {
            MULLIGAN_SELECTION.add(index);
        }
    }

    public static void clearMulligan() {
        MULLIGAN_SELECTION.clear();
    }

    public static void clear() {
        selectedHand = -1;
        selectedBoard = -1;
        MULLIGAN_SELECTION.clear();
    }
}
