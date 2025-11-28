package ru.otus.homework.eventLoop.events;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.ioc.Ioc;

import java.util.concurrent.BlockingDeque;
import java.util.function.Supplier;

@Slf4j
public class EventLoop extends Thread {

    private static final Object LOCK = new Object();
    private Supplier<Boolean> state;
    private final BlockingDeque<Command> deque;

    public EventLoop(BlockingDeque<Command> deque) {
        this.deque = deque;
        setState(() -> Boolean.TRUE);
    }

    @Override
    public void run() {
        log.info("Thread {} started", this.getClass().getSimpleName());

        Command command = null;

        while (state.get()) {
            try {
                command = deque.take();
                command.execute();
            } catch (Throwable e) {
                ((Command) Ioc.resolve("ExceptionHandler", new Object[]{command, e})).execute();
            }
        }

        log.info("Thread {} finished", this.getClass().getSimpleName());
    }

    public void startEventLoop() {
        log.info("EventLoop started");
        this.start();
    }

    public void setState(Supplier<Boolean> state) {
        synchronized (LOCK) {
            this.state = state;
        }
    }

    public BlockingDeque<Command> geCurrentDequeCommands() {
        return this.deque;
    }

}
