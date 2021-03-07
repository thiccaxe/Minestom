package net.minestom.server.command;

public class CommandExecutionOption {

    private boolean ignoreEvent = false;
    private boolean ignorePermission = false;
    private boolean ignoreMessage = false;

    public CommandExecutionOption() {

    }

    public boolean isIgnoreEvent() {
        return ignoreEvent;
    }

    public CommandExecutionOption ignoreEvent(boolean ignoreEvent) {
        this.ignoreEvent = ignoreEvent;
        return this;
    }

    public boolean isIgnorePermission() {
        return ignorePermission;
    }

    public CommandExecutionOption ignorePermission(boolean ignorePermission) {
        this.ignorePermission = ignorePermission;
        return this;
    }

    public boolean isIgnoreMessage() {
        return ignoreMessage;
    }

    public CommandExecutionOption ignoreMessage(boolean ignoreMessage) {
        this.ignoreMessage = ignoreMessage;
        return this;
    }
}
