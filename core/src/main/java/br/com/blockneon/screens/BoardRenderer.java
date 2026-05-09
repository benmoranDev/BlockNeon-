package br.com.blockneon.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import br.com.blockneon.model.ActivePiece;
import br.com.blockneon.model.Board;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;


public class BoardRenderer {

    // =========================================================
    // Palette / Paleta
    // =========================================================

    private static final Color BLOCK_I     = new Color(0.20f, 0.88f, 1.00f, 1f);
    private static final Color BLOCK_J     = new Color(0.28f, 0.46f, 1.00f, 1f);
    private static final Color BLOCK_L     = new Color(1.00f, 0.58f, 0.12f, 1f);
    private static final Color BLOCK_O     = new Color(1.00f, 0.83f, 0.18f, 1f);
    private static final Color BLOCK_S     = new Color(0.34f, 0.94f, 0.26f, 1f);
    private static final Color BLOCK_T     = new Color(0.88f, 0.28f, 1.00f, 1f);
    private static final Color BLOCK_Z     = new Color(1.00f, 0.22f, 0.20f, 1f);
    private static final Color GHOST_COLOR = new Color(0.82f, 0.82f, 0.92f, 1f);

    // =========================================================
    // 2.5D depth constants / Constantes de profundidade 2.5D
    // =========================================================

    // Horizontal extrusion offset | Offset de extrusão horizontal
    private static final float DEPTH_X     = 7f;
    // Vertical extrusion offset | Offset de extrusão vertical
    private static final float DEPTH_Y     = 7f;

    // Side face brightness (lower = darker) | Brilho da face lateral (menor = mais escura)
    private static final float SHADE_RIGHT = 0.32f;

    // Top face brightness (higher = brighter) | Brilho da face superior (maior = mais clara)
    private static final float SHADE_TOP   = 1.22f;

    // =========================================================
    // Renderer
    // =========================================================

    private final ShapeRenderer shapeRenderer;

    public BoardRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    // =========================================================
    // Public render entry point
    // =========================================================

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

    // =========================================================
    // Background / Fundo animado
    // =========================================================

    private void drawBackgroundDecor(float time) {
        float pulseA = (MathUtils.sin(time * 1.60f) + 1f) * 0.5f;
        float pulseB = (MathUtils.sin(time * 2.40f) + 1f) * 0.5f;
        float pulseC = (MathUtils.sin(time * 0.85f) + 1f) * 0.5f;
        float pulseD = (MathUtils.sin(time * 0.35f) + 1f) * 0.5f;
        float pulseE = (MathUtils.sin(time * 0.18f) + 1f) * 0.5f;

        float vw = GameLayout.WORLD_WIDTH;
        float vh = GameLayout.WORLD_HEIGHT;

        // 1. Base — preto-azul
        shapeRenderer.setColor(0.00f, 0.00f, 0.05f, 1f);
        shapeRenderer.rect(0, 0, vw, vh);

        // 2. Gradiente principal vertical — 24 faixas
        int bands = 24;
        for (int i = 0; i < bands; i++) {
            float t     = (float) i / (bands - 1);
            float bandH = vh / bands + 1.5f;
            float bandY = i * (vh / bands);
            float curve = (float) Math.pow(1f - t, 1.8f);
            shapeRenderer.setColor(
                0.01f + curve * 0.04f,
                0.02f + curve * 0.12f,
                0.06f + curve * 0.30f,
                curve * (0.70f + pulseD * 0.08f));
            shapeRenderer.rect(0, bandY, vw, bandH);
        }

        // 3. Gradiente lateral esquerdo — ciano frio
        int sideBands = 12;
        for (int i = 0; i < sideBands; i++) {
            float t     = (float) i / sideBands;
            float bandW = vw * 0.32f / sideBands;
            float curve = (float) Math.pow(1f - t, 2.2f);
            shapeRenderer.setColor(0.10f, 0.55f, 1.00f, curve * (0.06f + pulseC * 0.03f));
            shapeRenderer.rect(i * bandW, 0, bandW + 1f, vh);
        }

        // 4. Gradiente lateral direito — teal
        for (int i = 0; i < sideBands; i++) {
            float t     = (float) i / sideBands;
            float bandW = vw * 0.28f / sideBands;
            float curve = (float) Math.pow(1f - t, 2.5f);
            shapeRenderer.setColor(0.05f, 0.70f, 0.90f, curve * (0.05f + pulseA * 0.025f));
            shapeRenderer.rect(vw - (i + 1) * bandW, 0, bandW + 1f, vh);
        }

        // 5. Glow inferior — três camadas
        float glowBase = 0.16f + pulseA * 0.09f;
        shapeRenderer.setColor(0.04f, 0.18f, 0.52f, glowBase);
        shapeRenderer.rect(0, 0, vw, vh * 0.30f);
        shapeRenderer.setColor(0.02f, 0.28f, 0.55f, glowBase * 0.75f);
        shapeRenderer.rect(0, 0, vw, vh * 0.18f);
        shapeRenderer.setColor(0.05f, 0.55f, 0.90f, 0.10f + pulseB * 0.06f);
        shapeRenderer.rect(0, 0, vw, vh * 0.08f);

        // 6. Véus superiores
        float veilA = 0.28f + pulseE * 0.08f;
        shapeRenderer.setColor(0.00f, 0.00f, 0.03f, veilA);
        shapeRenderer.rect(0, vh * 0.72f, vw, vh * 0.28f);
        shapeRenderer.setColor(0.00f, 0.00f, 0.02f, veilA * 0.55f);
        shapeRenderer.rect(0, vh * 0.58f, vw, vh * 0.18f);

        // 7. Halos difusos
        float h1R = vw * 0.75f;
        float h1X = vw * 0.18f - h1R * 0.5f;
        float h1Y = vh * 0.25f - h1R * 0.5f + MathUtils.sin(time * 0.3f) * 20f;
        shapeRenderer.setColor(0.06f, 0.22f, 0.72f, 0.05f + pulseD * 0.025f);
        shapeRenderer.ellipse(h1X, h1Y, h1R, h1R);
        shapeRenderer.setColor(0.04f, 0.15f, 0.55f, 0.035f + pulseD * 0.015f);
        shapeRenderer.ellipse(h1X - h1R * 0.1f, h1Y - h1R * 0.1f, h1R * 1.25f, h1R * 1.25f);

        float h2R = vw * 0.55f;
        float h2X = vw * 0.75f - h2R * 0.5f;
        float h2Y = vh * 0.45f - h2R * 0.5f + MathUtils.sin(time * 0.22f + 1.8f) * 28f;
        shapeRenderer.setColor(0.04f, 0.45f, 0.80f, 0.04f + pulseC * 0.02f);
        shapeRenderer.ellipse(h2X, h2Y, h2R, h2R);
        shapeRenderer.setColor(0.02f, 0.30f, 0.60f, 0.025f + pulseC * 0.012f);
        shapeRenderer.ellipse(h2X - h2R * 0.12f, h2Y - h2R * 0.12f, h2R * 1.20f, h2R * 1.20f);

        float h3R = vw * 0.35f;
        float h3X = vw * 0.42f - h3R * 0.5f;
        float h3Y = vh * 0.72f - h3R * 0.5f + MathUtils.sin(time * 0.40f + 3.5f) * 15f;
        shapeRenderer.setColor(0.08f, 0.30f, 0.65f, 0.03f + pulseE * 0.015f);
        shapeRenderer.ellipse(h3X, h3Y, h3R, h3R);

        // 8. Auroras
        float a1Y = vh * 0.18f + MathUtils.sin(time * 0.55f) * 22f;
        shapeRenderer.setColor(0.20f, 0.75f, 1.00f, 0.048f + pulseA * 0.022f);
        shapeRenderer.rect(0, a1Y, vw, 32f);
        shapeRenderer.setColor(0.15f, 0.60f, 0.90f, 0.025f + pulseA * 0.012f);
        shapeRenderer.rect(0, a1Y - 12f, vw, 12f);
        shapeRenderer.rect(0, a1Y + 32f, vw, 10f);

        float a2Y = vh * 0.34f + MathUtils.sin(time * 0.40f + 1.4f) * 18f;
        shapeRenderer.setColor(0.12f, 0.50f, 0.88f, 0.032f + pulseB * 0.016f);
        shapeRenderer.rect(0, a2Y, vw, 22f);
        shapeRenderer.setColor(0.08f, 0.35f, 0.72f, 0.018f + pulseB * 0.010f);
        shapeRenderer.rect(0, a2Y - 8f, vw, 8f);
        shapeRenderer.rect(0, a2Y + 22f, vw, 7f);

        float a3Y = vh * 0.58f + MathUtils.sin(time * 0.28f + 2.8f) * 14f;
        shapeRenderer.setColor(0.10f, 0.40f, 0.78f, 0.018f + pulseC * 0.009f);
        shapeRenderer.rect(0, a3Y, vw, 14f);

        // 9. Scanlines
        float scanA = 0.022f + pulseD * 0.006f;
        for (float y = 0; y < vh; y += 4f) {
            shapeRenderer.setColor(0.00f, 0.05f, 0.15f, scanA);
            shapeRenderer.rect(0, y, vw, 1.5f);
        }

        // 10. Vinheta lateral
        int vigBands = 10;
        float vigW   = vw * 0.12f;
        for (int i = 0; i < vigBands; i++) {
            float t  = (float) i / vigBands;
            float bw = vigW / vigBands;
            shapeRenderer.setColor(0f, 0f, 0.02f, (1f - t) * (0.18f + pulseE * 0.04f));
            shapeRenderer.rect(i * bw, 0, bw + 1f, vh);
        }
        for (int i = 0; i < vigBands; i++) {
            float t  = (float) i / vigBands;
            float bw = vigW / vigBands;
            shapeRenderer.setColor(0f, 0f, 0.02f, (1f - t) * (0.16f + pulseE * 0.04f));
            shapeRenderer.rect(vw - (i + 1) * bw, 0, bw + 1f, vh);
        }
    }

    // =========================================================
    // Board panel
    // =========================================================

    private void drawBoardPanel(GameLayout layout) {
        shapeRenderer.setColor(0.12f, 0.20f, 0.32f, 1f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING,
            GameLayout.BOARD_WIDTH  + GameLayout.BOARD_FRAME_PADDING * 2f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f);

        shapeRenderer.setColor(0.05f, 0.08f, 0.14f, 1f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING + 4f,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING + 4f,
            GameLayout.BOARD_WIDTH  + GameLayout.BOARD_FRAME_PADDING * 2f - 8f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f - 8f);

        shapeRenderer.setColor(0.05f, 0.06f, 0.08f, 1f);
        shapeRenderer.rect(layout.boardX, layout.boardY,
            GameLayout.BOARD_WIDTH, GameLayout.BOARD_HEIGHT);

        shapeRenderer.setColor(0.10f, 0.11f, 0.13f, 0.30f);
        shapeRenderer.rect(
            layout.boardX + GameLayout.BOARD_INNER_PADDING,
            layout.boardY + GameLayout.BOARD_INNER_PADDING,
            GameLayout.BOARD_WIDTH  - GameLayout.BOARD_INNER_PADDING * 2f,
            GameLayout.BOARD_HEIGHT - GameLayout.BOARD_INNER_PADDING * 2f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.035f);
        shapeRenderer.rect(
            layout.boardX + 6f,
            layout.boardY + GameLayout.BOARD_HEIGHT - 14f,
            GameLayout.BOARD_WIDTH - 12f, 7f);
    }

    private void drawBoardFrameHighlights(GameLayout layout) {
        shapeRenderer.setColor(0.25f, 0.75f, 1.00f, 0.90f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING,
            GameLayout.BOARD_WIDTH  + GameLayout.BOARD_FRAME_PADDING * 2f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f);

        shapeRenderer.setColor(0.04f, 0.08f, 0.16f, 0.70f);
        shapeRenderer.rect(
            layout.boardX - GameLayout.BOARD_FRAME_PADDING + 4f,
            layout.boardY - GameLayout.BOARD_FRAME_PADDING + 4f,
            GameLayout.BOARD_WIDTH  + GameLayout.BOARD_FRAME_PADDING * 2f - 8f,
            GameLayout.BOARD_HEIGHT + GameLayout.BOARD_FRAME_PADDING * 2f - 8f);

        shapeRenderer.setColor(0.22f, 0.28f, 0.36f, 0.90f);
        shapeRenderer.rect(layout.boardX, layout.boardY,
            GameLayout.BOARD_WIDTH, GameLayout.BOARD_HEIGHT);
    }

    // =========================================================
    // Empty cells — 2.5D recessed back-wall / Poço recuado
    // =========================================================

    private void drawEmptyCells(GameLayout layout) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                float x = layout.boardX + col * GameLayout.CELL_SIZE;
                float y = layout.boardY + row * GameLayout.CELL_SIZE;

                // Parede de fundo recuada (ilusão de poço)
                shapeRenderer.setColor(0.03f, 0.04f, 0.06f, 0.80f);
                shapeRenderer.rect(
                    x + DEPTH_X + 1f,
                    y - DEPTH_Y + 1f,
                    GameLayout.CELL_SIZE - 2f,
                    GameLayout.CELL_SIZE - 2f);

                // Face frontal
                shapeRenderer.setColor(0.08f, 0.08f, 0.09f, 0.78f);
                shapeRenderer.rect(x + 1f, y + 1f,
                    GameLayout.CELL_SIZE - 2f, GameLayout.CELL_SIZE - 2f);

                // Glint especular
                shapeRenderer.setColor(1f, 1f, 1f, 0.015f);
                shapeRenderer.rect(x + 3f, y + GameLayout.CELL_SIZE - 8f,
                    GameLayout.CELL_SIZE - 6f, 2f);
            }
        }
    }

    // =========================================================
    // Board grid | Grade do tabuleiro
    // =========================================================

    private void drawBoardGrid(GameLayout layout) {
        for (int col = 0; col <= Board.COLS; col++) {
            float x = layout.boardX + col * GameLayout.CELL_SIZE;
            shapeRenderer.setColor(0.18f, 0.18f, 0.20f,
                col == 0 || col == Board.COLS ? 0.32f : 0.14f);
            shapeRenderer.line(x, layout.boardY,
                x, layout.boardY + GameLayout.BOARD_HEIGHT);
        }
        for (int row = 0; row <= Board.ROWS; row++) {
            float y = layout.boardY + row * GameLayout.CELL_SIZE;
            shapeRenderer.setColor(0.18f, 0.18f, 0.20f,
                row == 0 || row == Board.ROWS ? 0.32f : 0.14f);
            shapeRenderer.line(layout.boardX, y,
                layout.boardX + GameLayout.BOARD_WIDTH, y);
        }
    }

    // =========================================================
    // Locked cells | Celulas bloqueadas
    // =========================================================

    private void drawLockedCells(GameLayout layout, GameSession session) {
        Board board = session.getBoard();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                int value = board.getCell(row, col);
                if (value == 0) continue;
                drawNeonBlock2D5(
                    layout.boardX + col * GameLayout.CELL_SIZE + 2f,
                    layout.boardY + row * GameLayout.CELL_SIZE + 2f,
                    GameLayout.CELL_SIZE - 4f,
                    getColorForCell(value), 1f);
            }
        }
    }

    // =========================================================
    // Active piece | Peça ativa
    // =========================================================

    private void drawActivePiece(GameLayout layout, GameSession session, float time) {
        ActivePiece activePiece = session.getActivePiece();
        if (activePiece == null) return;

        int[][] shape = activePiece.getRotatedCells();
        Color   color = getColorForCell(activePiece.getTetromino().getColorId());
        float   pulse = (MathUtils.sin(time * 6f) + 1f) * 0.5f;
        float   alpha = 0.92f + pulse * 0.10f;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;
                int boardRow = activePiece.getRow() + r;
                int boardCol = activePiece.getCol() + c;
                if (boardRow < 0 || boardRow >= Board.ROWS) continue;
                if (boardCol < 0 || boardCol >= Board.COLS) continue;
                drawNeonBlock2D5(
                    layout.boardX + boardCol * GameLayout.CELL_SIZE + 2f,
                    layout.boardY + boardRow * GameLayout.CELL_SIZE + 2f,
                    GameLayout.CELL_SIZE - 4f,
                    color, alpha);
            }
        }
    }

    // =========================================================
    // Ghost piece | Peça fantasma
    // =========================================================

    private void drawGhostPiece(GameLayout layout, GameSession session) {
        ActivePiece activePiece = session.getActivePiece();
        if (activePiece == null) return;

        int     ghostRow = computeGhostRow(session.getBoard(), activePiece);
        int[][] shape    = activePiece.getRotatedCells();

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
                    GHOST_COLOR, 0.28f);
            }
        }
    }

    // =========================================================
    // Line clear flash | Flash de linhas limpas
    // =========================================================

    private void drawLineClearFlash(GameLayout layout, GameSession session) {
        if (session.getLineFlashTimer() <= 0f || session.getFlashRows().size == 0) return;

        float progress = session.getLineFlashTimer() / GameSession.LINE_FLASH_DURATION;
        // Fade quadrático: explosão forte no início, some suavemente no fim
        float alpha = progress * progress;

        for (Integer row : session.getFlashRows()) {
            float y      = layout.boardY + row * GameLayout.CELL_SIZE;
            float cx     = layout.boardX + GameLayout.BOARD_WIDTH * 0.5f;
            float cy     = y + GameLayout.CELL_SIZE * 0.5f;
            float halfW  = GameLayout.BOARD_WIDTH * 0.5f;

            // --- 1. Flash branco de fundo (brilho base) ---
            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.55f);
            shapeRenderer.rect(layout.boardX, y,
                GameLayout.BOARD_WIDTH, GameLayout.CELL_SIZE);

            // --- 2. Camada dourada central ---
            shapeRenderer.setColor(1f, 0.88f, 0.20f, alpha * 0.35f);
            shapeRenderer.rect(layout.boardX + 6f, y + 4f,
                GameLayout.BOARD_WIDTH - 12f, GameLayout.CELL_SIZE - 8f);

            // --- 3. Scanline horizontal brilhante (linha de luz no centro) ---
            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.90f);
            shapeRenderer.rect(layout.boardX, cy - 1.5f,
                GameLayout.BOARD_WIDTH, 3f);

            // --- 4. Partículas simuladas: pequenos quadrados espalhados pela linha ---
            // Usamos uma seed baseada no row para posições determinísticas mas variadas
            int particleCount = 18;
            for (int i = 0; i < particleCount; i++) {
                // Pseudo-random baseado em índice + row (sem objeto Random)
                float t       = (float) i / particleCount;
                float seed    = (float) Math.sin((i + row * 7.3f) * 137.508f);
                float offsetX = t * GameLayout.BOARD_WIDTH;
                float offsetY = seed * GameLayout.CELL_SIZE * 0.5f;
                // Partículas viajam para fora do centro ao longo do tempo
                float spread  = (1f - progress) * halfW * (Math.abs(seed) + 0.3f);
                float px      = cx + (offsetX - halfW) + (seed > 0 ? spread : -spread) * 0.4f;
                float py      = cy + offsetY;
                float pSize   = (2f + Math.abs(seed) * 5f) * progress;

                shapeRenderer.setColor(1f, 0.75f + Math.abs(seed) * 0.25f, 0.1f, alpha * 0.80f);
                shapeRenderer.rect(px - pSize * 0.5f, py - pSize * 0.5f, pSize, pSize);
            }

            // --- 5. Bordas laterais: raios de luz saindo para esquerda e direita ---
            int rayCount = 5;
            for (int i = 0; i < rayCount; i++) {
                float seed   = (float) Math.sin(i * 43.7f + row * 11.1f);
                float rayY   = cy + seed * GameLayout.CELL_SIZE * 0.35f;
                float rayLen = (20f + Math.abs(seed) * 30f) * (1f - progress); // crescem ao longo do tempo
                float rayH   = 1.5f + Math.abs(seed) * 1.5f;

                // Raio para a esquerda
                shapeRenderer.setColor(1f, 0.95f, 0.5f, alpha * 0.60f);
                shapeRenderer.rect(layout.boardX - rayLen, rayY - rayH * 0.5f, rayLen, rayH);
                // Raio para a direita
                shapeRenderer.rect(layout.boardX + GameLayout.BOARD_WIDTH, rayY - rayH * 0.5f, rayLen, rayH);
            }
        }
    }

    // =========================================================
    // 2.5D NEON BLOCK  ─ drawNeonBlock2D5
    // =========================================================
    //
    // Ordem de renderização (trás → frente):
    //   1. Outer glow halo         (2 rects expandidos, alpha baixo)
    //   2. Drop shadow             (rect preto deslocado)
    //   3. Back face               (22% brilho, no offset de profundidade)
    //   4. Face lateral direita    (SHADE_RIGHT — trapézio 2 triângulos)
    //   5. Face superior           (SHADE_TOP   — trapézio 2 triângulos)
    //   6. Front face              (4 rects aninhados: shell→mid→light→neon)
    //   7. Primary specular glint  (branco, topo-esq)
    //   8. Secondary glint         (branco suave, abaixo)
    //
    // @param x     X inferior-esquerdo da face frontal
    // @param y     Y inferior-esquerdo da face frontal
    // @param size  lado do bloco (quadrado)
    // @param c     cor neon base
    // @param alpha multiplicador global de alpha
    // =========================================================

    private void drawNeonBlock2D5(float x, float y, float size, Color c, float alpha) {

        // 1. Outer glow halo
        shapeRenderer.setColor(c.r, c.g, c.b, 0.07f * alpha);
        shapeRenderer.rect(x - 5f, y - 5f, size + 10f, size + 10f);
        shapeRenderer.setColor(c.r, c.g, c.b, 0.13f * alpha);
        shapeRenderer.rect(x - 2f, y - 2f, size + 4f,  size + 4f);

        // 2. Drop shadow
        shapeRenderer.setColor(0f, 0f, 0f, 0.25f * alpha);
        shapeRenderer.rect(x + DEPTH_X + 1f, y - DEPTH_Y - 1f, size, size);

        // 3. Back face
        shapeRenderer.setColor(c.r * 0.22f, c.g * 0.22f, c.b * 0.22f, 0.90f * alpha);
        shapeRenderer.rect(x + DEPTH_X, y - DEPTH_Y, size, size);

        // 4. Right lateral face — trapézio (2 triângulos)
        float sr = c.r * SHADE_RIGHT;
        float sg = c.g * SHADE_RIGHT;
        float sb = c.b * SHADE_RIGHT;
        shapeRenderer.setColor(sr, sg, sb, 0.95f * alpha);
        // Triângulo A: frente-baixo-dir → trás-baixo-dir → trás-cima-dir
        shapeRenderer.triangle(
            x + size,           y,
            x + size + DEPTH_X, y - DEPTH_Y,
            x + size + DEPTH_X, y - DEPTH_Y + size);
        // Triângulo B: frente-baixo-dir → frente-cima-dir → trás-cima-dir
        shapeRenderer.triangle(
            x + size,           y,
            x + size,           y + size,
            x + size + DEPTH_X, y - DEPTH_Y + size);

        // 5. Top face — trapézio (2 triângulos)
        float tr = Math.min(c.r * SHADE_TOP, 1f);
        float tg = Math.min(c.g * SHADE_TOP, 1f);
        float tb = Math.min(c.b * SHADE_TOP, 1f);
        shapeRenderer.setColor(tr, tg, tb, 0.95f * alpha);
        // Triângulo A: frente-cima-esq → trás-cima-esq → trás-cima-dir
        shapeRenderer.triangle(
            x,                  y + size,
            x + DEPTH_X,        y + size - DEPTH_Y,
            x + DEPTH_X + size, y + size - DEPTH_Y);
        // Triângulo B: frente-cima-esq → frente-cima-dir → trás-cima-dir
        shapeRenderer.triangle(
            x,                  y + size,
            x + size,           y + size,
            x + DEPTH_X + size, y + size - DEPTH_Y);

        // 6. Front face — 4 rects aninhados
        shapeRenderer.setColor(c.r * 0.32f, c.g * 0.32f, c.b * 0.32f, 0.95f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(c.r * 0.55f, c.g * 0.55f, c.b * 0.55f, 0.95f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(c.r * 0.78f, c.g * 0.78f, c.b * 0.78f, 0.98f * alpha);
        shapeRenderer.rect(x + 4f, y + 4f, size - 8f, size - 8f);

        shapeRenderer.setColor(c.r, c.g, c.b, 1f * alpha);
        shapeRenderer.rect(x + 6f, y + 6f, size - 12f, size - 12f);

        // 7. Primary specular glint
        shapeRenderer.setColor(1f, 1f, 1f, 0.22f * alpha);
        shapeRenderer.rect(x + 4f, y + size - 9f, size * 0.40f, 3f);

        // 8. Secondary glint
        shapeRenderer.setColor(1f, 1f, 1f, 0.08f * alpha);
        shapeRenderer.rect(x + 6f, y + size - 13f, size - 14f, 2f);
    }

    // =========================================================
    // Ghost block — 2.5D translúcido
    // =========================================================
    //
    // Layers:
    //   - Drop shadow traseira (sutil)
    //   - Face lateral direita (muito tênue)
    //   - Front face translúcida (shell + mid + core)
    // =========================================================

    private void drawGhostBlock(float x, float y, float size, Color baseColor, float alpha) {

        // Shadow traseira
        shapeRenderer.setColor(0f, 0f, 0f, 0.10f * alpha);
        shapeRenderer.rect(x + DEPTH_X, y - DEPTH_Y, size, size);

        // Face lateral direita — muito sutil
        shapeRenderer.setColor(
            baseColor.r * SHADE_RIGHT,
            baseColor.g * SHADE_RIGHT,
            baseColor.b * SHADE_RIGHT,
            0.15f * alpha);
        shapeRenderer.triangle(
            x + size,           y,
            x + size + DEPTH_X, y - DEPTH_Y,
            x + size + DEPTH_X, y - DEPTH_Y + size);
        shapeRenderer.triangle(
            x + size,           y,
            x + size,           y + size,
            x + size + DEPTH_X, y - DEPTH_Y + size);

        // Front face translúcida
        shapeRenderer.setColor(
            baseColor.r * 0.30f, baseColor.g * 0.30f, baseColor.b * 0.30f,
            0.25f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.12f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.06f * alpha);
        shapeRenderer.rect(x + 5f, y + 5f, size - 10f, size - 10f);
    }

    // =========================================================
    // Ghost row computation
    // =========================================================

    private int computeGhostRow(Board board, ActivePiece piece) {
        ActivePiece testPiece = new ActivePiece(
            piece.getTetromino(), piece.getRow(), piece.getCol());
        int[][] currentShape = piece.getRotatedCells();
        int rotations = detectRotationSteps(piece.getTetromino().getCells(), currentShape);
        for (int i = 0; i < rotations; i++) testPiece.rotateRight();
        while (board.canPlace(testPiece)) testPiece.moveDown();
        testPiece.moveUp();
        return testPiece.getRow();
    }

    private int detectRotationSteps(int[][] original, int[][] rotated) {
        int[][] test = copyMatrix(original);
        for (int i = 0; i < 4; i++) {
            if (sameMatrix(test, rotated)) return i;
            test = rotateClockwise(test);
        }
        return 0;
    }

    private int[][] copyMatrix(int[][] src) {
        int[][] copy = new int[src.length][src[0].length];
        for (int r = 0; r < src.length; r++)
            System.arraycopy(src[r], 0, copy[r], 0, src[r].length);
        return copy;
    }

    private boolean sameMatrix(int[][] a, int[][] b) {
        if (a.length != b.length) return false;
        for (int r = 0; r < a.length; r++) {
            if (a[r].length != b[r].length) return false;
            for (int c = 0; c < a[r].length; c++)
                if (a[r][c] != b[r][c]) return false;
        }
        return true;
    }

    private int[][] rotateClockwise(int[][] matrix) {
        int     size    = matrix.length;
        int[][] rotated = new int[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                rotated[c][size - 1 - r] = matrix[r][c];
        return rotated;
    }

    // =========================================================
    // Color mapping
    // =========================================================

    private Color getColorForCell(int value) {
        switch (value) {
            case 1:  return BLOCK_I;
            case 2:  return BLOCK_J;
            case 3:  return BLOCK_L;
            case 4:  return BLOCK_O;
            case 5:  return BLOCK_S;
            case 6:  return BLOCK_T;
            case 7:  return BLOCK_Z;
            default: return Color.WHITE;
        }
    }

    // =========================================================
    // Lifecycle
    // =========================================================

    public void dispose() {
        shapeRenderer.dispose();
    }
}
