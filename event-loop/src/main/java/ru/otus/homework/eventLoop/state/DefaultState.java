package ru.otus.homework.eventLoop.state;

import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.ioc.Ioc;

import java.util.concurrent.BlockingDeque;

public class DefaultState implements State {

    private final BlockingDeque<Command> deque;

    public DefaultState(BlockingDeque<Command> deque) {
        this.deque = deque;
    }

    @Override
    public State handle() {
        Command command = null;
        try {
            command = deque.take();
            command.execute();
        } catch (Throwable e) {
            ((Command) Ioc.resolve("ExceptionHandler", new Object[]{command, e})).execute();
        }
        return this;
    }
}
