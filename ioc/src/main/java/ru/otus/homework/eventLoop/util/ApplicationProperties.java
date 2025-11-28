package ru.otus.homework.eventLoop.util;

import java.io.FileInputStream;
import java.util.Properties;

public final class ApplicationProperties {

    private ApplicationProperties() {};

    public static Properties loadProperties() {
        try {
            String rootPath = Thread.currentThread().getContextClassLoader().getResource("app.properties").getPath();
            Properties appProperties = new Properties();
            appProperties.load(new FileInputStream(rootPath));
            return appProperties;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
