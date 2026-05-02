package br.com.blockneon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import br.com.blockneon.model.ActivePiece;
import br.com.blockneon.model.Board;
import br.com.blockneon.model.Tetromino;

public class GameRenderer {

    // =========================================================
    // Cyber blue glass palette / Paleta cyber blue glass
    // =========================================================

    private static final Color BG_1 = new Color(0.03f, 0.07f, 0.12f, 1f);
    private static final Color BG_2 = new Color(0.04f, 0.12f, 0.20f, 1f);

    private static final Color GRID_MAJOR = new Color(0.20f, 0.85f, 1.00f, 0.14f);
    private static final Color GRID_MINOR = new Color(0.20f, 0.85f, 1.00f, 0.04f);

    private static final Color PANEL_GLASS = new Color(0.05f, 0.16f, 0.24f, 0.52f);
    private static final Color PANEL_GLASS_INNER = new Color(0.08f, 0.22f, 0.32f, 0.18f);
    private static final Color PANEL_OUTLINE = new Color(0.28f, 0.92f, 1.00f, 0.78f);
    private static final Color PANEL_GLOW = new Color(0.28f, 0.92f, 1.00f, 0.09f);

    private static final Color TEXT_MAIN = new Color(0.92f, 0.98f, 1.00f, 0.96f);
    private static final Color TEXT_SOFT = new Color(0.72f, 0.88f, 1.00f, 0.68f);
    private static final Color TEXT_GLOW = new Color(0.25f, 0.95f, 1.00f, 0.18f);

    private static final Color BLOCK_I = new Color(0.25f, 0.95f, 1.00f, 1f);
    private static final Color BLOCK_J = new Color(0.35f, 0.55f, 1.00f, 1f);
    private static final Color BLOCK_L = new Color(1.00f, 0.72f, 0.20f, 1f);
    private static final Color BLOCK_O = new Color(1.00f, 0.92f, 0.35f, 1f);
    private static final Color BLOCK_S = new Color(0.38f, 1.00f, 0.45f, 1f);
    private static final Color BLOCK_T = new Color(0.88f, 0.45f, 1.00f, 1f);
    private static final Color BLOCK_Z = new Color(1.00f, 0.34f, 0.42f, 1f);
    private static final Color GHOST_COLOR = new Color(0.65f, 0.92f, 1.00f, 1f);

    // =========================================================
    // Rendering / Renderização
    // =========================================================

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;

    private final BitmapFont titleFont;
    private final BitmapFont labelFont;
    private final BitmapFont valueFont;
    private final BitmapFont hintFont;
    private final BitmapFont brandFont;

    public GameRenderer() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.00f);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(0.90f);

        valueFont = new BitmapFont();
        valueFont.getData().setScale(1.35f);

        hintFont = new BitmapFont();
        hintFont.getData().setScale(0.76f);

        brandFont = new BitmapFont();
        brandFont.getData().setScale(2.60f);
    }

    /**
     * Renders the complete game scene.
     * Renderiza a cena completa do jogo.
     */
    public void render(OrthographicCamera camera, GameLayout layout, GameSession session, float time) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBackgroundDecor(time);
        drawBoardAmbientGlow(layout, time);
        drawSidePanels(layout);
        drawSidebarBoxes(layout);
        drawBoardPanel(layout);
        drawBoardInnerGlass(layout);
        drawEmptyCells(layout);
        drawBottomEnergyGlow(layout, time);
        drawLockedCells(layout, session);
        drawGhostPiece(layout, session);
        drawActivePiece(layout, session, time);
        drawLineClearFlash(layout, session);
        drawHoldPiecePreview(layout, session);
        drawNextQueuePreviews(layout, session);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawSidePanelOutlines(layout);
        drawSidebarBoxOutlines(layout);
        drawBoardGrid(layout);
        drawBoardFrameHighlights(layout);
        endCornerAccents(layout);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        drawHudText(layout, session);
    }

    // =========================================================
    // Background / Fundo
    // =========================================================

    /**
     * Draws the animated cyber background with a tech grid.
     * Desenha o fundo cyber animado com grid técnico.
     */
    private void drawBackgroundDecor(float time) {
        float pulse = (MathUtils.sin(time * 1.7f) + 1f) * 0.5f;

        shapeRenderer.setColor(BG_1);
        shapeRenderer.rect(0, 0, GameLayout.WORLD_WIDTH, GameLayout.WORLD_HEIGHT);

        shapeRenderer.setColor(BG_2.r, BG_2.g, BG_2.b, 0.88f);
        shapeRenderer.rect(0, 0, GameLayout.WORLD_WIDTH, GameLayout.WORLD_HEIGHT * 0.78f);

        shapeRenderer.setColor(0.10f, 0.55f, 0.85f, 0.08f + pulse * 0.03f);
        shapeRenderer.rect(0, 180f, GameLayout.WORLD_WIDTH, 260f);

        shapeRenderer.setColor(0.18f, 0.80f, 1.00f, 0.04f);
        shapeRenderer.rect(0, 100f, GameLayout.WORLD_WIDTH, 90f);

        for (int x = 0; x <= (int) GameLayout.WORLD_WIDTH; x += 28) {
            Color lineColor = x % 56 == 0 ? GRID_MAJOR : GRID_MINOR;
            shapeRenderer.setColor(lineColor);
            shapeRenderer.rect(x, 0, 1f, GameLayout.WORLD_HEIGHT);
        }

        for (int y = 0; y <= (int) GameLayout.WORLD_HEIGHT; y += 28) {
            Color lineColor = y % 56 == 0 ? GRID_MAJOR : GRID_MINOR;
            shapeRenderer.setColor(lineColor);
            shapeRenderer.rect(0, y, GameLayout.WORLD_WIDTH, 1f);
        }

        shapeRenderer.setColor(0.90f, 1.00f, 1.00f, 0.035f);
        shapeRenderer.rect(0, 210f, GameLayout.WORLD_WIDTH, 2f);
    }

    /**
     * Draws ambient glow around the board area.
     * Desenha brilho ambiente ao redor da área do tabuleiro.
     */
    private void drawBoardAmbientGlow(GameLayout layout, float time) {
        float pulse = (MathUtils.sin(time * 2.2f) + 1f) * 0.5f;

        shapeRenderer.setColor(0.18f, 0.90f, 1.00f, 0.035f + pulse * 0.02f);
        shapeRenderer.rect(
            layout.boardX - 24f,
            layout.boardY - 24f,
            GameLayout.BOARD_WIDTH + 48f,
            GameLayout.BOARD_HEIGHT + 48f
        );

        shapeRenderer.setColor(0.60f, 0.25f, 1.00f, 0.018f + pulse * 0.012f);
        shapeRenderer.rect(
            layout.boardX - 10f,
            layout.boardY + GameLayout.BOARD_HEIGHT * 0.45f,
            GameLayout.BOARD_WIDTH + 20f,
            GameLayout.BOARD_HEIGHT * 0.25f
        );
    }

    // =========================================================
    // Side panels / Painéis laterais
    // =========================================================

    /**
     * Draws the left and right glass shells.
     * Desenha as carcaças de vidro esquerda e direita.
     */
    private void drawSidePanels(GameLayout layout) {
        drawGlassShell(layout.leftShellBounds);
        drawGlassShell(layout.rightShellBounds);
    }

    /**
     * Draws one glass shell.
     * Desenha uma carcaça de vidro.
     */
    private void drawGlassShell(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_GLOW);
        shapeRenderer.rect(bounds.x - 4f, bounds.y - 4f, bounds.width + 8f, bounds.height + 8f);

        shapeRenderer.setColor(PANEL_GLASS);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(PANEL_GLASS_INNER);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.04f);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + bounds.height - 10f, bounds.width - 8f, 2f);
    }

    /**
     * Draws side shell outlines.
     * Desenha os contornos das carcaças laterais.
     */
    private void drawSidePanelOutlines(GameLayout layout) {
        drawGlassShellOutline(layout.leftShellBounds);
        drawGlassShellOutline(layout.rightShellBounds);
    }

    /**
     * Draws one glass shell outline.
     * Desenha o contorno de uma carcaça de vidro.
     */
    private void drawGlassShellOutline(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_OUTLINE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(0.20f, 0.90f, 1.00f, 0.16f);
        shapeRenderer.rect(bounds.x + 2f, bounds.y + 2f, bounds.width - 4f, bounds.height - 4f);
    }

    /**
     * Draws the inner glass HUD boxes.
     * Desenha as caixas internas do HUD em vidro.
     */
    private void drawSidebarBoxes(GameLayout layout) {
        drawGlassBox(layout.holdBox);
        drawGlassBox(layout.levelBox);
        drawGlassBox(layout.goalBox);

        drawGlassBox(layout.nextMainBox);
        drawGlassBox(layout.nextQueueBox1);
        drawGlassBox(layout.nextQueueBox2);
        drawGlassBox(layout.nextQueueBox3);
        drawGlassBox(layout.nextQueueBox4);
    }

    /**
     * Draws outlines for all inner glass HUD boxes.
     * Desenha os contornos de todas as caixas internas de vidro.
     */
    private void drawSidebarBoxOutlines(GameLayout layout) {
        drawGlassBoxOutline(layout.holdBox);
        drawGlassBoxOutline(layout.levelBox);
        drawGlassBoxOutline(layout.goalBox);

        drawGlassBoxOutline(layout.nextMainBox);
        drawGlassBoxOutline(layout.nextQueueBox1);
        drawGlassBoxOutline(layout.nextQueueBox2);
        drawGlassBoxOutline(layout.nextQueueBox3);
        drawGlassBoxOutline(layout.nextQueueBox4);
    }

    /**
     * Draws one glass box.
     * Desenha uma caixa de vidro.
     */
    private void drawGlassBox(Rectangle bounds) {
        shapeRenderer.setColor(0.16f, 0.80f, 1.00f, 0.05f);
        shapeRenderer.rect(bounds.x - 2f, bounds.y - 2f, bounds.width + 4f, bounds.height + 4f);

        shapeRenderer.setColor(PANEL_GLASS);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(PANEL_GLASS_INNER);
        shapeRenderer.rect(bounds.x + 2f, bounds.y + 2f, bounds.width - 4f, bounds.height - 4f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + bounds.height - 8f, bounds.width - 6f, 2f);
    }

    /**
     * Draws one glass box outline.
     * Desenha o contorno de uma caixa de vidro.
     */
    private void drawGlassBoxOutline(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_OUTLINE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // =========================================================
    // Board drawing / Desenho do tabuleiro
    // =========================================================

    /**
     * Draws the main glass board frame.
     * Desenha a moldura principal em vidro do tabuleiro.
     */
    private void drawBoardPanel(GameLayout layout) {
        shapeRenderer.setColor(0.18f, 0.85f, 1.00f, 0.08f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING - 6f,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING - 6f,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f + 12f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f + 12f
        );

        shapeRenderer.setColor(0.04f, 0.14f, 0.20f, 0.62f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f
        );

        shapeRenderer.setColor(0.03f, 0.10f, 0.15f, 0.52f);
        shapeRenderer.rect(layout.boardX, layout.boardY, GameLayout.BOARD_WIDTH, GameLayout.BOARD_HEIGHT);
    }

    /**
     * Draws the inner glass layer inside the board.
     * Desenha a camada interna de vidro dentro do tabuleiro.
     */
    private void drawBoardInnerGlass(GameLayout layout) {
        shapeRenderer.setColor(0.22f, 0.88f, 1.00f, 0.03f);
        shapeRenderer.rect(
            layout.boardX + GameLayout.BOARD_INNER_PADDING,
            layout.boardY + GameLayout.BOARD_INNER_PADDING,
            GameLayout.BOARD_WIDTH - GameLayout.BOARD_INNER_PADDING * 2f,
            GameLayout.BOARD_HEIGHT - GameLayout.BOARD_INNER_PADDING * 2f
        );

        shapeRenderer.setColor(1f, 1f, 1f, 0.025f);
        shapeRenderer.rect(
            layout.boardX + 6f,
            layout.boardY + GameLayout.BOARD_HEIGHT - 10f,
            GameLayout.BOARD_WIDTH - 12f,
            2f
        );
    }

    /**
     * Draws the board frame highlights.
     * Desenha os highlights da moldura do tabuleiro.
     */
    private void drawBoardFrameHighlights(GameLayout layout) {
        shapeRenderer.setColor(0.28f, 0.92f, 1.00f, 0.60f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING,
            GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f
        );

        shapeRenderer.setColor(0.75f, 0.40f, 1.00f, 0.08f);
        shapeRenderer.rect(
            layout.boardX - 2f,
            layout.boardY - 2f,
            GameLayout.BOARD_WIDTH + 4f,
            GameLayout.BOARD_HEIGHT + 4f
        );

        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(layout.boardX, layout.boardY, GameLayout.BOARD_WIDTH, GameLayout.BOARD_HEIGHT);
    }

    /**
     * Draws small end accents on the board corners.
     * Desenha pequenos acentos nas quinas do tabuleiro.
     */
    private void endCornerAccents(GameLayout layout) {
        float bx = layout.boardX - GameLayout.BOARD_FRAME_PADDING;
        float by = layout.boardY - GameLayout.BOARD_FRAME_PADDING;
        float bw = GameLayout.BOARD_WIDTH + GameLayout.BOARD_FRAME_PADDING * 2f;
        float bh = GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f;
        float s = 18f;

        shapeRenderer.setColor(0.28f, 0.92f, 1.00f, 0.75f);

        shapeRenderer.line(bx, by, bx + s, by);
        shapeRenderer.line(bx, by, bx, by + s);

        shapeRenderer.line(bx + bw - s, by, bx + bw, by);
        shapeRenderer.line(bx + bw, by, bx + bw, by + s);

        shapeRenderer.line(bx, by + bh - s, bx, by + bh);
        shapeRenderer.line(bx, by + bh, bx + s, by + bh);

        shapeRenderer.line(bx + bw - s, by + bh, bx + bw, by + bh);
        shapeRenderer.line(bx + bw, by + bh - s, bx + bw, by + bh);
    }

    /**
     * Draws empty board slots.
     * Desenha os slots vazios do tabuleiro.
     */
    private void drawEmptyCells(GameLayout layout) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                float x = layout.boardX + col * GameLayout.CELL_SIZE;
                float y = layout.boardY + row * GameLayout.CELL_SIZE;

                shapeRenderer.setColor(0.08f, 0.12f, 0.16f, 0.30f);
                shapeRenderer.rect(x + 1.5f, y + 1.5f, GameLayout.CELL_SIZE - 3f, GameLayout.CELL_SIZE - 3f);

                shapeRenderer.setColor(0.20f, 0.85f, 1.00f, 0.010f);
                shapeRenderer.rect(x + 4f, y + 4f, GameLayout.CELL_SIZE - 8f, GameLayout.CELL_SIZE - 8f);

                shapeRenderer.setColor(1f, 1f, 1f, 0.02f);
                shapeRenderer.rect(x + 5f, y + GameLayout.CELL_SIZE - 8f, GameLayout.CELL_SIZE - 10f, 2f);
            }
        }
    }

    /**
     * Draws the board grid.
     * Desenha o grid do tabuleiro.
     */
    private void drawBoardGrid(GameLayout layout) {
        for (int col = 0; col <= Board.COLS; col++) {
            float x = layout.boardX + col * GameLayout.CELL_SIZE;

            float alpha = (col == 0 || col == Board.COLS) ? 0.16f : 0.05f;
            if (col % 2 == 0 && col > 0 && col < Board.COLS) alpha += 0.015f;

            shapeRenderer.setColor(0.55f, 0.82f, 1f, alpha);
            shapeRenderer.line(x, layout.boardY, x, layout.boardY + GameLayout.BOARD_HEIGHT);
        }

        for (int row = 0; row <= Board.ROWS; row++) {
            float y = layout.boardY + row * GameLayout.CELL_SIZE;

            float alpha = (row == 0 || row == Board.ROWS) ? 0.16f : 0.05f;
            if (row % 4 == 0 && row > 0 && row < Board.ROWS) alpha += 0.012f;

            shapeRenderer.setColor(0.55f, 0.82f, 1f, alpha);
            shapeRenderer.line(layout.boardX, y, layout.boardX + GameLayout.BOARD_WIDTH, y);
        }
    }

    /**
     * Draws a glow bar under the board like an energy base.
     * Desenha uma barra de brilho sob o tabuleiro como base de energia.
     */
    private void drawBottomEnergyGlow(GameLayout layout, float time) {
        float pulse = (MathUtils.sin(time * 5f) + 1f) * 0.5f;
        float glowY = layout.boardY - 10f;

        shapeRenderer.setColor(0.18f, 0.90f, 1.00f, 0.08f + pulse * 0.04f);
        shapeRenderer.rect(layout.boardX - 10f, glowY - 8f, GameLayout.BOARD_WIDTH + 20f, 20f);

        shapeRenderer.setColor(0.80f, 0.35f, 1.00f, 0.04f + pulse * 0.02f);
        shapeRenderer.rect(layout.boardX + 30f, glowY - 2f, GameLayout.BOARD_WIDTH - 60f, 10f);

        shapeRenderer.setColor(0.85f, 1.00f, 1.00f, 0.12f);
        shapeRenderer.rect(layout.boardX + 8f, glowY, GameLayout.BOARD_WIDTH - 16f, 2f);
    }

    /**
     * Draws locked board cells.
     * Desenha as células travadas do tabuleiro.
     */
    private void drawLockedCells(GameLayout layout, GameSession session) {
        Board board = session.getBoard();

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                int value = board.getCell(row, col);
                if (value == 0) continue;

                drawNeonBlockInternal(
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
     * Draws the active piece with pulse glow.
     * Desenha a peça ativa com brilho pulsante.
     */
    private void drawActivePiece(GameLayout layout, GameSession session, float time) {
        ActivePiece activePiece = session.getActivePiece();
        if (activePiece == null) return;

        int[][] shape = activePiece.getRotatedCells();
        Color color = getColorForCell(activePiece.getTetromino().getColorId());

        float pulse = (MathUtils.sin(time * 6.5f) + 1f) * 0.5f;
        float activeAlpha = 0.92f + pulse * 0.16f;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;

                int boardRow = activePiece.getRow() + r;
                int boardCol = activePiece.getCol() + c;

                if (boardRow < 0 || boardRow >= Board.ROWS) continue;
                if (boardCol < 0 || boardCol >= Board.COLS) continue;

                drawNeonBlockInternal(
                    layout.boardX + boardCol * GameLayout.CELL_SIZE + 2f,
                    layout.boardY + boardRow * GameLayout.CELL_SIZE + 2f,
                    GameLayout.CELL_SIZE - 4f,
                    color,
                    activeAlpha
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
     * Draws the line clear flash effect.
     * Desenha o efeito de flash ao limpar linhas.
     */
    private void drawLineClearFlash(GameLayout layout, GameSession session) {
        if (session.getLineFlashTimer() <= 0f || session.getFlashRows().size == 0) return;

        float progress = session.getLineFlashTimer() / GameSession.LINE_FLASH_DURATION;
        float alpha = progress * 0.85f;

        for (Integer row : session.getFlashRows()) {
            float y = layout.boardY + row * GameLayout.CELL_SIZE;

            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.34f);
            shapeRenderer.rect(layout.boardX, y + 3f, GameLayout.BOARD_WIDTH, GameLayout.CELL_SIZE - 6f);

            shapeRenderer.setColor(0f, 1f, 1f, alpha * 0.18f);
            shapeRenderer.rect(layout.boardX - 2f, y + 1f, GameLayout.BOARD_WIDTH + 4f, GameLayout.CELL_SIZE - 2f);

            shapeRenderer.setColor(0.80f, 0.35f, 1.00f, alpha * 0.12f);
            shapeRenderer.rect(layout.boardX + 10f, y + 6f, GameLayout.BOARD_WIDTH - 20f, GameLayout.CELL_SIZE - 12f);
        }
    }

    // =========================================================
    // Previews / Previews
    // =========================================================

    /**
     * Draws the hold preview piece.
     * Desenha a peça guardada no hold.
     */
    private void drawHoldPiecePreview(GameLayout layout, GameSession session) {
        Tetromino heldTetromino = session.getHeldTetromino();
        if (heldTetromino == null) return;

        drawPreviewPieceInBox(heldTetromino, layout.holdBox, 18f);
    }

    /**
     * Draws the next queue previews.
     * Desenha os previews da fila next.
     */
    private void drawNextQueuePreviews(GameLayout layout, GameSession session) {
        Array<Tetromino> nextQueue = session.getNextQueue();

        if (nextQueue.size > 0) drawPreviewPieceInBox(nextQueue.get(0), layout.nextMainBox, 18f);
        if (nextQueue.size > 1) drawPreviewPieceInBox(nextQueue.get(1), layout.nextQueueBox1, 15f);
        if (nextQueue.size > 2) drawPreviewPieceInBox(nextQueue.get(2), layout.nextQueueBox2, 15f);
        if (nextQueue.size > 3) drawPreviewPieceInBox(nextQueue.get(3), layout.nextQueueBox3, 15f);
        if (nextQueue.size > 4) drawPreviewPieceInBox(nextQueue.get(4), layout.nextQueueBox4, 15f);
    }

    /**
     * Draws a preview piece centered in one box.
     * Desenha uma peça de preview centralizada em uma caixa.
     */
    private void drawPreviewPieceInBox(Tetromino tetromino, Rectangle box, float previewCell) {
        int[][] shape = tetromino.getCells();
        Color color = getColorForCell(tetromino.getColorId());

        float pieceWidth = shape[0].length * previewCell;
        float pieceHeight = shape.length * previewCell;

        float startX = box.x + (box.width - pieceWidth) / 2f;
        float startY = box.y + (box.height - pieceHeight) / 2f;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;

                drawNeonBlockInternal(
                    startX + c * previewCell + 1f,
                    startY + r * previewCell + 1f,
                    previewCell - 2f,
                    color,
                    1f
                );
            }
        }
    }

    // =========================================================
    // HUD text / Texto do HUD
    // =========================================================

    /**
     * Draws all HUD text layers.
     * Desenha todas as camadas de texto do HUD.
     */
    private void drawHudText(GameLayout layout, GameSession session) {
        batch.begin();

        drawBoardBrand(layout);
        drawScoreText(layout, session);
        drawSideLabels(layout, session);
        drawControlsText(layout);

        batch.end();
    }

    /**
     * Draws the large brand text inside the board.
     * Desenha o texto grande da marca dentro do tabuleiro.
     */
    private void drawBoardBrand(GameLayout layout) {
        brandFont.setColor(0.20f, 0.95f, 1.00f, 0.07f);
        brandFont.draw(
            batch,
            "TETRIS",
            layout.boardX + 4f,
            layout.boardY + GameLayout.BOARD_HEIGHT * 0.56f + 2f,
            GameLayout.BOARD_WIDTH,
            Align.center,
            false
        );

        brandFont.setColor(0.95f, 0.98f, 1f, 0.90f);
        brandFont.draw(
            batch,
            "TETRIS",
            layout.boardX,
            layout.boardY + GameLayout.BOARD_HEIGHT * 0.56f,
            GameLayout.BOARD_WIDTH,
            Align.center,
            false
        );
    }

    /**
     * Draws the score above the board.
     * Desenha o score acima do tabuleiro.
     */
    private void drawScoreText(GameLayout layout, GameSession session) {
        labelFont.setColor(TEXT_SOFT);
        labelFont.draw(
            batch,
            "SCORE",
            layout.boardX,
            layout.boardY + GameLayout.BOARD_HEIGHT + 54f,
            GameLayout.BOARD_WIDTH,
            Align.center,
            false
        );

        valueFont.setColor(TEXT_GLOW);
        valueFont.draw(
            batch,
            String.valueOf(session.getScore()),
            layout.boardX + 2f,
            layout.boardY + GameLayout.BOARD_HEIGHT + 24f,
            GameLayout.BOARD_WIDTH,
            Align.center,
            false
        );

        valueFont.setColor(TEXT_MAIN);
        valueFont.draw(
            batch,
            String.valueOf(session.getScore()),
            layout.boardX,
            layout.boardY + GameLayout.BOARD_HEIGHT + 26f,
            GameLayout.BOARD_WIDTH,
            Align.center,
            false
        );
    }

    /**
     * Draws the side labels and values.
     * Desenha os rótulos e valores laterais.
     */
    private void drawSideLabels(GameLayout layout, GameSession session) {
        drawShadowedCenteredText(
            "HOLD",
            titleFont,
            TEXT_MAIN,
            layout.holdBox.x,
            layout.holdBox.y + layout.holdBox.height + 26f,
            layout.holdBox.width
        );

        drawShadowedCenteredText(
            "NEXT",
            titleFont,
            TEXT_MAIN,
            layout.nextMainBox.x,
            layout.nextMainBox.y + layout.nextMainBox.height + 26f,
            layout.nextMainBox.width
        );

        drawShadowedCenteredText(
            "LEVEL",
            labelFont,
            TEXT_SOFT,
            layout.levelBox.x,
            layout.levelBox.y + layout.levelBox.height + 18f,
            layout.levelBox.width
        );

        drawShadowedCenteredText(
            String.valueOf(session.getLevel()),
            valueFont,
            new Color(0.70f, 0.92f, 1f, 1f),
            layout.levelBox.x,
            layout.levelBox.y + 56f,
            layout.levelBox.width
        );

        drawShadowedCenteredText(
            "GOAL",
            labelFont,
            TEXT_SOFT,
            layout.goalBox.x,
            layout.goalBox.y + layout.goalBox.height + 18f,
            layout.goalBox.width
        );

        drawShadowedCenteredText(
            String.valueOf(session.getGoalLines()),
            valueFont,
            new Color(0.70f, 0.92f, 1f, 1f),
            layout.goalBox.x,
            layout.goalBox.y + 56f,
            layout.goalBox.width
        );
    }

    /**
     * Draws control hints under the board.
     * Desenha as dicas de controle abaixo do tabuleiro.
     */
    private void drawControlsText(GameLayout layout) {
        hintFont.setColor(new Color(0.76f, 0.90f, 1f, 0.52f));
        hintFont.draw(
            batch,
            "Tap: rotate   Swipe: move   Fling: drop   Long press / C: hold",
            layout.boardX - 70f,
            layout.boardY - 24f,
            GameLayout.BOARD_WIDTH + 140f,
            Align.center,
            true
        );
    }

    /**
     * Draws centered text with a soft glow shadow.
     * Desenha texto centralizado com sombra suave de brilho.
     */
    private void drawShadowedCenteredText(String text, BitmapFont font, Color color, float x, float y, float width) {
        font.setColor(0.18f, 0.95f, 1f, 0.16f);
        font.draw(batch, text, x + 1.5f, y - 1.5f, width, Align.center, false);

        font.setColor(color);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

    // =========================================================
    // Helpers / Auxiliares
    // =========================================================

    /**
     * Calculates the landing row for the ghost piece.
     * Calcula a linha final da ghost piece.
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
     * Detects how many right rotations were applied.
     * Detecta quantas rotações para a direita foram aplicadas.
     */
    private int detectRotationSteps(int[][] original, int[][] rotated) {
        int[][] test = copyMatrix(original);

        for (int i = 0; i < 4; i++) {
            if (sameMatrix(test, rotated)) return i;
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
     * Compares two matrices.
     * Compara duas matrizes.
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
     * Draws one glass-neon block.
     * Desenha um bloco glass-neon.
     */
    private void drawNeonBlockInternal(float x, float y, float size, Color baseColor, float alpha) {
        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.10f * alpha);
        shapeRenderer.rect(x - 6f, y - 6f, size + 12f, size + 12f);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.18f * alpha);
        shapeRenderer.rect(x - 3f, y - 3f, size + 6f, size + 6f);

        shapeRenderer.setColor(baseColor.r * 0.45f, baseColor.g * 0.45f, baseColor.b * 0.45f, 0.92f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(baseColor.r * 0.75f, baseColor.g * 0.75f, baseColor.b * 0.75f, 0.96f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 1f * alpha);
        shapeRenderer.rect(x + 5f, y + 5f, size - 10f, size - 10f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.18f * alpha);
        shapeRenderer.rect(x + 4f, y + size - 8f, size * 0.45f, 2f);
    }

    /**
     * Draws one ghost block.
     * Desenha um bloco da ghost piece.
     */
    private void drawGhostBlock(float x, float y, float size, Color baseColor, float alpha) {
        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.08f * alpha);
        shapeRenderer.rect(x - 3f, y - 3f, size + 6f, size + 6f);

        shapeRenderer.setColor(baseColor.r * 0.28f, baseColor.g * 0.28f, baseColor.b * 0.28f, 0.20f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.10f * alpha);
        shapeRenderer.rect(x + 3f, y + 3f, size - 6f, size - 6f);
    }

    /**
     * Returns the color for a board cell id.
     * Retorna a cor para o id da célula.
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

    /**
     * Releases all rendering resources.
     * Libera todos os recursos de renderização.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();

        titleFont.dispose();
        labelFont.dispose();
        valueFont.dispose();
        hintFont.dispose();
        brandFont.dispose();
    }
}
