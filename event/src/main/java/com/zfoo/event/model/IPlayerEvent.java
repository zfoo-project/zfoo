package com.zfoo.event.model;

/**
 * 玩家事件接口
 * <p>
 * 主要用于让同一个玩家ID在同一个线程里面进行事件的业务处理,防止玩家事件在不同的线程中处理造成的玩家数据冲突或者线程竞争导致的性能下降.
 * </p>
 *
 * @author veione
 */
public interface IPlayerEvent extends IEvent {

    @Override
    default int executorHash() {
        return (int) executePlayerId();
    }

    /**
     * 执行玩家ID
     *
     * @return 玩家ID
     */
    long executePlayerId();
}

