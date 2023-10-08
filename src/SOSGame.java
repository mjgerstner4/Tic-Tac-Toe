import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SOSGame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    enum PlayerType {
        HUMAN,
        COMPUTER
    }
    private int boardSize;
    private String gameMode;
    private PlayerType redPlayerType;
    private PlayerType bluePlayerType;
    private JButton[][] buttons;
    private char[][] board;
    private char currentPlayer;
    private List<String> recordedMoves;

    public SOSGame(int boardSize, String gameMode, PlayerType redPlayerType, PlayerType bluePlayerType) {
        this.boardSize = boardSize;
        this.gameMode = gameMode;
        this.redPlayerType = redPlayerType;
        this.bluePlayerType = bluePlayerType;
        this.board = new char[boardSize][boardSize];
        this.currentPlayer = 'S';
        recordedMoves = new ArrayList<>();

        setTitle("SOS Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(boardSize, boardSize));

        initializeBoard();
        createButtons();
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                board[row][col] = ' ';
            }
        }
    }

    private void createButtons() {
        buttons = new JButton[boardSize][boardSize];

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JButton button = new JButton();
                button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
                button.setPreferredSize(new Dimension(80, 80));
                button.addActionListener(new ButtonClickListener(row, col));
                buttons[row][col] = button;
                add(button);
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board[row][col] == ' ') {
                if (currentPlayer == 'S' && redPlayerType == PlayerType.HUMAN) {
                    // Red player's move
                    placeSymbol(row, col);
                    buttons[row][col].setText(Character.toString(currentPlayer));
                    buttons[row][col].setEnabled(false);
                    switchPlayers();
                    if (isGameOver()) {
                        char winner = determineWinner();
                        String message;
                        if (winner == ' ') {
                            message = "It's a draw!";
                        } else {
                            message = "Player " + winner + " wins!";
                        }
                        JOptionPane.showMessageDialog(SOSGame.this, message);
                        resetGame();
                    }
                } else if (currentPlayer == 'S' && redPlayerType == PlayerType.COMPUTER) {
                    // Red player (Computer) move
                    makeComputerMove();
                } else if (currentPlayer == 'O' && bluePlayerType == PlayerType.HUMAN) {
                    // Blue player's move
                    placeSymbol(row, col);
                    buttons[row][col].setText(Character.toString(currentPlayer));
                    buttons[row][col].setEnabled(false);
                    switchPlayers();
                    if (isGameOver()) {
                        char winner = determineWinner();
                        String message;
                        if (winner == ' ') {
                            message = "It's a draw!";
                        } else {
                            message = "Player " + winner + " wins!";
                        }
                        JOptionPane.showMessageDialog(SOSGame.this, message);
                        resetGame();
                    }
                } else if (currentPlayer == 'O' && bluePlayerType == PlayerType.COMPUTER) {
                    // Blue player (Computer) move
                    makeComputerMove();
                }
            }
        }
    }

    public boolean isGameOver() {
        // Check rows
        for (int row = 0; row < boardSize; row++) {
            if (isComplete(row, 0, 0, 1)) {
                return true;
            }
        }

        // Check columns
        for (int col = 0; col < boardSize; col++) {
            if (isComplete(0, col, 1, 0)) {
                return true;
            }
        }

        // Check diagonals
        if (isComplete(0, 0, 1, 1) || isComplete(0, boardSize - 1, 1, -1)) {
            return true;
        }

        // Check for draw game
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (board[row][col] == ' ') {
                    return false; // There are still empty cells, game is not over
                }
            }
        }

        return true; // All cells are filled, it's a draw game
    }

    private boolean isComplete(int startRow, int startCol, int rowIncrement, int colIncrement) {
        char symbol = currentPlayer;
        int target = (gameMode.equals("simple")) ? 3 : 4; // Target length based on game mode

        // Check if there is a complete SOS sequence in the specified direction
        for (int i = 0; i < target; i++) {
            int row = startRow + i * rowIncrement;
            int col = startCol + i * colIncrement;

            if (row < 0 || row >= boardSize || col < 0 || col >= boardSize || board[row][col] != symbol) {
                return false;
            }
        }

        return true;
    }

    public char determineWinner() {
        int scoreS = 0;
        int scoreO = 0;

        // Count SOS sequences on the board
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (isComplete(row, col, 0, 1)) {
                    scoreS++;
                } else if (isComplete(row, col, 1, 0)) {
                    scoreO++;
                } else if (isComplete(row, col, 1, 1)) {
                    scoreS++;
                    scoreO++;
                }
            }
        }

        if (scoreS > scoreO) {
            return 'S'; // Player S wins
        } else if (scoreO > scoreS) {
            return 'O'; // Player O wins
        } else {
            return ' '; // Draw game
        }
    }

    public void resetGame() {
        initializeBoard();

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setText("");
                buttons[row][col].setEnabled(true);
            }
        }

        currentPlayer = 'S';
    }
    
    private void switchPlayers() {
        if (currentPlayer == 'S') {
            currentPlayer = 'O';
        } else {
            currentPlayer = 'S';
        }
    }

    public void placeSymbol(int row, int col) {
        char symbol = currentPlayer;
        board[row][col] = symbol;
        
        // Record the move
        String move = currentPlayer + ": (" + row + ", " + col + ")";
        recordedMoves.add(move);
    }
    
    private void saveRecordedGame() {
        try {
            String fileName = "recorded_game.txt";
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write the recorded moves to the file
            for (String move : recordedMoves) {
                bufferedWriter.write(move);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();

            System.out.println("Recorded game saved to: " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving recorded game: " + e.getMessage());
        }
    }

    private void makeComputerMove() {
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(boardSize);
            col = random.nextInt(boardSize);
        } while (!isValidMove(row, col));

        placeSymbol(row, col);
        buttons[row][col].setText(Character.toString(currentPlayer));
        buttons[row][col].setEnabled(false);
        switchPlayers();
        if (isGameOver()) {
            char winner = determineWinner();
            String message;
            if (winner == ' ') {
                message = "It's a draw!";
            } else {
                message = "Player " + winner + " wins!";
            }
            JOptionPane.showMessageDialog(SOSGame.this, message);
            resetGame();
        }
        // Record the move
        String move = currentPlayer + ": (" + row + ", " + col + ")";
        recordedMoves.add(move);

        // Save the recorded game
        saveRecordedGame();
    }


    public boolean isValidMove(int row, int col) {
        return (row >= 0 && row < boardSize && col >= 0 && col < boardSize && board[row][col] == ' ');
    }
    
    private static PlayerType choosePlayerType(String playerColor) {
        String input = JOptionPane.showInputDialog("Choose " + playerColor + " player type (human or computer):");
        return (input.equalsIgnoreCase("human")) ? PlayerType.HUMAN : PlayerType.COMPUTER;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int boardSize = Integer.parseInt(JOptionPane.showInputDialog("Enter the size of the board:"));
                String gameMode = JOptionPane.showInputDialog("Enter the game mode (simple or general):");

                PlayerType redPlayerType = choosePlayerType("Red");
                PlayerType bluePlayerType = choosePlayerType("Blue");

                SOSGame game = new SOSGame(boardSize, gameMode, redPlayerType, bluePlayerType);
                game.setVisible(true);
            }
        });
    }
}