package ru.otus.homework.eventLoop.command;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.events.EventLoop;

import java.util.concurrent.BlockingDeque;

@Slf4j
public class SoftStopCommand implements Command {

    private final EventLoop eventLoop;

    public SoftStopCommand(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void execute() {
        BlockingDeque<Command> dequeCommands = eventLoop.geCurrentDequeCommands();
        if (!dequeCommands.isEmpty()) {
            eventLoop.setState(() -> !dequeCommands.isEmpty());
            log.info("Command soft stop success");
        }
    }
}
