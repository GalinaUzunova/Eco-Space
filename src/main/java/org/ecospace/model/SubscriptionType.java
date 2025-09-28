package org.ecospace.model;



public enum SubscriptionType {
    MONTHLY_MAINTENANCE("MONTHLY MAINTENANCE"),
    SIX_MONTHS_MAINTENANCE("6 MONTHS MAINTANACE"),
    ONE_YEAR_MAINTENANCE("1 YEAR MAINATNACE"),
    BASIC_2D_DESIGN("2D BASIC DESIGN"),
    PREMIUM_2D_DESIGN("2D PREMIUM DESIGN"),
    VISUALIZATION_3D_DESIGN("3D VISUALIZATION");

    private final String displayName;

    SubscriptionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
