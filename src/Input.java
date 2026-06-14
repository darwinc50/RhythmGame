import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Input {
    private long timePressed;
    private ArrayList<Integer> keyCodes;
    private boolean checked = false;

    public Input(long timePressed, ArrayList<Integer> keyCodes) {
        this.timePressed = timePressed;
        this.keyCodes = keyCodes;
    }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }

    public ArrayList<Integer> getKeyCodes() {
        return keyCodes;
    }

    public void addChordInput(Integer key) {
        keyCodes.add(key);
    }


    public long getTimePressed() {
        return timePressed;
    }

    @Override
    public String toString() {
        String keyString = "";
        for (Integer key : keyCodes) {
            keyString += KeyEvent.getKeyText(key);
        }
        return keyString;
    }

    public static int[] convert(ArrayList<Integer> pressCodes) {
        int[] codes = {0,0,0,0,0,0};
        if (pressCodes.contains(KeyEvent.VK_S)) {
            codes[0] = 1;
        }
        if (pressCodes.contains(KeyEvent.VK_D)) {
            codes[1] = 1;
        }
        if (pressCodes.contains(KeyEvent.VK_F)) {
            codes[2] = 1;
        }
        if (pressCodes.contains(KeyEvent.VK_J)) {
            codes[3] = 1;
        }
        if (pressCodes.contains(KeyEvent.VK_K)) {
            codes[4] = 1;
        }
        if (pressCodes.contains(KeyEvent.VK_L)) {
            codes[5] = 1;
        }
        return codes;
    }
}
