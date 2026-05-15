package com.finance.ui;

import com.finance.model.*;
import com.finance.service.FinanceService;
import com.finance.util.UIConstants;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionsPanel extends JPanel {

    private final FinanceService service;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public TransactionsPanel(FinanceService service) {
        this.service = service;
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        JLabel title = UIConstants.label("Transactions", UIConstants.FONT_TITLE, UIConstants.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        JButton addBtn = UIConstants.primaryButton("+ Add Transaction", UIConstants.ACCENT_GREEN);
        addBtn.addActionListener(e -> showAddDialog());
        top.add(addBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        String[] cols = {"Date", "Description", "Category", "Type", "Amount"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD2);
                c.setForeground(UIConstants.TEXT_PRIMARY);
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return c;
            }
        };
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setBackground(UIConstants.BG_DARK);
        table.setSelectionBackground(new Color(0x2A, 0x3A, 0x5E));
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_SUBHEAD);
        header.setBackground(UIConstants.BG_CARD);
        header.setForeground(UIConstants.TEXT_MUTED);
        header.setReorderingAllowed(false);

        // Amount column colored by type
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String type = (String) t.getValueAt(row, 3);
                setForeground("INCOME".equals(type) ? UIConstants.ACCENT_GREEN : UIConstants.ACCENT_RED);
                setBackground(row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD2);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(UIConstants.FONT_SUBHEAD);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBackground(UIConstants.BG_DARK);
        add(scroll, BorderLayout.CENTER);

        // Delete button at bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 24, 12, 24));
        JButton delBtn = UIConstants.primaryButton("Delete Selected", UIConstants.ACCENT_RED);
        delBtn.addActionListener(e -> deleteSelected());
        bottom.add(delBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Transaction> all = service.getAllTransactions();
        all.stream()
           .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
           .forEach(t -> tableModel.addRow(new Object[]{
               t.getDate().format(FMT),
               t.getDescription(),
               t.getCategory().getName(),
               t.getType().name(),
               (t.getType() == Transaction.Type.INCOME ? "+ " : "- ") + "₹" + String.format("%,.2f", t.getAmount())
           }));
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Add Transaction", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UIConstants.BG_CARD);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField descField = styledField("Description");
        JTextField amtField  = styledField("Amount (₹)");
        JComboBox<Transaction.Type> typeBox = new JComboBox<>(Transaction.Type.values());
        styleCombo(typeBox);
        JComboBox<Category> catBox = new JComboBox<>();
        styleCombo(catBox);

        typeBox.addActionListener(e -> {
            catBox.removeAllItems();
            if (typeBox.getSelectedItem() == Transaction.Type.INCOME)
                service.getIncomeCategories().forEach(catBox::addItem);
            else
                service.getExpenseCategories().forEach(catBox::addItem);
        });
        typeBox.setSelectedItem(Transaction.Type.EXPENSE);

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setBackground(UIConstants.BG_CARD2);
        dateSpinner.setForeground(UIConstants.TEXT_PRIMARY);

        int r = 0;
        addFormRow(form, gc, r++, "Description:", descField);
        addFormRow(form, gc, r++, "Amount (₹):", amtField);
        addFormRow(form, gc, r++, "Type:", typeBox);
        addFormRow(form, gc, r++, "Category:", catBox);
        addFormRow(form, gc, r++, "Date:", dateSpinner);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRow.setOpaque(false);
        JButton cancel = UIConstants.ghostButton("Cancel");
        JButton save   = UIConstants.primaryButton("Save", UIConstants.ACCENT_GREEN);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                String desc = descField.getText().trim();
                double amt  = Double.parseDouble(amtField.getText().trim());
                Transaction.Type type = (Transaction.Type) typeBox.getSelectedItem();
                Category cat = (Category) catBox.getSelectedItem();
                java.util.Date d = (java.util.Date) dateSpinner.getValue();
                LocalDate ld = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (desc.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Description is required"); return; }
                service.addTransaction(new Transaction(desc, amt, type, cat, ld));
                refreshTable();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter a valid amount");
            }
        });
        btnRow.add(cancel);
        btnRow.add(save);
        dlg.add(btnRow, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a transaction to delete"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this transaction?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            List<Transaction> all = service.getAllTransactions().stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(java.util.stream.Collectors.toList());
            service.removeTransaction(all.get(row));
            refreshTable();
        }
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(16);
        f.setBackground(UIConstants.BG_CARD2);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.TEXT_PRIMARY);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private <T> void styleCombo(JComboBox<T> box) {
        box.setBackground(UIConstants.BG_CARD2);
        box.setForeground(UIConstants.TEXT_PRIMARY);
        box.setFont(UIConstants.FONT_BODY);
        ((JComponent) box.getRenderer()).setBackground(UIConstants.BG_CARD2);
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.3;
        p.add(UIConstants.label(label, UIConstants.FONT_SUBHEAD, UIConstants.TEXT_MUTED), gc);
        gc.gridx = 1; gc.weightx = 0.7;
        p.add(field, gc);
    }

    public void refresh() { refreshTable(); }
}
