package ru.otus.homework.eventLoop.config;

import ru.otus.homework.eventLoop.annotation.Bean;
import ru.otus.homework.eventLoop.annotation.Configuration;
import ru.otus.homework.eventLoop.command.*;
import ru.otus.homework.eventLoop.events.EventLoop;
import ru.otus.homework.eventLoop.exceptionHandler.ExceptionHandler;
import ru.otus.homework.eventLoop.exceptionHandler.LoggingThrowingCommand;
import ru.otus.homework.eventLoop.ioc.Ioc;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

@Configuration
public class Config {

    @Bean
    public void exceptionHandler() {
        Function<Object[], Object> function = (arg) -> {
            Command command = (Command) arg[0];
            Throwable throwable = (Throwable) arg[1];

            ExceptionHandler.register(
                    command.getClass(),
                    throwable.getClass(),
                    (cmd, ex) -> new LoggingThrowingCommand(ex)
            );
            return ExceptionHandler.handle(command, throwable);
        };
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"ExceptionHandler", function})).execute();
    }

    @Bean
    public void eventLoopStart() {
        Function<Object[], Object> function = (args) -> new StartEventLoopCommand((EventLoop) args[0]);
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"EventLoop.Start", function})).execute();
    }

    @Bean
    public void eventLoopHardStop() {
        Function<Object[], Object> function = (args) -> new HardStopCommand((EventLoop) args[0]);
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"EventLoop.Hard.Stop", function})).execute();
    }

    @Bean
    public void eventLoopSoftStop() {
        Function<Object[], Object> function = (args) -> new SoftStopCommand((EventLoop) args[0]);
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"EventLoop.Soft.Stop", function})).execute();
    }

    @Bean
    public void threadCountDown() {
        Function<Object[], Object> function = (args) -> new ThreadCountDownCommand((CountDownLatch) args[0]);
        ((Command) Ioc.resolve("IoC.Register", new Object[]{"Thread.Count.Down", function})).execute();
    }

}
