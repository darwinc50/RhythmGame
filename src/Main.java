import javax.swing.JFrame;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("awesome femboy game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);

        // create a DisplayPanel object
        DisplayWindow panel = new DisplayWindow();

        // add it to the frame
        frame.add(panel);

        // Remove window borders and title bar
        frame.setUndecorated(true);

        // Get the default screen device
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        // Set the frame to full-screen mode
        device.setFullScreenWindow(frame);

        frame.setVisible(true);

        // call setVisible after everything else
        frame.setVisible(true);

        PlaySong takeAHint = new PlaySong(new File("src/songs/takeahint.wav"));
        PlaySong dangerousWoman = new PlaySong(new File("src/songs/dangerouswoman.wav"));
        dangerousWoman.playSound();
        try {
            Thread.sleep(5000);
            dangerousWoman.stopSound();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
