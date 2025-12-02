// Import necessary libraries
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Main class to run the game
public class Project2Runner {
   /*
    ******** Project Description ********
    *
    * This project will be a fully interactive and animated game of Tic Tac Toe programmed using
    * Java Swing. This game will include two game modes: Human vs Human and Human vs AI, both of 
    * which can be configured at runtime by the user. The interface will consist of a number of 
    * Swing components including JLabels for showing status and score, a JButton to reset the game,
    * and a custom JPanel that will be responsible for 2D graphics to display X's, O's, and a dynamic
    * winning line. The game will be mouse-controlled using a MouseListener and AI thinking animations
    * utilizing a Timer and JLabel updates. The AI will be simple but efficient, capable of blocking
    * the player or winning if it can. The design will maintain good separation of game logic and UI
    * elements and will include thoughtful user experience features such as symbol choice and score
    * management. This project will demonstrate usage of Swing layout management, event-driven
    * programming, animation, custom drawing with Graphics2D, and conditional game logic.
    *
    ******** Swing Requirement ********
    *
    * This program uses 3 unique Swing components:
    * - JLabel for status (defined in TicTacToeGame class)
    * - JButton for reset (defined in TicTacToeGame class)
    * - JPanel for the game board (GamePanel class)
    * These are all part of the same file: Project2Runner.java.
    *
    ******** 2D Graphics Requirement ********
    *
    * The program uses a custom JPanel subclass called GamePanel (defined in Project2Runner.java).
    * Inside it, 2D graphics are drawn using Graphics2D in the paintComponent() method.
    * This includes the board grid, X and O shapes, and the dynamic winning line.
    *
    ******** Event Listener Requirement ********
    *
    * - An ActionListener is attached to the reset JButton
    * - Another ActionListener is used for the AI thinking animation
    * - A MouseListener is implemented in GamePanel, where mouseClicked() handles player moves.
    * These listeners all cause updates to the JPanel when triggered.
    */

    public static void main(String[] args) {
        // Start the game on the Event Dispatch Thread
        SwingUtilities.invokeLater(TicTacToeGame::new);
    }
}

// Main game frame class
class TicTacToeGame extends JFrame {
    private GamePanel panel; // The game board panel
    private final JLabel statusLabel; // Label to show current player's turn
    private final JButton resetButton; // Button to reset the game
    private final JLabel scoreLabel; // Label to show scores

    public TicTacToeGame() {
        // Set up the main game window
        setTitle("Tic Tac Toe");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout());

        // Initialize and configure the status label
        statusLabel = new JLabel("Player X's Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Initialize and configure the score label
        scoreLabel = new JLabel("Score - X: 0 | O: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Initialize and configure the reset button
        resetButton = new JButton("Reset Game");
        resetButton.addActionListener(e -> {
            remove(panel);
            setupGame();
        });

        // Add components to the frame
        add(statusLabel, BorderLayout.NORTH);
        add(resetButton, BorderLayout.SOUTH);
        add(scoreLabel, BorderLayout.AFTER_LAST_LINE);

        // Set up the initial game
        setupGame();
        setVisible(true);
    }

    // Initialize method to set up a new game
    private void setupGame() {
        // Ask user to choose game mode
        String[] options = {"Human vs Human", "Human vs AI"};
        int mode = JOptionPane.showOptionDialog(this, "Choose game mode:", "Game Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (mode == JOptionPane.CLOSED_OPTION) System.exit(0);

        // Ask user to choose their symbol
        String[] symbols = {"X", "O"};
        int symbolChoice = JOptionPane.showOptionDialog(this, "Choose your symbol:", "Choose Symbol",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, symbols, symbols[0]);
        if (symbolChoice == JOptionPane.CLOSED_OPTION) System.exit(0);

        // Determine game parameters based on user choices
        boolean vsAI = (mode == 1);
        String playerSymbol = symbols[symbolChoice];

        // Create new game panel with selected options
        panel = new GamePanel(statusLabel, scoreLabel, vsAI, playerSymbol);
        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}

// Game panel class that handles the actual game logic and drawing
class GamePanel extends JPanel implements MouseListener {
    private final String[][] board = new String[3][3]; // 3x3 game board
    private boolean xTurn; // Flag for whose turn it is
    private String winner; // Stores the winner (X, O, or Draw)
    private final JLabel statusLabel; // Reference to status label
    private final JLabel scoreLabel; // Reference to score label
    private int xWins = 0; // Count of X wins
    private int oWins = 0; // Count of O wins
    private final boolean vsAI; // Flag for AI mode
    private final String playerSymbol; // Player's symbol
    private final String aiSymbol; // AI's symbol
    private JLabel thinkingLabel; // Label to show AI is thinking
    private Timer thinkingTimer; // Timer for thinking animation
    private int thinkingDots = 0; // Counter for thinking animation
    private String[] dotStrings = {"", ".", "..", "..."}; // Animation states
    private int[] winningLine = null; // Stores coordinates of winning line

    // Initialize constructor
    public GamePanel(JLabel statusLabel, JLabel scoreLabel, boolean vsAI, String playerSymbol) {
        this.statusLabel = statusLabel;
        this.scoreLabel = scoreLabel;
        this.vsAI = vsAI;
        this.playerSymbol = playerSymbol;
        this.aiSymbol = playerSymbol.equals("X") ? "O" : "X";
        this.xTurn = true;
        this.winner = null;
        
        // Set up panel appearance
        setBackground(Color.WHITE);
        
        // Set up thinking label (for AI moves)
        thinkingLabel = new JLabel("", SwingConstants.CENTER);
        thinkingLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        thinkingLabel.setForeground(Color.GRAY);
        thinkingLabel.setVisible(false);
        
        // Set layout and add thinking label
        setLayout(new BorderLayout());
        add(thinkingLabel, BorderLayout.NORTH);
        
        // Set up timer for thinking animation
        thinkingTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thinkingDots = (thinkingDots + 1) % 4;
                thinkingLabel.setText("AI is thinking" + dotStrings[thinkingDots]);
            }
        });
        
        // Add mouse listener for player moves
        addMouseListener(this);

        // If playing against AI and AI goes first, make first move
        if (vsAI && playerSymbol.equals("O")) {
            showThinking();
            Timer firstMoveTimer = new Timer(1500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    makeAIMove();
                }
            });
            firstMoveTimer.setRepeats(false);
            firstMoveTimer.start();
        }
    }

    // Initialize method to show AI thinking animation
    private void showThinking() {
        thinkingLabel.setVisible(true);
        thinkingTimer.start();
    }

    // Initialize method to hide AI thinking animation
    private void hideThinking() {
        thinkingLabel.setVisible(false);
        thinkingTimer.stop();
    }

    // Initialize method to paint the game board
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));

        int width = getWidth();
        int height = getHeight();
        
        // Draw grid lines
        for (int i = 1; i < 3; i++) {
            g2.drawLine(i * width / 3, 0, i * width / 3, height);
            g2.drawLine(0, i * height / 3, width, i * height / 3);
        }

        // Draw X's (purple) and O's (green)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = col * width / 3;
                int y = row * height / 3;
                if ("X".equals(board[row][col])) {
                    g2.setColor(new Color(128, 0, 128)); // Purple
                    g2.drawLine(x + 20, y + 20, x + width/3 - 20, y + height/3 - 20);
                    g2.drawLine(x + width/3 - 20, y + 20, x + 20, y + height/3 - 20);
                } else if ("O".equals(board[row][col])) {
                    g2.setColor(new Color(0, 128, 0)); // Green
                    g2.drawOval(x + 20, y + 20, width/3 - 40, height/3 - 40);
                }
            }
        }
        
        // Draw extended winning line if game is won
        if (winningLine != null) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(5));
            
            int cellWidth = width / 3;
            int cellHeight = height / 3;
            int extension = 20; // Pixels to extend the line beyond the cells
            
            // Calculate line coordinates
            int startX = winningLine[1] * cellWidth + cellWidth/2;
            int startY = winningLine[0] * cellHeight + cellHeight/2;
            int endX = winningLine[3] * cellWidth + cellWidth/2;
            int endY = winningLine[2] * cellHeight + cellHeight/2;
            
            // Calculate direction and extend line beyond the cells
            double angle = Math.atan2(endY - startY, endX - startX);
            int extendedStartX = (int)(startX - extension * Math.cos(angle));
            int extendedStartY = (int)(startY - extension * Math.sin(angle));
            int extendedEndX = (int)(endX + extension * Math.cos(angle));
            int extendedEndY = (int)(endY + extension * Math.sin(angle));
            
            g2.drawLine(extendedStartX, extendedStartY, extendedEndX, extendedEndY);
        }
    }

    // Initialize method to handle mouse clicks for player moves
    @Override
    public void mouseClicked(MouseEvent e) {
        if (winner != null) return; // Game already over

        String currentPlayer = xTurn ? "X" : "O";
        if (vsAI && !currentPlayer.equals(playerSymbol)) return; // Not player's turn in AI mode

        // Determine which cell was clicked
        int col = e.getX() / (getWidth() / 3);
        int row = e.getY() / (getHeight() / 3);

        // If cell is empty, make move
        if (board[row][col] == null) {
            board[row][col] = currentPlayer;
            repaint();
            
            // Check for winner and update game state
            SwingUtilities.invokeLater(() -> {
                winner = checkWinner();
                if (winner != null) {
                    repaint();
                }
                xTurn = !xTurn;
                updateStatus();
                
                // If playing against AI and game isn't over, let AI make move
                if (vsAI && winner == null) {
                    showThinking();
                    Timer aiMoveTimer = new Timer(1500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            makeAIMove();
                        }
                    });
                    aiMoveTimer.setRepeats(false);
                    aiMoveTimer.start();
                }
            });
        }
    }

    // Initialize method to get list of empty cells
    private List<int[]> getEmptyCells() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == null) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        return emptyCells;
    }

    // Initialize method for AI to make a move
    private void makeAIMove() {
        hideThinking();
        List<int[]> emptyCells = getEmptyCells();
        if (emptyCells.isEmpty()) return;

        // First, check if AI can win immediately
        for (int[] cell : emptyCells) {
            if (wouldWin(aiSymbol, cell[0], cell[1])) {
                board[cell[0]][cell[1]] = aiSymbol;
                repaint();
                winner = checkWinner();
                if (winner != null) {
                    repaint();
                }
                finishAIMove();
                return;
            }
        }

        // Then, check if player is about to win and block them
        for (int[] cell : emptyCells) {
            if (wouldWin(playerSymbol, cell[0], cell[1])) {
                board[cell[0]][cell[1]] = aiSymbol;
                repaint();
                winner = checkWinner();
                if (winner != null) {
                    repaint();
                }
                finishAIMove();
                return;
            }
        }

        // Otherwise, make a random move
        int[] randomCell = emptyCells.get((int)(Math.random() * emptyCells.size()));
        board[randomCell[0]][randomCell[1]] = aiSymbol;
        repaint();
        winner = checkWinner();
        if (winner != null) {
            repaint();
        }
        finishAIMove();
    }

    // Initialize method to check if placing a symbol at given position would result in a win
    private boolean wouldWin(String symbol, int row, int col) {
        board[row][col] = symbol;
        boolean win = Objects.equals(checkWinner(), symbol);
        board[row][col] = null;
        return win;
    }

    // Initialize method to complete AI move by updating turn and status
    private void finishAIMove() {
        xTurn = !xTurn;
        updateStatus();
    }

    // Initialize method to show game over dialog with options
    private void showGameOverDialog() {
        String[] options = {"Play Again", "Change Mode", "Quit"};
        int choice = JOptionPane.showOptionDialog(
            this,
            winner.equals("Draw") ? "It's a draw!" : "Player " + winner + " wins!",
            "Game Over",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );

        // Handle user choice
        if (choice == 0) {
            resetGame();
        } else if (choice == 1) {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            parentWindow.dispose();
            new TicTacToeGame();
        } else {
            System.exit(0);
        }
    }

    // Initialize method to update status label and score
    private void updateStatus() {
        if (winner != null) {
            // Update score
            if (winner.equals("X")) xWins++;
            else if (winner.equals("O")) oWins++;
            
            // Show winner with slight delay
            Timer delayTimer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    statusLabel.setText(winner.equals("Draw") ? "It's a Draw!" : "Player " + winner + " Wins!");
                    scoreLabel.setText("Score - X: " + xWins + " | O: " + oWins);
                    showGameOverDialog();
                }
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        } else {
            // Show whose turn it is
            statusLabel.setText("Player " + (xTurn ? "X" : "O") + "'s Turn");
        }
    }

    // Initialize method to check if there's a winner or draw
    private String checkWinner() {
        // Check rows for winner
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
                winningLine = new int[]{i, 0, i, 2}; // Store winning line coordinates
                return board[i][0];
            }
        }
        
        // Check columns for winner
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) {
                winningLine = new int[]{0, i, 2, i}; // Store winning line coordinates
                return board[0][i];
            }
        }
        
        // Check diagonals for winner
        if (board[0][0] != null && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
            winningLine = new int[]{0, 0, 2, 2}; // Store winning line coordinates
            return board[0][0];
        }
        if (board[0][2] != null && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            winningLine = new int[]{0, 2, 2, 0}; // Store winning line coordinates
            return board[0][2];
        }

        // Check for draw (no empty spaces left)
        for (String[] row : board) {
            for (String cell : row) {
                if (cell == null) {
                    winningLine = null;
                    return null; // Game continues
                }
            }
        }
        winningLine = null;
        return "Draw"; // Game is a draw
    }

    // Initialize method to reset the game board
    private void resetGame() {
        // Clear the board
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = null;
            }
        }
        // Reset game state
        xTurn = true;
        winner = null;
        winningLine = null;
        statusLabel.setText("Player " + (xTurn ? "X" : "O") + "'s Turn");
        repaint();

        // If playing against AI and AI goes first, make first move
        if (vsAI && playerSymbol.equals("O")) {
            showThinking();
            Timer firstMoveTimer = new Timer(1500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    makeAIMove();
                }
            });
            firstMoveTimer.setRepeats(false);
            firstMoveTimer.start();
        }
    }

    // Unused mouse event methods (required by MouseListener interface)
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}