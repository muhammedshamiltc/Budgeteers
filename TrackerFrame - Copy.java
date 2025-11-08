import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class TrackerFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JLabel totalLabel;
    private JTextField amountField, categoryField, noteField, dateField;
    private JComboBox<String> dateQuickSelect;
    private JComboBox<String> viewFilter;
    private int userId;
    private String username;

    public TrackerFrame(int userId, String username) {
        this.userId = userId;
        this.username = username;

        setTitle("Expense Tracker - " + username);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        getRootPane().setBorder(new javax.swing.border.LineBorder(new Color(220, 220, 220), 1, true));

        // --- Table setup ---
        model = new DefaultTableModel();
        model.addColumn("No.");
        model.addColumn("DB_ID");
        model.addColumn("Amount");
        model.addColumn("Category");
        model.addColumn("Date");
        model.addColumn("Description");

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);
        // Center align headers and cells
        javax.swing.table.DefaultTableCellRenderer headerRenderer = (javax.swing.table.DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 0; c < table.getColumnModel().getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(centerRenderer);
        }

        // --- Title Bar ---
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        titleBar.setBackground(new Color(42, 183, 172));
        titleBar.setBorder(new javax.swing.border.LineBorder(new Color(42, 183, 172), 1, true));
        JLabel appTitle = new JLabel("EXPENSES");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        appTitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
        titleBar.add(appTitle);

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new GridLayout(3, 5, 10, 10));
        amountField = new JTextField();
        styleTextField(amountField);
        categoryField = new JTextField();
        styleTextField(categoryField);
        noteField = new JTextField();
        styleTextField(noteField);
        dateField = new JTextField(); // New date field
        dateField.setToolTipText("Enter date as YYYY-MM-DD");
        styleTextField(dateField);
        dateQuickSelect = new JComboBox<>(new String[]{"Today", "Yesterday", "Custom"});
        dateQuickSelect.setBackground(Color.WHITE);
        dateQuickSelect.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(new Color(200, 200, 200), 1, true),
                new javax.swing.border.EmptyBorder(4, 8, 4, 8)));
        setDateFieldForQuickSelect("Today");
        dateField.setEditable(false);
        dateQuickSelect.addActionListener(e -> {
            String choice = (String) dateQuickSelect.getSelectedItem();
            setDateFieldForQuickSelect(choice);
        });

        JButton addBtn = createRoundedButton("Add", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addExpense());

        JButton deleteBtn = createRoundedButton("Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteExpense());

        // Make Add/Delete the same width
        Dimension actionSize = new Dimension(96, 32);
        addBtn.setPreferredSize(actionSize);
        deleteBtn.setPreferredSize(actionSize);

        JButton reportBtn = createRoundedButton("Generate Report", new Color(42, 183, 172));
        reportBtn.addActionListener(e -> generateReport());

        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        inputPanel.add(dateField);
        inputPanel.add(dateQuickSelect);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(noteField);
        JPanel actionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionsPanel.add(addBtn);
        actionsPanel.add(deleteBtn);
        inputPanel.add(actionsPanel);

        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.EmptyBorder(8, 16, 8, 16),
                new javax.swing.border.LineBorder(new Color(225, 225, 225), 1, true)));
        inputContainer.add(inputPanel, BorderLayout.CENTER);

        // --- Bottom Panel ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("$ 0.00");
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leftControls.add(new JLabel("View:"));
        viewFilter = new JComboBox<>(new String[]{"All", "This Month"});
        viewFilter.setBackground(Color.WHITE);
        viewFilter.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(new Color(200, 200, 200), 1, true),
                new javax.swing.border.EmptyBorder(4, 8, 4, 8)));
        viewFilter.addActionListener(e -> loadExpenses());
        leftControls.add(viewFilter);
        bottomPanel.add(leftControls, BorderLayout.WEST);
        bottomPanel.add(reportBtn, BorderLayout.EAST);

        // --- Add to frame ---
        // Improve table visuals
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.setBorder(new javax.swing.border.LineBorder(new Color(225, 225, 225), 1, true));
        add(scrollPane, BorderLayout.CENTER);
        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(titleBar, BorderLayout.NORTH);
        northWrapper.add(inputContainer, BorderLayout.SOUTH);
        add(northWrapper, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        loadExpenses();
        setVisible(true);
    }

    // ---------------- Add Expense ----------------
    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            String category = categoryField.getText().trim();
            String description = noteField.getText().trim();
            String dateStr = dateField.getText().trim();

            if (category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category cannot be empty.");
                return;
            }

            // Validate date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false); // Strict date parsing
            Date expenseDate = sdf.parse(dateStr);
            Date currentDate = new Date();
            if (expenseDate.after(currentDate)) {
                JOptionPane.showMessageDialog(this, "Date cannot be in the future!");
                return;
            }

            try (Connection conn = AuthManager.DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO expenses (user_id, amount, category, date, description) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, userId);
                ps.setDouble(2, amount);
                ps.setString(3, category);
                ps.setDate(4, new java.sql.Date(expenseDate.getTime())); // Convert to SQL Date
                ps.setString(5, description);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Failed to add expense.");
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Expense added successfully!");
            amountField.setText("");
            categoryField.setText("");
            dateField.setText("");
            noteField.setText("");
            loadExpenses();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------- Delete Expense ----------------
    private void deleteExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(selectedRow, 1).toString());

        try (Connection conn = AuthManager.DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id=? AND user_id=?");
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting expense.");
            e.printStackTrace();
        }

        loadExpenses();
    }

    // ---------------- Generate Report ----------------
    private void generateReport() {
        try {
            JFrame reportFrame = new JFrame("Expense Report - " + username);
            reportFrame.setSize(600, 400);
            reportFrame.setLocationRelativeTo(this);

            DefaultTableModel reportModel = new DefaultTableModel();
            reportModel.addColumn("No.");
            reportModel.addColumn("Amount");
            reportModel.addColumn("Category");
            reportModel.addColumn("Date");
            reportModel.addColumn("Description");

            double total = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                Vector<Object> row = new Vector<>();
                row.add(model.getValueAt(i, 0)); // No.
                row.add(model.getValueAt(i, 2)); // Amount
                row.add(model.getValueAt(i, 3)); // Category
                row.add(model.getValueAt(i, 4)); // Date
                row.add(model.getValueAt(i, 5)); // Description
                reportModel.addRow(row);
                String amtStr = model.getValueAt(i, 2).toString().replace("$", "").trim();
                total += Double.parseDouble(amtStr);
            }

            JTable reportTable = new JTable(reportModel);
            JScrollPane scrollPane = new JScrollPane(reportTable);

            JLabel totalLabel = new JLabel(String.format("Total: %.2f $", total));
            totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

            reportFrame.setLayout(new BorderLayout());
            reportFrame.add(scrollPane, BorderLayout.CENTER);
            reportFrame.add(totalLabel, BorderLayout.SOUTH);
            reportFrame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report.");
            e.printStackTrace();
        }
    }

    // ---------------- Load Expenses ----------------
    private void loadExpenses() {
        model.setRowCount(0);
        double total = 0;
        try (Connection conn = AuthManager.DBUtil.getConnection()) {
            String baseSql = "SELECT id, amount, category, date, description FROM expenses WHERE user_id=?";
            boolean thisMonth = viewFilter != null && "This Month".equals(viewFilter.getSelectedItem());
            if (thisMonth) {
                baseSql += " AND MONTH(date)=MONTH(CURDATE()) AND YEAR(date)=YEAR(CURDATE())";
            }
            baseSql += " ORDER BY date DESC";
            PreparedStatement ps = conn.prepareStatement(baseSql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            int idx = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(idx);
                row.add(rs.getInt("id"));
                double amt = rs.getDouble("amount");
                row.add(String.format("$ %.2f", amt));
                row.add(rs.getString("category"));
                Date d = rs.getDate("date");
                row.add(d != null ? new SimpleDateFormat("yyyy-MM-dd").format(d) : "");
                row.add(rs.getString("description"));
                model.addRow(row);
                total += amt;
                idx++;
            }
            totalLabel.setText(String.format("$ %.2f", total));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading expenses.");
            e.printStackTrace();
        }
    }

    private void setDateFieldForQuickSelect(String choice) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if ("Today".equals(choice)) {
                dateField.setText(sdf.format(new Date()));
                dateField.setEditable(false);
            } else if ("Yesterday".equals(choice)) {
                dateField.setText(sdf.format(new Date(System.currentTimeMillis() - 24L*60L*60L*1000L)));
                dateField.setEditable(false);
            } else {
                dateField.setEditable(true);
            }
        } catch (Exception ignored) {}
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                int arc = 14;
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(new javax.swing.border.EmptyBorder(6, 14, 6, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(new Color(200, 200, 200), 1, true),
                new javax.swing.border.EmptyBorder(6, 8, 6, 8)));
    }
}
