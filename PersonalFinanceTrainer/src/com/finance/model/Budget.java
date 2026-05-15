package com.finance.model;

public class Budget {
    private Category category;
    private double monthlyLimit;
    private double spent;

    public Budget(Category category, double monthlyLimit) {
        this.category     = category;
        this.monthlyLimit = monthlyLimit;
        this.spent        = 0.0;
    }

    public Category getCategory()         { return category; }
    public double getMonthlyLimit()       { return monthlyLimit; }
    public void setMonthlyLimit(double l) { this.monthlyLimit = l; }
    public double getSpent()              { return spent; }
    public void setSpent(double s)        { this.spent = s; }

    public double getRemaining()  { return monthlyLimit - spent; }
    public double getPercentUsed(){ return monthlyLimit > 0 ? (spent / monthlyLimit) * 100 : 0; }

    public BudgetStatus getStatus() {
        double pct = getPercentUsed();
        if (pct >= 100) return BudgetStatus.EXCEEDED;
        if (pct >= 80)  return BudgetStatus.WARNING;
        return BudgetStatus.GOOD;
    }

    public enum BudgetStatus { GOOD, WARNING, EXCEEDED }
}
