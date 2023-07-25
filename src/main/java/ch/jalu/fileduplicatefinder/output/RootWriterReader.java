package ch.jalu.fileduplicatefinder.output;

import java.util.Scanner;

public class RootWriterReader implements WriterReader {

    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_BLACK = "\u001B[30m";
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_BLUE = "\u001B[34m";
    static final String ANSI_PURPLE = "\u001B[35m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_WHITE = "\u001B[37m";

    private final Scanner scanner;

    public RootWriterReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public TaskWriterReader createWriterReaderForTask(String context) {
        return new TaskWriterReader(this, context);
    }

    @Override
    public void printNewLine() {
        System.out.println();
    }

    @Override
    public void printLn(String text) {
        System.out.println(text);
    }

    @Override
    public void print(String text) {
        System.out.print(text);
    }

    @Override
    public void printError(String text) {
        System.out.println(ANSI_RED + text + ANSI_RESET);
    }

    @Override
    public String getNextLine() {
        return scanner.nextLine();
    }
}
