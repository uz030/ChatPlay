package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UserSelectDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    // 기존 체크박스 리스트 → 사용자 선택 버튼 리스트로 변경
    private List<UserSelectButton> userButtons = new ArrayList<>();

    private List<String> selectedUsers = new ArrayList<>();
    private boolean isOk = false;

    public UserSelectDialog(ChatClientMain parent) {
        super(parent, "대화상대 초대", true);
        setSize(300, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        GradientTitlePanel titlePanel = new GradientTitlePanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel title = new JLabel("초대할 친구 선택", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        titlePanel.add(title, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);

   
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false); // 배경 투명 (중요)

      
        DefaultListModel<UserProfile> model = parent.getUserListModel();
        for (int i = 0; i < model.getSize(); i++) {
            UserProfile profile = model.getElementAt(i);

            UserSelectButton btn = new UserSelectButton(profile.getUsername());
            userButtons.add(btn);
            listPanel.add(btn);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setBorder(null);


        ScrollUtil.applyCustomScrollBar(scroll);
        add(scroll, BorderLayout.CENTER);

        JButton btnOk = new JButton("확인");

        btnOk.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnOk.setPreferredSize(new Dimension(300, 40));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(false);
        btnOk.setOpaque(true);

        Color baseColor = new Color(200, 210, 255);
        Color hoverColor = new Color(170, 185, 255);
        Color textColor = new Color(40, 50, 80);

        btnOk.setBackground(baseColor);
        btnOk.setForeground(textColor);

      
        btnOk.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnOk.setBackground(hoverColor);
                btnOk.setForeground(textColor); 
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnOk.setBackground(baseColor);
                btnOk.setForeground(textColor);
            }
        });
        
        btnOk.addActionListener(e -> {
            selectedUsers.clear();

            for (UserSelectButton btn : userButtons) {
                if (btn.isSelectedUser()) {
                    selectedUsers.add(btn.getUsername());
                }
            }

            if (!selectedUsers.isEmpty()) {
                isOk = true;
                dispose();
            }
        });

        add(btnOk, BorderLayout.SOUTH);

    }

    public boolean isOk() { return isOk; }

    public List<String> getSelectedUsers() { return selectedUsers; }
    
    class GradientTitlePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public GradientTitlePanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0xCF, 0xDC, 0xFC),
                    getWidth(), 0, new Color(0xC7, 0xD4, 0xF7)
            );

            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

}
