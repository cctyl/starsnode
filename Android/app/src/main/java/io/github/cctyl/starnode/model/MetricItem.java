package io.github.cctyl.starnode.model;

public class MetricItem {
    private String icon;
    private String value;
    private String label;
    private String detail;
    private double percentage;
    private String color;

    public MetricItem(String icon, String value, String label, String detail, double percentage, String color) {
        this.icon = icon;
        this.value = value;
        this.label = label;
        this.detail = detail;
        this.percentage = percentage;
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}