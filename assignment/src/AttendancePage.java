import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AttendancePage extends JFrame {
    private JTable studentTable;
    private DefaultTableModel studentModel;
    private JTable summaryTable;
    private DefaultTableModel summaryModel;
    private JLabel dateTimeLabel;
    private Timestamp currentDateTime;

    public AttendancePage() {
        setTitle("Mark Attendance");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🔹 Gradient blue background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(173, 216, 230), // Light Blue (top)
                        w, h, new Color(0, 0, 139)      // Dark Blue (bottom)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        add(mainPanel);

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        currentDateTime = new Timestamp(new Date().getTime());
        dateTimeLabel = new JLabel("Current Date & Time: " +
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(currentDateTime));
        JButton configureDateButton = new JButton("Configure Date & Time");
        topPanel.add(dateTimeLabel);
        topPanel.add(configureDateButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        configureDateButton.addActionListener(e -> configureDateTime());

        // Student Table (with checkbox for attendance)
        String[] studentCols = {"Student Name", "Reg No", "Present"};
        studentModel = new DefaultTableModel(studentCols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Boolean.class; // ✅ Checkbox
                return String.class;
            }
        };
        studentTable = new JTable(studentModel);
        studentTable.setRowHeight(30);
        setSingleRowColor(studentTable);

        loadStudents();
        mainPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Summary table
        String[] summaryCols = {"Present", "Absent"};
        summaryModel = new DefaultTableModel(summaryCols, 0);
        summaryTable = new JTable(summaryModel);
        summaryTable.setRowHeight(25);
        setSingleRowColor(summaryTable);
        styleSummaryTableHeaders(); // ✅ Custom headers

        JScrollPane summaryScroll = new JScrollPane(summaryTable);
        summaryScroll.setPreferredSize(new Dimension(780, 150));
        bottomPanel.add(summaryScroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton saveAttendanceBtn = new JButton("Save Attendance");
        JButton viewAttendanceBtn = new JButton("View Attendance");

        styleButton(saveAttendanceBtn, new Color(70, 130, 180));
        styleButton(viewAttendanceBtn, new Color(220, 20, 60));

        buttonPanel.add(saveAttendanceBtn);
        buttonPanel.add(viewAttendanceBtn);

        saveAttendanceBtn.addActionListener(e -> saveAttendance());
        viewAttendanceBtn.addActionListener(e -> updateSummary());

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    // ✅ Single row color for all rows
    private void setSingleRowColor(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(new Color(230, 240, 255)); // light blue
                }
                return c;
            }
        });
    }

    // ✅ Custom Header Renderer for Present & Absent
    private void styleSummaryTableHeaders() {
        JTableHeader header = summaryTable.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setOpaque(true);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setForeground(Color.WHITE);

                if (col == 0) { // Present Column
                    label.setBackground(new Color(34, 139, 34)); // Green
                    label.setText("✔ Present");
                } else if (col == 1) { // Absent Column
                    label.setBackground(new Color(178, 34, 34)); // Red
                    label.setText("✘ Absent");
                }
                return label;
            }
        });
    }

    private void configureDateTime() {
        String newDateTime = JOptionPane.showInputDialog(
                this, "Enter Date & Time (dd-MM-yyyy HH:mm:ss)",
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(currentDateTime));
        try {
            if (newDateTime != null && !newDateTime.trim().isEmpty()) {
                java.util.Date parsed = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(newDateTime);
                currentDateTime = new Timestamp(parsed.getTime());
                dateTimeLabel.setText("Current Date & Time: " + newDateTime);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format!");
        }
    }

    private void loadStudents() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, regno FROM students")) {

            while (rs.next()) {
                studentModel.addRow(new Object[]{
                        rs.getString("name"), rs.getString("regno"), false
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void updateSummary() {
        summaryModel.setRowCount(0);
        java.util.List<String> present = new ArrayList<>();
        java.util.List<String> absent = new ArrayList<>();

        for (int i = 0; i < studentModel.getRowCount(); i++) {
            String name = (String) studentModel.getValueAt(i, 0);
            String regno = (String) studentModel.getValueAt(i, 1);
            boolean isPresent = Boolean.TRUE.equals(studentModel.getValueAt(i, 2));

            if (isPresent) present.add(name + " (" + regno + ")");
            else absent.add(name + " (" + regno + ")");
        }

        int max = Math.max(present.size(), absent.size());
        for (int i = 0; i < max; i++) {
            String p = i < present.size() ? present.get(i) : "";
            String a = i < absent.size() ? absent.get(i) : "";
            summaryModel.addRow(new Object[]{p, a});
        }
    }

    private void saveAttendance() {
        try (Connection con = DBConnection.getConnection()) {
            String deleteSql = "DELETE FROM attendance WHERE att_date = ?";
            PreparedStatement deletePs = con.prepareStatement(deleteSql);
            deletePs.setTimestamp(1, currentDateTime);
            deletePs.executeUpdate();

            String sql = "INSERT INTO attendance (student_id, status, att_date) " +
                    "VALUES ((SELECT id FROM students WHERE regno = ?), ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            for (int i = 0; i < studentModel.getRowCount(); i++) {
                String regno = (String) studentModel.getValueAt(i, 1);
                boolean isPresent = Boolean.TRUE.equals(studentModel.getValueAt(i, 2));
                String status = isPresent ? "Present" : "Absent";

                ps.setString(1, regno);
                ps.setString(2, status);
                ps.setTimestamp(3, currentDateTime);
                ps.addBatch();
            }
            ps.executeBatch();

            JOptionPane.showMessageDialog(this, "Attendance saved successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AttendancePage::new);
    }
}
