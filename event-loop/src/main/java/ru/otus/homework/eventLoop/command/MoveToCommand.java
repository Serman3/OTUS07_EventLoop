package ru.otus.homework.eventLoop.command;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.events.EventLoop;
import ru.otus.homework.eventLoop.state.MoveToState;

import java.util.concurrent.BlockingDeque;

@Slf4j
public class MoveToCommand implements Command {

    private final EventLoop eventLoop;

    private final BlockingDeque<Command> helpDeque;

    public MoveToCommand(EventLoop eventLoop, BlockingDeque<Command> helpDeque) {
        this.eventLoop = eventLoop;
        this.helpDeque = helpDeque;
    }

    @Override
    public void execute() {
        eventLoop.setEventLoopState(new MoveToState(eventLoop.geCurrentDequeCommands(), helpDeque));
        log.info("Command MoveTo success");
    }
}
