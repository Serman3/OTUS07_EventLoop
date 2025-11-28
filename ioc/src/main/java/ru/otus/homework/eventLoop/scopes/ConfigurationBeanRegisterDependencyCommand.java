package ru.otus.homework.eventLoop.scopes;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import ru.otus.homework.eventLoop.annotation.Bean;
import ru.otus.homework.eventLoop.annotation.Configuration;
import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.util.ApplicationProperties;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ConfigurationBeanRegisterDependencyCommand implements Command {

    private static final String SCAN_PACKAGE = ApplicationProperties.loadProperties().getProperty("package.scan");

    @Override
    public void execute() {
        try {
            Map<Class<?>, Object> localBeanContext = scanConfigBeans();
            readDependencies(localBeanContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Class<?>, Object> scanConfigBeans() throws Exception {
        Map<Class<?>, Object> beanContext = new HashMap<>();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(SCAN_PACKAGE)
                .addScanners(Scanners.TypesAnnotated));

        Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(Configuration.class);

        for (Class<?> beanClass : configurationClasses) {
            Object beanInstance = beanClass.getDeclaredConstructor().newInstance();
            beanContext.put(beanClass, beanInstance);
        }
        return beanContext;
    }

    @SneakyThrows
    private void readDependencies(Map<Class<?>, Object> beanContext) {
        for (Object beanInstance : beanContext.values()) {
            Class<?> beanClass = beanInstance.getClass();
            Method[] methods = beanClass.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Bean.class)) {
                    method.setAccessible(true);
                    method.invoke(beanInstance);
                    log.info("Register bean {}", method.getName());
                }
            }
        }
    }

}
