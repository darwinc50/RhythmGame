import org.w3c.dom.css.RGBColor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DisplayWindow extends JPanel implements MouseListener, KeyListener, ActionListener {

    // -------------------------------------------------------------------------
    // Screen state
    // -------------------------------------------------------------------------
    private enum Screen { HOME, SONG_SELECT, GAME, SETTINGS }

    private Screen screen;

    // -------------------------------------------------------------------------
    // Game stats (non-final so they can actually change during gameplay)
    // -------------------------------------------------------------------------
    private static int greatCount;
    private static int goodCount;
    private static int badCount;
    private static int missCount;
    private static int combo;
    private static int score;
    private static int totalNotes;
    private static double accuracyTotal;
    private static double accuracy;

    private static String lastJudgement = "";
    private static long judgementTime;

    private static File currentChart;

    private ScheduledFuture<?> missChecker;

    private static ArrayList<Note> chartData = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // -------------------------------------------------------------------------
    // Input tracking
    // -------------------------------------------------------------------------
    private ArrayList<Input> pressedKeys = new ArrayList<Input>();
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
    private static PlaySong currentSong;
    private final JFrame   parentFrame;
    private final Timer    repaintTimer;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public DisplayWindow(JFrame frame) {
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
        returnButton = makeButton("Return To Home", e -> {
            currentChart = null;
            chartData.clear();       // ← clear here too
            pressedKeys.clear();
            currentSong.stopSound();
            totalNotes = 0;
            accuracyTotal = 0;
            greatCount = 0;
            goodCount = 0;
            badCount = 0;
            missCount = 0;
            combo = 0;
            score = 0;
            transitionTo(Screen.HOME);
        });
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

        repaintTimer = new Timer(8, this); // ~60 fps
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
            case GAME -> {
                drawGame(g2);
                drawNotes(g2);
            }
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
        int y = 100;

        g.drawString("Select a Song", x, y);
    }

    private void drawGame(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Accuracy: " + String.format("%.2f", accuracyTotal / totalNotes) + "%", 50, 30);
        g.drawString("Score: " + score,50, 55);
        g.drawString("Combo: " + combo,50, 80);

        g.drawOval(1920/2 - 350, 1080/2 - 350, 350,350);


        if (!lastJudgement.isEmpty() && currentSong != null) {
            long diff = currentSong.getTime() - judgementTime;
            if (diff < 300000) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                switch (lastJudgement) {
                    case "Miss" -> g.setColor(Color.decode("#e32007"));
                    case "Bad" -> g.setColor(Color.decode("#c49d33"));
                    case "Good" -> g.setColor(Color.decode("#33c45f"));
                    case "Great!" -> g.setColor(Color.decode("#0ab6ff"));
                }
                g.drawString(lastJudgement, getWidth() / 2 - 100, getHeight() - 80);
            } else {
                lastJudgement = "";
            }
        }

    }

    public void drawNotes(Graphics2D g) {
        long currentTime = currentSong.getTime();
        // System.out.println(currentTime);
        for (Note note : chartData) {
            Color note1 = Note.convert(Arrays.copyOfRange(note.getLanes(), 0, 3));
            Color note2 = Note.convert(Arrays.copyOfRange(note.getLanes(), 3, 6));
            if (note.getTime() - currentTime < 1000000 && note.getTime() - currentTime > 0) {
                if (note1 != null) {
                    g.setColor(note1);
                    g.fillRect((int) ((currentTime - note.getTime())/1000) + (1920/2 - 350), 1080/2 - 225, 100, 100);
                }
                if (note2 != null) {
                    g.setColor(note2);
                    g.fillRect((int) ((note.getTime() - currentTime)/1000) + (1920/2 - 175), 1080/2 - 225, 100, 100);
                }

            }
        }

        /*
        for (Note line: chartData) {
            System.out.println(line);
            System.out.println();
        }
        */
    }

    public void loadChart() {
        chartData.clear();
        try {
            Scanner chartScan = new Scanner(currentChart);
            while (chartScan.hasNextLine()) {
                String[] data = chartScan.nextLine().split("/");
                String[] stringLanes = data[0].split(",");
                int[] lanes = new int[6];
                for (int i = 0; i < 6; i++) {
                    lanes[i] = Integer.parseInt(stringLanes[i]);
                }
                chartData.add(new Note(lanes, Long.parseLong(data[1])));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Chart not found");
        }
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
                if (missChecker != null && !missChecker.isDone()) {
                    missChecker.cancel(true);
                }

                currentSong = new PlaySong(song.getSong());
                currentSong.playSound();
                currentChart = song.getChart();
                loadChart();
                // TODO: load and start the selected song, then transitionTo(Screen.GAME)
                transitionTo(Screen.GAME);
                missChecker = scheduler.scheduleAtFixedRate(() -> {
                    for (Note note : chartData) {
                        long currentTime = currentSong.getTime();
                        if (!note.isHit() && currentTime - note.getTime() > 70000) {
                            note.setHit(true);
                            accuracyTotal += 0.0;
                            combo = 0;
                            missCount++;
                            totalNotes++;
                            lastJudgement = "Miss";
                            judgementTime = currentTime;
                        }
                    }
                }, 0, 1, TimeUnit.MILLISECONDS);
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
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e)        {

        if (screen != Screen.GAME || currentSong == null) return;

        int code = e.getKeyCode();
        if (code == KeyEvent.VK_F11) {
            toggleFullscreen();
        }

        if (code == KeyEvent.VK_ESCAPE) {
            setPauseMenuVisible(!pauseMenuVisible);
        }
        long currentTime = currentSong.getTime();
        ArrayList<Integer> codes = new ArrayList<>();
        codes.add(code);
        Input key = new Input(currentTime, codes);


        if (!pressedKeys.isEmpty() && currentTime - pressedKeys.getLast().getTimePressed() < 30000) {
            pressedKeys.getLast().addChordInput(code);
            // System.out.println("Added to chord: " + pressedKeys.getLast().getKeyCodes());
        } else {
            pressedKeys.add(key);
            // System.out.println("New input created: " + key.getKeyCodes());
        }
        if (!pressedKeys.isEmpty()) {
            long diff = currentTime - pressedKeys.getLast().getTimePressed();
            // System.out.println("Time diff from last key: " + diff + " microseconds");
        }
        Input lastInput = pressedKeys.getLast();
        scheduler.schedule(() -> {
            if (lastInput.isChecked()) return;
            lastInput.setChecked(true);

            int[] pressedCodes = Input.convert(lastInput.getKeyCodes());
            for (Note note : chartData) {
                if (Math.abs(currentTime - note.getTime()) < 150000 && !note.isHit()) {
                    // System.out.println("Checking chord: " + Arrays.toString(pressedCodes));
                    if (Arrays.equals(note.getLanes(), pressedCodes)) {
                        note.setHit(true);
                        combo++;
                        totalNotes++;
                        if (Math.abs(currentTime - note.getTime()) < 60000) {
                            accuracyTotal += 99.0;
                            score += combo * 500;
                            greatCount++;
                            judgementTime = currentTime;
                            lastJudgement = "Great!";
                        } else if (Math.abs(currentTime - note.getTime()) < 100000) {
                            accuracyTotal += 50.0;
                            score += combo * 250;
                            goodCount++;
                            judgementTime = currentTime;
                            lastJudgement = "Good";
                        } else {
                            accuracyTotal += 33.3;
                            score += combo * 100;
                            badCount++;
                            judgementTime = currentTime;
                            lastJudgement = "Bad";
                        }
                    } else {
                        accuracyTotal += 10.0;
                        combo = 0;
                        totalNotes++;
                        missCount++;
                        judgementTime = currentTime;
                        lastJudgement = "Miss";
                    }
                    break;
                }
            }
        }, 30, TimeUnit.MILLISECONDS);
    }
    @Override
    public void keyReleased(KeyEvent e) {
        // pressedKeys.removeIf(in -> e.getKeyCode() == in.getKeyCodes());
    }
}