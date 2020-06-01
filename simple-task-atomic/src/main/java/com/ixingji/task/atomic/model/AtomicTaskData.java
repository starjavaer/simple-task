package com.ixingji.task.atomic.model;

import com.ixingji.task.atomic.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AtomicTaskData<T> {

    @Setter
    private T data;

//    @Setter
//    private String groupName;

    @Setter
    private String taskName;

    @Setter
    private String handlerName;

    @Setter
    private AtomicTask.Stat taskStat;

    @Setter
    private AtomicTaskAction taskAction;

    @Setter
    private String fromWho;

    private String startTime;

    private String updateTime;

    public AtomicTaskData() {
        // JSON序列化使用
    }

    public AtomicTaskData(T data) {
        this.data = data;
        this.startTime = DateUtils.nowInMs();
        this.updateTime = startTime;
    }

    public AtomicTaskData(T data, AtomicTask.Stat taskStat) {
        this(data);
        this.taskStat = taskStat;
    }

}
