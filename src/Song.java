import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Song {

    private final double starRating;
    private final File song;
    private final long length;
    private final String name;

    public Song(double sR, File s, long l, String n) {
        starRating = sR;
        song = s;
        length = l;
        this.name = n;
    }

    public static void grabSongs() {
        try (Stream<Path> paths = Files.walk(Paths.get("src/songs"))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                        // Process each file
                        System.out.println("Processing: " + path.getFileName());
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}