package ru.otus.homework.eventLoop.scopes;

import ru.otus.homework.eventLoop.adapter.DynamicAdapterFactory;
import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.generator.MethodBuilder;
import ru.otus.homework.eventLoop.ioc.Ioc;

import java.util.function.Function;

public class AdapterRegisterCommand implements Command {

    @Override
    public void execute() {
        Function<Object[], Object> function = (args) -> {
            Class<?> clazz = (Class<?>) args[0];
            MethodBuilder methodBuilder = (MethodBuilder) args[1];
            return DynamicAdapterFactory.createAdapterFactory(clazz, methodBuilder);
        };
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"Adapter", function})).execute();
    }

}
