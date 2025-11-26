package chatPlay;

import javax.swing.*;

public class ScrollUtil {

    public static void applyCustomScrollBar(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI(null));
        scroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI(null));
        scroll.setBorder(null);
    }
}
