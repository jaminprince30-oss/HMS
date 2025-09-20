import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Modern Hospital Management System with improved UI/UX and XAMPP integration
 * Features: Role-based authentication, appointment booking, patient management, doctor scheduling
 */
public class HospitalManagementSystem {
    // Main Frame and Panel Container
    private static JFrame mainFrame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    // User Session
    private static String currentAdminUser;
    private static Doctor currentDoctorUser;

    // Database Manager
    private static final DatabaseManager dbManager = new DatabaseManager();

    // Modern UI Colors
    public static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue-600
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216); // Blue-700
    public static final Color SECONDARY_COLOR = new Color(99, 102, 241); // Indigo-500
    public static final Color SUCCESS_COLOR = new Color(34, 197, 94); // Green-500
    public static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber-500
    public static final Color ERROR_COLOR = new Color(239, 68, 68); // Red-500
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252); // Slate-50
    public static final Color CARD_COLOR = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42); // Slate-900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate-500
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate-200

    public static void main(String[] args) {
        // Set system look and feel for better integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("SALVE Memorial Hospital - Management System");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1400, 900);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setMinimumSize(new Dimension(1200, 800));

            // Set application icon
            try {
                URL iconUrl = new URL("https://images.unsplash.com/photo-1576091160550-2173dba9996a?w=32&h=32&fit=crop");
                BufferedImage icon = ImageIO.read(iconUrl);
                mainFrame.setIconImage(icon);
            } catch (IOException e) {
                System.err.println("Could not load application icon");
            }

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);
            mainPanel.setBackground(BACKGROUND_COLOR);

            // Initialize all pages
            mainPanel.add(new MainPage(), "MainPage");
            mainPanel.add(new LoginPage(), "LoginPage");
            mainPanel.add(new AdminLoginPage(), "AdminLoginPage");
            mainPanel.add(new DoctorLoginPage(), "DoctorLoginPage");
            mainPanel.add(new PatientLoginPage(), "PatientLoginPage");
            mainPanel.add(new AppointmentBookingPage(), "AppointmentBookingPage");

            mainFrame.add(mainPanel);
            mainFrame.setVisible(true);
        });
    }

    public static void showPage(String panelName) {
        // Check if a panel for this name already exists to prevent duplicates
        for (Component comp : mainPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(panelName)) {
                cardLayout.show(mainPanel, panelName);
                mainPanel.revalidate();
                mainPanel.repaint();
                return;
            }
        }

        JPanel panelToAdd = null;
        switch (panelName) {
            case "AdminPage":
                panelToAdd = new AdminDashboard(currentAdminUser);
                break;
            case "DoctorPage":
                panelToAdd = new DoctorDashboard(currentDoctorUser);
                break;
            case "PatientManagementPage":
                panelToAdd = new PatientManagementPage();
                break;
            case "AppointmentManagementPage":
                panelToAdd = new AppointmentManagementPage();
                break;
            case "DoctorManagementPage":
                panelToAdd = new DoctorManagementPage();
                break;
            case "LoginPage":
                panelToAdd = new LoginPage();
                break;
            case "AdminLoginPage":
                panelToAdd = new AdminLoginPage();
                break;
            case "DoctorLoginPage":
                panelToAdd = new DoctorLoginPage();
                break;
            case "PatientLoginPage":
                panelToAdd = new PatientLoginPage();
                break;
            case "AppointmentBookingPage":
                panelToAdd = new AppointmentBookingPage();
                break;
            case "MainPage":    // ← Add this block!
                panelToAdd = new MainPage();
                break;
            default:
                System.err.println("Unknown panel: " + panelName);
                return;
        }
        if (panelToAdd != null) {
            panelToAdd.setName(panelName); // Set the component name for uniqueness
            mainPanel.add(panelToAdd, panelName);
        }

        cardLayout.show(mainPanel, panelName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void showPatientInfoPage(Patient patient) {
        PatientInfoPage page = new PatientInfoPage(patient);
        mainPanel.add(page, "PatientInfoPage");
        cardLayout.show(mainPanel, "PatientInfoPage");
    }

    public static void setCurrentAdminUser(String username) {
        currentAdminUser = username;
    }

    public static void setCurrentDoctorUser(Doctor doctor) {
        currentDoctorUser = doctor;
    }

    public static DatabaseManager getDbManager() {
        return dbManager;
    }

    // Utility method to create styled buttons
    public static JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (bgColor.equals(PRIMARY_COLOR)) {
                    button.setBackground(PRIMARY_HOVER);
                } else {
                    button.setBackground(bgColor.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // Utility method to create styled panels
    public static JPanel createStyledPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }
}

// =================================================================================
// GUI Pages/Panels
// =================================================================================

class MainPage extends JPanel {
    public MainPage() {
        // Use a custom panel with background image
        BackgroundPanel backgroundPanel = new BackgroundPanel("/main/resources/background.png");  // Path relative to src
        backgroundPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());  // MainPage uses BorderLayout

        // Header
        JPanel headerPanel = createHeader();
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel = createMainContent();
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = createFooter();
        backgroundPanel.add(footerPanel, BorderLayout.SOUTH);

        // Add the background panel to MainPage
        add(backgroundPanel, BorderLayout.CENTER);
    }

    // Custom panel for background image
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                if (imagePath.startsWith("http")) {
                    // Load from URL
                    URL imageUrl = new URL(imagePath);
                    backgroundImage = ImageIO.read(imageUrl);
                } else {
                    // Load from resources
                    URL imageUrl = getClass().getResource(imagePath);
                    if (imageUrl != null) {
                        backgroundImage = ImageIO.read(imageUrl);
                    } else {
                        // Fallback to a solid color
                        System.err.println("Background image not found, using solid color background");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading background image: " + e.getMessage());
                // Set a gradient background as fallback
                setBackground(new Color(240, 248, 255)); // Light blue
            }
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            if (backgroundImage != null) {
                // Enable anti-aliasing for smooth scaling
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Scale and draw the image to fit the panel perfectly
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                // Add a subtle overlay for better text readability
                g2d.setColor(new Color(255, 255, 255, 30)); // Semi-transparent white overlay
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // Fallback gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(240, 248, 255),
                        0, getHeight(), new Color(219, 234, 254)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.dispose();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Logo and Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🏥");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("SALVE");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(HospitalManagementSystem.PRIMARY_COLOR);
        JLabel subtitleLabel = new JLabel("Memorial Hospital");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        logoPanel.add(logoIcon);
        logoPanel.add(Box.createHorizontalStrut(10));
        logoPanel.add(titlePanel);

        // Login Button
        JButton loginButton = HospitalManagementSystem.createStyledButton(
                "Staff Login",
                HospitalManagementSystem.PRIMARY_COLOR,
                Color.BLUE
        );
        loginButton.addActionListener(e -> HospitalManagementSystem.showPage("LoginPage"));

        header.add(logoPanel, BorderLayout.WEST);
        header.add(loginButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // Hero Section
        JPanel heroPanel = createHeroSection();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(heroPanel, gbc);

        // Service Cards
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;

        gbc.gridx = 0;
        content.add(createDoctorsOnlineCard(), gbc);

        gbc.gridx = 1;
        content.add(createAppointmentCard(), gbc);

        gbc.gridx = 2;
        content.add(createPatientPortalCard(), gbc);

        return content;
    }

    private JPanel createHeroSection() {
        JPanel hero = HospitalManagementSystem.createStyledPanel();
        hero.setLayout(new BorderLayout());
        hero.setBackground(HospitalManagementSystem.PRIMARY_COLOR);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel heroTitle = new JLabel("Healthcare Made Simple");
        heroTitle.setFont(new Font("Inter", Font.BOLD, 36));
        heroTitle.setForeground(Color.WHITE);

        JLabel heroSubtitle = new JLabel("Experience world-class medical care with our advanced hospital management system");
        heroSubtitle.setFont(new Font("Inter", Font.PLAIN, 16));
        heroSubtitle.setForeground(new Color(219, 234, 254)); // Blue-100

        textPanel.add(heroTitle);
        textPanel.add(heroSubtitle);

        hero.add(textPanel, BorderLayout.CENTER);
        return hero;
    }

    private JPanel createDoctorsOnlineCard() {
        JPanel card = HospitalManagementSystem.createStyledPanel();
        card.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👨‍⚕️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel titleLabel = new JLabel("Doctors Online");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(titleLabel);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        List<Doctor> onlineDoctors = HospitalManagementSystem.getDbManager().getLoggedInDoctors();

        if (onlineDoctors.isEmpty()) {
            JLabel noDocsLabel = new JLabel("No doctors currently online");
            noDocsLabel.setFont(new Font("Inter", Font.PLAIN, 14));
            noDocsLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);
            contentPanel.add(noDocsLabel, BorderLayout.CENTER);
        } else {
            JPanel doctorsList = new JPanel(new GridLayout(0, 1, 0, 10));
            doctorsList.setOpaque(false);
            for (Doctor doctor : onlineDoctors.subList(0, Math.min(3, onlineDoctors.size()))) {
                JPanel doctorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                doctorPanel.setOpaque(false);

                JLabel statusDot = new JLabel("●");
                statusDot.setForeground(HospitalManagementSystem.SUCCESS_COLOR);
                statusDot.setFont(new Font("Inter", Font.BOLD, 16));

                JLabel doctorName = new JLabel("Dr. " + doctor.getName());
                doctorName.setFont(new Font("Inter", Font.BOLD, 14));
                doctorName.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

                JLabel specialty = new JLabel("(" + doctor.getSpecialization() + ")");
                specialty.setFont(new Font("Inter", Font.PLAIN, 12));
                specialty.setForeground(HospitalManagementSystem.TEXT_SECONDARY);

                doctorPanel.add(statusDot);
                doctorPanel.add(doctorName);
                doctorPanel.add(specialty);

                doctorsList.add(doctorPanel);
            }

            contentPanel.add(doctorsList, BorderLayout.CENTER);

            if (onlineDoctors.size() > 3) {
                JLabel moreLabel = new JLabel("+" + (onlineDoctors.size() - 3) + " more doctors available");
                moreLabel.setFont(new Font("Inter", Font.ITALIC, 12));
                moreLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);
                contentPanel.add(moreLabel, BorderLayout.SOUTH);
            }
        }

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAppointmentCard() {
        JPanel card = HospitalManagementSystem.createStyledPanel();
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add click listener
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HospitalManagementSystem.showPage("AppointmentBookingPage");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 250, 252));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("📅");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel titleLabel = new JLabel("Book Appointment");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(titleLabel);

        // Content
        JLabel descLabel = new JLabel("<html>Schedule an appointment with our specialists.<br/>Quick, easy, and secure online booking.</html>");
        descLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        descLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);
        descLabel.setBorder(new EmptyBorder(10, 0, 20, 0));

        JButton bookButton = HospitalManagementSystem.createStyledButton(
                "Get Started →",
                HospitalManagementSystem.PRIMARY_COLOR,
                Color.BLUE
        );
        bookButton.addActionListener(e -> HospitalManagementSystem.showPage("AppointmentBookingPage"));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        card.add(bookButton, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPatientPortalCard() {
        JPanel card = HospitalManagementSystem.createStyledPanel();
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add click listener
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HospitalManagementSystem.showPage("PatientLoginPage");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 250, 252));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel titleLabel = new JLabel("Patient Portal");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(titleLabel);

        // Content
        JLabel descLabel = new JLabel("<html>Access your medical records, appointments,<br/>and consultation history.</html>");
        descLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        descLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);
        descLabel.setBorder(new EmptyBorder(10, 0, 20, 0));

        JButton portalButton = HospitalManagementSystem.createStyledButton(
                "Access Portal →",
                HospitalManagementSystem.SECONDARY_COLOR,
                Color.BLUE
        );
        portalButton.addActionListener(e -> HospitalManagementSystem.showPage("PatientLoginPage"));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        card.add(portalButton, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(HospitalManagementSystem.TEXT_PRIMARY);
        footer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel footerText = new JLabel("© 2024 SALVE Memorial Hospital. All rights reserved.");
        footerText.setFont(new Font("Inter", Font.PLAIN, 12));
        footerText.setForeground(new Color(148, 163, 184)); // Slate-400

        footer.add(footerText, BorderLayout.CENTER);
        return footer;
    }
}

class LoginPage extends JPanel {
    public LoginPage() {
        setLayout(new GridBagLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        JPanel formPanel = HospitalManagementSystem.createStyledPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(400, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Title
        JLabel titleLabel = new JLabel("Select Your Role");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // Role buttons
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        JButton adminButton = HospitalManagementSystem.createStyledButton(
                "👨‍💼 Administrator",
                HospitalManagementSystem.PRIMARY_COLOR,
                Color.BLUE
        );
        adminButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminLoginPage"));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(adminButton, gbc);

        JButton doctorButton = HospitalManagementSystem.createStyledButton(
                "👨‍⚕️ Doctor",
                HospitalManagementSystem.SUCCESS_COLOR,
                Color.BLUE
        );
        doctorButton.addActionListener(e -> HospitalManagementSystem.showPage("DoctorLoginPage"));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(doctorButton, gbc);

        // Back button
        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Home",
                HospitalManagementSystem.TEXT_SECONDARY,
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(backButton, gbc);

        add(formPanel, new GridBagConstraints());
    }
}

class AdminLoginPage extends JPanel {
    private final JTextField userField;
    private final JPasswordField passField;

    public AdminLoginPage() {
        setLayout(new GridBagLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        JPanel formPanel = HospitalManagementSystem.createStyledPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(400, 350));
        formPanel.setOpaque(true);  // Ensure panel is opaque

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel title = new JLabel("Administrator Login");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        // Form fields
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 14));
        userLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userField = new JTextField(15);
        userField.setFont(new Font("Inter", Font.PLAIN, 14));
        userField.setPreferredSize(new Dimension(200, 40));  // Increased height
        userField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        userField.setEnabled(true);
        userField.setEditable(true);
        formPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 14));
        passLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passField = new JPasswordField(15);
        passField.setFont(new Font("Inter", Font.PLAIN, 10));
        passField.setPreferredSize(new Dimension(200, 40));  // Increased height
        passField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        passField.setEnabled(true);
        passField.setEditable(true);
        formPanel.add(passField, gbc);

        // Buttons
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton loginButton = HospitalManagementSystem.createStyledButton(
                "Login",
                HospitalManagementSystem.PRIMARY_COLOR,
                Color.BLUE
        );
        loginButton.addActionListener(e -> performLogin());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back",
                HospitalManagementSystem.TEXT_SECONDARY,
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("LoginPage"));
        gbc.gridy = 4;
        formPanel.add(backButton, gbc);

        // Demo credentials info
        JLabel demoInfo = new JLabel("<html><center><small>Demo: admin / admin123</small></center></html>");
        demoInfo.setFont(new Font("Inter", Font.ITALIC, 12));
        demoInfo.setForeground(HospitalManagementSystem.TEXT_SECONDARY);
        gbc.gridy = 5;
        formPanel.add(demoInfo, gbc);

        add(formPanel, new GridBagConstraints());
    }

    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String role = HospitalManagementSystem.getDbManager().validateUser(username, password);
        if ("Admin".equals(role)) {
            HospitalManagementSystem.setCurrentAdminUser(username);
            HospitalManagementSystem.showPage("AdminPage");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class DoctorLoginPage extends JPanel {
    private final JTextField userField;
    private final JPasswordField passField;

    public DoctorLoginPage() {
        setLayout(new GridBagLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        JPanel formPanel = HospitalManagementSystem.createStyledPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(400, 350));
        formPanel.setOpaque(true);  // Ensure panel is opaque

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel title = new JLabel("Doctor Login");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        // Form fields
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 14));
        userLabel.setPreferredSize(new Dimension(200, 30));
        userLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1;gbc.weightx = 1.0;
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userField = new JTextField(15);
        userField.setFont(new Font("Inter", Font.PLAIN, 14));
        userField.setPreferredSize(new Dimension(200, 40));  // Increased height
        userField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        userField.setEnabled(true);
        userField.setEditable(true);
        formPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 14));
        passLabel.setPreferredSize(new Dimension(200, 30));
        passLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;gbc.weightx = 1.0;
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passField = new JPasswordField(15);
        passField.setFont(new Font("Inter", Font.PLAIN, 10));
        passField.setPreferredSize(new Dimension(200, 40));  // Increased height
        passField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        passField.setEnabled(true);
        passField.setEditable(true);
        formPanel.add(passField, gbc);

        // Buttons
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton loginButton = HospitalManagementSystem.createStyledButton(
                "Login",
                HospitalManagementSystem.SUCCESS_COLOR,
                Color.BLUE
        );
        loginButton.addActionListener(e -> performLogin());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back",
                HospitalManagementSystem.TEXT_SECONDARY,
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("LoginPage"));
        gbc.gridy = 4;
        formPanel.add(backButton, gbc);

        // Demo credentials info
        JLabel demoInfo = new JLabel("<html><center><small>Demo: dr.smith / doc123</small></center></html>");
        demoInfo.setFont(new Font("Inter", Font.ITALIC, 12));
        demoInfo.setForeground(HospitalManagementSystem.TEXT_SECONDARY);
        gbc.gridy = 5;
        formPanel.add(demoInfo, gbc);

        add(formPanel, new GridBagConstraints());
    }

    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String role = HospitalManagementSystem.getDbManager().validateUser(username, password);
        if ("Doctor".equals(role)) {
            Doctor doctor = HospitalManagementSystem.getDbManager().getDoctorByUsername(username);
            if (doctor != null) {
                HospitalManagementSystem.getDbManager().recordDoctorLogin(doctor.getId());
                HospitalManagementSystem.setCurrentDoctorUser(doctor);
                HospitalManagementSystem.showPage("DoctorPage");
            } else {
                JOptionPane.showMessageDialog(this, "Doctor profile not found.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid doctor credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class PatientLoginPage extends JPanel {
    private final JTextField idField;
    private final JTextField nameField;

    public PatientLoginPage() {
        setLayout(new GridBagLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        JPanel formPanel = HospitalManagementSystem.createStyledPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(400, 350));
        formPanel.setOpaque(true);  // Ensure panel is opaque

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel title = new JLabel("Patient Portal Login");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        // Form fields
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;

        JLabel idLabel = new JLabel("Patient ID:");
        idLabel.setFont(new Font("Inter", Font.BOLD, 14));
        idLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(idLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        idField = new JTextField(15);
        idField.setFont(new Font("Inter", Font.PLAIN, 14));
        idField.setPreferredSize(new Dimension(200, 40));  // Increased height
        idField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        idField.setEnabled(true);
        idField.setEditable(true);
        formPanel.add(idField, gbc);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Inter", Font.BOLD, 14));
        nameLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(15);
        nameField.setFont(new Font("Inter", Font.PLAIN, 14));
        nameField.setPreferredSize(new Dimension(200, 40));  // Increased height
        nameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        nameField.setEnabled(true);
        nameField.setEditable(true);
        formPanel.add(nameField, gbc);

        // Buttons
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton loginButton = HospitalManagementSystem.createStyledButton(
                "Access Portal",
                HospitalManagementSystem.SECONDARY_COLOR,
                Color.BLUE
        );
        loginButton.addActionListener(e -> performLogin());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Home",
                HospitalManagementSystem.TEXT_SECONDARY,
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));
        gbc.gridy = 4;
        formPanel.add(backButton, gbc);

        add(formPanel, new GridBagConstraints());
    }

    private void performLogin() {
        String idText = idField.getText().trim();
        String name = nameField.getText().trim();

        if (idText.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            Patient patient = HospitalManagementSystem.getDbManager().getPatientByNameAndId(name, id);
            if (patient != null) {
                HospitalManagementSystem.showPatientInfoPage(patient);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid patient details.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Patient ID must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class AdminDashboard extends JPanel {
    public AdminDashboard(String adminUsername) {
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader(adminUsername);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel = createMainContent();
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader(String adminUsername) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome back, " + adminUsername + "!");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        navPanel.setOpaque(false);

        JButton patientsBtn = createNavButton("👥 Patients", e -> HospitalManagementSystem.showPage("PatientManagementPage"));
        patientsBtn.setForeground(Color.BLUE);
        JButton appointmentsBtn = createNavButton("📅 Appointments", e -> HospitalManagementSystem.showPage("AppointmentManagementPage"));
        appointmentsBtn.setForeground(Color.BLUE);
        JButton doctorsBtn = createNavButton("👨‍⚕️ Doctors", e -> HospitalManagementSystem.showPage("DoctorManagementPage"));
        doctorsBtn.setForeground(Color.BLUE);
        navPanel.add(patientsBtn);
        navPanel.add(appointmentsBtn);
        navPanel.add(doctorsBtn);

        // Logout button
        JButton logoutBtn = createNavButton("🚪 Logout", e -> HospitalManagementSystem.showPage("MainPage"));
        logoutBtn.setForeground(Color.BLUE);
        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(navPanel, BorderLayout.CENTER);
        header.add(logoutBtn, BorderLayout.EAST);


        return header;
    }

    private JButton createNavButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(new Color(29, 78, 216)); // Blue-700
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(30, 64, 175)); // Blue-800
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(29, 78, 216)); // Blue-700
            }
        });

        return button;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridLayout(2, 2, 30, 30));
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Statistics cards
        content.add(createStatsCard("Total Patients", String.valueOf(HospitalManagementSystem.getDbManager().getAllPatients().size()), "👥", HospitalManagementSystem.PRIMARY_COLOR));
        content.add(createStatsCard("Online Doctors", String.valueOf(HospitalManagementSystem.getDbManager().getLoggedInDoctors().size()), "👨‍⚕️", HospitalManagementSystem.SUCCESS_COLOR));
        content.add(createStatsCard("Today's Appointments", String.valueOf(HospitalManagementSystem.getDbManager().getTodayAppointmentsCount()), "📅", HospitalManagementSystem.WARNING_COLOR));
        content.add(createStatsCard("Total Doctors", String.valueOf(HospitalManagementSystem.getDbManager().getAllDoctors().size()), "⚕️", HospitalManagementSystem.SECONDARY_COLOR));

        return content;
    }

    private JPanel createStatsCard(String title, String value, String icon, Color color) {
        JPanel card = HospitalManagementSystem.createStyledPanel();
        card.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 36));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        titleLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);

        textPanel.add(valueLabel);
        textPanel.add(titleLabel);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(textPanel);

        card.add(headerPanel, BorderLayout.CENTER);
        return card;
    }
}

class DoctorDashboard extends JPanel {
    private Doctor doctor;

    public DoctorDashboard(Doctor doctor) {
        this.doctor = doctor;
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel = createMainContent();
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.SUCCESS_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, Dr. " + doctor.getName() + "!");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel specialtyLabel = new JLabel(doctor.getSpecialization() + " Department");
        specialtyLabel.setFont(new Font("Inter", Font.BOLD, 14));
        specialtyLabel.setForeground(new Color(220, 252, 231)); // Green-100

        JPanel welcomePanel = new JPanel(new GridLayout(2, 1));
        welcomePanel.setOpaque(false);
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(specialtyLabel);

        // Logout button
        JButton logoutBtn = HospitalManagementSystem.createStyledButton(
                "🚪 Finish Duty & Logout",
                new Color(220, 38, 38), // Red-600
                Color.BLUE
        );
        logoutBtn.addActionListener(e -> performLogout());

        header.add(welcomePanel, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridLayout(1, 3, 30, 30));
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        // My appointments today
        int todayAppointments = HospitalManagementSystem.getDbManager().getDoctorTodayAppointments(doctor.getId());
        content.add(createStatsCard("Today's Appointments", String.valueOf(todayAppointments), "📅", HospitalManagementSystem.PRIMARY_COLOR));

        // My total patients
        int totalPatients = HospitalManagementSystem.getDbManager().getDoctorPatientCount(doctor.getId());
        content.add(createStatsCard("My Patients", String.valueOf(totalPatients), "👥", HospitalManagementSystem.SECONDARY_COLOR));

        // Status
        content.add(createStatsCard("Status", "Online", "✅", HospitalManagementSystem.SUCCESS_COLOR));

        return content;
    }

    private JPanel createStatsCard(String title, String value, String icon, Color color) {
        JPanel card = HospitalManagementSystem.createStyledPanel();
        card.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 36));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        titleLabel.setForeground(HospitalManagementSystem.TEXT_SECONDARY);

        textPanel.add(valueLabel);
        textPanel.add(titleLabel);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(textPanel);

        card.add(headerPanel, BorderLayout.CENTER);
        return card;
    }

    private void performLogout() {
        HospitalManagementSystem.getDbManager().recordDoctorLogout(doctor.getId());
        JOptionPane.showMessageDialog(this, "You have been successfully logged out.", "Logout Successful", JOptionPane.INFORMATION_MESSAGE);
        HospitalManagementSystem.showPage("MainPage");
    }
}

class AppointmentBookingPage extends JPanel {
    private JTextField nameField;
    private JTextField phoneField;
    private JTextArea symptomsArea;
    private JComboBox<Doctor> doctorComboBox;

    public AppointmentBookingPage() {
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = createForm();
        add(formPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Book an Appointment");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLUE);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Home",
                new Color(29, 78, 216), // Blue-700
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createForm() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        JPanel formPanel = HospitalManagementSystem.createStyledPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(600, 500));
        formPanel.setOpaque(true);  // Ensure panel is opaque

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Patient Name
        JLabel nameLabel = new JLabel("Patient Full Name:");
        nameLabel.setFont(new Font("Inter", Font.BOLD, 14));
        nameLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(25);
        nameField.setFont(new Font("Inter", Font.PLAIN, 14));
        nameField.setPreferredSize(new Dimension(300, 40));  // Adjusted for better usability
        nameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        nameField.setEnabled(true);
        nameField.setEditable(true);
        formPanel.add(nameField, gbc);

        // Phone Number
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Inter", Font.BOLD, 14));
        phoneLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(phoneLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        phoneField = new JTextField(25);
        phoneField.setFont(new Font("Inter", Font.PLAIN, 14));
        phoneField.setPreferredSize(new Dimension(300, 40));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        phoneField.setEnabled(true);
        phoneField.setEditable(true);
        formPanel.add(phoneField, gbc);

        // Symptoms
        JLabel symptomsLabel = new JLabel("Describe Symptoms:");
        symptomsLabel.setFont(new Font("Inter", Font.BOLD, 14));
        symptomsLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(symptomsLabel, gbc);

        symptomsArea = new JTextArea(5, 25);
        symptomsArea.setFont(new Font("Inter", Font.PLAIN, 14));
        symptomsArea.setLineWrap(true);
        symptomsArea.setWrapStyleWord(true);
        symptomsArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JScrollPane symptomsScroll = new JScrollPane(symptomsArea);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(symptomsScroll, gbc);

        // Doctor Selection
        JLabel doctorLabel = new JLabel("Select Doctor:");
        doctorLabel.setFont(new Font("Inter", Font.BOLD, 14));
        doctorLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(doctorLabel, gbc);

        doctorComboBox = new JComboBox<>(HospitalManagementSystem.getDbManager().getAllDoctors().toArray(new Doctor[0]));
        doctorComboBox.setFont(new Font("Inter", Font.PLAIN, 14));
        doctorComboBox.setPreferredSize(new Dimension(300, 40));
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(doctorComboBox, gbc);

        // Submit Button
        JButton submitButton = HospitalManagementSystem.createStyledButton(
                "Book Appointment",
                HospitalManagementSystem.SUCCESS_COLOR,
                Color.BLUE
        );
        submitButton.addActionListener(e -> bookAppointment());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(submitButton, gbc);

        container.add(formPanel, new GridBagConstraints());
        return container;
    }

    private void bookAppointment() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String symptoms = symptomsArea.getText().trim();
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();

        if (name.isEmpty() || phone.isEmpty() || symptoms.isEmpty() || selectedDoctor == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create patient
        Patient patient = new Patient(0, name, phone);
        int patientId = HospitalManagementSystem.getDbManager().addPatient(patient);

        if (patientId > 0) {
            // Record consultation (appointment)
            LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0); // Default to tomorrow 10 AM
            String consultationId = HospitalManagementSystem.getDbManager().recordConsultation(
                    patientId, selectedDoctor.getId(), symptoms, appointmentTime, null
            );

            if (consultationId != null) {
                JOptionPane.showMessageDialog(this,
                        "Appointment booked successfully!\n" +
                                "Patient ID: " + patientId + "\n" +
                                "Doctor: Dr. " + selectedDoctor.getName() + "\n" +
                                "Scheduled for: " + appointmentTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
                        "Booking Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);

                // Clear form
                nameField.setText("");
                phoneField.setText("");
                symptomsArea.setText("");
                doctorComboBox.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to book appointment. Please try again.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create patient record. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class PatientManagementPage extends JPanel {
    private JTable patientsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public PatientManagementPage() {
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContent();
        add(contentPanel, BorderLayout.CENTER);

        loadPatients();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Patient Management");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLUE);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Dashboard",
                new Color(29, 78, 216), // Blue-700
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Inter", Font.BOLD, 14));
        searchLabel.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Inter", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JButton searchButton = HospitalManagementSystem.createStyledButton(
                "Search",
                HospitalManagementSystem.PRIMARY_COLOR,
                Color.BLUE
        );
        searchButton.addActionListener(e -> searchPatients());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        content.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Name", "Phone", "Registration Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientsTable = new JTable(tableModel);
        patientsTable.setFont(new Font("Inter", Font.PLAIN, 14));
        patientsTable.setRowHeight(30);
        patientsTable.setSelectionBackground(new Color(219, 234, 254)); // Blue-100
        patientsTable.setGridColor(HospitalManagementSystem.BORDER_COLOR);

        // Style table header
        JTableHeader header = patientsTable.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        header.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        scrollPane.setBorder(new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1));
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadPatients() {
        tableModel.setRowCount(0);
        List<Patient> patients = HospitalManagementSystem.getDbManager().getAllPatients();

        for (Patient patient : patients) {
            Object[] row = {
                    patient.getId(),
                    patient.getName(),
                    patient.getContactNumber(),
                    "N/A" // Registration date not tracked in current model
            };
            tableModel.addRow(row);
        }
    }

    private void searchPatients() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadPatients();
            return;
        }

        tableModel.setRowCount(0);
        List<Patient> patients = HospitalManagementSystem.getDbManager().getPatientsByName(searchTerm);
        for (Patient patient : patients) {
            Object[] row = {
                    patient.getId(),
                    patient.getName(),
                    patient.getContactNumber(),
                    "N/A"
            };
            tableModel.addRow(row);
        }
    }
}

class AppointmentManagementPage extends JPanel {
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;

    public AppointmentManagementPage() {
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContent();
        add(contentPanel, BorderLayout.CENTER);

        loadAppointments();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Appointment Management");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLUE);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Dashboard",
                new Color(29, 78, 216), // Blue-700
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table
        String[] columnNames = {"Patient ID", "Patient Name", "Doctor", "Date & Time", "Reason", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentsTable = new JTable(tableModel);
        appointmentsTable.setFont(new Font("Inter", Font.PLAIN, 14));
        appointmentsTable.setRowHeight(35);
        appointmentsTable.setSelectionBackground(new Color(219, 234, 254)); // Blue-100
        appointmentsTable.setGridColor(HospitalManagementSystem.BORDER_COLOR);

        // Style table header
        JTableHeader header = appointmentsTable.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        header.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1));
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        List<Consultation> appointments = HospitalManagementSystem.getDbManager().getAllUpcomingAppointments();

        for (Consultation appointment : appointments) {
            Patient patient = HospitalManagementSystem.getDbManager().getPatientById(appointment.getPatientId());
            Doctor doctor = HospitalManagementSystem.getDbManager().getDoctorById(appointment.getDoctorId());

            Object[] row = {
                    appointment.getPatientId(),
                    patient != null ? patient.getName() : "Unknown",
                    doctor != null ? "Dr. " + doctor.getName() : "Unknown",
                    appointment.getConsultationDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")),
                    appointment.getConsultingReason(),
                    "Scheduled"
            };
            tableModel.addRow(row);
        }
    }
}

class DoctorManagementPage extends JPanel {
    private JTable doctorsTable;
    private DefaultTableModel tableModel;

    public DoctorManagementPage() {
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContent();
        add(contentPanel, BorderLayout.CENTER);

        loadDoctors();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Doctor Management");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLUE);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Dashboard",
                new Color(29, 78, 216), // Blue-700
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("AdminPage"));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table
        String[] columnNames = {"ID", "Name", "Specialization", "Username", "Status", "Last Activity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorsTable = new JTable(tableModel);
        doctorsTable.setFont(new Font("Inter", Font.PLAIN, 14));
        doctorsTable.setRowHeight(35);
        doctorsTable.setSelectionBackground(new Color(219, 234, 254)); // Blue-100
        doctorsTable.setGridColor(HospitalManagementSystem.BORDER_COLOR);

        // Style table header
        JTableHeader header = doctorsTable.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        header.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        scrollPane.setBorder(new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1));
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadDoctors() {
        tableModel.setRowCount(0);
        List<Doctor> doctors = HospitalManagementSystem.getDbManager().getAllDoctors();

        for (Doctor doctor : doctors) {
            String[] statusInfo = HospitalManagementSystem.getDbManager().getDoctorCurrentLoginStatus(doctor.getId());

            Object[] row = {
                    doctor.getId().substring(0, 8) + "...", // Shortened ID for display
                    doctor.getName(),
                    doctor.getSpecialization(),
                    doctor.getUsername(),
                    statusInfo[0],
                    statusInfo[1]
            };
            tableModel.addRow(row);
        }
    }
}

class PatientInfoPage extends JPanel {
    public PatientInfoPage(Patient patient) {
        setLayout(new BorderLayout());
        setBackground(HospitalManagementSystem.BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = createHeader(patient);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContent(patient);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader(Patient patient) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HospitalManagementSystem.SECONDARY_COLOR);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Patient Information - " + patient.getName());
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JButton backButton = HospitalManagementSystem.createStyledButton(
                "← Back to Home",
                new Color(79, 70, 229), // Indigo-600
                Color.BLUE
        );
        backButton.addActionListener(e -> HospitalManagementSystem.showPage("MainPage"));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent(Patient patient) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(HospitalManagementSystem.BACKGROUND_COLOR);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Patient details card
        JPanel detailsCard = HospitalManagementSystem.createStyledPanel();
        detailsCard.setLayout(new GridLayout(3, 2, 10, 10));

        addDetailRow(detailsCard, "Patient ID:", String.valueOf(patient.getId()));
        addDetailRow(detailsCard, "Full Name:", patient.getName());
        addDetailRow(detailsCard, "Contact Number:", patient.getContactNumber());

        content.add(detailsCard, BorderLayout.NORTH);

        // Consultation history
        JPanel historyPanel = createConsultationHistory(patient);
        content.add(historyPanel, BorderLayout.CENTER);

        return content;
    }

    private void addDetailRow(JPanel parent, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Inter", Font.BOLD, 14));
        labelComp.setForeground(HospitalManagementSystem.TEXT_PRIMARY);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Inter", Font.PLAIN, 14));
        valueComp.setForeground(HospitalManagementSystem.TEXT_SECONDARY);

        parent.add(labelComp);
        parent.add(valueComp);
    }

    private JPanel createConsultationHistory(Patient patient) {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setOpaque(false);
        historyPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel historyTitle = new JLabel("Consultation History");
        historyTitle.setFont(new Font("Inter", Font.BOLD, 18));
        historyTitle.setForeground(HospitalManagementSystem.TEXT_PRIMARY);
        historyTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Inter", Font.PLAIN, 14));
        historyArea.setBackground(Color.WHITE);
        historyArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Load consultation history
        StringBuilder historyText = new StringBuilder();
        List<Consultation> consultations = HospitalManagementSystem.getDbManager().getConsultationsForPatient(patient.getId());

        if (consultations.isEmpty()) {
            historyText.append("No consultation history found.");
        } else {
            for (Consultation consultation : consultations) {
                Doctor doctor = HospitalManagementSystem.getDbManager().getDoctorById(consultation.getDoctorId());

                historyText.append("Date: ").append(consultation.getConsultationDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))).append("\n");
                historyText.append("Doctor: ").append(doctor != null ? "Dr. " + doctor.getName() : "Unknown").append("\n");
                historyText.append("Specialization: ").append(doctor != null ? doctor.getSpecialization() : "Unknown").append("\n");
                historyText.append("Reason: ").append(consultation.getConsultingReason()).append("\n");

                if (consultation.getNextConsultingDate() != null) {
                    historyText.append("Next Appointment: ").append(consultation.getNextConsultingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
                }

                // Load prescriptions
                List<Prescription> prescriptions = HospitalManagementSystem.getDbManager().getPrescriptionsForConsultation(consultation.getId());
                if (!prescriptions.isEmpty()) {
                    historyText.append("Prescriptions:\n");
                    for (Prescription prescription : prescriptions) {
                        historyText.append("  • ").append(prescription.getMedicineName()).append(" - ").append(prescription.getDosage()).append("\n");
                    }
                }

                historyText.append("\n" + "-".repeat(50) + "\n\n");
            }
        }

        historyArea.setText(historyText.toString());
        historyArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(new LineBorder(HospitalManagementSystem.BORDER_COLOR, 1));
        scrollPane.setPreferredSize(new Dimension(0, 300));

        historyPanel.add(historyTitle, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        return historyPanel;
    }
}

// =================================================================================
// Data Models
// =================================================================================

class Doctor {
    private String id;
    private String name;
    private String specialization;
    private String username;

    public Doctor(String id, String name, String specialization, String username) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.username = username;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getUsername() { return username; }

    @Override
    public String toString() {
        return "Dr. " + name + " (" + specialization + ")";
    }
}

class Patient {
    private int id;
    private String name;
    private String contactNumber;

    public Patient(int id, String name, String contactNumber) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getContactNumber() { return contactNumber; }
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Patient ID: " + id + ", Name: " + name;
    }
}

class Consultation {
    private String id;
    private int patientId;
    private String doctorId;
    private String consultingReason;
    private LocalDateTime consultationDateTime;
    private LocalDate nextConsultingDate;

    public Consultation(String id, int patientId, String doctorId, LocalDateTime consultationDateTime,
                        String consultingReason, LocalDate nextConsultingDate) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.consultationDateTime = consultationDateTime;
        this.consultingReason = consultingReason;
        this.nextConsultingDate = nextConsultingDate;
    }

    // Getters
    public String getId() { return id; }
    public int getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getConsultationDateTime() { return consultationDateTime; }
    public String getConsultingReason() { return consultingReason; }
    public LocalDate getNextConsultingDate() { return nextConsultingDate; }
}

class Prescription {
    private int id;
    private String consultationId;
    private String medicineName;
    private String dosage;

    public Prescription(int id, String consultationId, String medicineName, String dosage) {
        this.id = id;
        this.consultationId = consultationId;
        this.medicineName = medicineName;
        this.dosage = dosage;
    }

    // Getters
    public int getId() { return id; }
    public String getConsultationId() { return consultationId; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
}

// =================================================================================
// Database Manager (XAMPP/MySQL Integration)
// =================================================================================

class DatabaseManager {
    // Update these with your XAMPP MySQL details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Default XAMPP MySQL user
    private static final String PASSWORD = "root"; // Default XAMPP MySQL password (empty) // Default XAMPP MySQL password (empty)

    public DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create database if it doesn't exist
            createDatabase();

            // Create tables
            createTables();

            // Add sample data
            addSampleData();

            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database initialization failed: " + e.getMessage() +
                            "\n\nPlease ensure XAMPP is running and MySQL service is started.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    private void createDatabase() throws SQLException {
        String createDbUrl = "jdbc:mysql://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(createDbUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS hospital_management");
            System.out.println("Database 'hospital_management' created or already exists.");
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    role ENUM('Admin', 'Doctor') NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Doctors table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doctors (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    specialization VARCHAR(100) NOT NULL,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                )
            """);

            // Patients table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    contact_number VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Consultations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS consultations (
                    id VARCHAR(36) PRIMARY KEY,
                    patient_id INT NOT NULL,
                    doctor_id VARCHAR(36) NOT NULL,
                    consultation_datetime DATETIME NOT NULL,
                    consulting_reason TEXT NOT NULL,
                    next_consulting_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
                )
            """);

            // Prescriptions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prescriptions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    consultation_id VARCHAR(36) NOT NULL,
                    medicine_name VARCHAR(100) NOT NULL,
                    dosage VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (consultation_id) REFERENCES consultations(id) ON DELETE CASCADE
                )
            """);

            // Doctor login status table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doctor_login_status (
                    id VARCHAR(36) PRIMARY KEY,
                    doctor_id VARCHAR(36) NOT NULL,
                    login_time TIMESTAMP NOT NULL,
                    logout_time TIMESTAMP NULL,
                    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
                )
            """);

            // Doctor availability table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doctor_availability (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    doctor_id VARCHAR(36) NOT NULL,
                    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
                    start_time TIME NOT NULL,
                    end_time TIME NOT NULL,
                    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
                )
            """);

            System.out.println("All tables created successfully!");
        }
    }

    private void addSampleData() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Check if sample data already exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Sample data already exists.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add sample users
        addUser("admin", "admin123", "Admin");
        addUser("dr.smith", "doc123", "Doctor");
        addUser("dr.johnson", "doc123", "Doctor");
        addUser("dr.garcia", "doc123", "Doctor");

        // Add sample doctors
        String doctorId1 = UUID.randomUUID().toString();
        String doctorId2 = UUID.randomUUID().toString();
        String doctorId3 = UUID.randomUUID().toString();

        addDoctor(new Doctor(doctorId1, "Alice Smith", "Cardiology", "dr.smith"));
        addDoctor(new Doctor(doctorId2, "Bob Johnson", "Pediatrics", "dr.johnson"));
        addDoctor(new Doctor(doctorId3, "Maria Garcia", "Neurology", "dr.garcia"));

        // Add doctor availability
        addDoctorAvailability(doctorId1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        addDoctorAvailability(doctorId1, DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        addDoctorAvailability(doctorId1, DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));

        addDoctorAvailability(doctorId2, DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));
        addDoctorAvailability(doctorId2, DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));

        addDoctorAvailability(doctorId3, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(18, 0));
        addDoctorAvailability(doctorId3, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(18, 0));
        addDoctorAvailability(doctorId3, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(18, 0));

        // Add sample patient and consultation
        Patient samplePatient = new Patient(0, "John Doe", "555-0123");
        int patientId = addPatient(samplePatient);

        if (patientId > 0) {
            String consultationId = recordConsultation(
                    patientId,
                    doctorId1,
                    "Regular checkup and chest pain evaluation",
                    LocalDateTime.now().minusDays(7),
                    LocalDate.now().plusDays(30)
            );

            if (consultationId != null) {
                addPrescription(consultationId, "Aspirin", "81mg daily");
                addPrescription(consultationId, "Lisinopril", "10mg daily");
            }
        }

        // Log in Dr. Smith by default
        recordDoctorLogin(doctorId1);

        System.out.println("Sample data added successfully!");
    }

    // User Management
    public void addUser(String username, String password, String role) {
        String sql = "INSERT IGNORE INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, hash the password
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String validateUser(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, hash and compare
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Doctor Management
    public void addDoctor(Doctor doctor) {
        String sql = "INSERT IGNORE INTO doctors (id, name, specialization, username) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getId());
            pstmt.setString(2, doctor.getName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Doctor getDoctorByUsername(String username) {
        String sql = "SELECT * FROM doctors WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Doctor(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("username")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Doctor getDoctorById(String doctorId) {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Doctor(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("username")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    public List<Doctor> getLoggedInDoctors() {
        List<Doctor> loggedInDoctors = new ArrayList<>();
        String sql = """
            SELECT d.* FROM doctors d 
            JOIN doctor_login_status dls ON d.id = dls.doctor_id 
            WHERE dls.logout_time IS NULL
            ORDER BY d.name
        """;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                loggedInDoctors.add(new Doctor(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loggedInDoctors;
    }

    // Doctor Login/Logout Management
    public void recordDoctorLogin(String doctorId) {
        // First, logout any existing session
        recordDoctorLogout(doctorId);

        String sql = "INSERT INTO doctor_login_status (id, doctor_id, login_time) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, doctorId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordDoctorLogout(String doctorId) {
        String sql = "UPDATE doctor_login_status SET logout_time = ? WHERE doctor_id = ? AND logout_time IS NULL";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, doctorId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] getDoctorCurrentLoginStatus(String doctorId) {
        String sql = """
            SELECT login_time, logout_time 
            FROM doctor_login_status 
            WHERE doctor_id = ? 
            ORDER BY login_time DESC 
            LIMIT 1
        """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp logoutTime = rs.getTimestamp("logout_time");
                Timestamp loginTime = rs.getTimestamp("login_time");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

                if (logoutTime == null) {
                    return new String[]{"Online", loginTime.toLocalDateTime().format(formatter)};
                } else {
                    return new String[]{"Offline", logoutTime.toLocalDateTime().format(formatter)};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{"Unknown", "N/A"};
    }

    // Doctor Availability Management
    public void addDoctorAvailability(String doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        String sql = "INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            pstmt.setString(2, dayOfWeek.toString());
            pstmt.setTime(3, Time.valueOf(startTime));
            pstmt.setTime(4, Time.valueOf(endTime));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DayOfWeek> getDoctorAvailableDays(String doctorId) {
        List<DayOfWeek> days = new ArrayList<>();
        String sql = "SELECT DISTINCT day_of_week FROM doctor_availability WHERE doctor_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    public List<LocalTime> getAvailableTimeSlots(String doctorId, LocalDate date) {
        List<LocalTime> availableSlots = new ArrayList<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Get doctor's availability for the day
        String availabilitySql = "SELECT start_time, end_time FROM doctor_availability WHERE doctor_id = ? AND day_of_week = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(availabilitySql)) {
            pstmt.setString(1, doctorId);
            pstmt.setString(2, dayOfWeek.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalTime startTime = rs.getTime("start_time").toLocalTime();
                LocalTime endTime = rs.getTime("end_time").toLocalTime();

                // Generate 30-minute slots
                LocalTime currentTime = startTime;
                while (currentTime.isBefore(endTime)) {
                    availableSlots.add(currentTime);
                    currentTime = currentTime.plusMinutes(30);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Remove booked slots
        String bookedSql = "SELECT consultation_datetime FROM consultations WHERE doctor_id = ? AND DATE(consultation_datetime) = ?";
        Set<LocalTime> bookedSlots = new HashSet<>();
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(bookedSql)) {
            pstmt.setString(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LocalTime bookedTime = rs.getTimestamp("consultation_datetime").toLocalDateTime().toLocalTime();
                bookedSlots.add(bookedTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        availableSlots.removeAll(bookedSlots);
        return availableSlots;
    }

    // Patient Management
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

    public Patient getPatientById(int patientId) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_number")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByNameAndId(String name, int id) {
        String sql = "SELECT * FROM patients WHERE id = ? AND LOWER(name) = LOWER(?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_number")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_number")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public List<Patient> getPatientsByName(String name) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE LOWER(name) LIKE LOWER(?) ORDER BY name";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_number")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public List<Patient> getPatientsByDoctor(String doctorId) {
        List<Patient> patients = new ArrayList<>();
        String sql = """
            SELECT DISTINCT p.* FROM patients p 
            JOIN consultations c ON p.id = c.patient_id 
            WHERE c.doctor_id = ? 
            ORDER BY p.name
        """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_number")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    // Consultation Management
    public String recordConsultation(int patientId, String doctorId, String reason, LocalDateTime dateTime, LocalDate nextDate) {
        String consultationId = UUID.randomUUID().toString();
        String sql = "INSERT INTO consultations (id, patient_id, doctor_id, consultation_datetime, consulting_reason, next_consulting_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, consultationId);
            pstmt.setInt(2, patientId);
            pstmt.setString(3, doctorId);
            pstmt.setTimestamp(4, Timestamp.valueOf(dateTime));
            pstmt.setString(5, reason);
            pstmt.setDate(6, nextDate != null ? Date.valueOf(nextDate) : null);
            pstmt.executeUpdate();
            return consultationId;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Consultation> getConsultationsForPatient(int patientId) {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultations WHERE patient_id = ? ORDER BY consultation_datetime DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                consultations.add(new Consultation(
                        rs.getString("id"),
                        rs.getInt("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("consultation_datetime").toLocalDateTime(),
                        rs.getString("consulting_reason"),
                        rs.getDate("next_consulting_date") != null ? rs.getDate("next_consulting_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return consultations;
    }

    public List<Consultation> getAllUpcomingAppointments() {
        List<Consultation> appointments = new ArrayList<>();
        String sql = "SELECT * FROM consultations WHERE consultation_datetime >= ? ORDER BY consultation_datetime ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Consultation(
                        rs.getString("id"),
                        rs.getInt("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("consultation_datetime").toLocalDateTime(),
                        rs.getString("consulting_reason"),
                        rs.getDate("next_consulting_date") != null ? rs.getDate("next_consulting_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Consultation> getUpcomingAppointments(String doctorId) {
        List<Consultation> appointments = new ArrayList<>();
        String sql = "SELECT * FROM consultations WHERE doctor_id = ? AND consultation_datetime >= ? ORDER BY consultation_datetime ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Consultation(
                        rs.getString("id"),
                        rs.getInt("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getTimestamp("consultation_datetime").toLocalDateTime(),
                        rs.getString("consulting_reason"),
                        rs.getDate("next_consulting_date") != null ? rs.getDate("next_consulting_date").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Prescription Management
    public void addPrescription(String consultationId, String medicineName, String dosage) {
        String sql = "INSERT INTO prescriptions (consultation_id, medicine_name, dosage) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, consultationId);
            pstmt.setString(2, medicineName);
            pstmt.setString(3, dosage);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Prescription> getPrescriptionsForConsultation(String consultationId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE consultation_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, consultationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prescriptions.add(new Prescription(
                        rs.getInt("id"),
                        rs.getString("consultation_id"),
                        rs.getString("medicine_name"),
                        rs.getString("dosage")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prescriptions;
    }

    // Statistics Methods
    public int getTodayAppointmentsCount() {
        String sql = "SELECT COUNT(*) FROM consultations WHERE DATE(consultation_datetime) = CURDATE()";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getDoctorTodayAppointments(String doctorId) {
        String sql = "SELECT COUNT(*) FROM consultations WHERE doctor_id = ? AND DATE(consultation_datetime) = CURDATE()";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getDoctorPatientCount(String doctorId) {
        String sql = "SELECT COUNT(DISTINCT patient_id) FROM consultations WHERE doctor_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
