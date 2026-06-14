import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Song {

    private final double starRating;
    private final File song;
    private final long length;
    private final String name;
    private final File chart;
    private final AudioFormat format;
    private static final ArrayList<Song> songs = new ArrayList<>();

    public Song(double sR, File s, String n, File chart) throws UnsupportedAudioFileException, IOException {
        this.starRating = sR;
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(s);
        this.format = audioInputStream.getFormat();
        this.song = s;
        this.length = audioInputStream.getFrameLength();
        this.name = n;
        this.chart = chart;
    }

    public static void grabSongs() {
        try (Stream<Path> paths = Files.walk(Paths.get("src/songs"))) {
            paths.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().equals("name.txt")).forEach(path -> {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    String title = reader.readLine();
                    if (title == null || title.trim().isEmpty()) {
                        title = "Untitled: " + path.getFileName().toString();
                    } else {
                        title = title.trim();
                    }

                    // Get the parent subfolder (e.g., src/songs/dangerouswoman)
                    Path subfolderPath = path.getParent();
                    File audioFile = null;

                    // FIX: Scan the subfolder to find any file ending with .wav
                    try (Stream<Path> subFiles = Files.list(subfolderPath)) {
                        Path wavPath = subFiles.filter(f -> f.toString().toLowerCase().endsWith(".wav")).findFirst().orElse(null);
                        if (wavPath != null) {
                            audioFile = wavPath.toFile();
                        }
                    }

                    Path chartPath = subfolderPath.resolve("chart/mapdata.txt");
                    File chart = chartPath.toFile();

                    // Check if we successfully found a WAV file
                    if (audioFile != null && audioFile.exists()) {
                        double mockRating = 5.0; // Place your star rating logic here
                        Song newSong = new Song(mockRating, audioFile, title, chart);
                        songs.add(newSong);
                    } else {
                        System.err.println("No WAV file found inside folder: " + subfolderPath);
                    }

                } catch (IOException | UnsupportedAudioFileException e) {
                    System.err.println("Could not process audio data for: " + path.getFileName());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return (getName() + ", " + getLength());
    }

    public static ArrayList<Song> getSongs() {
        return songs;
    }

    public double getStarRating() {
        return starRating;
    }

    public String getName() {
        return name;
    }

    public String getLength() {
        double durationInSeconds = (length / format.getFrameRate());

        int minutes = (int) (durationInSeconds / 60);
        int seconds = (int) (durationInSeconds % 60);

        return String.format("Duration: %d:%02d", minutes, seconds);
    }

    public File getSong() {
        return song;
    }

    public File getChart() {
        return chart;
    }
}
