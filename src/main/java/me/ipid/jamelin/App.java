/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package me.ipid.jamelin;

import java.util.*;

public class App {
    private Stack<Boolean> stack = new Stack<>();

    public String getGreeting() {
        stack.push(true);
        boolean fuck = stack.pop();
        return "Hello world.";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
