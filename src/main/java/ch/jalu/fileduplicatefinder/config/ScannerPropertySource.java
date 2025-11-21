package ch.jalu.fileduplicatefinder.config;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ScannerPropertySource {

    private final Scanner scanner;
    private final Map<String, String> scannerInputs = new HashMap<>();

    public ScannerPropertySource(Scanner scanner) {
        this.scanner = scanner;
    }

    public @Nullable String getValue(String path) {
        return scannerInputs.get(path);
    }

    public String promptStringAndRegister(String path) {
        String valueFromScanner = scanner.nextLine();
        scannerInputs.put(path, valueFromScanner);
        return valueFromScanner;
    }
}
