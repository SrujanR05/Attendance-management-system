import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminPanel extends JFrame {

    private JLabel timeLabel;

    public AdminPanel() {
        setTitle("Admin Panel - Student Management");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Background panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));

        // Title label
        JLabel titleLabel = new JLabel("Admin Panel - Student Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 15, 15));
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        JButton btnViewStudents = new JButton("View / Edit Students");
        JButton btnMarkAttendance = new JButton("Mark Attendance");
        JButton btnViewRecords = new JButton("View Attendance Records");

        JButton[] buttons = {btnViewStudents, btnMarkAttendance, btnViewRecords};
        for (JButton b : buttons) {
            b.setBackground(new Color(60, 120, 180));
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 16));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Hover effect
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    b.setBackground(new Color(80, 140, 200));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    b.setBackground(new Color(60, 120, 180));
                }
            });
            buttonPanel.add(b);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Time label
        timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.DARK_GRAY);
        updateTime();
        mainPanel.add(timeLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // 🟢 Button actions
        btnViewStudents.addActionListener(e -> {
            try {
                new ViewStudents().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening student view: " + ex.getMessage());
            }
        });

        btnMarkAttendance.addActionListener(e -> {
            try {
                new AttendancePage().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening attendance page: " + ex.getMessage());
            }
        });

        btnViewRecords.addActionListener(e -> {
            try {
                new AttendanceRecords().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening records: " + ex.getMessage());
            }
        });

        // Timer to refresh clock
        new Timer(1000, e -> updateTime()).start();
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        timeLabel.setText("Current Time: " + sdf.format(new Date()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel().setVisible(true));
    }
}
