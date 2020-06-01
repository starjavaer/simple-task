package com.ixingji.task.atomic.model;

import com.ixingji.task.atomic.handler.AtomicTaskHandler;
import com.ixingji.task.model.Task;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class AtomicTask extends Task {

    private Stat stat = Stat.INIT;

    private Owner owner = Owner.SENDER;

    @Setter
    private boolean distributed;

    private AtomicTask prev;

    @Setter
    private AtomicTask next;

    @Setter
    private AtomicTaskHandler<?> handler;

    public AtomicTask(String name) {
        super(name);
    }

    public AtomicTask(String name, Owner owner) {
        super(name);
        this.owner = owner;
    }

    public void updateStatus(Stat stat) {
        this.stat = stat;
    }

    public void setPrev(AtomicTask prev) {
        this.prev = prev;
        this.prev.setNext(this);
    }

    public enum Stat {
        /* 初始状态 */
        INIT,
        /* 处理中 */
        HANDLING,
        /* 处理成功 */
        HANDLE_SUC,
        /* 处理失败 */
        HANDLE_FAIL,
        /* 恢复中 */
        RESTORING,
        /* 恢复成功 */
        RESTORE_SUC,
        /* 恢复失败 */
        RESTORE_FAIL,
        /* 完成成功 */
        FINISH_SUC,
        /* 完成失败 */
        FINISH_FAIL;
    }

    public enum Owner {
        /* 发起者 */
        SENDER,
        /* 参与者 */
        PARTER;
    }

}
