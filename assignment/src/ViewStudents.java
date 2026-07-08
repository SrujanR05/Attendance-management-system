import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewStudents extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public ViewStudents() {
        setTitle("Students List");
        setSize(600, 400);
        setLocationRelativeTo(null);

        // ✅ DO NOT exit whole app
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // ✅ When user clicks ❌ → go back to AdminPanel
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                new AdminPanel().setVisible(true);
            }
        });

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 90, 140));
        JLabel title = new JLabel("Students List", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[]{"Sl No", "RegNo", "Name"}, 0);
        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("Add Student");
        JButton removeButton = new JButton("Remove Student");
        JButton backButton = new JButton("← Back");

        addButton.setBackground(new Color(60, 179, 113));
        addButton.setForeground(Color.WHITE);

        removeButton.setBackground(new Color(178, 34, 34));
        removeButton.setForeground(Color.WHITE);

        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadStudentsFromDB();

        // Actions
        addButton.addActionListener(e -> addStudent());
        removeButton.addActionListener(e -> removeStudent());

        backButton.addActionListener(e -> {
            dispose();
            new AdminPanel().setVisible(true);
        });
    }

    // Load students
    private void loadStudentsFromDB() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT regno, name FROM students ORDER BY id ASC");
             ResultSet rs = pst.executeQuery()) {

            int slNo = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                        slNo++,
                        rs.getString("regno"),
                        rs.getString("name")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    // Add student
    private void addStudent() {
        JTextField regNoField = new JTextField();
        JTextField nameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] fields = {
                "Register Number (RegNo):", regNoField,
            "Student Name:", nameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Add Student", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String regNo = regNoField.getText().trim();
            String name = nameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (!regNo.isEmpty() && !name.isEmpty() && !password.isEmpty()) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement pst = con.prepareStatement(
                         "INSERT INTO students (regno, name, password) VALUES (?, ?, ?)")) {

                    pst.setString(1, regNo);
                    pst.setString(2, name);
                    pst.setString(3, password);
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(this, "✅ Student added successfully!");
                    loadStudentsFromDB();

                } catch (SQLIntegrityConstraintViolationException ex) {
                    JOptionPane.showMessageDialog(this, "⚠️ RegNo already exists!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "All fields are required.");
            }
        }
    }

    // Remove student
    private void removeStudent() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String regNo = model.getValueAt(row, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete student " + regNo + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement pst = con.prepareStatement(
                             "DELETE FROM students WHERE regno = ?")) {

                    pst.setString(1, regNo);
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(this, "🗑️ Student removed!");
                    loadStudentsFromDB();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a student first.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViewStudents().setVisible(true));
    }
}
