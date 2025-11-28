package ru.otus.homework.eventLoop.generator;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.*;

public class MetaDataCollector {

    private final static ThreadLocal<Set<Class<?>>> processedClasses = ThreadLocal.withInitial(HashSet::new); // Чтобы избежать циклических зависимостей

    public static List<MetaData> collectMetaData(Class<?> clazz) {
        try {
            List<MetaData> metaDataList = new ArrayList<>();
            collectMethods(clazz, metaDataList);
            return metaDataList;
        } finally {
            processedClasses.get().clear();
        }
    }

    private static void collectMethods(Class<?> clazz, List<MetaData> metaDataList) {
        if (clazz == null || clazz == Object.class || processedClasses.get().contains(clazz)) {
            return;
        }

        processedClasses.get().add(clazz);

        // Обрабатываем поля текущего класса
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            processMethod(method, metaDataList);
        }

        // Рекурсивно обрабатываем суперкласс
        collectMethods(clazz.getSuperclass(), metaDataList);
    }

    @SneakyThrows
    private static void processMethod(Method method, List<MetaData> metaDataList) {
        MetaData metaData = MetaData.builder()
                .methodName(method.getName())
                .parameterNameAndType(getNameAndTypeParameters(method))
                .returnType(method.getReturnType())
                .build();
        metaDataList.add(metaData);
    }

    private static Map<String, Class<?>> getNameAndTypeParameters(Method method) {
        Map<String, Class<?>> nameAndTypeParameters = new HashMap<>();

        Class<?>[] parametersTypes = method.getParameterTypes();
        for (Class<?> parameter : parametersTypes) {
            nameAndTypeParameters.put(parameter.getSimpleName(), parameter);
        }

        return nameAndTypeParameters;
    }

}
