package com.ixingji.task.serial.model;

import com.ixingji.task.model.Task;
import com.ixingji.task.util.AssertUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SerialSubTask extends Task {

    private final SerialSubTaskTemplate template;

    @Setter
    private Map<String, SerialStat> shardStatMap;

    @Setter
    private Object param;

    private SerialStat stat = SerialStat.INIT;


    public SerialSubTask(String name, SerialSubTaskTemplate template) {
        super(name);
        this.template = template;
    }

    public void updateState(SerialStat stat) {
        this.stat = stat;
    }

    public void addShard(String name) {
        if (this.shardStatMap == null) {
            this.shardStatMap = new HashMap<>(64);
        }
        this.shardStatMap.put(name, SerialStat.INIT);
    }

    public void updateShardState(String name, SerialStat stat) {
        AssertUtils.notNull(this.shardStatMap.get(name), "shard '" + name + "' is null");
        this.shardStatMap.put(name, stat);
    }

}
