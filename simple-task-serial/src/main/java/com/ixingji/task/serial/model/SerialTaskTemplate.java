package com.ixingji.task.serial.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SerialTaskTemplate {

    private String name;

    private List<SerialSubTaskTemplate> orderedSubTaskTemplates;

}
