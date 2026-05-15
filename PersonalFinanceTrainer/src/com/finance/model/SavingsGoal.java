package com.finance.model;

import java.time.LocalDate;

public class SavingsGoal {
    private String name;
    private double targetAmount;
    private double currentAmount;
    private LocalDate targetDate;
    private String description;

    public SavingsGoal(String name, double targetAmount, LocalDate targetDate, String description) {
        this.name          = name;
        this.targetAmount  = targetAmount;
        this.currentAmount = 0.0;
        this.targetDate    = targetDate;
        this.description   = description;
    }

    public String getName()               { return name; }
    public void setName(String n)         { this.name = n; }
    public double getTargetAmount()       { return targetAmount; }
    public void setTargetAmount(double a) { this.targetAmount = a; }
    public double getCurrentAmount()      { return currentAmount; }
    public void setCurrentAmount(double a){ this.currentAmount = a; }
    public LocalDate getTargetDate()      { return targetDate; }
    public void setTargetDate(LocalDate d){ this.targetDate = d; }
    public String getDescription()        { return description; }

    public double getProgress()    { return targetAmount > 0 ? (currentAmount / targetAmount) * 100 : 0; }
    public double getRemaining()   { return targetAmount - currentAmount; }
    public boolean isAchieved()    { return currentAmount >= targetAmount; }

    @Override
    public String toString() { return name + " (₹" + String.format("%.0f", targetAmount) + ")"; }
}
