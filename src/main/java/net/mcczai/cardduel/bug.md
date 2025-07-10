# 现阶段还没写完的东西

> ## 1.有关根据卡牌类型分类
> 在[CardItemBuilder.java](https://github.com/mcczai/neoforge-1.21-board_games/blob/c1ab8373d366ab74161e44a127f3f88166eb877e/src/main/java/net/mcczai/cardduel/items/builder/CardItemBuilder.java)(指向github，下同)  内
>> ### 需求
>> 在游戏加载阶段（狐狸转圈），先按不同类型的物品注册基础物品，然后形成Map。  
>> 在进入游戏后，加入世界时游戏会创建创造物品栏，此时调用前面生成的Map，将物品分类开来。  
>> 目的是未来方便扩展
>> ### 现状
>> 生成Map这步的event订阅错了，导致Map没正常生成
>> ### 解决
>> 暂时注释取消用法
> ## 2.空间命名错误/本地化失败
> 在成功加载后，游戏内物品名称为**default_card:sheep**，而不是期望的**default_card.card.sheep.name**
>> ### 分析 
>> 断点检查发现是在[CommonCardIndexLoader.java](https://github.com/mcczai/neoforge-1.21-board_games/blob/c1ab8373d366ab74161e44a127f3f88166eb877e/src/main/java/net/mcczai/cardduel/resources/loader/CommonCardIndexLoader.java)内32-65行中的创建读取有关  
>> 其中调用了**loadCardFromJsonString**方法后的**id**变量为**default_card:sheep**
>> ### ~~推测~~
>> ~~大概是在**CommonCardIndexLoader.java**中的读取资源部分，对空间命名的方法有误~~
>> ### 补充
>> 疑似其他地方(例如本地化、贴图资源读取)也有这类问题 

