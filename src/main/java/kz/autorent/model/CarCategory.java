package kz.autorent.model;

public enum CarCategory {
    ECONOMY("Эконом"),
    COMFORT("Комфорт"),
    BUSINESS("Бизнес"),
    SUV("Жол талғамайтын (SUV)"),
    MINIVAN("Минивэн");

    private final String label;

    CarCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
