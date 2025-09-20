import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

// Main Class
public class HospitalManagementSystem {

    // --- Main Application Frame and Panel Container ---
    private static JFrame mainFrame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    // --- User Session Management ---
    private static String currentAdminUser;
    private static Doctor currentDoctorUser;

    // --- Database Manager Instance ---
    private static final DatabaseManager dbManager = new DatabaseManager();
    private static TempPatientData tempPatientData;
    // --- UI Design and Color Palette ---
    public static final Color COLOR_PRIMARY = new Color(30, 136, 229);
    // A modern, friendly blue
    public static final Color COLOR_SECONDARY = new Color(25, 118, 210);
    // A slightly darker blue for accents
    public static final Color COLOR_BACKGROUND = new Color(244, 246, 249);
    // A light, clean grey
    public static final Color COLOR_FONT_LIGHT = Color.WHITE;
    public static final Color COLOR_FONT_DARK = new Color(51, 51, 51);
    public static final Color COLOR_SUCCESS = new Color(46, 125, 50);
    public static final Color COLOR_DANGER = new Color(211, 47, 47);
    // --- Main Page Background Image Path ---
    private static final String BACKGROUND_IMAGE_PATH = "https://ik.imagekit.io/1lb1vkk2o/WhatsApp%20Image%202025-08-30%20at%2017.52.54_96e24c43.jpg?updatedAt=1758033647291"; // Example: "C:/Users/YourUser/Pictures/background.jpg"

    /**
     * The main entry point of the application.
     * Initializes the main frame and sets up the user interface.
     */
    public static void main(String[] args) {
        // Use Swing's event dispatch thread to build the UI
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("SALVE Memorial Hospital - Management System");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1280, 800);
            mainFrame.setLocationRelativeTo(null); // Center the frame


            // Set up a background panel with an image from a URL
            try {
                // FIX: Changed from new URL() to new URI().toURL() for modern compatibility
                URL imageUrl = new URI(BACKGROUND_IMAGE_PATH).toURL();
                BufferedImage backgroundImage = ImageIO.read(imageUrl); // Read the image from the URL
                if (backgroundImage == null) {
                    throw new IOException("Failed to load image from URL.");
                }
                BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
                mainFrame.setContentPane(backgroundPanel);
            } catch (MalformedURLException e) {
                System.err.println("Invalid URL format: " + e.getMessage());
                mainFrame.getContentPane().setBackground(COLOR_BACKGROUND); // Fallback to solid color
            } catch (IOException e) {
                System.err.println("Could not load background image from URL: " + e.getMessage());
                System.err.println("Please update BACKGROUND_IMAGE_PATH to a valid image URL.");
                mainFrame.getContentPane().setBackground(COLOR_BACKGROUND); // Fallback to solid color
            } catch (URISyntaxException e) {
                // FIX: Added catch block for the new URI(...).toURL() method
                System.err.println("Invalid URI syntax: " + e.getMessage());
                mainFrame.getContentPane().setBackground(COLOR_BACKGROUND);
            }

            // Use CardLayout to switch between different pages (panels)
            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);
            mainPanel.setOpaque(false); // Make it transparent to see the background

            // Add all pages to the main panel
            mainPanel.add(new MainPage(), "MainPage");
            mainPanel.add(new LoginPage(), "LoginPage");
            mainPanel.add(new AdminLoginPage(), "AdminLoginPage");
            mainPanel.add(new DoctorLoginPage(), "DoctorLoginPage");
            mainPanel.add(new PatientLoginPage(), "PatientLoginPage");
            mainPanel.add(new AppointmentBookingPage1(), "AppointmentBookingPage1");
            mainPanel.add(new EnhancedOnlinePaymentPage(), "OnlinePaymentPage");
            mainPanel.add(new OnlinePharmacyPage(), "OnlinePharmacyPage");
            mainPanel.add(new PatientMedicinePage(), "PatientMedicinePage");
            mainPanel.add(new PharmacyStockPage(), "PharmacyStockPage");

            mainFrame.getContentPane().add(mainPanel);
            mainFrame.setVisible(true);
        });
    }

    public static void showConsultationPanel(Doctor doctor, Patient patient, Consultation consultation) {
        ConsultationPanel consultationPanel = new ConsultationPanel(doctor, patient, consultation);
        mainPanel.add(consultationPanel, "ConsultationPanel");
        cardLayout.show(mainPanel, "ConsultationPanel");
    }

    /**
     * Navigates to a specified page within the application.
     *
     * @param panelName The name of the JPanel to display.
     */
    public static void showPage(String panelName) {
        JPanel newPage;
        // For pages that need user data, create a new instance
        switch (panelName) {
            case "AdminPage":
                newPage = new AdminPage(currentAdminUser);
                break;
            case "DoctorPage":
                newPage = new DoctorPage(currentDoctorUser);
                break;
            case "DoctorStatusPage":
                newPage = new DoctorStatusPage();
                break;
            case "DoctorManagementPage":
                newPage = new DoctorManagementPage();
                break;
            case "AppointmentsViewPageAdmin":
                newPage = new AppointmentsViewPage(true, null);
                break;
            case "PatientDetailsPageAdmin":
                newPage = new PatientDetailsPage(true, null);
                break;
            case "UserManagementPage":
                newPage = new UserManagementPage();
                break;
            case "PatientMedicinePage":      // NEW
                newPage = new PatientMedicinePage();
                break;
            case "PharmacyStockPage":        // NEW
                newPage = new PharmacyStockPage();
                break;
            default:
                // For static pages, just show them
                cardLayout.show(mainPanel, panelName);
                return;
        }
        mainPanel.add(newPage, panelName);
        cardLayout.show(mainPanel, panelName);
    }

    public static void showAddressPage(Map<Medicine, Integer> cart) {
        AddressPage addressPage = new AddressPage(cart);
        mainPanel.add(addressPage, "AddressPage");
        cardLayout.show(mainPanel, "AddressPage");
    }

    public static void showPharmacyPaymentPage(Map<Medicine, Integer> cart, String address) {
        PharmacyPaymentPage paymentPage = new PharmacyPaymentPage(cart, address);
        mainPanel.add(paymentPage, "PharmacyPaymentPage");
        cardLayout.show(mainPanel, "PharmacyPaymentPage");
    }

    public static void showPharmacyEnhancedPaymentPage(Map<Medicine, Integer> cart, String address, double totalAmount) {
        EnhancedOnlinePaymentPage paymentPage = new EnhancedOnlinePaymentPage(cart, address, totalAmount, true);
        mainPanel.add(paymentPage, "PharmacyEnhancedPaymentPage");
        cardLayout.show(mainPanel, "PharmacyEnhancedPaymentPage");
    }

    public static class TempPatientData {
        public String name;
        public String contactNumber;
        public int age;
        public String symptoms;

        public TempPatientData(String name, String contactNumber, int age) {
            this.name = name;
            this.contactNumber = contactNumber;
            this.age = age;
        }
    }

    /**
     * Shows pages specific to a logged-in doctor.
     *
     * @param panelName The page to show.
     * @param doctor    The currently logged-in doctor.
     */
    public static void showDoctorSpecificPage(String panelName, Doctor doctor) {
        JPanel newPage = null;
        if (panelName.equals("AppointmentsViewPageDoctor")) {
            newPage = new AppointmentsViewPage(false, doctor);
        } else if (panelName.equals("PatientDetailsPageDoctor")) {
            newPage = new PatientDetailsPage(false, doctor);
        }
        if (newPage != null) {
            mainPanel.add(newPage, panelName);
            cardLayout.show(mainPanel, panelName);
        }
    }

    // --- Navigation methods for the multi-step appointment booking process ---

    public static void startAppointmentBooking(Patient patient) {
        AppointmentBookingPage2 page2 = new AppointmentBookingPage2(patient);
        mainPanel.add(page2, "AppointmentBookingPage2");
        cardLayout.show(mainPanel, "AppointmentBookingPage2");
    }

    public static void showDoctorSuggestions(Patient patient, String symptoms) {
        AppointmentBookingPage2_5 page2_5 = new AppointmentBookingPage2_5(patient, symptoms);
        mainPanel.add(page2_5, "AppointmentBookingPage2_5");
        cardLayout.show(mainPanel, "AppointmentBookingPage2_5");
    }

    public static void startAppointmentBookingWithTempData() {
        AppointmentBookingPage2 page2 = new AppointmentBookingPage2(null); // Pass null for temp data
        mainPanel.add(page2, "AppointmentBookingPage2");
        cardLayout.show(mainPanel, "AppointmentBookingPage2");
    }

    public static void showDoctorSuggestionsWithTempData(String symptoms) {
        tempPatientData.symptoms = symptoms;
        AppointmentBookingPage2_5 page25 = new AppointmentBookingPage2_5(null, symptoms); // ✅ Correct - 2 arguments
        mainPanel.add(page25, "AppointmentBookingPage2_5");
        cardLayout.show(mainPanel, "AppointmentBookingPage2_5");
    }

    public static void showDoctorCalendar(Patient patient, Doctor doctor, String reason) {
        AppointmentBookingPage3 page3 = new AppointmentBookingPage3(patient, doctor, reason);
        mainPanel.add(page3, "AppointmentBookingPage3");
        cardLayout.show(mainPanel, "AppointmentBookingPage3");
    }
    public static void showDoctorCalendarWithTempData(Doctor doctor, String reason) {
        // Create a temporary patient for calendar navigation
        Patient tempPatient = new Patient(0,
                tempPatientData.name,
                tempPatientData.contactNumber,
                tempPatientData.age);

        AppointmentBookingPage3 page3 = new AppointmentBookingPage3(tempPatient, doctor, reason);
        mainPanel.add(page3, "AppointmentBookingPage3");
        cardLayout.show(mainPanel, "AppointmentBookingPage3");
    }

    public static void showTimeSlots(Patient patient, Doctor doctor, String reason, LocalDate date) {
        AppointmentBookingPage4 page4 = new AppointmentBookingPage4(patient, doctor, reason, date);
        mainPanel.add(page4, "AppointmentBookingPage4");
        cardLayout.show(mainPanel, "AppointmentBookingPage4");
    }

    public static void showPatientInfoPage(Patient patient) {
        PatientInfoPage page = new PatientInfoPage(patient);
        mainPanel.add(page, "PatientInfoPage");
        cardLayout.show(mainPanel, "PatientInfoPage");
    }

    // --- Getters and Setters for session management ---
    public static void setCurrentAdminUser(String username) {
        currentAdminUser = username;
    }

    public static void setCurrentDoctorUser(Doctor doctor) {
        currentDoctorUser = doctor;
    }

    public static DatabaseManager getDbManager() {
        return dbManager;
    }

    // =================================================================================
    // FIX: All classes below are now static inner classes of HospitalManagementSystem
    // This allows them to correctly access static members like `showPage` and `dbManager`.
    // =================================================================================

    // Data Models
    public static class Doctor {
        private String id, name, specialization, username;
        private boolean isOnDuty;

        public Doctor(String id, String name, String specialization, String username, boolean isOnDuty) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.username = username;
            this.isOnDuty = isOnDuty;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSpecialization() {
            return specialization;
        }

        public String getUsername() {
            return username;
        }

        public boolean isOnDuty() {
            return isOnDuty;
        }
    }

    public static class Patient {
        private int id;
        private String name, contactNumber;
        private int age; // NEW: Add age field

        public Patient(int id, String name, String contactNumber) {
            this.id = id;
            this.name = name;
            this.contactNumber = contactNumber;
            this.age = 0; // Default age
        }

        // NEW: Constructor with age
        public Patient(int id, String name, String contactNumber, int age) {
            this.id = id;
            this.name = name;
            this.contactNumber = contactNumber;
            this.age = age;
        }

        // Existing getters and setters
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setId(int id) {
            this.id = id;
        }

        // NEW: Age getter and setter
        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }


    public static class Consultation {
        private String id, doctorId, consultingReason;
        private int patientId;
        private LocalDateTime consultationDateTime;
        private LocalDate nextConsultingDate;

        public Consultation(String id, int pId, String dId, LocalDateTime dt, String reason, LocalDate nextDate) {
            this.id = id;
            this.patientId = pId;
            this.doctorId = dId;
            this.consultationDateTime = dt;
            this.consultingReason = reason;
            this.nextConsultingDate = nextDate;
        }

        public String getId() {
            return id;
        }

        public int getPatientId() {
            return patientId;
        }

        public String getDoctorId() {
            return doctorId;
        }

        public LocalDateTime getConsultationDateTime() {
            return consultationDateTime;
        }

        public String getConsultingReason() {
            return consultingReason;
        }

        public LocalDate getNextConsultingDate() {
            return nextConsultingDate;
        }
    }

    public static class Prescription {
        private String consultationId, medicineName, dosage;

        public Prescription(String cId, String medName, String dosage) {
            this.consultationId = cId;
            this.medicineName = medName;
            this.dosage = dosage;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public String getDosage() {
            return dosage;
        }
    }

    public static class Medicine {
        private int id;
        private String name;
        private double price;
        private int stock;

        public Medicine(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getStock() {
            return stock;
        }
    }

    public static class MedicalTest {
        private int id;
        private String name, description, category;
        private double price;

        public MedicalTest(int id, String name, String description, double price, String category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getPrice() {
            return price;
        }

        public String getCategory() {
            return category;
        }
    }

    // Database Manager
    public static class DatabaseManager {
        private static final String JDBC_URL = "jdbc:mysql://salvehospital-salvehospital.d.aivencloud.com:28434/salve_memorial_hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        private static final String USER = "avnadmin";
        private static final String PASSWORD = "AVNS_ifCRh-mpXcyrldibIv0";

        public DatabaseManager() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                this.createDatabaseIfNotExists();
                this.createTables();
                this.addSampleData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public Connection getConnection() throws SQLException {
            try {
                return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            } catch (SQLException e) {
                System.err.println("Connection failed to: " + JDBC_URL);
                throw e;
            }
        }

        // Use this method wherever you need a connection:
        public void testConnection() {
            try (Connection con = getConnection()) {
                System.out.println("Connected Successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void createDatabaseIfNotExists() throws SQLException {
            String createDbUrl = "jdbc:mysql://salvehospital-salvehospital.d.aivencloud.com:28434/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection conn = DriverManager.getConnection(createDbUrl, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS salve_memorial_hospital");
            }
        }

        private void createTables() throws SQLException {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(255) PRIMARY KEY, password VARCHAR(255) NOT NULL, role VARCHAR(50) NOT NULL)");
                stmt.execute("CREATE TABLE IF NOT EXISTS doctors (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255) NOT NULL, specialization VARCHAR(255) NOT NULL, username VARCHAR(255) UNIQUE NOT NULL, is_on_duty BOOLEAN DEFAULT FALSE, FOREIGN KEY (username) REFERENCES users(username))");
                stmt.execute("CREATE TABLE IF NOT EXISTS patients (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, contact_number VARCHAR(20))");
                stmt.execute("CREATE TABLE IF NOT EXISTS consultations (id VARCHAR(255) PRIMARY KEY, patient_id INT NOT NULL, doctor_id VARCHAR(255) NOT NULL, consultation_datetime TIMESTAMP NOT NULL, consulting_reason TEXT NOT NULL, next_consulting_date DATE, FOREIGN KEY (patient_id) REFERENCES patients(id), FOREIGN KEY (doctor_id) REFERENCES doctors(id))");
                stmt.execute("CREATE TABLE IF NOT EXISTS prescriptions (id INT PRIMARY KEY AUTO_INCREMENT, consultation_id VARCHAR(255) NOT NULL, medicine_name VARCHAR(255) NOT NULL, dosage VARCHAR(255), FOREIGN KEY (consultation_id) REFERENCES consultations(id))");
                stmt.execute("CREATE TABLE IF NOT EXISTS doctor_log_status (id VARCHAR(255) PRIMARY KEY, doctor_id VARCHAR(255) NOT NULL, login_time TIMESTAMP NOT NULL, logout_time TIMESTAMP NULL DEFAULT NULL, FOREIGN KEY (doctor_id) REFERENCES doctors(id))");
                stmt.execute("CREATE TABLE IF NOT EXISTS doctor_availability (id INT PRIMARY KEY AUTO_INCREMENT, doctor_id VARCHAR(255) NOT NULL, day_of_week VARCHAR(20) NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, FOREIGN KEY (doctor_id) REFERENCES doctors(id))");
                stmt.execute("CREATE TABLE IF NOT EXISTS medicines (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, price DOUBLE NOT NULL, stock INT NOT NULL)");
                stmt.execute("CREATE TABLE IF NOT EXISTS medical_tests (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, description TEXT, price DOUBLE NOT NULL, category VARCHAR(100))");
                stmt.execute("CREATE TABLE IF NOT EXISTS consultation_tests (id INT PRIMARY KEY AUTO_INCREMENT, consultation_id VARCHAR(255) NOT NULL, test_id INT NOT NULL, quantity INT DEFAULT 1, FOREIGN KEY (consultation_id) REFERENCES consultations(id), FOREIGN KEY (test_id) REFERENCES medical_tests(id))");

            }
        }

        private void addSampleData() {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) > 0) return;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            addUser("admin", "admin123", "Admin");
            addUser("asmith", "doc123", "Doctor");
            addUser("bjohnson", "doc123", "Doctor");
            addUser("cjones", "doc123", "Doctor");
            Doctor doc1 = new Doctor(UUID.randomUUID().toString(), "Alice Smith", "Cardiology", "asmith", false);
            Doctor doc2 = new Doctor(UUID.randomUUID().toString(), "Bob Johnson", "Pediatrics", "bjohnson", false);
            Doctor doc3 = new Doctor(UUID.randomUUID().toString(), "Carol Jones", "General Medicine", "cjones", false);
            addDoctor(doc1);
            addDoctor(doc2);
            addDoctor(doc3);

            addDoctorAvailability(doc1.getId(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
            addDoctorAvailability(doc1.getId(), DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
            addDoctorAvailability(doc2.getId(), DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
            addDoctorAvailability(doc3.getId(), DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
            addMedicine("Paracetamol", 20.00, 100);
            addMedicine("Aspirin", 35.50, 50);
            addMedicine("Ibuprofen", 45.00, 75);
            addSampleTestData();
        }

        private void addSampleTestData() {
            addMedicalTest("Blood Test - Complete Blood Count (CBC)", "Comprehensive blood analysis including RBC, WBC, platelets", 450.00, "Blood Tests");
            addMedicalTest("X-Ray Chest", "Chest X-ray to examine lungs and heart", 350.00, "Radiology");
            addMedicalTest("ECG (Electrocardiogram)", "Heart rhythm and electrical activity test", 300.00, "Cardiology");
            addMedicalTest("Urine Analysis", "Complete urine examination", 200.00, "Pathology");
            addMedicalTest("Blood Sugar (Fasting)", "Fasting blood glucose level test", 150.00, "Blood Tests");
            addMedicalTest("Lipid Profile", "Cholesterol and triglycerides analysis", 500.00, "Blood Tests");
            addMedicalTest("Ultrasound Abdomen", "Abdominal organ imaging", 800.00, "Radiology");
            addMedicalTest("Thyroid Function Test", "TSH, T3, T4 hormone levels", 600.00, "Endocrinology");
        }

        public void addMedicalTest(String name, String description, double price, String category) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO medical_tests (name, description, price, category) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setDouble(3, price);
                pstmt.setString(4, category);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public boolean createPatientWithAppointment(Patient patient, String doctorId, String reason, LocalDateTime dateTime) {
            Connection conn = null;
            try {
                conn = getConnection();
                conn.setAutoCommit(false); // Start transaction

                // Insert patient
                PreparedStatement patientStmt = conn.prepareStatement(
                        "INSERT INTO patients (name, contactnumber, age) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                patientStmt.setString(1, patient.getName());
                patientStmt.setString(2, patient.getContactNumber());
                patientStmt.setInt(3, patient.getAge());
                patientStmt.executeUpdate();

                ResultSet rs = patientStmt.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                int patientId = rs.getInt(1);

                // Insert consultation
                PreparedStatement consultStmt = conn.prepareStatement(
                        "INSERT INTO consultations (id, patientid, doctorid, consultationdatetime, consultingreason) VALUES (?, ?, ?, ?, ?)");
                consultStmt.setString(1, UUID.randomUUID().toString());
                consultStmt.setInt(2, patientId);
                consultStmt.setString(3, doctorId);
                consultStmt.setTimestamp(4, Timestamp.valueOf(dateTime));
                consultStmt.setString(5, reason);
                consultStmt.executeUpdate();

                conn.commit(); // Commit transaction
                return true;

            } catch (SQLException e) {
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        public void addUser(String username, String password, String role) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO users (username, password, role) VALUES (?, ?, ?)")) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, role);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void addDoctor(Doctor doctor) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO doctors (id, name, specialization, username, is_on_duty) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setString(1, doctor.getId());
                pstmt.setString(2, doctor.getName());
                pstmt.setString(3, doctor.getSpecialization());
                pstmt.setString(4, doctor.getUsername());
                pstmt.setBoolean(5, doctor.isOnDuty());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void addDoctorAvailability(String doctorId, DayOfWeek day, LocalTime start, LocalTime end) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO doctor_availability (doctor_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, doctorId);
                pstmt.setString(2, day.toString());
                pstmt.setTime(3, Time.valueOf(start));
                pstmt.setTime(4, Time.valueOf(end));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void addMedicine(String name, double price, int stock) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO medicines (name, price, stock) VALUES (?, ?, ?)")) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.setInt(3, stock);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public int addPatient(Patient patient) {
            String sql = "INSERT INTO patients (name, contact_number) VALUES (?, ?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, patient.getName());
                pstmt.setString(2, patient.getContactNumber());
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public void recordConsultation(int pId, String dId, String reason, LocalDateTime dateTime, LocalDate nextDate) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO consultations (id, patient_id, doctor_id, consultation_datetime, consulting_reason, next_consulting_date) VALUES (?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, UUID.randomUUID().toString());
                pstmt.setInt(2, pId);
                pstmt.setString(3, dId);
                pstmt.setTimestamp(4, Timestamp.valueOf(dateTime));
                pstmt.setString(5, reason);
                pstmt.setDate(6, (nextDate != null) ? Date.valueOf(nextDate) : null);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<Doctor> getAllDoctors() {
            List<Doctor> doctors = new ArrayList<>();
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM doctors")) {
                while (rs.next()) {
                    doctors.add(new Doctor(rs.getString("id"), rs.getString("name"), rs.getString("specialization"), rs.getString("username"), rs.getBoolean("is_on_duty")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return doctors;
        }

        public List<Doctor> getDoctorsOnDuty() {
            List<Doctor> onDutyDoctors = new ArrayList<>();
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM doctors WHERE is_on_duty = TRUE")) {
                while (rs.next()) {
                    onDutyDoctors.add(new Doctor(rs.getString("id"), rs.getString("name"), rs.getString("specialization"), rs.getString("username"), rs.getBoolean("is_on_duty")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return onDutyDoctors;
        }

        public List<Doctor> getSuggestedDoctors(String symptoms) {
            List<Doctor> suggestedDoctors = new ArrayList<>();
            String specialization = determineSpecializationFromSymptoms(symptoms);

            // First, try to get doctors with the specific specialization
            String sql = "SELECT * FROM doctors WHERE specialization = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, specialization);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    suggestedDoctors.add(new Doctor(rs.getString("id"), rs.getString("name"), rs.getString("specialization"), rs.getString("username"), rs.getBoolean("is_on_duty")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // If no doctors found with specific specialization, show General Medicine doctors
            if (suggestedDoctors.isEmpty()) {
                sql = "SELECT * FROM doctors WHERE specialization = 'General Medicine'";
                try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        suggestedDoctors.add(new Doctor(rs.getString("id"), rs.getString("name"), rs.getString("specialization"), rs.getString("username"), rs.getBoolean("is_on_duty")));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return suggestedDoctors;
        }

        private String determineSpecializationFromSymptoms(String symptoms) {
            symptoms = symptoms.toLowerCase();
            if (symptoms.contains("heart") || symptoms.contains("chest") || symptoms.contains("cardiac"))
                return "Cardiology";
            if (symptoms.contains("child") || symptoms.contains("baby")) return "Pediatrics";
            return "General Medicine";
        }

        public List<Patient> getAllPatients() {
            List<Patient> patients = new ArrayList<>();
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM patients")) {
                while (rs.next()) {
                    patients.add(new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("contact_number")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return patients;
        }

        public List<DayOfWeek> getDoctorAvailableDays(String doctorId) {
            List<DayOfWeek> days = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT day_of_week FROM doctor_availability WHERE doctor_id = ?")) {
                pstmt.setString(1, doctorId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    days.add(DayOfWeek.valueOf(rs.getString("day_of_week")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return days;
        }

        public List<LocalTime> getAvailableTimeSlotsForDay(String doctorId, DayOfWeek day) {
            List<LocalTime> slots = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT start_time, end_time FROM doctor_availability WHERE doctor_id = ? AND day_of_week = ?")) {
                pstmt.setString(1, doctorId);
                pstmt.setString(2, day.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    LocalTime start = rs.getTime("start_time").toLocalTime();
                    LocalTime end = rs.getTime("end_time").toLocalTime();
                    while (start.isBefore(end)) {
                        slots.add(start);
                        start = start.plusMinutes(30);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return slots;
        }

        public List<Medicine> searchMedicines(String query) {
            List<Medicine> medicines = new ArrayList<>();
            String sql = "SELECT * FROM medicines WHERE LOWER(name) LIKE LOWER(?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + query + "%");
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    medicines.add(new Medicine(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return medicines;
        }

        public void recordDoctorLogin(String doctorId) {
            recordDoctorLogout(doctorId);
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO doctor_log_status (id, doctor_id, login_time) VALUES (?, ?, ?)")) {
                pstmt.setString(1, UUID.randomUUID().toString());
                pstmt.setString(2, doctorId);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void recordDoctorLogout(String doctorId) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE doctor_log_status SET logout_time = ? WHERE doctor_id = ? AND logout_time IS NULL")) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(2, doctorId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void updateDoctorDutyStatus(String doctorId, boolean isOnDuty) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE doctors SET is_on_duty = ? WHERE id = ?")) {
                pstmt.setBoolean(1, isOnDuty);
                pstmt.setString(2, doctorId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public boolean isDoctorOnDuty(String doctorId) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT is_on_duty FROM doctors WHERE id = ?")) {
                pstmt.setString(1, doctorId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getBoolean("is_on_duty");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        public String[] getDoctorCurrentLoginStatus(String doctorId) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT login_time, logout_time FROM doctor_log_status WHERE doctor_id = ? ORDER BY login_time DESC LIMIT 1")) {
                pstmt.setString(1, doctorId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Timestamp logoutTime = rs.getTimestamp("logout_time");
                    Timestamp loginTime = rs.getTimestamp("login_time");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    if (logoutTime == null) {
                        return new String[]{"🟢 Logged In", loginTime.toLocalDateTime().format(formatter)};
                    } else {
                        return new String[]{"🔴 Logged Out", logoutTime.toLocalDateTime().format(formatter)};
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new String[]{"❓ Unknown", "N/A"};
        }

        public Patient getPatientById(int patientId) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM patients WHERE id = ?")) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next())
                    return new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("contact_number"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Doctor getDoctorById(String doctorId) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM doctors WHERE id = ?")) {
                pstmt.setString(1, doctorId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // FIX: Added missing return statement
                    return new Doctor(rs.getString("id"), rs.getString("name"), rs.getString("specialization"), rs.getString("username"), rs.getBoolean("is_on_duty"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<Consultation> getAllUpcomingAppointments() {
            List<Consultation> appointments = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM consultations WHERE consultation_datetime >= ? ORDER BY consultation_datetime ASC")) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    appointments.add(new Consultation(rs.getString("id"), rs.getInt("patient_id"), rs.getString("doctor_id"), rs.getTimestamp("consultation_datetime").toLocalDateTime(), rs.getString("consulting_reason"), rs.getDate("next_consulting_date") != null ? rs.getDate("next_consulting_date").toLocalDate() : null));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return appointments;
        }

        public List<MedicalTest> getAllMedicalTests() {
            List<MedicalTest> tests = new ArrayList<>();
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM medical_tests ORDER BY category, name")) {
                while (rs.next()) {
                    tests.add(new MedicalTest(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("category")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return tests;
        }

        public void addConsultationTest(String consultationId, int testId, int quantity) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO consultation_tests (consultation_id, test_id, quantity) VALUES (?, ?, ?)")) {
                pstmt.setString(1, consultationId);
                pstmt.setInt(2, testId);
                pstmt.setInt(3, quantity);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<MedicalTest> getTestsForConsultation(String consultationId) {
            List<MedicalTest> tests = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT mt.*, ct.quantity FROM medical_tests mt JOIN consultation_tests ct ON mt.id = ct.test_id WHERE ct.consultation_id = ?")) {
                pstmt.setString(1, consultationId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    tests.add(new MedicalTest(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("category")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return tests;
        }


        public List<Consultation> getUpcomingAppointments(String doctorId) {
            List<Consultation> appointments = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM consultations WHERE doctor_id = ? AND consultation_datetime >= ? ORDER BY consultation_datetime ASC")) {
                pstmt.setString(1, doctorId);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    appointments.add(new Consultation(rs.getString("id"), rs.getInt("patient_id"), rs.getString("doctor_id"), rs.getTimestamp("consultation_datetime").toLocalDateTime(), rs.getString("consulting_reason"), rs.getDate("next_consulting_date") != null ? rs.getDate("next_consulting_date").toLocalDate() : null));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return appointments;
        }

        public List<Patient> getPatientsByName(String name) {
            List<Patient> patients = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM patients WHERE LOWER(name) LIKE LOWER(?)")) {
                pstmt.setString(1, "%" + name + "%");
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    patients.add(new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("contact_number")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return patients;
        }

        public List<Patient> getPatientsByDoctor(String doctorId) {
            List<Patient> patients = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT p.* FROM patients p JOIN consultations c ON p.id = c.patient_id WHERE c.doctor_id = ?")) {
                pstmt.setString(1, doctorId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    patients.add(new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("contact_number")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return patients;
        }

        public String validateUser(String username, String password) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT role FROM users WHERE username = ? AND password = ?")) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getString("role");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Doctor getDoctorByUsername(String username) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM doctors WHERE username = ?")) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Doctor(rs.getString("id"), rs.getString("name"), rs.getString("specialization"), rs.getString("username"), rs.getBoolean("is_on_duty"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Patient getPatientByNameAndId(String name, int id) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM patients WHERE id = ? AND LOWER(name) = LOWER(?)")) {
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("contact_number"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<Consultation> getConsultationsForPatient(int patientId) {
            List<Consultation> consultations = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM consultations WHERE patient_id = ? ORDER BY consultation_datetime DESC")) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    consultations.add(new Consultation(rs.getString("id"), rs.getInt("patient_id"), rs.getString("doctor_id"), rs.getTimestamp("consultation_datetime").toLocalDateTime(), rs.getString("consulting_reason"), rs.getDate("next_consulting_date") != null ? rs.getDate("next_consulting_date").toLocalDate() : null));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return consultations;
        }

        public List<Prescription> getPrescriptionsForConsultation(String consultationId) {
            List<Prescription> prescriptions = new ArrayList<>();
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM prescriptions WHERE consultation_id = ?")) {
                pstmt.setString(1, consultationId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    prescriptions.add(new Prescription(consultationId, rs.getString("medicine_name"), rs.getString("dosage")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return prescriptions;
        }

        public void addPrescription(String consultationId, String medicineName, String dosage) {
            // First, insert the prescription
            String sql = "INSERT INTO prescriptions (consultation_id, medicine_name, dosage) VALUES (?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, consultationId);
                pstmt.setString(2, medicineName);
                pstmt.setString(3, dosage);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            addOrUpdateMedicineToInventory(medicineName);
        }

        private void addOrUpdateMedicineToInventory(String medicineName) {
            // Check if medicine already exists
            String checkSql = "SELECT COUNT(*) FROM medicines WHERE LOWER(name) = LOWER(?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, medicineName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Medicine doesn't exist, add with random price and stock
                    Random random = new Random();
                    double price = 10.0 + (90.0 * random.nextDouble()); // Random between 10.0 and 100.0
                    int stock = 50 + random.nextInt(151);
                    // Random between 50 and 200
                    addMedicine(medicineName, price, stock);
                }
                // If it exists, you could optionally update stock or do nothing
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // UI Classes
    public static class BackgroundPanel extends JPanel {
        private final BufferedImage backgroundImage;

        public BackgroundPanel(BufferedImage backgroundImage) {
            this.backgroundImage = backgroundImage;
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static class MainPage extends JPanel {
        private JLabel scrollingLabel;
        private Timer scrollTimer;
        private int scrollPosition = 0;

        public MainPage() {
            setOpaque(false);
            setLayout(new BorderLayout());
            // --- Top Navigation Panel ---
            JPanel navigationPanel = new JPanel(new BorderLayout());
            navigationPanel.setOpaque(false);
            navigationPanel.setBorder(new EmptyBorder(15, 40, 15, 40));
            // Hospital Logo and Name
            JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            logoPanel.setOpaque(false);
            JLabel logoIcon = new JLabel("🏥");
            logoIcon.setFont(new Font("SansSerif", Font.BOLD, 32));
            logoIcon.setForeground(Color.WHITE);
            JLabel hospitalName = new JLabel("SALVE Memorial Hospital");
            hospitalName.setFont(new Font("Serif", Font.BOLD, 28));
            hospitalName.setForeground(Color.WHITE);
            logoPanel.add(logoIcon);
            logoPanel.add(hospitalName);

            // Navigation Menu
            JPanel navMenu = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
            navMenu.setOpaque(false);
            // Login Button
            JButton loginButton = new JButton("Login");
            stylePrimaryButton(loginButton);
            loginButton.addActionListener(e -> HospitalManagementSystem.showPage("LoginPage"));
            navigationPanel.add(logoPanel, BorderLayout.WEST);
            navigationPanel.add(navMenu, BorderLayout.CENTER);
            navigationPanel.add(loginButton, BorderLayout.EAST);
            add(navigationPanel, BorderLayout.NORTH);

            // --- Main Content Panel ---
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

            // Hero Section
            JPanel heroSection = new JPanel(new BorderLayout());
            heroSection.setOpaque(false);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel mainTitle = new JLabel("Your Health Is");
            mainTitle.setFont(new Font("", Font.BOLD, 48));
            mainTitle.setForeground(Color.WHITE);
            mainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel subTitle = new JLabel("Our Priority.");
            subTitle.setFont(new Font("SansSerif", Font.BOLD, 48));
            subTitle.setForeground(new Color(100, 149, 237));
            // Cornflower blue
            subTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel description = new JLabel("<html>Comprehensive healthcare services with experienced doctors<br>" +
                    "and state-of-the-art facilities. Your health is our mission<br>" +
                    "and we're committed to providing quality care.</html>");
            description.setFont(new Font("SansSerif", Font.PLAIN, 16));
            description.setForeground(new Color(220, 220, 220));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            description.setBorder(new EmptyBorder(20, 0, 30, 0));
            JButton heroButton = new JButton("Book An Appointment");
            stylePrimaryButton(heroButton);
            heroButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            heroButton.addActionListener(e -> HospitalManagementSystem.showPage("AppointmentBookingPage1"));
            heroButton.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(mainTitle);
            textPanel.add(subTitle);
            textPanel.add(description);
            textPanel.add(heroButton);

            heroSection.add(textPanel, BorderLayout.WEST);
            contentPanel.add(heroSection, BorderLayout.CENTER);

            // --- Feature Services Panel ---
            JPanel servicesPanel = new JPanel(new GridLayout(1, 3, 30, 0));
            servicesPanel.setOpaque(false);
            servicesPanel.setBorder(new EmptyBorder(60, 0, 0, 0));

            // Service 1
            JPanel service1 = createServiceBox(
                    "👤",
                    "Patient Portal",
                    "Access your medical records and consultation history",
                    () -> HospitalManagementSystem.showPage("PatientLoginPage")

            );

            // Service 2
            JPanel service2 = createServiceBox(
                    "💳",
                    "Online Bill Pay",
                    "Secure online payment for hospital bills and services",
                    () -> HospitalManagementSystem.showPage("OnlinePaymentPage")
            );
            // Service 3
            JPanel service3 = createServiceBox(
                    "💊",
                    "Online Pharmacy",
                    "Order medicines and health products for delivery",
                    () -> HospitalManagementSystem.showPage("OnlinePharmacyPage")
            );
            servicesPanel.add(service1);
            servicesPanel.add(service2);
            servicesPanel.add(service3);

            contentPanel.add(servicesPanel, BorderLayout.SOUTH);
            add(contentPanel, BorderLayout.CENTER);

            // --- NEW: Scrolling Toll-Free Number Banner at Bottom ---
            add(createScrollingBanner(), BorderLayout.SOUTH);
        }

        private JPanel createScrollingBanner() {
            // Use null layout for absolute positioning
            JPanel bannerPanel = new JPanel(null);
            bannerPanel.setOpaque(false);
            bannerPanel.setPreferredSize(new Dimension(0, 50));
            bannerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

            // Create scrolling label
            scrollingLabel = new JLabel("📞 24/7 Emergency Helpline: 1800-SALVE-HELP (1800-725-834) | Free Consultation: Call Now! | Ambulance Service Available |");
            scrollingLabel.setForeground(Color.WHITE);
            scrollingLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            scrollingLabel.setOpaque(false);

            // Set initial size and position
            Dimension labelSize = scrollingLabel.getPreferredSize();
            scrollingLabel.setBounds(0, 15, labelSize.width, 20); // y=15 to center vertically in 50px height
            bannerPanel.add(scrollingLabel);
            // Initialize scrolling animation immediately
            startScrollingAnimation();

            return bannerPanel;
        }

        private void startScrollingAnimation() {
            scrollTimer = new Timer(50, e -> { // 50ms for smooth animation
                if (scrollingLabel != null) {
                    Container parent = scrollingLabel.getParent();
                    if (parent != null && parent.getWidth() > 0) {

                        int parentWidth = parent.getWidth();
                        int labelWidth = scrollingLabel.getPreferredSize().width;

                        // Move text from right to left
                        scrollPosition -= 2;

                        //
                        // Reset position when text completely moves off left side
                        if (scrollPosition + labelWidth < 0) {
                            scrollPosition = parentWidth; // Start from right side
                        }


                        // Update label position using setLocation (not setBounds)
                        scrollingLabel.setLocation(scrollPosition, 15);
                        parent.repaint();
                    }
                }
            });
            // Start the timer when component is shown
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentShown(java.awt.event.ComponentEvent e) {
                    if (scrollTimer != null && !scrollTimer.isRunning()) {
                        scrollPosition = getWidth(); // Start from right edge

                        scrollTimer.start();
                    }
                }

                @Override
                public void componentHidden(java.awt.event.ComponentEvent e) {
                    if (scrollTimer != null && scrollTimer.isRunning()) {

                        scrollTimer.stop();
                    }
                }
            });
        }

        private JPanel createServiceBox(String icon, String title, String description, Runnable action) {
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBackground(new Color(255, 255, 255, 240));
            box.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(230, 230, 230), 1),
                    new EmptyBorder(25, 20, 25, 20)
            ));
            box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            final JPanel finalBox = box; // For the inner class
            box.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }

                @Override

                public void mouseEntered(MouseEvent e) {
                    finalBox.setBackground(new Color(248, 249, 250));
                    finalBox.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    finalBox.setBackground(new Color(255, 255, 255, 240));

                    finalBox.repaint();
                }
            });
            // Icon with colored background circle
            JPanel iconPanel = new JPanel();
            iconPanel.setOpaque(false);
            iconPanel.setPreferredSize(new Dimension(60, 60));
            iconPanel.setMaximumSize(new Dimension(60, 60));
            iconPanel.setLayout(new BorderLayout());

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            iconLabel.setOpaque(true);
            iconLabel.setBackground(new Color(30, 136, 229, 30));
            iconLabel.setPreferredSize(new Dimension(60, 60));
            iconPanel.add(iconLabel, BorderLayout.CENTER);
            iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            titleLabel.setForeground(new Color(51, 51, 51));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setBorder(new EmptyBorder(15, 0, 10, 0));

            JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
            descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            descLabel.setForeground(new Color(120, 120, 120));
            descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            box.add(iconPanel);
            box.add(titleLabel);
            box.add(descLabel);

            return box;
        }

        private void stylePrimaryButton(JButton button) {
            button.setBackground(new Color(220, 53, 69));
            // Red color like in the image
            button.setForeground(Color.WHITE);
            button.setFont(new Font("SansSerif", Font.BOLD, 14));
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(12, 25, 12, 25));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public static class LoginPage extends JPanel {
        public LoginPage() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(255, 255, 255, 230));
            formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel titleLabel = new JLabel("Select Your Role");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
            titleLabel.setForeground(HospitalManagementSystem.COLOR_FONT_DARK);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(titleLabel, gbc);
            JButton adminLoginButton = createStyledButton("Admin Login", e -> HospitalManagementSystem.showPage("AdminLoginPage"));
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(adminLoginButton, gbc);
            JButton doctorLoginButton = createStyledButton("Doctor Login", e -> HospitalManagementSystem.showPage("DoctorLoginPage"));
            gbc.gridx = 1;
            formPanel.add(doctorLoginButton, gbc);
            JButton backButton = new JButton("⬅ Back to Home");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            formPanel.add(backButton, gbc);

            add(formPanel, new GridBagConstraints());
        }

        private JButton createStyledButton(String text, ActionListener actionListener) {
            JButton button = new JButton(text);
            button.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("SansSerif", Font.BOLD, 16));
            button.setBorder(new EmptyBorder(15, 25, 15, 25));
            button.addActionListener(actionListener);
            return button;
        }
    }

    public static class AdminLoginPage extends JPanel {
        private final JTextField userField;
        private final JPasswordField passField;

        public AdminLoginPage() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(255, 255, 255, 230));
            formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel title = new JLabel("Admin Login");
            title.setFont(new Font("SansSerif", Font.BOLD, 28));
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(title, gbc);

            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Username:"), gbc);
            gbc.gridy = 2;
            formPanel.add(new JLabel("Password:"), gbc);

            gbc.anchor = GridBagConstraints.WEST;
            userField = new JTextField(15);
            userField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridx = 1;
            gbc.gridy = 1;
            formPanel.add(userField, gbc);

            passField = new JPasswordField(15);
            passField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridy = 2;
            formPanel.add(passField, gbc);
            JButton loginButton = new JButton("Login");
            loginButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            loginButton.setForeground(Color.WHITE);
            loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            loginButton.addActionListener(e -> performLogin());
            gbc.gridy = 3;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(loginButton, gbc);

            JButton backButton = new JButton("⬅ Back");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("LoginPage"));
            gbc.gridy = 4;
            formPanel.add(backButton, gbc);

            add(formPanel, new GridBagConstraints());
        }

        private void performLogin() {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String role = HospitalManagementSystem.getDbManager().validateUser(username, password);
            if ("Admin".equals(role)) {
                HospitalManagementSystem.setCurrentAdminUser(username);
                HospitalManagementSystem.showPage("AdminPage");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class DoctorLoginPage extends JPanel {
        private final JTextField userField;
        private final JPasswordField passField;

        public DoctorLoginPage() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(255, 255, 255, 230));
            formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel title = new JLabel("Doctor Login");
            title.setFont(new Font("SansSerif", Font.BOLD, 28));
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(title, gbc);

            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Username:"), gbc);
            gbc.gridy = 2;
            formPanel.add(new JLabel("Password:"), gbc);

            gbc.anchor = GridBagConstraints.WEST;
            userField = new JTextField(15);
            userField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridx = 1;
            gbc.gridy = 1;
            formPanel.add(userField, gbc);

            passField = new JPasswordField(15);
            passField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridy = 2;
            formPanel.add(passField, gbc);
            JButton loginButton = new JButton("Login");
            loginButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            loginButton.setForeground(Color.WHITE);
            loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            loginButton.addActionListener(e -> performLogin());
            gbc.gridy = 3;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(loginButton, gbc);

            JButton backButton = new JButton("⬅ Back");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("LoginPage"));
            gbc.gridy = 4;
            formPanel.add(backButton, gbc);

            add(formPanel, new GridBagConstraints());
        }

        private void performLogin() {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String role = HospitalManagementSystem.getDbManager().validateUser(username, password);
            if ("Doctor".equals(role)) {
                Doctor doctor = HospitalManagementSystem.getDbManager().getDoctorByUsername(username);
                if (doctor != null) {
                    HospitalManagementSystem.getDbManager().recordDoctorLogin(doctor.getId());
                    HospitalManagementSystem.setCurrentDoctorUser(doctor);
                    HospitalManagementSystem.showPage("DoctorPage");
                } else {
                    JOptionPane.showMessageDialog(this, "Doctor profile not found for this user.", "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid doctor credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class PatientLoginPage extends JPanel {
        private final JTextField nameField;
        private final JTextField idField;

        public PatientLoginPage() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(255, 255, 255, 230));
            formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel title = new JLabel("Patient Portal Login");
            title.setFont(new Font("SansSerif", Font.BOLD, 28));
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(title, gbc);

            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Patient Name:"), gbc);
            gbc.gridy = 2;
            formPanel.add(new JLabel("Patient ID:"), gbc);

            gbc.anchor = GridBagConstraints.WEST;
            nameField = new JTextField(15);
            nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridx = 1;
            gbc.gridy = 1;
            formPanel.add(nameField, gbc);

            idField = new JTextField(15);
            idField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridy = 2;
            formPanel.add(idField, gbc);
            JButton loginButton = new JButton("Access Portal");
            loginButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            loginButton.setForeground(Color.WHITE);
            loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            loginButton.addActionListener(e -> performLogin());
            gbc.gridy = 3;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(loginButton, gbc);
            JButton backButton = new JButton("⬅ Back");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            gbc.gridy = 4;
            formPanel.add(backButton, gbc);

            add(formPanel, new GridBagConstraints());
        }

        private void performLogin() {
            String name = nameField.getText().trim();
            String idText = idField.getText().trim();

            if (name.isEmpty() || idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both name and patient ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int patientId = Integer.parseInt(idText);
                Patient patient = HospitalManagementSystem.getDbManager().getPatientByNameAndId(name, patientId);

                if (patient != null) {
                    HospitalManagementSystem.showPatientInfoPage(patient);
                } else {
                    JOptionPane.showMessageDialog(this, "Patient not found. Please check your name and ID.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid patient ID (numbers only).", "Invalid ID", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class EnhancedOnlinePaymentPage extends JPanel {
        private JTextField patientIdField;
        private JTextField amountField; // Hidden field for hospital payments
        private CardLayout paymentMethodLayout;
        private JPanel paymentMethodPanel;
        private JTextField cardNameField;
        private JTextField cardNumberField;
        private JComboBox<String> monthCombo;
        private JComboBox<String> yearCombo;
        private JTextField cvvField;
        private JTextField upiIdField;
        private JLabel expiryDisplay;
        private JLabel cardNumberDisplay;
        private JLabel nameDisplay;
        private JLabel uploadedImageLabel;
        private final JLabel orderSummaryLabel;

        // NEW: Pharmacy-specific fields
        private final Map<Medicine, Integer> pharmacyCart;
        private final String deliveryAddress;
        private final boolean isPharmacyPayment;
        private final double predefinedAmount;

        // Original constructor for regular payments
        public EnhancedOnlinePaymentPage() {
            this(null, null, 0.0, false);
        }

        // NEW: Constructor for pharmacy payments
        public EnhancedOnlinePaymentPage(Map<Medicine, Integer> cart, String address, double amount, boolean isPharmacy) {
            this.pharmacyCart = cart;
            this.deliveryAddress = address;
            this.predefinedAmount = amount;
            this.isPharmacyPayment = isPharmacy;

            setOpaque(false);
            setLayout(new BorderLayout(20, 20));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            // Main split panel
            JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 20));
            mainPanel.setOpaque(true);

            // Payment info panel (left)
            JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
            leftPanel.setOpaque(false);
            TitledBorder paymentBorder = BorderFactory.createTitledBorder(
                    isPharmacyPayment ? "Pharmacy Payment Information" : "Hospital Payment Information"
            );
            paymentBorder.setTitleFont(new Font("Serif", Font.BOLD, 28));
            paymentBorder.setTitleJustification(TitledBorder.CENTER);
            leftPanel.setBorder(paymentBorder);

            JPanel casePanel = new JPanel(new GridBagLayout());
            casePanel.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);

            if (isPharmacyPayment) {
                // For pharmacy payments, show delivery address and pre-filled amount
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.EAST;
                casePanel.add(new JLabel("Delivery Address:"), gbc);
                JTextArea addressArea = new JTextArea(address, 3, 20);
                addressArea.setEditable(false);
                addressArea.setBackground(Color.LIGHT_GRAY);
                addressArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                casePanel.add(new JScrollPane(addressArea), gbc);

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.anchor = GridBagConstraints.EAST;
                casePanel.add(new JLabel("Total Amount (INR):"), gbc);
                amountField = new JTextField(String.format("%.2f", amount), 10);
                amountField.setEditable(false);
                amountField.setBackground(Color.LIGHT_GRAY);
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                casePanel.add(amountField, gbc);

                // Hide patient ID field for pharmacy payments
                patientIdField = new JTextField(10);
                patientIdField.setVisible(false);
            } else {
                // Hospital payments - only show Patient ID field (amount is auto-calculated)
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.EAST;
                casePanel.add(new JLabel("Patient ID:"), gbc);
                patientIdField = new JTextField(10);
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                casePanel.add(patientIdField, gbc);

                // Create hidden amount field for internal use
                amountField = new JTextField();
                amountField.setVisible(false);

                // Add document listener to patient ID field for auto-lookup
                patientIdField.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {
                        updateOrderSummaryFromPatientId();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        updateOrderSummaryFromPatientId();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        updateOrderSummaryFromPatientId();
                    }
                });

                // Add Upload Button for hospital payments
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.anchor = GridBagConstraints.EAST;
                casePanel.add(new JLabel("Upload Document:"), gbc);
                JButton uploadButton = new JButton("Choose File");
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                casePanel.add(uploadButton, gbc);
                uploadButton.addActionListener(e -> uploadImage());
            }

            leftPanel.add(casePanel, BorderLayout.NORTH);

            // Payment method buttons
            JPanel methodTabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            JButton cardButton = new JButton("Credit/Debit Card");
            JButton paypalButton = new JButton("Pay with PayPal");
            JButton upiButton = new JButton("UPI Payment");
            methodTabPanel.add(cardButton);
            methodTabPanel.add(paypalButton);
            methodTabPanel.add(upiButton);
            leftPanel.add(methodTabPanel, BorderLayout.CENTER);

            // Payment method panels
            setupPaymentMethodPanels();
            leftPanel.add(paymentMethodPanel, BorderLayout.SOUTH);
            mainPanel.add(leftPanel);

            // Order Summary panel (right)
            JPanel summaryPanel = new JPanel(new BorderLayout(10, 10));
            summaryPanel.setOpaque(true);
            TitledBorder orderBorder = BorderFactory.createTitledBorder("Order Summary");
            orderBorder.setTitleFont(new Font("Serif", Font.BOLD, 28));
            orderBorder.setTitleJustification(TitledBorder.CENTER);
            summaryPanel.setBorder(orderBorder);

            orderSummaryLabel = new JLabel();
            orderSummaryLabel.setVerticalAlignment(SwingConstants.TOP);
            summaryPanel.add(orderSummaryLabel, BorderLayout.CENTER);

            if (!isPharmacyPayment) {
                // Add image display label only for hospital payments
                uploadedImageLabel = new JLabel("No document uploaded", SwingConstants.CENTER);
                uploadedImageLabel.setPreferredSize(new Dimension(200, 200));
                uploadedImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                summaryPanel.add(uploadedImageLabel, BorderLayout.SOUTH);
            } else {
                uploadedImageLabel = null;
            }

            mainPanel.add(summaryPanel);
            add(mainPanel, BorderLayout.CENTER);

            // Setup payment method button actions
            cardButton.addActionListener(e -> paymentMethodLayout.show(paymentMethodPanel, "Card"));
            paypalButton.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.paypal.com"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Unable to open PayPal website.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            upiButton.addActionListener(e -> paymentMethodLayout.show(paymentMethodPanel, "UPI"));

            // Bottom panel with buttons
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setOpaque(false);
            JButton payButton = new JButton(isPharmacyPayment ? "Complete Order" : "Pay Now");
            payButton.setBackground(new Color(46, 125, 50));
            payButton.setForeground(Color.WHITE);
            payButton.addActionListener(e -> processPayment());

            JButton backButton = new JButton("← " + (isPharmacyPayment ? "Back to Order" : "Back to Home"));
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> {
                if (isPharmacyPayment) {
                    // Go back to pharmacy payment page
                    HospitalManagementSystem.showPharmacyPaymentPage(pharmacyCart, deliveryAddress);
                } else {
                    HospitalManagementSystem.showPage("MainPage");
                }
            });

            bottomPanel.add(payButton);
            bottomPanel.add(backButton);
            add(bottomPanel, BorderLayout.SOUTH);

            // Initialize order summary
            if (isPharmacyPayment) {
                updatePharmacyOrderSummary();
            } else {
                // Show initial message for hospital payments
                orderSummaryLabel.setText("<html><i>Enter Patient ID to see billing details</i></html>");
            }
        }

        // NEW METHOD: Auto-update order summary based on Patient ID
        private void updateOrderSummaryFromPatientId() {
            if (isPharmacyPayment) {
                updatePharmacyOrderSummary();
                return;
            }

            String patientIdText = patientIdField.getText().trim();
            if (patientIdText.isEmpty()) {
                orderSummaryLabel.setText("<html><i>Enter Patient ID to see billing details</i></html>");
                return;
            }

            try {
                int patientId = Integer.parseInt(patientIdText);

                // Check if patient exists
                Patient patient = HospitalManagementSystem.getDbManager().getPatientById(patientId);
                if (patient == null) {
                    orderSummaryLabel.setText("<html><span style='color: red;'>Patient ID " + patientId + " not found</span></html>");
                    return;
                }

                // Generate a realistic bill amount based on patient's consultation history
                double calculatedAmount = calculatePatientBillAmount(patientId, patient);

                // Set the amount in the hidden field
                amountField.setText(String.valueOf(calculatedAmount));

                // Update order summary with patient info and calculated amount
                updateOrderSummaryWithPatientInfo(patient, calculatedAmount);

            } catch (NumberFormatException e) {
                orderSummaryLabel.setText("<html><span style='color: red;'>Please enter a valid Patient ID (numbers only)</span></html>");
            }
        }

        // NEW METHOD: Calculate realistic bill amount based on patient history
        private double calculatePatientBillAmount(int patientId, Patient patient) {
            // Get patient's consultation history to calculate bill
            List<Consultation> consultations = HospitalManagementSystem.getDbManager().getConsultationsForPatient(patientId);

            double baseAmount = 500.0; // Base consultation fee
            double additionalCharges = 0.0;

            // Add charges based on consultation history
            for (Consultation consultation : consultations) {
                // Add doctor consultation fee (varies by specialization)
                Doctor doctor = HospitalManagementSystem.getDbManager().getDoctorById(consultation.getDoctorId());
                if (doctor != null) {
                    if ("Cardiology".equals(doctor.getSpecialization())) {
                        additionalCharges += 800.0;
                    } else if ("Pediatrics".equals(doctor.getSpecialization())) {
                        additionalCharges += 600.0;
                    } else {
                        additionalCharges += 400.0; // General Medicine
                    }
                }

                // Add prescription costs
                List<Prescription> prescriptions = HospitalManagementSystem.getDbManager()
                        .getPrescriptionsForConsultation(consultation.getId());
                for (Prescription prescription : prescriptions) {
                    additionalCharges += 50.0; // Average medicine cost
                }
            }

            // If no consultation history, use base amount
            if (consultations.isEmpty()) {
                return baseAmount;
            }

            // Cap the maximum amount to reasonable limits
            double totalAmount = Math.min(baseAmount + additionalCharges, 5000.0);
            return Math.max(totalAmount, 200.0); // Minimum bill amount
        }

        // NEW METHOD: Update order summary with detailed patient information
        private void updateOrderSummaryWithPatientInfo(Patient patient, double amount) {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<h3>Patient Billing Details</h3><hr>");

            // Patient Information
            sb.append("<b>Patient:</b> ").append(patient.getName()).append("<br>");
            sb.append("<b>ID:</b> ").append(patient.getId()).append("<br>");
            sb.append("<b>Contact:</b> ").append(patient.getContactNumber()).append("<br><br>");

            // Get recent consultations with tests
            List<Consultation> recentConsultations = HospitalManagementSystem.getDbManager()
                    .getConsultationsForPatient(patient.getId());

            if (!recentConsultations.isEmpty()) {
                Consultation recent = recentConsultations.get(0);
                Doctor doctor = HospitalManagementSystem.getDbManager().getDoctorById(recent.getDoctorId());

                sb.append("<b>Recent Consultation:</b><br>");
                sb.append("• Doctor: Dr. ").append(doctor != null ? doctor.getName() : "N/A").append("<br>");
                sb.append("• Date: ").append(recent.getConsultationDateTime().toLocalDate()).append("<br><br>");

                // Show prescribed tests
                List<MedicalTest> prescribedTests = HospitalManagementSystem.getDbManager()
                        .getTestsForConsultation(recent.getId());
                if (!prescribedTests.isEmpty()) {
                    sb.append("<b>Prescribed Tests:</b><br>");
                    for (MedicalTest test : prescribedTests) {
                        sb.append("• ").append(test.getName()).append(" - ₹")
                                .append(String.format("%.2f", test.getPrice())).append("<br>");
                    }
                    sb.append("<br>");
                }
            }

            // Continue with existing billing breakdown...
            sb.append("<b>Bill Breakdown:</b><br>");
            sb.append("• Consultation Fee: ₹").append(String.format("%.2f", amount * 0.4)).append("<br>");
            sb.append("• Medical Tests: ₹").append(String.format("%.2f", amount * 0.4)).append("<br>");
            sb.append("• Medicines: ₹").append(String.format("%.2f", amount * 0.2)).append("<br>");

            // Rest of your existing billing calculation...
            sb.append("</html>");
            orderSummaryLabel.setText(sb.toString());
        }


        private void setupPaymentMethodPanels() {
            paymentMethodLayout = new CardLayout();
            paymentMethodPanel = new JPanel(paymentMethodLayout);
            paymentMethodPanel.setOpaque(true);

            // Card panel
            JPanel cardPanel = new JPanel(new GridBagLayout());
            cardPanel.setOpaque(true);
            cardPanel.setName("Card");

            int row = 0;
            JPanel cardGraphicPanel = new JPanel(null);
            cardGraphicPanel.setBackground(new Color(51, 51, 51));
            cardGraphicPanel.setPreferredSize(new Dimension(470, 170));
            cardGraphicPanel.setBorder(new LineBorder(new Color(51, 51, 51), 2, true));
            JLabel chipLabel = new JLabel("🔑");
            chipLabel.setForeground(Color.WHITE);
            chipLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            chipLabel.setBounds(20, 25, 40, 40);
            cardGraphicPanel.add(chipLabel);
            cardNumberDisplay = new JLabel("•••• •••• •••• ••••");
            cardNumberDisplay.setFont(new Font("SansSerif", Font.BOLD, 24));
            cardNumberDisplay.setForeground(Color.WHITE);
            cardNumberDisplay.setBounds(40, 70, 350, 30);
            cardGraphicPanel.add(cardNumberDisplay);
            nameDisplay = new JLabel("YOUR NAME");
            nameDisplay.setFont(new Font("SansSerif", Font.BOLD, 16));
            nameDisplay.setForeground(Color.WHITE);
            nameDisplay.setBounds(40, 110, 200, 25);
            cardGraphicPanel.add(nameDisplay);

            expiryDisplay = new JLabel("MM/YY");
            expiryDisplay.setFont(new Font("SansSerif", Font.BOLD, 14));
            expiryDisplay.setForeground(Color.WHITE);
            expiryDisplay.setBounds(350, 140, 70, 20);
            cardGraphicPanel.add(expiryDisplay);

            JLabel cardTypeDisplay = new JLabel("Credit Card");
            cardTypeDisplay.setFont(new Font("SansSerif", Font.BOLD, 14));
            cardTypeDisplay.setForeground(Color.WHITE);
            cardTypeDisplay.setBounds(320, 30, 110, 20);
            cardGraphicPanel.add(cardTypeDisplay);

            GridBagConstraints cbg = new GridBagConstraints();
            cbg.gridx = 0;
            cbg.gridy = row++;
            cbg.gridwidth = 2;
            cbg.insets = new Insets(10, 10, 20, 10);
            cbg.anchor = GridBagConstraints.NORTH;
            cardPanel.add(cardGraphicPanel, cbg);

            // Cardholder Name
            cbg.gridwidth = 1;
            cbg.gridx = 0;
            cbg.gridy = row;
            cbg.anchor = GridBagConstraints.EAST;
            cardPanel.add(new JLabel("Cardholder Name:"), cbg);
            cardNameField = new JTextField(18);
            cbg.gridx = 1;
            cbg.anchor = GridBagConstraints.WEST;
            cardPanel.add(cardNameField, cbg);

            // Card Number
            cbg.gridx = 0;
            cbg.gridy = ++row;
            cbg.anchor = GridBagConstraints.EAST;
            cardPanel.add(new JLabel("Card Number:"), cbg);
            cardNumberField = new JTextField(18);
            cbg.gridx = 1;
            cbg.anchor = GridBagConstraints.WEST;
            cardPanel.add(cardNumberField, cbg);

            cardNumberField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateCardDisplay();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateCardDisplay();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateCardDisplay();
                }

                private void updateCardDisplay() {
                    String text = cardNumberField.getText().replaceAll("\\D", "");
                    if (text.length() > 16) text = text.substring(0, 16);
                    String display = text.replaceAll(".{4}", "$0 ").trim();
                    if (display.isEmpty()) display = "•••• •••• •••• ••••";
                    cardNumberDisplay.setText(display);
                }
            });

            cardNameField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateName();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateName();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateName();
                }

                private void updateName() {
                    String name = cardNameField.getText().trim();
                    nameDisplay.setText(name.isEmpty() ? "YOUR NAME" : name);
                }
            });

            // Expiry month/year
            cbg.gridx = 0;
            cbg.gridy = ++row;
            cbg.anchor = GridBagConstraints.EAST;
            cardPanel.add(new JLabel("Month:"), cbg);
            monthCombo = new JComboBox<>();
            for (int i = 1; i <= 12; i++) monthCombo.addItem(String.format("%02d", i));
            cbg.gridx = 1;
            cbg.anchor = GridBagConstraints.WEST;
            cardPanel.add(monthCombo, cbg);

            cbg.gridx = 0;
            cbg.gridy = ++row;
            cbg.anchor = GridBagConstraints.EAST;
            cardPanel.add(new JLabel("Year:"), cbg);
            yearCombo = new JComboBox<>();
            int thisYear = java.time.Year.now().getValue();
            for (int i = thisYear; i <= thisYear + 15; i++) yearCombo.addItem(String.valueOf(i));
            cbg.gridx = 1;
            cbg.anchor = GridBagConstraints.WEST;
            cardPanel.add(yearCombo, cbg);

            monthCombo.addActionListener(e -> updateExpiry());
            yearCombo.addActionListener(e -> updateExpiry());

            // CVV
            cbg.gridx = 0;
            cbg.gridy = ++row;
            cbg.anchor = GridBagConstraints.EAST;
            cardPanel.add(new JLabel("CVV:"), cbg);
            cvvField = new JTextField(5);
            cbg.gridx = 1;
            cbg.anchor = GridBagConstraints.WEST;
            cardPanel.add(cvvField, cbg);

            paymentMethodPanel.add(cardPanel, "Card");

            // UPI panel
            JPanel upiPanel = new JPanel(new GridBagLayout());
            upiPanel.setOpaque(true);
            upiPanel.setName("UPI");
            GridBagConstraints ugbc = new GridBagConstraints();
            ugbc.insets = new Insets(10, 10, 10, 10);
            ugbc.gridx = 0;
            ugbc.gridy = 0;
            upiPanel.add(new JLabel("Enter your UPI ID:"), ugbc);
            upiIdField = new JTextField(20);
            ugbc.gridx = 1;
            upiPanel.add(upiIdField, ugbc);

            // QR Code
            try {
                String qrUrl = "https://i.postimg.cc/VkTtX0xS/qrcode.png";
                URI uri = new URI(qrUrl);
                URL url = uri.toURL();
                BufferedImage qrImage = ImageIO.read(url);
                JLabel qrLabel = new JLabel(new ImageIcon(qrImage.getScaledInstance(180, 180, java.awt.Image.SCALE_SMOOTH)));
                GridBagConstraints qgbc = new GridBagConstraints();
                qgbc.gridx = 0;
                qgbc.gridy = 1;
                qgbc.gridwidth = 2;
                qgbc.anchor = GridBagConstraints.CENTER;
                qgbc.insets = new Insets(18, 10, 10, 10);
                upiPanel.add(qrLabel, qgbc);
                JLabel scanLabel = new JLabel("Or SCAN to PAY with any UPI app");
                scanLabel.setForeground(Color.BLACK);
                scanLabel.setHorizontalAlignment(SwingConstants.CENTER);
                qgbc.gridy = 2;
                upiPanel.add(scanLabel, qgbc);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            paymentMethodPanel.add(upiPanel, "UPI");

            // PayPal panel
            JPanel paypalPanel = new JPanel();
            paypalPanel.setOpaque(false);
            paypalPanel.setName("PayPal");
            paypalPanel.add(new JLabel("PayPal payments not implemented in demo."));
            paymentMethodPanel.add(paypalPanel, "PayPal");
        }

        private void updatePharmacyOrderSummary() {
            if (!isPharmacyPayment || pharmacyCart == null) return;

            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<h3>Pharmacy Order Details</h3>");

            double subtotal = 0;
            for (Map.Entry<Medicine, Integer> entry : pharmacyCart.entrySet()) {
                Medicine med = entry.getKey();
                int quantity = entry.getValue();
                double itemTotal = med.getPrice() * quantity;
                subtotal += itemTotal;

                sb.append(String.format("%s x%d<br>₹%.2f each = ₹%.2f<br><br>",
                        med.getName(), quantity, med.getPrice(), itemTotal));
            }

            double deliveryCharges = 50.00;
            double tax = subtotal * 0.05;
            double total = subtotal + deliveryCharges + tax;

            sb.append("<hr>");
            sb.append(String.format("Subtotal: ₹%.2f<br>", subtotal));
            sb.append(String.format("Delivery: ₹%.2f<br>", deliveryCharges));
            sb.append(String.format("Tax (5%%): ₹%.2f<br>", tax));
            sb.append("<hr>");
            sb.append(String.format("<b>Total: ₹%.2f</b><br>", total));
            sb.append("<br><small>Delivery Address:<br>");
            sb.append(deliveryAddress.replace(", ", "<br>"));
            sb.append("</small>");
            sb.append("</html>");

            orderSummaryLabel.setText(sb.toString());
        }

        private void processPayment() {
            if (isPharmacyPayment) {
                // Process pharmacy payment
                String message = String.format("Pharmacy Order Confirmed!\n\nTotal Amount: ₹%.2f\n\nYour medicines will be delivered to:\n%s\n\nEstimated delivery: 2-3 working days",
                        predefinedAmount, deliveryAddress);

                JOptionPane.showMessageDialog(this, message, "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                // Clear cart and go back to main page
                HospitalManagementSystem.showPage("MainPage");
            } else {
                // Hospital payment processing - now uses auto-calculated amount
                String patientId = patientIdField.getText().trim();
                String amountStr = amountField.getText().trim(); // This is now auto-populated

                if (patientId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a Patient ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (amountStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No billing amount found for this patient.", "No Amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double typedAmount;
                try {
                    typedAmount = Double.parseDouble(amountStr);
                    if (typedAmount <= 0) {
                        JOptionPane.showMessageDialog(this, "Invalid billing amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid billing amount format.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Calculate the total using the same logic as updateOrderSummary
                double tax = typedAmount * 0.18;
                double discount = 50.0;
                double total = typedAmount + tax - discount;
                String totalStr = String.format("%.2f", total);
                String message = "Payment Confirmed:\nPatient ID: " + patientId + "\nAmount: ₹" + totalStr;

                String method = getVisibleCardName(paymentMethodPanel);
                if ("Card".equals(method)) {
                    String name = cardNameField.getText().trim();
                    String number = cardNumberField.getText().replaceAll("\\D", "");
                    String cvv = cvvField.getText().trim();
                    if (name.isEmpty() || number.length() != 16 || cvv.length() < 3) {
                        JOptionPane.showMessageDialog(this, "Enter valid card details.", "Invalid Card", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    message += "\nPaid by Card for " + name;
                } else if ("UPI".equals(method)) {
                    if (upiIdField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Enter your UPI ID.", "UPI Required", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    message += "\nPaid by UPI: " + upiIdField.getText().trim();
                }

                JOptionPane.showMessageDialog(this, message, "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);

                // Clear fields after successful payment
                patientIdField.setText("");
                amountField.setText("");
                cardNameField.setText("");
                cardNumberField.setText("");
                cvvField.setText("");
                upiIdField.setText("");
            }
        }

        private void uploadImage() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select an image file");
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "png", "gif", "bmp");
            fileChooser.addChoosableFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedImage originalImage = ImageIO.read(selectedFile);
                    Image scaledImage = originalImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    if (uploadedImageLabel != null) {
                        uploadedImageLabel.setIcon(new ImageIcon(scaledImage));
                        uploadedImageLabel.setText(null);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error reading the image file.", "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void updateExpiry() {
            if (monthCombo != null && yearCombo != null && expiryDisplay != null) {
                expiryDisplay.setText(monthCombo.getSelectedItem() + "/" + yearCombo.getSelectedItem().toString().substring(2));
            }
        }

        // Helper method to get visible card name
        private String getVisibleCardName(JPanel container) {
            for (Component comp : container.getComponents()) {
                if (comp.isVisible()) {
                    return comp.getName();
                }
            }
            return null;
        }
    }

    public static class AdminPage extends JPanel {
        public AdminPage(String adminUsername) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            JLabel welcomeLabel = new JLabel("  Welcome back, " + adminUsername + "!");
            welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            welcomeLabel.setForeground(Color.WHITE);
            topPanel.add(welcomeLabel, BorderLayout.WEST);
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            buttonPanel.setOpaque(false);
            buttonPanel.add(createStyledButton("Patient Details", e -> HospitalManagementSystem.showPage("PatientDetailsPageAdmin")));
            buttonPanel.add(createStyledButton("Upcoming Appointments", e -> HospitalManagementSystem.showPage("AppointmentsViewPageAdmin")));
            buttonPanel.add(createStyledButton("Doctor Status", e -> HospitalManagementSystem.showPage("DoctorStatusPage")));
            buttonPanel.add(createStyledButton("Doctor Management", e -> HospitalManagementSystem.showPage("DoctorManagementPage")));
            topPanel.add(buttonPanel, BorderLayout.CENTER);

            JButton logoutButton = createStyledButton("Logout", e -> HospitalManagementSystem.showPage("LoginPage"));
            JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            logoutPanel.setOpaque(false);
            logoutPanel.add(logoutButton);
            topPanel.add(logoutPanel, BorderLayout.EAST);

            add(topPanel, BorderLayout.NORTH);

            JLabel centerMessage = new JLabel("Admin Dashboard", SwingConstants.CENTER);
            centerMessage.setFont(new Font("SansSerif", Font.BOLD, 48));
            centerMessage.setForeground(Color.WHITE);
            add(centerMessage, BorderLayout.CENTER);
        }

        private JButton createStyledButton(String text, ActionListener actionListener) {
            JButton button = new JButton(text);
            button.setBackground(HospitalManagementSystem.COLOR_SECONDARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setBorder(new EmptyBorder(8, 15, 8, 15));
            button.addActionListener(actionListener);
            return button;
        }
    }

    public static class DoctorPage extends JPanel {
        private final Doctor doctor;
        private final JToggleButton dutyToggleButton;

        public DoctorPage(Doctor doctor) {
            this.doctor = doctor;
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            JLabel welcomeLabel = new JLabel("  Welcome back, Dr. " + doctor.getName() + "!");
            welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            welcomeLabel.setForeground(Color.WHITE);
            topPanel.add(welcomeLabel, BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            buttonPanel.setOpaque(false);
            buttonPanel.add(createStyledButton("My Patient Details", e -> HospitalManagementSystem.showDoctorSpecificPage("PatientDetailsPageDoctor", doctor)));
            buttonPanel.add(createStyledButton("My Upcoming Appointments", e -> HospitalManagementSystem.showDoctorSpecificPage("AppointmentsViewPageDoctor", doctor)));
            topPanel.add(buttonPanel, BorderLayout.CENTER);
            add(topPanel, BorderLayout.NORTH);

            JLabel centerMessage = new JLabel("Doctor Dashboard", SwingConstants.CENTER);
            centerMessage.setFont(new Font("SansSerif", Font.BOLD, 48));
            centerMessage.setForeground(Color.WHITE);
            add(centerMessage, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            bottomPanel.setOpaque(false);
            buttonPanel.add(createStyledButton("Start Consultation", e -> showConsultationOptions()));
            dutyToggleButton = new JToggleButton();
            dutyToggleButton.setOpaque(true);
            dutyToggleButton.setContentAreaFilled(true);
            updateDutyButtonText();
            dutyToggleButton.setEnabled(true);
            dutyToggleButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            dutyToggleButton.setBorder(new EmptyBorder(10, 20, 10, 20));
            dutyToggleButton.setEnabled(true);
            dutyToggleButton.addActionListener(e -> toggleDutyStatus());
            bottomPanel.add(dutyToggleButton);

            JButton logoutButton = createStyledButton("Logout", e -> performLogout());
            bottomPanel.add(logoutButton);

            add(bottomPanel, BorderLayout.SOUTH);
            boolean isOnDuty = HospitalManagementSystem.getDbManager().isDoctorOnDuty(doctor.getId());
            dutyToggleButton.setSelected(isOnDuty);
            updateDutyButtonText();
            dutyToggleButton.setEnabled(true);
        }

        private void toggleDutyStatus() {
            boolean isOnDuty = dutyToggleButton.isSelected();
            HospitalManagementSystem.getDbManager().updateDoctorDutyStatus(doctor.getId(), isOnDuty);
            updateDutyButtonText();
            dutyToggleButton.setEnabled(true);
            String message = isOnDuty ? "You are now ON DUTY" : "You are now OFF DUTY";
            JOptionPane.showMessageDialog(this, message, "Duty Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }

        private void updateDutyButtonText() {
            if (dutyToggleButton.isSelected()) {
                dutyToggleButton.setText("🟢 ON DUTY - Click to Sign Off");
                dutyToggleButton.setBackground(HospitalManagementSystem.COLOR_SUCCESS);
                dutyToggleButton.setForeground(Color.WHITE);
            } else {
                dutyToggleButton.setText("🔴 OFF DUTY - Click to Sign On");
                dutyToggleButton.setBackground(HospitalManagementSystem.COLOR_DANGER);
                dutyToggleButton.setForeground(Color.WHITE);
            }
            dutyToggleButton.setEnabled(true);
        }

        private void performLogout() {
            HospitalManagementSystem.getDbManager().recordDoctorLogout(doctor.getId());
            JOptionPane.showMessageDialog(this, "You have been successfully logged out.", "Logout Successful", JOptionPane.INFORMATION_MESSAGE);
            HospitalManagementSystem.showPage("LoginPage");
        }

        private JButton createStyledButton(String text, ActionListener actionListener) {
            JButton button = new JButton(text);
            button.setBackground(HospitalManagementSystem.COLOR_SECONDARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 14));
            button.setBorder(new EmptyBorder(10, 20, 10, 20));
            button.addActionListener(actionListener);
            return button;
        }

        private void showConsultationOptions() {
            List<Consultation> upcomingAppointments = HospitalManagementSystem.getDbManager()
                    .getUpcomingAppointments(doctor.getId());
            if (upcomingAppointments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No upcoming appointments found.",
                        "No Appointments",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }


            String[] appointmentOptions = new String[upcomingAppointments.size()];
            for (int i = 0; i < upcomingAppointments.size(); i++) {
                Consultation c = upcomingAppointments.get(i);
                Patient p = HospitalManagementSystem.getDbManager().getPatientById(c.getPatientId());
                appointmentOptions[i] = String.format("%s - %s (%s)",
                        p.getName(),
                        c.getConsultationDateTime().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                        c.getConsultingReason());
            }

            String selected = (String) JOptionPane.showInputDialog(this,
                    "Select appointment to start consultation:",
                    "Start Consultation",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    appointmentOptions,
                    appointmentOptions[0]);
            if (selected != null) {
                int selectedIndex = java.util.Arrays.asList(appointmentOptions).indexOf(selected);
                Consultation selectedConsultation = upcomingAppointments.get(selectedIndex);
                Patient selectedPatient = HospitalManagementSystem.getDbManager()
                        .getPatientById(selectedConsultation.getPatientId());
                HospitalManagementSystem.showConsultationPanel(doctor, selectedPatient, selectedConsultation);
            }
        }
    }

    public static class ConsultationPanel extends JPanel {
        private final Doctor doctor;
        private final Patient patient;
        private final Consultation consultation;
        private JTextArea consultationNotesArea;
        private JPanel prescriptionsPanel;
        private JPanel testsPanel;
        private final List<PrescriptionEntry> prescriptionEntries;
        private final List<TestEntry> testEntries;
        private JLabel totalBillLabel;

        public ConsultationPanel(Doctor doctor, Patient patient, Consultation consultation) {
            this.doctor = doctor;
            this.patient = patient;
            this.consultation = consultation;
            this.prescriptionEntries = new ArrayList<>();
            this.testEntries = new ArrayList<>();

            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            // Main content panel
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBackground(new Color(255, 255, 255, 230));
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Top panel with patient info
            JPanel topPanel = createPatientInfoPanel();
            contentPanel.add(topPanel, BorderLayout.NORTH);

            // Center panel with tabbed interface
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Consultation Notes", createConsultationNotesPanel());
            tabbedPane.addTab("Prescriptions", createPrescriptionsPanel());
            tabbedPane.addTab("Medical Tests", createTestsPanel());
            tabbedPane.addTab("Bill Summary", createBillSummaryPanel());
            contentPanel.add(tabbedPane, BorderLayout.CENTER);

            // Bottom panel with action buttons
            JPanel bottomPanel = createActionButtonsPanel();
            contentPanel.add(bottomPanel, BorderLayout.SOUTH);

            add(contentPanel, BorderLayout.CENTER);
        }

        private JPanel createTestsPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setOpaque(false);

            // Tests selection panel
            testsPanel = new JPanel();
            testsPanel.setLayout(new BoxLayout(testsPanel, BoxLayout.Y_AXIS));
            testsPanel.setOpaque(false);

            JButton addTestButton = new JButton("+ Add Medical Test");
            addTestButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            addTestButton.setForeground(Color.WHITE);
            addTestButton.addActionListener(e -> addTestEntry());

            panel.add(new JScrollPane(testsPanel), BorderLayout.CENTER);
            panel.add(addTestButton, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createBillSummaryPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setOpaque(false);

            totalBillLabel = new JLabel();
            totalBillLabel.setVerticalAlignment(SwingConstants.TOP);
            totalBillLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

            JButton calculateBillButton = new JButton("Calculate Total Bill");
            calculateBillButton.setBackground(COLOR_SUCCESS);
            calculateBillButton.setForeground(Color.WHITE);
            calculateBillButton.addActionListener(e -> updateBillSummary());

            panel.add(new JScrollPane(totalBillLabel), BorderLayout.CENTER);
            panel.add(calculateBillButton, BorderLayout.SOUTH);

            return panel;
        }

        private void addTestEntry() {
            TestEntry entry = new TestEntry();
            testEntries.add(entry);
            testsPanel.add(entry);
            testsPanel.revalidate();
            testsPanel.repaint();
        }

        private void updateBillSummary() {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<h3>Consultation Bill Summary</h3><hr>");

            double consultationFee = 500.0; // Base consultation fee
            double prescriptionTotal = 0.0;
            double testsTotal = 0.0;

            // Consultation fee
            sb.append("<b>Consultation Fee:</b> ₹").append(String.format("%.2f", consultationFee)).append("<br><br>");

            // Prescriptions
            sb.append("<b>Prescribed Medicines:</b><br>");
            for (PrescriptionEntry entry : prescriptionEntries) {
                if (!entry.getMedicineName().trim().isEmpty()) {
                    double medicinePrice = 50.0; // Default medicine price
                    prescriptionTotal += medicinePrice;
                    sb.append("• ").append(entry.getMedicineName())
                            .append(" - ₹").append(String.format("%.2f", medicinePrice)).append("<br>");
                }
            }
            if (prescriptionTotal == 0) {
                sb.append("No medicines prescribed<br>");
            }
            sb.append("<br>");

            // Medical tests
            sb.append("<b>Medical Tests:</b><br>");
            for (TestEntry entry : testEntries) {
                if (entry.getSelectedTest() != null) {
                    MedicalTest test = entry.getSelectedTest();
                    double testPrice = test.getPrice() * entry.getQuantity();
                    testsTotal += testPrice;
                    sb.append("• ").append(test.getName())
                            .append(" x").append(entry.getQuantity())
                            .append(" - ₹").append(String.format("%.2f", testPrice)).append("<br>");
                }
            }
            if (testsTotal == 0) {
                sb.append("No tests prescribed<br>");
            }

            // Calculate totals
            double subtotal = consultationFee + prescriptionTotal + testsTotal;
            double tax = subtotal * 0.18; // 18% GST
            double total = subtotal + tax;

            sb.append("<br><hr>");
            sb.append("<b>Subtotal:</b> ₹").append(String.format("%.2f", subtotal)).append("<br>");
            sb.append("<b>GST (18%):</b> ₹").append(String.format("%.2f", tax)).append("<br>");
            sb.append("<b>Total Amount:</b> ₹").append(String.format("%.2f", total)).append("<br>");
            sb.append("</html>");

            totalBillLabel.setText(sb.toString());
        }

        private void saveConsultation() {
            // Save prescriptions
            for (PrescriptionEntry entry : prescriptionEntries) {
                if (!entry.getMedicineName().trim().isEmpty()) {
                    HospitalManagementSystem.getDbManager().addPrescription(
                            consultation.getId(),
                            entry.getMedicineName(),
                            entry.getDosage()
                    );
                }
            }

            // Save medical tests
            for (TestEntry entry : testEntries) {
                if (entry.getSelectedTest() != null) {
                    HospitalManagementSystem.getDbManager().addConsultationTest(
                            consultation.getId(),
                            entry.getSelectedTest().getId(),
                            entry.getQuantity()
                    );
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Consultation saved successfully!\nPrescriptions and tests have been recorded.\nBill has been generated for the patient.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            HospitalManagementSystem.showPage("DoctorPage");
        }

        // Inner class for test entries
        private class TestEntry extends JPanel {
            private final JComboBox<MedicalTest> testComboBox;
            private final JSpinner quantitySpinner;
            private final JLabel priceLabel;

            public TestEntry() {
                setLayout(new FlowLayout(FlowLayout.LEFT));
                setOpaque(false);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                add(new JLabel("Test:"));

                // Populate test combo box
                List<MedicalTest> tests = HospitalManagementSystem.getDbManager().getAllMedicalTests();
                testComboBox = new JComboBox<>(tests.toArray(new MedicalTest[0]));
                testComboBox.setPreferredSize(new Dimension(300, 25));
                testComboBox.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof MedicalTest) {
                            MedicalTest test = (MedicalTest) value;
                            setText(test.getName() + " - ₹" + String.format("%.2f", test.getPrice()));
                        }
                        return this;
                    }
                });
                testComboBox.addActionListener(e -> updatePrice());
                add(testComboBox);

                add(new JLabel("Qty:"));
                quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
                quantitySpinner.setPreferredSize(new Dimension(60, 25));
                quantitySpinner.addChangeListener(e -> updatePrice());
                add(quantitySpinner);

                priceLabel = new JLabel("₹0.00");
                priceLabel.setPreferredSize(new Dimension(100, 25));
                add(priceLabel);

                JButton removeButton = new JButton("Remove");
                removeButton.setBackground(HospitalManagementSystem.COLOR_DANGER);
                removeButton.setForeground(Color.WHITE);
                removeButton.addActionListener(e -> removeTest());
                add(removeButton);

                updatePrice();
            }

            private void updatePrice() {
                if (testComboBox.getSelectedItem() instanceof MedicalTest) {
                    MedicalTest test = (MedicalTest) testComboBox.getSelectedItem();
                    int quantity = (Integer) quantitySpinner.getValue();
                    double totalPrice = test.getPrice() * quantity;
                    priceLabel.setText("₹" + String.format("%.2f", totalPrice));
                }
            }

            private void removeTest() {
                testsPanel.remove(this);
                testEntries.remove(this);
                testsPanel.revalidate();
                testsPanel.repaint();
            }

            public MedicalTest getSelectedTest() {
                return (MedicalTest) testComboBox.getSelectedItem();
            }

            public int getQuantity() {
                return (Integer) quantitySpinner.getValue();
            }
        }

        private JPanel createPatientInfoPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);

            JLabel title = new JLabel("Consultation - " + patient.getName(), SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            panel.add(title, BorderLayout.CENTER);

            JLabel info = new JLabel("Patient ID: " + patient.getId() + " | Contact: " + patient.getContactNumber(), SwingConstants.CENTER);
            info.setFont(new Font("SansSerif", Font.PLAIN, 14));
            panel.add(info, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createConsultationNotesPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setOpaque(false);

            JLabel label = new JLabel("Consultation Notes:");
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            panel.add(label, BorderLayout.NORTH);

            consultationNotesArea = new JTextArea(10, 30);
            consultationNotesArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            consultationNotesArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(new JScrollPane(consultationNotesArea), BorderLayout.CENTER);

            return panel;
        }

        private JPanel createPrescriptionsPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setOpaque(false);

            // Prescriptions panel
            prescriptionsPanel = new JPanel();
            prescriptionsPanel.setLayout(new BoxLayout(prescriptionsPanel, BoxLayout.Y_AXIS));
            prescriptionsPanel.setOpaque(false);

            JButton addPrescriptionButton = new JButton("+ Add Prescription");
            addPrescriptionButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            addPrescriptionButton.setForeground(Color.WHITE);
            addPrescriptionButton.addActionListener(e -> addPrescriptionEntry());

            panel.add(new JScrollPane(prescriptionsPanel), BorderLayout.CENTER);
            panel.add(addPrescriptionButton, BorderLayout.SOUTH);

            return panel;
        }

        private JPanel createActionButtonsPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panel.setOpaque(false);

            JButton saveButton = new JButton("Save Consultation");
            saveButton.setBackground(COLOR_SUCCESS);
            saveButton.setForeground(Color.WHITE);
            saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            saveButton.addActionListener(e -> saveConsultation());

            JButton backButton = new JButton("Back to Dashboard");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("DoctorPage"));

            panel.add(backButton);
            panel.add(saveButton);

            return panel;
        }

        private void addPrescriptionEntry() {
            PrescriptionEntry entry = new PrescriptionEntry();
            prescriptionEntries.add(entry);
            prescriptionsPanel.add(entry);
            prescriptionsPanel.revalidate();
            prescriptionsPanel.repaint();
        }

        // Inner class for prescription entries
        private class PrescriptionEntry extends JPanel {
            private final JTextField medicineField;
            private final JTextField dosageField;

            public PrescriptionEntry() {
                setLayout(new FlowLayout(FlowLayout.LEFT));
                setOpaque(false);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                add(new JLabel("Medicine:"));
                medicineField = new JTextField(20);
                add(medicineField);

                add(new JLabel("Dosage:"));
                dosageField = new JTextField(15);
                add(dosageField);

                JButton removeButton = new JButton("Remove");
                removeButton.setBackground(HospitalManagementSystem.COLOR_DANGER);
                removeButton.setForeground(Color.WHITE);
                removeButton.addActionListener(e -> removePrescription());
                add(removeButton);
            }

            private void removePrescription() {
                prescriptionsPanel.remove(this);
                prescriptionEntries.remove(this);
                prescriptionsPanel.revalidate();
                prescriptionsPanel.repaint();
            }

            public String getMedicineName() {
                return medicineField.getText();
            }

            public String getDosage() {
                return dosageField.getText();
            }
        }

    }


    public static class AppointmentBookingPage1 extends JPanel {
        private final JTextField nameField;
        private final JTextField contactField;
        private final JTextField ageField; // Age field

        public AppointmentBookingPage1() {
            setOpaque(false);
            setLayout(new GridBagLayout());

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(255, 255, 255, 230));
            formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel title = new JLabel("Book an Appointment - Step 1: Patient Info");
            title.setFont(new Font("SansSerif", Font.BOLD, 28));
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(title, gbc);

            // Labels
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;

            gbc.gridy = 1;
            formPanel.add(new JLabel("Patient Full Name:"), gbc);

            gbc.gridy = 2;
            formPanel.add(new JLabel("Contact Number:"), gbc);

            gbc.gridy = 3; // Age label
            formPanel.add(new JLabel("Age:"), gbc);

            // Fields
            gbc.anchor = GridBagConstraints.WEST;

            nameField = new JTextField(20);
            nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridx = 1;
            gbc.gridy = 1;
            formPanel.add(nameField, gbc);

            contactField = new JTextField(20);
            contactField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridy = 2;
            formPanel.add(contactField, gbc);

            // Age field
            ageField = new JTextField(20);
            ageField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            gbc.gridy = 3;
            formPanel.add(ageField, gbc);

            JButton nextButton = new JButton("Next");
            nextButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
            nextButton.setForeground(Color.WHITE);
            nextButton.addActionListener(e -> proceedToNextStep());
            gbc.gridy = 4;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(nextButton, gbc);

            JButton backButton = new JButton("Cancel");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            gbc.gridy = 5;
            formPanel.add(backButton, gbc);

            add(formPanel, new GridBagConstraints());
        }

        private void proceedToNextStep() {
            String patientName = nameField.getText().trim();
            String contactNumber = contactField.getText().trim();
            String ageText = ageField.getText().trim();

            // Validate all fields
            if (patientName.isEmpty() || contactNumber.isEmpty() || ageText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.",
                        "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate patient name
            if (!patientName.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(this,
                        "Patient name should contain only letters and spaces.",
                        "Invalid Name", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate phone number
            if (!contactNumber.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid 10-digit phone number (numbers only).",
                        "Invalid Phone Number", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate age
            int age;
            try {
                age = Integer.parseInt(ageText);
                if (age < 0 || age > 150) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid age (0-150).",
                            "Invalid Age", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid age (numbers only).",
                        "Invalid Age", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Store temporarily instead of creating patient immediately
            HospitalManagementSystem.tempPatientData = new TempPatientData(patientName, contactNumber, age);

            // Continue to symptoms page without patient ID
            HospitalManagementSystem.startAppointmentBookingWithTempData();
        }


        public static class AppointmentBookingPage2 extends JPanel {
            private final Patient patient;
            private final JTextArea symptomsArea;

            public AppointmentBookingPage2(Patient patient) {
                this.patient = patient;
                setOpaque(false);
                setLayout(new GridBagLayout());
                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(new Color(255, 255, 255, 230));
                formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(10, 10, 10, 10);
                JLabel title = new JLabel("Step 2: Describe Your Symptoms");
                title.setFont(new Font("SansSerif", Font.BOLD, 28));
                gbc.gridwidth = 2;
                gbc.gridx = 0;
                gbc.gridy = 0;
                formPanel.add(title, gbc);

                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.NORTHEAST;
                gbc.gridy = 1;
                formPanel.add(new JLabel("Describe Symptoms:"), gbc);
                symptomsArea = new JTextArea(8, 30);
                symptomsArea.setLineWrap(true);
                symptomsArea.setWrapStyleWord(true);
                symptomsArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
                symptomsArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                JScrollPane scrollPane = new JScrollPane(symptomsArea);
                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                formPanel.add(scrollPane, gbc);
                JButton nextButton = new JButton("Get Doctor Suggestions ➡");
                nextButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                nextButton.setForeground(Color.WHITE);
                nextButton.setFont(new Font("SansSerif", Font.BOLD, 16));
                nextButton.addActionListener(e -> proceedToNextStep());
                gbc.gridy = 2;
                gbc.gridx = 0;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.CENTER;
                formPanel.add(nextButton, gbc);

                JButton backButton = new JButton("⬅ Back");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("AppointmentBookingPage1"));
                gbc.gridy = 3;
                formPanel.add(backButton, gbc);

                add(formPanel, new GridBagConstraints());
            }

            private void proceedToNextStep() {
                String symptoms = symptomsArea.getText().trim();
                if (symptoms.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please describe your symptoms.", "Input Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (patient != null) {
                    HospitalManagementSystem.showDoctorSuggestions(patient, symptoms);
                } else {
                    // Use temp data
                    HospitalManagementSystem.showDoctorSuggestionsWithTempData(symptoms);
                }
            }
        }

        public static class AppointmentBookingPage2_5 extends JPanel {
            public AppointmentBookingPage2_5(Patient patient, String symptoms) {
                setOpaque(false);
                setLayout(new GridBagLayout());

                JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
                mainContainer.setOpaque(true);
                mainContainer.setBackground(new Color(255, 255, 255, 230));
                mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

                JLabel title = new JLabel("Step 3: Recommended Doctors Based on Your Symptoms", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                mainContainer.add(title, BorderLayout.NORTH);

                // NEW: Check patient age and override suggestions for children
                List<Doctor> suggestedDoctors;
                if (patient.getAge() < 18) {
                    // For children under 18, ONLY show pediatricians
                    suggestedDoctors = HospitalManagementSystem.getDbManager().getSuggestedDoctors("child");
                } else {
                    // For adults, use normal symptom-based suggestions
                    suggestedDoctors = HospitalManagementSystem.getDbManager().getSuggestedDoctors(symptoms);
                }

                JPanel doctorsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                doctorsPanel.setBorder(BorderFactory.createTitledBorder(
                        patient.getAge() < 18 ? "Pediatricians (Recommended for children under 18)" : "Recommended Doctors"
                ));

                if (suggestedDoctors.isEmpty()) {
                    JLabel noDocsLabel = new JLabel("No doctors available for this specialization.");
                    noDocsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    doctorsPanel.add(noDocsLabel);

                    // Fallback to general doctors if no pediatricians found
                    if (patient.getAge() < 18) {
                        suggestedDoctors = HospitalManagementSystem.getDbManager().getDoctorsOnDuty();
                    }
                }

                for (Doctor doctor : suggestedDoctors) {
                    JPanel doctorPanel = new JPanel(new BorderLayout(10, 5));
                    doctorPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    doctorPanel.setBackground(Color.WHITE);

                    JLabel doctorInfo = new JLabel("<html><b>Dr. " + doctor.getName() + "</b><br/>" +
                            "Specialization: " + doctor.getSpecialization() + "</html>");
                    doctorInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    doctorPanel.add(doctorInfo, BorderLayout.CENTER);

                    JButton selectButton = new JButton("Select");
                    selectButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                    selectButton.setForeground(Color.WHITE);
                    selectButton.addActionListener(e ->
                            HospitalManagementSystem.showDoctorCalendar(patient, doctor, symptoms));
                    doctorPanel.add(selectButton, BorderLayout.EAST);

                    doctorsPanel.add(doctorPanel);
                }

                JScrollPane doctorsScrollPane = new JScrollPane(doctorsPanel);
                doctorsScrollPane.setPreferredSize(new Dimension(600, 300));
                mainContainer.add(doctorsScrollPane, BorderLayout.CENTER);

                JButton backButton = new JButton("Back to Symptoms");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.addActionListener(e ->
                        HospitalManagementSystem.startAppointmentBooking(patient));

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setOpaque(false);
                buttonPanel.add(backButton);
                mainContainer.add(buttonPanel, BorderLayout.SOUTH);

                add(mainContainer, new GridBagConstraints());
            }
        }


        public static class AppointmentBookingPage3 extends JPanel {
            public AppointmentBookingPage3(Patient patient, Doctor doctor, String reason) {
                setOpaque(false);
                setLayout(new GridBagLayout());
                JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
                mainContainer.setOpaque(true);
                mainContainer.setBackground(new Color(255, 255, 255, 230));
                mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

                // Title
                JLabel title = new JLabel("Step 4: Select Date for Dr. " + doctor.getName(), SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                mainContainer.add(title, BorderLayout.NORTH);

                // Calendar panel
                DoctorCalendarPanel calendarPanel = new DoctorCalendarPanel(doctor, date -> {
                    HospitalManagementSystem.showTimeSlots(patient, doctor, reason, date);
                });
                mainContainer.add(calendarPanel, BorderLayout.CENTER);

                // NEW: Bottom panel with back button
                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
                bottomPanel.setOpaque(false);

                JButton backButton = new JButton("⬅ Back to Doctor Selection");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setForeground(Color.BLACK);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                backButton.addActionListener(e -> HospitalManagementSystem.showDoctorSuggestions(patient, reason));

                JButton cancelButton = new JButton("Cancel Booking");
                cancelButton.setBackground(HospitalManagementSystem.COLOR_DANGER);
                cancelButton.setForeground(Color.WHITE);
                cancelButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                cancelButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                cancelButton.addActionListener(e -> {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to cancel the appointment booking?",
                            "Cancel Booking",
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        HospitalManagementSystem.showPage("MainPage");
                    }
                });

                bottomPanel.add(backButton);
                bottomPanel.add(cancelButton);
                mainContainer.add(bottomPanel, BorderLayout.SOUTH);

                add(mainContainer, new GridBagConstraints());
            }
        }


        public static class AppointmentBookingPage4 extends JPanel {
            public AppointmentBookingPage4(Patient patient, Doctor doctor, String reason, LocalDate date) {
                setOpaque(false);
                setLayout(new GridBagLayout());
                JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
                mainContainer.setOpaque(true);
                mainContainer.setBackground(new Color(255, 255, 255, 230));
                mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

                // Title
                JLabel title = new JLabel("Step 5: Select Time on " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                mainContainer.add(title, BorderLayout.NORTH);

                // Time slots panel - FIXED: Remove the method declaration from inside the lambda
                TimeSlotSelectionPanel timePanel = new TimeSlotSelectionPanel(doctor, date, time -> {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Confirm appointment for " + HospitalManagementSystem.tempPatientData.name +
                                    " with Dr. " + doctor.getName() + " on " + date + " at " +
                                    time.format(DateTimeFormatter.ofPattern("HH:mm")) + "?",
                            "Confirm Booking", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        createPatientAndAppointment(doctor, reason, LocalDateTime.of(date, time));
                    }
                });

                mainContainer.add(timePanel, BorderLayout.CENTER);

                // Bottom panel with navigation buttons
                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
                bottomPanel.setOpaque(false);

                JButton backButton = new JButton("⬅ Back to Calendar");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setForeground(Color.BLACK);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                backButton.addActionListener(e -> HospitalManagementSystem.showDoctorCalendar(patient, doctor, reason));

                JButton cancelButton = new JButton("Cancel Booking");
                cancelButton.setBackground(HospitalManagementSystem.COLOR_DANGER);
                cancelButton.setForeground(Color.WHITE);
                cancelButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                cancelButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                cancelButton.addActionListener(e -> {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to cancel the appointment booking?",
                            "Cancel Booking",
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        HospitalManagementSystem.showPage("MainPage");
                    }
                });

                bottomPanel.add(backButton);
                bottomPanel.add(cancelButton);
                mainContainer.add(bottomPanel, BorderLayout.SOUTH);

                add(mainContainer, new GridBagConstraints());
            }

            // MOVED: The method is now properly outside the constructor
            private void createPatientAndAppointment(Doctor doctor, String reason, LocalDateTime dateTime) {
                try {
                    // Create patient first
                    Patient patient = new Patient(0,
                            HospitalManagementSystem.tempPatientData.name,
                            HospitalManagementSystem.tempPatientData.contactNumber,
                            HospitalManagementSystem.tempPatientData.age);

                    int generatedId = HospitalManagementSystem.getDbManager().addPatient(patient);

                    if (generatedId > 0) {
                        // Create appointment with the new patient ID
                        HospitalManagementSystem.getDbManager().recordConsultation(
                                generatedId,
                                doctor.getId(),
                                reason,
                                dateTime,
                                null
                        );

                        JOptionPane.showMessageDialog(this,
                                "Booking confirmed! Your Patient ID is: " + generatedId,
                                "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Clear temporary data
                        HospitalManagementSystem.tempPatientData = null;
                        HospitalManagementSystem.showPage("MainPage");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to create patient record. Please try again.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error creating appointment: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


        public static class PatientInfoPage extends JPanel {
            public PatientInfoPage(Patient patient) {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                // Top panel with title and back button
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);

                JLabel title = new JLabel("Patient Portal - " + patient.getName(), SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                topPanel.add(title, BorderLayout.CENTER);
                // Add back button
                JButton backButton = new JButton("⬅ Back to Login");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(8, 15, 8, 15));
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("PatientLoginPage"));

                JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                backButtonPanel.setOpaque(false);
                backButtonPanel.add(backButton);
                topPanel.add(backButtonPanel, BorderLayout.EAST);

                contentPanel.add(topPanel, BorderLayout.NORTH);

                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.addTab("Consultation History", createConsultationsPanel(patient));
                tabbedPane.addTab("Prescriptions", createPrescriptionsPanel(patient));

                contentPanel.add(tabbedPane, BorderLayout.CENTER);
                add(contentPanel, BorderLayout.CENTER);
            }

            private JPanel createConsultationsPanel(Patient patient) {
                JPanel panel = new JPanel(new BorderLayout());
                JTextArea consultationsArea = new JTextArea();
                consultationsArea.setEditable(false);
                consultationsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

                List<Consultation> consultations = HospitalManagementSystem.getDbManager().getConsultationsForPatient(patient.getId());
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-20s | %-20s | %-30s | %s\n", "Date & Time", "Doctor", "Reason", "Next Appointment"));
                sb.append("-".repeat(100) + "\n");
                if (consultations.isEmpty()) {
                    sb.append("No consultation history found.");
                } else {
                    DatabaseManager db = HospitalManagementSystem.getDbManager();
                    for (Consultation c : consultations) {
                        Doctor doctor = db.getDoctorById(c.getDoctorId());
                        String doctorName = doctor != null ? doctor.getName() : "N/A";
                        String nextDate = c.getNextConsultingDate() != null ? c.getNextConsultingDate().toString() : "N/A";
                        sb.append(String.format("%-20s | %-20s | %-30s | %s\n",
                                c.getConsultationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                doctorName,
                                c.getConsultingReason(),

                                nextDate));
                    }
                }

                consultationsArea.setText(sb.toString());
                panel.add(new JScrollPane(consultationsArea), BorderLayout.CENTER);
                return panel;
            }

            private JPanel createPrescriptionsPanel(Patient patient) {
                JPanel panel = new JPanel(new BorderLayout());
                JTextArea prescriptionsArea = new JTextArea();
                prescriptionsArea.setEditable(false);
                prescriptionsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

                List<Consultation> consultations = HospitalManagementSystem.getDbManager().getConsultationsForPatient(patient.getId());
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-20s | %-20s | %-25s | %s\n", "Date", "Doctor", "Medicine", "Dosage"));
                sb.append("-".repeat(90) + "\n");

                boolean hasPrescriptions = false;
                DatabaseManager db = HospitalManagementSystem.getDbManager();

                for (Consultation c : consultations) {
                    List<Prescription> prescriptions = db.getPrescriptionsForConsultation(c.getId());
                    if (!prescriptions.isEmpty()) {
                        hasPrescriptions = true;
                        Doctor doctor = db.getDoctorById(c.getDoctorId());
                        String doctorName = doctor != null ? doctor.getName() : "N/A";
                        String date = c.getConsultationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        for (Prescription p : prescriptions) {
                            sb.append(String.format("%-20s | %-20s | %-25s | %s\n",
                                    date, doctorName, p.getMedicineName(), p.getDosage()));
                        }
                    }
                }

                if (!hasPrescriptions) {
                    sb.append("No prescriptions found.");
                }

                prescriptionsArea.setText(sb.toString());
                panel.add(new JScrollPane(prescriptionsArea), BorderLayout.CENTER);
                return panel;
            }
        }

        public static class PatientDetailsPage extends JPanel {
            private final JTextArea displayArea;
            private final JTextField searchField;
            private final boolean isAdminView;
            private final Doctor doctor;

            public PatientDetailsPage(boolean isAdminView, Doctor doctor) {
                this.isAdminView = isAdminView;
                this.doctor = doctor;
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);
                JLabel title = new JLabel(isAdminView ? "All Patient Details" : "My Patient Details", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                topPanel.add(title, BorderLayout.NORTH);
                JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                controlPanel.setOpaque(false);
                if (isAdminView) {
                    controlPanel.add(new JLabel("Search by Patient Name:"));
                    searchField = new JTextField(20);
                    searchField.addActionListener(e -> searchPatients());
                    controlPanel.add(searchField);
                    JButton searchButton = new JButton("Search");
                    searchButton.addActionListener(e -> searchPatients());
                    controlPanel.add(searchButton);
                } else {
                    searchField = null;
                }
                JButton backButton = new JButton("⬅ Back to Dashboard");
                backButton.addActionListener(e -> HospitalManagementSystem.showPage(isAdminView ? "AdminPage" : "DoctorPage"));
                JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                backButtonPanel.setOpaque(false);
                backButtonPanel.add(backButton);
                topPanel.add(controlPanel, BorderLayout.CENTER);
                topPanel.add(backButtonPanel, BorderLayout.EAST);
                contentPanel.add(topPanel, BorderLayout.NORTH);
                displayArea = new JTextArea();
                displayArea.setEditable(false);
                displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                contentPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
                add(contentPanel, BorderLayout.CENTER);
                loadPatientData();
            }

            private void loadPatientData() {
                List<Patient> patients;
                if (isAdminView) {
                    patients = HospitalManagementSystem.getDbManager().getAllPatients();
                } else {
                    patients = HospitalManagementSystem.getDbManager().getPatientsByDoctor(doctor.getId());
                }
                displayPatients(patients);
            }

            private void searchPatients() {
                String searchTerm = searchField.getText();
                List<Patient> patients = HospitalManagementSystem.getDbManager().getPatientsByName(searchTerm);
                displayPatients(patients);
            }

            private void displayPatients(List<Patient> patients) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-10s | %-25s | %-15s\n", "ID", "Name", "Contact"));
                sb.append("-".repeat(60) + "\n");
                if (patients.isEmpty()) {
                    sb.append("No patient records found.");
                } else {
                    for (Patient p : patients) {
                        sb.append(String.format("%-10d | %-25s | %-15s\n", p.getId(), p.getName(), p.getContactNumber()));
                    }
                }
                displayArea.setText(sb.toString());
                displayArea.setCaretPosition(0);
            }
        }

        public static class DoctorStatusPage extends JPanel {
            public DoctorStatusPage() {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);
                JLabel title = new JLabel("Real-Time Doctor Status", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                topPanel.add(title, BorderLayout.NORTH);
                JButton backButton = new JButton("⬅ Back to Dashboard");
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));
                JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                backButtonPanel.setOpaque(false);
                backButtonPanel.add(backButton);
                topPanel.add(backButtonPanel, BorderLayout.EAST);
                contentPanel.add(topPanel, BorderLayout.NORTH);
                JTextArea displayArea = new JTextArea();
                displayArea.setEditable(false);
                displayArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
                contentPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
                add(contentPanel, BorderLayout.CENTER);
                Timer timer = new Timer(5000, e -> updateStatus(displayArea));
                timer.setInitialDelay(0);
                timer.start();
                SwingUtilities.invokeLater(() -> updateStatus(displayArea));
                addHierarchyListener(e -> {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) {
                        timer.stop();
                    }
                });
            }

            private void updateStatus(JTextArea displayArea) {
                List<Doctor> doctors = HospitalManagementSystem.getDbManager().getAllDoctors();
                System.out.println("Retrieved " + doctors.size() + " doctors"); // Add this line

                if (doctors.isEmpty()) {
                    displayArea.setText("No doctors found in database.");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-20s | %-20s | %-12s | %-12s | %s\n", "Doctor Name", "Specialty", "Login Status", "Duty Status", "Last Activity"));
                sb.append("-".repeat(100) + "\n");
                for (Doctor d : doctors) {
                    String[] loginInfo = HospitalManagementSystem.getDbManager().getDoctorCurrentLoginStatus(d.getId());
                    boolean isOnDuty = HospitalManagementSystem.getDbManager().isDoctorOnDuty(d.getId());
                    String dutyStatus = isOnDuty ? "🟢 ON DUTY" : "🔴 OFF DUTY";
                    sb.append(String.format("%-20s | %-20s | %-12s | %-12s | %s\n",
                            d.getName(), d.getSpecialization(), loginInfo[0], dutyStatus, loginInfo[1]));
                }
                displayArea.setText(sb.toString());
            }
        }


        public static class AppointmentsViewPage extends JPanel {
            public AppointmentsViewPage(boolean isAdminView, Doctor doctor) {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);
                JLabel title = new JLabel(isAdminView ? "All Upcoming Appointments" : "My Upcoming Appointments", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                topPanel.add(title, BorderLayout.NORTH);
                JButton backButton = new JButton("⬅ Back to Dashboard");
                backButton.addActionListener(e -> HospitalManagementSystem.showPage(isAdminView ? "AdminPage" : "DoctorPage"));
                JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                backButtonPanel.setOpaque(false);
                backButtonPanel.add(backButton);
                topPanel.add(backButtonPanel, BorderLayout.EAST);
                contentPanel.add(topPanel, BorderLayout.NORTH);
                JTextArea displayArea = new JTextArea();
                displayArea.setEditable(false);
                displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                contentPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
                add(contentPanel, BorderLayout.CENTER);
                List<Consultation> appointments;
                if (isAdminView) {
                    appointments = HospitalManagementSystem.getDbManager().getAllUpcomingAppointments();
                } else {
                    appointments = HospitalManagementSystem.getDbManager().getUpcomingAppointments(doctor.getId());
                }
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-10s | %-20s | %-20s | %-20s | %s\n", "Patient ID", "Patient Name", "Doctor Name", "Appointment Time", "Reason"));
                sb.append("-".repeat(110) + "\n");
                if (appointments.isEmpty()) {
                    sb.append("No upcoming appointments found.");
                } else {
                    DatabaseManager db = HospitalManagementSystem.getDbManager();
                    appointments.forEach(a -> {
                        Patient p = db.getPatientById(a.getPatientId());
                        Doctor d = db.getDoctorById(a.getDoctorId());
                        sb.append(String.format("%-10d | %-20s | %-20s | %-20s | %s\n",
                                p != null ? p.getId() : -1,

                                p != null ? p.getName() : "N/A",
                                d != null ? d.getName() : "N/A",
                                a.getConsultationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),

                                a.getConsultingReason()));
                    });
                }
                displayArea.setText(sb.toString());
            }
        }

        public static class UserManagementPage extends JPanel {
            public UserManagementPage() {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                JLabel title = new JLabel("User Management", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                contentPanel.add(title, BorderLayout.NORTH);
                JLabel centerMessage = new JLabel("User Management Features Coming Soon", SwingConstants.CENTER);
                centerMessage.setFont(new Font("SansSerif", Font.BOLD, 18));
                contentPanel.add(centerMessage, BorderLayout.CENTER);

                add(contentPanel, BorderLayout.CENTER);
            }
        }

        public static class OnlinePharmacyPage extends JPanel {

            public OnlinePharmacyPage() {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                // Main content panel - make it more opaque
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 240)); // More opaque
                contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

                // Title
                JLabel title = new JLabel("Online Pharmacy", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 32));
                title.setForeground(HospitalManagementSystem.COLOR_FONT_DARK);
                contentPanel.add(title, BorderLayout.NORTH);

                // Center panel with options - simplified layout
                JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 20, 20));
                optionsPanel.setOpaque(true); // Make it opaque
                optionsPanel.setBackground(new Color(255, 255, 255, 200));
                optionsPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

                // Patient Medicine Button
                JButton patientMedicineButton = createStyledButton("Get Patient Medicines",
                        "Access prescribed medicines for specific patients");
                patientMedicineButton.addActionListener(e -> HospitalManagementSystem.showPage("PatientMedicinePage"));

                // Pharmacy Stock Button
                JButton pharmacyStockButton = createStyledButton("View Pharmacy Stock",
                        "Browse all available medicines in pharmacy");
                pharmacyStockButton.addActionListener(e -> HospitalManagementSystem.showPage("PharmacyStockPage"));

                optionsPanel.add(patientMedicineButton);
                optionsPanel.add(pharmacyStockButton);

                contentPanel.add(optionsPanel, BorderLayout.CENTER);

                // Back button
                JButton backButton = new JButton("⬅ Back to Home");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));

                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                bottomPanel.setOpaque(false);
                bottomPanel.add(backButton);
                contentPanel.add(bottomPanel, BorderLayout.SOUTH);

                add(contentPanel, BorderLayout.CENTER);
            }

            private JButton createStyledButton(String text, String description) {
                // Create a simple, visible button instead of complex panel
                JButton button = new JButton();
                button.setLayout(new BorderLayout());

                // Main text
                JLabel titleLabel = new JLabel(text, SwingConstants.CENTER);
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                titleLabel.setForeground(Color.WHITE);

                // Description text
                JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", SwingConstants.CENTER);
                descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                descLabel.setForeground(new Color(220, 220, 220));

                // Add labels to button
                JPanel textPanel = new JPanel(new BorderLayout());
                textPanel.setOpaque(false);
                textPanel.add(titleLabel, BorderLayout.CENTER);
                textPanel.add(descLabel, BorderLayout.SOUTH);

                button.add(textPanel, BorderLayout.CENTER);

                // Style the button
                button.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(20, 20, 20, 20));
                button.setFocusPainted(false);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                button.setPreferredSize(new Dimension(400, 100));

                // Add hover effect
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setBackground(HospitalManagementSystem.COLOR_SECONDARY);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                    }
                });

                return button;
            }
        }

        public static class PatientMedicinePage extends JPanel {
            private final JTextField searchField;
            private final JTextField patientIdField;
            private final JPanel resultsPanel;
            private final Map<Medicine, Integer> cart = new HashMap<>();
            private final JLabel cartLabel;

            public PatientMedicinePage() {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                // Top panel for patient ID, search and cart
                JPanel topPanel = new JPanel(new BorderLayout(10, 10));
                topPanel.setOpaque(false);

                // Patient ID panel (top section)
                JPanel patientIdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                patientIdPanel.setOpaque(false);
                patientIdField = new JTextField(15);
                JButton loadMedicinesButton = new JButton("Load Patient Medicines");
                patientIdPanel.add(new JLabel("Patient ID:"));
                patientIdPanel.add(patientIdField);
                patientIdPanel.add(loadMedicinesButton);

                // Search components (middle section)
                JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                searchPanel.setOpaque(false);
                searchField = new JTextField(30);
                JButton searchButton = new JButton("Search");
                searchPanel.add(new JLabel("Search Medicines:"));
                searchPanel.add(searchField);
                searchPanel.add(searchButton);

                // Combine patient ID and search panels
                JPanel leftControlsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
                leftControlsPanel.setOpaque(false);
                leftControlsPanel.add(patientIdPanel);
                leftControlsPanel.add(searchPanel);

                topPanel.add(leftControlsPanel, BorderLayout.CENTER);

                // Cart components (right section)
                JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                cartPanel.setOpaque(false);
                cartLabel = new JLabel("Cart: 0 items");
                cartLabel.setForeground(Color.WHITE);
                JButton viewCartButton = new JButton("View Cart");
                cartPanel.add(cartLabel);
                cartPanel.add(viewCartButton);
                topPanel.add(cartPanel, BorderLayout.EAST);

                add(topPanel, BorderLayout.NORTH);

                // Results panel
                resultsPanel = new JPanel();
                resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
                JScrollPane scrollPane = new JScrollPane(resultsPanel);
                add(scrollPane, BorderLayout.CENTER);

                // Back button
                JButton backButton = new JButton("⬅ Back to Pharmacy");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("OnlinePharmacyPage"));
                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                bottomPanel.setOpaque(false);
                bottomPanel.add(backButton);
                add(bottomPanel, BorderLayout.SOUTH);

                // Action Listeners
                loadMedicinesButton.addActionListener(e -> loadPatientMedicines());
                searchButton.addActionListener(e -> searchMedicines());
                viewCartButton.addActionListener(e -> viewCart());

                // Initially show message to enter patient ID
                showInitialMessage();
            }

            private void showInitialMessage() {
                resultsPanel.removeAll();
                JLabel messageLabel = new JLabel("<html><center>Please enter a Patient ID and click 'Load Patient Medicines'<br>to view prescribed medicines for that patient.</center></html>");
                messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
                resultsPanel.add(messageLabel);
                resultsPanel.revalidate();
                resultsPanel.repaint();
            }

            // Load medicines prescribed to a specific patient
            private void loadPatientMedicines() {
                String patientIdText = patientIdField.getText().trim();
                if (patientIdText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a Patient ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int patientId;
                try {
                    patientId = Integer.parseInt(patientIdText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid Patient ID (numbers only).", "Invalid ID", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if patient exists
                Patient patient = HospitalManagementSystem.getDbManager().getPatientById(patientId);
                if (patient == null) {
                    JOptionPane.showMessageDialog(this, "Patient with ID " + patientId + " not found.", "Patient Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get prescribed medicines for this patient
                List<Medicine> patientMedicines = getPatientPrescribedMedicines(patientId);
                displayMedicines(patientMedicines, "Prescribed medicines for Patient: " + patient.getName() + " (ID: " + patientId + ")");
            }

            private void searchMedicines() {
                String patientIdText = patientIdField.getText().trim();
                if (patientIdText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a Patient ID first.", "Patient ID Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int patientId;
                try {
                    patientId = Integer.parseInt(patientIdText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid Patient ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get prescribed medicines for this patient and filter by search term
                List<Medicine> patientMedicines = getPatientPrescribedMedicines(patientId);
                String searchTerm = searchField.getText().toLowerCase();

                List<Medicine> filteredMedicines = new ArrayList<>();
                for (Medicine med : patientMedicines) {
                    if (med.getName().toLowerCase().contains(searchTerm)) {
                        filteredMedicines.add(med);
                    }
                }

                displayMedicines(filteredMedicines, "Search results for: \"" + searchField.getText() + "\"");
            }

            // Get medicines prescribed to a specific patient
            private List<Medicine> getPatientPrescribedMedicines(int patientId) {
                List<Medicine> patientMedicines = new ArrayList<>();
                Set<String> prescribedMedicineNames = new HashSet<>();

                // Get all consultations for this patient
                List<Consultation> consultations = HospitalManagementSystem.getDbManager().getConsultationsForPatient(patientId);

                // Get all prescribed medicines from all consultations
                for (Consultation consultation : consultations) {
                    List<Prescription> prescriptions = HospitalManagementSystem.getDbManager().getPrescriptionsForConsultation(consultation.getId());
                    for (Prescription prescription : prescriptions) {
                        // NEW: Capitalize the first letter of medicine name from prescription
                        String capitalizedMedicineName = capitalizeMedicineName(prescription.getMedicineName());
                        prescribedMedicineNames.add(capitalizedMedicineName.toLowerCase()); // Store lowercase for comparison
                    }
                }

                // Find these medicines in the medicine inventory
                List<Medicine> allMedicines = HospitalManagementSystem.getDbManager().searchMedicines(""); // Get all medicines
                for (Medicine medicine : allMedicines) {
                    if (prescribedMedicineNames.contains(medicine.getName().toLowerCase())) {
                        // Create a new Medicine object with properly capitalized name
                        Medicine capitalizedMedicine = new Medicine(
                                medicine.getId(),
                                capitalizeMedicineName(medicine.getName()),
                                medicine.getPrice(),
                                medicine.getStock()
                        );
                        patientMedicines.add(capitalizedMedicine);
                    }
                }

                return patientMedicines;
            }

            // NEW: Helper method to capitalize medicine names properly
            private String capitalizeMedicineName(String medicineName) {
                if (medicineName == null || medicineName.trim().isEmpty()) {
                    return medicineName;
                }

                // Split the medicine name by spaces to handle multi-word medicine names
                String[] words = medicineName.trim().split("\\s+");
                StringBuilder capitalizedName = new StringBuilder();

                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (word.length() > 0) {
                        // Capitalize first letter and make rest lowercase
                        String capitalizedWord = word.substring(0, 1).toUpperCase() +
                                word.substring(1).toLowerCase();
                        capitalizedName.append(capitalizedWord);

                        // Add space between words (except for the last word)
                        if (i < words.length - 1) {
                            capitalizedName.append(" ");
                        }
                    }
                }

                return capitalizedName.toString();
            }

            private void displayMedicines(List<Medicine> medicines, String headerText) {
                resultsPanel.removeAll();

                // Add header
                JLabel headerLabel = new JLabel(headerText);
                headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
                resultsPanel.add(headerLabel);

                if (medicines.isEmpty()) {
                    JLabel noMedicinesLabel = new JLabel("No prescribed medicines found for this patient.");
                    noMedicinesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    noMedicinesLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    resultsPanel.add(noMedicinesLabel);
                } else {
                    for (Medicine med : medicines) {
                        resultsPanel.add(createMedicinePanel(med));
                    }
                }
                resultsPanel.revalidate();
                resultsPanel.repaint();
            }

            // Updated createMedicinePanel method with stock indicator
            private JPanel createMedicinePanel(Medicine med) {
                JPanel panel = new JPanel(new BorderLayout(10, 5));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        new EmptyBorder(10, 10, 10, 10)
                ));
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                // Medicine info - now uses properly capitalized name
                JLabel nameLabel = new JLabel(String.format("<html><b>%s</b><br>Price: ₹%.2f | Stock: %d</html>",
                        med.getName(), med.getPrice(), med.getStock()));
                panel.add(nameLabel, BorderLayout.CENTER);

                // Stock status indicator
                JLabel stockStatus = new JLabel();
                if (med.getStock() > 20) {
                    stockStatus.setText("✓ In Stock");
                    stockStatus.setForeground(HospitalManagementSystem.COLOR_SUCCESS);
                } else if (med.getStock() > 0) {
                    stockStatus.setText("⚠ Low Stock");
                    stockStatus.setForeground(Color.ORANGE);
                } else {
                    stockStatus.setText("✗ Out of Stock");
                    stockStatus.setForeground(HospitalManagementSystem.COLOR_DANGER);
                }
                stockStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
                panel.add(stockStatus, BorderLayout.WEST);

                // Add to cart button
                JButton addToCartButton = new JButton("Add to Cart");
                addToCartButton.addActionListener(e -> addToCart(med));
                if (med.getStock() == 0) {
                    addToCartButton.setEnabled(false);
                }
                panel.add(addToCartButton, BorderLayout.EAST);

                return panel;
            }

            private void addToCart(Medicine med) {
                if (med.getStock() > 0) {
                    cart.put(med, cart.getOrDefault(med, 0) + 1);
                    updateCartLabel();
                    JOptionPane.showMessageDialog(this, med.getName() + " added to cart!", "Added to Cart", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Sorry, this item is out of stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
                }
            }

            private void updateCartLabel() {
                int totalItems = cart.values().stream().mapToInt(Integer::intValue).sum();
                cartLabel.setText("Cart: " + totalItems + " items");
            }

            private void viewCart() {
                if (cart.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Your cart is empty.", "Empty Cart", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JDialog cartDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Shopping Cart", Dialog.ModalityType.APPLICATION_MODAL);
                cartDialog.setSize(400, 300);
                cartDialog.setLayout(new BorderLayout(10, 10));

                JTextArea cartItemsArea = new JTextArea();
                cartItemsArea.setEditable(false);
                double total = 0;
                for (Map.Entry<Medicine, Integer> entry : cart.entrySet()) {
                    Medicine med = entry.getKey();
                    int quantity = entry.getValue();
                    cartItemsArea.append(String.format("%s (x%d) - ₹%.2f\n", med.getName(), quantity, med.getPrice() * quantity));
                    total += med.getPrice() * quantity;
                }
                cartItemsArea.append("\nTotal: ₹" + String.format("%.2f", total));
                cartDialog.add(new JScrollPane(cartItemsArea), BorderLayout.CENTER);

                JButton checkoutButton = new JButton("Checkout");
                checkoutButton.addActionListener(e -> {
                    cartDialog.dispose();
                    HospitalManagementSystem.showAddressPage(cart);
                });
                cartDialog.add(checkoutButton, BorderLayout.SOUTH);

                cartDialog.setLocationRelativeTo(this);
                cartDialog.setVisible(true);
            }
        }


        public static class PharmacyStockPage extends JPanel {
            private final JTextField searchField;
            private final JPanel resultsPanel;
            private final Map<Medicine, Integer> cart = new HashMap<>();
            private final JLabel cartLabel;
            private List<Medicine> allMedicines;

            public PharmacyStockPage() {
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                // Top panel for search and cart
                JPanel topPanel = new JPanel(new BorderLayout(10, 10));
                topPanel.setOpaque(false);

                // Search components
                JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                searchPanel.setOpaque(false);
                searchField = new JTextField(30);
                JButton searchButton = new JButton("Search");
                JButton showAllButton = new JButton("Show All");
                searchPanel.add(new JLabel("Search Medicines:"));
                searchPanel.add(searchField);
                searchPanel.add(searchButton);
                searchPanel.add(showAllButton);

                topPanel.add(searchPanel, BorderLayout.CENTER);

                // Cart components (right section)
                JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                cartPanel.setOpaque(false);
                cartLabel = new JLabel("Cart: 0 items");
                cartLabel.setForeground(Color.WHITE);
                JButton viewCartButton = new JButton("View Cart");
                cartPanel.add(cartLabel);
                cartPanel.add(viewCartButton);
                topPanel.add(cartPanel, BorderLayout.EAST);

                add(topPanel, BorderLayout.NORTH);

                // Results panel
                resultsPanel = new JPanel();
                resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
                JScrollPane scrollPane = new JScrollPane(resultsPanel);
                add(scrollPane, BorderLayout.CENTER);

                // Back button
                JButton backButton = new JButton("⬅ Back to Pharmacy");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("OnlinePharmacyPage"));
                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                bottomPanel.setOpaque(false);
                bottomPanel.add(backButton);
                add(bottomPanel, BorderLayout.SOUTH);

                // Action Listeners
                searchButton.addActionListener(e -> searchMedicines());
                showAllButton.addActionListener(e -> showAllMedicines());
                viewCartButton.addActionListener(e -> viewCart());

                // Load all medicines initially
                loadAllMedicines();
            }

            private void loadAllMedicines() {
                allMedicines = HospitalManagementSystem.getDbManager().searchMedicines(""); // Get all medicines
                showAllMedicines();
            }

            private void showAllMedicines() {
                displayMedicines(allMedicines, "All Medicines in Pharmacy Stock (" + allMedicines.size() + " items)");
            }

            private void searchMedicines() {
                String searchTerm = searchField.getText().toLowerCase().trim();
                if (searchTerm.isEmpty()) {
                    showAllMedicines();
                    return;
                }

                List<Medicine> filteredMedicines = new ArrayList<>();
                for (Medicine med : allMedicines) {
                    if (med.getName().toLowerCase().contains(searchTerm)) {
                        filteredMedicines.add(med);
                    }
                }

                displayMedicines(filteredMedicines, "Search results for: \"" + searchField.getText() + "\" (" + filteredMedicines.size() + " items)");
            }

            private void displayMedicines(List<Medicine> medicines, String headerText) {
                resultsPanel.removeAll();

                // Add header
                JLabel headerLabel = new JLabel(headerText);
                headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
                resultsPanel.add(headerLabel);

                if (medicines.isEmpty()) {
                    JLabel noMedicinesLabel = new JLabel("No medicines found matching your search.");
                    noMedicinesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    noMedicinesLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    resultsPanel.add(noMedicinesLabel);
                } else {
                    for (Medicine med : medicines) {
                        resultsPanel.add(createMedicinePanel(med));
                    }
                }
                resultsPanel.revalidate();
                resultsPanel.repaint();
            }

            private JPanel createMedicinePanel(Medicine med) {
                JPanel panel = new JPanel(new BorderLayout(10, 5));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        new EmptyBorder(10, 10, 10, 10)
                ));
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                // Medicine info
                JLabel nameLabel = new JLabel(String.format("<html><b>%s</b><br>Price: ₹%.2f | Stock: %d</html>",
                        med.getName(), med.getPrice(), med.getStock()));
                panel.add(nameLabel, BorderLayout.CENTER);

                // Stock status indicator
                JLabel stockStatus = new JLabel();
                if (med.getStock() > 20) {
                    stockStatus.setText("✓ In Stock");
                    stockStatus.setForeground(HospitalManagementSystem.COLOR_SUCCESS);
                } else if (med.getStock() > 0) {
                    stockStatus.setText("⚠ Low Stock");
                    stockStatus.setForeground(Color.ORANGE);
                } else {
                    stockStatus.setText("✗ Out of Stock");
                    stockStatus.setForeground(HospitalManagementSystem.COLOR_DANGER);
                }
                stockStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
                panel.add(stockStatus, BorderLayout.WEST);

                // Add to cart button
                JButton addToCartButton = new JButton("Add to Cart");
                addToCartButton.addActionListener(e -> addToCart(med));
                if (med.getStock() == 0) {
                    addToCartButton.setEnabled(false);
                }
                panel.add(addToCartButton, BorderLayout.EAST);

                return panel;
            }

            private void addToCart(Medicine med) {
                if (med.getStock() > 0) {
                    cart.put(med, cart.getOrDefault(med, 0) + 1);
                    updateCartLabel();
                    JOptionPane.showMessageDialog(this, med.getName() + " added to cart!", "Added to Cart", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Sorry, this item is out of stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
                }
            }

            private void updateCartLabel() {
                int totalItems = cart.values().stream().mapToInt(Integer::intValue).sum();
                cartLabel.setText("Cart: " + totalItems + " items");
            }

            private void viewCart() {
                if (cart.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Your cart is empty.", "Empty Cart", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JDialog cartDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Shopping Cart", Dialog.ModalityType.APPLICATION_MODAL);
                cartDialog.setSize(400, 300);
                cartDialog.setLayout(new BorderLayout(10, 10));

                JTextArea cartItemsArea = new JTextArea();
                cartItemsArea.setEditable(false);
                double total = 0;
                for (Map.Entry<Medicine, Integer> entry : cart.entrySet()) {
                    Medicine med = entry.getKey();
                    int quantity = entry.getValue();
                    cartItemsArea.append(String.format("%s (x%d) - ₹%.2f\n", med.getName(), quantity, med.getPrice() * quantity));
                    total += med.getPrice() * quantity;
                }
                cartItemsArea.append("\nTotal: ₹" + String.format("%.2f", total));
                cartDialog.add(new JScrollPane(cartItemsArea), BorderLayout.CENTER);

                JButton checkoutButton = new JButton("Checkout");
                checkoutButton.addActionListener(e -> {
                    cartDialog.dispose();
                    HospitalManagementSystem.showAddressPage(cart);
                });
                cartDialog.add(checkoutButton, BorderLayout.SOUTH);

                cartDialog.setLocationRelativeTo(this);
                cartDialog.setVisible(true);
            }
        }


        public static class AddressPage extends JPanel {
            private final JTextField nameField, addressField, cityField, pincodeField;

            public AddressPage(Map<Medicine, Integer> cart) {
                setOpaque(false);
                setLayout(new GridBagLayout());
                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(new Color(255, 255, 255, 230));
                formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(10, 10, 10, 10);

                // Title
                JLabel titleLabel = new JLabel("Enter Delivery Address");
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                titleLabel.setForeground(HospitalManagementSystem.COLOR_FONT_DARK);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 2;
                gbc.anchor = GridBagConstraints.CENTER;
                formPanel.add(titleLabel, gbc);

                // Reset constraints for form fields
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.EAST;

                // Full Name field
                gbc.gridx = 0;
                gbc.gridy = 1;
                JLabel nameLabel = new JLabel("Full Name:");
                nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                formPanel.add(nameLabel, gbc);

                nameField = new JTextField(20);
                nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                formPanel.add(nameField, gbc);

                // Address field
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.anchor = GridBagConstraints.EAST;
                JLabel addressLabel = new JLabel("Street Address:");
                addressLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                formPanel.add(addressLabel, gbc);

                addressField = new JTextField(20);
                addressField.setFont(new Font("SansSerif", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                formPanel.add(addressField, gbc);

                // City field
                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.anchor = GridBagConstraints.EAST;
                JLabel cityLabel = new JLabel("City:");
                cityLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                formPanel.add(cityLabel, gbc);

                cityField = new JTextField(20);
                cityField.setFont(new Font("SansSerif", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                formPanel.add(cityField, gbc);

                // Pincode field
                gbc.gridx = 0;
                gbc.gridy = 4;
                gbc.anchor = GridBagConstraints.EAST;
                JLabel pincodeLabel = new JLabel("Pincode:");
                pincodeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                formPanel.add(pincodeLabel, gbc);

                pincodeField = new JTextField(20);
                pincodeField.setFont(new Font("SansSerif", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                formPanel.add(pincodeField, gbc);

                // Buttons panel
                JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
                buttonsPanel.setOpaque(false);

                // Back button
                JButton backButton = new JButton("← Back to Cart");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setForeground(Color.BLACK);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("OnlinePharmacyPage"));

                // Proceed button with validation
                JButton proceedButton = new JButton("Proceed to Payment");
                proceedButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                proceedButton.setForeground(Color.WHITE);
                proceedButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                proceedButton.setBorder(new EmptyBorder(10, 20, 10, 20));

                // ADD VALIDATION HERE
                proceedButton.addActionListener(e -> {
                    // Validate all fields
                    if (!validateFields()) {
                        return; // Don't proceed if validation fails
                    }

                    // If validation passes, proceed with payment
                    String address = String.format("%s, %s, %s - %s",
                            nameField.getText().trim(),
                            addressField.getText().trim(),
                            cityField.getText().trim(),
                            pincodeField.getText().trim());
                    HospitalManagementSystem.showPharmacyPaymentPage(cart, address);
                });

                buttonsPanel.add(backButton);
                buttonsPanel.add(proceedButton);

                gbc.gridy = 5;
                gbc.gridx = 0;
                gbc.gridwidth = 2;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets = new Insets(20, 10, 10, 10);
                formPanel.add(buttonsPanel, gbc);

                add(formPanel);
            }

            // NEW METHOD: Validate all address fields
            private boolean validateFields() {
                String name = nameField.getText().trim();
                String address = addressField.getText().trim();
                String city = cityField.getText().trim();
                String pincode = pincodeField.getText().trim();

                // Check if any field is empty
                if (name.isEmpty()) {
                    showValidationError("Please enter your full name.");
                    nameField.requestFocus();
                    return false;
                }

                if (address.isEmpty()) {
                    showValidationError("Please enter your street address.");
                    addressField.requestFocus();
                    return false;
                }

                if (city.isEmpty()) {
                    showValidationError("Please enter your city.");
                    cityField.requestFocus();
                    return false;
                }

                if (pincode.isEmpty()) {
                    showValidationError("Please enter your pincode.");
                    pincodeField.requestFocus();
                    return false;
                }

                // Validate pincode format (should be 6 digits)
                if (!pincode.matches("\\d{6}")) {
                    showValidationError("Pincode must be exactly 6 digits.");
                    pincodeField.requestFocus();
                    pincodeField.selectAll();
                    return false;
                }

                // Validate name (should contain only letters and spaces)
                if (!name.matches("^[a-zA-Z\\s]+$")) {
                    showValidationError("Name should contain only letters and spaces.");
                    nameField.requestFocus();
                    nameField.selectAll();
                    return false;
                }

                // Validate minimum length requirements
                if (name.length() < 2) {
                    showValidationError("Name must be at least 2 characters long.");
                    nameField.requestFocus();
                    nameField.selectAll();
                    return false;
                }

                if (address.length() < 5) {
                    showValidationError("Address must be at least 5 characters long.");
                    addressField.requestFocus();
                    addressField.selectAll();
                    return false;
                }

                if (city.length() < 2) {
                    showValidationError("City name must be at least 2 characters long.");
                    cityField.requestFocus();
                    cityField.selectAll();
                    return false;
                }

                return true; // All validations passed
            }

            // Helper method to show validation error messages
            private void showValidationError(String message) {
                JOptionPane.showMessageDialog(this,
                        message,
                        "Address Validation Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }


        public static class PharmacyPaymentPage extends JPanel {
            private final Map<Medicine, Integer> cart;
            private final String address;

            public PharmacyPaymentPage(Map<Medicine, Integer> cart, String address) {
                this.cart = cart;
                this.address = address;
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                // Main content panel
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

                // Title
                JLabel title = new JLabel("Order Summary", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                contentPanel.add(title, BorderLayout.NORTH);

                // Order details
                JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
                orderPanel.setOpaque(false);

                // Cart items
                JTextArea orderDetails = new JTextArea();
                orderDetails.setEditable(false);
                orderDetails.setFont(new Font("Monospaced", Font.PLAIN, 14));

                StringBuilder orderText = new StringBuilder();
                orderText.append("DELIVERY ADDRESS:\n");
                orderText.append(address).append("\n\n");
                orderText.append("ORDERED MEDICINES:\n");
                orderText.append("-".repeat(50)).append("\n");

                double subtotal = 0;
                for (Map.Entry<Medicine, Integer> entry : cart.entrySet()) {
                    Medicine med = entry.getKey();
                    int quantity = entry.getValue();
                    double itemTotal = med.getPrice() * quantity;
                    subtotal += itemTotal;

                    orderText.append(String.format("%-20s x%d @ ₹%.2f = ₹%.2f\n",
                            med.getName(), quantity, med.getPrice(), itemTotal));
                }

                orderText.append("-".repeat(50)).append("\n");
                orderText.append(String.format("Subtotal: ₹%.2f\n", subtotal));
                orderText.append(String.format("Delivery Charges: ₹50.00\n"));
                orderText.append(String.format("Tax (5%%): ₹%.2f\n", subtotal * 0.05));
                orderText.append("-".repeat(50)).append("\n");
                orderText.append(String.format("TOTAL: ₹%.2f\n", subtotal + 50 + (subtotal * 0.05)));

                orderDetails.setText(orderText.toString());
                orderPanel.add(new JScrollPane(orderDetails), BorderLayout.CENTER);

                contentPanel.add(orderPanel, BorderLayout.CENTER);

                // Buttons
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
                buttonPanel.setOpaque(false);

                JButton proceedToPaymentButton = new JButton("Proceed to Payment");
                proceedToPaymentButton.setBackground(HospitalManagementSystem.COLOR_SUCCESS);
                proceedToPaymentButton.setForeground(Color.WHITE);
                proceedToPaymentButton.setFont(new Font("SansSerif", Font.BOLD, 16));
                proceedToPaymentButton.setBorder(new EmptyBorder(12, 25, 12, 25));
                proceedToPaymentButton.addActionListener(e -> proceedToPayment());

                JButton backButton = new JButton("⬅ Back to Cart");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("OnlinePharmacyPage"));

                buttonPanel.add(backButton);
                buttonPanel.add(proceedToPaymentButton);
                contentPanel.add(buttonPanel, BorderLayout.SOUTH);

                add(contentPanel, BorderLayout.CENTER);
            }

            private void proceedToPayment() {
                // Calculate total amount including delivery and tax
                double subtotal = 0;
                for (Map.Entry<Medicine, Integer> entry : cart.entrySet()) {
                    Medicine med = entry.getKey();
                    int quantity = entry.getValue();
                    subtotal += med.getPrice() * quantity;
                }

                double deliveryCharges = 50.00;
                double tax = subtotal * 0.05;
                double totalAmount = subtotal + deliveryCharges + tax;

                // Forward to EnhancedOnlinePaymentPage with pharmacy data
                HospitalManagementSystem.showPharmacyEnhancedPaymentPage(cart, address, totalAmount);
            }
        }


        public static class DoctorCalendarPanel extends JPanel {
            private YearMonth currentMonth;

            public DoctorCalendarPanel(Doctor doctor, DateSelectionListener listener) {
                List<DayOfWeek> availableDays = HospitalManagementSystem.getDbManager().getDoctorAvailableDays(doctor.getId());
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                currentMonth = YearMonth.now();

                JPanel navigationPanel = new JPanel(new BorderLayout());
                navigationPanel.setOpaque(false);
                JButton prevButton = new JButton("<");
                JButton nextButton = new JButton(">");
                JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
                monthLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                navigationPanel.add(prevButton, BorderLayout.WEST);
                navigationPanel.add(monthLabel, BorderLayout.CENTER);
                navigationPanel.add(nextButton, BorderLayout.EAST);
                add(navigationPanel, BorderLayout.NORTH);

                JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
                calendarGrid.setOpaque(false);
                add(calendarGrid, BorderLayout.CENTER);

                prevButton.addActionListener(e -> {
                    currentMonth = currentMonth.minusMonths(1);
                    updateCalendar(calendarGrid, monthLabel, currentMonth, availableDays, listener);
                });
                nextButton.addActionListener(e -> {
                    currentMonth = currentMonth.plusMonths(1);
                    updateCalendar(calendarGrid, monthLabel, currentMonth, availableDays, listener);
                });

                updateCalendar(calendarGrid, monthLabel, currentMonth, availableDays, listener);
            }

            private void updateCalendar(JPanel grid, JLabel label, YearMonth month, List<DayOfWeek> availableDays, DateSelectionListener listener) {
                grid.removeAll();
                label.setText(month.getMonth().name() + " " + month.getYear());
                String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                for (String day : daysOfWeek) {
                    JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
                    dayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                    grid.add(dayLabel);
                }
                LocalDate firstOfMonth = month.atDay(1);
                int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7;
                for (int i = 0; i < dayOfWeekValue; i++) {
                    grid.add(new JLabel(""));
                }
                for (int day = 1; day <= month.lengthOfMonth(); day++) {
                    LocalDate date = month.atDay(day);
                    JButton dayButton = new JButton(String.valueOf(day));
                    dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    boolean isAvailable = availableDays.contains(date.getDayOfWeek()) && date.isAfter(LocalDate.now().minusDays(1));
                    if (isAvailable) {
                        dayButton.setBackground(new Color(144, 238, 144));
                        dayButton.addActionListener(e -> listener.dateSelected(date));
                    } else {
                        dayButton.setBackground(Color.LIGHT_GRAY);
                        dayButton.setEnabled(false);
                    }
                    grid.add(dayButton);
                }
                grid.revalidate();
                grid.repaint();
            }

            @FunctionalInterface
            interface DateSelectionListener {
                void dateSelected(LocalDate date);
            }
        }

        public static class TimeSlotSelectionPanel extends JPanel {
            public TimeSlotSelectionPanel(Doctor doctor, LocalDate date, TimeSlotSelectionListener listener) {
                setOpaque(false);
                setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
                List<LocalTime> timeSlots = HospitalManagementSystem.getDbManager().getAvailableTimeSlotsForDay(doctor.getId(), date.getDayOfWeek());
                if (timeSlots.isEmpty()) {
                    add(new JLabel("No available time slots for this day."));
                } else {
                    for (LocalTime time : timeSlots) {
                        JButton timeButton = new JButton(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                        timeButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                        timeButton.setForeground(Color.WHITE);
                        timeButton.addActionListener(e -> listener.timeSlotSelected(time));
                        add(timeButton);
                    }
                }
            }

            @FunctionalInterface
            interface TimeSlotSelectionListener {
                void timeSlotSelected(LocalTime time);
            }
        }

        public static class DoctorManagementPage extends JPanel {
            private final JPanel doctorsPanel;
            private final List<DoctorTogglePanel> doctorTogglePanels;

            public DoctorManagementPage() {
                this.doctorTogglePanels = new ArrayList<>();
                setOpaque(false);
                setLayout(new BorderLayout(10, 10));
                setBorder(new EmptyBorder(20, 20, 20, 20));

                // Main content panel
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(new Color(255, 255, 255, 230));
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                // Top panel with title and back button
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);

                JLabel title = new JLabel("Doctor Management - Toggle Duty Status", SwingConstants.CENTER);
                title.setFont(new Font("SansSerif", Font.BOLD, 24));
                topPanel.add(title, BorderLayout.NORTH);
                JButton backButton = new JButton("⬅ Back to Dashboard");
                backButton.setBackground(Color.LIGHT_GRAY);
                backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                backButton.setBorder(new EmptyBorder(8, 15, 8, 15));
                backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));
                JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                backButtonPanel.setOpaque(false);
                backButtonPanel.add(backButton);
                topPanel.add(backButtonPanel, BorderLayout.EAST);

                contentPanel.add(topPanel, BorderLayout.NORTH);
                // Doctors panel with scroll
                doctorsPanel = new JPanel();
                doctorsPanel.setLayout(new BoxLayout(doctorsPanel, BoxLayout.Y_AXIS));
                doctorsPanel.setOpaque(false);
                JScrollPane scrollPane = new JScrollPane(doctorsPanel);
                scrollPane.setPreferredSize(new Dimension(800, 500));
                scrollPane.setOpaque(false);
                scrollPane.getViewport().setOpaque(false);
                contentPanel.add(scrollPane, BorderLayout.CENTER);
                // Refresh button
                JButton refreshButton = new JButton("🔄 Refresh Status");
                refreshButton.setBackground(HospitalManagementSystem.COLOR_SECONDARY);
                refreshButton.setForeground(Color.WHITE);
                refreshButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                refreshButton.setBorder(new EmptyBorder(10, 20, 10, 20));
                refreshButton.addActionListener(e -> refreshDoctorsList());
                JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                refreshPanel.setOpaque(false);
                refreshPanel.add(refreshButton);
                contentPanel.add(refreshPanel, BorderLayout.SOUTH);

                add(contentPanel, BorderLayout.CENTER);

                // Load doctors initially
                loadDoctors();
            }

            private void loadDoctors() {
                doctorsPanel.removeAll();
                doctorTogglePanels.clear();
                List<Doctor> doctors = HospitalManagementSystem.getDbManager().getAllDoctors();

                if (doctors.isEmpty()) {
                    JLabel noDocsLabel = new JLabel("No doctors found in the system.", SwingConstants.CENTER);
                    noDocsLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    doctorsPanel.add(noDocsLabel);
                } else {
                    for (Doctor doctor : doctors) {
                        DoctorTogglePanel togglePanel = new DoctorTogglePanel(doctor);
                        doctorTogglePanels.add(togglePanel);
                        doctorsPanel.add(togglePanel);
                        doctorsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing between panels
                    }
                }

                doctorsPanel.revalidate();
                doctorsPanel.repaint();
            }

            private void refreshDoctorsList() {
                loadDoctors();
                JOptionPane.showMessageDialog(this, "Doctor status refreshed successfully!", "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
            }

            // Inner class for individual doctor toggle panels
            // Inner class for individual doctor toggle panels
            private class DoctorTogglePanel extends JPanel {
                private final Doctor doctor;
                private final JToggleButton dutyToggleButton;
                private final JLabel statusLabel;

                public DoctorTogglePanel(Doctor doctor) {
                    this.doctor = doctor;
                    setLayout(new BorderLayout(10, 10));
                    setBackground(new Color(248, 249, 250));
                    setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(200, 200, 200), 1),
                            new EmptyBorder(15, 20, 15, 20)
                    ));
                    setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                    // Doctor info panel (left side)
                    JPanel doctorInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
                    doctorInfoPanel.setOpaque(false);

                    JLabel nameLabel = new JLabel("Dr. " + doctor.getName());
                    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                    nameLabel.setForeground(HospitalManagementSystem.COLOR_FONT_DARK);
                    JLabel specializationLabel = new JLabel("Specialization: " + doctor.getSpecialization());
                    specializationLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    specializationLabel.setForeground(Color.GRAY);

                    doctorInfoPanel.add(nameLabel);
                    doctorInfoPanel.add(specializationLabel);
                    add(doctorInfoPanel, BorderLayout.WEST);
                    // Status and toggle panel (right side)
                    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
                    controlPanel.setOpaque(false);

                    // Status label
                    statusLabel = new JLabel();
                    statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                    updateStatusLabel();

                    // Toggle button
                    dutyToggleButton = new JToggleButton();
                    dutyToggleButton.setFont(new Font("SansSerif", Font.BOLD, 12));
                    dutyToggleButton.setBorder(new EmptyBorder(8, 16, 8, 16));
                    dutyToggleButton.setFocusPainted(false);
                    dutyToggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    // Set initial state
                    boolean isOnDuty = HospitalManagementSystem.getDbManager().isDoctorOnDuty(doctor.getId());
                    dutyToggleButton.setSelected(isOnDuty);
                    updateToggleButton();

                    dutyToggleButton.addActionListener(e -> toggleDutyStatus());

                    controlPanel.add(statusLabel);
                    controlPanel.add(dutyToggleButton);
                    add(controlPanel, BorderLayout.EAST);
                }

                private void toggleDutyStatus() {
                    boolean newStatus = dutyToggleButton.isSelected();
                    try {
                        // Update database
                        HospitalManagementSystem.getDbManager().updateDoctorDutyStatus(doctor.getId(), newStatus);
                        // Update UI
                        updateToggleButton();
                        updateStatusLabel();
                        // Show confirmation
                        String message = newStatus ?
                                "Dr. " + doctor.getName() + " is now ON DUTY" :
                                "Dr. " + doctor.getName() + " is now OFF DUTY";
                        // Create a small notification instead of a popup
                        showNotification(message);
                    } catch (Exception ex) {
                        // Revert toggle if database update failed
                        dutyToggleButton.setSelected(!newStatus);
                        JOptionPane.showMessageDialog(this,
                                "Failed to update duty status for Dr. " + doctor.getName(),
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

                private void updateToggleButton() {
                    if (dutyToggleButton.isSelected()) {
                        dutyToggleButton.setText("🟢 ON DUTY");
                        dutyToggleButton.setBackground(HospitalManagementSystem.COLOR_SUCCESS);
                        dutyToggleButton.setForeground(Color.WHITE);
                    } else {
                        dutyToggleButton.setText("🔴 OFF DUTY");
                        dutyToggleButton.setBackground(HospitalManagementSystem.COLOR_DANGER);
                        dutyToggleButton.setForeground(Color.WHITE);
                    }
                }

                private void updateStatusLabel() {
                    boolean isOnDuty = HospitalManagementSystem.getDbManager().isDoctorOnDuty(doctor.getId());
                    if (isOnDuty) {
                        statusLabel.setText("Status: Available");
                        statusLabel.setForeground(HospitalManagementSystem.COLOR_SUCCESS);
                    } else {
                        statusLabel.setText("Status: Offline");
                        statusLabel.setForeground(HospitalManagementSystem.COLOR_DANGER);
                    }
                }

                private void showNotification(String message) {
                    // Create a simple notification that disappears after 2 seconds
                    JLabel notification = new JLabel(message);
                    notification.setOpaque(true);
                    notification.setBackground(new Color(76, 175, 80, 200));
                    notification.setForeground(Color.WHITE);
                    notification.setFont(new Font("SansSerif", Font.BOLD, 12));
                    notification.setBorder(new EmptyBorder(5, 10, 5, 10));
                    // You could add this to a notification area or just print to console
                    System.out.println("✓ " + message);
                }
            }
        }
    }

    public static class PatientMedicinePage extends JPanel {
        public PatientMedicinePage() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Patient Medicine Page - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Home");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class PharmacyStockPage extends JPanel {
        public PharmacyStockPage() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Pharmacy Stock Page - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Home");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class DoctorStatusPage extends JPanel {
        public DoctorStatusPage() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Doctor Status Page - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Admin");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class DoctorManagementPage extends JPanel {
        public DoctorManagementPage() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Doctor Management Page - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Admin");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class AppointmentsViewPage extends JPanel {
        public AppointmentsViewPage(boolean isAdmin, Doctor doctor) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Appointments View - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton(isAdmin ? "Back to Admin" : "Back to Doctor Dashboard");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage(isAdmin ? "AdminPage" : "DoctorPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class PatientDetailsPage extends JPanel {
        public PatientDetailsPage(boolean isAdmin, Doctor doctor) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Patient Details - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton(isAdmin ? "Back to Admin" : "Back to Doctor Dashboard");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage(isAdmin ? "AdminPage" : "DoctorPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class UserManagementPage extends JPanel {
        public UserManagementPage() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("User Management - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Admin");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class AddressPage extends JPanel {
        public AddressPage(Map<Medicine, Integer> cart) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Address Page - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Pharmacy");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("OnlinePharmacyPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class PharmacyPaymentPage extends JPanel {
        public PharmacyPaymentPage(Map<Medicine, Integer> cart, String address) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Pharmacy Payment - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Pharmacy");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("OnlinePharmacyPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class AppointmentBookingPage2 extends JPanel {
        public AppointmentBookingPage2(Patient patient) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Appointment Booking Step 2 - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Home");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class AppointmentBookingPage2_5 extends JPanel {
        public AppointmentBookingPage2_5(Patient patient, String symptoms) {
            setOpaque(false);
            setLayout(new GridBagLayout());
            JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
            mainContainer.setOpaque(true);
            mainContainer.setBackground(new Color(255, 255, 255, 230));
            mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Title
            JLabel title = new JLabel("Step 3: Recommended Doctors Based on Your Symptoms", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            mainContainer.add(title, BorderLayout.NORTH);

            // NEW: Check patient age and override suggestions for children
            List<Doctor> suggestedDoctors;
            if (patient != null && patient.getAge() < 18) {
                // For children under 18, ONLY show pediatricians
                suggestedDoctors = HospitalManagementSystem.getDbManager().getSuggestedDoctors("child");
            } else if (patient == null) {
                // For temporary patient data, use symptom-based suggestions
                suggestedDoctors = HospitalManagementSystem.getDbManager().getSuggestedDoctors(symptoms);
            } else {
                // For adults, use normal symptom-based suggestions
                suggestedDoctors = HospitalManagementSystem.getDbManager().getSuggestedDoctors(symptoms);
            }

            JPanel doctorsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            doctorsPanel.setBorder(BorderFactory.createTitledBorder(
                    (patient != null && patient.getAge() < 18) ?
                            "Pediatricians - Recommended for children under 18" :
                            "Recommended Doctors"));

            if (suggestedDoctors.isEmpty()) {
                JLabel noDocsLabel = new JLabel("No doctors available for this specialization.");
                noDocsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                doctorsPanel.add(noDocsLabel);

                // Fallback to general doctors if no pediatricians found
                if (patient != null && patient.getAge() < 18) {
                    suggestedDoctors = HospitalManagementSystem.getDbManager().getDoctorsOnDuty();
                }
            }

            for (Doctor doctor : suggestedDoctors) {
                JPanel doctorPanel = new JPanel(new BorderLayout(10, 5));
                doctorPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                doctorPanel.setBackground(Color.WHITE);

                JLabel doctorInfo = new JLabel("<html><b>Dr. " + doctor.getName() + "</b><br>" +
                        "Specialization: " + doctor.getSpecialization() + "</html>");
                doctorInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
                doctorPanel.add(doctorInfo, BorderLayout.CENTER);

                JButton selectButton = new JButton("Select");
                selectButton.setBackground(HospitalManagementSystem.COLOR_PRIMARY);
                selectButton.setForeground(Color.WHITE);
                selectButton.addActionListener(e -> {
                    if (patient != null) {
                        HospitalManagementSystem.showDoctorCalendar(patient, doctor, symptoms);
                    } else {
                        // Use temp data for calendar
                        HospitalManagementSystem.showDoctorCalendarWithTempData(doctor, symptoms);
                    }
                });

                doctorPanel.add(selectButton, BorderLayout.EAST);
                doctorsPanel.add(doctorPanel);
            }

            JScrollPane doctorsScrollPane = new JScrollPane(doctorsPanel);
            doctorsScrollPane.setPreferredSize(new Dimension(600, 300));
            mainContainer.add(doctorsScrollPane, BorderLayout.CENTER);

            JButton backButton = new JButton("← Back to Symptoms");
            backButton.setBackground(Color.LIGHT_GRAY);
            backButton.addActionListener(e -> {
                if (patient != null) {
                    HospitalManagementSystem.startAppointmentBooking(patient);
                } else {
                    HospitalManagementSystem.startAppointmentBookingWithTempData();
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setOpaque(false);
            buttonPanel.add(backButton);
            mainContainer.add(buttonPanel, BorderLayout.SOUTH);

            add(mainContainer, new GridBagConstraints());
        }
    }


    public static class AppointmentBookingPage3 extends JPanel {
        public AppointmentBookingPage3(Patient patient, Doctor doctor, String reason) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Calendar Selection - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Home");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class AppointmentBookingPage4 extends JPanel {
        public AppointmentBookingPage4(Patient patient, Doctor doctor, String reason, LocalDate date) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Time Slot Selection - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Home");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }

    public static class PatientInfoPage extends JPanel {
        public PatientInfoPage(Patient patient) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel placeholder = new JLabel("Patient Info - Coming Soon", SwingConstants.CENTER);
            placeholder.setFont(new Font("SansSerif", Font.BOLD, 24));
            placeholder.setForeground(Color.WHITE);
            add(placeholder, BorderLayout.CENTER);

            JButton backButton = new JButton("Back to Home");
            backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
            add(backButton, BorderLayout.SOUTH);
        }
    }
}
