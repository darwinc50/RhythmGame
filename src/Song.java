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
                        // Read only the first line to grab the title
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            String title = reader.readLine();
                            if (title != null && !title.trim().isEmpty()) {
                                System.out.println("Song Title: " + title.trim());
                            } else {
                                System.out.println("Untitled (Empty File): " + path.getFileName());
                            }
                        } catch (IOException e) {
                            System.err.println("Could not read file: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
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
            return fileData;
        }
        catch (FileNotFoundException e) {
            return fileData;
        }
    }

}