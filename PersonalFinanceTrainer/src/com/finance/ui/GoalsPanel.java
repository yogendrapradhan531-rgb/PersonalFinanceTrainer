package com.finance.ui;

import com.finance.model.SavingsGoal;
import com.finance.service.FinanceService;
import com.finance.util.UIConstants;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoalsPanel extends JPanel {

    private final FinanceService service;
    private JPanel goalListPanel;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public GoalsPanel(FinanceService service) {
        this.service = service;
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        top.add(UIConstants.label("Savings Goals", UIConstants.FONT_TITLE, UIConstants.TEXT_PRIMARY), BorderLayout.WEST);
        JButton addBtn = UIConstants.primaryButton("+ New Goal", UIConstants.ACCENT_ORANGE);
        addBtn.addActionListener(e -> showAddGoalDialog());
        top.add(addBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        goalListPanel = new JPanel(new GridLayout(0, 2, 16, 16));
        goalListPanel.setBackground(UIConstants.BG_DARK);
        goalListPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(goalListPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        add(scroll, BorderLayout.CENTER);

        refreshGoals();
    }

    private void refreshGoals() {
        goalListPanel.removeAll();
        List<SavingsGoal> goals = service.getGoals();
        if (goals.isEmpty()) {
            goalListPanel.setLayout(new FlowLayout());
            goalListPanel.add(UIConstants.label("No goals yet. Click '+ New Goal' to create one.",
                UIConstants.FONT_BODY, UIConstants.TEXT_MUTED));
        } else {
            goalListPanel.setLayout(new GridLayout(0, 2, 16, 16));
            goals.forEach(g -> goalListPanel.add(buildGoalCard(g)));
        }
        goalListPanel.revalidate();
        goalListPanel.repaint();
    }

    private JPanel buildGoalCard(SavingsGoal g) {
        Color accent = g.isAchieved() ? UIConstants.ACCENT_GREEN :
                       g.getProgress() > 50 ? UIConstants.ACCENT_BLUE : UIConstants.ACCENT_ORANGE;

        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // top gradient band
                GradientPaint gp = new GradientPaint(0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60),
                                                     0, 60, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 60, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(UIConstants.label(g.getName(), UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY), BorderLayout.WEST);
        if (g.isAchieved()) titleRow.add(UIConstants.label("✅ Achieved!", UIConstants.FONT_SMALL, UIConstants.ACCENT_GREEN), BorderLayout.EAST);

        // Description
        JLabel desc = UIConstants.label(g.getDescription(), UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);

        // Amount row
        JPanel amounts = new JPanel(new GridLayout(1, 2));
        amounts.setOpaque(false);
        amounts.add(UIConstants.label("₹" + String.format("%,.0f", g.getCurrentAmount()), UIConstants.FONT_NUM, accent));
        JLabel target = UIConstants.label("/ ₹" + String.format("%,.0f", g.getTargetAmount()), UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
        target.setVerticalAlignment(SwingConstants.BOTTOM);
        amounts.add(target);

        // Progress bar
        double pct = g.getProgress();
        JPanel progressBar = new JPanel() {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BORDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, (int)(getWidth() * pct / 100), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 10));
        progressBar.setOpaque(false);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(UIConstants.label(String.format("%.1f%% complete", pct), UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED), BorderLayout.WEST);
        footer.add(UIConstants.label("By " + g.getTargetDate().format(FMT), UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED), BorderLayout.EAST);

        // Action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton addFunds = UIConstants.primaryButton("Add Funds", accent);
        addFunds.setPreferredSize(new Dimension(110, 30));
        addFunds.addActionListener(e -> addFunds(g));
        JButton del = UIConstants.ghostButton("Remove");
        del.addActionListener(e -> { service.removeGoal(g); refreshGoals(); });
        btns.add(addFunds); btns.add(del);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(desc);
        center.add(Box.createVerticalStrut(8));
        center.add(amounts);
        center.add(Box.createVerticalStrut(8));
        center.add(progressBar);
        center.add(Box.createVerticalStrut(6));
        center.add(footer);
        center.add(Box.createVerticalStrut(10));
        center.add(btns);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(center,   BorderLayout.CENTER);
        return card;
    }

    private void addFunds(SavingsGoal g) {
        String input = JOptionPane.showInputDialog(this, "Add amount to \"" + g.getName() + "\" (₹):");
        if (input != null) {
            try {
                double amt = Double.parseDouble(input.trim());
                g.setCurrentAmount(Math.min(g.getCurrentAmount() + amt, g.getTargetAmount()));
                refreshGoals();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount");
            }
        }
    }

    private void showAddGoalDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "New Savings Goal", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 320);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UIConstants.BG_CARD);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 4, 7, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = styledField("Goal name");
        JTextField amtField  = styledField("Target amount");
        JTextField descField = styledField("Short description");
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor de = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(de);

        int r = 0;
        addRow(form, gc, r++, "Goal Name:", nameField);
        addRow(form, gc, r++, "Target (₹):", amtField);
        addRow(form, gc, r++, "Description:", descField);
        addRow(form, gc, r++, "Target Date:", dateSpinner);
        dlg.add(form, BorderLayout.CENTER);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        row.setOpaque(false);
        JButton cancel = UIConstants.ghostButton("Cancel");
        JButton save   = UIConstants.primaryButton("Create", UIConstants.ACCENT_ORANGE);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double target = Double.parseDouble(amtField.getText().trim());
                String desc   = descField.getText().trim();
                java.util.Date d = (java.util.Date) dateSpinner.getValue();
                LocalDate ld = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (name.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Name is required"); return; }
                service.addGoal(new SavingsGoal(name, target, ld, desc));
                refreshGoals();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid amount");
            }
        });
        row.add(cancel); row.add(save);
        dlg.add(row, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JTextField styledField(String ph) {
        JTextField f = new JTextField(16);
        f.setBackground(UIConstants.BG_CARD2);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.TEXT_PRIMARY);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35;
        p.add(UIConstants.label(label, UIConstants.FONT_SUBHEAD, UIConstants.TEXT_MUTED), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        p.add(field, gc);
    }

    public void refresh() { refreshGoals(); }
}
