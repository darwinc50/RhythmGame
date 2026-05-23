import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.event.ActionListener;
import java.io.File;

public class PlaySong {
    private File song;
    private Clip clip;
    private long pausePos;

    public PlaySong(File song) {
        this.song = song;
    }

    public ActionListener playSound() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(song);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopSound() {
        if (clip != null && clip.isRunning()) {
            pausePos=clip.getMicrosecondPosition();
            clip.stop();
        }
    }

    public void resumeSound() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(pausePos);
            clip.start();
        }
    }
}
