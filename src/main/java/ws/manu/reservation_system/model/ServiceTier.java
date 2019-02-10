package ws.manu.reservation_system.model;

import java.util.Arrays;

public enum ServiceTier {
    Tier1(45l),
    Tier2(60l),
    Tier3(90l);

    private Long value;

    ServiceTier(Long value) {
        this.value = value;
    }

    public static ServiceTier fromValue(Long value) {
        for (ServiceTier serviceTier : values()) {
            if (serviceTier.value == value) {
                return serviceTier;
            }
        }
        throw new IllegalArgumentException(
                "Unknown tier type " + value + ", Allowed values are " + Arrays.toString(values()));
    }

    public Long getValue() {
        return this.value;
    }
}
