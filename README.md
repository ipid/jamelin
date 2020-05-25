<h1 align="center">Jamelin</h1>
<p align="center"><b>·</b> Promela interpreter, now in pure Java <b>·</b></p>
<br>

## ℹ️ What is this?

The JAMELIN project, also the **JA**va Pro**MEL**a **IN**terpreter, is a Promela interpreter that being written in pure Java, without foreign dependencies. Jamelin is taking great advantages of the Java platform, to provide high portability and reliability.

<br>

## ℹ️ Why another Promela interpreter?

SPIN, also can be called *The* Promela Interpreter, is written in C and the code is highly optimized for production use. I rewrote a new Promela interpreter in Java for educational purpose.

<br>

## ℹ️ How to compile these things?

Just run the following command in your shell:

```shell
./gradlew shadowJar
```

Then it will compile like charm.

<br>

## ℹ️ How to run the code?

First, find the location of generated `jamelin-all.jar`.

Then you need to run the jar file. I assume that you have change your working directory to where the jar file lies, so just run the following command in your shell:

```shell
java -jar jamelin-all.jar <PROMELA File Path>
```

It's that simple - you don't need those fancy command line arguments. Just input the file path and it will read your code and do the simulation.

**NOTICE:** The program output are all written in Chinese. 

<br>

## © License

The repository is released under MIT license.

<br>

