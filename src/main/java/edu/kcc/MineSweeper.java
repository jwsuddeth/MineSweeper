package edu.kcc;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



public class MineSweeper {

    private final int gridSize = 20;

    Cell[][] cells;

    private final JFrame frame;
    private JButton reset;
    private JButton giveUp;

    // use the same ActionListener for all buttons, so we don't have to create hundreds of them
    private final ActionListener actionListener = e -> {
        Object source = e.getSource();

        if (source == reset) {
            createMines();
        }
        else if (source == giveUp) {
            revealBoardAndDisplay("You gave up!");
        }
        else {
            handleCell( (Cell) source);
        }
    };


    class Cell extends JButton{
        private final int row;
        private final int col;
        private int value;

        Cell(final int row, final int col, final ActionListener actionListener){
            this.row = row;
            this.col = col;
            addActionListener(actionListener);
            setText("");
        }

        int getValue() { return value; }
        void setValue(int value) { this.value = value; }

        boolean isMine() { return value == 10;}

        void reset() {
            setValue(0);
            setEnabled(true);
            setText("");
        }

        void reveal() {
            setEnabled(false);
            setText(isMine() ? "X" : String.valueOf(value));
        }

        void updateNeighborCount() {
            Cell[] neighbors = getNeighbors();
            for (Cell neighbor : neighbors) {
                if (neighbor == null)break;

                if (neighbor.isMine()) {
                    value++;
                }
            }
        }

        Cell[] getNeighbors() {

            Cell [] neighbors = new Cell[8];

            int index = 0;
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                for (int colOffset = -1; colOffset <= 1; colOffset++) {

                    if (rowOffset == 0 && colOffset == 0) {
                        continue;
                    }

                    int rowValue = row + rowOffset;
                    int colValue = col + colOffset;
                    if (rowValue < 0 || rowValue >= gridSize || colValue < 0 || colValue >= gridSize) {
                        continue;
                    }

                    neighbors[index++] = cells[rowValue][colValue];
                }
            }

            return neighbors;
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Cell c = (Cell)obj;
            return row == c.row && col == c.col;
        }

        public int hashCode() {
            return Objects.hash(row, col);
        }
    }


    public MineSweeper() {
        cells = new Cell[gridSize][gridSize];

        frame = new JFrame("Minesweeper");
        frame.setSize(900, 900);
        frame.setLayout(new BorderLayout());

        initializeButtonPanel();
        initializeGrid();

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    private void initializeButtonPanel() {
        JPanel buttonPanel = new JPanel();

        reset = new JButton("Reset");
        giveUp = new JButton("Give Up");

        reset.addActionListener(actionListener);
        giveUp.addActionListener(actionListener);

        buttonPanel.add(reset);
        buttonPanel.add(giveUp);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initializeGrid() {
        Container grid = new Container();
        grid.setLayout(new GridLayout(gridSize, gridSize));

        for (int row=0; row< gridSize; row++) {
            for (int col=0; col<gridSize; col++) {
                cells[row][col] = new Cell(row, col, actionListener);
                grid.add(cells[row][col]);
            }
        }
        createMines();
        frame.add(grid, BorderLayout.CENTER);
    }

    private void resetAllCells() {
        for (int row=0; row< gridSize; row++) {
            for (int col=0; col<gridSize; col++) {
                cells[row][col].reset();
            }
        }
    }

    private void createMines() {
        resetAllCells();

        int mineCount = 20;
        Random random = new Random();

        // map cells to unique integers
        Set<Integer> positions = new HashSet<>(gridSize*gridSize);
        for (int row=0; row<gridSize; row++) {
            for (int col=0; col<gridSize; col++) {
                positions.add(row*gridSize + col);
            }
        }

        // Initialize mines
        for (int i=0; i<mineCount; i++) {
            int choice = random.nextInt(positions.size());
            int row = choice / gridSize;
            int col = choice % gridSize;
            cells[row][col].setValue(10);
            positions.remove(choice);
        }

        // initialize neighbor counts
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!cells[row][col].isMine()) {
                    cells[row][col].updateNeighborCount();
                }
            }
        }
    }

    private void handleCell(Cell cell) {
        if (cell.isMine()) {
            cell.setForeground(Color.RED);
            cell.reveal();
            revealBoardAndDisplay("You clicked on a mine!");
            return;
        }
        if (cell.getValue() == 0) {
            Set<Cell> positions = new HashSet<>();
            positions.add(cell);
            cascade(positions);
        } else {
            cell.reveal();
        }
        checkForWin();
    }

    private void revealBoardAndDisplay(String message) {

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!cells[row][col].isEnabled()) {
                    cells[row][col].reveal();
                }
            }
        }

        JOptionPane.showMessageDialog(
                frame, message, "Game Over",
                JOptionPane.ERROR_MESSAGE
        );

        createMines();
    }

    private void cascade(Set<Cell> positionsToClear) {
        while (!positionsToClear.isEmpty()) {
            // Set does not have a clean way for retrieving
            // a single element. This is the best way I could think of.
            Cell cell = positionsToClear.iterator().next();
            positionsToClear.remove(cell);
            cell.reveal();

            Cell [] neighbors = cell.getNeighbors();
            for (Cell neighbour : neighbors) {
                if (neighbour == null) {
                    break;
                }
                if (neighbour.getValue() == 0
                        && neighbour.isEnabled()) {
                    positionsToClear.add(neighbour);
                } else {
                    neighbour.reveal();
                }
            }
        }
    }

    private void checkForWin() {
        boolean won = true;
        outer:
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                if (!cell.isMine() && cell.isEnabled()) {
                    won = false;
                    break outer;
                }
            }
        }

        if (won) {
            JOptionPane.showMessageDialog(
                    frame, "You have won!", "Congratulations",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }	}

    public static void main(String[] args) {

        SwingUtilities.invokeLater(MineSweeper::new);
    }

}

