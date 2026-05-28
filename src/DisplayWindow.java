import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class DisplayWindow extends JPanel implements MouseListener, KeyListener, ActionListener {
    private static final double accuracy = 0.0; //yes this works correctly i checked it goes to #.##

    private final boolean[] pressedKeys;
    private boolean visible = false;
    private final boolean gameOver;
    private final boolean isBGBlack;

    private int screen;

    private final Timer timer;

    private final PlaySong currentSong;

    private BufferedImage background;

    private final JButton resumeButton;
    private final JButton startButton;
    private final JButton settingsButton;
    private final JButton stopButton;
    private final JButton playButton;
    private final JButton returnButton;
    private final JButton exitButton;
    private final JButton blackBG;
    private final JLabel volume;
    private final JSlider volumeSlider;

    private final JFrame parentFrame;

    private JPanel songSelect = new JPanel();

    private JScrollPane songSelectWindow = new JScrollPane(songSelect);

    private int perfectCount;
    private int greatCount;
    private int goodCount;
    private int badCount;
    private int missCount;
    private int combo;
    private final int score;

    private static final ArrayList<Song> songs = new ArrayList<>();

    public DisplayWindow(PlaySong currentSong, JFrame frame) {
        this.parentFrame = frame;
        this.currentSong = currentSong;
        isBGBlack = false;

        startButton = new JButton("Start Music");
        startButton.addActionListener(e -> currentSong.playSound());
        stopButton = new JButton("Stop Music");
        stopButton.addActionListener(e -> currentSong.stopSound());
        resumeButton = new JButton("Resume Music");
        resumeButton.addActionListener(e -> currentSong.resumeSound());
        returnButton = new JButton("Return To Home Page");
        returnButton.addActionListener(e -> screen = 0);
        playButton = new JButton("PLAY");
        playButton.addActionListener(e -> screen = 1);
        settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> screen = 3); //dawg IDK make it go to settings page ig
        exitButton = new JButton("Exit game");
        exitButton.addActionListener(e->System.exit(0));
        volumeSlider = new JSlider(0, 100, 50);
        volume = new JLabel("Volume: 50%");
        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            volume.setText("Volume: " + value + "%");
            float volumeFloat = value / 100f;
        });
        blackBG = new JButton("Remove Background");
        blackBG.addActionListener(e-> {
            if (isBGBlack == false){
                try {
                    background = ImageIO.read(new File("src/pictures/black.jpg"));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        gameOver = false;
        score = 0;
        screen = 0;
        timer = new Timer(10, this);
        pressedKeys = new boolean[128]; // 128 keys on keyboard, max keycode is 127

        try {
            background = ImageIO.read(new File("src/pictures/anothermooda.jpg")); //change background
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        add(exitButton);
        add(playButton);
        add(settingsButton);
        add(returnButton);
        add(songSelectWindow);
        add(stopButton);
        add(startButton);
        add(resumeButton);
        add(volumeSlider);
        add(volume);
        add(blackBG);

        stopButton.setVisible(visible);
        resumeButton.setVisible(visible);
        startButton.setVisible(visible);
        returnButton.setVisible(visible);
        volumeSlider.setVisible(visible);
        volume.setVisible(visible);
        blackBG.setVisible(visible);

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
                background = ImageIO.read(new File("src/pictures/m2.png"));
                g.drawImage(background, 0, 0, null);
                returnButton.setVisible(false);
                g.drawString("very good game", 1980/2, 100);
                g.setFont(new Font("Cosmic Sans MS", Font.PLAIN,100));
                playButton.setSize(500, 200);
                playButton.setLocation(1980/2 - 250,1080/2-300); // wow we centered a button apple hire us please
                playButton.setVisible(screen == 0);
                settingsButton.setVisible(screen == 0);
                settingsButton.setSize(500,200);
                settingsButton.setLocation(1980/2 -250,1080/2);
                settingsButton.setVisible(true);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (screen == 1) { //song select
            try {
                background = ImageIO.read(new File("src/pictures/spinnin.png"));
                drawScaledImage(background, g, 0.5, 50, 100);
                playButton.setVisible(false);
                settingsButton.setVisible(false);
                returnButton.setVisible(true);
                songSelect = new JPanel();
                // String[] songList = {"take a hint", "dangerous woman"};
                songSelect.setLayout(new BoxLayout(songSelect, BoxLayout.Y_AXIS));
                for (int i = 0; i < 500; i++) {
                    JLabel song = new JLabel("Label" + i);
                    songSelect.add(song);
                }
                songSelect.revalidate();
                songSelectWindow = new JScrollPane(songSelect);
                songSelectWindow.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                songSelectWindow.setPreferredSize(new Dimension(300, 1080));
                add(songSelectWindow);
                repaint();
                songSelectWindow.setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //songSelect.set
            songSelectWindow.createVerticalScrollBar();
        }

        if (screen == 2) { //actual game
            playButton.setVisible(false);
            settingsButton.setVisible(false);
            g.drawString("Accuracy: " + Math.round(accuracy * 100.0) / 100.0, 50, 30);
        }

        if (screen == 3){ //settings
            try {
                returnButton.setVisible(true);
                playButton.setVisible(false);
                settingsButton.setVisible(false);
                volumeSlider.setVisible(true);
                volume.setVisible(true);
                background = ImageIO.read(new File("src/pictures/astolfo.jpg"));
                g.drawImage(background, 0, 0, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // set font and color of text
        g.setFont(new Font("Arial", Font.BOLD, 16));
    }

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
            requestFocusInWindow();
        }

        if (keyCode == KeyEvent.VK_ESCAPE) {
            visible = !visible;

            stopButton.setVisible(visible);
            resumeButton.setVisible(visible);
            startButton.setVisible(visible);
            returnButton.setVisible(visible);
            volumeSlider.setVisible(visible);
            volume.setVisible(visible);
            blackBG.setVisible(visible);

            requestFocusInWindow();
            revalidate();
            repaint();
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = false;
    }

    private void drawScaledImage(BufferedImage img, Graphics g, double scaleFactor, int x, int y) {
        if (img == null) return;

        int originalWidth = img.getWidth();
        int originalHeight = img.getHeight();
        double aspectRatio = (double) originalWidth / originalHeight;

        // Calculate maximum target bounds based on window size and scale factor
        int targetWidth = (int) (getWidth() * scaleFactor);
        int targetHeight = (int) (getHeight() * scaleFactor);

        // Adjust dimensions to strictly preserve aspect ratio
        if (targetWidth / (double) targetHeight > aspectRatio) {
            targetWidth = (int) (targetHeight * aspectRatio);
        } else {
            targetHeight = (int) (targetWidth / aspectRatio);
        }

        // makes the image be higher quality rather than running fast
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw the image at the specified x and y coordinates
        g2d.drawImage(img, x, y, targetWidth, targetHeight, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
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
}
