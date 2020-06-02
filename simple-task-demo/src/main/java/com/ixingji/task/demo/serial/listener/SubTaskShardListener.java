package com.ixingji.task.demo.serial.listener;

import com.ixingji.task.demo.serial.dao.SubTaskDao;
import com.ixingji.task.demo.serial.dao.impl.SubTaskDaoImpl;
import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.serial.listener.SerialSubTaskShardListener;
import com.ixingji.task.serial.model.SerialStat;
import com.ixingji.task.serial.model.SerialSubTask;

public class SubTaskShardListener implements SerialSubTaskShardListener {

    @Override
    public void beforeHandle(SerialSubTask serialSubTask, String shard) {
        //cdo nothing
    }

    @Override
    public void afterHandle(SerialSubTask serialSubTask, String shard) {
        SubTaskDao subTaskDao = new SubTaskDaoImpl();
        BusinessSubTask businessSubTask = subTaskDao.queryById((Integer) serialSubTask.getAttachment());

        SerialStat serialStat = serialSubTask.getShardStatMap().get(shard);
        businessSubTask.getShardMap().put(shard, serialStat.getValue());

        BusinessSubTask businessSubTask2 = new BusinessSubTask();
        businessSubTask2.setId((Integer) serialSubTask.getAttachment());
        businessSubTask2.setShardMap(businessSubTask.getShardMap());

        subTaskDao.update(businessSubTask2);
    }

}
