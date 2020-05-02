package ch.jalu.fileduplicatefinder.rename;

import ch.jalu.fileduplicatefinder.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegexFileRenamer extends FileRenamer {

    private final Path folder;
    private final Pattern pattern;
    private final String replacement;
    private Map<String, String> renamings;

    public RegexFileRenamer(Path folder, Pattern pattern, String replacement) {
        super(folder);
        this.folder = checkNotNull(folder, "folder");
        this.pattern = checkNotNull(pattern, "pattern");
        this.replacement = checkNotNull(replacement, "replacement");
    }

    public Map<String, String> generateRenamingsPreview() {
        renamings = new LinkedHashMap<>();
        PathUtils.list(folder)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                String fileName = file.getFileName().toString();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    renamings.put(fileName, matcher.replaceAll(replacement));
                }
            });
        return renamings;
    }

    @Override
    protected Map<String, String> getRenamings() {
        return renamings;
    }
}
