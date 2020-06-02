package com.ixingji.task.demo.serial.listener;

import com.ixingji.task.demo.serial.converter.SubTaskConverter;
import com.ixingji.task.demo.serial.converter.TaskConverter;
import com.ixingji.task.demo.serial.dao.SubTaskDao;
import com.ixingji.task.demo.serial.dao.TaskDao;
import com.ixingji.task.demo.serial.dao.impl.SubTaskDaoImpl;
import com.ixingji.task.demo.serial.dao.impl.TaskDaoImpl;
import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.demo.serial.model.BusinessTask;
import com.ixingji.task.serial.listener.SerialTaskListener;
import com.ixingji.task.serial.model.SerialStat;
import com.ixingji.task.serial.model.SerialSubTask;
import com.ixingji.task.serial.model.SerialTask;
import com.ixingji.task.util.AssertUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TaskListener implements SerialTaskListener {

    @Override
    public void beforeHandle(SerialTask serialTask) {
        if (serialTask.getAttachment() != null) {
            BusinessTask businessTask = (BusinessTask) serialTask.getAttachment();

            List<BusinessSubTask> businessSubTasks = businessTask.getSubTasks();
            List<SerialSubTask> serialSubTasks = serialTask.getOrderedSubTasks();

            AssertUtils.isTrue(serialSubTasks.size() == businessSubTasks.size(), "amount of subtasks has changed");

            for (int i = 0; i < serialSubTasks.size(); i++) {
                BusinessSubTask businessSubTask = businessSubTasks.get(i);
                SerialSubTask serialSubTask = serialSubTasks.get(i);

                // 将task的id存放到task的attachment中，后面更新状态时使用
                serialSubTask.setAttachment(businessSubTask.getId());

                // 填充任务参数
                if (Objects.nonNull(businessSubTask.getParam())) {
                    serialSubTask.setParam(businessSubTask.getParam());
                }

                // 填充任务分片
                if (Objects.nonNull(businessSubTask.getShardMap())) {
                    for (Map.Entry<String, String> shardMapEntry : businessSubTask.getShardMap().entrySet()) {
                        if (!Objects.equals(shardMapEntry.getValue(), SerialStat.SUC.getValue())) {
                            serialSubTask.addShard(shardMapEntry.getKey());
                        }
                    }
                }
            }

            serialTask.setStat(SerialStat.value(businessTask.getState()));
            // 要把task里的attachment重新设为taskId，后面更新状态时使用
            serialTask.setAttachment(businessTask.getId());
        } else {
            BusinessTask businessTask = TaskConverter.convert(serialTask);
            TaskDao taskDao = new TaskDaoImpl();
            SubTaskDao subTaskDao = new SubTaskDaoImpl();
            serialTask.setAttachment(taskDao.insert(businessTask));
            AssertUtils.notNull(serialTask.getAttachment(), "business task id is null");
            serialTask.getOrderedSubTasks().forEach(serialSubTask -> {
                BusinessSubTask businessSubTask =SubTaskConverter.convert(serialSubTask);
                businessSubTask.setTaskId((Integer) serialTask.getAttachment());
                serialSubTask.setAttachment(subTaskDao.insert(businessSubTask));
            });
        }
    }

    @Override
    public void afterHandle(SerialTask serialTask) {
        TaskDao taskDao = new TaskDaoImpl();
        BusinessTask businessTask = new BusinessTask();
        businessTask.setId((Integer) serialTask.getAttachment());
        businessTask.setState(serialTask.getStat().getValue());
        taskDao.update(businessTask);
    }

}
