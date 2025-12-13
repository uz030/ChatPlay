package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

/**
 * 채팅방 목록 패널
 * 채팅방 목록 표시 및 새 채팅방 생성 기능 제공
 */
public class ChatPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ChatClientMain parent;

    /**
     * 채팅 패널 생성자
     * @param parent 부모 클라이언트 프레임
     */
    public ChatPanel(ChatClientMain parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setOpaque(false);

        // ========== 상단 패널 (채팅방 생성 버튼) ==========
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(207, 220, 252),
                        getWidth(), 0, new Color(199, 212, 247)
                );

                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        top.setOpaque(false);

        ImageIcon iconDefault = loadIcon("/images/chatPlus.png");
        ImageIcon iconHover   = loadIcon("/images/chatPlus_hover.png");
        ImageIcon iconPressed = loadIcon("/images/chatPlus_pressed.png");

        JButton btnCreate = new JButton();
        
        if (iconDefault != null) btnCreate.setIcon(iconDefault);
        if (iconHover != null) btnCreate.setRolloverIcon(iconHover);
        if (iconPressed != null) btnCreate.setPressedIcon(iconPressed);

        btnCreate.setBorderPainted(false);
        btnCreate.setContentAreaFilled(false);
        btnCreate.setFocusPainted(false);
        btnCreate.setOpaque(false);
        btnCreate.setToolTipText("채팅방 생성");
        btnCreate.addActionListener(e -> createRoom());
        btnCreate.setMargin(new Insets(0, 0, 0, 0));
        btnCreate.setPreferredSize(new Dimension(30, 30)); 
        
        top.add(btnCreate);
        add(top, BorderLayout.NORTH);

        // ========== 채팅방 목록 ==========
        JList<ChatRoomData> list = new JList<>(parent.getRoomListModel());
        list.setFixedCellHeight(50);
        // 더블클릭 시 채팅방 열기
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ChatRoomData room = list.getSelectedValue();
                    if (room != null) parent.enterChatRoom(room);
                }
            }
        });
        
        // 스크롤 패널 설정
        JScrollPane scroll = new JScrollPane(list);
        ScrollUtil.applyCustomScrollBar(scroll);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * 새 채팅방 생성
     * 사용자 선택 다이얼로그를 열고 선택된 사용자들과 방 생성
     */
    private void createRoom() {
        // 1단계: 사용자 선택 다이얼로그 열기
    	UserSelectDialog dialog = new UserSelectDialog(parent, parent, true);
        dialog.setVisible(true);

        if (dialog.isOk()) {
            // 2단계: 선택된 사용자 목록 가져오기
            List<String> selected = dialog.getSelectedUsers();
            
            // 3단계: 방 이름 결정
            String inputName = dialog.getRoomNameInput();
            String roomName;

            if (inputName != null && !inputName.isEmpty()) {
                // 사용자가 입력한 이름 사용 (공백은 언더스코어로 변환)
                roomName = inputName.replace(" ", "_"); 
            } else {
                // 입력 없으면 자동 생성 (나,친구1,친구2...)
                roomName = parent.getMyProfile().getUsername();
                for (String u : selected) roomName += "," + u;
            }

            // 4단계: 서버에 방 생성 요청 전송
            StringBuilder cmd = new StringBuilder("/makeroom " + roomName);
            for (String u : selected) cmd.append(" ").append(u);
            
            try { 
                parent.getDos().writeUTF(cmd.toString()); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 이미지 아이콘 로드
     * @param path 이미지 경로
     * @return 이미지 아이콘
     */
    private ImageIcon loadIcon(String path) {
        URL imgUrl = getClass().getResource(path);
        if (imgUrl == null) {
            return null;
        }
        return new ImageIcon(imgUrl);
    }
}