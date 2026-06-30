package net.mcczai.cardduel.API.item;

import com.google.gson.annotations.SerializedName;

/**
 *  卡牌属性分类：
 *   末影、自然、怪物、海洋
 */
public enum CardTribe {
    //末影
    @SerializedName("ender")
    ENDER,
//自然
    @SerializedName("nature")
    NATURE,
//怪物
    @SerializedName("monster")
    MONSTER,
//海洋
    @SerializedName("ocean")
    OCEAN;
}
