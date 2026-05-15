package com.finance.ui;

import com.finance.model.*;
import com.finance.service.FinanceService;
import com.finance.util.UIConstants;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsPanel extends JPanel {

    private final FinanceService service;

    public ReportsPanel(FinanceService service) {
        this.service = service;
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        top.add(UIConstants.label("Reports & Analytics", UIConstants.FONT_TITLE, UIConstants.TEXT_PRIMARY), BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(2, 2, 16, 16));
        content.setBackground(UIConstants.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        content.add(buildPieChartCard());
        content.add(buildMonthlyBarCard());
        content.add(buildSummaryTableCard());
        content.add(buildSavingsRateCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        add(scroll, BorderLayout.CENTER);

        revalidate(); repaint();
    }

    // ─── Pie Chart ───────────────────────────────────────────────────────────

    private JPanel buildPieChartCard() {
        JPanel card = roundCard("Expense Distribution");
        Map<String, Double> data = service.getExpensesByCategory();
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        List<Category> cats = service.getCategories();

        JPanel chartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (data.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = Math.min(getWidth(), getHeight()) - 30;
                int x = (getWidth() - s) / 2, y = (getHeight() - s) / 2;
                double start = 0;
                List<Map.Entry<String, Double>> sorted = data.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
                for (Map.Entry<String, Double> e : sorted) {
                    double arc = (e.getValue() / total) * 360;
                    Color c = cats.stream().filter(cat -> cat.getName().equals(e.getKey()))
                        .map(Category::getColor).findFirst().orElse(UIConstants.ACCENT_BLUE);
                    g2.setColor(c);
                    g2.fillArc(x, y, s, s, (int) start, (int) arc);
                    start += arc;
                }
                // center hole
                g2.setColor(UIConstants.BG_CARD);
                g2.fillOval(x + s/4, y + s/4, s/2, s/2);
                g2.setColor(UIConstants.TEXT_PRIMARY);
                g2.setFont(UIConstants.FONT_SMALL);
                String totalStr = "₹" + String.format("%,.0f", total);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(totalStr, getWidth()/2 - fm.stringWidth(totalStr)/2, getHeight()/2 + 4);
                g2.dispose();
            }
        };
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(0, 180));

        // Legend
        JPanel legend = new JPanel(new GridLayout(0, 2, 4, 4));
        legend.setOpaque(false);
        data.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(6)
            .forEach(e -> {
                Color c = cats.stream().filter(cat -> cat.getName().equals(e.getKey()))
                    .map(Category::getColor).findFirst().orElse(UIConstants.ACCENT_BLUE);
                JPanel legItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                legItem.setOpaque(false);
                JPanel dot = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(c);
                        g2.fillOval(0, 1, 10, 10);
                        g2.dispose();
                    }
                };
                dot.setPreferredSize(new Dimension(10, 12));
                dot.setOpaque(false);
                legItem.add(dot);
                legItem.add(UIConstants.label(e.getKey(), UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));
                legend.add(legItem);
            });

        card.add(chartPanel, BorderLayout.CENTER);
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    // ─── Monthly Bar Chart (last 6 months) ───────────────────────────────────

    private JPanel buildMonthlyBarCard() {
        JPanel card = roundCard("Monthly Cash Flow");

        LocalDate now = LocalDate.now();
        String[] labels = new String[6];
        double[] incomes  = new double[6];
        double[] expenses = new double[6];

        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            int idx = 5 - i;
            labels[idx]   = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int finalI = i;
            incomes[idx]  = service.getAllTransactions().stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME
                          && t.getDate().getYear() == m.getYear()
                          && t.getDate().getMonthValue() == m.getMonthValue())
                .mapToDouble(Transaction::getAmount).sum();
            expenses[idx] = service.getAllTransactions().stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE
                          && t.getDate().getYear() == m.getYear()
                          && t.getDate().getMonthValue() == m.getMonthValue())
                .mapToDouble(Transaction::getAmount).sum();
        }

        double max = 0;
        for (int i = 0; i < 6; i++) max = Math.max(max, Math.max(incomes[i], expenses[i]));
        final double finalMax = max == 0 ? 1 : max;
        final String[] fLabels = labels;
        final double[] fInc = incomes, fExp = expenses;

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pad = 30, bw = 14, gap = (getWidth() - pad*2) / 6;
                int chartH = getHeight() - pad - 20;
                g2.setColor(UIConstants.BORDER);
                g2.drawLine(pad, pad, pad, getHeight() - 20);
                g2.drawLine(pad, getHeight() - 20, getWidth() - 10, getHeight() - 20);
                for (int i = 0; i < 6; i++) {
                    int cx = pad + gap * i + gap / 2;
                    int ih = (int)((fInc[i] / finalMax) * chartH);
                    int eh = (int)((fExp[i] / finalMax) * chartH);
                    g2.setColor(UIConstants.ACCENT_GREEN);
                    g2.fillRoundRect(cx - bw - 2, getHeight() - 20 - ih, bw, ih, 4, 4);
                    g2.setColor(UIConstants.ACCENT_RED);
                    g2.fillRoundRect(cx + 2, getHeight() - 20 - eh, bw, eh, 4, 4);
                    g2.setColor(UIConstants.TEXT_MUTED);
                    g2.setFont(UIConstants.FONT_SMALL);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(fLabels[i], cx - fm.stringWidth(fLabels[i])/2, getHeight() - 6);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);

        // Legend
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        leg.setOpaque(false);
        leg.add(colorDot(UIConstants.ACCENT_GREEN, "Income"));
        leg.add(colorDot(UIConstants.ACCENT_RED,   "Expenses"));

        card.add(chart, BorderLayout.CENTER);
        card.add(leg,   BorderLayout.SOUTH);
        return card;
    }

    // ─── Summary Table ────────────────────────────────────────────────────────

    private JPanel buildSummaryTableCard() {
        JPanel card = roundCard("This Month Summary");

        double income  = service.getTotalIncomeThisMonth();
        double expense = service.getTotalExpensesThisMonth();
        double savings = service.getNetSavingsThisMonth();
        double rate    = income > 0 ? (savings / income) * 100 : 0;

        Object[][] rows = {
            {"💰 Total Income",   "₹" + String.format("%,.2f", income),  UIConstants.ACCENT_GREEN},
            {"💸 Total Expenses", "₹" + String.format("%,.2f", expense), UIConstants.ACCENT_RED},
            {"🏦 Net Savings",    "₹" + String.format("%,.2f", savings), savings >= 0 ? UIConstants.ACCENT_BLUE : UIConstants.ACCENT_RED},
            {"📊 Savings Rate",   String.format("%.1f%%", rate),          rate >= 20 ? UIConstants.ACCENT_GREEN : UIConstants.ACCENT_ORANGE},
            {"📝 Transactions",   String.valueOf(service.getAllTransactions().size()), UIConstants.TEXT_MUTED},
        };

        JPanel table = new JPanel(new GridLayout(rows.length, 1, 0, 8));
        table.setOpaque(false);

        for (Object[] row : rows) {
            JPanel r = new JPanel(new BorderLayout());
            r.setOpaque(false);
            r.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            r.add(UIConstants.label((String)row[0], UIConstants.FONT_BODY, UIConstants.TEXT_PRIMARY), BorderLayout.WEST);
            r.add(UIConstants.label((String)row[1], UIConstants.FONT_SUBHEAD, (Color)row[2]), BorderLayout.EAST);
            table.add(r);
        }

        card.add(table, BorderLayout.CENTER);
        return card;
    }

    // ─── Savings Rate Gauge ───────────────────────────────────────────────────

    private JPanel buildSavingsRateCard() {
        JPanel card = roundCard("Financial Health Score");

        double income  = service.getTotalIncomeThisMonth();
        double expense = service.getTotalExpensesThisMonth();
        double savings = service.getNetSavingsThisMonth();
        double rate    = income > 0 ? (savings / income) * 100 : 0;
        int score      = computeScore(rate, income, expense);

        JPanel gauge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = getHeight() - 20;
                int r = Math.min(cx - 20, cy - 10);
                g2.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(UIConstants.BORDER);
                g2.drawArc(cx - r, cy - r, r*2, r*2, 0, 180);
                Color scoreColor = score >= 80 ? UIConstants.ACCENT_GREEN :
                                   score >= 55 ? UIConstants.ACCENT_BLUE  :
                                   score >= 35 ? UIConstants.ACCENT_ORANGE : UIConstants.ACCENT_RED;
                g2.setColor(scoreColor);
                g2.drawArc(cx - r, cy - r, r*2, r*2, 180, (int)(score / 100.0 * 180));
                g2.setColor(UIConstants.TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 32));
                String s = String.valueOf(score);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s, cx - fm.stringWidth(s)/2, cy - 10);
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.setFont(UIConstants.FONT_SMALL);
                String label = score >= 80 ? "Excellent" : score >= 55 ? "Good" : score >= 35 ? "Fair" : "Needs Work";
                g2.drawString(label, cx - g2.getFontMetrics().stringWidth(label)/2, cy + 16);
                g2.dispose();
            }
        };
        gauge.setPreferredSize(new Dimension(0, 160));
        gauge.setOpaque(false);

        JPanel tips = new JPanel(new GridLayout(0, 1, 0, 4));
        tips.setOpaque(false);
        if (rate < 10)  tips.add(UIConstants.label("• Aim to save at least 20% of income", UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));
        if (rate >= 20) tips.add(UIConstants.label("• Good savings rate! Consider investing surplus", UIConstants.FONT_SMALL, UIConstants.ACCENT_GREEN));
        service.getBudgets().stream().filter(b -> b.getStatus() == Budget.BudgetStatus.EXCEEDED)
            .forEach(b -> tips.add(UIConstants.label("• Over budget: " + b.getCategory().getName(), UIConstants.FONT_SMALL, UIConstants.ACCENT_RED)));
        if (tips.getComponentCount() == 0)
            tips.add(UIConstants.label("• All budgets on track! 🎉", UIConstants.FONT_SMALL, UIConstants.ACCENT_GREEN));

        card.add(gauge, BorderLayout.CENTER);
        card.add(tips,  BorderLayout.SOUTH);
        return card;
    }

    private int computeScore(double savingsRate, double income, double expense) {
        int score = 50;
        if (savingsRate >= 30) score += 30;
        else if (savingsRate >= 20) score += 20;
        else if (savingsRate >= 10) score += 10;
        else score -= 10;
        long exceeded = service.getBudgets().stream()
            .filter(b -> b.getStatus() == Budget.BudgetStatus.EXCEEDED).count();
        score -= (int)(exceeded * 10);
        return Math.max(0, Math.min(100, score));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private JPanel roundCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.add(UIConstants.label(title, UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY), BorderLayout.NORTH);
        return card;
    }

    private JPanel colorDot(Color c, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                ((Graphics2D)g).setColor(c);
                ((Graphics2D)g).fillOval(0, 1, 10, 10);
            }
        };
        dot.setPreferredSize(new Dimension(10, 12));
        dot.setOpaque(false);
        p.add(dot);
        p.add(UIConstants.label(label, UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));
        return p;
    }

    public void refresh() { buildUI(); }
}
