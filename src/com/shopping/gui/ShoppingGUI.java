package com.shopping.gui;

import com.shopping.cart.CartItem;
import com.shopping.cart.ShoppingCart;
import com.shopping.dao.ProductDAO;
import com.shopping.dao.ProductDAO.DiscountSetting;
import com.shopping.dao.UserDAO;
import com.shopping.event.EventBus;
import com.shopping.event.InventoryAlertListener;
import com.shopping.event.OrderPlacedEvent;
import com.shopping.model.PhysicalProduct;
import com.shopping.model.Product;
import com.shopping.model.Role;
import com.shopping.model.User;
import com.shopping.order.Order;
import com.shopping.order.OrderProcessor;
import com.shopping.util.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Modern Dark-Themed Shopping GUI with full Admin product management,
 * multi-mode discount system, user management, and professional receipts.
 */
public class ShoppingGUI extends JFrame implements InventoryAlertListener {

    private ProductDAO productDAO;
    private UserDAO userDAO;
    private ShoppingCart shoppingCart;
    private User loggedInUser;

    private JPanel mainCardPanel;
    private CardLayout cardLayout;

    private JPanel cartItemsPanel; // replaces the old read-only cartTextArea
    private JLabel subtotalLabel;
    private JPanel customerCatalogPanel;
    private DefaultTableModel adminTableModel;
    private DarkPieChartPanel adminAnalyticsPanel;
    private JTabbedPane adminRightTabs;

    // Admin form fields
    private JTextField adminIdField;
    private JTextField adminNameField;
    private JTextField adminPriceField;
    private JSpinner adminStockSpinner;
    private JLabel adminImagePreview;
    private String adminSelectedImagePath = null;
    private boolean adminEditMode = false;

    // Discount admin panel
    private JPanel discountSettingsPanel;

    // Users admin panel
    private DefaultTableModel usersTableModel;

    // Orders admin panel
    private DefaultTableModel ordersTableModel;

    // Performance: Scaled Image Cache
    private final Map<String, ImageIcon> iconCache = new HashMap<>();

    // ?????? Dark Theme Palette
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private static final Color BG_DARKEST = new Color(18, 18, 30);
    private static final Color BG_DARK = new Color(25, 25, 45);
    private static final Color BG_CARD = new Color(32, 33, 55);
    private static final Color BG_CARD_HOVER = new Color(42, 43, 70);
    private static final Color BG_INPUT = new Color(38, 39, 62);
    private static final Color ACCENT_BLUE = new Color(0, 150, 255);
    private static final Color ACCENT_CYAN = new Color(0, 210, 255);
    private static final Color ACCENT_GREEN = new Color(0, 230, 118);
    private static final Color ACCENT_RED = new Color(255, 69, 58);
    private static final Color ACCENT_ORANGE = new Color(255, 159, 10);
    private static final Color ACCENT_PURPLE = new Color(175, 82, 222);
    private static final Color ACCENT_GOLD = new Color(255, 214, 10);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 245);
    private static final Color TEXT_SECONDARY = new Color(155, 155, 175);
    private static final Color BORDER_SUBTLE = new Color(58, 58, 80);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
    private static final Font FONT_RECEIPT = new Font("Consolas", Font.PLAIN, 12);

    // Image path mapping for all 30 tools
    private static final Map<String, String> TOOL_IMAGES = new HashMap<>();
    static {
        TOOL_IMAGES.put("TOOL-001", "assets/drill.png");
        TOOL_IMAGES.put("TOOL-002", "assets/hammer.png");
        TOOL_IMAGES.put("TOOL-003", "assets/wrench.png");
        TOOL_IMAGES.put("TOOL-004", "assets/saw.png");
        TOOL_IMAGES.put("TOOL-005", "assets/screwdriver.png");
        TOOL_IMAGES.put("TOOL-006", "assets/level.png");
        TOOL_IMAGES.put("TOOL-007", "assets/tape_measure.png");
        TOOL_IMAGES.put("TOOL-008", "assets/socket_set.png");
        TOOL_IMAGES.put("TOOL-009", "assets/sander.png");
        TOOL_IMAGES.put("TOOL-010", "assets/pipe_wrench.png");
        TOOL_IMAGES.put("TOOL-011", "assets/pliers.png");
        TOOL_IMAGES.put("TOOL-012", "assets/knife.png");
        TOOL_IMAGES.put("TOOL-013", "assets/grinder.png");
        TOOL_IMAGES.put("TOOL-014", "assets/work_light.png");
        TOOL_IMAGES.put("TOOL-015", "assets/hex_keys.png");
        TOOL_IMAGES.put("TOOL-016", "assets/jigsaw.png");
        TOOL_IMAGES.put("TOOL-017", "assets/goggles.png");
        TOOL_IMAGES.put("TOOL-018", "assets/gloves.png");
        TOOL_IMAGES.put("TOOL-019", "assets/drill_bits.png");
        TOOL_IMAGES.put("TOOL-020", "assets/voltage_tester.png");
        TOOL_IMAGES.put("TOOL-021", "assets/ratchet_set.png");
        TOOL_IMAGES.put("TOOL-022", "assets/knife.png");
        TOOL_IMAGES.put("TOOL-023", "assets/heat_gun.png");
        TOOL_IMAGES.put("TOOL-024", "assets/stud_finder.png");
        TOOL_IMAGES.put("TOOL-025", "assets/bit_holder.png");
        TOOL_IMAGES.put("TOOL-026", "assets/wire_stripper.png");
        TOOL_IMAGES.put("TOOL-027", "assets/clamp.png");
        TOOL_IMAGES.put("TOOL-028", "assets/caulking_gun.png");
        TOOL_IMAGES.put("TOOL-029", "assets/spirit_level.png");
        TOOL_IMAGES.put("TOOL-030", "assets/impact_driver.png");
    }

    public ShoppingGUI() {
        super("ToolShop Pro - Advanced Tools Shop");
        setSize(1150, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARKEST);

        // Set app icon
        try {
            File logoFile = new File("assets/logo.png");
            if (logoFile.exists()) {
                setIconImage(new ImageIcon(logoFile.getAbsolutePath()).getImage());
            }
        } catch (Exception e) {
            /* fallback */ }

        // Core Initialize
        DatabaseConnection.getInstance().initializeDatabase();
        productDAO = new ProductDAO();
        userDAO = new UserDAO();
        EventBus.registerListener(this);

        // Seed 30 default products
        seedDefaultProducts();
        productDAO.seedDefaultDiscounts();

        // Seed default users
        userDAO.registerUser("admin", "admin123", Role.ADMIN);
        userDAO.registerUser("shopper", "shop123", Role.CUSTOMER);

        // Cleanup listener on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                EventBus.unregisterListener(ShoppingGUI.this);
            }
        });

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BG_DARKEST);

        mainCardPanel.add(buildLoginPanel(), "LOGIN");
        mainCardPanel.add(buildCustomerView(), "CUSTOMER_VIEW");
        mainCardPanel.add(buildAdminDashboard(), "ADMIN_DASHBOARD");

        add(mainCardPanel);
        cardLayout.show(mainCardPanel, "LOGIN");
    }

    private void seedDefaultProducts() {
        if (!productDAO.getAllProducts().isEmpty())
            return;

        String[][] tools = {
                { "TOOL-001", "Power Drill 20V", "89.99", "15", "Cordless" },
                { "TOOL-002", "Steel Claw Hammer", "15.49", "30", "16 oz" },
                { "TOOL-003", "Adjustable Wrench 10in", "12.99", "45", "10-inch" },
                { "TOOL-004", "Circular Saw 7.25in", "129.99", "10", "Electric" },
                { "TOOL-005", "Cordless Screwdriver Set", "34.99", "25", "32-piece" },
                { "TOOL-006", "Digital Laser Level", "59.99", "18", "Self-leveling" },
                { "TOOL-007", "Tape Measure 25ft", "9.99", "50", "Heavy duty" },
                { "TOOL-008", "Socket Wrench Set", "49.99", "20", "72-piece" },
                { "TOOL-009", "Electric Sander", "45.99", "12", "120 grit" },
                { "TOOL-010", "Pipe Wrench 14in", "22.99", "28", "Cast iron" },
                { "TOOL-011", "Needle Nose Pliers", "8.49", "40", "6-inch" },
                { "TOOL-012", "Utility Knife Set", "14.99", "35", "Retractable" },
                { "TOOL-013", "Angle Grinder 4.5in", "39.99", "15", "850W" },
                { "TOOL-014", "LED Work Light", "27.99", "22", "Rechargeable" },
                { "TOOL-015", "Hex Key Allen Set", "11.99", "38", "30-piece" },
                { "TOOL-016", "Jigsaw Electric", "64.99", "14", "Variable speed" },
                { "TOOL-017", "Safety Goggle Pro", "6.99", "60", "Anti-fog" },
                { "TOOL-018", "Work Gloves Heavy Duty", "12.49", "55", "Leather" },
                { "TOOL-019", "Drill Bit Set HSS", "24.99", "30", "29-piece" },
                { "TOOL-020", "Voltage Tester Digital", "19.99", "25", "Non-contact" },
                { "TOOL-021", "Ratchet Set Metric", "54.99", "16", "40-piece" },
                { "TOOL-022", "Wood Chisel Set", "29.99", "20", "6-piece" },
                { "TOOL-023", "Heat Gun 1500W", "32.99", "18", "Adjustable" },
                { "TOOL-024", "Stud Finder Digital", "21.99", "22", "Deep scan" },
                { "TOOL-025", "Magnetic Bit Holder", "7.99", "45", "Universal" },
                { "TOOL-026", "Wire Stripper Tool", "13.99", "35", "Auto-adjust" },
                { "TOOL-027", "Clamp Set 6-Pack", "18.99", "28", "Spring clamps" },
                { "TOOL-028", "Caulking Gun Pro", "11.49", "30", "Drip-free" },
                { "TOOL-029", "Spirit Level 24in", "16.99", "25", "Aluminum" },
                { "TOOL-030", "Impact Driver 18V", "99.99", "12", "Brushless" },
        };

        for (String[] t : tools) {
            Product p = new PhysicalProduct(t[0], t[1], Double.parseDouble(t[2]),
                    Integer.parseInt(t[3]), 1.0, t[4]);
            String imgPath = TOOL_IMAGES.get(t[0]);
            if (imgPath != null)
                p.setImagePath(imgPath);
            productDAO.addProduct(p);
        }
        System.out.println("Seeded 30 default tool products.");
    }

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        SwingUtilities.invokeLater(() -> {
            for (CartItem item : event.getOrder().getCart().getItems()) {
                if (item.getProduct().getStockQuantity() <= 5) {
                    System.out.println("ALERT: Low stock for " + item.getProduct().getName()
                            + " (" + item.getProduct().getStockQuantity() + " remaining)");
                }
            }
        });
    }

    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // LOGIN PANEL
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARKEST, getWidth(), getHeight(), new Color(15, 32, 60));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wrapper.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        try {
            File logoFile = new File("assets/logo.png");
            if (logoFile.exists()) {
                ImageIcon rawIcon = new ImageIcon(logoFile.getAbsolutePath());
                Image scaled = rawIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            /* ignore */ }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(logoLabel, gbc);

        JLabel titleLabel = new JLabel("ToolShop Pro", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        gbc.gridy = 1;
        card.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 8, 20, 8);
        card.add(subtitleLabel, gbc);

        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridwidth = 2;

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(FONT_BODY);
        userLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 3;
        card.add(userLabel, gbc);

        JTextField userField = createDarkTextField(18);
        gbc.gridy = 4;
        card.add(userField, gbc);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(FONT_BODY);
        passLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 5;
        card.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(18);
        styleDarkField(passField);
        gbc.gridy = 6;
        card.add(passField, gbc);

        JButton loginButton = createAccentButton("Sign In", ACCENT_BLUE);
        loginButton.setPreferredSize(new Dimension(280, 42));
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 8, 8, 8);
        loginButton.addActionListener(e -> {
            User user = userDAO.authenticate(userField.getText(), new String(passField.getPassword()));
            if (user != null) {
                loggedInUser = user;
                if (user.getRole() == Role.ADMIN) {
                    refreshAdminDashboardAsync();
                    cardLayout.show(mainCardPanel, "ADMIN_DASHBOARD");
                } else {
                    shoppingCart = new ShoppingCart(productDAO);
                    updateCartDisplay();
                    refreshCustomerCatalogAsync();
                    cardLayout.show(mainCardPanel, "CUSTOMER_VIEW");
                }
                userField.setText("");
                passField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.", "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(loginButton, gbc);

        JLabel versionLabel = new JLabel("v2.0 - Enterprise Edition", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        versionLabel.setForeground(new Color(100, 100, 120));
        gbc.gridy = 8;
        gbc.insets = new Insets(12, 8, 0, 8);
        card.add(versionLabel, gbc);

        wrapper.add(card);
        return wrapper;
    }

    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // CUSTOMER VIEW
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private JPanel buildCustomerView() {
        JPanel view = new JPanel(new BorderLayout(0, 0));
        view.setBackground(BG_DARKEST);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(BG_DARK);
        JLabel miniLogo = new JLabel();
        try {
            File logoFile = new File("assets/logo.png");
            if (logoFile.exists()) {
                miniLogo.setIcon(new ImageIcon(
                        new ImageIcon(logoFile.getAbsolutePath()).getImage().getScaledInstance(36, 36,
                                Image.SCALE_SMOOTH)));
            }
        } catch (Exception e) {
        }
        titlePanel.add(miniLogo);
        JLabel shopTitle = new JLabel("ToolShop Pro - Product Catalog");
        shopTitle.setFont(FONT_HEADING);
        shopTitle.setForeground(TEXT_PRIMARY);
        titlePanel.add(shopTitle);
        topBar.add(titlePanel, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setBackground(BG_DARK);
        JLabel welcomeLabel = new JLabel("Welcome, Customer");
        welcomeLabel.setFont(FONT_SMALL);
        welcomeLabel.setForeground(TEXT_SECONDARY);
        topRight.add(welcomeLabel);
        JButton logoutBtn = createAccentButton("Logout", new Color(80, 80, 100));
        logoutBtn.addActionListener(e -> {
            loggedInUser = null;
            cardLayout.show(mainCardPanel, "LOGIN");
        });
        topRight.add(logoutBtn);
        topBar.add(topRight, BorderLayout.EAST);
        view.add(topBar, BorderLayout.NORTH);

        // Product catalog
        customerCatalogPanel = new JPanel();
        customerCatalogPanel.setLayout(new BoxLayout(customerCatalogPanel, BoxLayout.Y_AXIS));
        customerCatalogPanel.setBackground(BG_DARKEST);
        customerCatalogPanel.setBorder(new EmptyBorder(10, 15, 10, 5));
        JScrollPane scrollPane = new JScrollPane(customerCatalogPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_DARKEST);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        view.add(scrollPane, BorderLayout.CENTER);

        // Cart sidebar
        JPanel cartPanel = new JPanel(new BorderLayout(0, 10));
        cartPanel.setBackground(BG_DARK);
        cartPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        cartPanel.setPreferredSize(new Dimension(400, 0));

        JLabel cartTitle = new JLabel("Shopping Basket", SwingConstants.CENTER);
        cartTitle.setFont(FONT_HEADING);
        cartTitle.setForeground(TEXT_PRIMARY);
        cartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        cartPanel.add(cartTitle, BorderLayout.NORTH);

        // Interactive cart items panel (replaces the old static JTextArea)
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(BG_CARD);
        cartItemsPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane cartScroll = new JScrollPane(cartItemsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // ??? forces content to fit width
        cartScroll.setBorder(new LineBorder(BORDER_SUBTLE));
        cartScroll.getViewport().setBackground(BG_CARD);
        cartScroll.getVerticalScrollBar().setUnitIncrement(10);
        cartPanel.add(cartScroll, BorderLayout.CENTER);

        JPanel cartBottom = new JPanel();
        cartBottom.setLayout(new BoxLayout(cartBottom, BoxLayout.Y_AXIS));
        cartBottom.setBackground(BG_DARK);

        subtotalLabel = new JLabel("Subtotal: $0.00", SwingConstants.RIGHT);
        subtotalLabel.setFont(FONT_HEADING);
        subtotalLabel.setForeground(ACCENT_GREEN);
        subtotalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        subtotalLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        cartBottom.add(subtotalLabel);

        JButton checkoutButton = createAccentButton("Complete Purchase", ACCENT_GREEN);
        checkoutButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        checkoutButton.addActionListener(e -> {
            if (shoppingCart.getItems().size() == 0) {
                JOptionPane.showMessageDialog(this, "Your basket is empty.");
                return;
            }
            performCheckout();
        });
        cartBottom.add(Box.createVerticalStrut(8));
        cartBottom.add(checkoutButton);
        cartPanel.add(cartBottom, BorderLayout.SOUTH);
        view.add(cartPanel, BorderLayout.EAST);

        return view;
    }

    private void performCheckout() {
        double subtotal = shoppingCart.calculateSubtotal();
        int itemCount = 0;
        for (CartItem item : shoppingCart.getItems()) {
            itemCount += item.getQuantity();
        }

        List<DiscountSetting> applicableDiscounts = productDAO.getApplicableDiscounts(subtotal, itemCount);
        DiscountSetting bestDiscount = null;
        double bestSavings = 0;
        for (DiscountSetting ds : applicableDiscounts) {
            double savings = subtotal * (ds.discountPercent / 100.0);
            if (savings > bestSavings) {
                bestSavings = savings;
                bestDiscount = ds;
            }
        }

        if (bestDiscount != null) {
            shoppingCart.setDiscountPercent(bestDiscount.discountPercent);
        } else {
            shoppingCart.setDiscountPercent(0.0);
        }

        OrderProcessor processor = new OrderProcessor(productDAO);
        try {
            Order result = processor.checkout(shoppingCart, loggedInUser.getId(), loggedInUser.getUsername());
            showReceipt(result, subtotal, bestDiscount, bestSavings);
            shoppingCart = new ShoppingCart(productDAO);
            refreshCustomerCatalogAsync();
            updateCartDisplay();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Displays a professional receipt dialog with Print and Save as Image options.
     */
    private void showReceipt(Order order, double subtotal, DiscountSetting discount, double savings) {
        JDialog receiptDialog = new JDialog(this, "Order Receipt", true);
        receiptDialog.setSize(460, 650);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.getContentPane().setBackground(BG_DARKEST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARKEST);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Build receipt text
        String receiptText = buildReceiptText(order, subtotal, discount, savings);

        JTextArea receiptArea = new JTextArea(receiptText);
        receiptArea.setFont(FONT_RECEIPT);
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setForeground(new Color(40, 40, 40));
        receiptArea.setBorder(new EmptyBorder(20, 25, 20, 25));
        receiptArea.setCaretPosition(0);

        JScrollPane receiptScroll = new JScrollPane(receiptArea);
        receiptScroll.setBorder(new LineBorder(new Color(200, 200, 200)));
        mainPanel.add(receiptScroll, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttonPanel.setBackground(BG_DARKEST);

        JButton closeBtn = createAccentButton("Close", new Color(80, 80, 100));
        closeBtn.addActionListener(e -> receiptDialog.dispose());
        buttonPanel.add(closeBtn);

        JButton printBtn = createAccentButton("Print", ACCENT_BLUE);
        printBtn.addActionListener(e -> {
            try {
                receiptArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(receiptDialog, "Printing failed: " + ex.getMessage());
            }
        });
        buttonPanel.add(printBtn);

        JButton saveImgBtn = createAccentButton("Save as Image", ACCENT_GREEN);
        saveImgBtn.addActionListener(e -> saveReceiptAsImage(receiptArea, receiptDialog));
        buttonPanel.add(saveImgBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        receiptDialog.add(mainPanel);
        receiptDialog.setVisible(true);
    }

    private String buildReceiptText(Order order, double subtotal, DiscountSetting discount, double savings) {
        StringBuilder r = new StringBuilder();
        String line = "--------------------------------------------";
        String dline = "============================================";

        r.append("\n");
        r.append("            TOOLSHOP PRO\n");
        r.append("         Advanced Tools Shop\n");
        r.append("       Your Trusted Tool Partner\n");
        r.append("\n");
        r.append(dline).append("\n");
        r.append("            SALES RECEIPT\n");
        r.append(dline).append("\n\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);
        r.append(" Date:     ").append(LocalDateTime.now().format(dtf)).append("\n");
        r.append(" Order:    ").append(order.getOrderId().substring(0, 8).toUpperCase()).append("...\n");
        r.append(" Customer: ").append(order.getCustomerName()).append("\n");
        r.append(" Cashier:  System (Auto)\n");

        r.append("\n").append(line).append("\n");
        r.append(String.format(" %-24s %4s %10s\n", "ITEM", "QTY", "AMOUNT"));
        r.append(line).append("\n");

        for (CartItem item : order.getCart().getItems()) {
            String itemName = item.getProduct().getName();
            if (itemName.length() > 24)
                itemName = itemName.substring(0, 22) + "..";
            double amount = item.getProduct().getPrice() * item.getQuantity();
            r.append(String.format(Locale.US, " %-24s %4d %10s\n",
                    itemName, item.getQuantity(), String.format(Locale.US, "$%.2f", amount)));
            if (item.getQuantity() > 1) {
                r.append(String.format(Locale.US, "   @ $%.2f each\n", item.getProduct().getPrice()));
            }
        }

        r.append(line).append("\n\n");
        r.append(String.format(Locale.US, " %-30s %10s\n", "SUBTOTAL:", String.format(Locale.US, "$%.2f", subtotal)));

        if (discount != null && savings > 0) {
            r.append(String.format(Locale.US, " %-30s %10s\n",
                    discount.label + " (" + (int) discount.discountPercent + "%):",
                    "-$" + String.format(Locale.US, "%.2f", savings)));
        }

        r.append(" ").append(line).append("\n");
        r.append(String.format(Locale.US, " %-30s %10s\n", "TOTAL:",
                String.format(Locale.US, "$%.2f", order.getFinalTotal())));
        r.append(" ").append(dline).append("\n\n");

        r.append("      Payment: Approved\n");
        r.append("      Status:  Complete\n\n");
        r.append(" ").append(line).append("\n");
        r.append("    Thank you for shopping at\n");
        r.append("         ToolShop Pro!\n\n");
        r.append("    Returns within 30 days with\n");
        r.append("    receipt. Keep this for your\n");
        r.append("    records.\n\n");
        r.append("    Customer Service:\n");
        r.append("    support@toolshoppro.com\n");
        r.append(" ").append(dline).append("\n");

        return r.toString();
    }

    /**
     * Renders the receipt text area to a PNG image and saves it via file chooser.
     */
    private void saveReceiptAsImage(JTextArea receiptArea, JDialog parent) {
        // Render the text area to a buffered image
        int w = receiptArea.getWidth();
        int h = receiptArea.getHeight();
        if (w <= 0 || h <= 0) {
            w = 400;
            h = 600;
        }

        BufferedImage image = new BufferedImage(w + 40, h + 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w + 40, h + 40);
        g2.translate(20, 20);
        receiptArea.paint(g2);
        g2.dispose();

        // File chooser
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("receipt_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png"));
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");
                }
                ImageIO.write(image, "png", file);
                JOptionPane.showMessageDialog(parent,
                        "Receipt saved as: " + file.getAbsolutePath(),
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to save image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshCustomerCatalogAsync() {
        customerCatalogPanel.removeAll();
        JLabel loading = new JLabel("Loading products...", SwingConstants.CENTER);
        loading.setFont(FONT_HEADING);
        loading.setForeground(TEXT_SECONDARY);
        loading.setBorder(new EmptyBorder(60, 0, 0, 0));
        customerCatalogPanel.add(loading);
        customerCatalogPanel.revalidate();
        customerCatalogPanel.repaint();

        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() {
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                }
                return new ArrayList<>(productDAO.getAllProducts().values());
            }

            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    customerCatalogPanel.removeAll();
                    if (products.isEmpty()) {
                        JLabel empty = new JLabel("No products available.", SwingConstants.CENTER);
                        empty.setFont(FONT_BODY);
                        empty.setForeground(TEXT_SECONDARY);
                        empty.setBorder(new EmptyBorder(60, 0, 0, 0));
                        customerCatalogPanel.add(empty);
                    } else {
                        List<DiscountSetting> active = productDAO.getApplicableDiscounts(0, 0);
                        if (!active.isEmpty()) {
                            customerCatalogPanel.add(createDiscountBanner(active));
                            customerCatalogPanel.add(Box.createVerticalStrut(10));
                        }
                        for (Product product : products) {
                            customerCatalogPanel.add(buildProductCard(product));
                            customerCatalogPanel.add(Box.createVerticalStrut(6));
                        }
                    }
                    customerCatalogPanel.revalidate();
                    customerCatalogPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private JPanel createDiscountBanner(List<DiscountSetting> discounts) {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 50, 0), getWidth(), 0,
                        new Color(180, 100, 0));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 8));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        StringBuilder text = new StringBuilder("Active Deals: ");
        for (int i = 0; i < discounts.size(); i++) {
            if (i > 0)
                text.append(" | ");
            DiscountSetting ds = discounts.get(i);
            text.append(ds.label).append(" ").append((int) ds.discountPercent).append("% OFF");
        }

        JLabel bannerLabel = new JLabel(text.toString());
        bannerLabel.setFont(FONT_BODY_BOLD);
        bannerLabel.setForeground(Color.WHITE);
        banner.add(bannerLabel);
        return banner;
    }

    private JPanel buildProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? BG_CARD_HOVER : BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 15, 12, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        card.setPreferredSize(new Dimension(600, 80));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(56, 56));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            String path = product.getImagePath();
            if (iconCache.containsKey(path)) {
                imageLabel.setIcon(iconCache.get(path));
            } else if (new File(path).exists()) {
                ImageIcon icon = new ImageIcon(
                        new ImageIcon(path).getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH));
                iconCache.put(path, icon);
                imageLabel.setIcon(icon);
            } else {
                setPlaceholderIcon(imageLabel);
            }
        } else {
            setPlaceholderIcon(imageLabel);
        }
        card.add(imageLabel, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);
        details.setBorder(new EmptyBorder(3, 0, 3, 0));

        JLabel nameLabel = new JLabel(product.getName(), SwingConstants.CENTER);
        nameLabel.setFont(FONT_BODY_BOLD);
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        details.add(nameLabel);

        JLabel codeLabel = new JLabel("Code: " + product.getId(), SwingConstants.CENTER);
        codeLabel.setFont(FONT_SMALL);
        codeLabel.setForeground(TEXT_SECONDARY);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        details.add(codeLabel);

        JPanel priceStockRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        priceStockRow.setOpaque(false);
        priceStockRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel priceLabel = new JLabel(String.format(Locale.US, "$%.2f", product.getPrice()));
        priceLabel.setFont(FONT_BODY_BOLD);
        priceLabel.setForeground(ACCENT_CYAN);
        priceStockRow.add(priceLabel);

        String stockText = product.getStockQuantity() <= 5
                ? "   Low Stock: " + product.getStockQuantity()
                : "   In Stock: " + product.getStockQuantity();
        JLabel stockLabel = new JLabel(stockText);
        stockLabel.setFont(FONT_SMALL);
        stockLabel.setForeground(product.getStockQuantity() <= 5 ? ACCENT_RED : TEXT_SECONDARY);
        priceStockRow.add(stockLabel);
        details.add(priceStockRow);

        card.add(details, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        btnPanel.setOpaque(false);
        JButton addBtn = createAccentButton("Add to Basket", ACCENT_BLUE);
        addBtn.addActionListener(e -> {
            try {
                shoppingCart.addProduct(product.getId(), 1);
                updateCartDisplay();
            } catch (com.shopping.exception.OutOfStockException ex) {
                JOptionPane.showMessageDialog(this, "Not enough stock available!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(addBtn);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    private void setPlaceholderIcon(JLabel label) {
        label.setText("IMG");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_SECONDARY);
        label.setBorder(BorderFactory.createDashedBorder(BORDER_SUBTLE, 1, 3, 2, true));
    }

    /**
     * Rebuilds the interactive cart panel with one row per CartItem.
     * Each row has: product name+price | ??? qty + | line total | Remove button.
     */
    private void updateCartDisplay() {
        if (cartItemsPanel == null)
            return;
        cartItemsPanel.removeAll();

        int totalItems = 0;

        if (shoppingCart.getItems().size() == 0) {
            JLabel empty = new JLabel("Your basket is empty", SwingConstants.CENTER);
            empty.setFont(FONT_SMALL);
            empty.setForeground(TEXT_SECONDARY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
            cartItemsPanel.add(empty);
        } else {
            for (CartItem item : shoppingCart.getItems()) {
                totalItems += item.getQuantity();
                cartItemsPanel.add(buildCartItemRow(item));
                // Thin separator
                JSeparator sep = new JSeparator();
                sep.setForeground(BORDER_SUBTLE);
                sep.setBackground(BORDER_SUBTLE);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                cartItemsPanel.add(sep);
            }
        }

        subtotalLabel.setText(String.format(Locale.US,
                "Subtotal (%d items): $%.2f", totalItems, shoppingCart.calculateSubtotal()));
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    /**
     * Builds one interactive cart row.
     * Three-line stacked layout ??? always fits any width:
     *
     * [ Product Name $lineTotal ]
     * [ @ $unit each ]
     * [ [ - ] [ qty ] [ + ] [ Remove ] ]
     */
    private JPanel buildCartItemRow(CartItem item) {
        String productId = item.getProduct().getId();
        double lineTotal = item.getProduct().getPrice() * item.getQuantity();

        // Outer wrapper ??? full width, fixed height
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(BG_CARD);
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ?????? Line 1: name (left) + line total (right)
        // ???????????????????????????????????????
        JPanel line1 = new JPanel(new BorderLayout());
        line1.setOpaque(false);
        line1.setAlignmentX(Component.LEFT_ALIGNMENT);
        line1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel nameLabel = new JLabel(item.getProduct().getName());
        nameLabel.setFont(FONT_BODY_BOLD);
        nameLabel.setForeground(TEXT_PRIMARY);
        line1.add(nameLabel, BorderLayout.WEST);

        JLabel totalLabel = new JLabel(String.format(Locale.US, "  $%.2f", lineTotal));
        totalLabel.setFont(FONT_BODY_BOLD);
        totalLabel.setForeground(ACCENT_GREEN);
        line1.add(totalLabel, BorderLayout.EAST);
        row.add(line1);

        row.add(Box.createVerticalStrut(2));

        // ?????? Line 2: unit price
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        JLabel unitLabel = new JLabel(
                String.format(Locale.US, "$%.2f / unit", item.getProduct().getPrice()));
        unitLabel.setFont(FONT_SMALL);
        unitLabel.setForeground(TEXT_SECONDARY);
        unitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(unitLabel);

        row.add(Box.createVerticalStrut(5));

        // ?????? Line 3: qty controls (left) + Remove (right)
        // ?????????????????????????????????
        JPanel line3 = new JPanel(new BorderLayout());
        line3.setOpaque(false);
        line3.setAlignmentX(Component.LEFT_ALIGNMENT);
        line3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel qtyGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        qtyGroup.setOpaque(false);
        qtyGroup.add(makeQtyButton("-", new Color(55, 57, 88), e -> {
            shoppingCart.reduceProduct(productId);
            updateCartDisplay();
        }));

        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()), SwingConstants.CENTER);
        qtyLabel.setFont(FONT_BODY_BOLD);
        qtyLabel.setForeground(ACCENT_CYAN);
        qtyLabel.setPreferredSize(new Dimension(34, 28));
        qtyLabel.setBackground(new Color(42, 43, 68));
        qtyLabel.setOpaque(true);
        qtyLabel.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true));
        qtyGroup.add(qtyLabel);

        qtyGroup.add(makeQtyButton("+", new Color(55, 57, 88), e -> {
            try {
                shoppingCart.addProduct(productId, 1);
                updateCartDisplay();
            } catch (com.shopping.exception.OutOfStockException ex) {
                JOptionPane.showMessageDialog(this, "No more stock available for this item!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }));

        line3.add(qtyGroup, BorderLayout.WEST);
        line3.add(makeRemoveButton("Remove", e -> {
            shoppingCart.removeProduct(productId);
            updateCartDisplay();
        }), BorderLayout.EAST);

        row.add(line3);
        return row;
    }

    /** Creates a small square qty button (??? or +) with custom paint. */
    private JButton makeQtyButton(String label, Color bg,
            java.awt.event.ActionListener action) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.brighter() : getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(label)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(label, tx, ty);
            }
        };
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    /** Creates the Remove button with a custom red rounded style. */
    private JButton makeRemoveButton(String label,
            java.awt.event.ActionListener action) {
        Color bg = ACCENT_RED;
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(label)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(label, tx, ty);
            }
        };
        btn.setPreferredSize(new Dimension(64, 28));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // ADMIN DASHBOARD
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private JPanel buildAdminDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARKEST);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(BG_DARK);
        JLabel miniLogo = new JLabel();
        try {
            File logoFile = new File("assets/logo.png");
            if (logoFile.exists()) {
                miniLogo.setIcon(new ImageIcon(
                        new ImageIcon(logoFile.getAbsolutePath()).getImage().getScaledInstance(32, 32,
                                Image.SCALE_SMOOTH)));
            }
        } catch (Exception e) {
        }
        titlePanel.add(miniLogo);
        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        titlePanel.add(title);
        topBar.add(titlePanel, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setBackground(BG_DARK);
        JButton logoutBtn = createAccentButton("Logout", new Color(80, 80, 100));
        logoutBtn.addActionListener(e -> {
            loggedInUser = null;
            cardLayout.show(mainCardPanel, "LOGIN");
        });
        topRight.add(logoutBtn);
        topBar.add(topRight, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Product table
        adminTableModel = new DefaultTableModel(
                new String[] { "Code", "Name", "Price", "Stock", "Sold" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(adminTableModel);
        table.setRowHeight(30);
        table.setFont(FONT_BODY);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_SUBTLE);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(35, 35, 60));
        header.setForeground(ACCENT_CYAN);
        header.setBorder(new LineBorder(BORDER_SUBTLE));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(BG_CARD);
        centerRenderer.setForeground(TEXT_PRIMARY);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new LineBorder(BORDER_SUBTLE));
        tableScroll.getViewport().setBackground(BG_CARD);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tableActions.setBackground(BG_DARKEST);

        JButton addItemBtn = createAccentButton("+ Add New Item", ACCENT_GREEN);
        addItemBtn.addActionListener(e -> {
            clearAdminForm();
            if (adminRightTabs != null)
                adminRightTabs.setSelectedIndex(0);
        });

        JButton editBtn = createAccentButton("Edit Selected", ACCENT_ORANGE);
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a product to edit.");
                return;
            }
            String id = (String) adminTableModel.getValueAt(row, 0);
            Product p = productDAO.getAllProducts().get(id);
            if (p != null) {
                adminIdField.setText(p.getId());
                adminIdField.setEditable(false);
                adminNameField.setText(p.getName());
                adminPriceField.setText(String.format(Locale.US, "%.2f", p.getPrice()));
                adminStockSpinner.setValue(p.getStockQuantity());
                adminSelectedImagePath = p.getImagePath();
                adminEditMode = true;
                loadImagePreview(adminSelectedImagePath);
                if (adminRightTabs != null)
                    adminRightTabs.setSelectedIndex(0);
            }
        });

        JButton deleteBtn = createAccentButton("Delete Selected", ACCENT_RED);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a product to delete.");
                return;
            }
            String id = (String) adminTableModel.getValueAt(row, 0);
            String name = (String) adminTableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete product \"" + name + "\" (" + id + ")?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (productDAO.deleteProduct(id)) {
                    refreshAdminDashboardAsync();
                    JOptionPane.showMessageDialog(this, "Product deleted.");
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot delete - referenced by orders.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton refreshBtn = createAccentButton("Refresh", ACCENT_BLUE);
        refreshBtn.addActionListener(e -> refreshAdminDashboardAsync());

        tableActions.add(addItemBtn);
        tableActions.add(editBtn);
        tableActions.add(deleteBtn);
        tableActions.add(refreshBtn);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BG_DARKEST);
        leftPanel.add(tableScroll, BorderLayout.CENTER);
        leftPanel.add(tableActions, BorderLayout.SOUTH);

        // Right sidebar: Tabs
        adminRightTabs = new JTabbedPane();
        adminRightTabs.setFont(FONT_BODY_BOLD);
        adminRightTabs.setBackground(BG_DARK);
        adminRightTabs.setForeground(TEXT_PRIMARY);
        adminRightTabs.setPreferredSize(new Dimension(370, 0));

        adminRightTabs.addTab("Products", buildProductFormPanel());
        adminRightTabs.addTab("Discounts", buildDiscountSettingsPanel());
        adminRightTabs.addTab("Orders", buildOrdersPanel());
        adminRightTabs.addTab("Users", buildUsersPanel());
        adminRightTabs.addTab("Analytics", buildAnalyticsTab());
        JTabbedPane rightTabs = adminRightTabs;

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightTabs);
        split.setDividerLocation(680);
        split.setBorder(null);
        split.setBackground(BG_DARKEST);
        split.setDividerSize(4);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildProductFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARKEST);

        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(5, 5, 5, 5);
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.gridwidth = 2;
        fc.gridx = 0;

        JLabel formTitle = new JLabel("Product Details");
        formTitle.setFont(FONT_HEADING);
        formTitle.setForeground(TEXT_PRIMARY);
        fc.gridy = 0;
        formCard.add(formTitle, fc);

        fc.gridwidth = 1;

        fc.gridy = 1;
        fc.gridx = 0;
        formCard.add(createFormLabel("Product Code"), fc);
        adminIdField = createDarkTextField(12);
        fc.gridx = 1;
        formCard.add(adminIdField, fc);

        fc.gridy = 2;
        fc.gridx = 0;
        formCard.add(createFormLabel("Name"), fc);
        adminNameField = createDarkTextField(12);
        fc.gridx = 1;
        formCard.add(adminNameField, fc);

        fc.gridy = 3;
        fc.gridx = 0;
        formCard.add(createFormLabel("Price ($)"), fc);
        adminPriceField = createDarkTextField(12);
        fc.gridx = 1;
        formCard.add(adminPriceField, fc);

        fc.gridy = 4;
        fc.gridx = 0;
        formCard.add(createFormLabel("Stock"), fc);
        adminStockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 99999, 1));
        adminStockSpinner.setFont(FONT_BODY);
        adminStockSpinner.getEditor().getComponent(0).setBackground(BG_INPUT);
        adminStockSpinner.getEditor().getComponent(0).setForeground(TEXT_PRIMARY);
        fc.gridx = 1;
        formCard.add(adminStockSpinner, fc);

        fc.gridy = 5;
        fc.gridx = 0;
        formCard.add(createFormLabel("Photo"), fc);
        JButton chooseImgBtn = createAccentButton("Browse...", ACCENT_PURPLE);
        chooseImgBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif", "bmp"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                try {
                    Path assetsDir = Paths.get("assets");
                    if (!Files.exists(assetsDir))
                        Files.createDirectories(assetsDir);
                    Path dest = assetsDir.resolve(selectedFile.getName());
                    Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    adminSelectedImagePath = "assets/" + selectedFile.getName();
                    loadImagePreview(adminSelectedImagePath);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to copy image: " + ex.getMessage());
                }
            }
        });
        fc.gridx = 1;
        formCard.add(chooseImgBtn, fc);

        adminImagePreview = new JLabel("No image", SwingConstants.CENTER);
        adminImagePreview.setFont(FONT_SMALL);
        adminImagePreview.setForeground(TEXT_SECONDARY);
        adminImagePreview.setPreferredSize(new Dimension(80, 80));
        adminImagePreview.setBorder(BorderFactory.createDashedBorder(BORDER_SUBTLE, 2, 4, 3, true));
        fc.gridy = 6;
        fc.gridx = 0;
        fc.gridwidth = 2;
        formCard.add(adminImagePreview, fc);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        formButtons.setOpaque(false);
        JButton addProductBtn = createAccentButton("Save Product", ACCENT_GREEN);
        addProductBtn.addActionListener(e -> handleSaveProduct());
        formButtons.add(addProductBtn);
        JButton clearBtn = createAccentButton("Clear", new Color(80, 80, 100));
        clearBtn.addActionListener(e -> clearAdminForm());
        formButtons.add(clearBtn);

        fc.gridy = 7;
        fc.gridwidth = 2;
        fc.insets = new Insets(15, 5, 5, 5);
        formCard.add(formButtons, fc);

        wrapper.add(formCard, BorderLayout.NORTH);
        return wrapper;
    }

    // ?????? DISCOUNT SETTINGS PANEL (FIXED LAYOUT)
    // ??????????????????????????????????????????????????????????????????

    private JPanel buildDiscountSettingsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARKEST);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_CARD);
        headerPanel.setBorder(new EmptyBorder(12, 15, 8, 15));
        JLabel headerLabel = new JLabel("Discount Management");
        headerLabel.setFont(FONT_HEADING);
        headerLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        JLabel helpLabel = new JLabel("Toggle and configure active discounts");
        helpLabel.setFont(FONT_SMALL);
        helpLabel.setForeground(TEXT_SECONDARY);
        headerPanel.add(helpLabel, BorderLayout.SOUTH);
        wrapper.add(headerPanel, BorderLayout.NORTH);

        discountSettingsPanel = new JPanel();
        discountSettingsPanel.setLayout(new BoxLayout(discountSettingsPanel, BoxLayout.Y_AXIS));
        discountSettingsPanel.setBackground(BG_DARKEST);
        discountSettingsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(discountSettingsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_DARKEST);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private void refreshDiscountSettingsUI() {
        if (discountSettingsPanel == null)
            return;
        discountSettingsPanel.removeAll();

        List<DiscountSetting> settings = productDAO.getAllDiscountSettings();
        for (DiscountSetting ds : settings) {
            discountSettingsPanel.add(buildDiscountCard(ds));
            discountSettingsPanel.add(Box.createVerticalStrut(6));
        }
        discountSettingsPanel.revalidate();
        discountSettingsPanel.repaint();
    }

    private JPanel buildDiscountCard(DiscountSetting ds) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.setPreferredSize(new Dimension(320, 190));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 4, 2, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1.0;

        // Row 0: Title + Toggle
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.weightx = 0.6;
        JLabel nameLabel = new JLabel(ds.label);
        nameLabel.setFont(FONT_BODY_BOLD);
        nameLabel.setForeground(ds.enabled ? ACCENT_GREEN : TEXT_SECONDARY);
        card.add(nameLabel, gc);

        gc.gridx = 1;
        gc.weightx = 0.4;
        JCheckBox enableToggle = new JCheckBox("Enabled", ds.enabled);
        enableToggle.setFont(FONT_SMALL);
        enableToggle.setForeground(ds.enabled ? ACCENT_GREEN : TEXT_SECONDARY);
        enableToggle.setOpaque(false);
        card.add(enableToggle, gc);

        // Row 1: Percent
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0.5;
        JLabel pctLabel = createFormLabel("Discount %");
        pctLabel.setFont(FONT_SMALL);
        card.add(pctLabel, gc);
        JTextField percentField = createDarkTextField(5);
        percentField.setFont(FONT_SMALL);
        percentField.setText(String.format("%.0f", ds.discountPercent));
        gc.gridx = 1;
        card.add(percentField, gc);

        // Row 2: Min order
        gc.gridx = 0;
        gc.gridy = 2;
        JLabel moLabel = createFormLabel("Min Order ($)");
        moLabel.setFont(FONT_SMALL);
        card.add(moLabel, gc);
        JTextField minOrderField = createDarkTextField(5);
        minOrderField.setFont(FONT_SMALL);
        minOrderField.setText(String.format("%.0f", ds.minOrderValue));
        gc.gridx = 1;
        card.add(minOrderField, gc);

        // Row 3: Min items
        gc.gridx = 0;
        gc.gridy = 3;
        JLabel miLabel = createFormLabel("Min Items");
        miLabel.setFont(FONT_SMALL);
        card.add(miLabel, gc);
        JTextField minItemsField = createDarkTextField(5);
        minItemsField.setFont(FONT_SMALL);
        minItemsField.setText(String.valueOf(ds.minItems));
        gc.gridx = 1;
        card.add(minItemsField, gc);

        // Row 4: Save button - FULL WIDTH
        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 2;
        gc.insets = new Insets(8, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        JButton saveBtn = createAccentButton("Save", ACCENT_BLUE);
        saveBtn.setPreferredSize(new Dimension(0, 32));
        saveBtn.addActionListener(e -> {
            try {
                ds.enabled = enableToggle.isSelected();
                ds.discountPercent = Double.parseDouble(percentField.getText().trim());
                ds.minOrderValue = Double.parseDouble(minOrderField.getText().trim());
                ds.minItems = Integer.parseInt(minItemsField.getText().trim());
                if (ds.discountPercent < 0 || ds.discountPercent > 100) {
                    JOptionPane.showMessageDialog(this, "Discount must be 0-100%.");
                    return;
                }
                productDAO.updateDiscountSetting(ds);
                JOptionPane.showMessageDialog(this, "\"" + ds.label + "\" updated!", "Saved",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshDiscountSettingsUI();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid numbers.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        card.add(saveBtn, gc);

        return card;
    }

    // ?????? USERS MANAGEMENT PANEL
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private JPanel buildUsersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARKEST);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_CARD);
        headerPanel.setBorder(new EmptyBorder(12, 15, 8, 15));
        JLabel headerLabel = new JLabel("Registered Users");
        headerLabel.setFont(FONT_HEADING);
        headerLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        wrapper.add(headerPanel, BorderLayout.NORTH);

        usersTableModel = new DefaultTableModel(new String[] { "ID", "Username", "Role" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable usersTable = new JTable(usersTableModel);
        usersTable.setRowHeight(30);
        usersTable.setFont(FONT_BODY);
        usersTable.setBackground(BG_CARD);
        usersTable.setForeground(TEXT_PRIMARY);
        usersTable.setSelectionBackground(ACCENT_BLUE.darker());
        usersTable.setSelectionForeground(Color.WHITE);
        usersTable.setGridColor(BORDER_SUBTLE);
        usersTable.setShowGrid(true);

        JTableHeader uHeader = usersTable.getTableHeader();
        uHeader.setFont(FONT_BODY_BOLD);
        uHeader.setBackground(new Color(35, 35, 60));
        uHeader.setForeground(ACCENT_CYAN);
        uHeader.setBorder(new LineBorder(BORDER_SUBTLE));

        DefaultTableCellRenderer uCenter = new DefaultTableCellRenderer();
        uCenter.setHorizontalAlignment(SwingConstants.CENTER);
        uCenter.setBackground(BG_CARD);
        uCenter.setForeground(TEXT_PRIMARY);
        for (int i = 0; i < usersTable.getColumnCount(); i++) {
            usersTable.getColumnModel().getColumn(i).setCellRenderer(uCenter);
        }

        JScrollPane usersScroll = new JScrollPane(usersTable);
        usersScroll.setBorder(new LineBorder(BORDER_SUBTLE));
        usersScroll.getViewport().setBackground(BG_CARD);
        wrapper.add(usersScroll, BorderLayout.CENTER);

        JPanel usersActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        usersActions.setBackground(BG_DARKEST);
        JButton refreshUsersBtn = createAccentButton("Refresh", ACCENT_BLUE);
        refreshUsersBtn.addActionListener(e -> refreshUsersTable());
        usersActions.add(refreshUsersBtn);

        JLabel countLabel = new JLabel("Total: " + userDAO.getUserCount());
        countLabel.setFont(FONT_SMALL);
        countLabel.setForeground(TEXT_SECONDARY);
        usersActions.add(countLabel);

        wrapper.add(usersActions, BorderLayout.SOUTH);
        return wrapper;
    }

    private void refreshUsersTable() {
        if (usersTableModel == null)
            return;
        usersTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            usersTableModel.addRow(new Object[] { u.getId(), u.getUsername(), u.getRole().name() });
        }
    }

    // ?????? ORDERS HISTORY PANEL
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private JPanel buildOrdersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARKEST);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_CARD);
        headerPanel.setBorder(new EmptyBorder(12, 15, 8, 15));
        JLabel headerLabel = new JLabel("Order History");
        headerLabel.setFont(FONT_HEADING);
        headerLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        JLabel hintLabel = new JLabel("Double-click a row to view items");
        hintLabel.setFont(FONT_SMALL);
        hintLabel.setForeground(TEXT_SECONDARY);
        headerPanel.add(hintLabel, BorderLayout.EAST);
        wrapper.add(headerPanel, BorderLayout.NORTH);

        // Column 4 ("_fullId") is HIDDEN ??? stores the real UUID for detail lookup
        ordersTableModel = new DefaultTableModel(
                new String[] { "Order ID", "Customer", "Total", "Date", "_fullId" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable ordersTable = new JTable(ordersTableModel);
        ordersTable.setRowHeight(28);
        ordersTable.setFont(FONT_SMALL);
        ordersTable.setBackground(BG_CARD);
        ordersTable.setForeground(TEXT_PRIMARY);
        ordersTable.setSelectionBackground(ACCENT_BLUE.darker());
        ordersTable.setSelectionForeground(Color.WHITE);
        ordersTable.setGridColor(BORDER_SUBTLE);
        ordersTable.setShowGrid(true);

        // Hide the _fullId helper column
        ordersTable.getColumnModel().getColumn(4).setMinWidth(0);
        ordersTable.getColumnModel().getColumn(4).setMaxWidth(0);
        ordersTable.getColumnModel().getColumn(4).setWidth(0);

        JTableHeader oHeader = ordersTable.getTableHeader();
        oHeader.setFont(FONT_BODY_BOLD);
        oHeader.setBackground(new Color(35, 35, 60));
        oHeader.setForeground(ACCENT_CYAN);
        oHeader.setBorder(new LineBorder(BORDER_SUBTLE));

        DefaultTableCellRenderer oCenter = new DefaultTableCellRenderer();
        oCenter.setHorizontalAlignment(SwingConstants.CENTER);
        oCenter.setBackground(BG_CARD);
        oCenter.setForeground(TEXT_PRIMARY);
        for (int i = 0; i < 4; i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(oCenter);
        }

        // Double-click ??? show order detail dialog
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ordersTable.getSelectedRow();
                    if (row >= 0) {
                        String fullId = (String) ordersTableModel.getValueAt(row, 4);
                        String display = (String) ordersTableModel.getValueAt(row, 0);
                        String customer = (String) ordersTableModel.getValueAt(row, 1);
                        String total = (String) ordersTableModel.getValueAt(row, 2);
                        String date = (String) ordersTableModel.getValueAt(row, 3);
                        showOrderDetailDialog(fullId, display, customer, total, date);
                    }
                }
            }
        });

        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        ordersScroll.setBorder(new LineBorder(BORDER_SUBTLE));
        ordersScroll.getViewport().setBackground(BG_CARD);
        wrapper.add(ordersScroll, BorderLayout.CENTER);

        JPanel ordersActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        ordersActions.setBackground(BG_DARKEST);
        JButton refreshOrdersBtn = createAccentButton("Refresh", ACCENT_BLUE);
        refreshOrdersBtn.addActionListener(e -> refreshOrdersTable());
        ordersActions.add(refreshOrdersBtn);
        wrapper.add(ordersActions, BorderLayout.SOUTH);

        return wrapper;
    }

    /** Shows a styled dialog listing every item in the given order. */
    private void showOrderDetailDialog(String fullOrderId, String displayId,
            String customer, String total, String date) {
        JDialog dlg = new JDialog(this, "Order Details ??? " + displayId, true);
        dlg.setMinimumSize(new Dimension(580, 420));
        dlg.getContentPane().setBackground(BG_DARKEST);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(BG_DARKEST);
        main.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Header
        JPanel hdr = new JPanel(new GridLayout(2, 2, 8, 2));
        hdr.setBackground(BG_CARD);
        hdr.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel[] hdrLabels = {
                new JLabel("Customer: " + customer),
                new JLabel("Total: " + total),
                new JLabel("Date: " + date),
                new JLabel("Order: " + displayId)
        };
        for (JLabel lbl : hdrLabels) {
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(TEXT_SECONDARY);
            hdr.add(lbl);
        }
        main.add(hdr, BorderLayout.NORTH);

        // Items table
        DefaultTableModel itemModel = new DefaultTableModel(
                new String[] { "Product", "Qty", "Unit Price", "Line Total" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable itemTable = new JTable(itemModel);
        itemTable.setRowHeight(30);
        itemTable.setFont(FONT_BODY);
        itemTable.setBackground(BG_CARD);
        itemTable.setForeground(TEXT_PRIMARY);
        itemTable.setSelectionBackground(ACCENT_BLUE.darker());
        itemTable.setSelectionForeground(Color.WHITE);
        itemTable.setGridColor(BORDER_SUBTLE);
        itemTable.setShowGrid(true);
        itemTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        // Fixed-width columns for Qty / Price / Total; Product name gets remaining
        // space
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        itemTable.getColumnModel().getColumn(1).setMaxWidth(60);
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        itemTable.getColumnModel().getColumn(2).setMaxWidth(110);
        itemTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        itemTable.getColumnModel().getColumn(3).setMaxWidth(110);

        JTableHeader ih = itemTable.getTableHeader();
        ih.setFont(FONT_BODY_BOLD);
        ih.setBackground(new Color(35, 35, 60));
        ih.setForeground(ACCENT_CYAN);
        ih.setBorder(new LineBorder(BORDER_SUBTLE));

        DefaultTableCellRenderer iRight = new DefaultTableCellRenderer();
        iRight.setHorizontalAlignment(SwingConstants.RIGHT);
        iRight.setBackground(BG_CARD);
        iRight.setForeground(TEXT_PRIMARY);
        DefaultTableCellRenderer iCenter = new DefaultTableCellRenderer();
        iCenter.setHorizontalAlignment(SwingConstants.CENTER);
        iCenter.setBackground(BG_CARD);
        iCenter.setForeground(TEXT_PRIMARY);
        DefaultTableCellRenderer iLeft = new DefaultTableCellRenderer();
        iLeft.setHorizontalAlignment(SwingConstants.LEFT);
        iLeft.setBackground(BG_CARD);
        iLeft.setForeground(TEXT_PRIMARY);
        itemTable.getColumnModel().getColumn(0).setCellRenderer(iLeft);
        itemTable.getColumnModel().getColumn(1).setCellRenderer(iCenter);
        itemTable.getColumnModel().getColumn(2).setCellRenderer(iRight);
        itemTable.getColumnModel().getColumn(3).setCellRenderer(iRight);

        // Fetch items from DB
        String q = "SELECT p.name, oi.quantity, oi.price_at_purchase "
                + "FROM Order_Items oi JOIN Products p ON oi.product_id = p.id "
                + "WHERE oi.order_id = ? ORDER BY p.name";
        try (java.sql.PreparedStatement stmt = productDAO.getConnection().prepareStatement(q)) {
            stmt.setString(1, fullOrderId);
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                double unit = rs.getDouble("price_at_purchase");
                double lineTot = qty * unit;
                itemModel.addRow(new Object[] {
                        name,
                        qty,
                        String.format(Locale.US, "$%.2f", unit),
                        String.format(Locale.US, "$%.2f", lineTot)
                });
            }
        } catch (java.sql.SQLException ex) {
            itemModel.addRow(new Object[] { "Error loading items", "", "", "" });
        }

        JScrollPane iScroll = new JScrollPane(itemTable);
        iScroll.setBorder(new LineBorder(BORDER_SUBTLE));
        iScroll.getViewport().setBackground(BG_CARD);
        main.add(iScroll, BorderLayout.CENTER);

        // Close button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnRow.setBackground(BG_DARKEST);
        JButton closeBtn = createAccentButton("Close", new Color(80, 80, 100));
        closeBtn.addActionListener(e -> dlg.dispose());
        btnRow.add(closeBtn);
        main.add(btnRow, BorderLayout.SOUTH);

        dlg.add(main);
        dlg.pack();
        // Ensure it's at least the minimum, then centre on parent
        Dimension sz = dlg.getSize();
        dlg.setSize(Math.max(sz.width, 580), Math.min(Math.max(sz.height, 350), 620));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void refreshOrdersTable() {
        if (ordersTableModel == null)
            return;
        ordersTableModel.setRowCount(0);
        String query = "SELECT o.id, u.username, o.total_amount, o.timestamp "
                + "FROM Orders o JOIN Users u ON o.user_id = u.id ORDER BY o.timestamp DESC";
        try (java.sql.PreparedStatement stmt = productDAO.getConnection().prepareStatement(query);
                java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String fullId = rs.getString("id");
                String display = fullId.length() > 10 ? fullId.substring(0, 8) + ".." : fullId;
                ordersTableModel.addRow(new Object[] {
                        display,
                        rs.getString("username"),
                        String.format(Locale.US, "$%.2f", rs.getDouble("total_amount")),
                        rs.getTimestamp("timestamp").toString(),
                        fullId // hidden column ??? full UUID for detail lookup
                });
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
    }

    private JPanel buildAnalyticsTab() {
        JPanel analyticsWrapper = new JPanel(new BorderLayout());
        analyticsWrapper.setBackground(BG_DARKEST);
        analyticsWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        adminAnalyticsPanel = new DarkPieChartPanel();
        analyticsWrapper.add(adminAnalyticsPanel, BorderLayout.CENTER);

        return analyticsWrapper;
    }

    private void handleSaveProduct() {
        String id = adminIdField.getText().trim();
        String name = adminNameField.getText().trim();
        String priceStr = adminPriceField.getText().trim();
        int stock = (int) adminStockSpinner.getValue();

        if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all required fields.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a valid positive number.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (adminEditMode) {
            Product updated = new PhysicalProduct(id, name, price, stock, 1.0, "");
            updated.setImagePath(adminSelectedImagePath);
            productDAO.updateProduct(updated);
            JOptionPane.showMessageDialog(this, "Product \"" + name + "\" updated!");
        } else {
            if (productDAO.productExists(id)) {
                JOptionPane.showMessageDialog(this, "Product code \"" + id + "\" already exists.", "Duplicate",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Product newProduct = new PhysicalProduct(id, name, price, stock, 1.0, "");
            newProduct.setImagePath(adminSelectedImagePath);
            productDAO.addProduct(newProduct);
            JOptionPane.showMessageDialog(this, "Product \"" + name + "\" added!");
        }
        clearAdminForm();
        iconCache.clear();
        refreshAdminDashboardAsync();
    }

    private void clearAdminForm() {
        adminIdField.setText("");
        adminIdField.setEditable(true);
        adminNameField.setText("");
        adminPriceField.setText("");
        adminStockSpinner.setValue(1);
        adminSelectedImagePath = null;
        adminImagePreview.setIcon(null);
        adminImagePreview.setText("No image");
        adminEditMode = false;
    }

    private void loadImagePreview(String path) {
        if (path != null && new File(path).exists()) {
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(path).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
            adminImagePreview.setIcon(icon);
            adminImagePreview.setText("");
        } else {
            adminImagePreview.setIcon(null);
            adminImagePreview.setText("No image");
        }
    }

    private void refreshAdminDashboardAsync() {
        applyAdminFilter("All");
        refreshDiscountSettingsUI();
        refreshUsersTable();
        refreshOrdersTable();
    }

    private void applyAdminFilter(String typeName) {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                Map<String, Product> prods = productDAO.getAllProducts();

                // Units sold per product
                Map<String, Integer> soldMap = new java.util.HashMap<>();
                String sqlSold = "SELECT product_id, SUM(quantity) AS total_sold "
                        + "FROM Order_Items GROUP BY product_id";
                try (java.sql.PreparedStatement s = productDAO.getConnection().prepareStatement(sqlSold);
                        java.sql.ResultSet rs = s.executeQuery()) {
                    while (rs.next())
                        soldMap.put(rs.getString("product_id"), rs.getInt("total_sold"));
                } catch (java.sql.SQLException ex) {
                    System.err.println("Error fetching sold data: " + ex.getMessage());
                }

                // Total orders + total revenue
                int totalOrders = 0;
                double totalRevenue = 0.0;
                String sqlOrders = "SELECT COUNT(*) AS cnt, COALESCE(SUM(total_amount),0) AS rev FROM Orders";
                try (java.sql.PreparedStatement s = productDAO.getConnection().prepareStatement(sqlOrders);
                        java.sql.ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        totalOrders = rs.getInt("cnt");
                        totalRevenue = rs.getDouble("rev");
                    }
                } catch (java.sql.SQLException ex) {
                    System.err.println("Error fetching orders data: " + ex.getMessage());
                }

                return new Object[] { prods, soldMap, totalOrders, totalRevenue };
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] result = get();
                    Map<String, Product> dbProducts = (Map<String, Product>) result[0];
                    Map<String, Integer> soldMap = (Map<String, Integer>) result[1];
                    int totalOrders = (int) result[2];
                    double totalRevenue = (double) result[3];

                    adminTableModel.setRowCount(0);
                    for (Product p : dbProducts.values()) {
                        if ("All".equals(typeName) || p.getClass().getSimpleName().equals(typeName)) {
                            int sold = soldMap.getOrDefault(p.getId(), 0);
                            adminTableModel.addRow(new Object[] {
                                    p.getId(), p.getName(),
                                    String.format(Locale.US, "$%.2f", p.getPrice()),
                                    p.getStockQuantity(), sold
                            });
                        }
                    }
                    if (adminAnalyticsPanel != null)
                        adminAnalyticsPanel.setAnalyticsData(dbProducts, soldMap, totalOrders, totalRevenue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // UI HELPERS
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

    private JTextField createDarkTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleDarkField(field);
        return field;
    }

    private void styleDarkField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JButton createAccentButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hovered ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new ShoppingGUI().setVisible(true));
    }
}

/**
 * Full-featured Analytics Dashboard Panel.
 *
 * Sections (top ??? bottom):
 * 1. Title bar
 * 2. Six stat cards (2 rows ?? 3 cols): Products | Orders | Revenue
 * Stock | Inv.Val| LowStock
 * 3. Donut chart (stock VALUE share) + single-column legend
 * 4. Horizontal bar chart: Top 10 by Units Sold
 */
class DarkPieChartPanel extends JPanel {

    // ── data fields
    // ──────────────────────────────────────────────────────────────────────────────
    private Map<String, Product> products = new java.util.LinkedHashMap<>();
    private Map<String, Integer> soldMap = new java.util.HashMap<>();
    private int totalOrders = 0;
    private double totalRevenue = 0.0;
    private java.util.LinkedHashMap<String, double[]> dailyData = new java.util.LinkedHashMap<>();

    // ── color palette
    // ────────────────────────────────────────────────────────────────────────
    private static final Color[] PALETTE = {
            new Color(0, 150, 255), new Color(0, 230, 118), new Color(255, 69, 58),
            new Color(255, 159, 10), new Color(175, 82, 222), new Color(0, 210, 255),
            new Color(255, 214, 10), new Color(88, 86, 214), new Color(255, 55, 95),
            new Color(50, 215, 75), new Color(255, 100, 50), new Color(100, 200, 255)
    };

    // ── theme colors
    // ───────────────────────────────────────────────────────────────────────────
    private static final Color BG = new Color(28, 29, 50);
    private static final Color CARD_BG = new Color(38, 40, 65);
    private static final Color TEXT1 = new Color(235, 235, 245);
    private static final Color TEXT2 = new Color(140, 140, 165);
    private static final Color DIVIDER = new Color(55, 57, 85);

    public DarkPieChartPanel() {
        setBackground(BG);
    }

    /** Legacy compat — called from places that don't have full data yet. */
    public void setProducts(Map<String, Product> products) {
        this.products = products != null ? products : new java.util.LinkedHashMap<>();
        repaint();
    }

    /** Full data update — called from applyAdminFilter. */
    public void setAnalyticsData(Map<String, Product> products,
            Map<String, Integer> soldMap,
            int totalOrders, double totalRevenue,
            java.util.LinkedHashMap<String, double[]> dailyData) {
        this.products = products != null ? products : new java.util.LinkedHashMap<>();
        this.soldMap = soldMap != null ? soldMap : new java.util.HashMap<>();
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.dailyData = dailyData != null ? dailyData : new java.util.LinkedHashMap<>();
        repaint();
    }
    
    // Legacy overload for backward compatibility if called without daily data
    public void setAnalyticsData(Map<String, Product> products,
            Map<String, Integer> soldMap,
            int totalOrders, double totalRevenue) {
        setAnalyticsData(products, soldMap, totalOrders, totalRevenue, new java.util.LinkedHashMap<>());
    }

    // ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int W = getWidth();
        int H = getHeight();
        int P = 15; // outer padding

        // Full background
        g2.setColor(BG);
        g2.fillRect(0, 0, W, H);

        // ── 1. TITLE
        // ──────────────────────────────────────────────────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(TEXT1);
        String title = "Analytics Dashboard";
        FontMetrics tf = g2.getFontMetrics();
        g2.drawString(title, (W - tf.stringWidth(title)) / 2, P + tf.getAscent());
        int y = P + tf.getAscent() + 15;

        // ── 2. COMPUTE METRICS
        // ────────────────────────────────────────────────────────────────────────
        java.util.List<Product> pList = new java.util.ArrayList<>(products.values());
        int productCount = pList.size();
        int totalStock = pList.stream().mapToInt(Product::getStockQuantity).sum();
        double totalValue = pList.stream().mapToDouble(p -> p.getPrice() * p.getStockQuantity()).sum();
        long lowStock = pList.stream().filter(p -> p.getStockQuantity() <= 5).count();

        // ── 3. SIX STAT CARDS (1 row of 6 OR 2 rows of 3)
        // ──────────────────────────────────────────────────────────────────
        int cardCols = 3;
        int cardGap = 10;
        int cardW = (W - 2 * P - (cardCols - 1) * cardGap) / cardCols;
        int cardH = 65;

        Object[][] cards = {
                // {value, label, color}
                { String.valueOf(productCount), "Products", new Color(0, 150, 255) },
                { String.valueOf(totalOrders), "Orders", new Color(0, 230, 118) },
                { String.format(Locale.US, "$%.0f", totalRevenue), "Revenue", new Color(255, 214, 10) },
                { String.valueOf(totalStock), "Total Stock", new Color(0, 210, 255) },
                { String.format(Locale.US, "$%.0f", totalValue), "Inventory Value", new Color(175, 82, 222) },
                { String.valueOf(lowStock), "Low Stock", new Color(255, 69, 58) },
        };

        for (int ci = 0; ci < 6; ci++) {
            int col = ci % cardCols;
            int row = ci / cardCols;
            int bx = P + col * (cardW + cardGap);
            int by = y + row * (cardH + cardGap);

            // Card background
            g2.setColor(CARD_BG);
            g2.fill(new RoundRectangle2D.Double(bx, by, cardW, cardH, 12, 12));

            // Value
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(TEXT1);
            FontMetrics vfm = g2.getFontMetrics();
            String val = (String) cards[ci][0];
            g2.drawString(val, bx + (cardW - vfm.stringWidth(val)) / 2, by + 30);

            // Label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(TEXT2);
            FontMetrics lfm = g2.getFontMetrics();
            String lbl = (String) cards[ci][1];
            g2.drawString(lbl, bx + (cardW - lfm.stringWidth(lbl)) / 2, by + 50);
            
            // Underline / Bar indicator (instead of side)
            Color accent = (Color) cards[ci][2];
            g2.setColor(accent);
            g2.fill(new RoundRectangle2D.Double(bx + 20, by + cardH - 6, cardW - 40, 3, 3, 3));
        }

        y += 2 * (cardH + cardGap) + 10;
        
        // ── 4. DIVIDER
        g2.setColor(DIVIDER);
        g2.fillRect(P, y, W - 2 * P, 1);
        y += 15;

        // ── 5. REVENUE PERFORMANCE LINE CHART 
        // ─────────────────────────────────────────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(TEXT1);
        g2.drawString("Revenue Performance (Last 30 Days)", P, y + g2.getFontMetrics().getAscent());
        y += 25;
        
        int chartX = P;
        int chartY = y;
        int chartW = W - 2 * P;
        int chartH = (H - y - P) / 2 - 10;
        
        if (chartH < 50 || dailyData.isEmpty()) {
            g2.setColor(TEXT2);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            g2.drawString("Not enough data to display chart.", chartX + 10, chartY + 20);
            return;
        }

        // Draw Chart Background
        g2.setColor(new Color(35, 37, 60));
        g2.fill(new RoundRectangle2D.Double(chartX, chartY, chartW, chartH, 12, 12));
        
        int padding = 40;
        int graphW = chartW - padding * 2;
        int graphH = chartH - padding * 2;
        int graphX = chartX + padding;
        int graphY = chartY + padding;

        // Find max revenue
        double maxRev = 0;
        for (double[] data : dailyData.values()) {
            if (data[1] > maxRev) maxRev = data[1];
        }
        
        // Ensure maxRev is at least some number so we don't divide by 0 and scales cleanly
        maxRev = Math.max(maxRev, 100);
        // Round maxRev up to nearest 100 for grid
        maxRev = Math.ceil(maxRev / 100.0) * 100.0;
        
        // Draw grid and Y labels
        g2.setColor(new Color(255, 255, 255, 20)); // Faint lines
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        FontMetrics fm = g2.getFontMetrics();
        
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int lineY = graphY + graphH - (i * graphH / gridLines);
            g2.drawLine(graphX, lineY, graphX + graphW, lineY);
            
            String label = "$" + (int)(maxRev * i / gridLines);
            g2.setColor(TEXT2);
            g2.drawString(label, graphX - fm.stringWidth(label) - 5, lineY + fm.getAscent() / 2);
            g2.setColor(new Color(255, 255, 255, 20));
        }
        
        // Draw the line and gradient fill
        int numPoints = dailyData.size();
        int[] xPoints = new int[numPoints + 2];
        int[] yPoints = new int[numPoints + 2];
        
        java.util.List<String> keys = new java.util.ArrayList<>(dailyData.keySet());
        
        for (int i = 0; i < numPoints; i++) {
            String key = keys.get(i);
            double rev = dailyData.get(key)[1];
            
            xPoints[i + 1] = graphX + (numPoints > 1 ? (i * graphW / (numPoints - 1)) : graphW / 2);
            yPoints[i + 1] = graphY + graphH - (int)((rev / maxRev) * graphH);
            
            // X labels (draw a max of 7 labels)
            if (i % Math.max(1, numPoints / 7) == 0 || i == numPoints - 1) {
                // Try to make label MM-DD
                String[] parts = key.split("-");
                String label = parts.length == 3 ? parts[1] + "-" + parts[2] : key;
                g2.setColor(TEXT2);
                g2.drawString(label, xPoints[i + 1] - fm.stringWidth(label) / 2, graphY + graphH + 15);
            }
        }
        
        // close the polygon for the gradient fill
        xPoints[0] = xPoints[1];
        yPoints[0] = graphY + graphH;
        xPoints[numPoints + 1] = xPoints[numPoints];
        yPoints[numPoints + 1] = graphY + graphH;
        
        // Gradient Fill
        Color gradientStart = new Color(0, 210, 255, 80);
        Color gradientEnd = new Color(175, 82, 222, 10);
        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                graphX, graphY, gradientStart, 
                graphX, graphY + graphH, gradientEnd);
        g2.setPaint(gp);
        g2.fillPolygon(xPoints, yPoints, numPoints + 2);
        
        // Draw the main line
        g2.setPaint(null);
        g2.setColor(new Color(0, 210, 255)); // Bright Cyan/Blue line
        g2.setStroke(new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        for (int i = 1; i < numPoints; i++) {
            // Draw segment by segment to color blend if we want, but single color is fine
            // We use the interior points of the arrays
            g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
        }
        
        // Draw points
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        for (int i = 1; i <= numPoints; i++) {
            g2.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
            g2.setColor(new Color(175, 82, 222));
            g2.drawOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
            g2.setColor(Color.WHITE);
        }
        
        y += chartH + 25;

        // ── 6. BAR CHART: Top Products by Units Sold
        // ─────────────────────────────────────────────────────────────────────
        String barTitle = "Top Products by Units Sold";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(TEXT1);
        g2.drawString(barTitle, P, y + g2.getFontMetrics().getAscent());
        y += g2.getFontMetrics().getAscent() + 15;

        // Build top-10 list
        java.util.List<Product> barList = new java.util.ArrayList<>(pList);
        int totalSold = soldMap.values().stream().mapToInt(Integer::intValue).sum();
        if (totalSold > 0) {
            barList.sort((a, b) -> Integer.compare(
                    soldMap.getOrDefault(b.getId(), 0),
                    soldMap.getOrDefault(a.getId(), 0)));
        } else {
            barList.sort((a, b) -> Double.compare(
                    b.getPrice() * b.getStockQuantity(),
                    a.getPrice() * a.getStockQuantity()));
        }
        int barItems = Math.min(barList.size(), 10);

        int namW = 160; // fixed name column
        int valW = 44; // fixed value column
        int barArea = W - 2 * P - namW - valW - 8;
        int barH = 14;
        int barGap = 8;

        // Max value for scaling
        double maxBar = 1;
        for (int bi = 0; bi < barItems; bi++) {
            Product p = barList.get(bi);
            double v = totalSold > 0 ? soldMap.getOrDefault(p.getId(), 0)
                    : p.getPrice() * p.getStockQuantity();
            maxBar = Math.max(maxBar, v);
        }

        // Build a quick index from product id -> palette index
        java.util.Map<String, Integer> colorIdx = new java.util.HashMap<>();
        for (int i = 0; i < pList.size(); i++) colorIdx.put(pList.get(i).getId(), i);

        for (int bi = 0; bi < barItems; bi++) {
            Product p = barList.get(bi);
            double val = totalSold > 0 ? soldMap.getOrDefault(p.getId(), 0)
                    : p.getPrice() * p.getStockQuantity();
            int ci = colorIdx.getOrDefault(p.getId(), bi);
            Color bc = PALETTE[ci % PALETTE.length];

            int bx = P;
            int by = y + bi * (barH + barGap);

            // Name
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(TEXT2);
            FontMetrics nfm = g2.getFontMetrics();
            String nm = p.getName();
            while (nm.length() > 2 && nfm.stringWidth(nm) > namW - 4)
                nm = nm.substring(0, nm.length() - 1);
            g2.drawString(nm, bx, by + barH - 2);

            // Bar track (background)
            int barX = bx + namW;
            int barW = (int) Math.round((val / maxBar) * barArea);
            barW = Math.max(barW, 2);
            g2.setColor(new Color(50, 52, 80));
            g2.fillRoundRect(barX, by + 2, barArea, barH - 4, 6, 6);

            // Bar fill with gradient
            java.awt.GradientPaint gp2 = new java.awt.GradientPaint(
                    barX, by, bc, barX + barW, by, bc.darker());
            g2.setPaint(gp2);
            g2.fillRoundRect(barX, by + 2, barW, barH - 4, 6, 6);
            g2.setPaint(null);

            // Value label
            String valStr = totalSold > 0
                    ? String.valueOf((int) val)
                    : String.format(Locale.US, "$%.0f", val);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(TEXT1);
            g2.drawString(valStr, barX + barArea + 8, by + barH - 2);

            // Check if we'd overflow
            if (by + barH + barGap + 10 > H)
                break;
        }
    }
}
