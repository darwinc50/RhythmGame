import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;

public class GameWindow {

    public GameWindow(int width, int height, String title) {
        // 1. Create the window frame
        JFrame frame = new JFrame(title);

        // 2. Set the window size
        frame.setPreferredSize(new Dimension(width, height));
        frame.setMaximumSize(new Dimension(width, height));
        frame.setMinimumSize(new Dimension(width, height));

        // 3. Ensure the program exits completely when the window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 4. Center the window on the screen
        frame.setLocationRelativeTo(null);

        // 5. Disable resizing to keep a fixed aspect ratio for your game
        frame.setResizable(false);

        // 6. Add a canvas for rendering graphics
        Canvas canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        frame.add(canvas);

        // 7. Adjust the frame to fit the canvas, then make the window visible
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Launch the window
        new GameWindow(1920, 1080, "My Awesome Java Game");
    }
}