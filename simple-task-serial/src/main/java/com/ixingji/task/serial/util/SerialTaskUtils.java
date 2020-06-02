package com.ixingji.task.serial.util;

import com.ixingji.task.serial.SerialTaskConfig;
import com.ixingji.task.serial.SerialTaskTemplateManager;
import com.ixingji.task.serial.exception.SerialTaskException;
import com.ixingji.task.serial.model.SerialSubTask;
import com.ixingji.task.serial.model.SerialSubTaskTemplate;
import com.ixingji.task.serial.model.SerialTask;
import com.ixingji.task.serial.model.SerialTaskTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class SerialTaskUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialTaskUtils.class);

    public static void scanTask() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String scanPath = SerialTaskConfig.getInstance().getScanPackage().replace(".", "/");

        Enumeration<URL> resources = classLoader.getResources(scanPath);
        List<File> directories = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            directories.add(new File(resource.getFile()));
        }

        for (File directory : directories) {
            scanTask(directory, SerialTaskConfig.getInstance().getScanPackage());
        }
    }

    private static void scanTask(File directory, String parentPackage) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanTask(file, parentPackage + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String clazzName = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        // skip inner class
                        if (clazzName.contains("$")) {
                            continue;
                        }
                        Class<?> clazz = Class.forName(parentPackage + "." + clazzName);
                        scanSubTask(clazz);
                    } catch (ClassNotFoundException e) {
                        throw new SerialTaskException("class '" + parentPackage + "." + clazzName + "' not found");
                    }
                }
            }
        }
    }

    private static void scanSubTask(Class<?> clazz) {
        if (clazz.getAnnotation(com.ixingji.task.serial.annotation.SerialTask.class) == null) {
            return;
        }

        List<SerialSubTaskTemplate> subTaskTemplates = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(com.ixingji.task.serial.annotation.SerialSubTask.class)) {
                com.ixingji.task.serial.annotation.SerialSubTask subTaskAnnotation
                        = (com.ixingji.task.serial.annotation.SerialSubTask) method.getAnnotation(com.ixingji.task.serial.annotation.SerialSubTask.class);
                String subTaskName = subTaskAnnotation.name();
                int subTaskIndex = subTaskAnnotation.index();
                subTaskTemplates.add(new SerialSubTaskTemplate(subTaskName, subTaskIndex, clazz, method));
            }
        }

        sortAndCheck(subTaskTemplates);

        com.ixingji.task.serial.annotation.SerialTask taskAnnotation
                = (com.ixingji.task.serial.annotation.SerialTask) clazz.getAnnotation(com.ixingji.task.serial.annotation.SerialTask.class);
        String taskName = taskAnnotation.name();

        SerialTaskTemplateManager.register(taskName, new SerialTaskTemplate(taskName, subTaskTemplates));
    }

    public static void sortAndCheck(List<SerialSubTaskTemplate> subTaskTemplates) {
        // sort by index
        subTaskTemplates.sort(Comparator.comparingInt(SerialSubTaskTemplate::getIndex));

        // check index
        for (int i = 0; i < subTaskTemplates.size(); i++) {
            if (subTaskTemplates.get(i).getIndex() != (i + 1)) {
                throw new SerialTaskException("subtasks are not be indexed in serial");
            }
        }
    }

    public static SerialTask makeTask(SerialTaskTemplate taskTemplate) {
        List<SerialSubTask> subTasks = taskTemplate.getOrderedSubTaskTemplates().stream()
                .map(SerialTaskUtils::makeSubTask)
                .collect(Collectors.toList());

        return new SerialTask(taskTemplate.getName(), subTasks);
    }

    public static SerialSubTask makeSubTask(SerialSubTaskTemplate subTaskTemplate) {
        return new SerialSubTask(subTaskTemplate.getName(), subTaskTemplate);
    }

    public static boolean invokeSubTask(SerialSubTask subTask) {
        boolean hasErr = false;
        SerialSubTaskTemplate subTaskTemplate = subTask.getTemplate();
        try {
            subTaskTemplate.getMethod()
                    .invoke(subTaskTemplate.getClazz().newInstance(), subTask.getParam());
        } catch (Exception e) {
            hasErr = true;
            LOGGER.error("invoke subtask error, " + e);
        }
        return !hasErr;
    }

    public static boolean invokeSubTaskShard(SerialSubTask subTask, String shard) {
        boolean hasErr = false;
        SerialSubTaskTemplate subTaskTemplate = subTask.getTemplate();
        try {
            subTaskTemplate.getMethod().invoke(subTaskTemplate.getClazz().newInstance(), shard, subTask.getParam());
        } catch (Exception e) {
            hasErr = true;
            LOGGER.error("invoke subtask shard error, " + e);
        }
        return !hasErr;
    }

}
