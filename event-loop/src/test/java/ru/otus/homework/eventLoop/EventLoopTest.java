package ru.otus.homework.eventLoop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.events.EventLoop;
import ru.otus.homework.eventLoop.ioc.Ioc;
import ru.otus.homework.eventLoop.scopes.InitCommand;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class EventLoopTest {

    private final BlockingDeque<Command> dequeCommands = new LinkedBlockingDeque<>();

    @BeforeEach
    void setUp() {
        Command command1 = Mockito.mock();
        doThrow(RuntimeException.class).when(command1).execute();
        Command command2 = Mockito.mock();
        Command command3 = Mockito.mock();
        Command command4 = Mockito.mock();
        Command command5 = Mockito.mock();
        dequeCommands.addAll(List.of(command1, command2, command3, command4, command5));

        new InitCommand().execute();
        Object iocScope = Ioc.resolve("IoC.Scope.Create", new Object[]{});
        ((Command) Ioc.resolve("IoC.Scope.Current.Set", new Object[]{iocScope})).execute();
    }

    @AfterEach
    void tearDown() {
        ((Command) Ioc.resolve("IoC.Scope.Current.Clear", null)).execute();
    }

    @Test
    public void eventLoopStartedTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Command threadCountDownCommand = Ioc.resolve("Thread.Count.Down", new Object[]{latch});
        dequeCommands.add(threadCountDownCommand);

        EventLoop eventLoop = new EventLoop(dequeCommands);
        ((Command) Ioc.resolve("EventLoop.Start", new Object[]{eventLoop})).execute();

        latch.await();

        assertEquals(0, dequeCommands.size());
    }

    @Test
    public void afterCommandHardStopThreadIsStoppedTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        EventLoop eventLoop = new EventLoop(dequeCommands);

        Command threadCountDownCommand = Ioc.resolve("Thread.Count.Down", new Object[]{latch});
        Command hardStopCommand = Ioc.resolve("EventLoop.Hard.Stop", new Object[]{eventLoop});
        Command command6 = Mockito.mock();
        Command command7 = Mockito.mock();
        Command command8 = Mockito.mock();
        dequeCommands.addAll(List.of(threadCountDownCommand, hardStopCommand, command6, command7, command8));

        ((Command) Ioc.resolve("EventLoop.Start", new Object[]{eventLoop})).execute();

        latch.await();

        assertEquals(3, dequeCommands.size());
    }

    @Test
    public void afterCommandSoftStopThreadIsStoppedTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        EventLoop eventLoop = new EventLoop(dequeCommands);

        Command threadCountDownCommand = Ioc.resolve("Thread.Count.Down", new Object[]{latch});
        Command softStopCommand = Ioc.resolve("EventLoop.Soft.Stop", new Object[]{eventLoop});
        Command command6 = Mockito.mock();
        Command command7 = Mockito.mock();
        Command command8 = Mockito.mock();
        dequeCommands.addAll(List.of(softStopCommand, command6, command7, command8, threadCountDownCommand));

        ((Command) Ioc.resolve("EventLoop.Start", new Object[]{eventLoop})).execute();

        latch.await();

        assertEquals(0, dequeCommands.size());
    }

}