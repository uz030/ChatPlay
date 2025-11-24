package chatPlay;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

@SuppressWarnings("unused")
public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatClientMain parentFrame;
    private JTextField txtUserName;
    private JLabel selectedProfileImageLabel;
    private ImageIcon selectedIcon;
    private ImageIcon defaultProfileIcon;

    public MainPanel(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setOpaque(false);

        try {
            defaultProfileIcon = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
            selectedIcon = defaultProfileIcon;
        } catch (Exception e) {
            defaultProfileIcon = new ImageIcon();
            selectedIcon = defaultProfileIcon;
        }

        selectedProfileImageLabel = new JLabel(getScaledIcon(selectedIcon, 115, 115));
        selectedProfileImageLabel.setBounds(135, 95, 115, 115);
        add(selectedProfileImageLabel);

        JLabel defaultOption = new JLabel(getScaledIcon(defaultProfileIcon, 60, 60));
        defaultOption.setBounds(125, 220, 60, 60);
        defaultOption.setCursor(new Cursor(Cursor.HAND_CURSOR));
        defaultOption.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedIcon = defaultProfileIcon;
                selectedProfileImageLabel.setIcon(getScaledIcon(selectedIcon, 115, 115));
            }
        });
        add(defaultOption);

        JLabel addOption = new JLabel("[+]");
        try {
            ImageIcon addIcon = new ImageIcon(getClass().getResource("/images/add.png"));
            addOption.setIcon(getScaledIcon(addIcon, 60, 60));
        } catch (Exception e) {}
        addOption.setBounds(205, 220, 60, 60);
        addOption.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addOption.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
                if (chooser.showOpenDialog(MainPanel.this) == JFileChooser.APPROVE_OPTION) {
                    selectedIcon = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
                    selectedProfileImageLabel.setIcon(getScaledIcon(selectedIcon, 115, 115));
                }
            }
        });
        add(addOption);

        JLabel lbl = new JLabel("이름을 입력해주세요.");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lbl.setBounds(100, 290, 200, 33);
        add(lbl);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(120, 325, 150, 33);
        add(txtUserName);

        RoundedButton btnConnect = new RoundedButton("회원가입", new Color(200, 210, 255), new Color(170, 185, 255), new Color(40, 50, 80));
        btnConnect.setBounds(93, 380, 205, 50);
        btnConnect.addActionListener(e -> {
            String name = txtUserName.getText().trim();
            if (!name.isEmpty()) parentFrame.connectToServer(name, selectedIcon);
        });
        add(btnConnect);
    }

    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) return new ImageIcon();
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(226, 230, 255), getWidth(), getHeight(), new Color(245, 245, 255)));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}