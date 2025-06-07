import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class LibraryManagementSystem {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_database";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Abhay@mysql123";

    // Main method to start the application
    public static void main(String[] args) {
        initializeDatabase();
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }

    // Database initialization
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "username VARCHAR(50) UNIQUE NOT NULL, " +
                         "password VARCHAR(100) NOT NULL, " +
                         "role ENUM('ADMIN', 'LIBRARIAN', 'STUDENT') NOT NULL, " +
                         "active BOOLEAN DEFAULT TRUE, " +
                         "security_question VARCHAR(200), " +
                         "security_answer VARCHAR(200))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                         "book_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "title VARCHAR(100) NOT NULL, " +
                         "author VARCHAR(100) NOT NULL, " +
                         "available BOOLEAN DEFAULT TRUE)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                         "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "book_id INT NOT NULL, " +
                         "user_id INT NOT NULL, " +
                         "issue_date DATE NOT NULL, " +
                         "due_date DATE NOT NULL, " +
                         "return_date DATE, " +
                         "fine DOUBLE DEFAULT 0, " +
                         "status ENUM('ISSUED', 'RETURNED', 'OVERDUE', 'HOLD') DEFAULT 'ISSUED', " +
                         "FOREIGN KEY (book_id) REFERENCES books(book_id), " +
                         "FOREIGN KEY (user_id) REFERENCES users(user_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS requests (" +
                         "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT NOT NULL, " +
                         "book_title VARCHAR(100), " +
                         "request_type ENUM('NEW_BOOK', 'HOLD', 'REISSUE'), " +
                         "status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING', " +
                         "request_date DATE NOT NULL, " +
                         "FOREIGN KEY (user_id) REFERENCES users(user_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                         "notification_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT NOT NULL, " +
                         "message VARCHAR(255) NOT NULL, " +
                         "date DATE NOT NULL, " +
                         "read_status BOOLEAN DEFAULT FALSE, " +
                         "FOREIGN KEY (user_id) REFERENCES users(user_id))");
            
            // Create admin if not exists
            ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='admin'");
            if (!rs.next()) {
                stmt.executeUpdate("INSERT INTO users (username, password, role, security_question, security_answer) VALUES " +
                                  "('admin', 'admin123', 'ADMIN', 'What is your favorite color?', 'blue'), " +
                                  "('librarian', 'lib123', 'LIBRARIAN', 'What is your favorite color?', 'blue'), " +
                                  "('student', 'stu123', 'STUDENT', 'What is your favorite color?', 'blue')");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database initialization failed: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Model classes
    static class User {
        private int userId;
        private String username;
        private String password;
        private String role;
        private boolean active;
        private String securityQuestion;
        private String securityAnswer;

        public User(int userId, String username, String password, String role, boolean active, 
                   String securityQuestion, String securityAnswer) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.role = role;
            this.active = active;
            this.securityQuestion = securityQuestion;
            this.securityAnswer = securityAnswer;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
        public boolean isActive() { return active; }
        public String getSecurityQuestion() { return securityQuestion; }
        public String getSecurityAnswer() { return securityAnswer; }
    }

    static class Book {
        private int bookId;
        private String title;
        private String author;
        private boolean available;

        public Book(int bookId, String title, String author, boolean available) {
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.available = available;
        }

        public int getBookId() { return bookId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public boolean isAvailable() { return available; }
    }

    static class Transaction {
        private int transactionId;
        private int bookId;
        private int userId;
        private Date issueDate;
        private Date dueDate;
        private Date returnDate;
        private double fine;
        private String status;

        public Transaction(int transactionId, int bookId, int userId, Date issueDate, 
                          Date dueDate, Date returnDate, double fine, String status) {
            this.transactionId = transactionId;
            this.bookId = bookId;
            this.userId = userId;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.fine = fine;
            this.status = status;
        }

        public int getTransactionId() { return transactionId; }
        public int getBookId() { return bookId; }
        public int getUserId() { return userId; }
        public Date getIssueDate() { return issueDate; }
        public Date getDueDate() { return dueDate; }
        public Date getReturnDate() { return returnDate; }
        public double getFine() { return fine; }
        public String getStatus() { return status; }
    }

    static class Request {
        private int requestId;
        private int userId;
        private String bookTitle;
        private String requestType;
        private String status;
        private Date requestDate;

        public Request(int requestId, int userId, String bookTitle, String requestType, String status, Date requestDate) {
            this.requestId = requestId;
            this.userId = userId;
            this.bookTitle = bookTitle;
            this.requestType = requestType;
            this.status = status;
            this.requestDate = requestDate;
        }

        public int getRequestId() { return requestId; }
        public int getUserId() { return userId; }
        public String getBookTitle() { return bookTitle; }
        public String getRequestType() { return requestType; }
        public String getStatus() { return status; }
        public Date getRequestDate() { return requestDate; }
    }

    static class Notification {
        private int notificationId;
        private int userId;
        private String message;
        private Date date;
        private boolean readStatus;

        public Notification(int notificationId, int userId, String message, Date date, boolean readStatus) {
            this.notificationId = notificationId;
            this.userId = userId;
            this.message = message;
            this.date = date;
            this.readStatus = readStatus;
        }

        public int getNotificationId() { return notificationId; }
        public int getUserId() { return userId; }
        public String getMessage() { return message; }
        public Date getDate() { return date; }
        public boolean isReadStatus() { return readStatus; }
    }

    // Service classes
    static class UserService {
        public User authenticate(String username, String password) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND active = TRUE";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("active"),
                        rs.getString("security_question"),
                        rs.getString("security_answer")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<User> getAllUsers(String roleFilter) {
            List<User> users = new ArrayList<>();
            String sql = "SELECT * FROM users";
            if (roleFilter != null) {
                sql += " WHERE role = '" + roleFilter + "'";
            }
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("active"),
                        rs.getString("security_question"),
                        rs.getString("security_answer")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }

        public boolean addUser(User user) {
            String sql = "INSERT INTO users (username, password, role, security_question, security_answer) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getRole());
                pstmt.setString(4, user.getSecurityQuestion());
                pstmt.setString(5, user.getSecurityAnswer());
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean updateUserStatus(int userId, boolean active) {
            String sql = "UPDATE users SET active = ? WHERE user_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setBoolean(1, active);
                pstmt.setInt(2, userId);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean deleteUser(int userId) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public User getUserByUsername(String username) {
            String sql = "SELECT * FROM users WHERE username = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("active"),
                        rs.getString("security_question"),
                        rs.getString("security_answer")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public boolean updatePassword(int userId, String newPassword) {
            String sql = "UPDATE users SET password = ? WHERE user_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newPassword);
                pstmt.setInt(2, userId);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean resetPassword(String username, String securityAnswer, String newPassword) {
            String sql = "UPDATE users SET password = ? WHERE username = ? AND security_answer = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newPassword);
                pstmt.setString(2, username);
                pstmt.setString(3, securityAnswer);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    static class BookService {
        public boolean addBook(String title, String author) {
            String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public List<Book> getAllBooks() {
            List<Book> books = new ArrayList<>();
            String sql = "SELECT * FROM books";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    books.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBoolean("available")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return books;
        }

        public boolean deleteBook(int bookId) {
            String sql = "DELETE FROM books WHERE book_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, bookId);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean issueBook(int bookId, int userId) {
            String checkSql = "SELECT available FROM books WHERE book_id = ?";
            String updateSql = "UPDATE books SET available = FALSE WHERE book_id = ?";
            String transSql = "INSERT INTO transactions (book_id, user_id, issue_date, due_date, status) " +
                              "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'ISSUED')";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                conn.setAutoCommit(false);
                
                // Check availability
                try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setInt(1, bookId);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next() || !rs.getBoolean("available")) {
                        return false;
                    }
                }
                
                // Update book status
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, bookId);
                    pstmt.executeUpdate();
                }
                
                // Create transaction
                try (PreparedStatement pstmt = conn.prepareStatement(transSql)) {
                    pstmt.setInt(1, bookId);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean returnBook(int transactionId, double fine) {
            String transSql = "UPDATE transactions SET return_date = CURDATE(), fine = ?, status = 'RETURNED' " +
                             "WHERE transaction_id = ?";
            String bookSql = "UPDATE books b JOIN transactions t ON b.book_id = t.book_id " +
                            "SET b.available = TRUE WHERE t.transaction_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                conn.setAutoCommit(false);
                
                // Update transaction
                try (PreparedStatement pstmt = conn.prepareStatement(transSql)) {
                    pstmt.setDouble(1, fine);
                    pstmt.setInt(2, transactionId);
                    pstmt.executeUpdate();
                }
                
                // Update book availability
                try (PreparedStatement pstmt = conn.prepareStatement(bookSql)) {
                    pstmt.setInt(1, transactionId);
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public List<Transaction> getTransactionsByUser(int userId) {
            List<Transaction> transactions = new ArrayList<>();
            String sql = "SELECT * FROM transactions WHERE user_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("book_id"),
                        rs.getInt("user_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine"),
                        rs.getString("status")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return transactions;
        }

        public List<Transaction> getAllTransactions() {
            List<Transaction> transactions = new ArrayList<>();
            String sql = "SELECT * FROM transactions";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("book_id"),
                        rs.getInt("user_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine"),
                        rs.getString("status")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return transactions;
        }

        public List<Transaction> getOverdueTransactions() {
            List<Transaction> transactions = new ArrayList<>();
            String sql = "SELECT * FROM transactions WHERE due_date < CURDATE() AND status = 'ISSUED'";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("book_id"),
                        rs.getInt("user_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine"),
                        rs.getString("status")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return transactions;
        }

        public double calculateFine(Date dueDate) {
            long diff = new Date().getTime() - dueDate.getTime();
            long daysOverdue = diff / (1000 * 60 * 60 * 24);
            return daysOverdue > 0 ? daysOverdue * 5.0 : 0.0; // $5 per day fine
        }

        public Book getBookById(int bookId) {
            String sql = "SELECT * FROM books WHERE book_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, bookId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBoolean("available")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static class RequestService {
        public boolean createRequest(int userId, String bookTitle, String requestType) {
            String sql = "INSERT INTO requests (user_id, book_title, request_type, request_date) " +
                         "VALUES (?, ?, ?, CURDATE())";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                pstmt.setString(2, bookTitle);
                pstmt.setString(3, requestType);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public List<Request> getPendingRequests() {
            List<Request> requests = new ArrayList<>();
            String sql = "SELECT * FROM requests WHERE status = 'PENDING'";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    requests.add(new Request(
                        rs.getInt("request_id"),
                        rs.getInt("user_id"),
                        rs.getString("book_title"),
                        rs.getString("request_type"),
                        rs.getString("status"),
                        rs.getDate("request_date")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return requests;
        }

        public boolean updateRequestStatus(int requestId, String status) {
            String sql = "UPDATE requests SET status = ? WHERE request_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, status);
                pstmt.setInt(2, requestId);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    static class NotificationService {
        public boolean createNotification(int userId, String message) {
            String sql = "INSERT INTO notifications (user_id, message, date) VALUES (?, ?, CURDATE())";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                pstmt.setString(2, message);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public List<Notification> getUserNotifications(int userId) {
            List<Notification> notifications = new ArrayList<>();
            String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY date DESC";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    notifications.add(new Notification(
                        rs.getInt("notification_id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getDate("date"),
                        rs.getBoolean("read_status")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return notifications;
        }

        public boolean markAsRead(int notificationId) {
            String sql = "UPDATE notifications SET read_status = TRUE WHERE notification_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, notificationId);
                
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // GUI Screens
    static class LoginScreen extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JComboBox<String> roleComboBox;
        
        public LoginScreen() {
            setTitle("Library Management System - Login");
            setSize(400, 350);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            // Username
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Username:"), gbc);
            
            gbc.gridx = 1;
            usernameField = new JTextField(15);
            panel.add(usernameField, gbc);
            
            // Password
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Password:"), gbc);
            
            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            panel.add(passwordField, gbc);
            
            // Role
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Role:"), gbc);
            
            gbc.gridx = 1;
            String[] roles = {"ADMIN", "LIBRARIAN", "STUDENT"};
            roleComboBox = new JComboBox<>(roles);
            panel.add(roleComboBox, gbc);
            
            // Login Button
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.CENTER;
            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();
                
                User user = new UserService().authenticate(username, password);
                if (user != null && user.getRole().equals(role)) {
                    openDashboard(user);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginScreen.this, 
                        "Invalid credentials or role mismatch", 
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
            panel.add(loginButton, gbc);
            
            // Forgot Password Button
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JButton forgotPasswordButton = new JButton("Forgot Password?");
            forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());
            panel.add(forgotPasswordButton, gbc);
            
            add(panel);
        }
        
        private void showForgotPasswordDialog() {
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            JTextField usernameField = new JTextField();
            JLabel securityQuestionLabel = new JLabel();
            JTextField securityAnswerField = new JTextField();
            JPasswordField newPasswordField = new JPasswordField();
            
            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Security Question:"));
            panel.add(securityQuestionLabel);
            panel.add(new JLabel("Answer:"));
            panel.add(securityAnswerField);
            panel.add(new JLabel("New Password:"));
            panel.add(newPasswordField);
            
            usernameField.addActionListener(e -> {
                User user = new UserService().getUserByUsername(usernameField.getText());
                if (user != null) {
                    securityQuestionLabel.setText(user.getSecurityQuestion());
                } else {
                    JOptionPane.showMessageDialog(this, "Username not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            int result = JOptionPane.showConfirmDialog(this, panel, "Reset Password", 
                                                     JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String answer = securityAnswerField.getText();
                String newPassword = new String(newPasswordField.getPassword());
                
                if (new UserService().resetPassword(username, answer, newPassword)) {
                    JOptionPane.showMessageDialog(this, "Password reset successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reset password. Check your answer.", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void openDashboard(User user) {
            switch (user.getRole()) {
                case "ADMIN":
                    new AdminDashboard(user).setVisible(true);
                    break;
                case "LIBRARIAN":
                    new LibrarianDashboard(user).setVisible(true);
                    break;
                case "STUDENT":
                    new StudentDashboard(user).setVisible(true);
                    break;
            }
        }
    }

    static class AdminDashboard extends JFrame {
        private User currentUser;
        
        public AdminDashboard(User user) {
            this.currentUser = user;
            setTitle("Library Management System - Admin Dashboard");
            setSize(900, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            // Manage Users Tab
            tabbedPane.addTab("Manage Users", createManageUsersPanel());
            
            // Reports Tab
            tabbedPane.addTab("Reports", createReportsPanel());
            
            // Logout Button
            JButton logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> {
                new LoginScreen().setVisible(true);
                dispose();
            });
            
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(logoutButton);
            
            add(tabbedPane, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private JPanel createManageUsersPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Toolbar
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addLibrarianButton = new JButton("Add Librarian");
            JButton refreshButton = new JButton("Refresh");
            
            toolbar.add(addLibrarianButton);
            toolbar.add(refreshButton);
            
            // User table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Username", "Role", "Status"}, 0);
            JTable userTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(userTable);
            
            // Action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton toggleStatusButton = new JButton("Toggle Status");
            JButton deleteButton = new JButton("Delete User");
            
            actionPanel.add(toggleStatusButton);
            actionPanel.add(deleteButton);
            
            panel.add(toolbar, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.SOUTH);
            
            // Load users
            refreshUserTable(model);
            
            // Event handlers
            addLibrarianButton.addActionListener(e -> {
                JPanel addPanel = new JPanel(new GridLayout(5, 2, 5, 5));
                addPanel.add(new JLabel("Username:"));
                JTextField usernameField = new JTextField();
                addPanel.add(usernameField);
                addPanel.add(new JLabel("Password:"));
                JPasswordField passwordField = new JPasswordField();
                addPanel.add(passwordField);
                addPanel.add(new JLabel("Security Question:"));
                JTextField questionField = new JTextField("What is your favorite color?");
                addPanel.add(questionField);
                addPanel.add(new JLabel("Answer:"));
                JTextField answerField = new JTextField();
                addPanel.add(answerField);
                
                int result = JOptionPane.showConfirmDialog(this, addPanel, "Add Librarian", 
                                                          JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    String question = questionField.getText();
                    String answer = answerField.getText();
                    
                    if (!username.isEmpty() && !password.isEmpty() && !answer.isEmpty()) {
                        User newUser = new User(0, username, password, "LIBRARIAN", true, question, answer);
                        if (new UserService().addUser(newUser)) {
                            JOptionPane.showMessageDialog(this, "Librarian added successfully!");
                            refreshUserTable(model);
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to add librarian", 
                                                          "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshUserTable(model));
            
            toggleStatusButton.addActionListener(e -> {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int userId = (int) userTable.getValueAt(selectedRow, 0);
                    boolean currentStatus = userTable.getValueAt(selectedRow, 3).equals("Active");
                    
                    if (new UserService().updateUserStatus(userId, !currentStatus)) {
                        refreshUserTable(model);
                    }
                }
            });
            
            deleteButton.addActionListener(e -> {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int userId = (int) userTable.getValueAt(selectedRow, 0);
                    
                    if (new UserService().deleteUser(userId)) {
                        refreshUserTable(model);
                    }
                }
            });
            
            return panel;
        }
        
        private void refreshUserTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<User> users = new UserService().getAllUsers(null);
            for (User user : users) {
                model.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    user.isActive() ? "Active" : "Inactive"
                });
            }
        }
        
        private JPanel createReportsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Report type selection
            JPanel reportTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            String[] reportTypes = {"All Transactions", "Overdue Books", "Monthly Fines"};
            JComboBox<String> reportTypeCombo = new JComboBox<>(reportTypes);
            JButton generateButton = new JButton("Generate Report");
            
            reportTypePanel.add(new JLabel("Report Type:"));
            reportTypePanel.add(reportTypeCombo);
            reportTypePanel.add(generateButton);
            
            // Report display
            DefaultTableModel model = new DefaultTableModel();
            JTable reportTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(reportTable);
            
            panel.add(reportTypePanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Event handler
            generateButton.addActionListener(e -> {
                String reportType = (String) reportTypeCombo.getSelectedItem();
                switch (reportType) {
                    case "All Transactions":
                        generateAllTransactionsReport(model);
                        break;
                    case "Overdue Books":
                        generateOverdueBooksReport(model);
                        break;
                    case "Monthly Fines":
                        generateMonthlyFinesReport(model);
                        break;
                }
            });
            
            return panel;
        }
        
        private void generateAllTransactionsReport(DefaultTableModel model) {
            model.setColumnIdentifiers(new Object[]{"Transaction ID", "Book ID", "User ID", 
                                                   "Issue Date", "Due Date", "Return Date", "Fine", "Status"});
            model.setRowCount(0);
            
            List<Transaction> transactions = new BookService().getAllTransactions();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Transaction t : transactions) {
                model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getBookId(),
                    t.getUserId(),
                    sdf.format(t.getIssueDate()),
                    sdf.format(t.getDueDate()),
                    t.getReturnDate() != null ? sdf.format(t.getReturnDate()) : "N/A",
                    t.getFine(),
                    t.getStatus()
                });
            }
        }
        
        private void generateOverdueBooksReport(DefaultTableModel model) {
            model.setColumnIdentifiers(new Object[]{"Transaction ID", "Book ID", "User ID", 
                                                   "Due Date", "Days Overdue", "Estimated Fine"});
            model.setRowCount(0);
            
            List<Transaction> transactions = new BookService().getOverdueTransactions();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            
            for (Transaction t : transactions) {
                long daysOverdue = (today.getTime() - t.getDueDate().getTime()) / (1000 * 60 * 60 * 24);
                double fine = daysOverdue * 5.0;
                
                model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getBookId(),
                    t.getUserId(),
                    sdf.format(t.getDueDate()),
                    daysOverdue,
                    "$" + fine
                });
            }
        }
        
        private void generateMonthlyFinesReport(DefaultTableModel model) {
            model.setColumnIdentifiers(new Object[]{"Month", "Total Fines Collected", "Number of Transactions"});
            model.setRowCount(0);
            
            String sql = "SELECT DATE_FORMAT(return_date, '%Y-%m') AS month, " +
                         "SUM(fine) AS total_fines, COUNT(*) AS transaction_count " +
                         "FROM transactions WHERE return_date IS NOT NULL " +
                         "GROUP BY DATE_FORMAT(return_date, '%Y-%m') " +
                         "ORDER BY month";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("month"),
                        "$" + rs.getDouble("total_fines"),
                        rs.getInt("transaction_count")
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        private JPanel createSettingsPanel() {
            JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Dark Mode Toggle
            JPanel darkModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JCheckBox darkModeCheckbox = new JCheckBox("Enable Dark Mode");
            darkModeCheckbox.addActionListener(e -> {
                boolean darkMode = darkModeCheckbox.isSelected();
                if (darkMode) {
                    UIManager.put("control", new Color(50, 50, 50));
                    UIManager.put("text", Color.WHITE);
                    UIManager.put("nimbusBase", new Color(18, 30, 49));
                    UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                    UIManager.put("nimbusDisabledText", new Color(150, 150, 150));
                    UIManager.put("nimbusFocus", new Color(115, 164, 209));
                    UIManager.put("nimbusLightBackground", new Color(30, 30, 30));
                    UIManager.put("nimbusSelectionBackground", new Color(65, 65, 65));
                    UIManager.put("textBackground", new Color(30, 30, 30));
                } else {
                    UIManager.put("control", Color.white);
                    UIManager.put("text", Color.black);
                    UIManager.put("nimbusBase", new Color(51, 98, 140));
                    UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                    UIManager.put("nimbusDisabledText", new Color(142, 143, 145));
                    UIManager.put("nimbusFocus", new Color(115, 164, 209));
                    UIManager.put("nimbusLightBackground", Color.white);
                    UIManager.put("nimbusSelectionBackground", new Color(57, 105, 138));
                    UIManager.put("textBackground", Color.white);
                }
                SwingUtilities.updateComponentTreeUI(this);
            });
            darkModePanel.add(darkModeCheckbox);
            
            // Change Password
            JPanel changePasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton changePasswordButton = new JButton("Change Password");
            changePasswordButton.addActionListener(e -> {
                JPanel passwordPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                passwordPanel.add(new JLabel("Current Password:"));
                JPasswordField currentPasswordField = new JPasswordField();
                passwordPanel.add(currentPasswordField);
                passwordPanel.add(new JLabel("New Password:"));
                JPasswordField newPasswordField = new JPasswordField();
                passwordPanel.add(newPasswordField);
                passwordPanel.add(new JLabel("Confirm New Password:"));
                JPasswordField confirmPasswordField = new JPasswordField();
                passwordPanel.add(confirmPasswordField);
                
                int result = JOptionPane.showConfirmDialog(this, passwordPanel, 
                                                         "Change Password", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String currentPassword = new String(currentPasswordField.getPassword());
                    String newPassword = new String(newPasswordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());
                    
                    if (!newPassword.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(this, "New passwords don't match", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    User authenticatedUser = new UserService().authenticate(currentUser.getUsername(), currentPassword);
                    if (authenticatedUser != null) {
                        if (new UserService().updatePassword(currentUser.getUserId(), newPassword)) {
                            JOptionPane.showMessageDialog(this, "Password changed successfully!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to change password", 
                                                          "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Current password is incorrect", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            changePasswordPanel.add(changePasswordButton);
            
            // Backup
            JPanel backupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton backupButton = new JButton("Backup Database");
            backupButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Backup Location");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String backupPath = fileChooser.getSelectedFile().getAbsolutePath();
                    try {
                        ProcessBuilder pb = new ProcessBuilder(
                            "mysqldump", 
                            "-u", DB_USER, 
                            "-p" + DB_PASSWORD, 
                            "library_db"
                        );
                        pb.redirectOutput(new java.io.File(backupPath + "/library_backup_" + 
                            new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".sql"));
                        Process process = pb.start();
                        process.waitFor();
                        JOptionPane.showMessageDialog(this, "Database backup completed successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            backupPanel.add(backupButton);
            
            // Restore
            JPanel restorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton restoreButton = new JButton("Restore Database");
            restoreButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Backup File");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Files", "sql"));
                
                int userSelection = fileChooser.showOpenDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String backupFile = fileChooser.getSelectedFile().getAbsolutePath();
                    try {
                        ProcessBuilder pb = new ProcessBuilder(
                            "mysql", 
                            "-u", DB_USER, 
                            "-p" + DB_PASSWORD, 
                            "library_db"
                        );
                        pb.redirectInput(new java.io.File(backupFile));
                        Process process = pb.start();
                        process.waitFor();
                        JOptionPane.showMessageDialog(this, "Database restore completed successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage(), 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            restorePanel.add(restoreButton);
            
            panel.add(darkModePanel);
            panel.add(changePasswordPanel);
            panel.add(backupPanel);
            panel.add(restorePanel);
            
            return panel;
        }
    }

    static class LibrarianDashboard extends JFrame {
        private User currentUser;
        
        public LibrarianDashboard(User user) {
            this.currentUser = user;
            setTitle("Library Management System - Librarian Dashboard");
            setSize(900, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            // Book Management Tab
            tabbedPane.addTab("Manage Books", createBookManagementPanel());
            
            // Issue/Return Books Tab
            tabbedPane.addTab("Issue/Return Books", createIssueReturnPanel());
            
            // Requests Tab
            tabbedPane.addTab("Manage Requests", createRequestsPanel());
            
            // Overdue Books Tab
            tabbedPane.addTab("Overdue Books", createOverdueBooksPanel());
            
            // Settings Tab
            tabbedPane.addTab("Settings", createSettingsPanel());
            
            // Logout Button
            JButton logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> {
                new LoginScreen().setVisible(true);
                dispose();
            });
            
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(logoutButton);
            
            add(tabbedPane, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private JPanel createBookManagementPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Toolbar
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addBookButton = new JButton("Add Book");
            JButton deleteBookButton = new JButton("Delete Book");
            JButton refreshButton = new JButton("Refresh");
            
            toolbar.add(addBookButton);
            toolbar.add(deleteBookButton);
            toolbar.add(refreshButton);
            
            // Book table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "Status"}, 0);
            JTable bookTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(bookTable);
            
            panel.add(toolbar, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Load books
            refreshBookTable(model);
            
            // Event handlers
            addBookButton.addActionListener(e -> {
                JPanel addPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                addPanel.add(new JLabel("Title:"));
                JTextField titleField = new JTextField();
                addPanel.add(titleField);
                addPanel.add(new JLabel("Author:"));
                JTextField authorField = new JTextField();
                addPanel.add(authorField);
                
                int result = JOptionPane.showConfirmDialog(this, addPanel, "Add Book", 
                                                          JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String title = titleField.getText();
                    String author = authorField.getText();
                    
                    if (!title.isEmpty() && !author.isEmpty()) {
                        if (new BookService().addBook(title, author)) {
                            JOptionPane.showMessageDialog(this, "Book added successfully!");
                            refreshBookTable(model);
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to add book", 
                                                          "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
            deleteBookButton.addActionListener(e -> {
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int bookId = (int) bookTable.getValueAt(selectedRow, 0);
                    
                    if (new BookService().deleteBook(bookId)) {
                        refreshBookTable(model);
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshBookTable(model));
            
            return panel;
        }
        
        private void refreshBookTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<Book> books = new BookService().getAllBooks();
            for (Book book : books) {
                model.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.isAvailable() ? "Available" : "Checked Out"
                });
            }
        }
        
        private JPanel createIssueReturnPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Tabbed pane for issue/return
            JTabbedPane issueReturnTabs = new JTabbedPane();
            
            // Issue Book Tab
            JPanel issuePanel = new JPanel(new GridLayout(4, 2, 5, 5));
            issuePanel.add(new JLabel("Book ID:"));
            JTextField issueBookIdField = new JTextField();
            issuePanel.add(issueBookIdField);
            issuePanel.add(new JLabel("Student ID:"));
            JTextField issueStudentIdField = new JTextField();
            issuePanel.add(issueStudentIdField);
            
            JButton issueButton = new JButton("Issue Book");
            issuePanel.add(new JLabel());
            issuePanel.add(issueButton);
            
            issueReturnTabs.addTab("Issue Book", issuePanel);
            
            // Return Book Tab
            JPanel returnPanel = new JPanel(new GridLayout(5, 2, 5, 5));
            returnPanel.add(new JLabel("Transaction ID:"));
            JTextField returnTransIdField = new JTextField();
            returnPanel.add(returnTransIdField);
            
            JLabel fineLabel = new JLabel("Fine: $0.00");
            returnPanel.add(new JLabel("Calculated Fine:"));
            returnPanel.add(fineLabel);
            
            JButton calculateFineButton = new JButton("Calculate Fine");
            returnPanel.add(calculateFineButton);
            returnPanel.add(new JLabel());
            
            JButton returnButton = new JButton("Return Book");
            returnPanel.add(new JLabel());
            returnPanel.add(returnButton);
            
            issueReturnTabs.addTab("Return Book", returnPanel);
            
            panel.add(issueReturnTabs, BorderLayout.CENTER);
            
            // Event handlers
            issueButton.addActionListener(e -> {
                try {
                    int bookId = Integer.parseInt(issueBookIdField.getText());
                    int studentId = Integer.parseInt(issueStudentIdField.getText());
                    
                    if (new BookService().issueBook(bookId, studentId)) {
                        JOptionPane.showMessageDialog(this, "Book issued successfully!");
                        issueBookIdField.setText("");
                        issueStudentIdField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to issue book", 
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid IDs", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            calculateFineButton.addActionListener(e -> {
                try {
                    int transId = Integer.parseInt(returnTransIdField.getText());
                    List<Transaction> transactions = new BookService().getAllTransactions();
                    
                    for (Transaction t : transactions) {
                        if (t.getTransactionId() == transId && t.getReturnDate() == null) {
                            double fine = new BookService().calculateFine(t.getDueDate());
                            fineLabel.setText("Fine: $" + String.format("%.2f", fine));
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Transaction not found or already returned", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid transaction ID", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            returnButton.addActionListener(e -> {
                try {
                    int transId = Integer.parseInt(returnTransIdField.getText());
                    double fine = Double.parseDouble(fineLabel.getText().replace("Fine: $", ""));
                    
                    if (new BookService().returnBook(transId, fine)) {
                        JOptionPane.showMessageDialog(this, "Book returned successfully!");
                        fineLabel.setText("Fine: $0.00");
                        returnTransIdField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to return book", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid data", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            return panel;
        }
        
        private JPanel createRequestsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Requests table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Student ID", "Request Type", 
                                                                      "Book Title", "Date", "Status"}, 0);
            JTable requestsTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(requestsTable);
            
            // Action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton approveButton = new JButton("Approve");
            JButton rejectButton = new JButton("Reject");
            JButton refreshButton = new JButton("Refresh");
            
            actionPanel.add(approveButton);
            actionPanel.add(rejectButton);
            actionPanel.add(refreshButton);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.SOUTH);
            
            // Load requests
            refreshRequestsTable(model);
            
            // Event handlers
            approveButton.addActionListener(e -> {
                int selectedRow = requestsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int requestId = (int) requestsTable.getValueAt(selectedRow, 0);
                    
                    if (new RequestService().updateRequestStatus(requestId, "APPROVED")) {
                        JOptionPane.showMessageDialog(this, "Request approved!");
                        refreshRequestsTable(model);
                    }
                }
            });
            
            rejectButton.addActionListener(e -> {
                int selectedRow = requestsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int requestId = (int) requestsTable.getValueAt(selectedRow, 0);
                    
                    if (new RequestService().updateRequestStatus(requestId, "REJECTED")) {
                        JOptionPane.showMessageDialog(this, "Request rejected!");
                        refreshRequestsTable(model);
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshRequestsTable(model));
            
            return panel;
        }
        
        private void refreshRequestsTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<Request> requests = new RequestService().getPendingRequests();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Request r : requests) {
                model.addRow(new Object[]{
                    r.getRequestId(),
                    r.getUserId(),
                    r.getRequestType(),
                    r.getBookTitle(),
                    sdf.format(r.getRequestDate()),
                    r.getStatus()
                });
            }
        }
        
        private JPanel createOverdueBooksPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Overdue books table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Transaction ID", "Book ID", 
                                                                      "Student ID", "Due Date", "Days Overdue"}, 0);
            JTable overdueTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(overdueTable);
            
            // Action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton notifyButton = new JButton("Notify Student");
            JButton refreshButton = new JButton("Refresh");
            
            actionPanel.add(notifyButton);
            actionPanel.add(refreshButton);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.SOUTH);
            
            // Load overdue books
            refreshOverdueTable(model);
            
            // Event handlers
            notifyButton.addActionListener(e -> {
                int selectedRow = overdueTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int studentId = (int) overdueTable.getValueAt(selectedRow, 2);
                    String message = "Your book is overdue. Please return it as soon as possible to avoid additional fines.";
                    
                    if (new NotificationService().createNotification(studentId, message)) {
                        JOptionPane.showMessageDialog(this, "Notification sent to student!");
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshOverdueTable(model));
            
            return panel;
        }
        
        private void refreshOverdueTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<Transaction> transactions = new BookService().getOverdueTransactions();
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Transaction t : transactions) {
                long daysOverdue = (today.getTime() - t.getDueDate().getTime()) / (1000 * 60 * 60 * 24);
                model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getBookId(),
                    t.getUserId(),
                    sdf.format(t.getDueDate()),
                    daysOverdue
                });
            }
        }
        
        private JPanel createSettingsPanel() {
            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Dark Mode Toggle
            JPanel darkModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JCheckBox darkModeCheckbox = new JCheckBox("Enable Dark Mode");
            darkModeCheckbox.addActionListener(e -> {
                boolean darkMode = darkModeCheckbox.isSelected();
                if (darkMode) {
                    UIManager.put("control", new Color(50, 50, 50));
                    UIManager.put("text", Color.WHITE);
                    UIManager.put("nimbusBase", new Color(18, 30, 49));
                    UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                    UIManager.put("nimbusDisabledText", new Color(150, 150, 150));
                    UIManager.put("nimbusFocus", new Color(115, 164, 209));
                    UIManager.put("nimbusLightBackground", new Color(30, 30, 30));
                    UIManager.put("nimbusSelectionBackground", new Color(65, 65, 65));
                    UIManager.put("textBackground", new Color(30, 30, 30));
                } else {
                    UIManager.put("control", Color.white);
                    UIManager.put("text", Color.black);
                    UIManager.put("nimbusBase", new Color(51, 98, 140));
                    UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                    UIManager.put("nimbusDisabledText", new Color(142, 143, 145));
                    UIManager.put("nimbusFocus", new Color(115, 164, 209));
                    UIManager.put("nimbusLightBackground", Color.white);
                    UIManager.put("nimbusSelectionBackground", new Color(57, 105, 138));
                    UIManager.put("textBackground", Color.white);
                }
                SwingUtilities.updateComponentTreeUI(this);
            });
            darkModePanel.add(darkModeCheckbox);
            
            // Change Password
            JPanel changePasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton changePasswordButton = new JButton("Change Password");
            changePasswordButton.addActionListener(e -> {
                JPanel passwordPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                passwordPanel.add(new JLabel("Current Password:"));
                JPasswordField currentPasswordField = new JPasswordField();
                passwordPanel.add(currentPasswordField);
                passwordPanel.add(new JLabel("New Password:"));
                JPasswordField newPasswordField = new JPasswordField();
                passwordPanel.add(newPasswordField);
                passwordPanel.add(new JLabel("Confirm New Password:"));
                JPasswordField confirmPasswordField = new JPasswordField();
                passwordPanel.add(confirmPasswordField);
                
                int result = JOptionPane.showConfirmDialog(this, passwordPanel, 
                                                         "Change Password", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String currentPassword = new String(currentPasswordField.getPassword());
                    String newPassword = new String(newPasswordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());
                    
                    if (!newPassword.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(this, "New passwords don't match", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    User authenticatedUser = new UserService().authenticate(currentUser.getUsername(), currentPassword);
                    if (authenticatedUser != null) {
                        if (new UserService().updatePassword(currentUser.getUserId(), newPassword)) {
                            JOptionPane.showMessageDialog(this, "Password changed successfully!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to change password", 
                                                          "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Current password is incorrect", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            changePasswordPanel.add(changePasswordButton);
            
            panel.add(darkModePanel);
            panel.add(changePasswordPanel);
            
            return panel;
        }
    }

    static class StudentDashboard extends JFrame {
        private User currentUser;
        
        public StudentDashboard(User user) {
            this.currentUser = user;
            setTitle("Library Management System - Student Dashboard");
            setSize(900, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            // Browse Books Tab
            tabbedPane.addTab("Browse Books", createBrowseBooksPanel());
            
            // My Books Tab
            tabbedPane.addTab("My Books", createMyBooksPanel());
            
            // Requests Tab
            tabbedPane.addTab("Make Requests", createRequestsPanel());
            
            // Notifications Tab
            tabbedPane.addTab("Notifications", createNotificationsPanel());
            
            // Logout Button
            JButton logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> {
                new LoginScreen().setVisible(true);
                dispose();
            });
            
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(logoutButton);
            
            add(tabbedPane, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private JPanel createBrowseBooksPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Book table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "Status"}, 0);
            JTable bookTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(bookTable);
            
            // Action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton borrowButton = new JButton("Borrow Book");
            JButton refreshButton = new JButton("Refresh");
            
            actionPanel.add(borrowButton);
            actionPanel.add(refreshButton);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.SOUTH);
            
            // Load books
            refreshBookTable(model);
            
            // Event handlers
            borrowButton.addActionListener(e -> {
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int bookId = (int) bookTable.getValueAt(selectedRow, 0);
                    
                    if (new BookService().issueBook(bookId, currentUser.getUserId())) {
                        JOptionPane.showMessageDialog(this, "Book borrowed successfully!");
                        refreshBookTable(model);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to borrow book", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshBookTable(model));
            
            return panel;
        }
        
        private void refreshBookTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<Book> books = new BookService().getAllBooks();
            for (Book book : books) {
                model.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.isAvailable() ? "Available" : "Checked Out"
                });
            }
        }
        
        private JPanel createMyBooksPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // My books table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Transaction ID", "Book ID", 
                                                                      "Title", "Issue Date", "Due Date", "Status"}, 0);
            JTable myBooksTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(myBooksTable);
            
            // Action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton returnButton = new JButton("Return Book");
            JButton reissueButton = new JButton("Request Reissue");
            JButton refreshButton = new JButton("Refresh");
            
            actionPanel.add(returnButton);
            actionPanel.add(reissueButton);
            actionPanel.add(refreshButton);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.SOUTH);
            
            // Load my books
            refreshMyBooksTable(model);
            
            // Event handlers
            returnButton.addActionListener(e -> {
                int selectedRow = myBooksTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int transId = (int) myBooksTable.getValueAt(selectedRow, 0);
                    Date dueDate = (Date) myBooksTable.getValueAt(selectedRow, 4);
                    double fine = new BookService().calculateFine(dueDate);
                    
                    if (fine > 0) {
                        JOptionPane.showMessageDialog(this, "Please pay fine of $" + fine + " at the library desk");
                    }
                    
                    if (new BookService().returnBook(transId, fine)) {
                        JOptionPane.showMessageDialog(this, "Book returned successfully!");
                        refreshMyBooksTable(model);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to return book", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            reissueButton.addActionListener(e -> {
                int selectedRow = myBooksTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int bookId = (int) myBooksTable.getValueAt(selectedRow, 1);
                    String bookTitle = (String) myBooksTable.getValueAt(selectedRow, 2);
                    
                    if (new RequestService().createRequest(currentUser.getUserId(), bookTitle, "REISSUE")) {
                        JOptionPane.showMessageDialog(this, "Reissue request submitted!");
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshMyBooksTable(model));
            
            return panel;
        }
        
        private void refreshMyBooksTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<Transaction> transactions = new BookService().getTransactionsByUser(currentUser.getUserId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Transaction t : transactions) {
                Book book = new BookService().getBookById(t.getBookId());
                String title = book != null ? book.getTitle() : "Unknown";
                
                model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getBookId(),
                    title,
                    sdf.format(t.getIssueDate()),
                    sdf.format(t.getDueDate()),
                    t.getStatus()
                });
            }
        }
        
        private JPanel createRequestsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Request type selection
            JPanel requestTypePanel = new JPanel(new GridLayout(4, 2, 5, 5));
            requestTypePanel.add(new JLabel("Request Type:"));
            String[] requestTypes = {"NEW_BOOK", "HOLD"};
            JComboBox<String> requestTypeCombo = new JComboBox<>(requestTypes);
            requestTypePanel.add(requestTypeCombo);
            
            requestTypePanel.add(new JLabel("Book Title:"));
            JTextField bookTitleField = new JTextField();
            requestTypePanel.add(bookTitleField);
            
            JButton submitButton = new JButton("Submit Request");
            requestTypePanel.add(new JLabel());
            requestTypePanel.add(submitButton);
            
            // My requests table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Request ID", "Type", "Book Title", 
                                                                      "Date", "Status"}, 0);
            JTable requestsTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(requestsTable);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(requestTypePanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            
            panel.add(mainPanel, BorderLayout.CENTER);
            
            // Load my requests
            refreshMyRequestsTable(model);
            
            // Event handler
            submitButton.addActionListener(e -> {
                String requestType = (String) requestTypeCombo.getSelectedItem();
                String bookTitle = bookTitleField.getText();
                
                if (!bookTitle.isEmpty()) {
                    if (new RequestService().createRequest(currentUser.getUserId(), bookTitle, requestType)) {
                        JOptionPane.showMessageDialog(this, "Request submitted successfully!");
                        bookTitleField.setText("");
                        refreshMyRequestsTable(model);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to submit request", 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            return panel;
        }
        
        private void refreshMyRequestsTable(DefaultTableModel model) {
            model.setRowCount(0);
            String sql = "SELECT * FROM requests WHERE user_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, currentUser.getUserId());
                ResultSet rs = pstmt.executeQuery();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("request_type"),
                        rs.getString("book_title"),
                        sdf.format(rs.getDate("request_date")),
                        rs.getString("status")
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        private JPanel createNotificationsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Notifications table
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Date", "Message", "Status"}, 0);
            JTable notificationsTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(notificationsTable);
            
            // Action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton markReadButton = new JButton("Mark as Read");
            JButton refreshButton = new JButton("Refresh");
            
            actionPanel.add(markReadButton);
            actionPanel.add(refreshButton);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.SOUTH);
            
            // Load notifications
            refreshNotificationsTable(model);
            
            // Event handlers
            markReadButton.addActionListener(e -> {
                int selectedRow = notificationsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int notificationId = (int) notificationsTable.getValueAt(selectedRow, 0);
                    
                    if (new NotificationService().markAsRead(notificationId)) {
                        refreshNotificationsTable(model);
                    }
                }
            });
            
            refreshButton.addActionListener(e -> refreshNotificationsTable(model));
            
            return panel;
        }
        
        private void refreshNotificationsTable(DefaultTableModel model) {
            model.setRowCount(0);
            List<Notification> notifications = new NotificationService().getUserNotifications(currentUser.getUserId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Notification n : notifications) {
                model.addRow(new Object[]{
                    n.getNotificationId(),
                    sdf.format(n.getDate()),
                    n.getMessage(),
                    n.isReadStatus() ? "Read" : "Unread"
                });
            }
        }
    }
}