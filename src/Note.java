import java.awt.*;
import java.util.Arrays;

public class Note {
    private int[] lanes;
    private long time;
    private boolean hit = false;

    public Note(int[] lanes, long time) {
        this.lanes = lanes;
        this.time = time;
    }

    @Override
    public String toString() {
        String out = "";
        for (int lane : lanes) {
            out += lane;
            out += ",";
        }
        return out.substring(0, out.length()-1)  + "\n" + time;
    }

    public long getTime() {
        return time;
    }

    public int[] getLanes() {
        return lanes;
    }

    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    public static Color convert(int[] notes) {
        if (Arrays.equals(notes, new int[]{0, 0, 0})) {
            return null;
        } else if (Arrays.equals(notes, new int[]{1, 0, 0})) {
            return Color.RED;
        } else if (Arrays.equals(notes, new int[]{0, 1, 0})) {
            return Color.GREEN;
        } else if (Arrays.equals(notes, new int[]{0, 0, 1})) {
            return Color.BLUE;
        } else if (Arrays.equals(notes, new int[]{1, 1, 0})) {
            return Color.YELLOW;
        } else if (Arrays.equals(notes, new int[]{0, 1, 1})) {
            return Color.CYAN;
        } else if (Arrays.equals(notes, new int[]{1, 0, 1})) {
            return Color.magenta;
        } else if (Arrays.equals(notes, new int[]{1, 1, 1})) {
            return Color.white;
        }
        return null;
    }

    public static Color judgementConvert(String judgement) {
        return switch (judgement) {
            case "Great!" -> Color.decode("#0ab6ff");
            case "Good" -> Color.decode("#33c45f");
            case "Bad" -> Color.decode("#c49d33");
            case "Miss" -> Color.decode("#e32007");
            default -> null;
        };
    }
}
