package io.github.cctyl.starnode.model;

public class StatItem {
    private String icon;
    private String number;
    private String label;

    public StatItem(String icon, String number, String label) {
        this.icon = icon;
        this.number = number;
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}