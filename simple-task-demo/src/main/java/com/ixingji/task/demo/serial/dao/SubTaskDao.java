package com.ixingji.task.demo.serial.dao;

import com.ixingji.task.demo.serial.model.BusinessSubTask;

import java.util.List;

public interface SubTaskDao {

    BusinessSubTask queryById(Integer id);

    List<BusinessSubTask> queryListByTaskId(Integer taskId);

    Integer insert(BusinessSubTask subTask);

    Integer update(BusinessSubTask subTask);

}
