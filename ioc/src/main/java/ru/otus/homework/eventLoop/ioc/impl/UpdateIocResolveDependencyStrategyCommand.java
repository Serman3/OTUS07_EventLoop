package ru.otus.homework.eventLoop.ioc.impl;

import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.ioc.Ioc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UpdateIocResolveDependencyStrategyCommand implements Command {

    private final Lock lock = new ReentrantLock();
    private final Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> updateIoCStrategy;

    public UpdateIocResolveDependencyStrategyCommand(Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>> updateIoCStrategy) {
        this.updateIoCStrategy = updateIoCStrategy;
    }

    @Override
    public void execute() {
        lock.lock();
        try {
            Ioc.strategy = updateIoCStrategy.apply(Ioc.strategy);
        } finally {
            lock.unlock();
        }
    }
}
