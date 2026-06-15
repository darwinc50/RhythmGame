import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("sick pain in the ass game that Edward wanted to make we are dead-butt so screwed lol");
        frame.setIconImage(new ImageIcon("src/background/spinnin.png").getImage());
        Song.grabSongs();
        ArrayList<Song> songs = Song.getSongs();

        for (Song track : songs) {
            System.out.println("Title: " + track.getName() + " | " + track.getLength());

        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        DisplayWindow panel = new DisplayWindow(frame);
        frame.add(panel);
        frame.setVisible(true);
    }
}
