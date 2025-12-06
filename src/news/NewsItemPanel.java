package news;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class NewsItemPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;

    public NewsItemPanel(NewsItem item) {
        setLayout(new BorderLayout(0, 5)); 
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 5, 10), 
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1), 
                BorderFactory.createEmptyBorder(10, 10, 10, 10) 
            )
        ));
        setBackground(Color.WHITE);

        // 1. 제목 
        JTextArea titleArea = new JTextArea(item.getTitle());
        titleArea.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleArea.setLineWrap(true);        // 자동 줄바꿈 켜기
        titleArea.setWrapStyleWord(true);   // 단어 단위로 끊기
        titleArea.setEditable(false);       // 편집 불가
        titleArea.setOpaque(false);         // 배경 투명
        titleArea.setFocusable(false);      // 포커스 방지 (클릭 시 커서 깜빡임 제거)
        
        // 2. 내용 (자동 줄바꿈을 위해 JTextArea 사용)
        JTextArea descArea = new JTextArea(item.getDescription());
        descArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        descArea.setForeground(Color.GRAY);
        descArea.setLineWrap(true);         // 자동 줄바꿈 켜기
        descArea.setWrapStyleWord(true);    // 단어 단위로 끊기
        descArea.setEditable(false);        // 편집 불가
        descArea.setOpaque(false);          // 배경 투명
        descArea.setFocusable(false);       // 포커스 방지

        add(titleArea, BorderLayout.NORTH);
        add(descArea, BorderLayout.CENTER);
        
        // 3. 클릭 시 브라우저 열기 (패널 전체에 이벤트 걸기)
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(item.getLink()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // 마우스 올렸을 때 효과
            @Override
            public void mouseEntered(MouseEvent e) {
                setOpaque(true); // 배경색 보이게
                setBackground(new Color(245, 248, 255)); // 연한 하늘색 하이라이트
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setOpaque(false); // 다시 투명하게
                setBackground(Color.WHITE);
                repaint();
            }
        };

        // 패널 자체와 내부 텍스트 영역 모두에 리스너 추가
        this.addMouseListener(clickListener);
        titleArea.addMouseListener(clickListener);
        descArea.addMouseListener(clickListener);
        
        // 마우스 커서 손가락 모양 설정
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        titleArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
        descArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}