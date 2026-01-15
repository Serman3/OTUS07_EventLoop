package ru.otus.homework.eventLoop.command;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ThreadCountDownCommand implements Command {

    private final CountDownLatch countDownLatch;

    public ThreadCountDownCommand(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void execute() {
        log.info("Command ThreadCountDown success");
        countDownLatch.countDown();
    }
}
