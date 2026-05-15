package com.finance.ui;

import com.finance.model.*;
import com.finance.service.FinanceService;
import com.finance.util.UIConstants;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final FinanceService service;

    public DashboardPanel(FinanceService service) {
        this.service = service;
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header
        JLabel header = UIConstants.label("Dashboard", UIConstants.FONT_TITLE, UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        content.add(header);

        // Summary cards row
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cards.add(summaryCard("Total Income",  "₹" + fmt(service.getTotalIncomeThisMonth()),  UIConstants.ACCENT_GREEN,  "↑"));
        cards.add(summaryCard("Total Expenses","₹" + fmt(service.getTotalExpensesThisMonth()),UIConstants.ACCENT_RED,    "↓"));
        cards.add(summaryCard("Net Savings",   "₹" + fmt(service.getNetSavingsThisMonth()),   UIConstants.ACCENT_BLUE,   "✦"));
        content.add(cards);
        content.add(Box.createVerticalStrut(20));

        // Middle row: Expense breakdown + Budget status
        JPanel mid = new JPanel(new GridLayout(1, 2, 16, 0));
        mid.setOpaque(false);
        mid.add(expenseBreakdownCard());
        mid.add(budgetStatusCard());
        content.add(mid);
        content.add(Box.createVerticalStrut(20));

        // Tips row
        content.add(tipsCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel summaryCard(String title, String value, Color accent, String icon) {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel iconLabel = UIConstants.label(icon, new Font("SansSerif", Font.PLAIN, 28), accent);
        JLabel titleLabel = UIConstants.label(title, UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
        JLabel valueLabel = UIConstants.label(value, UIConstants.FONT_NUM_LG, UIConstants.TEXT_PRIMARY);

        JPanel left = new JPanel(new GridLayout(3, 1, 0, 4));
        left.setOpaque(false);
        left.add(iconLabel);
        left.add(titleLabel);
        left.add(valueLabel);
        card.add(left, BorderLayout.CENTER);

        // color bar on bottom
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 4));
        bar.setOpaque(false);
        card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel expenseBreakdownCard() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        card.add(UIConstants.label("Expenses by Category", UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY), BorderLayout.NORTH);

        Map<String, Double> byCategory = service.getExpensesByCategory();
        double total = byCategory.values().stream().mapToDouble(Double::doubleValue).sum();

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        List<Category> cats = service.getCategories();

        byCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(6)
            .forEach(e -> {
                double pct = total > 0 ? (e.getValue() / total) * 100 : 0;
                Color catColor = cats.stream().filter(c -> c.getName().equals(e.getKey()))
                    .map(Category::getColor).findFirst().orElse(UIConstants.ACCENT_BLUE);

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

                JLabel nameLabel = UIConstants.label(e.getKey(), UIConstants.FONT_BODY, UIConstants.TEXT_PRIMARY);
                nameLabel.setPreferredSize(new Dimension(130, 16));

                JPanel barBg = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(UIConstants.BORDER);
                        g2.fillRoundRect(0, 4, getWidth(), 8, 6, 6);
                        g2.setColor(catColor);
                        g2.fillRoundRect(0, 4, (int)(getWidth() * pct / 100), 8, 6, 6);
                        g2.dispose();
                    }
                };
                barBg.setOpaque(false);

                JLabel pctLabel = UIConstants.label(String.format("₹%.0f", e.getValue()), UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
                pctLabel.setPreferredSize(new Dimension(65, 16));
                pctLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                row.add(nameLabel, BorderLayout.WEST);
                row.add(barBg,     BorderLayout.CENTER);
                row.add(pctLabel,  BorderLayout.EAST);
                list.add(row);
            });

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel budgetStatusCard() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.add(UIConstants.label("Budget Status", UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (Budget b : service.getBudgets()) {
            Color statusColor = b.getStatus() == Budget.BudgetStatus.EXCEEDED ? UIConstants.ACCENT_RED :
                                b.getStatus() == Budget.BudgetStatus.WARNING   ? UIConstants.ACCENT_ORANGE :
                                UIConstants.ACCENT_GREEN;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JLabel dot = UIConstants.label("●", UIConstants.FONT_BODY, statusColor);
            dot.setPreferredSize(new Dimension(16, 16));

            JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
            info.setOpaque(false);
            info.add(UIConstants.label(b.getCategory().getName(), UIConstants.FONT_SUBHEAD, UIConstants.TEXT_PRIMARY));
            info.add(UIConstants.label(String.format("₹%.0f / ₹%.0f  (%.0f%%)",
                b.getSpent(), b.getMonthlyLimit(), b.getPercentUsed()),
                UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));

            row.add(dot, BorderLayout.WEST);
            row.add(info, BorderLayout.CENTER);
            list.add(row);
        }

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel tipsCard() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        card.add(UIConstants.label("💡 Finance Tips", UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY), BorderLayout.NORTH);

        JPanel tipsList = new JPanel();
        tipsList.setOpaque(false);
        tipsList.setLayout(new BoxLayout(tipsList, BoxLayout.Y_AXIS));

        for (String tip : service.getFinancialTips()) {
            JLabel tipLabel = UIConstants.label(tip, UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
            tipLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            tipsList.add(tipLabel);
        }
        card.add(tipsList, BorderLayout.CENTER);
        return card;
    }

    private JPanel roundCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIConstants.RADIUS * 2, UIConstants.RADIUS * 2);
                g2.dispose();
            }
        };
    }

    private String fmt(double v) {
        return String.format("%,.0f", v);
    }

    public void refresh() { buildUI(); }
}
