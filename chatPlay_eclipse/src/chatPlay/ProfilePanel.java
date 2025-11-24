package chatPlay;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfilePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public ProfilePanel(ChatClientMain parent) {
        setLayout(new BorderLayout());
        setOpaque(false);
        UserProfile myProfile = parent.getMyProfile();

        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileBox.setOpaque(false);
        profileBox.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        profileBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new ProfileDetailDialog(parent, myProfile, true).setVisible(true);
                removeAll(); add(new ProfilePanel(parent)); revalidate();
            }
        });

        JLabel imgLabel = new JLabel(getScaledIcon(myProfile.getIcon(), 60, 60));
        JLabel nameLabel = new JLabel(myProfile.getUsername());
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        profileBox.add(imgLabel); profileBox.add(nameLabel);
        add(profileBox, BorderLayout.NORTH);

        JList<UserProfile> friendList = new JList<>(parent.getUserListModel());
        friendList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setOpaque(isSelected);
                return this;
            }
        });
        friendList.setOpaque(false);
        ((JComponent)friendList.getCellRenderer()).setOpaque(false);
        friendList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    UserProfile friend = friendList.getSelectedValue();
                    if (friend != null) new ProfileDetailDialog(parent, friend, false).setVisible(true);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(friendList);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }
    
    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if(src==null) return new ImageIcon();
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(230, 225, 255), 0, getHeight(), Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}