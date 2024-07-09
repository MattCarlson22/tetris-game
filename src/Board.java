import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int TILE_SIZE = 30; // Size of each block
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    //private int numLinesRemoved = 0;
    private int score = 0;
    private int highScore = 0;
    private int curX = 0;
    private int curY = 0;
    private Tetromino curPiece;
    private Tetromino.Tetrominoe[] board;

    public Board() {
        initBoard();
    }

    private void initBoard() {
        setFocusable(true); // Make the panel focusable
        requestFocusInWindow(); // Request focus for the panel
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        curPiece = new Tetromino();
        timer = new Timer(500, this);
        timer.start();
        board = new Tetromino.Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();
        addKeyListener(new TAdapter());
        newPiece(); // Initialize the first piece
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);

        if (isGameOver) {
            drawGameOver(g);
        } else {
            drawScore(g);
        }
    }

    private void drawBoard(Graphics g) {
        // Draw the grid
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Tetromino.Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetromino.Tetrominoe.NoShape) {
                    drawSquare(g, j * TILE_SIZE, i * TILE_SIZE, shape);
                }
            }
        }

        // Draw the current Tetromino
        if (curPiece.getShape() != Tetromino.Tetrominoe.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, x * TILE_SIZE, (BOARD_HEIGHT - y - 1) * TILE_SIZE, curPiece.getShape());
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("High Score: " + highScore, 10, 40);
    }

    private void drawGameOver(Graphics g) {
        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(msg, (BOARD_WIDTH * TILE_SIZE - metr.stringWidth(msg)) / 2, BOARD_HEIGHT * TILE_SIZE / 2);

        g.drawString("Score: " + score, (BOARD_WIDTH * TILE_SIZE - metr.stringWidth("Score: " + score)) / 2, (BOARD_HEIGHT * TILE_SIZE / 2) + 20);
        g.drawString("High Score: " + highScore, (BOARD_WIDTH * TILE_SIZE - metr.stringWidth("High Score: " + highScore)) / 2, (BOARD_HEIGHT * TILE_SIZE / 2) + 40);

        // Draw restart button
        g.drawString("Press 'R' to Restart", (BOARD_WIDTH * TILE_SIZE - metr.stringWidth("Press 'R' to Restart")) / 2, (BOARD_HEIGHT * TILE_SIZE / 2) + 60);
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1)) {
            pieceDropped();
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetromino.Tetrominoe.NoShape);
            timer.stop();
            isGameOver = true;
            highScore = Math.max(highScore, score);
            repaint();
        }
    }

    private boolean tryMove(Tetromino newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }

            if (shapeAt(x, y) != Tetromino.Tetrominoe.NoShape) {
                return false;
            }
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();

        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Tetromino.Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLines++;

                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }

                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[((BOARD_HEIGHT - 1) * BOARD_WIDTH) + j] = Tetromino.Tetrominoe.NoShape;
                }

                i++; // Re-check the current row after shifting rows down
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            score += numFullLines * 100; // Update score
            isFallingFinished = true;
            curPiece.setShape(Tetromino.Tetrominoe.NoShape);
            repaint();
        }
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetromino.Tetrominoe.NoShape;
        }
    }

    private Tetromino.Tetrominoe shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }

    private void drawSquare(Graphics g, int x, int y, Tetromino.Tetrominoe shape) {
        Color[] colors = { new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
                new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0) };

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + TILE_SIZE - 1, x, y);
        g.drawLine(x, y, x + TILE_SIZE - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + TILE_SIZE - 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1);
        g.drawLine(x + TILE_SIZE - 1, y + TILE_SIZE - 1, x + TILE_SIZE - 1, y + 1);
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (isGameOver) {
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    restart();
                }
                return;
            }

            if (curPiece.getShape() == Tetromino.Tetrominoe.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == 'p' || keycode == 'P') {
                pause();
                return;
            }

            if (isPaused) {
                return;
            }

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    tryMove(curPiece, curX, curY - 1);
                    break;
                case KeyEvent.VK_UP:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case 'd':
                    oneLineDown();
                    break;
            }
        }
    }

    private void dropDown() {
        int newY = curY;

        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }

        pieceDropped();
    }

    private void pause() {
        isPaused = !isPaused;

        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }

        repaint();
    }

    private void restart() {
        clearBoard();
        isGameOver = false;
        score = 0;
        newPiece();
        timer.start();
    }
}
