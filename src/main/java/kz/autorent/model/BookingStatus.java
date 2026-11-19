package kz.autorent.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Брондың өмірлік циклі:
 * PENDING -> CONFIRMED -> COMPLETED
 * PENDING/CONFIRMED -> CANCELLED
 */
public enum BookingStatus {
    PENDING("Күтуде"),
    CONFIRMED("Расталған"),
    COMPLETED("Аяқталған"),
    CANCELLED("Бас тартылған");

    /** Көлікті "алып тұратын" статустар — күндердің қиылысуын тексергенде қолданылады. */
    public static final Set<BookingStatus> ACTIVE = EnumSet.of(PENDING, CONFIRMED);

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
