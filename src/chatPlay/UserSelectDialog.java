package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UserSelectDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private List<UserSelectButton> userButtons = new ArrayList<>();
    private List<String> selectedUsers = new ArrayList<>();
    private boolean isOk = false;
    
    // 방 이름 입력 필드
    private JTextField roomNameField;

    // 기존 생성자 유지 (방 만들기 용)
    public UserSelectDialog(ChatClientMain parent) {
        this(parent, parent, true);
    }
    
    // 기존 생성자 유지
    public UserSelectDialog(Window owner, ChatClientMain parent) {
        this(owner, parent, true);
    }

    // ✅ 새로운 생성자: showHeader 로 모드 구분
    public UserSelectDialog(Window owner, ChatClientMain parent, boolean showHeader) {
        super(owner, showHeader ? "채팅방 생성" : "참여자 초대", ModalityType.APPLICATION_MODAL);
        setSize(300, showHeader ? 500 : 430);
        setLocationRelativeTo(owner);  
        setLayout(new BorderLayout());
        
        // --- 1. 상단 헤더 (옵션) ---
        if (showHeader) {
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);

            GradientTitlePanel titlePanel = new GradientTitlePanel();
            titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JLabel title = new JLabel("새로운 채팅방", SwingConstants.CENTER);
            title.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            titlePanel.add(title, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.setBackground(Color.WHITE);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            JLabel lblInput = new JLabel("방 이름 (선택사항)");
            lblInput.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            lblInput.setForeground(Color.GRAY);

            roomNameField = new JTextField();
            roomNameField.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            roomNameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            inputPanel.add(lblInput, BorderLayout.NORTH);
            inputPanel.add(roomNameField, BorderLayout.CENTER);

            headerPanel.add(titlePanel, BorderLayout.NORTH);
            headerPanel.add(inputPanel, BorderLayout.CENTER);

            add(headerPanel, BorderLayout.NORTH);
        }

        // --- 2. 리스트 패널 ---
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(true); 
        listPanel.setBackground(Color.WHITE);

        DefaultListModel<UserProfile> model = parent.getUserListModel();
        for (int i = 0; i < model.getSize(); i++) {
            UserProfile profile = model.getElementAt(i);
            UserSelectButton btn = new UserSelectButton(profile);
            userButtons.add(btn);
            listPanel.add(btn);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setOpaque(true); 
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ScrollUtil.applyCustomScrollBar(scroll);
        add(scroll, BorderLayout.CENTER);

        // --- 3. 하단 버튼 ---
        String btnLabel = showHeader ? "만들기" : "초대하기";  // 버튼 이름 자동 변경

        JButton btnOk = new JButton(btnLabel);
        btnOk.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnOk.setPreferredSize(new Dimension(300, 50));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(false);
        btnOk.setOpaque(true);

        Color baseColor = new Color(100, 149, 237); 
        Color hoverColor = new Color(80, 120, 220);
        Color textColor = Color.WHITE;

        btnOk.setBackground(baseColor);
        btnOk.setForeground(textColor);

        btnOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnOk.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnOk.setBackground(baseColor);
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
            } else {
                JOptionPane.showMessageDialog(this, "대화 상대를 한 명 이상 선택해주세요.");
            }
        });

        add(btnOk, BorderLayout.SOUTH);
    }

    public boolean isOk() { return isOk; }
    public List<String> getSelectedUsers() { return selectedUsers; }
    
    public String getRoomNameInput() {
        return roomNameField != null ? roomNameField.getText().trim() : "";
    }
    
    class GradientTitlePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        public GradientTitlePanel() {
            setOpaque(false); setLayout(new BorderLayout());
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, new Color(0xCF, 0xDC, 0xFC), getWidth(), 0, new Color(0xC7, 0xD4, 0xF7));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
