package net.mcczai.cardduel.duel;

/**
 * 牌桌对局的阶段状态机
 */
public enum DuelPhase {
    /** 无人入座 */
    IDLE,
    /** 房主已入座，等待设置上限 */
    SETUP,
    /** 上限已设置，等待双方入座并提交牌组 */
    WAITING,
    /** 换牌阶段 */
    MULLIGAN,
    /** 对局进行中 */
    PLAYING,
    /** 对局结束 */
    FINISHED
}
