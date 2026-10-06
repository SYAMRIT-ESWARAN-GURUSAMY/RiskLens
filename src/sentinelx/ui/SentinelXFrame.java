package sentinelx.ui;

import sentinelx.exception.BankingException;
import sentinelx.exception.AuthenticationException;
import sentinelx.fraud.RiskAssessment;
import sentinelx.model.*;
import sentinelx.service.BankingService;
import sentinelx.simulator.AttackSimulator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Customer console for the educational ATM security simulator. */
public final class SentinelXFrame extends JFrame {
    private static final Color NAVY = new Color(20, 34, 55);
    private static final Color BLUE = new Color(35, 105, 190);
    private static final Color BG = new Color(242, 246, 251);
    private static final Color MUTED = new Color(94, 108, 125);
    private final BankingService bank;
    private final JTextField account = new JTextField("1001002", 15);
    private final JPasswordField pin = new JPasswordField(15);
    private final JTextField totp = new JTextField(8);
    private final JTextField amount = new JTextField("1000", 12);
    private final JComboBox<TransactionType> type = new JComboBox<>(TransactionType.values());
    private final JComboBox<Channel> channel = new JComboBox<>(Channel.values());
    private final JTextField terminal = new JTextField("ATM-DELHI-01", 16);
    private final JTextArea output = new JTextArea();
    private final JLabel sessionStatus = new JLabel("Signed out");
    private final JLabel balanceStatus = new JLabel("Balance: —");
    private final JButton loginButton = new JButton("Authenticate");
    private final JButton submitButton = new JButton("Process transaction");
    private Session session;

    public SentinelXFrame(BankingService bank) {
        super("RiskLens | ATM Security Simulator");
        this.bank = bank;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 680));
        setSize(920, 740);
        setLocationByPlatform(true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        add(createHeader(), BorderLayout.NORTH);
        add(createWorkspace(), BorderLayout.CENTER);
        add(createActions(), BorderLayout.SOUTH);
        loginButton.addActionListener(event -> authenticate());
        submitButton.addActionListener(event -> process());
        submitButton.setEnabled(false);
        type.addActionListener(event -> updateAmountAvailability());
        output.setText("Welcome. Authenticate to begin.\n\nTransactions are assessed before they affect the account.\nA challenge or freeze will explain the risk signals that triggered it.");
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 24, 18, 24));
        JPanel brand = new JPanel(); brand.setOpaque(false); brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("RISKLENS"); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel subtitle = new JLabel("ATM SECURITY SIMULATOR   •   EDUCATIONAL ENVIRONMENT"); subtitle.setForeground(new Color(190, 205, 224)); subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        brand.add(title); brand.add(Box.createVerticalStrut(4)); brand.add(subtitle);
        JPanel right = new JPanel(); right.setOpaque(false); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        sessionStatus.setForeground(new Color(214, 231, 250)); sessionStatus.setFont(new Font("SansSerif", Font.BOLD, 13)); sessionStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
        balanceStatus.setForeground(Color.WHITE); balanceStatus.setFont(new Font("SansSerif", Font.BOLD, 16)); balanceStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(sessionStatus); right.add(Box.createVerticalStrut(5)); right.add(balanceStatus);
        header.add(brand, BorderLayout.WEST); header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(14, 14));
        workspace.setBackground(BG); workspace.setBorder(new EmptyBorder(18, 20, 14, 20));
        JPanel top = new JPanel(new GridLayout(1, 2, 14, 0)); top.setOpaque(false);
        top.add(createAccountCard()); top.add(createTransactionCard());
        workspace.add(top, BorderLayout.NORTH);
        JPanel resultCard = card("TRANSACTION RESPONSE", "Risk decision and next steps");
        output.setEditable(false); output.setLineWrap(true); output.setWrapStyleWord(true);
        output.setFont(new Font("Consolas", Font.PLAIN, 14)); output.setForeground(NAVY); output.setBackground(Color.WHITE);
        output.setBorder(new EmptyBorder(14, 14, 14, 14));
        JScrollPane scroll = new JScrollPane(output); scroll.setBorder(BorderFactory.createLineBorder(new Color(222, 230, 239))); scroll.getViewport().setBackground(Color.WHITE);
        resultCard.add(scroll, BorderLayout.CENTER);
        workspace.add(resultCard, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createAccountCard() {
        JPanel panel = card("CUSTOMER ACCESS", "Sign in to a simulator account");
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        addField(fields, 0, "Account number", account); addField(fields, 1, "PIN", pin);
        panel.add(fields, BorderLayout.CENTER);
        JLabel note = new JLabel("Demo: 1001002 / PIN 5678  ·  saved demo account may be frozen"); note.setForeground(MUTED); note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTransactionCard() {
        JPanel panel = card("TRANSACTION DETAILS", "Set the operation and simulated channel");
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        addField(fields, 0, "Operation", type); addField(fields, 1, "Channel", channel);
        addField(fields, 2, "Amount (Rs.)", amount); addField(fields, 3, "Terminal ID", terminal);
        addField(fields, 4, "TOTP code", totp);
        JLabel note = new JLabel("Enter a code only if the risk engine requests verification."); note.setForeground(MUTED); note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(fields, BorderLayout.CENTER); panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel card(String heading, String description) {
        JPanel panel = new JPanel(new BorderLayout(8, 12)); panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(222, 230, 239)), new EmptyBorder(14, 16, 14, 16)));
        JPanel labels = new JPanel(); labels.setOpaque(false); labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(heading); title.setForeground(NAVY); title.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel desc = new JLabel(description); desc.setForeground(MUTED); desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labels.add(title); labels.add(Box.createVerticalStrut(4)); labels.add(desc);
        panel.add(labels, BorderLayout.NORTH);
        return panel;
    }

    private void addField(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4, 0, 4, 8); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        JLabel caption = new JLabel(label); caption.setForeground(new Color(55, 70, 90)); caption.setFont(new Font("SansSerif", Font.PLAIN, 12)); panel.add(caption, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(4, 0, 4, 0);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13)); field.setPreferredSize(new Dimension(170, 30)); panel.add(field, c);
    }

    private JComponent createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 10)); actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 230, 239)));
        stylePrimary(loginButton); stylePrimary(submitButton);
        JButton history = new JButton("Transaction history"); history.addActionListener(event -> showHistory());
        JButton changePin = new JButton("Change PIN"); changePin.addActionListener(event -> changePin());
        JButton simulate = new JButton("Run simulation"); simulate.addActionListener(event -> runSimulation());
        JButton admin = new JButton("Administrator"); admin.addActionListener(event -> openAdmin());
        JButton logout = new JButton("Sign out"); logout.addActionListener(event -> logout());
        actions.add(loginButton); actions.add(submitButton); actions.add(history); actions.add(changePin); actions.add(simulate); actions.add(admin); actions.add(logout);
        return actions;
    }

    private void stylePrimary(JButton button) { button.setBackground(BLUE); button.setForeground(Color.WHITE); button.setFocusPainted(false); button.setFont(new Font("SansSerif", Font.BOLD, 12)); }

    private void updateAmountAvailability() {
        boolean needed = type.getSelectedItem() != TransactionType.BALANCE_ENQUIRY;
        amount.setEnabled(needed); if (!needed) amount.setText("0"); else if ("0".equals(amount.getText())) amount.setText("1000");
    }

    private void process() {
        try {
            if (session == null || !account.getText().trim().equals(session.accountNumber())) throw new BankingException("Authenticate this account before processing a transaction.");
            double value = Double.parseDouble(amount.getText().trim());
            if (!Double.isFinite(value) || value < 0) throw new NumberFormatException();
            RiskAssessment result = bank.transact(session, (TransactionType) type.getSelectedItem(), (Channel) channel.getSelectedItem(), value, terminal.getText().trim(), totp.getText().trim());
            Account selected = bank.findAccount(account.getText().trim()).orElseThrow();
            output.setText("APPROVED  ·  " + result.decision() + "  ·  RISK " + result.score() + "\n\n" + result.explanation() + "\n\nUpdated balance: Rs. %.2f".formatted(selected.balance()));
            balanceStatus.setText("Balance: Rs. %.2f".formatted(selected.balance()));
        } catch (NumberFormatException error) { output.setText("Check the amount. Enter a valid, non-negative number."); }
        catch (BankingException error) {
            output.setText("ACTION REQUIRED\n\n" + error.getMessage());
            if (error.getMessage().contains("session has expired") || error.getMessage().contains("account is frozen")) clearSession();
        }
    }

    private void authenticate() {
        try {
            session = bank.login(account.getText().trim(), new String(pin.getPassword()));
            sessionStatus.setText("Signed in  ·  " + session.accountNumber());
            submitButton.setEnabled(true);
            Account selected = bank.findAccount(session.accountNumber()).orElseThrow();
            balanceStatus.setText("Balance: Rs. %.2f".formatted(selected.balance()));
            output.setText("AUTHENTICATED\n\nAccount " + session.accountNumber() + " is ready. Session expires after five minutes.\n\nThe demo verification code is shown by the simulator when a challenge is raised.");
        } catch (AuthenticationException error) { output.setText("SIGN IN FAILED\n\n" + error.getMessage()); clearSession(); }
    }

    private void showHistory() {
        StringBuilder history = new StringBuilder("TRANSACTION HISTORY\n\n");
        bank.transactions().forEach(tx -> history.append(TransactionFormatter.format(tx)).append('\n'));
        output.setText(history.toString());
    }

    private void runSimulation() {
        if (session == null || !account.getText().trim().equals(session.accountNumber())) { output.setText("Authenticate before running a simulation."); return; }
        String[] options = {"Rapid withdrawal", "Brute-force PIN", "Stolen card", "Cross-channel fraud", "High-value withdrawal"};
        String selected = (String) JOptionPane.showInputDialog(this, "Select a safe local simulation:", "Attack simulator", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected != null) output.setText(new AttackSimulator(bank).runScenario(account.getText().trim(), selected));
    }

    private void changePin() {
        if (session == null) { output.setText("Authenticate before changing your PIN."); return; }
        JPasswordField oldPin = new JPasswordField(); JPasswordField newPin = new JPasswordField();
        Object[] fields = {"Current PIN:", oldPin, "New 4-digit PIN:", newPin};
        if (JOptionPane.showConfirmDialog(this, fields, "Change PIN", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try { bank.changePin(session.accountNumber(), new String(oldPin.getPassword()), new String(newPin.getPassword())); output.setText("PIN changed successfully. Sign in again with your new PIN."); clearSession(); }
        catch (BankingException error) { output.setText("PIN change failed: " + error.getMessage()); }
    }

    private void clearSession() { session = null; sessionStatus.setText("Signed out"); balanceStatus.setText("Balance: —"); submitButton.setEnabled(false); }
    private void logout() { if (session != null) bank.logout(session.token()); clearSession(); output.setText("Signed out. Authenticate to begin a new session."); }

    private void openAdmin() {
        String code = JOptionPane.showInputDialog(this, "Enter administrator code:");
        if (sentinelx.util.Constants.ADMIN_ACCESS_CODE.equals(code)) new AdminDashboardFrame(bank).setVisible(true);
        else if (code != null) JOptionPane.showMessageDialog(this, "Invalid administrator code.", "Access denied", JOptionPane.ERROR_MESSAGE);
    }
}
