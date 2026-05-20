import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DisplayWindow extends JPanel implements MouseListener, KeyListener, ActionListener {
    private int score;
    private boolean yellowColor;
    private int marioX;
    private int marioY;
    private double goombaX;
    private BufferedImage background;
    private BufferedImage mario;
    private BufferedImage goomba;
    private BufferedImage coin;
    private boolean[] pressedKeys;
    private Timer timer;
    private boolean gameOver;
    private ArrayList<Point> coins;

    public DisplayWindow() {
        score = 0;
        coins = new ArrayList<>();
        yellowColor = true;
        gameOver = false;
        marioX = 50;
        marioY = 435;
        goombaX = -50;  // off-screen by the amount of the image width
        timer = new Timer(10, this);
        pressedKeys = new boolean[128]; // 128 keys on keyboard, max keycode is 127
        try {
            background = ImageIO.read(new File("src/astolfo.jpg"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            mario = ImageIO.read(new File("src/marioright.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            goomba = ImageIO.read(new File("src/goomba.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            coin = ImageIO.read(new File("src/coin.png"));
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
        } else {
            g.drawImage(mario, marioX, marioY, null);
            g.drawImage(goomba, (int) goombaX, 470, null);
        }

        for (Point c : coins) {
            g.drawImage(coin, c.x, c.y, null);
        }

        // set font and color of text
        g.setFont(new Font("Arial", Font.BOLD, 16));
        if (yellowColor) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.BLACK);
        }
        g.drawString("Score: " + score, 50, 30);
    }

    @Override
    public void mouseClicked(MouseEvent e) { } // unimplemented
    // unimplemented because if you move your mouse while clicking, this method isn't
    // called, so mouseReleased is best

    @Override
    public void mousePressed(MouseEvent e) { } // unimplemented

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            yellowColor = !yellowColor;
            repaint();
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point clickLocation = e.getPoint();
            coins.add(clickLocation);
            repaint();
        }
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
        if (keyCode == KeyEvent.VK_A) {  // A key; VK_A equals 65
            try {
                mario = ImageIO.read(new File("src/marioleft.png"));
            } catch (IOException error) { }
        }
        if (keyCode == KeyEvent.VK_D) {  // D key; VK_D equals 65
            try {
                mario = ImageIO.read(new File("src/marioright.png"));
            } catch (IOException error) { }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = false;
    }

    private void moveMario() {
        // player moves left (A)
        if (pressedKeys[KeyEvent.VK_A]) {
            marioX -= 5;
        }

        // player moves right (D)
        if (pressedKeys[KeyEvent.VK_D]) {
            marioX += 5;
        }

        // player moves up (W)
        if (pressedKeys[KeyEvent.VK_W]) {
            marioY -= 5;
        }

        // player moves down (S)
        if (pressedKeys[KeyEvent.VK_S]) {
            marioY += 5;
        }
    }

    private void moveGoomba() {
        goombaX += 0.5;
        if (goombaX > 1010) {  // 960 + 50, 50 is approx goomba's image width
            goombaX = -50;  // off screen
        }
    }

    private Rectangle marioRectangle() {
        int imageHeight = mario.getHeight();
        int imageWidth = mario.getWidth();
        Rectangle rect = new Rectangle(marioX, marioY, imageWidth, imageHeight);
        return rect;
    }

    private Rectangle goombaRectangle() {
        int imageHeight = goomba.getHeight();
        int imageWidth = goomba.getWidth();
        Rectangle rect = new Rectangle((int) goombaX, 470, imageWidth, imageHeight);
        return rect;
    }

    private Rectangle coinRectangle(Point point) {
        int imageHeight = coin.getHeight();
        int imageWidth = coin.getWidth();
        Rectangle rect = new Rectangle(point.x, point.y, imageWidth, imageHeight);
        return rect;
    }

    private boolean checkForMarioGoombaCollision() {
        Rectangle marioRect = marioRectangle();
        Rectangle goombaRect = goombaRectangle();
        return marioRect.intersects(goombaRect);
    }

    private void checkForMarioCoinCollision() {
        Rectangle marioRect = marioRectangle();
        for (int i = 0; i < coins.size(); i++) {
            Rectangle coinRect = coinRectangle(coins.get(i));
            if (marioRect.intersects(coinRect)) {
                score++;
                coins.remove(i);
                i--;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveMario();
        moveGoomba();
        checkForMarioCoinCollision();
        if (checkForMarioGoombaCollision() || score == 10) {
            gameOver = true;
            timer.stop();
        }
        repaint();
    }
}
