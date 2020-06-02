package com.ixingji.task.demo.serial.dao;

import com.ixingji.task.demo.serial.model.BusinessTask;
import com.ixingji.task.demo.serial.model.TaskQueryCondition;

import java.util.List;

public interface TaskDao {

    BusinessTask queryById(Integer id);

    List<BusinessTask> queryListByCons(TaskQueryCondition queryCondition);

    Integer queryCountByCons(TaskQueryCondition queryCondition);

    Integer insert(BusinessTask task);

    boolean update(BusinessTask task);

}
