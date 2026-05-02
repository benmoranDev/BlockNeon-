package br.com.blockneon.model;

public class ActivePiece {

    private final Tetromino tetromino;
    private int row;
    private int col;
    private int rotation;

    public ActivePiece(Tetromino tetromino, int row, int col) {
        this.tetromino = tetromino;
        this.row = row;
        this.col = col;
        this.rotation = 0;
    }

    public Tetromino getTetromino() { return tetromino; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public void moveDown() { row--; }
    public void moveUp() { row++; }
    public void moveLeft() { col--; }
    public void moveRight() { col++; }

    public void rotateRight() {
        rotation = (rotation + 1) % 4;
    }

    public void rotateLeft() {
        rotation = (rotation + 3) % 4;
    }

    public int[][] getRotatedCells() {
        int[][] result = copyMatrix(tetromino.getCells());
        for (int i = 0; i < rotation; i++) {
            result = rotateClockwise(result);
        }
        return result;
    }

    private int[][] copyMatrix(int[][] src) {
        int size = src.length;
        int[][] copy = new int[size][size];
        for (int r = 0; r < size; r++) {
            System.arraycopy(src[r], 0, copy[r], 0, size);
        }
        return copy;
    }

    private int[][] rotateClockwise(int[][] matrix) {
        int size = matrix.length;
        int[][] rotated = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                rotated[c][size - 1 - r] = matrix[r][c];
            }
        }
        return rotated;
    }
}
