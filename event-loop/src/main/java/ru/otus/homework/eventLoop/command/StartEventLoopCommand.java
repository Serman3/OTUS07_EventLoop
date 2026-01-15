package ru.otus.homework.eventLoop.command;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.events.EventLoop;

@Slf4j
public class StartEventLoopCommand implements Command {

    private final EventLoop eventLoop;

    public StartEventLoopCommand(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void execute() {
        eventLoop.startEventLoop();
        log.info("Command StartEventLoop success");
    }
}
