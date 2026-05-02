package br.com.blockneon.model;

public enum Tetromino {
    I(new int[][]{
        {0, 0, 0, 0},
        {1, 1, 1, 1},
        {0, 0, 0, 0},
        {0, 0, 0, 0}
    }, 1),
    J(new int[][]{
        {1, 0, 0},
        {1, 1, 1},
        {0, 0, 0}
    }, 2),
    L(new int[][]{
        {0, 0, 1},
        {1, 1, 1},
        {0, 0, 0}
    }, 3),
    O(new int[][]{
        {1, 1},
        {1, 1}
    }, 4),
    S(new int[][]{
        {0, 1, 1},
        {1, 1, 0},
        {0, 0, 0}
    }, 5),
    T(new int[][]{
        {0, 1, 0},
        {1, 1, 1},
        {0, 0, 0}
    }, 6),
    Z(new int[][]{
        {1, 1, 0},
        {0, 1, 1},
        {0, 0, 0}
    }, 7);

    private final int[][] cells;
    private final int colorId;

    Tetromino(int[][] cells, int colorId) {
        this.cells = cells;
        this.colorId = colorId;
    }

    public int[][] getCells() {
        return cells;
    }

    public int getColorId() {
        return colorId;
    }
}
