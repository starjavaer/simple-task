package com.ixingji.task.demo.serial.dao.impl;

import com.ixingji.task.demo.serial.dao.TaskDao;
import com.ixingji.task.demo.serial.model.BusinessTask;
import com.ixingji.task.demo.serial.model.TaskQueryCondition;

import java.util.List;

public class TaskDaoImpl implements TaskDao {

    @Override
    public BusinessTask queryById(Integer id) {
        return null;
    }

    @Override
    public List<BusinessTask> queryListByCons(TaskQueryCondition queryCondition) {
        return null;
    }

    @Override
    public Integer queryCountByCons(TaskQueryCondition queryCondition) {
        return null;
    }

    @Override
    public Integer insert(BusinessTask task) {
        return null;
    }

    @Override
    public boolean update(BusinessTask task) {
        return false;
    }

}
