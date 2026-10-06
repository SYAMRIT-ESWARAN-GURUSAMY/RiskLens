package sentinelx.ui;

import sentinelx.exception.BankingException;
import sentinelx.model.Account;
import sentinelx.model.SecurityAlert;
import sentinelx.service.BankingService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Administrator dashboard for alerts, account freezes, audit status, and activity. */
public final class AdminDashboardFrame extends JFrame {
    private final BankingService bank;
    private final DefaultTableModel accountModel = new DefaultTableModel(new String[]{"Account", "Customer", "Type", "Balance", "Status"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final DefaultTableModel alertModel = new DefaultTableModel(new String[]{"Time", "Severity", "Account", "Event", "Details"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final JTextArea audit = new JTextArea();

    public AdminDashboardFrame(BankingService bank) {
        super("SentinelX - Administrator Dashboard"); this.bank = bank;
        setSize(980, 610); setLocationByPlatform(true); setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Accounts", createAccountsTab()); tabs.addTab("Security alerts", new JScrollPane(new JTable(alertModel))); tabs.addTab("Audit verification", createAuditTab()); tabs.addTab("Transaction activity", createActivityTab());
        add(tabs); refresh();
    }
    private JPanel createAccountsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTable table = new JTable(accountModel); panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> refresh());
        JButton unfreeze = new JButton("Unfreeze selected"); unfreeze.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            try { bank.unfreeze(String.valueOf(accountModel.getValueAt(row, 0))); refresh(); }
            catch (BankingException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT)); controls.add(refresh); controls.add(unfreeze); panel.add(controls, BorderLayout.SOUTH); return panel;
    }
    private JPanel createAuditTab() {
        JPanel panel = new JPanel(new BorderLayout()); audit.setEditable(false); audit.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JButton verify = new JButton("Verify tamper-evident audit chain"); verify.addActionListener(e -> refreshAudit()); panel.add(new JScrollPane(audit), BorderLayout.CENTER); panel.add(verify, BorderLayout.SOUTH); return panel;
    }
    private JScrollPane createActivityTab() {
        JTextArea area = new JTextArea(); area.setEditable(false); area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        StringBuilder text = new StringBuilder("LIVE TRANSACTION ACTIVITY\n\n"); bank.transactions().forEach(tx -> text.append(TransactionFormatter.format(tx)).append('\n')); area.setText(text.toString()); return new JScrollPane(area);
    }
    private void refresh() {
        accountModel.setRowCount(0); alertModel.setRowCount(0);
        for (Account account : bank.accounts()) accountModel.addRow(new Object[]{account.accountNumber(), account.customerName(), account.accountKind(), "Rs. %.2f".formatted(account.balance()), account.frozen() ? "FROZEN" : "ACTIVE"});
        for (SecurityAlert alert : bank.alerts()) alertModel.addRow(new Object[]{alert.createdAt().withNano(0), alert.severity(), alert.accountNumber(), alert.title(), alert.details().replace(System.lineSeparator(), " ")});
        refreshAudit();
    }
    private void refreshAudit() { var result = bank.verifyAuditLog(); audit.setText("AUDIT CHAIN STATUS\n\n" + (result.valid() ? "VALID" : "INVALID") + "\nRecords checked: " + result.recordsChecked() + "\n" + result.message() + "\n\nActive sessions: " + bank.activeSessions()); }
}
