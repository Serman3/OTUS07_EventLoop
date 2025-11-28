package ru.otus.homework.eventLoop.command;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.events.EventLoop;

@Slf4j
public class HardStopCommand implements Command {

    private final EventLoop eventLoop;

    public HardStopCommand(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void execute() {
        eventLoop.setState(() -> Boolean.FALSE);
        log.info("Command hard stop success");
    }
}
