package weather;

public class WeatherTestMain {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new WeatherFrame().setVisible(true);
            }
        });
    }
}
