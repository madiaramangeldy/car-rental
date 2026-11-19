package kz.autorent.model;

public enum ContractStatus {
    ACTIVE("Белсенді"),
    CLOSED("Жабылған"),
    TERMINATED("Бұзылған");

    private final String label;

    ContractStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
