package ch.jalu.fileduplicatefinder.output;

import static ch.jalu.fileduplicatefinder.output.RootWriterReader.ANSI_BLUE;
import static ch.jalu.fileduplicatefinder.output.RootWriterReader.ANSI_RESET;

public class TaskWriterReader implements WriterReader {

    private final RootWriterReader writerReader;
    private final String prefix;

    TaskWriterReader(RootWriterReader writerReader, String context) {
        this.writerReader = writerReader;
        this.prefix = ANSI_BLUE + context + "> " + ANSI_RESET;
    }

    @Override
    public void printNewLine() {
        writerReader.printNewLine();
    }

    @Override
    public void printLn(String text) {
        writerReader.printLn(prefix + text);
    }

    @Override
    public void print(String text) {
        writerReader.print(prefix + text);
    }

    public void printWithoutPrefix(String text) {
        writerReader.print(text);
    }

    @Override
    public void printError(String text) {
        writerReader.printError(prefix + text);
    }

    @Override
    public String getNextLine() {
        return writerReader.getNextLine();
    }
}
