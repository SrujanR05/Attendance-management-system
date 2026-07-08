import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/attendance_system?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; // MySQL username
    private static final String PASSWORD = "root123"; // password you just set

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void saveAttendance(int studentId, String status) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, status, att_date) VALUES (?, ?, NOW())";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, status);
            pstmt.executeUpdate();
        }
    }
}
