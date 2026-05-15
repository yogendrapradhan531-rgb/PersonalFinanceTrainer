package com.finance.ui;

import com.finance.model.*;
import com.finance.service.FinanceService;
import com.finance.util.UIConstants;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BudgetPanel extends JPanel {

    private final FinanceService service;
    private JPanel budgetListPanel;

    public BudgetPanel(FinanceService service) {
        this.service = service;
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        top.add(UIConstants.label("Budget Manager", UIConstants.FONT_TITLE, UIConstants.TEXT_PRIMARY), BorderLayout.WEST);

        JButton addBtn = UIConstants.primaryButton("+ Set Budget", UIConstants.ACCENT_PURPLE);
        addBtn.addActionListener(e -> showAddBudgetDialog());
        top.add(addBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        budgetListPanel = new JPanel();
        budgetListPanel.setLayout(new BoxLayout(budgetListPanel, BoxLayout.Y_AXIS));
        budgetListPanel.setBackground(UIConstants.BG_DARK);
        budgetListPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(budgetListPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        add(scroll, BorderLayout.CENTER);

        refreshBudgets();
    }

    private void refreshBudgets() {
        budgetListPanel.removeAll();

        List<Budget> budgets = service.getBudgets();
        if (budgets.isEmpty()) {
            budgetListPanel.add(UIConstants.label("No budgets set yet. Click '+ Set Budget' to add one.",
                UIConstants.FONT_BODY, UIConstants.TEXT_MUTED));
        }

        for (Budget b : budgets) {
            budgetListPanel.add(buildBudgetCard(b));
            budgetListPanel.add(Box.createVerticalStrut(12));
        }

        budgetListPanel.revalidate();
        budgetListPanel.repaint();
    }

    private JPanel buildBudgetCard(Budget b) {
        Color statusColor = b.getStatus() == Budget.BudgetStatus.EXCEEDED ? UIConstants.ACCENT_RED :
                            b.getStatus() == Budget.BudgetStatus.WARNING   ? UIConstants.ACCENT_ORANGE :
                            UIConstants.ACCENT_GREEN;

        JPanel card = new JPanel(new BorderLayout(12, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // left accent bar
                g2.setColor(statusColor);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left: icon + name
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        left.add(UIConstants.label(b.getCategory().getIcon() + "  " + b.getCategory().getName(),
            UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY));
        left.add(UIConstants.label(String.format("Spent ₹%,.0f of ₹%,.0f", b.getSpent(), b.getMonthlyLimit()),
            UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));

        // Center: progress bar
        double pct = Math.min(b.getPercentUsed(), 100);
        JPanel barContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = 10;
                int y = (getHeight() - h) / 2;
                g2.setColor(UIConstants.BORDER);
                g2.fillRoundRect(0, y, getWidth(), h, h, h);
                g2.setColor(statusColor);
                g2.fillRoundRect(0, y, (int)(getWidth() * pct / 100), h, h, h);
                g2.dispose();
            }
        };
        barContainer.setOpaque(false);
        barContainer.setPreferredSize(new Dimension(0, 30));

        // Right: pct + actions
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 4));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(80, 50));
        right.add(UIConstants.label(String.format("%.0f%%", pct), UIConstants.FONT_NUM, statusColor));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btns.setOpaque(false);
        JButton edit = UIConstants.ghostButton("✏");
        JButton del  = UIConstants.ghostButton("🗑");
        edit.setFont(new Font("SansSerif", Font.PLAIN, 14));
        del.setFont(new Font("SansSerif", Font.PLAIN, 14));
        edit.addActionListener(e -> editBudget(b));
        del.addActionListener(e  -> {
            service.removeBudget(b);
            refreshBudgets();
        });
        btns.add(edit); btns.add(del);
        right.add(btns);

        card.add(left,         BorderLayout.WEST);
        card.add(barContainer, BorderLayout.CENTER);
        card.add(right,        BorderLayout.EAST);
        return card;
    }

    private void editBudget(Budget b) {
        String input = JOptionPane.showInputDialog(this,
            "New monthly limit for " + b.getCategory().getName() + " (₹):",
            String.format("%.0f", b.getMonthlyLimit()));
        if (input != null) {
            try {
                b.setMonthlyLimit(Double.parseDouble(input.trim()));
                refreshBudgets();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount");
            }
        }
    }

    private void showAddBudgetDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Set Budget", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(360, 230);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UIConstants.BG_CARD);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Category> catBox = new JComboBox<>();
        service.getExpenseCategories().forEach(catBox::addItem);
        catBox.setBackground(UIConstants.BG_CARD2);
        catBox.setForeground(UIConstants.TEXT_PRIMARY);
        catBox.setFont(UIConstants.FONT_BODY);

        JTextField limitField = new JTextField(12);
        limitField.setBackground(UIConstants.BG_CARD2);
        limitField.setForeground(UIConstants.TEXT_PRIMARY);
        limitField.setFont(UIConstants.FONT_BODY);
        limitField.setCaretColor(UIConstants.TEXT_PRIMARY);
        limitField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.4;
        form.add(UIConstants.label("Category:", UIConstants.FONT_SUBHEAD, UIConstants.TEXT_MUTED), gc);
        gc.gridx = 1; gc.weightx = 0.6;
        form.add(catBox, gc);
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0.4;
        form.add(UIConstants.label("Monthly Limit (₹):", UIConstants.FONT_SUBHEAD, UIConstants.TEXT_MUTED), gc);
        gc.gridx = 1; gc.weightx = 0.6;
        form.add(limitField, gc);
        dlg.add(form, BorderLayout.CENTER);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        row.setOpaque(false);
        JButton cancel = UIConstants.ghostButton("Cancel");
        JButton save   = UIConstants.primaryButton("Save", UIConstants.ACCENT_PURPLE);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                Category cat = (Category) catBox.getSelectedItem();
                double limit = Double.parseDouble(limitField.getText().trim());
                service.addBudget(new Budget(cat, limit));
                refreshBudgets();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid amount");
            }
        });
        row.add(cancel); row.add(save);
        dlg.add(row, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public void refresh() {
        service.refreshBudgetSpending();
        refreshBudgets();
    }
}
