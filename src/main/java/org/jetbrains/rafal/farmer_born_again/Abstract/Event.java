package org.jetbrains.rafal.farmer_born_again.Abstract;

public interface Event {
    void execute();
    boolean isExecuted();
    boolean canExecute();
    byte selectedOption();
}
