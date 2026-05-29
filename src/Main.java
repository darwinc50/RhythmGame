import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("sick pain in the ass game that Edward wanted to make we are dead-butt so screwed lol");
        frame.setIconImage(new ImageIcon("src/spinnin.png").getImage()); //change app icon image
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        PlaySong dangerousWoman = new PlaySong(new File("src/songs/dangerouswoman.wav"));
        //PlaySong takeAHint = new PlaySong(new File("src/songs/takeahint.wav"));
        // create a DisplayPanel object
        DisplayWindow panel = new DisplayWindow(dangerousWoman,frame);
        frame.add(panel);
        frame.setVisible(true);

    }
}
