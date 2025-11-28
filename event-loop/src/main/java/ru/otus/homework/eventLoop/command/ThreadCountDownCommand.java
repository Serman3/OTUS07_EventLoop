package ru.otus.homework.eventLoop.command;

import java.util.concurrent.CountDownLatch;

public class ThreadCountDownCommand implements Command {

    private final CountDownLatch countDownLatch;

    public ThreadCountDownCommand(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void execute() {
        countDownLatch.countDown();
    }
}
