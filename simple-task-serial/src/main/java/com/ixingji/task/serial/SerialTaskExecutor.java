package com.ixingji.task.serial;

import com.ixingji.task.serial.listener.SerialSubTaskListener;
import com.ixingji.task.serial.listener.SerialSubTaskShardListener;
import com.ixingji.task.serial.listener.SerialTaskListener;
import com.ixingji.task.serial.model.*;
import com.ixingji.task.serial.util.SerialTaskUtils;
import com.ixingji.task.util.AssertUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SerialTaskExecutor {

    private final String taskName;

    private Object taskAttachment;

    private List<Object> subTasksParam;

    private List<String[]> subTasksShards;

    @Getter
    private SerialTaskListener taskListener;

    @Getter
    private SerialSubTaskListener subTaskListener;

    @Getter
    private SerialSubTaskShardListener subTaskShardListener;

    public SerialTaskExecutor(String taskName) {
        this.taskName = taskName;
    }

    public SerialTaskExecutor setTaskAttachment(Object taskAttachment) {
        this.setTaskAttachment(taskAttachment);
        return this;
    }

    public SerialTaskExecutor setSubTaskParam(int subTaskIndex, Object subTaskParam) {
        if (subTasksParam == null) {
            subTasksParam = new ArrayList<>();
        }

        while (subTasksParam.size() < subTaskIndex) {
            subTasksParam.add(null);
        }

        subTasksParam.set(subTaskIndex - 1, subTaskParam);

        return this;
    }

    public SerialTaskExecutor setSubTaskShards(int subTaskIndex, String[] subTaskShards) {
        if (subTaskShards == null) {
            subTasksShards = new ArrayList<>();
        }

        while (subTasksShards.size() < subTaskIndex) {
            subTasksShards.add(null);
        }

        subTasksShards.set(subTaskIndex - 1, subTaskShards);

        return this;
    }

    public boolean execute() {
        SerialTaskTemplate taskTemplate = SerialTaskTemplateManager.get(taskName);
        return execute(1, taskTemplate.getOrderedSubTaskTemplates().size());
    }

    public boolean execute(int fromIndex) {
        SerialTaskTemplate taskTemplate = SerialTaskTemplateManager.get(taskName);
        return execute(fromIndex, taskTemplate.getOrderedSubTaskTemplates().size());
    }

    /**
     * [fromIndex,toIndex]
     *
     * @param fromIndex from索引，从1开始
     * @param toIndex   to索引，至最后一个任务
     * @return true 成功 false 失败
     */
    public boolean execute(int fromIndex, int toIndex) {
        AssertUtils.isTrue(fromIndex >= 1, "from index should >= 1");

        SerialTask task = initTask();
        List<SerialSubTask> subTasks = task.getOrderedSubTasks();

        AssertUtils.isTrue(toIndex <= subTasks.size(), "to index should <= size of subtasks");

        task.updateState(SerialStat.RUNNING);

        if (taskListener != null) {
            taskListener.beforeHandle(task);
        }

        boolean doSuccess = true;

        for (int i = fromIndex - 1; i < toIndex - 1; i++) {
            SerialSubTask subTask = subTasks.get(i);

            subTask.updateState(SerialStat.RUNNING);

            if (subTaskListener != null) {
                subTaskListener.beforeHandle(subTask);
            }

            if (subTask.getShardStatMap() != null
                    && subTask.getShardStatMap().size() > 0) {
                for (String shard : subTask.getShardStatMap().keySet()) {
                    if (subTaskShardListener != null) {
                        subTaskShardListener.beforeHandle(subTask, shard);
                    }

                    boolean doShardSuccess = SerialTaskUtils.invokeSubTaskShard(subTask, shard);

                    subTask.updateShardState(shard, doShardSuccess ? SerialStat.SUC : SerialStat.FAIL);

                    if (!doShardSuccess) {
                        subTask.updateState(SerialStat.FAIL);
                    }

                    if (subTaskShardListener != null) {
                        subTaskShardListener.afterHandle(subTask, shard);
                    }

                    if (!doShardSuccess) {
                        doSuccess = false;
                        break;
                    }
                }
            } else {
                doSuccess = SerialTaskUtils.invokeSubTask(subTask);
            }

            task.updateState(doSuccess ? SerialStat.SUC : SerialStat.FAIL);

            if (subTaskListener != null) {
                subTaskListener.afterHandle(subTask);
            }

            if (!doSuccess) {
                break;
            }
        }

        SerialStat taskStat;
        if (doSuccess) {
            if (toIndex < subTasks.size()) {
                taskStat = SerialStat.PAUSE;
            } else {
                taskStat = SerialStat.SUC;
            }
        } else {
            taskStat = SerialStat.FAIL;
        }
        task.updateState(taskStat);

        if (taskListener != null) {
            taskListener.afterHandle(task);
        }

        return doSuccess;
    }

    private SerialTask initTask() {
        SerialTaskTemplate taskTemplate = SerialTaskTemplateManager.get(taskName);

        SerialTask task = SerialTaskUtils.makeTask(taskTemplate);
        SerialTaskManager.register(task);

        task.setAttachment(this.taskAttachment);

        List<SerialSubTask> subTasks = task.getOrderedSubTasks();

        while (subTasksParam != null && subTasksParam.size() < subTasks.size()) {
            subTasksParam.add(null);
        }

        while (subTasksShards != null && subTasksShards.size() < subTasks.size()) {
            subTasksShards.add(null);
        }

        if (subTasksParam != null || subTasksShards != null) {
            for (int i = 0; i < subTasks.size(); i++) {
                if (subTasksParam != null && subTasks.get(i) != null) {
                    subTasks.get(i).setParam(subTasksParam.get(i));
                }

                if (subTasksShards != null && subTasksShards.get(i) != null) {
                    String[] subTaskShards = subTasksShards.get(i);
                    for (String subTaskShard : subTaskShards) {
                        subTasks.get(i).addShard(subTaskShard);
                    }
                }
            }
        }

        return task;
    }


}
