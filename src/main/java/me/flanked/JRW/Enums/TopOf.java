package me.flanked.JRW.Enums;

public enum TopOf {
    HOUR ("hour"),
    DAY ("day"),
    WEEK ("week"),
    MONTH ("month"),
    YEAR ("year"),
    ALLTIME ("all");

    private final String value;
    TopOf (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
