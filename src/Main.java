import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("sick pain in the ass game that edward wanted to make we are deadbutt so screwed lol");
        frame.setIconImage(new ImageIcon("src/spinnin.png").getImage()); //change app icon image
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        PlaySong dangerousWoman = new PlaySong(new File("src/songs/dangerouswoman.wav"));
        PlaySong takeAHint = new PlaySong(new File("src/songs/takeahint.wav"));
        // create a DisplayPanel object
        DisplayWindow panel = new DisplayWindow(dangerousWoman,frame);

        // add it to the frame
        frame.add(panel);

        // Remove window borders and title bar
        frame.setUndecorated(true);

        // Get the default screen device
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Set the frame to full-screen mode
        device.setFullScreenWindow(frame);

        frame.setVisible(true);

        // call setVisible after everything else
        frame.setVisible(true);

    }
}
