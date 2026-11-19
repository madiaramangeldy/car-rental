package kz.autorent.model;

public enum CarStatus {
    AVAILABLE("Қолжетімді"),
    MAINTENANCE("Техникалық қызметте"),
    RETIRED("Пайдаланудан шығарылған");

    private final String label;

    CarStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
