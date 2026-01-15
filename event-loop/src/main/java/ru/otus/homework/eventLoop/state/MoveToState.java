package ru.otus.homework.eventLoop.state;

import ru.otus.homework.eventLoop.command.Command;

import java.util.concurrent.BlockingDeque;

public class MoveToState implements State {

    private final BlockingDeque<Command> sourceDeque;

    private final BlockingDeque<Command> targetDeque;

    public MoveToState(BlockingDeque<Command> sourceDeque,
                       BlockingDeque<Command> targetDeque) {
        this.sourceDeque = sourceDeque;
        this.targetDeque = targetDeque;
    }

    @Override
    public State handle() {
        sourceDeque.drainTo(targetDeque);
        return this;
    }
}
