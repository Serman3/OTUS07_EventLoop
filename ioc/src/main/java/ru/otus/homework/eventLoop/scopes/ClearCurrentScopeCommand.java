package ru.otus.homework.eventLoop.scopes;

import ru.otus.homework.eventLoop.command.Command;

public class ClearCurrentScopeCommand implements Command {

    @Override
    public void execute() {
        InitCommand.CURRENT_SCOPES.remove();
    }
}
