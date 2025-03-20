package ch.jalu.fileduplicatefinder.output;

/**
 * Service for writing to console, writing logs and reading user input from the console.
 */
public interface WriterReader {

    /**
     * Prints a new line character.
     */
    void printNewLine();

    /**
     * Prints the text and ends it with a new line.
     *
     * @param text the text to print
     */
    void printLn(String text);

    /**
     * Prints the text (no new line at the end).
     *
     * @param text the text to print
     */
    void print(String text);

    /**
     * Prints the text, stylized as an error. Ends with a new line.
     *
     * @param text the text
     */
    void printError(String text);

    /**
     * Prompts the user for input and returns it.
     *
     * @return next line the user writes
     */
    String getNextLine();

}
