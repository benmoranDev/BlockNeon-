package br.com.blockneon.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import br.com.blockneon.model.ActivePiece;
import br.com.blockneon.model.Board;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;


public class BoardRenderer {

    // =========================================================
    // Palette / Paleta
    // =========================================================

    private static final Color BLOCK_I = new Color(0.20f, 0.88f, 1.00f, 1f);
    private static final Color BLOCK_J = new Color(0.28f, 0.46f, 1.00f, 1f);
    private static final Color BLOCK_L = new Color(1.00f, 0.58f, 0.12f, 1f);
    private static final Color BLOCK_O = new Color(1.00f, 0.83f, 0.18f, 1f);
    private static final Color BLOCK_S = new Color(0.34f, 0.94f, 0.26f, 1f);
    private static final Color BLOCK_T = new Color(0.88f, 0.28f, 1.00f, 1f);
    private static final Color BLOCK_Z = new Color(1.00f, 0.22f, 0.20f, 1f);
    private static final Color GHOST_COLOR = new Color(0.82f, 0.82f, 0.92f, 1f);

    private final ShapeRenderer shapeRenderer;

    public BoardRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    /**
     * Renders background, board and gameplay blocks.
     * Renderiza fundo, tabuleiro e blocos de gameplay.
     */
    public void render(OrthographicCamera camera, GameLayout layout, GameSession session, float time) {
        shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBackgroundDecor(time);
        drawBoardPanel(layout);
        drawEmptyCells(layout);
        drawLockedCells(layout, session);
        drawGhostPiece(layout, session);
        drawActivePiece(layout, session, time);
        drawLineClearFlash(layout, session);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawBoardGrid(layout);
        drawBoardFrameHighlights(layout);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Draws the animated background.
     * Desenha o fundo animado.
     */
    private void drawBackgroundDecor(float time) {
        float pulse = (float) ((Math.sin(time * 1.6f) + 1f) * 0.5f);

        shapeRenderer.setColor(0.05f, 0.02f, 0.12f, 1f);
        shapeRenderer.rect(0, 0, GameLayout.WORLD_WIDTH, GameLayout.WORLD_HEIGHT);

        shapeRenderer.setColor(0.18f, 0.06f, 0.34f, 1f);
        shapeRenderer.rect(0, 0, GameLayout.WORLD_WIDTH, GameLayout.WORLD_HEIGHT * 0.82f);

        shapeRenderer.setColor(0.44f, 0.16f, 0.82f, 1f);
        shapeRenderer.rect(0, 0, GameLayout.WORLD_WIDTH, GameLayout.WORLD_HEIGHT * 0.38f);

        shapeRenderer.setColor(0.80f, 0.82f, 1f, 0.10f + pulse * 0.03f);
        shapeRenderer.rect(0, 150f, GameLayout.WORLD_WIDTH, 26f);

        shapeRenderer.setColor(0.95f, 0.95f, 1f, 0.05f);
        shapeRenderer.rect(0, 120f, GameLayout.WORLD_WIDTH, 10f);
    }

    /**
     * Draws the main board panel.
     * Desenha o painel principal do tabuleiro.
     */
    private void drawBoardPanel(GameLayout layout) {
        shapeRenderer.setColor(0.36f, 0.32f, 0.44f, 1f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f
        );

        shapeRenderer.setColor(0.20f, 0.19f, 0.24f, 1f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING + 4f,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING + 4f,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f - 8f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f - 8f
        );

        shapeRenderer.setColor(0.05f, 0.06f, 0.08f, 1f);
        shapeRenderer.rect(layout.boardX, layout.boardY, GameLayout.BOARD_WIDTH, GameLayout.BOARD_HEIGHT);

        shapeRenderer.setColor(0.10f, 0.11f, 0.13f, 0.30f);
        shapeRenderer.rect(
            layout.boardX + GameLayout.BOARD_INNER_PADDING,
            layout.boardY + GameLayout.BOARD_INNER_PADDING,
            GameLayout.BOARD_WIDTH - GameLayout.BOARD_INNER_PADDING * 2f,
            GameLayout.BOARD_HEIGHT - GameLayout.BOARD_INNER_PADDING * 2f
        );

        shapeRenderer.setColor(1f, 1f, 1f, 0.035f);
        shapeRenderer.rect(
            layout.boardX + 6f,
            layout.boardY + GameLayout.BOARD_HEIGHT - 14f,
            GameLayout.BOARD_WIDTH - 12f,
            7f
        );
    }

    /**
     * Draws frame outlines of the board.
     * Desenha os contornos da moldura do tabuleiro.
     */
    private void drawBoardFrameHighlights(GameLayout layout) {
        shapeRenderer.setColor(0.70f, 0.68f, 0.82f, 0.90f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f
        );

        shapeRenderer.setColor(0.12f, 0.10f, 0.16f, 0.70f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING + 4f,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING + 4f,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f - 8f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f - 8f
        );

        shapeRenderer.setColor(0.34f, 0.34f, 0.40f, 0.90f);
        shapeRenderer.rect(layout.boardX, layout.boardY, GameLayout.BOARD_WIDTH, GameLayout.BOARD_HEIGHT);
    }

    /**
     * Draws the empty board cells.
     * Desenha as células vazias do tabuleiro.
     */
    private void drawEmptyCells(GameLayout layout) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                float x = layout.boardX + col * GameLayout.CELL_SIZE;
                float y = layout.boardY + row * GameLayout.CELL_SIZE;

                shapeRenderer.setColor(0.08f, 0.08f, 0.09f, 0.78f);
                shapeRenderer.rect(x + 1f, y + 1f, GameLayout.CELL_SIZE - 2f, GameLayout.CELL_SIZE - 2f);

                shapeRenderer.setColor(1f, 1f, 1f, 0.015f);
                shapeRenderer.rect(x + 3f, y + GameLayout.CELL_SIZE - 8f, GameLayout.CELL_SIZE - 6f, 2f);
            }
        }
    }

    /**
     * Draws the board grid lines.
     * Desenha as linhas do grid do tabuleiro.
     */
    private void drawBoardGrid(GameLayout layout) {
        for (int col = 0; col <= Board.COLS; col++) {
            float x = layout.boardX + col * GameLayout.CELL_SIZE;
            shapeRenderer.setColor(0.18f, 0.18f, 0.20f, col == 0 || col == Board.COLS ? 0.32f : 0.14f);
            shapeRenderer.line(x, layout.boardY, x, layout.boardY + GameLayout.BOARD_HEIGHT);
        }

        for (int row = 0; row <= Board.ROWS; row++) {
            float y = layout.boardY + row * GameLayout.CELL_SIZE;
            shapeRenderer.setColor(0.18f, 0.18f, 0.20f, row == 0 || row == Board.ROWS ? 0.32f : 0.14f);
            shapeRenderer.line(layout.boardX, y, layout.boardX + GameLayout.BOARD_WIDTH, y);
        }
    }

    /**
     * Draws all locked cells.
     * Desenha todas as células travadas.
     */
    private void drawLockedCells(GameLayout layout, GameSession session) {
        Board board = session.getBoard();

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                int value = board.getCell(row, col);
                if (value == 0) continue;

                drawArcadeBoardBlock(
                    layout.boardX + col * GameLayout.CELL_SIZE + 2f,
                    layout.boardY + row * GameLayout.CELL_SIZE + 2f,
                    GameLayout.CELL_SIZE - 4f,
                    getColorForCell(value),
                    1f
                );
            }
        }
    }

    /**
     * Draws the active falling piece.
     * Desenha a peça ativa em queda.
     */
    private void drawActivePiece(GameLayout layout, GameSession session, float time) {
        ActivePiece activePiece = session.getActivePiece();
        if (activePiece == null) return;

        int[][] shape = activePiece.getRotatedCells();
        Color color = getColorForCell(activePiece.getTetromino().getColorId());

        float pulse = (float) ((Math.sin(time * 6f) + 1f) * 0.5f);
        float alpha = 0.92f + pulse * 0.10f;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;

                int boardRow = activePiece.getRow() + r;
                int boardCol = activePiece.getCol() + c;

                if (boardRow < 0 || boardRow >= Board.ROWS) continue;
                if (boardCol < 0 || boardCol >= Board.COLS) continue;

                drawArcadeBoardBlock(
                    layout.boardX + boardCol * GameLayout.CELL_SIZE + 2f,
                    layout.boardY + boardRow * GameLayout.CELL_SIZE + 2f,
                    GameLayout.CELL_SIZE - 4f,
                    color,
                    alpha
                );
            }
        }
    }

    /**
     * Draws the ghost landing piece.
     * Desenha a ghost piece de pouso.
     */
    private void drawGhostPiece(GameLayout layout, GameSession session) {
        ActivePiece activePiece = session.getActivePiece();
        if (activePiece == null) return;

        int ghostRow = computeGhostRow(session.getBoard(), activePiece);
        int[][] shape = activePiece.getRotatedCells();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;

                int boardRow = ghostRow + r;
                int boardCol = activePiece.getCol() + c;

                if (boardRow < 0 || boardRow >= Board.ROWS) continue;
                if (boardCol < 0 || boardCol >= Board.COLS) continue;

                drawGhostBlock(
                    layout.boardX + boardCol * GameLayout.CELL_SIZE + 2f,
                    layout.boardY + boardRow * GameLayout.CELL_SIZE + 2f,
                    GameLayout.CELL_SIZE - 4f,
                    GHOST_COLOR,
                    0.28f
                );
            }
        }
    }

    /**
     * Draws the line clear flash.
     * Desenha o flash de limpeza de linha.
     */
    private void drawLineClearFlash(GameLayout layout, GameSession session) {
        if (session.getLineFlashTimer() <= 0f || session.getFlashRows().size == 0) return;

        float progress = session.getLineFlashTimer() / GameSession.LINE_FLASH_DURATION;
        float alpha = progress * 0.85f;

        for (Integer row : session.getFlashRows()) {
            float y = layout.boardY + row * GameLayout.CELL_SIZE;

            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.32f);
            shapeRenderer.rect(layout.boardX, y + 2f, GameLayout.BOARD_WIDTH, GameLayout.CELL_SIZE - 4f);

            shapeRenderer.setColor(1f, 0.90f, 0.30f, alpha * 0.18f);
            shapeRenderer.rect(layout.boardX + 8f, y + 6f, GameLayout.BOARD_WIDTH - 16f, GameLayout.CELL_SIZE - 12f);
        }
    }

    /**
     * Draws one 2.5D arcade block.
     * Desenha um bloco arcade 2.5D.
     */
    private void drawArcadeBoardBlock(float x, float y, float size, Color baseColor, float alpha) {
        float shadowOffset = 3f;

        shapeRenderer.setColor(0f, 0f, 0f, 0.20f * alpha);
        shapeRenderer.rect(x + shadowOffset, y - shadowOffset, size, size);

        shapeRenderer.setColor(baseColor.r * 0.32f, baseColor.g * 0.32f, baseColor.b * 0.32f, 0.95f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(baseColor.r * 0.55f, baseColor.g * 0.55f, baseColor.b * 0.55f, 0.95f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(baseColor.r * 0.78f, baseColor.g * 0.78f, baseColor.b * 0.78f, 0.98f * alpha);
        shapeRenderer.rect(x + 4f, y + 4f, size - 8f, size - 8f);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 1f * alpha);
        shapeRenderer.rect(x + 6f, y + 6f, size - 12f, size - 12f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.16f * alpha);
        shapeRenderer.rect(x + 4f, y + size - 9f, size * 0.40f, 3f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.06f * alpha);
        shapeRenderer.rect(x + 6f, y + size - 13f, size - 14f, 2f);
    }

    /**
     * Draws one ghost block.
     * Desenha um bloco da ghost piece.
     */
    private void drawGhostBlock(float x, float y, float size, Color baseColor, float alpha) {
        shapeRenderer.setColor(baseColor.r * 0.30f, baseColor.g * 0.30f, baseColor.b * 0.30f, 0.25f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.15f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.08f * alpha);
        shapeRenderer.rect(x + 5f, y + 5f, size - 10f, size - 10f);
    }

    /**
     * Calculates the ghost landing row.
     * Calcula a linha de pouso da ghost piece.
     */
    private int computeGhostRow(Board board, ActivePiece piece) {
        ActivePiece testPiece = new ActivePiece(piece.getTetromino(), piece.getRow(), piece.getCol());

        int[][] currentShape = piece.getRotatedCells();
        int rotations = detectRotationSteps(piece.getTetromino().getCells(), currentShape);

        for (int i = 0; i < rotations; i++) {
            testPiece.rotateRight();
        }

        while (board.canPlace(testPiece)) {
            testPiece.moveDown();
        }
        testPiece.moveUp();

        return testPiece.getRow();
    }

    /**
     * Detects rotation steps.
     * Detecta os passos de rotação.
     */
    private int detectRotationSteps(int[][] original, int[][] rotated) {
        int[][] test = copyMatrix(original);

        for (int i = 0; i < 4; i++) {
            if (sameMatrix(test, rotated)) {
                return i;
            }
            test = rotateClockwise(test);
        }

        return 0;
    }

    /**
     * Copies a matrix.
     * Copia uma matriz.
     */
    private int[][] copyMatrix(int[][] src) {
        int[][] copy = new int[src.length][src[0].length];
        for (int r = 0; r < src.length; r++) {
            System.arraycopy(src[r], 0, copy[r], 0, src[r].length);
        }
        return copy;
    }

    /**
     * Compares matrices.
     * Compara matrizes.
     */
    private boolean sameMatrix(int[][] a, int[][] b) {
        if (a.length != b.length) return false;

        for (int r = 0; r < a.length; r++) {
            if (a[r].length != b[r].length) return false;

            for (int c = 0; c < a[r].length; c++) {
                if (a[r][c] != b[r][c]) return false;
            }
        }

        return true;
    }

    /**
     * Rotates a square matrix clockwise.
     * Rotaciona uma matriz quadrada no sentido horário.
     */
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

    /**
     * Returns the color associated with a cell id.
     * Retorna a cor associada ao id da célula.
     */
    private Color getColorForCell(int value) {
        switch (value) {
            case 1: return BLOCK_I;
            case 2: return BLOCK_J;
            case 3: return BLOCK_L;
            case 4: return BLOCK_O;
            case 5: return BLOCK_S;
            case 6: return BLOCK_T;
            case 7: return BLOCK_Z;
            default: return Color.WHITE;
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
