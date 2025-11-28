package ru.otus.homework.eventLoop.command;

import ru.otus.homework.eventLoop.events.EventLoop;

public class StartEventLoopCommand implements Command {

    private final EventLoop eventLoop;

    public StartEventLoopCommand(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void execute() {
        eventLoop.startEventLoop();
    }
}
