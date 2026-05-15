package com.finance.ui;

import com.finance.service.FinanceService;
import com.finance.util.UIConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    private final FinanceService service;
    private JPanel contentArea;
    private DashboardPanel    dashPanel;
    private TransactionsPanel txPanel;
    private BudgetPanel       budgetPanel;
    private GoalsPanel        goalsPanel;
    private ReportsPanel      reportsPanel;
    private JButton           activeBtn;

    private static final String[] NAV_ITEMS  = {"Dashboard", "Transactions", "Budgets", "Goals", "Reports"};
    private static final String[] NAV_ICONS  = {"⊞", "⇄", "⊡", "◎", "◈"};

    public MainFrame(FinanceService service) {
        this.service = service;
        setTitle("Personal Finance Trainer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1140, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        initPanels();
        buildUI();
        setVisible(true);
    }

    private void initPanels() {
        dashPanel    = new DashboardPanel(service);
        txPanel      = new TransactionsPanel(service);
        budgetPanel  = new BudgetPanel(service);
        goalsPanel   = new GoalsPanel(service);
        reportsPanel = new ReportsPanel(service);
    }

    private void buildUI() {
        getContentPane().setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(UIConstants.BG_DARK);
        contentArea.add(dashPanel,    "Dashboard");
        contentArea.add(txPanel,      "Transactions");
        contentArea.add(budgetPanel,  "Budgets");
        contentArea.add(goalsPanel,   "Goals");
        contentArea.add(reportsPanel, "Reports");
        add(contentArea, BorderLayout.CENTER);
        showPanel("Dashboard", null);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border
                g2.setColor(UIConstants.BORDER);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_W, 0));
        sidebar.setLayout(new BorderLayout());

        // Logo area
        JPanel logo = new JPanel(new BorderLayout());
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(28, 20, 24, 20));
        JLabel appIcon = UIConstants.label("₹", new Font("SansSerif", Font.BOLD, 28), UIConstants.ACCENT_GREEN);
        JPanel logoText = new JPanel(new GridLayout(2, 1, 0, 2));
        logoText.setOpaque(false);
        logoText.add(UIConstants.label("FinanceTrainer", UIConstants.FONT_SUBHEAD, UIConstants.TEXT_PRIMARY));
        logoText.add(UIConstants.label("Personal Budget", UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));
        logo.add(appIcon,  BorderLayout.WEST);
        logo.add(logoText, BorderLayout.CENTER);
        sidebar.add(logo, BorderLayout.NORTH);

        // Nav buttons
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        for (int i = 0; i < NAV_ITEMS.length; i++) {
            JButton btn = buildNavButton(NAV_ICONS[i], NAV_ITEMS[i]);
            nav.add(btn);
            nav.add(Box.createVerticalStrut(4));
        }
        sidebar.add(nav, BorderLayout.CENTER);

        // Bottom info
        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 4));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 20, 24, 20));
        bottom.add(UIConstants.label("v1.0.0", UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));
        bottom.add(UIConstants.label("© 2025 FinanceTrainer", UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED));
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton buildNavButton(String icon, String label) {
        JButton btn = new JButton(icon + "  " + label) {
            boolean hover = false;
            boolean active = false;

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(new Color(0x00, 0xD4, 0x8A, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(UIConstants.ACCENT_GREEN);
                    g2.fillRoundRect(0, 6, 4, getHeight() - 12, 4, 4);
                } else if (hover) {
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, 12));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }

            { // instance initializer
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }

            public void setActive(boolean a) { active = a; repaint(); }
            public boolean isActive() { return active; }
        };

        btn.setFont(UIConstants.FONT_BODY);
        btn.setForeground(UIConstants.TEXT_MUTED);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(UIConstants.SIDEBAR_W - 24, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        btn.addActionListener(e -> showPanel(label, btn));
        return btn;
    }

    @SuppressWarnings("unchecked")
    private void showPanel(String name, JButton btn) {
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, name);

        // Refresh active panel
        switch (name) {
            case "Dashboard":    dashPanel.refresh();    break;
            case "Transactions": txPanel.refresh();      break;
            case "Budgets":      budgetPanel.refresh();  break;
            case "Goals":        goalsPanel.refresh();   break;
            case "Reports":      reportsPanel.refresh(); break;
        }

        // Update nav active states
        if (activeBtn != null) setButtonActive(activeBtn, false);
        if (btn != null) { setButtonActive(btn, true); activeBtn = btn; }
        else {
            // Find and activate "Dashboard" button on startup
            Container nav = (Container) ((BorderLayout)((JPanel)getContentPane().getComponent(0)).getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        }
    }

    private void setButtonActive(JButton btn, boolean active) {
        try {
            btn.getClass().getMethod("setActive", boolean.class).invoke(btn, active);
            btn.setForeground(active ? UIConstants.ACCENT_GREEN : UIConstants.TEXT_MUTED);
            btn.setFont(active ? UIConstants.FONT_SUBHEAD : UIConstants.FONT_BODY);
        } catch (Exception ignored) {}
    }
}
