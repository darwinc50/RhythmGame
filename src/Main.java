import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("sick pain in the ass game that Edward wanted to make we are dead-butt so screwed lol");
        frame.setIconImage(new ImageIcon("src/background/spinnin.png").getImage()); //change app icon image
        Song.grabSongs();
        ArrayList<Song> songs = Song.getSongs();
        ArrayList<PlaySong> playsongs = new ArrayList<>();
        for (Song track : songs) {
            System.out.println("Title: " + track.getName() + " | " + track.getLength());

        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);
        PlaySong dangerousWoman = new PlaySong(new File("src/songs/Funny/dangerouswoman.wav"));
        PlaySong takeAHint = new PlaySong(new File("src/songs/Funny/takeahint.wav"));
        PlaySong Funny = new PlaySong(new File("src/songs/Funny/Funny.wav"));

        DisplayWindow panel = new DisplayWindow(Funny,frame);
        frame.add(panel);
        frame.setVisible(true);
    }
}
