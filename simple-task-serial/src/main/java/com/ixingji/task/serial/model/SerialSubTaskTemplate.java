package com.ixingji.task.serial.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class SerialSubTaskTemplate {

    private String name;

    private int index;

    private Class<?> clazz;

    private Method method;

}
