package ru.otus.homework.eventLoop.command;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.events.EventLoop;
import ru.otus.homework.eventLoop.state.DefaultState;

@Slf4j
public class RunCommand implements Command {

    private final EventLoop eventLoop;

    public RunCommand(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void execute() {
        eventLoop.setEventLoopState(new DefaultState(eventLoop.geCurrentDequeCommands()));
        log.info("Command RunCommand success");
    }
}
