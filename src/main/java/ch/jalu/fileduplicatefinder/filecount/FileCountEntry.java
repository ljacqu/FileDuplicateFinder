package ch.jalu.fileduplicatefinder.filecount;

import java.math.BigDecimal;

public interface FileCountEntry {

    long getCount();

    BigDecimal getTotalSizeInBytes();

}
