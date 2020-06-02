package com.ixingji.task.demo.serial.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class BusinessSubTask {

    private Integer id;

    private Integer taskId;

    private String name;

    private String state;

    private Integer index;

    private String param;

    /**
     * shard name -> shard state
     */
    private Map<String, String> shardMap;

    private String log;

    private Date startTime;

    private Date updateTime;

}
