package br.com.blockneon.model;

import com.badlogic.gdx.utils.Array;

public class Board {

    public static final int COLS = 10;
    public static final int ROWS = 20;

    private final int[][] cells;

    public Board() {
        cells = new int[ROWS][COLS];
    }

    public int getCell(int row, int col) {
        return cells[row][col];
    }

    public int[][] getCells() {
        return cells;
    }

    /**
     * Clears full lines and returns the row indexes that were cleared.
     * This is useful for visual effects like line flash animations.
     */
    public Array<Integer> clearLinesWithInfo() {
        Array<Integer> clearedRows = new Array<>();

        for (int r = ROWS - 1; r >= 0; r--) {
            boolean isFull = true;

            for (int c = 0; c < COLS; c++) {
                if (cells[r][c] == 0) {
                    isFull = false;
                    break;
                }
            }

            if (isFull) {
                clearedRows.add(r);

                // Shift all rows above downward
                for (int moveRow = r; moveRow < ROWS - 1; moveRow++) {
                    for (int c = 0; c < COLS; c++) {
                        cells[moveRow][c] = cells[moveRow + 1][c];
                    }
                }

                // Clear top row after shifting
                for (int c = 0; c < COLS; c++) {
                    cells[ROWS - 1][c] = 0;
                }

                // Recheck same row after shifting content
                r++;
            }
        }

        return clearedRows;
    }

    /**
     * Compatibility helper if you still want only the total count.
     */
    public int clearLines() {
        return clearLinesWithInfo().size;
    }

    /**
     * Checks whether the piece can occupy its current position.
     */
    public boolean canPlace(ActivePiece piece) {
        int[][] shape = piece.getRotatedCells();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;

                int boardRow = piece.getRow() + r;
                int boardCol = piece.getCol() + c;

                // Piece may exist above visible top area
                if (boardRow >= ROWS) continue;

                // Hits walls or floor
                if (boardCol < 0 || boardCol >= COLS || boardRow < 0) {
                    return false;
                }

                // Hits locked block
                if (cells[boardRow][boardCol] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Locks the active piece into the fixed board matrix.
     */
    public void lockPiece(ActivePiece piece) {
        int[][] shape = piece.getRotatedCells();
        int colorId = piece.getTetromino().getColorId();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;

                int boardRow = piece.getRow() + r;
                int boardCol = piece.getCol() + c;

                if (boardRow >= 0 && boardRow < ROWS && boardCol >= 0 && boardCol < COLS) {
                    cells[boardRow][boardCol] = colorId;
                }
            }
        }
    }

    /**
     * Clears the whole board.
     */
    public void clear() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                cells[r][c] = 0;
            }
        }
    }
}
