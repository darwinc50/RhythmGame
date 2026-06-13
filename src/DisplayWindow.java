import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DisplayWindow extends JPanel implements MouseListener, KeyListener, ActionListener {

    // -------------------------------------------------------------------------
    // Screen state
    // -------------------------------------------------------------------------
    private enum Screen { HOME, SONG_SELECT, GAME, SETTINGS }

    private Screen screen;

    // -------------------------------------------------------------------------
    // Game stats (non-final so they can actually change during gameplay)
    // -------------------------------------------------------------------------
    private double accuracy = 0.0;
    private int perfectCount;
    private int greatCount;
    private int goodCount;
    private int badCount;
    private int missCount;
    private int combo;
    private int score;

    // -------------------------------------------------------------------------
    // Input tracking
    // -------------------------------------------------------------------------
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean pauseMenuVisible = false;

    // -------------------------------------------------------------------------
    // Background images (loaded once, not on every repaint)
    // -------------------------------------------------------------------------
    private BufferedImage bgHome;
    private BufferedImage bgSongSelect;
    private BufferedImage bgSettings;
    private BufferedImage bgGame;
    private BufferedImage currentBackground;

    // -------------------------------------------------------------------------
    // UI components
    // -------------------------------------------------------------------------
    private final JButton playButton;
    private final JButton settingsButton;
    private final JButton returnButton;
    private final JButton exitButton;

    // Pause-menu controls (shown/hidden via ESC)
    private final JButton startButton;
    private final JButton stopButton;
    private final JButton resumeButton;
    private final JSlider volumeSlider;
    private final JLabel  volumeLabel;

    // Song select panel
    private final JScrollPane songSelectPane;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private final PlaySong currentSong;
    private final JFrame   parentFrame;
    private final Timer    repaintTimer;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public DisplayWindow(PlaySong currentSong, JFrame frame) {
        this.currentSong = currentSong;
        this.parentFrame = frame;

        loadImages();

        // --- Main nav buttons ---
        playButton = makeButton("PLAY", e -> transitionTo(Screen.SONG_SELECT));
        playButton.setText(""); // Clears text so it doesn't render over your image file

        // Load original play button image and compute shrunk sizes (90% scale)
        ImageIcon originalPlayIcon = new ImageIcon("src/pictures/playButton.png");
        int playTargetWidth = (int) (originalPlayIcon.getIconWidth() * 0.9);
        int playTargetHeight = (int) (originalPlayIcon.getIconHeight() * 0.9);

        // Generate the smooth scaled-down variant
        java.awt.Image playScaledImg = originalPlayIcon.getImage().getScaledInstance(playTargetWidth, playTargetHeight, java.awt.Image.SCALE_SMOOTH);
        ImageIcon shrunkPlayIcon = new ImageIcon(playScaledImg);

        // Strip default background and borders
        playButton.setIcon(originalPlayIcon);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setFocusPainted(false);

        // Absolute position hover behavior (adjusts bounds to stay centered)
        playButton.addMouseListener(new java.awt.event.MouseAdapter() {
            private java.awt.Rectangle originalBounds;

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                originalBounds = playButton.getBounds();
                playButton.setIcon(shrunkPlayIcon);

                // Calculate positional offsets so it shrinks inward toward the center
                int dx = (originalBounds.width - playTargetWidth) / 2;
                int dy = (originalBounds.height - playTargetHeight) / 2;
                playButton.setBounds(originalBounds.x + dx, originalBounds.y + dy, playTargetWidth, playTargetHeight);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                playButton.setIcon(originalPlayIcon);
                if (originalBounds != null) {
                    playButton.setBounds(originalBounds);
                }
            }
        });

        settingsButton = makeButton("Settings", e -> transitionTo(Screen.SETTINGS));
        returnButton = makeButton("Return To Home", e -> transitionTo(Screen.HOME));
        exitButton = makeButton("Exit Game", e -> System.exit(0));

        // --- Pause menu buttons ---
        startButton  = makeButton("Start Music",  e -> currentSong.playSound());
        stopButton   = makeButton("Stop Music",   e -> currentSong.stopSound());
        resumeButton = makeButton("Resume Music", e -> currentSong.resumeSound());


        // --- Volume slider ---
        volumeSlider = new JSlider(0, 100, 50);
        volumeLabel  = new JLabel("Volume: 50%");
        volumeSlider.addChangeListener(e -> {
            int val = volumeSlider.getValue();
            volumeLabel.setText("Volume: " + val + "%");
            float gain = val / 100f;
//            currentSong.setVolume(gain); // wire up when PlaySong supports it
        });

        // --- Song select scroll pane ---
        JPanel songListPanel = buildSongListPanel();
        songSelectPane = new JScrollPane(songListPanel);
        songSelectPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        songSelectPane.setPreferredSize(new Dimension(300, 900));

        // Add everything; visibility is managed by transitionTo()
        setLayout(null); // absolute layout so we can position manually
        add(exitButton);
        add(playButton);
        add(settingsButton);
        add(returnButton);
        add(startButton);
        add(stopButton);
        add(resumeButton);
        add(volumeSlider);
        add(volumeLabel);
        add(songSelectPane);

        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        repaintTimer = new Timer(16, this); // ~60 fps
        repaintTimer.start();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Only run once — remove self after first valid size
                if (getWidth() > 0 && getHeight() > 0) {
                    removeComponentListener(this);
                    transitionTo(Screen.HOME);
                }
            }
        });// set initial state properly
    }


    // -------------------------------------------------------------------------
    // Screen transitions — ONE place that owns button visibility & background
    // -------------------------------------------------------------------------
    private void transitionTo(Screen next) {
        screen = next;
        pauseMenuVisible = false; // reset pause overlay on any nav

        // Hide everything first, then show what's needed
        setAllVisible(false);

        switch (screen) {
            case HOME -> {
                exitButton.setVisible(true);
                currentBackground = bgHome;
                playButton.setVisible(true);
                settingsButton.setVisible(true);
                positionButton(playButton,     centerX(500), centerY(200) - 220, 500, 200);
                positionButton(settingsButton, centerX(500), centerY(200),       500, 200);
                positionButton(exitButton,     centerX(200), centerY(60)  + 160, 200,  60);
            }
            case SONG_SELECT -> {
                currentBackground = bgSongSelect;
//                returnButton.setVisible(true);
                songSelectPane.setVisible(true);
                positionButton(returnButton, 20, 20, 180, 40);
                songSelectPane.setBounds(20, 70, 300, 900);
            }
            case GAME -> {
                currentBackground = null;
                // game UI set up here as needed
            }
            case SETTINGS -> {
                currentBackground = bgSettings;
                returnButton.setVisible(true);
                volumeSlider.setVisible(true);
                volumeLabel.setVisible(true);
                positionButton(returnButton,   20, 20,  180,  40);
                volumeSlider.setBounds(centerX(300), 200, 300, 50);
                volumeLabel.setBounds(centerX(200),  260, 200, 30);
            }
        }

        revalidate();
        repaint();
    }

    private void setPauseMenuVisible(boolean visible) {
        pauseMenuVisible = visible;
        startButton.setVisible(visible);
        stopButton.setVisible(visible);
        resumeButton.setVisible(visible);
        returnButton.setVisible(visible);
        if (visible) {
            // Position pause buttons in the centre of the screen
            positionButton(startButton,  centerX(180), centerY(40) - 70, 180, 40);
            positionButton(stopButton,   centerX(180), centerY(40),      180, 40);
            positionButton(resumeButton, centerX(180), centerY(40) + 70, 180, 40);
        }
        revalidate();
        repaint();
    }

    // -------------------------------------------------------------------------
    // Painting — ONLY drawing here, no file I/O, no component creation
    // -------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);

        // Draw background (black fallback if null)
        if (currentBackground != null) {
            g2.drawImage(currentBackground, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        switch (screen) {
            case HOME -> drawHome(g2);
            case SONG_SELECT -> drawSongSelect(g2);
            case GAME -> drawGame(g2);
            case SETTINGS -> drawSettings(g2);
        }

        if (pauseMenuVisible) drawPauseOverlay(g2);
    }

    private void drawHome(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 48));
        drawCenteredString(g, "Very Good Game", getWidth() / 2, 100);
    }

    private void drawSongSelect(Graphics2D g) {
        g.setColor(Color.WHITE);
        Font font = new Font("Serif", Font.BOLD, 75);
        g.setFont(font);

        // Calculate the exact pixel width of the string
        int stringWidth = g.getFontMetrics(font).stringWidth("Select a Song");

        // Dynamically center the text on a 1920px wide screen
        int x = (1920 - stringWidth) / 2;
        int y = 100; // Keeps it at the top of the screen

        g.drawString("Select a Song", x, y);
    }

    private void drawGame(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Accuracy: " + String.format("%.2f", accuracy) + "%", 50, 30);
        g.drawString("Score: "    + score,                                    50, 55);
        g.drawString("Combo: "    + combo,                                    50, 80);
    }

    private void drawSettings(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 36));
        drawCenteredString(g, "Settings", getWidth() / 2, 100);
    }

    private void drawPauseOverlay(Graphics2D g) {
        // Semi-transparent dark overlay
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 36));
        drawCenteredString(g, "Paused", getWidth() / 2, 120);
    }

    // -------------------------------------------------------------------------
    // Key handling
    // -------------------------------------------------------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        pressedKeys.add(code);

        if (code == KeyEvent.VK_F11) {
            toggleFullscreen();
        }

        if (code == KeyEvent.VK_ESCAPE) {
            setPauseMenuVisible(!pauseMenuVisible);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    private void toggleFullscreen() {
        parentFrame.dispose();
        parentFrame.setUndecorated(!parentFrame.isUndecorated());
        parentFrame.setExtendedState(
                parentFrame.isUndecorated() ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL
        );
        parentFrame.setVisible(true);
        requestFocusInWindow();
    }

    // -------------------------------------------------------------------------
    // Timer tick
    // -------------------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    // -------------------------------------------------------------------------
    // Image loading — called once in constructor
    // -------------------------------------------------------------------------
    private void loadImages() {
        bgHome      = loadImage("src/background/m2.png");
        bgSongSelect = loadImage("src/background/spinnin.png");
        bgSettings  = loadImage("src/background/astolfo.jpg");
        bgGame      = loadImage("src/background/anothermooda.jpg");
        currentBackground = bgHome;
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load image: " + path + " (" + e.getMessage() + ")");
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Song list panel — built once
    // -------------------------------------------------------------------------
    private JPanel buildSongListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (Song song : Song.getSongs()) {
            JButton btn = new JButton(song.toString());
            btn.addActionListener(e -> {
                PlaySong selectSong = new PlaySong(song.getSong());
                selectSong.playSound();
                // TODO: load and start the selected song, then transitionTo(Screen.GAME)
                transitionTo(Screen.GAME);
            });
            panel.add(btn);
        }
        return panel;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void setAllVisible(boolean v) {
        playButton.setVisible(v);
        settingsButton.setVisible(v);
        returnButton.setVisible(v);
        exitButton.setVisible(v);
        startButton.setVisible(v);
        stopButton.setVisible(v);
        resumeButton.setVisible(v);
        volumeSlider.setVisible(v);
        volumeLabel.setVisible(v);
        songSelectPane.setVisible(v);
    }

    private JButton makeButton(String label, ActionListener listener) {
        JButton btn = new JButton(label);
        btn.addActionListener(listener);
        return btn;
    }

    private void positionButton(JButton btn, int x, int y, int w, int h) {
        btn.setBounds(x, y, w, h);
    }

    private int centerX(int w) { return (getWidth()  - w) / 2; }
    private int centerY(int h) { return (getHeight() - h) / 2; }

    private void drawCenteredString(Graphics2D g, String text, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        g.drawString(text, x, y);
    }

    // -------------------------------------------------------------------------
    // Unused listener stubs
    // -------------------------------------------------------------------------
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void keyTyped(KeyEvent e)        {}
}