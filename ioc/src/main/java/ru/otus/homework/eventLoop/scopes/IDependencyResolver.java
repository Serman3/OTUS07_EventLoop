package ru.otus.homework.eventLoop.scopes;

public interface IDependencyResolver {

    Object resolve(String dependency, Object[] args);
}
