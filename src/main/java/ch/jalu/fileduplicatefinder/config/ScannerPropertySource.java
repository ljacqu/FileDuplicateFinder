package ch.jalu.fileduplicatefinder.config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ScannerPropertySource {

    private final Scanner scanner;
    private final Map<String, String> scannerInputs = new HashMap<>();

    public ScannerPropertySource(Scanner scanner) {
        this.scanner = scanner;
    }

    @Nullable
    public String getValue(String path) {
        return scannerInputs.get(path);
    }

    public String promptStringAndRegister(String path) {
        String valueFromScanner = scanner.nextLine();
        scannerInputs.put(path, valueFromScanner);
        return valueFromScanner;
    }
}
