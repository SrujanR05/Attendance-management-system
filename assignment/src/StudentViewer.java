import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class StudentViewer extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public StudentViewer() {
        setTitle("Students List");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table model with columns: ID, Name, Roll No, Attendance
        model = new DefaultTableModel(new String[]{"ID", "Name", "Roll No", "Attendance"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // ID is not editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        loadStudents();

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Toolbar with buttons and search field
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Add Student");
        JButton removeButton = new JButton("Remove Student");
        JButton saveButton = new JButton("Save Changes");

        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");

        toolBar.add(addButton);
        toolBar.add(removeButton);
        toolBar.add(saveButton);
        toolBar.add(new JLabel("Search: "));
        toolBar.add(searchField);
        toolBar.add(searchButton);

        add(toolBar, BorderLayout.NORTH);

        // Add Student Button
        addButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Student Name:");
            String rollNo = JOptionPane.showInputDialog(this, "Enter Roll No:");
            String attendance = JOptionPane.showInputDialog(this, "Enter Attendance:");

            if (name != null && rollNo != null && attendance != null &&
                !name.isEmpty() && !rollNo.isEmpty() && !attendance.isEmpty()) {
                addStudent(name, rollNo, attendance);
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
            }
        });

        // Remove Student Button
        removeButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) model.getValueAt(selectedRow, 0);

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete this student?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    removeStudent(id);
                    loadStudents();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a student to remove.");
            }
        });

        // Save Changes Button
        saveButton.addActionListener(e -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                int id = (int) model.getValueAt(i, 0);
                String name = (String) model.getValueAt(i, 1);
                String rollNo = (String) model.getValueAt(i, 2);
                String attendance = (String) model.getValueAt(i, 3);
                updateStudent(id, name, rollNo, attendance);
            }
            JOptionPane.showMessageDialog(this, "✅ Changes saved successfully!");
        });

        // Search Button
        searchButton.addActionListener(e -> {
            String text = searchField.getText();
            if (text.trim().length() == 0) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    // Load students into table
    private void loadStudents() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("roll_no"),
                        rs.getString("attendance")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    // Add student
    private void addStudent(String name, String rollNo, String attendance) {
        String sql = "INSERT INTO students(name, roll_no, attendance) VALUES(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, rollNo);
            pstmt.setString(3, attendance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding student: " + e.getMessage());
        }
    }

    // Remove student by ID
    private void removeStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error removing student: " + e.getMessage());
        }
    }

    // Update student by ID
    private void updateStudent(int id, String name, String rollNo, String attendance) {
        String sql = "UPDATE students SET name = ?, roll_no = ?, attendance = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, rollNo);
            pstmt.setString(3, attendance);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating student: " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new StudentViewer().setVisible(true));
    }
}
