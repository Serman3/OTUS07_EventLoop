package ru.otus.homework.eventLoop.scopes;

import ru.otus.homework.eventLoop.command.Command;

public class SetCurrentScopeCommand implements Command {

    private final Object scope;

    public SetCurrentScopeCommand(Object scope) {
        this.scope = scope;
    }

    @Override
    public void execute() {
        InitCommand.CURRENT_SCOPES.set(scope);
    }
}
