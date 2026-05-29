import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Song {

    private final double starRating;
    private final File song;
    private final long length;
    private final String name;
    private final AudioFormat format;
    private static ArrayList<Song> songs = new ArrayList<>();

    public Song(double sR, File s, String n) throws UnsupportedAudioFileException, IOException {
        this.starRating = sR;
        // This will crash if 's' is a .txt file instead of a valid audio file (.wav)
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(s);
        this.format = audioInputStream.getFormat();
        this.song = s;
        this.length = audioInputStream.getFrameLength();
        this.name = n;
        audioInputStream.close(); // Good practice to close stream
    }

    public static void main (String[] args) {


        try (Stream<Path> paths = Files.walk(Paths.get("src/songs"))) {
            paths.filter(Files::isRegularFile).filter(path -> {
                try {
                    String type = Files.probeContentType(path);
                    return type != null && type.startsWith("text/");
                } catch (IOException e) {
                    return false;
                }
            }).forEach(path -> {
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
                        Path wavPath = subFiles
                                .filter(f -> f.toString().toLowerCase().endsWith(".wav"))
                                .findFirst()
                                .orElse(null);

                        if (wavPath != null) {
                            audioFile = wavPath.toFile();
                        }
                    }

                    // Check if we successfully found a WAV file
                    if (audioFile != null && audioFile.exists()) {
                        double mockRating = 5.0; // Place your star rating logic here
                        Song newSong = new Song(mockRating, audioFile, title);
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

        // Output results
        System.out.println("\n--- Loaded Songs (" + songs.size() + ") ---");
        for (Song track : songs) {
            System.out.println("Title: " + track.name + " | " + track.getLength());
        }
    }


    public static ArrayList<String> getFileData(String fileName) {
        ArrayList<String> fileData = new ArrayList<String>();
        try {
            File f = new File(fileName);
            Scanner s = new Scanner(f);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (!line.equals(""))
                    fileData.add(line);
            }
            s.close(); // Good practice to close scanner
            return fileData;
        }
        catch (FileNotFoundException e) {
            return fileData;
        }
    }

    public String getLength() {
        double durationInSeconds =(length / format.getFrameRate());
        return ("Duration: " + durationInSeconds + " seconds");
    }
}
