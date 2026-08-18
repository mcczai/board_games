# Card Duel Mod — 问题清单

> 自动生成于代码审查 | 共 **3** 处问题（已修复 **54** 处）

---

## 🔴 Critical (6/6) — 会导致崩溃/数据损坏/功能完全不可用

| # | 文件 | 行号 | 说明 |
|---|------|------|------|
| ~~1~~ | ~~`items/AbstractCardItem.java`~~ | ~~56-104~~ | ~~`fillItemTab` 的 `switch` 缺少 `break`，所有 case 穿透到底部 default，导致每张卡在创造标签页重复添加 5 次~~ |
| ~~2~~ | ~~`init/ModMenuType.java` + `CardduelMod.java`~~ | ~~25-34~~ | ~~`ModMenuType.MENUS` 定义了 `DeferredRegister` 但构造函数中未调用 `register(bus)`，菜单类型永远不会被注册~~ |
| ~~3~~ | ~~`items/inventory/CardBundleMenu.java`~~ | ~~22~~ | ~~`super(MenuType.HOPPER, containerId)` 硬编码使用原版漏斗菜单类型，应使用 `ModMenuType.CARD_BUNDLE_MENU.get()`~~ |
| ~~4~~ | ~~`items/inventory/CardBundleMenu.java`~~ | ~~14, 28-30~~ | ~~`CONTAINER_SIZE=30` 但只添加了 5 个 slot，剩余 25 个不可见槽位可能导致物品丢失/复制/同步异常~~ |
| ~~5~~ | ~~`client/renderer/CardItemRenderer.java`~~ | ~~23-29~~ | ~~`getInstance()` 中 `Minecraft.getInstance()` 无空值检查；若在 Minecraft 实例初始化前调用会 NPE~~ |
| ~~6~~ | ~~`skill/CommonSummonOperation.java`~~ | ~~55~~ | ~~`commonAttack` 中防御方 HP 从 `tag1`（攻击方）错误读取，应为 `tag2`（防御方），导致战斗伤害计算完全错误~~ |

---

## 🟠 Major (11/11) — 显著运行时 Bug / 逻辑错误 / 架构问题

| #      | 文件                                                                       | 行号        | 说明                                                                                                                                                 |
|--------|--------------------------------------------------------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| ~~7~~  | ~~`client/resource/ClientCardIndex.java`~~                               | ~~76-78~~ | ~~`getInstance()` 未设置 `pojo` 字段，`getPojo()` 始终返回 null → `ClientCardTooltip.getText()` 会 NPE~~                                                      |
| ~~8~~ | ~~`items/AbstractCardItem.java`~~ | ~~36-43~~ | ~~`getName()` 调用了 `@OnlyIn(Dist.CLIENT)` 标注的 `CdAPI.getClientCardIndex()`；在专用服务端会崩溃~~ |
| ~~9~~ | ~~`items/CardBundleItem.java`~~ | ~~62-69~~ | ~~`use()` 的服务端分支为空，无打开菜单逻辑 → 右键卡包无法打开 GUI~~ |
| ~~10~~ | ~~`client/resource/loader/asset/TextureLoader.java`~~ | ~~35~~ | ~~`new ZipPackTexture(id, zipFile.getName())` 只传文件名而非完整路径，ZIP 内纹理加载会 `FileNotFoundException`~~ |
| ~~11~~ | ~~`client/gui/screens/inventory/CardBundleScreen.java`~~ | ~~17~~ | ~~GUI 引用的 `textures/gui/container/card_bundle.png` 纹理文件不存在，渲染为紫色/黑色棋盘格~~ |
| ~~12~~ | ~~`client/resource/ClientCardPackLoader.java`~~                          | ~~100~~   | ~~对每个 ZIP 条目无条件调用 `PackInfoLoader.load()`（已在 94 行处理过），冗余无效代码~~                                                                                     |
| ~~13~~ | ~~`resources/CommonCardPackLoader.java`~~ | ~~29~~ | ~~`new ResourceLocation.Serializer()` — Minecraft 1.20.5+ 已移除 `ResourceLocation.Serializer` 内部类，**编译错误**~~ |
| ~~14~~ | ~~`client/resource/serialize/ItemStackSerializer.java`~~ | ~~32~~ | ~~`ResourceLocation.tryBySeparator(itemName, ':')` — MC 1.21 中应为 `ResourceLocation.tryParse()`，**编译错误**~~ |
| ~~15~~ | ~~`block/DuelTableBlock.java`~~                                              | ~~39-55~~     | ~~双重桌放置逻辑要求点击位置和相邻位置均已存在方块 → `DOUBLE=true` 实际上永远无法通过正常放置达成~~                                                                                           |
| ~~16~~ | ~~`init/ModDataComponents.java` 与 `API/item/nbt/CardDataAccessor.java`~~ | ~~-~~     | ~~注册了 HP/ATK/MP/Type/Skill/CardId 等 DataComponent，但 `CardDataAccessor` 全部通过 `CUSTOM_DATA`（原始 NBT）读写，注册的组件从未被使用（仅 `CARD_ID` 和 `CARD_BUNDLE` 实际在用）~~ |
| ~~17~~ | ~~`CardduelCreativeTab.java`~~ | ~~19~~ | ~~标签页 ID `"tarp_tab"` 拼写错误，应为 `"trap"`。`en_us.json`、`zh_cn.json` 中翻译键也全部写错~~ |

---

## 🟡 Minor(3/32) — 代码质量 / 可维护性 / 死代码

### Bug（小）
| # | 文件 | 说明 |
|---|------|------|
| ~~18~~ | ~~`resources/CommonCardIndex.java:41-43`~~ | ~~两处验证错误消息都写的是 "HP"（应分别为 "ATK" 和 "MP"）~~ |
| ~~19~~ | ~~`block/blockstate/DuelTableType.java:27`~~ | ~~default 分支 `throw new MatchException(null, null)` 参数为 null，应使用 `IllegalStateException`~~ |
| ~~20~~ | ~~`items/CardBundleItem.java:45`~~ | ~~插入物品时仅检查 `canFitInsideContainerItems()`，未验证 `ICard` 类型，非卡牌物品也能放入卡包~~ |
| ~~21~~ | ~~`client/ClientCardTooltip.java:40-42`~~ | ~~`getHeight()` 始终返回 0，Tooltip 渲染会与其他 UI 重叠~~ |
| ~~22~~ | ~~`items/CardItem.java:38`~~ | ~~方块比较使用 `==` 引用相等，应使用注册键比较~~ |
| ~~23~~ | ~~`API/item/nbt/CardDataAccessor.java:73`~~ | ~~`getType()` 默认值 `"tarp"`（拼写错误），与 `fillItemTab` 中 `"trap"` 分支不匹配~~ |
| ~~24~~ | ~~`resources/VersionChecker.java:61,64`~~ | ~~多余的类型转换 + `exception.getMessage()` 可能为 null~~ |
| ~~25~~ | ~~`items/CardTooltipPart.java:32`~~ | ~~`int itemHP = tag.getInt("HideFlags")` 读取后立即被覆写（死赋值）~~ |
| ~~26~~ | ~~`items/inventory/CardBundleContents.java:56-57`~~ | ~~`getWeight` 包含原版蜜蜂蜂巢逻辑（`DataComponents.BEES`），卡牌永远不会触发此路径~~ |

### 死代码 / 空壳
| # | 文件 | 说明 |
|---|------|------|
| ~~27~~ | ~~`init/ModItem.java:38-48`~~ | ~~`onItemRegister` 的 `@SubscribeEvent` 被注释，导致 `CardItemManager.CARD_ITEM_MAP` 永不为空 → 整个 `CardItemManager` 为死代码~~ |
| ~~28~~ | ~~`items/CardItemManager.java`~~ | ~~整类因事件未触发成为死代码（Map 从未被填充）~~ |
| ~~29~~ | ~~`CardduelCreativeTab.java:29-51`~~ | ~~23 行被注释的 MANA/EQUIP/SUMMON 标签页代码~~ |
| ~~30~~ | ~~`block/blockstate/DuelTableType.java`~~ | ~~枚举完全未被任何代码引用~~ |
| ~~31~~ | ~~`block/entity/DuelTableBlockEntity.java`~~ | ~~方块实体无任何自定义逻辑（无 NBT 保存/加载、无 tick、无数据存储）~~ |
| ~~32~~ | ~~`player/getPlayerSight.java`~~ | ~~类名小写开头违反 Java 规范 + `getPlayerLookBlockDirection()` 始终返回 false + 公共字段 `Pix` 未被引用~~ |
| ~~33~~ | ~~`util/SkillHandler.java`~~ | ~~完全空类~~ |
| ~~34~~ | ~~`items/inventory/BaseCardBundleItem.java`~~ | ~~空壳子类，从未被使用~~ |
| ~~35~~ | ~~`items/component/CardDataComponent.java`~~ | ~~Record 类（Codec + 字段），从未被导入或使用~~ |
| ~~36~~ | ~~`items/component/ItemStackWrapper.java`~~ | ~~包装类（Codec + StreamCodec），从未被使用~~ |
| ~~37~~ | ~~`items/component/Slots.java`~~ | ~~Record 类（Codec + StreamCodec），从未被使用~~ |
| ~~38~~ | ~~`network/IMessage.java`~~ | ~~网络接口（encode/decode/handle），无人实现~~ |
| ~~39~~ | ~~`network/SerialPacketBase.java`~~ | ~~空接口 `{}`，无人实现~~ |
| ~~40~~ | ~~`util/GetJarResources.java:64-66`~~ | ~~`copyModDirectory(String, Path, String)` 重载从未被调用~~ |
| ~~41~~ | ~~`items/CardBundleItem.java:80-82`~~ | ~~`playDropContentsSound()` 定义但从未调用~~ |
| ~~42~~ | ~~`items/CardBundleItem.java:18`~~ | ~~`MAX_ROW = 5` 字段从未被引用~~ |
| 43 | `items/CardItem.java:33-41` | `useOn` 中 if 块为空（TODO 桩） |
| 44 | `items/CardItem.java:44-47` | `Rclick` 为空实现（TODO 桩） |
| ~~45~~ | ~~`config/common/OtherConfig.java:8-13`~~ | ~~`builder.push("other")` 缺少对应的 `pop()`~~ |

### 架构 / 代码风格
| #      | 文件 | 说明 |
|--------|------|------|
| ~~46~~ | ~~`cardduel/bug.md`~~ | ~~bug 文档放在 Java 源码目录中（`src/main/java/net/mcczai/cardduel/`）~~ |
| ~~47~~ | ~~`items/AbstractCardItem.java:33`~~ | ~~`Rclick` 方法名违反 Java 命名规范（应为 `rClick` 或 `rightClick`）~~ |
| ~~48~~ | ~~`API/DefaultAsset.java` 与 `resources/DefaultAssets.java`~~ | ~~两个仅差一个 's' 的类（单数/复数），功能应合并~~ |
| ~~49~~     | ~~`resources/DefaultAssets.java:7`~~ | ~~`EMPTY_CARD_ID` 未声明为 `final`~~ |
| ~~50~~     | ~~`init/ModBlocks.java:4`~~ | ~~未使用的 import `DuelTableBlockEntity`~~ |
| ~~51~~     | ~~`init/ModItem.java:12-18`~~ | ~~5 个 import 仅被注释掉的 `onItemRegister` 方法使用~~ |
| ~~52~~     | ~~`CardduelMod.java:24`~~ | ~~参数命名 `IEventBus Bus` 首字母应为小写 `bus`~~ |
| ~~53~~     | ~~`models/item/dueltable_block_item.json`~~ | ~~block 父模型下定义 `"layer0"` 纹理无效（block 模型不使用 layer0）~~ |
| ~~54~~     | ~~`gradle.properties:41`~~ | ~~`mod_group_id=net.mcczai.carddduel` 多一个 'd'（实际包名为 `cardduel`）~~ |
| ~~55~~     | ~~`custom/default_card_pack/.../lang/`~~ | ~~默认卡包仅有 `zh_cn.json`，缺少 `en_us.json`（非中文客户端看到的是原始翻译键）~~ |
| ~~56~~     | ~~`init/ModBlockEntities.java:19`~~ | ~~`.build(null)` 传递 null 而非 `DataFixers.getDataFixer()`~~ |
| 57     | 多处 | NeoForge 1.21+ 中 `@OnlyIn(Dist.CLIENT)` 行为改变，不再在加载时剥离类，可能不可靠 |
| ~~58~~     | ~~`gradle.properties:10-11`~~ | ~~Parchment 映射版本 `1.20.6` 与目标 MC `1.21.1` 不匹配~~ |

---

## 📊 统计

| 严重程度 | 数量     |
|----------|--------|
| 🔴 Critical | 0      |
| 🟠 Major | 0      |
| 🟡 Minor | 3      |
| **总计** | **3**  |

| 类别 | 数量 |
|------|------|
| Bug | 0 |
| DeadCode | 2 |
| Architecture | 1 |
| Compilation | 0 |
| CodeStyle/Other | 0 |
