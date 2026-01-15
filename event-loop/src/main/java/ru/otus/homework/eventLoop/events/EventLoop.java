package ru.otus.homework.eventLoop.events;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.state.DefaultState;

import java.util.concurrent.BlockingDeque;

@Slf4j
public class EventLoop extends Thread {

    private static final Object LOCK = new Object();
    private final BlockingDeque<Command> deque;
    private ru.otus.homework.eventLoop.state.State eventLoopState;

    public EventLoop(BlockingDeque<Command> deque) {
        this.deque = deque;
        setEventLoopState(new DefaultState(geCurrentDequeCommands()));
    }

    @Override
    public void run() {
        log.info("Thread {} started", this.getClass().getSimpleName());

        while (this.eventLoopState != null && !deque.isEmpty()) {
            this.eventLoopState.handle();
        }

        log.info("Thread {} finished", this.getClass().getSimpleName());
    }

    public void startEventLoop() {
        this.start();
    }

    public void setEventLoopState(ru.otus.homework.eventLoop.state.State eventLoopState) {
        synchronized (LOCK) {
            this.eventLoopState = eventLoopState;
        }
    }

    public ru.otus.homework.eventLoop.state.State getEventLoopState() {
        return this.eventLoopState;
    }

    public BlockingDeque<Command> geCurrentDequeCommands() {
        return this.deque;
    }

}
