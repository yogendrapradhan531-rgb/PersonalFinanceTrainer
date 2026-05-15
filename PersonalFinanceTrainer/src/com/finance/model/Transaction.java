package com.finance.model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private String id;
    private String description;
    private double amount;
    private Type type;
    private Category category;
    private LocalDate date;
    private String notes;

    public Transaction(String description, double amount, Type type, Category category, LocalDate date) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.notes = "";
    }

    // Getters & Setters
    public String getId()               { return id; }
    public String getDescription()      { return description; }
    public void setDescription(String d){ this.description = d; }
    public double getAmount()           { return amount; }
    public void setAmount(double a)     { this.amount = a; }
    public Type getType()               { return type; }
    public void setType(Type t)         { this.type = t; }
    public Category getCategory()       { return category; }
    public void setCategory(Category c) { this.category = c; }
    public LocalDate getDate()          { return date; }
    public void setDate(LocalDate d)    { this.date = d; }
    public String getNotes()            { return notes; }
    public void setNotes(String n)      { this.notes = n; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | ₹%.2f | %s", 
            date, type, category.getName(), amount, description);
    }
}
