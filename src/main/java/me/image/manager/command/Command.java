package me.image.manager.command;

@FunctionalInterface
public interface Command<T> {
    void execute(T context);
}
