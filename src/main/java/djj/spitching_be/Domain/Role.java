package djj.spitching_be.Domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String key;

    Role(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}