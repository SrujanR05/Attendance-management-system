import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentAttendanceView extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private String regNo;

    public StudentAttendanceView(String regNo) {
        this.regNo = regNo;

        setTitle("Attendance Record - " + regNo);
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Attendance Records for " + regNo, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(65, 105, 225));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Date", "Status"}, 0);
        table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backButton = new JButton("Logout");
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> {
            dispose();
            new StudentLogin().setVisible(true);
        });
        add(backButton, BorderLayout.SOUTH);

        loadAttendance();
    }

    private void loadAttendance() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT att_date, status FROM attendance " +
                             "WHERE student_id = (SELECT id FROM students WHERE regno = ?) " +
                             "ORDER BY att_date DESC"
             )) {

            ps.setString(1, regNo);
            ResultSet rs = ps.executeQuery();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getTimestamp("att_date"),
                        rs.getString("status")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No attendance records found!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
