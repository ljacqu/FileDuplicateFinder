package ch.jalu.fileduplicatefinder.tree;

import java.util.Locale;

public enum TreeDisplayMode {

    ALL,

    DIRECTORIES,

    FILES;

    // todo: Integrate ability to have shortcuts
    public static TreeDisplayMode fromString(String text) {
        if (text.length() == 1) {
            switch (text.toLowerCase(Locale.ROOT)) {
                case "a": return ALL;
                case "d": return DIRECTORIES;
                case "f": return FILES;
            }
        }

        String nameUpper = text.toUpperCase(Locale.ROOT);
        return TreeDisplayMode.valueOf(nameUpper);
    }
}
