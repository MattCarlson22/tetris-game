import javax.swing.JFrame;

public class Tetris extends JFrame{
    public Tetris() {
        initUI();
    }

    private void initUI() {
        add(new Board());
        setTitle("Tetris");
        setSize(305, 635);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        Tetris game = new Tetris();
        game.setVisible(true);
    }
}