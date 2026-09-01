# Card Duel · 基础卡牌设计（P1 测试卡池）

> 状态：**设计稿 v1，已部分实现**。四类结算区分 + P1 内建技能最小集已实现（2025，见第 7 节）；37 张卡包 JSON/贴图仍待 P2。
> 关联：`P1_TEST.md`（测试清单，含 P1-5 四类结算用例）。

---

## 0. 目的与硬约束

**目的**：为 P1 对战核心提供可测试的卡池。P1 阶段所有卡牌（含魔法/陷阱/装备）按召唤卡数值结算；P2 技能系统实现后按类型差异化。

**硬约束**（来自现有代码，设计已满足）：
1. 卡包不允许重复卡（`CardBagMenu.canInsertCard`）→ 组满一副 27 张需要 27 张不同卡 → 本卡池 37 张，可组 2 副不同牌组
2. 数据校验要求 `ATK ≥ 1、HP ≥ 1、MP ≥ 1` → 纯防御/纯法术卡也设有 ≥1 的攻击（"P1 临时数值"）
3. 卡牌 JSON 目前无"稀有度"字段 → P2 实现卡包时需在 `CardDataPOJO`/`CardIndexPOJO` 增加 `rarity` 字段（见第 6 节）

---

## 1. 设计原则

1. **MC 原作用映射**：每张卡的效果必须对应原版物品/生物/建筑的机制（表中有"MC 依据"列）
2. **四类卡牌**：
   - 召唤（summon）＝ 生物，靠攻/血站场战斗
   - 魔法（mana）＝ 药水/消耗品/法术，即时效果（直伤/回复/抽牌/临时法力）
   - 陷阱（trap）＝ 红石机关与环境危害，以"秘密"（对手回合触发）与反制为主
   - 装备（equip）＝ 剑/盾/弓/护甲/鞘翅，P2 附着到召唤物上增强
3. **派系**：自然、下界、末地、虚空、红石、史蒂夫（详见第 2 节）
4. **稀有度**：普通/稀有/罕见/传说。强度随稀有度上升，但保留战术弹性（稀有卡可凭特效胜过低稀有度白板高攻，反之高攻白板也能赢花哨罕见卡）
5. **技能**：关键字参考炉石等卡牌游戏（战吼/亡语/嘲讽/冲锋/风怒/秘密/剧毒/潜行），全部以"P2 技能 id"形式预留；`SkillHooks` 已就位

---

## 2. 派系定义

| 派系 | id | 承载内容 | 典型卡 |
|---|---|---|---|
| 自然 | nature | 主世界生态（含海洋、敌对刷怪）与作物 | 羊、铁傀儡、苦力怕、仙人掌、三叉戟 |
| 下界 | nether | 下界生物、下界材料与灼烧主题 | 烈焰人、熔岩桶、岩浆块、凋零玫瑰、下界合金剑 |
| 末地 | end | 末地生物与瞬移/悬浮主题 | 末影人、潜影贝、末影珍珠、鞘翅 |
| 虚空 | void | 深暗/夜空/深渊（监守者、幻翼、幽匿） | 幻翼、监守者、幽匿尖啸体 |
| 红石 | redstone | 红石机关与生电陷阱 | 压力板地雷、发射器陷阱、铁砧坠落、TNT、红石脉冲 |
| 史蒂夫 | steve | 玩家道具：药水、剑盾、图腾 | 治疗药水、木剑、盾牌、不死图腾 |

> 原 `CardTribe` 枚举（ender/nature/monster/ocean）将被本表 6 派系取代（P2 更新枚举与语言文件）。

---

## 3. 稀有度定义

| 稀有度 | id | 数量（本卡池） | 定位 |
|---|---|---|---|
| 普通 | common | 16 | 基础数值卡，构筑骨干 |
| 稀有 | rare | 11 | 一个明确的特效 |
| 罕见 | epic | 7 | 强力特效或组合核心 |
| 传说 | legendary | 3 | 高费终结者：监守者、不死图腾、下界合金剑 |

---

## 4. 数值规范

- **HP** ≈ 生物血量（1 心 = 1 HP）；**ATK** ≈ 生物近战伤害（1 心 = 1 ATK）
- **费用 MP** ≈ (ATK + HP) / 2，再按特效强度 ±1；传说卡费用 5-8
- 药水类：回复/伤害量直接折算数值；抽牌/法力类特效 +1 费
- **装备卡**：atk = 攻击加成；**hp = 耐久值**（每次受攻击 -1，归零损坏，同时是该装备提供的额外生命层）
- **魔法/陷阱卡**：atk/hp 为 P1 占位数值（陷阱 thorns 类 hp 会参与站场战斗）

---

## 5. 完整卡表（37 张）

### 5.1 召唤卡（11 张）— 生物

| id | 名字 | 派系 | 稀有度 | 费用 | 攻/血 | 技能 id（P2） | 技能描述（P2） | MC 依据 |
|---|---|---|---|---|---|---|---|---|
| sheep | 羊 | 自然 | 普通 | 1 | 1/4 | — | 白板 | 羊 4 心血量、无攻击 |
| bee | 蜜蜂 | 自然 | 普通 | 1 | 1/1 | bee_sting | 亡语毒针：对击杀者造成 1 伤害 | 蜜蜂蜇人后死亡 |
| wolf | 狼 | 自然 | 普通 | 2 | 2/4 | wolf_pack | 战吼狼群：其他己方狼 +1 攻 | 狼群协同 |
| zombie | 僵尸 | 自然 | 普通 | 2 | 2/3 | zombie_rise | 亡语：召唤 1 个 1/2 僵尸 | 僵尸重生/尸潮 |
| iron_golem | 铁傀儡 | 自然 | 稀有 | 5 | 4/8 | iron_guard | 嘲讽 | 铁傀儡护卫村民 |
| creeper | 苦力怕 | 自然 | 稀有 | 3 | 1/6 | creeper_explosion | 亡语爆炸：双方全场召唤物与双方玩家各受 2 伤害 | 苦力怕自爆 |
| enderman | 末影人 | 末地 | 稀有 | 4 | 4/5 | ender_teleport | 潜行；本回合首次受击免疫（瞬移闪避） | 末影人瞬移 |
| shulker | 潜影贝 | 末地 | 罕见 | 5 | 3/6 | shulker_levitate | 战吼：一个敌方召唤物悬浮 1 回合（无法攻击） | 潜影贝漂浮弹 |
| blaze | 烈焰人 | 下界 | 稀有 | 4 | 3/3 | blaze_barrage | 风怒（每回合攻击两次） | 烈焰人三连火球 |
| phantom | 幻翼 | 虚空 | 稀有 | 3 | 3/2 | phantom_dive | 冲锋（上场即可攻击） | 幻翼俯冲偷袭 |
| warden | 监守者 | 虚空 | 传说 | 8 | 6/8 | warden_sonic | 回声重击：攻击时对相邻敌方召唤物各造成 2 伤害 | 监守者声波范围攻击 |

### 5.2 魔法卡（9 张）— 药水/消耗品/法术

| id | 名字 | 派系 | 稀有度 | 费用 | 攻/血(P1临时) | 技能 id（P2） | 技能描述（P2） | MC 依据 |
|---|---|---|---|---|---|---|---|---|
| healing_potion | 治疗药水 | 史蒂夫 | 普通 | 1 | 1/3 | heal_4 | 回复自己 4 点生命 | 喷溅治疗药水 |
| harming_potion | 伤害药水 | 史蒂夫 | 普通 | 2 | 2/2 | harm_3 | 对对方玩家造成 3 伤害 | 喷溅伤害药水 |
| redstone_pulse | 红石脉冲 | 红石 | 普通 | 1 | 1/2 | mana_1 | 本回合 +1 法力 | 红石中继器延时供能 |
| ender_pearl | 末影珍珠 | 末地 | 普通 | 2 | 2/2 | pearl_strike | 对随机敌方召唤物造成 2 伤害 | 珍珠瞬移+摔落伤害 |
| fire_charge | 火焰弹 | 下界 | 普通 | 2 | 2/2 | fire_3 | 对任一目标（召唤物或玩家）造成 3 伤害 | 火焰弹远程点火 |
| golden_apple | 金苹果 | 自然 | 稀有 | 3 | 1/3 | golden_heal | 回复 5 点生命，抽 1 张牌 | 金苹果回血+吸收 |
| lava_bucket | 熔岩桶 | 下界 | 稀有 | 4 | 3/2 | lava_4 | 对随机敌方单位（含玩家）造成 4 伤害 | 熔岩灼烧 |
| bottle_o_enchanting | 经验瓶 | 史蒂夫 | 罕见 | 3 | 1/2 | draw_2 | 抽 2 张牌 | 经验瓶掉落经验=成长 |
| totem_of_undying | 不死图腾 | 史蒂夫 | 传说 | 6 | 1/4 | totem_protect | 本局接下来一次致命伤害被抵挡，改为回复 5 生命 | 图腾免死效果 |

### 5.3 陷阱卡（8 张）— 红石机关与环境危害

| id | 名字 | 派系 | 稀有度 | 费用 | 攻/血(P1临时) | 技能 id（P2） | 技能描述（P2） | MC 依据 |
|---|---|---|---|---|---|---|---|---|
| pressure_plate_mine | 压力板地雷 | 红石 | 普通 | 1 | 2/2 | secret_mine | 秘密：对方打出下一张牌时，对其玩家造成 2 伤害 | 压力板+TNT 矿车 |
| anvil_drop | 铁砧坠落 | 红石 | 普通 | 2 | 3/2 | anvil_3 | 对一个敌方召唤物造成 3 伤害 | 铁砧掉落砸伤 |
| cactus | 仙人掌 | 自然 | 普通 | 2 | 1/3 | cactus_thorns | 反伤：受到攻击时对攻击方造成 1 伤害 | 仙人掌接触掉血 |
| magma_block | 岩浆块 | 下界 | 普通 | 2 | 1/3 | magma_burn | 反伤：受到攻击时对攻击方造成 1 伤害 | 岩浆块站立灼烧 |
| dispenser_trap | 发射器陷阱 | 红石 | 稀有 | 3 | 2/2 | secret_arrow | 秘密：对方召唤物上场时，对其造成 3 伤害 | 发射器射箭 |
| tnt | TNT | 红石 | 稀有 | 3 | 3/2 | secret_tnt | 秘密：对方场上有 3 个召唤物时触发，双方全场召唤物各受 3 伤害 | TNT 爆破 |
| wither_rose | 凋零玫瑰 | 下界 | 罕见 | 3 | 2/2 | secret_wither | 秘密：对方召唤物攻击时，攻击方立即受到 2 伤害 | 凋零玫瑰凋零效果 |
| sculk_shrieker | 幽匿尖啸体 | 虚空 | 罕见 | 4 | 2/2 | secret_sculk | 秘密：触发后对方下回合跳过抽牌 | 尖啸唤醒监守者（打断行动） |

### 5.4 装备卡（9 张）— 剑盾弓甲与鞘翅

| id | 名字 | 派系 | 稀有度 | 费用 | 攻/血(P1临时) | 技能 id（P2） | 技能描述（P2） | MC 依据 |
|---|---|---|---|---|---|---|---|---|
| wooden_sword | 木剑 | 史蒂夫 | 普通 | 1 | 1/2 | equip_atk_1 | 装备：+1 攻 | 木剑 4 伤害≈2 心 |
| iron_sword | 铁剑 | 史蒂夫 | 普通 | 2 | 2/2 | equip_atk_2 | 装备：+2 攻 | 铁剑 6 伤害≈3 心 |
| shield | 盾牌 | 史蒂夫 | 普通 | 2 | 1/3 | equip_guard | 装备：+3 生命，每次受击伤害 -1 | 盾牌格挡减伤 |
| bow | 弓 | 史蒂夫 | 稀有 | 2 | 1/2 | equip_ranged | 装备：+1 攻，攻击敌方召唤物时不受反击 | 弓箭远程无接触 |
| diamond_sword | 钻石剑 | 史蒂夫 | 稀有 | 3 | 3/2 | equip_atk_3 | 装备：+3 攻 | 钻石剑 7 伤害 |
| elytra | 鞘翅 | 末地 | 罕见 | 3 | 1/2 | equip_elytra | 装备：获得冲锋（上场即可攻击），免疫陷阱伤害 | 鞘翅滑翔越障 |
| trident | 三叉戟 | 自然 | 罕见 | 4 | 3/2 | equip_trident | 装备：+3 攻，风怒 | 三叉戟快速连刺 |
| diamond_armor | 钻石甲 | 史蒂夫 | 罕见 | 4 | 1/6 | equip_taunt | 装备：+6 生命，嘲讽 | 钻石甲高护甲值 |
| netherite_sword | 下界合金剑 | 下界 | 传说 | 5 | 4/2 | equip_netherite | 装备：+4 攻 +2 生命 | 下界合金武器强化 |

---

## 6. P2 实现卡包时需要的数据结构改动

1. `CardDataPOJO` / `CardIndexPOJO` 增加 `rarity` 字段（common/rare/epic/legendary），工具提示显示稀有度与彩色边框
2. `CardTribe` 枚举更新为 6 派系（nature/nether/end/void/redstone/steve）+ 语言文件派系名
3. 卡面贴图 37 张：美术未定前可全部复用 `sheep.png` 或按派系使用占位纯色（P2 再补）
4. 技能 id 注册：`SkillRegistry`（P2）按本表"技能 id"列实现完整技能；P1 内建子集（第 7 节）将被其取代
5. 陷阱"秘密"机制已实现（P1：秘密区 + 5 种内建触发）；P2 扩展触发条件与卡背渲染

## 7. 已实现（P1 四类结算区分 + 内建技能最小集）

**结算路径**（`DuelEngine.playCard` 按 type+skill 分流，未知技能一律按召唤卡站场兜底）：
- **召唤**：占己方战场槽（召唤失调/每回合一次攻击）
- **魔法**：打出立即结算内建效果 → 进弃牌堆（不占槽）
- **陷阱**：`secret_*` → 秘密区（上限 `DuelConfig.trapZoneLimit`，默认 3，可配 1-8，SERVER 配置）；`anvil_N` → 立即对点选敌方召唤物 N 伤；`*_thorns` → 占槽站场但不可主动攻击，受击反伤 1
- **装备**（用户 2025 规则修订）：附着己方召唤物，**每召唤物 1 件，新装备替换旧装备**。装备提供**属性**（atk=攻击加成）、**机制/技能**（内建子集见下）与**耐久**：
  - 耐久 = 装备卡 hp 字段，**每次"受到攻击"（主攻击/反击/反伤）-1**；魔法/陷阱伤害不磨损耐久
  - 耐久同时是额外生命层（召唤物存活判定 = 本体 HP + 装备耐久，随磨损递减）
  - 耐久归零 → 装备**重置后**进弃牌堆，召唤物恢复原有属性（本体 HP 已 ≤0 则立即死亡）
  - 召唤物死亡 → 装备与召唤物均**重置数值**后进弃牌堆（`DuelEngine.freshCard` 从注册表重建全新卡）
  - 装备特殊机制（内建）：`equip_guard` 受击伤害 -1 / `equip_ranged` 不受反击 / `equip_elytra` 冲锋+免疫陷阱伤害 / `equip_trident` 风怒（每回合两次攻击）/ `equip_taunt` 嘲讽（对方必须先攻击嘲讽单位）
  - 装备提供**派系**与"永久增加攻击力、血量"类特殊说明留 P2

**P1 内建技能子集**（skill id → 效果）：
- mana 类：`heal_N` 回 N 血（封顶 hpCap）/ `harm_N` 对方玩家 N 伤 / `fire_N` 随机敌方召唤物 N 伤（无召唤物兜底打玩家）/ `lava_N` 随机敌方单位 N 伤 / `draw_N` 抽 N 张 / `mana_N` 本回合 +N 法力 / `golden_heal` 回 5+抽 1 / `pearl_strike` 随机敌方召唤物受本卡 ATK 伤 / `totem_protect` 免死一次（抵挡致命伤害，生命置 5）
- trap 类：`secret_mine` 对方出任意牌→其玩家 2 伤 / `secret_arrow` 对方召唤物上场→该召唤物 3 伤 / `secret_wither` 对方攻击→攻击方 2 伤 / `secret_sculk` 对方回合开始→跳过抽牌 / `secret_tnt` 对方场上≥3 召唤物→双方全场 3 伤 / `anvil_N` 点选敌方召唤物 N 伤 / `cactus_thorns`·`magma_burn` 站场反伤 1
- equip 类：atk=攻击加成、hp=耐久（每受击-1）；特殊机制内建子集 `equip_guard`/`equip_ranged`/`equip_elytra`/`equip_trident`/`equip_taunt`；`equip_atk_1/2/3`、`equip_netherite` 为纯数值（netherite 的"永久"语义留 P2）
- summon 类：技能全部留 P2（白板站场）

**尚未实现（P2）**：战吼/亡语/嘲讽/冲锋/风怒/剧毒/潜行等召唤卡技能；装备独立槽位与特殊装备效果；秘密触发扩展；卡包 JSON 与贴图；拖拽施法交互（第 8 节）。

## 8. P2 交互规划（用户已确认，先记录后实现）

**炉石式拖拽施法目标选择**（用户 2025 提出）：
- 长按拖动手牌 / 玩家头像 / 召唤物，移动到想要施法的对象上释放
- 被拖动的来源与目标之间绘制一条**指示箭头**（贯穿整个拖动过程）
- 用于：魔法牌选目标（替代当前"点己方半场随机/点敌方槽"）、装备牌选宿主、攻击选目标、打脸拖到对方头像
- 实现要点（P2）：鼠标按下/拖动/释放三段事件 → 拖拽指示层渲染（`RenderGuiEvent`）→ 释放时命中检测（复用 `HudClickManager.hitTable` 的屏幕→桌面映射）→ 服务端沿用现有 payload（`ServerboundPlayCardPayload`/`ServerboundAttackPayload`）
- 当前 P1 的点击两段式交互（选中→点目标）保持不变，拖拽为增强交互

---

## 9. 技能系统（数据驱动）方向与难度预估

> 用户 2025 需求：技能做成独立模块，可被**任意类型卡牌**调用；玩家能像添加卡牌一样**添加新机制**。
> 结论：**框架能力足够（项目已有完整的卡包数据驱动基础设施可复用），但属于 P2 级重构，现不实施**。理由：P1 内建技能已闭环且尚未实测，立即重构会叠加测试盲区；本草案作为 P2 开局第一项。
> 用户 2025 补充设计（本节 9.1 已按此重写）：技能 = `trigger` + **有序 effects 列表**；效果由**基础效果接口**（effect id + 参数）组成，一个技能可按顺序串联多个效果。

**难度预估**：中-高。10-15 个文件，`DuelEngine` 大半重构，建议 3 个阶段交付（技能定义→执行器→迁移）。

### 9.1 架构草案（含用户确认的技能格式）

1. **技能资源格式**（与卡包同模式，放资源包 `cards/skills/*.json`；用户示例规范化后如下）：
   ```json
   {
     "id": "egskill_1",
     "trigger": "on_play_mana",
     "effects": [
       { "effect": "add_atk",  "target": "self", "amount": 1 },
       { "effect": "taunt",   "target": "self" }
     ]
   }
   ```
   即：该技能触发时**先**"增加 1 点攻击力"、**再**获得"嘲讽"。规范化说明（与用户原始示例的差异，已按此定稿）：
   - `effects` 用 **数组** 而非对象——JSON 对象不保证顺序，而技能要求"按序依次触发"，数组是唯一保序写法
   - 效果条目统一为对象：`{ "effect": "<效果类型id>", "<参数名>": <参数值> }`
   - 默认 `target` 为 `"self"`（技能承载者自身：召唤物/装备宿主/玩家），可省略；`deal_damage` 等必须显式指定目标
   - `trigger` 枚举：on_summon / on_death / on_attack / on_attacked / on_damaged / on_turn_start / on_turn_end / on_draw / on_play_mana / on_play_card / on_opponent_play_card / on_opponent_summon / on_opponent_attack / on_equip / on_unequip …

2. **基础效果接口清单 v1**（P2 实施时的内建 effect 集合；玩家组合它们即可造新技能）：

   | effect id | 类别 | 参数 | 语义 |
   |---|---|---|---|
   | `add_atk` | 数值 | `amount`, `target` | +攻击力 |
   | `add_hp` | 数值 | `amount`, `target` | +生命（治疗，封顶） |
   | `deal_damage` | 数值 | `amount`, `target` | 造成伤害（target：`enemy_player`/`random_enemy_summon`/`random_enemy_unit`/`selected_enemy_summon`/`attacker`/`self`） |
   | `draw` | 数值 | `amount` | 抽牌（触发者玩家） |
   | `add_mana` | 数值 | `amount` | 本回合 +法力 |
   | `block_damage` | 状态 | `once` | 抵挡下一次伤害（通用化的"不死图腾"） |
   | `add_durability` | 数值 | `amount`, `target` | 调整装备耐久（可选） |
   | `taunt` | 规则 | — | 嘲讽：对方必须先攻击此单位 |
   | `charge` | 规则 | — | 冲锋：上场即可攻击 |
   | `windfury` | 规则 | — | 风怒：每回合攻击两次 |
   | `guard` | 规则 | — | 受击伤害 -1 |
   | `ranged` | 规则 | — | 攻击不受反击 |
   | `trap_immune` | 规则 | — | 免疫陷阱伤害 |
   | `thorns` | 规则 | `amount` | 受击反伤 N（含"无法主动攻击"副作用，P2 拆分为独立规则项再议） |

   - **数值型**：纯数据驱动，玩家加 JSON 即可组合（对应 `SkillHooks` 的一次性效果）
   - **规则型**：引擎提供规则钩子接口（`canAttack/canTarget/damageModify/isImmune`），钩子即上表内建 effect；玩家可组合已有规则，**新规则需 Java 扩展**
   - **统一模型红利**：秘密陷阱也归一到此模型——`secret_mine` = `trigger: on_opponent_play_card` + `effects: [deal_damage enemy_player 2]`，与魔法/装备/召唤技能共用一套机制

3. **SkillRegistry**：加载 + 校验 + 按 trigger 索引（复用 `CommonCardPackLoader`/`CardAssetManager`/POJO 模式）

4. **SkillDispatcher**：引擎各触发点调用 `dispatcher.dispatch(trigger, context)`，遍历相关卡牌（场上/手牌/装备/秘密区）的 skill id → 按 `effects` 顺序执行

5. **effect 扩展点**：`DeferredRegister<EffectType>`（NeoForge 注册），其他 mod/开发者可注册全新 effect 类型（供高级自定义机制）

### 9.2 迁移清单（P2 实施时）

- 现有硬编码内建技能全部转为技能 JSON，映射示例：
  - `heal_4` = on_play_mana → `[add_hp self 4]`；`harm_3` = on_play_mana → `[deal_damage enemy_player 3]`
  - `golden_heal` = on_play_mana → `[add_hp self 5, draw 1]`（多效果串联示例）
  - `totem_protect` = on_play_mana → `[block_damage once]`
  - `secret_mine/arrow/wither/sculk/tnt` = on_opponent_* → 对应效果
  - `cactus_thorns` = on_attacked → `[deal_damage attacker 1]`；装备机制 5 个 = 规则型 effect
- `DuelEngine` 触发点接 dispatcher：playCard / attack / applyCardDamage / startTurn / endTurn / drawCard / death / summon（约 10 处）
- 卡数据 `skill` 字段语义不变（存 skill id），技能定义独立于卡牌 JSON
- 校验：引用不存在的 skill id / effect id / 参数越界 → 加载时警告 + 对局中兜底白板

### 9.3 装备规则修订记录（用户 2025 确认）

- 装备 HP 字段**即耐久**，不新增字段
- 耐久磨损条件：召唤物**受到任意伤害**（攻击/反击/反伤/魔法/陷阱）→ 耐久 -1；guard 减伤减到 0 视为未受伤不磨损
- 耐久归零 → 装备重置进弃牌堆，召唤物恢复原有属性；召唤物死亡 → 卡与装备均重置数值进弃牌堆（`freshCard` 注册表重建）

### 9.4 存储与性能评估（技能数据化对牌桌 NBT 的影响）

**结论：零增长。** 技能定义不进牌桌 NBT——技能 JSON 属卡包静态资源，加载进内存注册表（同卡包索引）；卡上 `skill` 字段仅存 id 字符串，数据化只是换字符串。

- 单卡 NBT（`ItemStack.saveOptional`：id/count + card_id/card_data/in_duel 组件）≈ 150-200B
- 一局规则固定的卡总数 ≤56 张（deck 27×2 + 手牌 8×2 + 场 7×2 + 装备 7×2 + 秘密 ≤3×2，弃牌堆为循环兜底）
- 牌桌落盘 NBT 最坏 ≈ **12KB**（正常 5-8KB）；对照 chunk 同步包 2MB / `NbtAccounter` 2MB 上限占 0.6%，安全
- 技能注册表内存 <200KB（按 200 个技能计）；dispatcher 每次触发微秒级，无性能压力

**附带发现（P2 优化项）**：现有 `DuelPlayerData.savePublic` 将 deck（54 张）与 discard **内容**全量放进公开同步包，每次 `sync()` 重发（~8KB/次），而客户端只需数量——P2 应改为 deck/discard 仅同步数量，可显著减小高频同步包。

---

## 10. 待用户敲定的事项

1. 卡池规模是否合适（37 张 / 2 副牌）；如需更多/更少，指出方向即可
2. 个别卡的主题与数值是否要调整（如监守者 8 费、不死图腾 6 费是否合理）
3. 秘密触发的优先级（多秘密同时满足时按放置顺序依次触发，当前实现）——如要改动 P2 再议
