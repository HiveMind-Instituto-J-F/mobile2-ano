package com.aula.mobile_hivemind.ui.dashboard.itens;

public class ProgressItem {
    private String label;
    private int progress;
    private int color;

    public ProgressItem(String label, int progress, int color) {
        this.label = label;
        this.progress = progress;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getProgress() {
        return progress;
    }

    public int getColor() {
        return color;
    }
}
