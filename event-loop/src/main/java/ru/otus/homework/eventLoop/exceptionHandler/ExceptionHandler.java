package ru.otus.homework.eventLoop.exceptionHandler;

import ru.otus.homework.eventLoop.command.Command;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ExceptionHandler {

    private static final Map<Type, Map<Type, BiFunction<Command, Throwable, Command>>> STORE = new HashMap<>();

    public static Command handle(Command command, Throwable exception) {
        Type commandType = command.getClass();
        Type exceptionType = exception.getClass();
        return STORE.get(commandType).get(exceptionType).apply(command, exception);
    }

    public static void register(Type commandType, Type exceptionType, BiFunction<Command, Throwable, Command> function) {
        if (!STORE.containsKey(commandType)) {
            STORE.put(commandType, new HashMap<>(Map.of(exceptionType, function)));
        } else {
            STORE.get(commandType).put(exceptionType, function);
        }
    }
}
