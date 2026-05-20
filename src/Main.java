import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("super awesome game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2000, 1100);
        frame.setLocationRelativeTo(null);

        // create a DisplayPanel object
        DisplayWindow panel = new DisplayWindow();

        // add it to the frame
        frame.add(panel);

        // call setVisible after everything else
        frame.setVisible(true);
    }
}
