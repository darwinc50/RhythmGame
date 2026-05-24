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
    private JButton settingsButton;
    private JFrame parentFrame;
    private boolean visible;
    private int screen;
    private JButton playButton;

    public DisplayWindow(PlaySong currentSong, JFrame frame) {
        this.parentFrame = frame;
        this.currentSong = currentSong;
        startButton = new JButton("Start Music");
        startButton.addActionListener(e -> currentSong.playSound());
        stopButton = new JButton("Stop Music");
        stopButton.addActionListener(e -> currentSong.stopSound());
        resumeButton = new JButton("Resume Music");
        resumeButton.addActionListener(e -> currentSong.resumeSound());
        playButton = new JButton("PLAY");
        playButton.addActionListener(e -> screen = 1);
        settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> screen = 3); //dawg idk make it go to settings page ig
        add(startButton);
        add(stopButton);
        add(resumeButton);
        visible = stopButton.isVisible();
        stopButton.setVisible(!visible);
        resumeButton.setVisible(!visible);
        startButton.setVisible(!visible);
        gameOver = false;
        score = 0;
        screen = 0;
        timer = new Timer(10, this);
        pressedKeys = new boolean[128]; // 128 keys on keyboard, max keycode is 127
        try {
            background = ImageIO.read(new File("src/anothermooda.jpg")); //change background
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

        if (screen == 0) { //home screen
            try {
                g.drawImage(background, 0, 0, null);
                add(playButton);
                playButton.setVisible(screen == 0);
                background = ImageIO.read(new File("src/m2.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (screen == 1) { //song select
            try {
                playButton.setVisible(false);
                background = ImageIO.read(new File("src/spinnin.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (screen == 2) { //actual game
            g.drawString("Accuracy: " + Math.round(accuracy * 100.0) / 100.0, 50, 30);
        }
        if (screen == 3){ //settings

        }
        // set font and color of text
        g.setFont(new Font("Times New Roman", Font.BOLD, 16));


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
        if (keyCode == KeyEvent.VK_ESCAPE){
            visible= !visible;
            try {
                if (visible) {
                    background = ImageIO.read(new File("src/astolfo.jpg"));
                    stopButton.setVisible(visible);
                    resumeButton.setVisible(visible);
                    startButton.setVisible(visible);
                } else {
                    background = ImageIO.read(new File("src/anothermooda.jpg"));
                    stopButton.setVisible(visible);
                    resumeButton.setVisible(visible);
                    startButton.setVisible(visible);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
