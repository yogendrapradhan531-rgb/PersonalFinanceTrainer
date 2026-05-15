# 💰 Personal Finance Trainer

A full-featured personal finance management desktop app built with Java Swing.

---

## Features

| Module         | What it does                                                      |
|----------------|-------------------------------------------------------------------|
| **Dashboard**  | Monthly income/expense summary cards, budget status, finance tips |
| **Transactions** | Add, view, and delete income & expense transactions            |
| **Budgets**    | Set monthly spending limits per category with visual progress bars|
| **Goals**      | Create savings goals with target dates and fund tracking          |
| **Reports**    | Pie chart, bar chart, summary table, and financial health score   |

---

## Project Structure

```
PersonalFinanceTrainer/
├── .project              ← Eclipse project descriptor
├── .classpath            ← Eclipse build config
└── src/
    └── com/finance/
        ├── Main.java                     ← Entry point
        ├── model/
        │   ├── Transaction.java
        │   ├── Category.java
        │   ├── Budget.java
        │   └── SavingsGoal.java
        ├── service/
        │   └── FinanceService.java       ← All business logic
        ├── ui/
        │   ├── MainFrame.java            ← App shell + sidebar nav
        │   ├── DashboardPanel.java
        │   ├── TransactionsPanel.java
        │   ├── BudgetPanel.java
        │   ├── GoalsPanel.java
        │   └── ReportsPanel.java
        └── util/
            └── UIConstants.java          ← Design tokens & helpers
```

---

## How to Import into Eclipse

1. Open **Eclipse IDE**
2. Go to **File → Import → General → Existing Projects into Workspace**
3. Click **Browse**, select the `PersonalFinanceTrainer` folder
4. Click **Finish**

## How to Run

1. In the Eclipse **Package Explorer**, expand `src/com/finance/`
2. Right-click `Main.java`
3. Select **Run As → Java Application**

> **Requires:** Java 8 or higher. No external dependencies — uses only the JDK standard library.

---

## Sample Data

The app starts with pre-loaded sample data for the current month:
- **Income:** ₹85,000 salary + ₹12,000 freelance
- **Expenses:** groceries, transport, utilities, entertainment, etc.
- **Budgets:** 6 pre-configured category budgets
- **Goals:** Emergency Fund, New Laptop, Goa Vacation

---

## Extending the App

- **Persistence:** Add JSON file I/O in `FinanceService` to save/load data between sessions
- **Export:** Add CSV export to `TransactionsPanel`
- **Recurring transactions:** Add a `RecurringTransaction` model
- **Multi-month view:** Extend `ReportsPanel` with a date range picker
