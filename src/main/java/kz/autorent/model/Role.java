package kz.autorent.model;

/** Жүйедегі рөлдер (RBAC). */
public enum Role {
    CLIENT("Клиент"),
    MANAGER("Менеджер"),
    ADMIN("Әкімші");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
