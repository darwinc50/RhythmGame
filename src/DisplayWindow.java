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

    private enum Screen { HOME, SONG_SELECT, GAME, SETTINGS }

    private Screen screen;

    private static int greatCount;
    private static int goodCount;
    private static int badCount;
    private static int missCount;
    private static int combo;
    private static long score;
    private static int totalNotes;
    private static double accuracyTotal;
    private static double accuracy;

    private static String lastJudgement = "";
    private static String lastJudgementOffset;
    private static long judgementTime;

    private static File currentChart;

    private ScheduledFuture<?> missChecker;

    private static ArrayList<Note> chartData = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private boolean bot = false;

    private ArrayList<Input> pressedKeys = new ArrayList<Input>();
    private boolean pauseMenuVisible = false;


    private BufferedImage bgHome;
    private BufferedImage bgSongSelect;
    private BufferedImage bgSettings;
    private BufferedImage bgGame;
    private BufferedImage currentBackground;

    private final JButton playButton;
    private final JButton returnButton;
    private final JButton exitButton;


    private final JButton startButton;
    private final JButton stopButton;
    private final JButton resumeButton;


    private final JScrollPane songSelectPane;


    private static PlaySong currentSong;
    private final JFrame   parentFrame;
    private final Timer    repaintTimer;


    public DisplayWindow(JFrame frame) {
        this.parentFrame = frame;

        loadImages();


        playButton = makeButton("PLAY", e -> transitionTo(Screen.SONG_SELECT));
        playButton.setText("PLAY");
        returnButton = makeButton("Return To Home", e -> {
            currentChart = null;
            chartData.clear();
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


        startButton  = makeButton("Start Music",  e -> currentSong.playSound());
        stopButton   = makeButton("Stop Music",   e -> currentSong.stopSound());
        resumeButton = makeButton("Resume Music", e -> currentSong.resumeSound());

        JPanel songListPanel = buildSongListPanel();
        songSelectPane = new JScrollPane(songListPanel);

        songSelectPane.setPreferredSize(new Dimension(300, 900));


        setLayout(null);
        add(exitButton);
        add(playButton);
        add(returnButton);
        add(startButton);
        add(stopButton);
        add(resumeButton);
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

                if (getWidth() > 0 && getHeight() > 0) {
                    removeComponentListener(this);
                    transitionTo(Screen.HOME);
                }
            }
        });
    }

    private void transitionTo(Screen next) {
        screen = next;
        pauseMenuVisible = false;

        setAllVisible(false);

        switch (screen) {
            case HOME -> {
                exitButton.setVisible(true);
                currentBackground = bgHome;
                playButton.setVisible(true);
                positionButton(playButton,     centerX(500), centerY(200), 500, 200);
                positionButton(exitButton,     centerX(200), centerY(60)  + 160, 200,  60);
            }
            case SONG_SELECT -> {
                currentBackground = bgSongSelect;

                songSelectPane.setVisible(true);
                positionButton(returnButton, 20, 20, 180, 40);
                songSelectPane.setBounds(20, 70, 300, 900);
            }
            case GAME -> {
                currentBackground = null;

            }
        }

        revalidate();
        repaint();
    }

    private void setPauseMenuVisible(boolean visible) {
        pauseMenuVisible = visible;
        if (visible) {
            currentSong.stopSound();
        } else {
            currentSong.resumeSound();
        }
        returnButton.setVisible(visible);
        if (visible) {
            positionButton(returnButton,   centerX(180), centerY(40),      180, 40);
        }
        revalidate();
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (screen == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);


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
        g.setFont(new Font("Arial", Font.BOLD, 48));
        drawCenteredString(g, "very hard rhythm game", getWidth() / 2, 100);
    }

    private void drawSongSelect(Graphics2D g) {
        g.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 75);
        g.setFont(font);

        int stringWidth = g.getFontMetrics(font).stringWidth("Select a Song");

        int x = (1920 - stringWidth) / 2;
        int y = 100;

        g.drawString("Select a Song", x, y);
    }

    private void drawGame(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString(String.format("%.2f", accuracyTotal / totalNotes) + "%", 1920/2 - 50, 200);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(score),10, 75);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.decode("#969692"));
        g.drawString(combo + "x",10, 125);
        g.setColor(Color.decode("#312a44"));

        g.fillRect(0, 1080/2 - 245, 1980, 140);

        g.setColor(Color.decode("#383a54"));
        g.setColor(Note.judgementConvert(lastJudgement));
        g.fillOval(1920/2 - 350, 1080/2 - 350, 350,350);

        g.setColor(Color.decode("#242424"));
        g.fillOval(1920/2 - 300, 1080/2 - 300, 250,250);

        g.drawOval(1920/2 - 350, 1080/2 - 350, 350,350);


        if (!lastJudgement.isEmpty() && currentSong != null) {
            long diff = currentSong.getTime() - judgementTime;
            if (diff < 200000) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                switch (lastJudgement) {
                    case "Miss" -> g.setColor(Color.decode("#e32007"));
                    case "Bad" -> g.setColor(Color.decode("#c49d33"));
                    case "Good" -> g.setColor(Color.decode("#33c45f"));
                    case "Great!" -> g.setColor(Color.decode("#0ab6ff"));
                }
                g.drawString(lastJudgement, getWidth() / 2 - 30, getHeight() - 80);
                g.setFont(new Font("Arial", Font.BOLD, 20));

            } else {
                lastJudgement = "";
            }
        }

    }

    public void drawNotes(Graphics2D g) {
        long currentTime = currentSong.getTime();

        for (Note note : chartData) {
            Color note1 = Note.convert(Arrays.copyOfRange(note.getLanes(), 0, 3));
            Color note2 = Note.convert(Arrays.copyOfRange(note.getLanes(), 3, 6));
            if (note.getTime() - currentTime < 1000000 && currentTime - note.getTime() < 150000 && !note.isHit()) {
                long offset = note.getTime() - currentTime;

                if (note1 != null) {
                    g.setColor(note1);
                    int x = (1920/2 - 350) - (int)(offset / 1000) - 100;
                    g.fillRect(x, 1080/2 - 225, 100, 100);
                }
                if (note2 != null) {
                    g.setColor(note2);
                    int x = (1920/2) + (int)(offset / 1000);
                    g.fillRect(x, 1080/2 - 225, 100, 100);
                }
            }
        }

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

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 36));
        drawCenteredString(g, "Paused", getWidth() / 2, 120);
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


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }


    private void loadImages() {
        bgHome      = loadImage("src/background/home.jpg");
        bgSongSelect = loadImage("src/background/rhythm_home.jpg");
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


    private JPanel buildSongListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);
        for (Song song : Song.getSongs()) {
            JButton btn = new JButton(song.toString());


            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            btn.setFont(new Font("Arial", Font.BOLD, 16));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(40, 40, 60));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 120)),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setHorizontalAlignment(SwingConstants.LEFT);


            Color normal = new Color(40, 40, 60);
            Color hover  = new Color(70, 70, 110);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
                public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(normal); }
            });

            btn.addActionListener(e -> {
                if (missChecker != null && !missChecker.isDone()) {
                    missChecker.cancel(true);
                }

                currentSong = new PlaySong(song.getSong());
                currentSong.playSound();
                currentChart = song.getChart();
                loadChart();

                transitionTo(Screen.GAME);
                missChecker = scheduler.scheduleAtFixedRate(() -> {
                    long currentTime = currentSong.getTime();
                    for (Note note : chartData) {
                        if (bot && Math.abs(currentTime - note.getTime()) < 30000 && !note.isHit()) {

                            note.setHit(true);
                            totalNotes++;
                            combo++;
                            judgementTime = note.getTime();
                            lastJudgementOffset = 0 + " ms";
                            if ((int) (Math.random() * 62) < 60) {
                                lastJudgement = "Great!";
                                accuracyTotal += 100.0;
                                score += (long) combo * 500;
                            } else {
                                lastJudgement = "Good";
                                accuracyTotal += 50.0;
                                score += (long) combo * 250;
                            }

                        } else if (!note.isHit() && currentTime - note.getTime() > 150000) {
                            note.setHit(true);
                            accuracyTotal += 0.0;
                            combo = 0;
                            missCount++;
                            totalNotes++;
                            lastJudgement = "Miss";
                            judgementTime = currentTime;
                            lastJudgementOffset = "";
                        }
                    }
                }, 0, 1, TimeUnit.MILLISECONDS);
            });
            panel.add(btn);
        }
        return panel;
    }

    private void setAllVisible(boolean v) {
        playButton.setVisible(v);
        returnButton.setVisible(v);
        exitButton.setVisible(v);
        startButton.setVisible(v);
        stopButton.setVisible(v);
        resumeButton.setVisible(v);
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


    @Override
    public void keyReleased(KeyEvent e) {
    }
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e)        {

        if (screen != Screen.GAME || currentSong == null) return;

        int code = e.getKeyCode();

        if (code == KeyEvent.VK_0) {
            bot = !bot;
        }

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

        if (!bot) {
            if (!pressedKeys.isEmpty() && currentTime - pressedKeys.getLast().getTimePressed() < 30000) {
                pressedKeys.getLast().addChordInput(code);

            } else {
                pressedKeys.add(key);

            }
            if (!pressedKeys.isEmpty()) {
                long diff = currentTime - pressedKeys.getLast().getTimePressed();

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
                                accuracyTotal += 100.0;
                                score += (long) combo * 500;
                                greatCount++;
                                judgementTime = currentTime;
                                lastJudgement = "Great!";

                            } else if (Math.abs(currentTime - note.getTime()) < 100000) {
                                accuracyTotal += 50.0;
                                score += (long) combo * 250;
                                goodCount++;
                                judgementTime = currentTime;
                                lastJudgement = "Good";
                            } else {
                                accuracyTotal += 33.3;
                                score += (long) combo * 100;
                                badCount++;
                                judgementTime = currentTime;
                                lastJudgement = "Bad";
                            }
                            lastJudgementOffset = (currentTime - note.getTime()) / 1000 + " ms";
                        } else {
                            accuracyTotal += 0.0;
                            combo = 0;
                            totalNotes++;
                            missCount++;
                            judgementTime = currentTime;
                            lastJudgement = "Miss";
                            lastJudgementOffset = "";
                        }
                        break;
                    }
                }
            }, 30, TimeUnit.MILLISECONDS);
        }
    }
}