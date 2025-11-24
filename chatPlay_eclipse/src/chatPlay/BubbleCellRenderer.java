package chatPlay;

import javax.swing.*;
import java.awt.*;

public class BubbleCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {
    
   
	private static final long serialVersionUID = 1L;
	private JLabel senderLabel;
    private JTextArea contentArea;
    private JPanel bubblePanel;

    public BubbleCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(false); 
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 

        // 1. 보낸 사람 이름 (상대방일 때만 표시)
        senderLabel = new JLabel();
        senderLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        senderLabel.setForeground(Color.GRAY);
        senderLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 0));

        // 2. 말풍선 본체 
        contentArea = new JTextArea();
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setOpaque(false); 
        contentArea.setEditable(false);
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        contentArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); 

        // 말풍선 배경을 그릴 패널
        bubblePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        bubblePanel.setOpaque(false);
        bubblePanel.add(contentArea, BorderLayout.CENTER);

        add(senderLabel, BorderLayout.NORTH);
        add(bubblePanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage msg, int index, boolean isSelected, boolean cellHasFocus) {
        // 1. 내용 설정
        contentArea.setText(msg.getContent());
        
        // 2. 텍스트 크기에 따른 버블 크기 계산
        int maxWidth = list.getWidth() - 100; 
        if (maxWidth < 100) maxWidth = 100; 
        
        contentArea.setSize(maxWidth, Short.MAX_VALUE); 
        
        // 3. 내 메시지 vs 상대 메시지 스타일 분기
        if (msg.isMine()) {
            senderLabel.setVisible(false); 
            bubblePanel.setBackground(new Color(0, 149, 246)); 
            contentArea.setForeground(Color.WHITE);
            
            JPanel rightAlignPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightAlignPanel.setOpaque(false);
            rightAlignPanel.add(bubblePanel);
            
            removeAll();
            add(rightAlignPanel, BorderLayout.CENTER);
            
        } else {
            senderLabel.setVisible(true);
            senderLabel.setText(msg.getSender());
            bubblePanel.setBackground(new Color(239, 239, 239)); 
            contentArea.setForeground(Color.BLACK);
            
            JPanel leftAlignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            leftAlignPanel.setOpaque(false);
            leftAlignPanel.add(bubblePanel);
            
            removeAll();
            add(senderLabel, BorderLayout.NORTH); 
            add(leftAlignPanel, BorderLayout.CENTER);
        }
        
        return this;
    }
}