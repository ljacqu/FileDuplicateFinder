package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.output.WriterReader;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexFileRenamer extends FileRenamer {

    public RegexFileRenamer(Path folder, WriterReader logger) {
        super(folder, logger);
    }

    public Map<String, String> generateRenamingsPreview(Pattern pattern, String replacement) {
        Map<String, String> renamings = new LinkedHashMap<>();
        streamFiles()
            .forEach(file -> {
                String fileName = file.getFileName().toString();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    renamings.put(fileName, matcher.replaceAll(replacement));
                }
            });
        setRenamings(renamings);
        return renamings;
    }
}
