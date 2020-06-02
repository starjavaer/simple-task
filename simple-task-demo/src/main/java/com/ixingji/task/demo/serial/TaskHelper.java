package com.ixingji.task.demo.serial;

import com.ixingji.task.demo.serial.dao.SubTaskDao;
import com.ixingji.task.demo.serial.dao.impl.SubTaskDaoImpl;
import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.util.AssertUtils;

public class TaskHelper {

    public static void log(String log) {
        Integer subTaskId = TaskContext.get().getCurrentSubTaskId();
        AssertUtils.notNull(subTaskId, "subtask id in the context is null");

        SubTaskDao subTaskDao = new SubTaskDaoImpl();
        BusinessSubTask businessSubTask = subTaskDao.queryById(subTaskId);

        BusinessSubTask businessSubTask2 = new BusinessSubTask();
        businessSubTask2.setId(businessSubTask.getId());
        String log2 = businessSubTask.getLog() + "\n" + log;
        businessSubTask.setLog(log2);

        subTaskDao.update(businessSubTask2);
    }

}
