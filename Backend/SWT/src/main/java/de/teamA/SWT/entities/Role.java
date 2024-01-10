package de.teamA.SWT.entities;

public enum Role {

    ROLE_STUDENT("student"), ROLE_STAFF("staff"), ROLE_ADMIN("admin");

    private String simpleName;

    Role(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getSimpleName() {
        return this.simpleName;
    }

}
