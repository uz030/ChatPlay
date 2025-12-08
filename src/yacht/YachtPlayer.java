package yacht;

public class YachtPlayer {
    private String name;
    private int[] scores; // 0~11: Categories, 12: Bonus, 13: Total
    private boolean[] isCategoryFilled;

    public YachtPlayer(String name) {
        this.name = name;
        this.scores = new int[14]; 
        this.isCategoryFilled = new boolean[12];
    }

    public String getName() { return name; }
    public int getScore(int index) { return scores[index]; }
    public void setScore(int index, int score) {
        scores[index] = score;
        if (index < 12) isCategoryFilled[index] = true;
        updateTotal();
    }
    public boolean isFilled(int index) { return isCategoryFilled[index]; }

    private void updateTotal() {
        int subTotal = 0;
        for (int i = 0; i < 6; i++) subTotal += scores[i];
        
        // 보너스 점수 (상단 63점 이상 시 35점)
        scores[12] = (subTotal >= 63) ? 35 : 0;
        
        int total = scores[12];
        for (int i = 0; i < 12; i++) total += scores[i];
        scores[13] = total;
    }
    
    public int getTotalScore() { return scores[13]; }
}