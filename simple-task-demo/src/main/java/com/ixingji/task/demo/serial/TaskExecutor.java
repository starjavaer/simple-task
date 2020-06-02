package com.ixingji.task.demo.serial;

import com.ixingji.task.demo.serial.dao.SubTaskDao;
import com.ixingji.task.demo.serial.dao.TaskDao;
import com.ixingji.task.demo.serial.dao.impl.SubTaskDaoImpl;
import com.ixingji.task.demo.serial.dao.impl.TaskDaoImpl;
import com.ixingji.task.demo.serial.listener.SubTaskListener;
import com.ixingji.task.demo.serial.listener.SubTaskShardListener;
import com.ixingji.task.demo.serial.listener.TaskListener;
import com.ixingji.task.demo.serial.model.BusinessSubTask;
import com.ixingji.task.demo.serial.model.BusinessTask;
import com.ixingji.task.serial.SerialTaskExecutor;
import com.ixingji.task.serial.model.SerialStat;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TaskExecutor {

    public static boolean execute(String taskName, List<String> tasksParam) throws Exception {
        return execute(taskName, tasksParam, null);
    }

    public static boolean execute(String taskName, List<String> tasksParam, List<String[]> tasksShards) throws Exception {
        try {
            SerialTaskExecutor taskExecutor = new SerialTaskExecutor(taskName);

            taskExecutor.setTaskListener(new TaskListener());
            taskExecutor.setSubTaskListener(new SubTaskListener());
            taskExecutor.setSubTaskShardListener(new SubTaskShardListener());

            if (tasksParam != null && tasksParam.size() > 0) {
                for (int i = 0; i < tasksParam.size(); i++) {
                    taskExecutor.setSubTaskParam(i + 1, tasksParam.get(i));
                }
            }

            if (tasksShards != null && tasksShards.size() > 0) {
                for (int i = 0; i < tasksShards.size(); i++) {
                    String[] taskShards = tasksShards.get(i);
                    if (taskShards != null) {
                        taskExecutor.setSubTaskShards(i + 1, taskShards);
                    }
                }
            }

            return taskExecutor.execute();
        } finally {
            TaskContext.remove();
        }
    }

    /**
     * 续作task
     * @param taskId 待续作taskId
     * @return
     * @throws Exception
     */
    public static boolean execute(int taskId) throws Exception {
        try {
            TaskDao taskDao = new TaskDaoImpl();
            BusinessTask businessTask = taskDao.queryById(taskId);

            if (businessTask != null) {
                SubTaskDao subTaskDao = new SubTaskDaoImpl();
                List<BusinessSubTask> businessSubTasks = subTaskDao.queryListByTaskId(taskId);

                businessSubTasks.sort(Comparator.comparing(BusinessSubTask::getIndex));

                BusinessSubTask startSubTask = null;
                for (BusinessSubTask businessSubTask : businessSubTasks) {
                    if (!Objects.equals(businessSubTask.getState(), SerialStat.SUC.getValue())) {
                        startSubTask = businessSubTask;
                        break;
                    }
                }

                if (startSubTask != null) {
                    SerialTaskExecutor taskExecutor = new SerialTaskExecutor(businessTask.getName());
                    taskExecutor.setTaskListener(new TaskListener());
                    taskExecutor.setSubTaskListener(new SubTaskListener());
                    taskExecutor.setSubTaskShardListener(new SubTaskShardListener());

                    businessTask.setSubTasks(businessSubTasks);
                    taskExecutor.setTaskAttachment(businessTask);

                    return taskExecutor.execute(startSubTask.getIndex());
                }
            }
        } finally {
            TaskContext.remove();
        }

        return false;
    }

}
