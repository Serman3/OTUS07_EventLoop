package ru.otus.homework.eventLoop.generator;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MetaData {

    private String methodName;

    private Map<String, Class<?>> parameterNameAndType;

    private String methodBody;

    private Class<?> returnType;
}
