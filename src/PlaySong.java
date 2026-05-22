import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class PlaySong {
    private File song;

    public PlaySong(File song) {
        this.song = song;
    }

    public void playSound() {
        try {
            // Place your .wav file in the resources folder (e.g., src/main/resources/sound.wav
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(song);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSound() {
        try {
            // Place your .wav file in the resources folder (e.g., src/main/resources/sound.wav
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(song);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
