package me.ipid.jamelin.exception;

public class Unreachable extends Error {
    public Unreachable() {
        super("The program should not reach here.");
    }
}
