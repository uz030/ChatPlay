package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Emoji extends JPanel {

    private ChatClientMain client;
    private ChatRoomData roomData;
    private ChatRoomPanel ownerPanel; 

  
    public Emoji(Component owner, ChatClientMain client, ChatRoomData roomData) {
        
        if (owner instanceof ChatRoomPanel) {
            this.ownerPanel = (ChatRoomPanel) owner;
        }
        this.client = client;
        this.roomData = roomData;
        
        setLayout(new BorderLayout()); 
        
        setPreferredSize(new Dimension(350, 150)); 
        
        JPanel emojiPanel = new JPanel();
        emojiPanel.setLayout(new BoxLayout(emojiPanel, BoxLayout.X_AXIS));
        emojiPanel.setBackground(Color.WHITE); 

        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        loadIcons(emojiPanel);
    }
    
    private void loadIcons(JPanel emojiPanel) {

        File folder = new File("src/images/icon");
        if (!folder.exists()) {
            System.out.println("⚠️ 폴더 없음: src/images/icon");
            return;
        }

        File[] files = folder.listFiles((d, n) -> n.endsWith(".png"));
        if (files == null) return;

        for (File f : files) {

            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);

            JButton btn = new JButton(new ImageIcon(scaled));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                sendEmoji(f.getName());
            });

            emojiPanel.add(btn);
            emojiPanel.add(Box.createHorizontalStrut(10));
        }
    }

    private void sendEmoji(String fileName) {
        try {
            client.getDos().writeUTF(
                "/roommsg " + roomData.getRoomId() +
                " @images " + fileName
            );
            
           
            if (ownerPanel != null) {
                ownerPanel.closeEmoji(); 
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}