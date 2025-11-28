package ru.otus.homework.eventLoop.generator;

public interface MethodBuilder {

    String getBody(String methodName, String interfaceName, String returnTypeName);

    String getConstructorBody(String methodName, String interfaceName);

    String getCustomBody(String methodName, String interfaceName, String returnTypeName);
}
