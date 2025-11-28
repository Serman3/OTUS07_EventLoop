package ru.otus.homework.eventLoop.scopes;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import ru.otus.homework.eventLoop.annotation.Bean;
import ru.otus.homework.eventLoop.annotation.Inject;
import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.ioc.Ioc;
import ru.otus.homework.eventLoop.util.ApplicationProperties;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class BeanRegisterDependencyCommand implements Command {

    private static final String SCAN_PACKAGE = ApplicationProperties.loadProperties().getProperty("package.scan");

    @Override
    public void execute() {
        try {
            Map<Class<?>, Object> localBeanContext = scanAndRegisterBeans();
            injectDependencies(localBeanContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Class<?>, Object> scanAndRegisterBeans() throws Exception {
        Map<Class<?>, Object> beanContext = new HashMap<>();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(SCAN_PACKAGE)
                .addScanners(Scanners.TypesAnnotated));

        Set<Class<?>> beanClasses = reflections.getTypesAnnotatedWith(Bean.class);

        for (Class<?> beanClass : beanClasses) {
            Object beanInstance = createInstance(beanClass);
            beanContext.put(beanClass, beanInstance);
            register(getBeanName(beanClass), (args) -> beanInstance);
            log.info("Register bean: {}", beanClass.getName());
        }
        return beanContext;
    }

    private void injectDependencies(Map<Class<?>, Object> beanContext) throws Exception {
        for (Object beanInstance : beanContext.values()) {
            Class<?> beanClass = beanInstance.getClass();
            for (Constructor<?> constructor : beanClass.getConstructors()) {
                if (constructor.isAnnotationPresent(Inject.class)) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> paramType = paramTypes[i];
                        Object dependency = beanContext.get(paramType);
                        if (dependency == null) {
                            throw new RuntimeException("Зависимость не найдена: " + paramType.getName());
                        }
                        params[i] = dependency;
                    }

                    Object fullyInitializedBean = constructor.newInstance(params);
                    register(getBeanName(beanClass), (args) -> fullyInitializedBean);
                    log.info("Injected dependencies to constructor class: {}", beanClass.getName());
                    break;
                }
            }
        }
    }

    private void register(String dependency, Function<Object[], Object> dependencyResolverStrategy) {
        ((Command) Ioc.resolve(
                "IoC.Register",
                new Object[]{dependency, dependencyResolverStrategy})
        ).execute();
    }

    private String getBeanName(Class<?> beanClass) {
        return beanClass.isAnnotationPresent(Bean.class) ? beanClass.getAnnotation(Bean.class).value() : beanClass.getSimpleName();
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

}
