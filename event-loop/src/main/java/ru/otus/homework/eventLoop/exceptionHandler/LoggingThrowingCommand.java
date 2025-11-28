package ru.otus.homework.eventLoop.exceptionHandler;

import lombok.extern.slf4j.Slf4j;
import ru.otus.homework.eventLoop.command.Command;

@Slf4j
public class LoggingThrowingCommand implements Command {

    private final Throwable throwable;

    public LoggingThrowingCommand(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void execute() {
        log.warn("Logging exception", throwable);
    }

}
