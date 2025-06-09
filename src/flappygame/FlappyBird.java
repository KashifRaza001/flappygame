package flappygame;

import javax.swing.*;      // GUI components ke liye (JFrame, JPanel, Timer, JOptionPane, etc.)
import java.awt.*;         // Graphics aur colors ke liye (Graphics, Color, Font, Image, etc.)
import java.awt.event.*;   // Events handle karne ke liye (KeyEvent, ActionListener, etc.)
import java.util.ArrayList; // Pipes ya objects ko list me store karne ke liye (dynamic array)
import java.util.Random;   // Random numbers generate karne ke liye (pipes ki random height ke liye)

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // --- Board ka size (screen ka area) ---
    int boardWidth = 360;
    int boardHeight = 640;

    // --- Game me use hone wali images ---
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // --- Bird ki starting position aur size ---
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    // --- Bird class jo bird ka position, size aur image rakhegi ---
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img; // Bird ki image assign kar rahe
        }
    }

    // --- Pipe ki width/height aur opening space ka size ---
    int pipeWidth = 64;
    int pipeHeight = 512;
    final int openingSpace = boardHeight / 4; // Upar aur neeche pipe ke darmiyan gap

    // --- Pipe class jo har pipe ka position, size aur image rakhegi ---
    class Pipe {
        int x;
        int y;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false; // agar bird ne is pipe ko cross kar liya ho

        Pipe(Image img, int x, int y) {
            this.img = img;
            this.x = x;
            this.y = y;
        }
    }

    Bird bird; // ek bird object

    int velocityX;       // Pipes ki speed (difficulty ke hisaab se)
    int baseSpeed = -4;  // Default speed
    int maxSpeed = -10;  // Max speed (hard mode)
    int velocityY = 0;   // Bird ki vertical speed
    int gravity = 1;     // Bird pe neeche girne wali force

    ArrayList<Pipe> pipes; // Pipes ka list
    Random random = new Random(); // Random pipe height ke liye

    Timer gameLoop;        // Game ka main loop (60 fps)
    Timer placePipeTimer;  // Har kuch second me naye pipes add karne ke liye

    boolean gameOver = false;   // Game over status
    boolean gameStarted = false; // Game start hua ya nahi
    double score = 0;           // Player ka score
    double highScore = 0;       // Sabse zyada score

    // --- Constructor jo sab initialize karega ---
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight)); // Panel ka size set
        setFocusable(true);        // Keyboard events ko focus mile
        addKeyListener(this);      // Key events handle karne ke liye

        // --- Images load kar rahe (resource folder se) ---
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg); // Bird object banaya
        pipes = new ArrayList<>(); // Pipes ka arraylist

        // --- Game loop (60 FPS) ---
        gameLoop = new Timer(1000 / 60, this);

        // --- Timer jo har 1500ms me naye pipes banaye ---
        placePipeTimer = new Timer(1500, e -> placePipes());

        // --- Game start hone se pehle difficulty poochho ---
        chooseDifficulty();
    }

    // --- Difficulty select karne ka menu ---
    void chooseDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select Difficulty:",
                "Flappy Bird",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        // --- Difficulty ke hisaab se speed set karna ---
        switch (choice) {
            case 0: // Easy
                baseSpeed = -3;
                maxSpeed = -6;
                break;
            case 1: // Medium
                baseSpeed = -4;
                maxSpeed = -8;
                break;
            case 2: // Hard
                baseSpeed = -5;
                maxSpeed = -10;
                break;
            default:
                baseSpeed = -4;
                maxSpeed = -8;
                break;
        }

        velocityX = baseSpeed;
        gameLoop.start();
        placePipeTimer.start();
        gameStarted = true;
    }

    // --- Pipes ko random height ke sath place karta hai ---
    void placePipes() {
        int minPipeTopY = -pipeHeight +100;
        int maxPipeTopY = -100;
        
        int randomPipeY = minPipeTopY + random.nextInt(maxPipeTopY - minPipeTopY + 1);

        Pipe topPipe = new Pipe(topPipeImg, boardWidth, randomPipeY);
        Pipe bottomPipe = new Pipe(bottomPipeImg, boardWidth, randomPipeY + pipeHeight + openingSpace);

        pipes.add(topPipe);
        pipes.add(bottomPipe);
    }
    

    // --- JPanel draw karte waqt call hota hai ---
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g); // Custom draw method call
    }

    // --- Screen pe saari cheeze draw karna ---
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null); // Background
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null); // Bird

        // --- Har pipe ko draw karo ---
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // --- Score aur high score draw karo ---
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Score: " + (int) score, 10, 35);
        g.drawString("High: " + (int) highScore, 200, 35);

        // --- Game over text ---
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over", 100, boardHeight / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press SPACE to Restart", 80, boardHeight / 2 + 40);
        }
    }

    // --- Game ki movement aur logic har frame me update hoti hai ---
    public void move() {
        velocityY += gravity; // Bird neeche girta rahega
        bird.y += velocityY;  // Bird ki y position update

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;

                if ((int) score % 10 == 0 && velocityX > maxSpeed) {
                    velocityX -= 1;
                }
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight || bird.y < 0) {
            gameOver = true;
        }

        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);
    }

    // --- Collision check (bird aur pipe ke darmiyan) ---
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    // --- Game loop (har frame pe call hota hai) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            move();
            repaint();
        }

        if (gameOver) {
            if (score > highScore) {
                highScore = score;
            }
            gameLoop.stop();
            placePipeTimer.stop();
        }
    }

    // --- Space press hone pe bird jump karega ya game reset hoga ---
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9; // Bird jump karega

            if (gameOver) {
                // --- Game reset karna ---
                bird.x = birdX;
                bird.y = birdY;
                velocityY = 0;
                velocityX = baseSpeed;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    // --- Baaki key events (unused) ---
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    // --- Main method: Game start karne ke liye ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.setSize(flappyBird.boardWidth, flappyBird.boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        flappyBird.requestFocus();
        frame.setVisible(true);
    }
}
