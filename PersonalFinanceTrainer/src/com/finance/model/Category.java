package com.finance.model;

import java.awt.Color;

public class Category {
    private String name;
    private String icon;
    private Color color;

    public Category(String name, String icon, Color color) {
        this.name  = name;
        this.icon  = icon;
        this.color = color;
    }

    public String getName()  { return name; }
    public String getIcon()  { return icon; }
    public Color  getColor() { return color; }

    @Override
    public String toString() { return name; }
}
