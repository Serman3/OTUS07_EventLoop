package ru.otus.homework.eventLoop.adapter;

public interface AdapterFactory<T> {

    T create(Object obj);
}
