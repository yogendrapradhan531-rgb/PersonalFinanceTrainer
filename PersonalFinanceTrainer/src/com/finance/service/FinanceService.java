package com.finance.service;

import com.finance.model.*;
import java.awt.Color;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceService {

    private List<Transaction>  transactions = new ArrayList<>();
    private List<Budget>       budgets      = new ArrayList<>();
    private List<SavingsGoal>  goals        = new ArrayList<>();
    private List<Category>     categories   = new ArrayList<>();

    public FinanceService() {
        initDefaultCategories();
        initDefaultBudgets();
        loadSampleData();
    }

    // ─── Categories ──────────────────────────────────────────────────────────

    private void initDefaultCategories() {
        categories.add(new Category("Food & Dining",    "🍽", new Color(0xFF6B6B)));
        categories.add(new Category("Transportation",   "🚗", new Color(0x4ECDC4)));
        categories.add(new Category("Housing",          "🏠", new Color(0x45B7D1)));
        categories.add(new Category("Entertainment",    "🎬", new Color(0x96CEB4)));
        categories.add(new Category("Healthcare",       "💊", new Color(0xFECEA8)));
        categories.add(new Category("Shopping",         "🛍", new Color(0xFF9F43)));
        categories.add(new Category("Education",        "📚", new Color(0xA29BFE)));
        categories.add(new Category("Utilities",        "💡", new Color(0xFD79A8)));
        categories.add(new Category("Salary",           "💼", new Color(0x00B894)));
        categories.add(new Category("Freelance",        "💻", new Color(0x00CEC9)));
        categories.add(new Category("Investment",       "📈", new Color(0x6C5CE7)));
        categories.add(new Category("Other",            "📦", new Color(0xB2BEC3)));
    }

    private void initDefaultBudgets() {
        getCategoryByName("Food & Dining")  .ifPresent(c -> budgets.add(new Budget(c, 8000)));
        getCategoryByName("Transportation") .ifPresent(c -> budgets.add(new Budget(c, 3000)));
        getCategoryByName("Entertainment")  .ifPresent(c -> budgets.add(new Budget(c, 2000)));
        getCategoryByName("Shopping")       .ifPresent(c -> budgets.add(new Budget(c, 5000)));
        getCategoryByName("Utilities")      .ifPresent(c -> budgets.add(new Budget(c, 2500)));
        getCategoryByName("Healthcare")     .ifPresent(c -> budgets.add(new Budget(c, 1500)));
    }

    private void loadSampleData() {
        LocalDate today = LocalDate.now();
        LocalDate m = today.withDayOfMonth(1);

        addTransaction(new Transaction("Monthly Salary",       85000, Transaction.Type.INCOME,  cat("Salary"),         m.plusDays(0)));
        addTransaction(new Transaction("Freelance Project",    12000, Transaction.Type.INCOME,  cat("Freelance"),      m.plusDays(5)));
        addTransaction(new Transaction("Groceries - BigBasket", 3200, Transaction.Type.EXPENSE, cat("Food & Dining"),  m.plusDays(2)));
        addTransaction(new Transaction("Zomato Order",          850,  Transaction.Type.EXPENSE, cat("Food & Dining"),  m.plusDays(4)));
        addTransaction(new Transaction("Petrol",               2100,  Transaction.Type.EXPENSE, cat("Transportation"), m.plusDays(3)));
        addTransaction(new Transaction("Ola/Uber rides",        650,  Transaction.Type.EXPENSE, cat("Transportation"), m.plusDays(7)));
        addTransaction(new Transaction("Netflix + Spotify",     750,  Transaction.Type.EXPENSE, cat("Entertainment"),  m.plusDays(1)));
        addTransaction(new Transaction("Movie tickets",          600,  Transaction.Type.EXPENSE, cat("Entertainment"),  m.plusDays(9)));
        addTransaction(new Transaction("Electricity Bill",     1800,  Transaction.Type.EXPENSE, cat("Utilities"),      m.plusDays(6)));
        addTransaction(new Transaction("Amazon Shopping",      2400,  Transaction.Type.EXPENSE, cat("Shopping"),       m.plusDays(8)));
        addTransaction(new Transaction("Doctor visit",          900,  Transaction.Type.EXPENSE, cat("Healthcare"),     m.plusDays(10)));
        addTransaction(new Transaction("Udemy Course",          799,  Transaction.Type.EXPENSE, cat("Education"),      m.plusDays(11)));

        SavingsGoal g1 = new SavingsGoal("Emergency Fund",   100000, today.plusMonths(6), "3-month expense cushion");
        g1.setCurrentAmount(35000);
        SavingsGoal g2 = new SavingsGoal("New Laptop",        80000, today.plusMonths(4), "MacBook for work");
        g2.setCurrentAmount(22000);
        SavingsGoal g3 = new SavingsGoal("Vacation - Goa",    30000, today.plusMonths(3), "Beach trip with family");
        g3.setCurrentAmount(9500);
        goals.add(g1); goals.add(g2); goals.add(g3);
    }

    private Category cat(String name) {
        return getCategoryByName(name).orElse(categories.get(categories.size() - 1));
    }

    // ─── Transaction CRUD ────────────────────────────────────────────────────

    public void addTransaction(Transaction t) {
        transactions.add(t);
        refreshBudgetSpending();
    }

    public void removeTransaction(Transaction t) {
        transactions.remove(t);
        refreshBudgetSpending();
    }

    public void updateTransaction(Transaction t) {
        refreshBudgetSpending();
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getTransactionsByMonth(int year, int month) {
        return transactions.stream()
            .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
            .sorted(Comparator.comparing(Transaction::getDate).reversed())
            .collect(Collectors.toList());
    }

    public List<Transaction> getExpenses() {
        return transactions.stream()
            .filter(t -> t.getType() == Transaction.Type.EXPENSE)
            .collect(Collectors.toList());
    }

    // ─── Budget ──────────────────────────────────────────────────────────────

    public List<Budget> getBudgets() { return Collections.unmodifiableList(budgets); }

    public void addBudget(Budget b)    { budgets.add(b); }
    public void removeBudget(Budget b) { budgets.remove(b); }

    public void refreshBudgetSpending() {
        LocalDate now = LocalDate.now();
        for (Budget b : budgets) {
            double spent = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE
                          && t.getCategory().getName().equals(b.getCategory().getName())
                          && t.getDate().getYear()       == now.getYear()
                          && t.getDate().getMonthValue() == now.getMonthValue())
                .mapToDouble(Transaction::getAmount).sum();
            b.setSpent(spent);
        }
    }

    // ─── Goals ───────────────────────────────────────────────────────────────

    public List<SavingsGoal> getGoals()   { return Collections.unmodifiableList(goals); }
    public void addGoal(SavingsGoal g)    { goals.add(g); }
    public void removeGoal(SavingsGoal g) { goals.remove(g); }

    // ─── Summary Stats ───────────────────────────────────────────────────────

    public double getTotalIncomeThisMonth() {
        LocalDate now = LocalDate.now();
        return transactions.stream()
            .filter(t -> t.getType() == Transaction.Type.INCOME
                      && t.getDate().getYear()       == now.getYear()
                      && t.getDate().getMonthValue() == now.getMonthValue())
            .mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalExpensesThisMonth() {
        LocalDate now = LocalDate.now();
        return transactions.stream()
            .filter(t -> t.getType() == Transaction.Type.EXPENSE
                      && t.getDate().getYear()       == now.getYear()
                      && t.getDate().getMonthValue() == now.getMonthValue())
            .mapToDouble(Transaction::getAmount).sum();
    }

    public double getNetSavingsThisMonth() {
        return getTotalIncomeThisMonth() - getTotalExpensesThisMonth();
    }

    public Map<String, Double> getExpensesByCategory() {
        LocalDate now = LocalDate.now();
        return transactions.stream()
            .filter(t -> t.getType() == Transaction.Type.EXPENSE
                      && t.getDate().getYear()       == now.getYear()
                      && t.getDate().getMonthValue() == now.getMonthValue())
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.summingDouble(Transaction::getAmount)));
    }

    public List<String> getFinancialTips() {
        List<String> tips = new ArrayList<>();
        double income   = getTotalIncomeThisMonth();
        double expenses = getTotalExpensesThisMonth();
        double savings  = getNetSavingsThisMonth();

        if (income > 0) {
            double savingsRate = (savings / income) * 100;
            if (savingsRate < 20)
                tips.add("💡 Try to save at least 20% of your income. Currently saving " + String.format("%.1f%%", savingsRate));
            else
                tips.add("✅ Great savings rate! You're saving " + String.format("%.1f%%", savingsRate) + " this month.");
        }

        for (Budget b : budgets) {
            if (b.getStatus() == Budget.BudgetStatus.EXCEEDED)
                tips.add("⚠️ You've exceeded your " + b.getCategory().getName() + " budget by ₹" + String.format("%.0f", -b.getRemaining()));
            else if (b.getStatus() == Budget.BudgetStatus.WARNING)
                tips.add("🔔 " + b.getCategory().getName() + " budget is 80%+ used. ₹" + String.format("%.0f", b.getRemaining()) + " remaining.");
        }

        Map<String, Double> catExp = getExpensesByCategory();
        catExp.entrySet().stream()
            .filter(e -> e.getValue() > 5000)
            .forEach(e -> tips.add("📊 High spending on " + e.getKey() + ": ₹" + String.format("%.0f", e.getValue())));

        if (tips.isEmpty()) tips.add("👍 Your finances look healthy this month! Keep it up.");
        return tips;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public List<Category> getCategories()    { return Collections.unmodifiableList(categories); }
    public List<Category> getExpenseCategories() {
        return categories.stream()
            .filter(c -> !c.getName().equals("Salary") && !c.getName().equals("Freelance") && !c.getName().equals("Investment"))
            .collect(Collectors.toList());
    }
    public List<Category> getIncomeCategories() {
        return categories.stream()
            .filter(c -> c.getName().equals("Salary") || c.getName().equals("Freelance") || c.getName().equals("Investment"))
            .collect(Collectors.toList());
    }

    private Optional<Category> getCategoryByName(String name) {
        return categories.stream().filter(c -> c.getName().equals(name)).findFirst();
    }
}
