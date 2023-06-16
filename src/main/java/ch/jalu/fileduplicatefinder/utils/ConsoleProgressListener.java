package ch.jalu.fileduplicatefinder.utils;

public class ConsoleProgressListener {

    private final int outputInterval;
    private int count;

    public ConsoleProgressListener(int outputInterval) {
        this.outputInterval = outputInterval;
    }

    public void notifyItemProcessed() {
        if ((++count & outputInterval) == 0) {
            System.out.print("  .");
        }
    }

    public int getCount() {
        return count;
    }
}
