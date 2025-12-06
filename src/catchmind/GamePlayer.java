package catchmind;

public class GamePlayer {
    private String username;
    private int score;
    private boolean isDrawer;
    private boolean isParticipating; // 게임 참여 여부
    
    public GamePlayer(String username) {
        this.username = username;
        this.score = 0;
        this.isDrawer = false;
        this.isParticipating = false;
    }
    
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public boolean isDrawer() { return isDrawer; }
    public void setDrawer(boolean drawer) { this.isDrawer = drawer; }
    public boolean isParticipating() { return isParticipating; }
    public void setParticipating(boolean participating) { this.isParticipating = participating; }
}