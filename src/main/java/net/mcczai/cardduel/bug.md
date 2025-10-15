# 1.20.1移植的部分bug

> ## 1.有关根据卡牌类型分类
> 在[CardItemBuilder.java](https://github.com/mcczai/neoforge-1.21-board_games/blob/master/src/main/java/net/mcczai/cardduel/items/builder/CardItemBuilder.java)(指向github，下同)  内
>> ### 需求
>> 在游戏加载阶段（狐狸转圈），先按不同类型的物品注册基础物品，然后形成Map。  
>> 在进入游戏后，加入世界时游戏会创建创造物品栏，此时调用前面生成的Map，将物品分类开来。  
>> 目的是未来方便扩展
>> ### 现状
>> 生成Map这步的event订阅错了，导致Map没正常生成
>> ### 解决
>> 暂时注释取消用法
> ## 2.本地化失败
> 在成功加载后，游戏内物品名称为**default_card.card.sheep.name**，但未加载本地化名称
>> ### 分析 
>> 经测试，将本地化的东西塞到mod本地化中，会正常显示。而在默认卡包的本地化文件中无法显示
>> ### 推测
>> 推测为本地化的mixin部分错误，到时候再改改
>> ### 补充
>> 疑似其他地方(例如本地化、贴图资源读取)也有这类问题

