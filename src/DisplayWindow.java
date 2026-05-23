import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;



public class DisplayWindow extends JPanel implements MouseListener, KeyListener, ActionListener {
    private static double accuracy = 0.0; //yes this works correctly i checked it goes to #.##
    private int score;
    private BufferedImage background;
    private boolean[] pressedKeys;
    private Timer timer;
    private boolean gameOver;
    private PlaySong currentSong;
    private JButton stopButton;
    private JButton resumeButton;
    private JButton startButton;
    private JFrame parentFrame;

    public DisplayWindow(PlaySong currentSong, JFrame frame) {
        this.parentFrame = frame;
        this.currentSong = currentSong;
        startButton = new JButton("Start Music");
        startButton.addActionListener(e -> currentSong.playSound());
        stopButton = new JButton("Stop Music");
        stopButton.addActionListener(e -> currentSong.stopSound());
        resumeButton = new JButton("Resume Music");
        resumeButton.addActionListener(e -> currentSong.resumeSound());
        add(startButton);
        add(stopButton);
        add(resumeButton);
        gameOver = false;
        score = 0;
        timer = new Timer(10, this);
        pressedKeys = new boolean[128]; // 128 keys on keyboard, max keycode is 127
        try {
            background = ImageIO.read(new File("src/wayer.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, null);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 32));
            if (score == 10) {
                g.drawString("GAME OVER, YOU WIN!", 350, 240);
            } else {
                g.drawString("GAME OVER, YOU LOSE :(", 350, 240);
            }
        }



        // set font and color of text
        g.setFont(new Font("Times New Roman", Font.BOLD, 16));
        g.drawString("Accuracy: " + Math.round(accuracy * 100.0) / 100.0, 50, 30);
    }

    @Override
    public void mouseClicked(MouseEvent e) { } // unimplemented
    // unimplemented because if you move your mouse while clicking, this method isn't
    // called, so mouseReleased is best

    @Override
    public void mousePressed(MouseEvent e) { } // unimplemented

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) { } // unimplemented

    @Override
    public void mouseExited(MouseEvent e) { } // unimplemented

    @Override
    public void keyTyped(KeyEvent e) { } // unimplemented

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("key pressed: " + keyCode);
        pressedKeys[keyCode] = true;
        if (keyCode == KeyEvent.VK_F11) {
            if (parentFrame.isUndecorated()) {
                parentFrame.dispose();
                parentFrame.setUndecorated(false);
                parentFrame.setExtendedState(JFrame.NORMAL);
                parentFrame.setVisible(true);
            } else {
                parentFrame.dispose();
                parentFrame.setUndecorated(true);
                parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                parentFrame.setVisible(true);
            }
            requestFocusInWindow(); // regain focus after toggle
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = false;
    }




    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}
