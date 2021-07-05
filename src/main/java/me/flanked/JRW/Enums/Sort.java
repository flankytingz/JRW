package me.flanked.JRW.Enums;

public enum Sort {
    HOT ("hot"),
    NEW ("new"),
    RISING ("rising"),
    TOP ("top");

    private final String value;
    private String of = null;
    Sort(String sort) {
        this.value = sort;
    }

    public String getValue() {
        return value;
    }

    public String getTopOfValue() {
        return of;
    }

    public Sort of(TopOf of) {
        this.of = of.getValue();
        return this;
    }
}
