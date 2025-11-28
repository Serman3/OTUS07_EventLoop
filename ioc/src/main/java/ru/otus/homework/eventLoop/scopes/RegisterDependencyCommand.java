package ru.otus.homework.eventLoop.scopes;

import ru.otus.homework.eventLoop.command.Command;
import ru.otus.homework.eventLoop.ioc.Ioc;

import java.util.Map;
import java.util.function.Function;

public class RegisterDependencyCommand implements Command {

    private final String dependency;
    private final Function<Object[], Object> dependencyResolverStrategy;

    public RegisterDependencyCommand(String dependency, Function<Object[], Object> dependencyResolverStrategy) {
        this.dependency = dependency;
        this.dependencyResolverStrategy = dependencyResolverStrategy;
    }

    @Override
    public void execute() {
        Map<String, Function<Object[], Object>> currentScope = Ioc.resolve("IoC.Scope.Current", new Object[]{});
        currentScope.put(dependency, dependencyResolverStrategy);
    }
}
