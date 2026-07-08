import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AttendanceRecords extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<Timestamp> dateSelector;

    public AttendanceRecords() {
        setTitle("Attendance Records");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🔹 Gradient background (same as AttendancePage)
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(173, 216, 230), // Light Blue
                        w, h, new Color(0, 0, 139)      // Dark Blue
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        add(mainPanel);

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Select Attendance Date & Time:"));
        dateSelector = new JComboBox<>();
        loadAttendanceDates();
        dateSelector.addActionListener(e -> loadAttendanceRecords());
        topPanel.add(dateSelector);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Student Name", "Reg No", "Status"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        setRowColors(table);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        if (dateSelector.getItemCount() > 0) {
            dateSelector.setSelectedIndex(0);
            loadAttendanceRecords();
        }

        setVisible(true);
    }

    // ✅ Green for Present, Red for Absent
    private void setRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);

                if (col == 2) { // Status Column
                    String status = (String) tbl.getValueAt(row, 2);
                    if ("Present".equalsIgnoreCase(status)) {
                        c.setForeground(Color.GREEN.darker());
                    } else if ("Absent".equalsIgnoreCase(status)) {
                        c.setForeground(Color.RED);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                if (!isSelected) {
                    c.setBackground(new Color(230, 240, 255)); // Light Blue rows
                }
                return c;
            }
        });
    }

    private void loadAttendanceDates() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT att_date FROM attendance ORDER BY att_date DESC")) {

            while (rs.next()) {
                dateSelector.addItem(rs.getTimestamp("att_date"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading dates: " + e.getMessage());
        }
    }

    private void loadAttendanceRecords() {
        model.setRowCount(0);
        Timestamp selectedDate = (Timestamp) dateSelector.getSelectedItem();
        if (selectedDate == null) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT s.name, s.regno, a.status FROM attendance a " +
                             "JOIN students s ON a.student_id = s.id " +
                             "WHERE a.att_date = ?")) {

            ps.setTimestamp(1, selectedDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("name"));
                row.add(rs.getString("regno"));
                row.add(rs.getString("status"));
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading records: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AttendanceRecords::new);
    }
}
