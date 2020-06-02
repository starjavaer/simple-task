package com.ixingji.task.demo.serial.model;

import lombok.Data;

import java.util.Date;

@Data
public class TaskQueryCondition {

    /**
     * task name
     */
    private String name;

    /**
     * task id
     */
    private Integer id;

    /**
     * task state
     */
    private String state;

    /**
     * begin time
     */
    private Date beginTime;

    /**
     * end time
     */
    private Date endTime;

    /**
     * page no
     */
    private Integer pageNo = 1;

    /**
     * page size
     */
    private Integer pageSize = 10;

    private Integer pageStart;

    public Integer getPageStart() {
        return (pageNo - 1) * pageSize;
    }

    public static void main(String[] args) {
        TaskQueryCondition queryCondition = new TaskQueryCondition();
        queryCondition.setPageNo(2);
        System.err.println(queryCondition.getPageStart());
    }

}
