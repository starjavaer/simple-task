package com.ixingji.task.demo.serial.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BusinessTask {

    private Integer id;

    private String name;

    private String state;

    private Date startTime;

    private Date updateTime;

    private List<BusinessSubTask> subTasks;

}
