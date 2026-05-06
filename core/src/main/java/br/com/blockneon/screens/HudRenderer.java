package br.com.blockneon.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import br.com.blockneon.model.Tetromino;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;


public class HudRenderer {

    // =========================================================
    // Panel palette — neon
    // =========================================================

    private static final Color PANEL_OUTER      = new Color(0.04f, 0.10f, 0.20f, 1f);
    private static final Color PANEL_INNER      = new Color(0.02f, 0.06f, 0.14f, 1f);
    private static final Color PANEL_HIGHLIGHT  = new Color(1f,    1f,    1f,    0.05f);
    private static final Color PANEL_OUTLINE    = new Color(0.20f, 0.80f, 1.00f, 0.55f);
    private static final Color BOX_OUTLINE      = new Color(0.18f, 0.70f, 0.90f, 0.70f);
    private static final Color BOX_BG           = new Color(0.03f, 0.08f, 0.16f, 1f);
    private static final Color BOX_BG_INNER     = new Color(0.02f, 0.05f, 0.10f, 1f);

    // =========================================================
    // Piece palette
    // =========================================================

    private static final Color BLOCK_I = new Color(0.20f, 0.88f, 1.00f, 1f);
    private static final Color BLOCK_J = new Color(0.28f, 0.46f, 1.00f, 1f);
    private static final Color BLOCK_L = new Color(1.00f, 0.58f, 0.12f, 1f);
    private static final Color BLOCK_O = new Color(1.00f, 0.83f, 0.18f, 1f);
    private static final Color BLOCK_S = new Color(0.34f, 0.94f, 0.26f, 1f);
    private static final Color BLOCK_T = new Color(0.88f, 0.28f, 1.00f, 1f);
    private static final Color BLOCK_Z = new Color(1.00f, 0.22f, 0.20f, 1f);

    // =========================================================
    // Text colors
    // =========================================================

    private static final Color TEXT_WHITE  = new Color(1f,    1f,    1f,    1f);
    private static final Color TEXT_HINT   = new Color(1f,    1f,    1f,    0.38f);
    private static final Color TEXT_SHADOW = new Color(0f,    0f,    0f,    0.50f);
    private static final Color TEXT_VALUE  = new Color(1f,    0.85f, 0.20f, 1f);

    // =========================================================
    // Rendering
    // =========================================================

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;

    // =========================================================
    // Fonts
    // =========================================================

    private final BitmapFont titleFont;
    private final BitmapFont labelFont;
    private final BitmapFont valueFont;
    private final BitmapFont hintFont;

    // =========================================================
    // State
    // =========================================================

    private float time = 0f;

    // =========================================================
    // Constructor
    // =========================================================

    public HudRenderer() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.05f);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(0.95f);

        valueFont = new BitmapFont();
        valueFont.getData().setScale(1.65f);

        hintFont = new BitmapFont();
        hintFont.getData().setScale(0.78f);
    }

    // =========================================================
    // Public render
    // =========================================================

    /**
     * Renders the whole HUD layer.
     * Renderiza toda a camada de HUD.
     */
    public void render(OrthographicCamera camera, GameLayout layout,
                       GameSession session, float delta) {
        time += delta;

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ── Passe 1: painéis + caixas (Filled) ───────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawTopBottomPanels(layout);
        drawTopAdBar(layout);
        drawTopBottomBoxes(layout);
        drawHoldPiecePreview(layout, session);
        drawNextQueuePreviews(layout, session);
        shapeRenderer.end();

        // ── Passe 2: contornos (Line) ─────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawTopBottomPanelOutlines(layout);
        drawTopBottomBoxOutlines(layout);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        drawHudText(layout, session);
    }

    // =========================================================
    // Shells
    // =========================================================

    private void drawTopBottomPanels(GameLayout layout) {
        drawArcadeShell(layout.topShellBounds);
        drawArcadeShell(layout.bottomShellBounds);
    }

    // Barra de anúncio superior
    private void drawTopAdBar(GameLayout layout) {
        Rectangle ad = layout.topAdBounds;

        // Fundo da barra
        shapeRenderer.setColor(0.03f, 0.08f, 0.18f, 1f);
        shapeRenderer.rect(ad.x, ad.y, ad.width, ad.height);

        // Linha de destaque inferior
        shapeRenderer.setColor(0.20f, 0.80f, 1f, 0.35f);
        shapeRenderer.rect(ad.x, ad.y, ad.width, 2f);

        // Glow levinho
        float pulse = (MathUtils.sin(time * 2.8f) + 1f) * 0.5f;
        shapeRenderer.setColor(0.20f, 0.80f, 1f, 0.05f + pulse * 0.04f);
        shapeRenderer.rect(ad.x - 2f, ad.y - 2f, ad.width + 4f, ad.height + 4f);
    }

    private void drawTopBottomPanelOutlines(GameLayout layout) {
        drawArcadeShellOutline(layout.topShellBounds);
        drawArcadeShellOutline(layout.bottomShellBounds);
    }

    private void drawTopBottomBoxes(GameLayout layout) {
        drawArcadeBox(layout.holdBox);
        drawArcadeBox(layout.levelBox);
        drawArcadeBox(layout.goalBox);
        drawArcadeBox(layout.nextMainBox);
        drawArcadeBox(layout.nextQueueBox1);
        drawArcadeBox(layout.nextQueueBox2);
        drawArcadeBox(layout.nextQueueBox3);
        drawArcadeBox(layout.nextQueueBox4);
    }

    private void drawTopBottomBoxOutlines(GameLayout layout) {
        drawArcadeBoxOutline(layout.holdBox);
        drawArcadeBoxOutline(layout.levelBox);
        drawArcadeBoxOutline(layout.goalBox);
        drawArcadeBoxOutline(layout.nextMainBox);
        drawArcadeBoxOutline(layout.nextQueueBox1);
        drawArcadeBoxOutline(layout.nextQueueBox2);
        drawArcadeBoxOutline(layout.nextQueueBox3);
        drawArcadeBoxOutline(layout.nextQueueBox4);
    }

    private void drawArcadeShell(Rectangle bounds) {
        // Fundo externo
        shapeRenderer.setColor(PANEL_OUTER);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Camada interna
        shapeRenderer.setColor(PANEL_INNER);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + 4f,
            bounds.width - 8f, bounds.height - 8f);

        // Reflexo de vidro
        shapeRenderer.setColor(PANEL_HIGHLIGHT);
        shapeRenderer.rect(bounds.x + 6f, bounds.y + bounds.height - 16f,
            bounds.width - 12f, 7f);

        // Glow pulsante na borda superior do painel
        float pulse = (MathUtils.sin(time * 2.2f) + 1f) * 0.5f;
        shapeRenderer.setColor(0.20f, 0.80f, 1f, 0.04f + pulse * 0.04f);
        shapeRenderer.rect(bounds.x - 2f, bounds.y - 2f,
            bounds.width + 4f, bounds.height + 4f);
    }

    private void drawArcadeShellOutline(Rectangle bounds) {
        float pulse = (MathUtils.sin(time * 2.2f) + 1f) * 0.5f;

        // Borda ciano pulsante
        shapeRenderer.setColor(0.20f, 0.80f, 1f, 0.35f + pulse * 0.20f);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Borda interna escura
        shapeRenderer.setColor(0.05f, 0.10f, 0.20f, 0.70f);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f,
            bounds.width - 6f, bounds.height - 6f);
    }

    private void drawArcadeBox(Rectangle bounds) {
        shapeRenderer.setColor(BOX_BG);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(BOX_BG_INNER);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f,
            bounds.width - 6f, bounds.height - 6f);

        // Reflexo de vidro no topo da caixa
        shapeRenderer.setColor(1f, 1f, 1f, 0.04f);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + bounds.height - 11f,
            bounds.width - 8f, 4f);
    }

    private void drawArcadeBoxOutline(Rectangle bounds) {
        float pulse = (MathUtils.sin(time * 2.2f) + 1f) * 0.5f;
        shapeRenderer.setColor(BOX_OUTLINE.r, BOX_OUTLINE.g, BOX_OUTLINE.b,
            0.50f + pulse * 0.20f);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // =========================================================
    // Previews
    // =========================================================

    private void drawHoldPiecePreview(GameLayout layout, GameSession session) {
        Tetromino held = session.getHeldTetromino();
        if (held == null) return;
        drawPreviewPieceInBox(held, layout.holdBox, 18f);
    }

    private void drawNextQueuePreviews(GameLayout layout, GameSession session) {
        Array<Tetromino> nextQueue = session.getNextQueue();
        if (nextQueue.size > 0) drawPreviewPieceInBox(nextQueue.get(0), layout.nextMainBox,  18f);
        if (nextQueue.size > 1) drawPreviewPieceInBox(nextQueue.get(1), layout.nextQueueBox1, 15f);
        if (nextQueue.size > 2) drawPreviewPieceInBox(nextQueue.get(2), layout.nextQueueBox2, 15f);
        if (nextQueue.size > 3) drawPreviewPieceInBox(nextQueue.get(3), layout.nextQueueBox3, 15f);
        if (nextQueue.size > 4) drawPreviewPieceInBox(nextQueue.get(4), layout.nextQueueBox4, 15f);
    }

    private void drawPreviewPieceInBox(Tetromino tetromino, Rectangle box, float previewCell) {
        int[][] shape  = tetromino.getCells();
        Color   color  = getColorForCell(tetromino.getColorId());

        float pieceWidth  = shape[0].length * previewCell;
        float pieceHeight = shape.length    * previewCell;
        float startX = box.x + (box.width  - pieceWidth)  / 2f;
        float startY = box.y + (box.height - pieceHeight) / 2f;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 0) continue;
                drawPreviewBlock(
                    startX + c * previewCell + 1f,
                    startY + r * previewCell + 1f,
                    previewCell - 2f,
                    color, 1f
                );
            }
        }
    }

    private void drawPreviewBlock(float x, float y, float size, Color c, float alpha) {
        // Camadas de profundidade + glow neon
        shapeRenderer.setColor(c.r * 0.20f, c.g * 0.20f, c.b * 0.20f, 0.95f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(c.r * 0.45f, c.g * 0.45f, c.b * 0.45f, 0.95f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(c.r * 0.72f, c.g * 0.72f, c.b * 0.72f, 0.98f * alpha);
        shapeRenderer.rect(x + 4f, y + 4f, size - 8f, size - 8f);

        shapeRenderer.setColor(c.r, c.g, c.b, 1f * alpha);
        shapeRenderer.rect(x + 6f, y + 6f, size - 12f, size - 12f);

        // Reflexo interno
        shapeRenderer.setColor(1f, 1f, 1f, 0.18f * alpha);
        shapeRenderer.rect(x + 4f, y + size - 9f, size * 0.40f, 3f);
    }

    // =========================================================
    // Text
    // =========================================================

    private void drawHudText(GameLayout layout, GameSession session) {
        batch.begin();
        drawScore(layout, session);
        drawTopStats(layout, session);
        drawBottomLabels(layout);
        drawHints(layout);
        batch.end();
    }

    private void drawScore(GameLayout layout, GameSession session) {
        float pulse = (MathUtils.sin(time * 4.5f) + 1f) * 0.5f;

        // Score com efeito neon ciano
        Rectangle ad = layout.topAdBounds;

        neon(labelFont,
            "SCORE  " + session.getScore(),
            ad.x,
            ad.y + ad.height / 2f + 8f,
            ad.width,
            new Color(0f, 1f, 1f, 1f),
            new Color(0.85f, 1f, 1f, 1f),
            0.65f + pulse * 0.28f);
    }

    private void drawTopStats(GameLayout layout, GameSession session) {
        // HOLD label
        drawShadowedCenteredText("HOLD", titleFont, TEXT_WHITE,
            layout.holdBox.x,
            layout.holdBox.y + layout.holdBox.height + 14f,
            layout.holdBox.width);

        // LEVEL label + valor
        drawShadowedCenteredText("LEVEL", labelFont, TEXT_WHITE,
            layout.levelBox.x,
            layout.levelBox.y + layout.levelBox.height + 14f,
            layout.levelBox.width);

        float lvlPulse = (MathUtils.sin(time * 3.0f) + 1f) * 0.5f;
        neon(valueFont,
            String.valueOf(session.getLevel()),
            layout.levelBox.x,
            layout.levelBox.y + 42f,
            layout.levelBox.width,
            new Color(1f, 0.85f, 0.10f, 1f),
            new Color(1f, 1f,    0.60f, 1f),
            0.70f + lvlPulse * 0.25f);

        // GOAL label + valor
        drawShadowedCenteredText("GOAL", labelFont, TEXT_WHITE,
            layout.goalBox.x,
            layout.goalBox.y + layout.goalBox.height + 14f,
            layout.goalBox.width);

        neon(valueFont,
            String.valueOf(session.getGoalLines()),
            layout.goalBox.x,
            layout.goalBox.y + 42f,
            layout.goalBox.width,
            new Color(0.40f, 1f, 0.55f, 1f),
            new Color(0.80f, 1f, 0.85f, 1f),
            0.70f + lvlPulse * 0.25f);
    }

    private void drawBottomLabels(GameLayout layout) {
        float nextRowX     = layout.nextMainBox.x;
        float nextRowWidth = (layout.nextQueueBox4.x + layout.nextQueueBox4.width)
            - layout.nextMainBox.x;

        drawShadowedCenteredText("NEXT", titleFont, TEXT_WHITE,
            nextRowX,
            layout.bottomShellBounds.y + layout.bottomShellBounds.height - 12f,
            nextRowWidth);
    }

    private void drawHints(GameLayout layout) {
        hintFont.setColor(TEXT_HINT);
        hintFont.draw(batch,
            "Tap rotate   Swipe move   Fling drop   Long press / C hold",
            layout.bottomShellBounds.x + 8f,
            layout.bottomShellBounds.y + 16f,
            layout.bottomShellBounds.width - 16f,
            Align.center, true);
    }

    // =========================================================
    // Helpers
    // =========================================================

    /**
     * Neon text — múltiplas camadas de glow + texto principal.
     */
    private void neon(BitmapFont font, String text,
                      float x, float y, float width,
                      Color glowColor, Color mainColor, float alpha) {
        float gr = glowColor.r, gg = glowColor.g, gb = glowColor.b;
        float mr = mainColor.r, mg = mainColor.g, mb = mainColor.b;

        font.setColor(gr, gg, gb, alpha * 0.10f);
        font.draw(batch, text, x,      y + 5f, width, Align.center, false);
        font.draw(batch, text, x - 5f, y,      width, Align.center, false);
        font.draw(batch, text, x + 5f, y,      width, Align.center, false);
        font.draw(batch, text, x,      y - 5f, width, Align.center, false);

        font.setColor(gr, gg, gb, alpha * 0.22f);
        font.draw(batch, text, x - 2f, y,      width, Align.center, false);
        font.draw(batch, text, x + 2f, y,      width, Align.center, false);
        font.draw(batch, text, x,      y + 2f, width, Align.center, false);

        font.setColor(gr, gg, gb, alpha * 0.45f);
        font.draw(batch, text, x, y, width, Align.center, false);

        font.setColor(mr, mg, mb, alpha);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

    private void drawShadowedCenteredText(String text, BitmapFont font, Color color,
                                          float x, float y, float width) {
        font.setColor(TEXT_SHADOW);
        font.draw(batch, text, x + 1.5f, y - 1.5f, width, Align.center, false);
        font.setColor(color);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

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

    // =========================================================
    // Lifecycle
    // =========================================================

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        labelFont.dispose();
        valueFont.dispose();
        hintFont.dispose();
    }
}
