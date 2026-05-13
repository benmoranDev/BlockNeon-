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

    private static final Color PANEL_OUTER     = new Color(0.04f, 0.10f, 0.20f, 1f);
    private static final Color PANEL_INNER     = new Color(0.02f, 0.06f, 0.14f, 1f);
    private static final Color PANEL_HIGHLIGHT = new Color(1f, 1f, 1f, 0.05f);
    private static final Color PANEL_OUTLINE   = new Color(0.20f, 0.80f, 1.00f, 0.55f);
    private static final Color BOX_OUTLINE     = new Color(0.18f, 0.70f, 0.90f, 0.70f);
    private static final Color BOX_BG          = new Color(0.03f, 0.08f, 0.16f, 1f);
    private static final Color BOX_BG_INNER    = new Color(0.02f, 0.05f, 0.10f, 1f);

    // =========================================================
    // Style tuning
    // =========================================================

    private static final float BEVEL       = 8f;
    private static final float INNER_BEVEL = 5f;

    // =========================================================
    // Piece palette
    // =========================================================

    private static final Color BLOCK_I = new Color(0.20f, 0.88f, 1.00f, 1f);
    private static final Color BLOCK_J = new Color(0.28f, 0.46f, 1.00f, 1f);
    private static final Color BLOCK_L = new Color(1.00f, 0.58f, 0.12f, 1f);
    private static final Color BLOCK_O = new Color(1.00f, 0.83f, 0.18f, 1f);
    private static final Color BLOCK_S = new Color(0.34f, 0.94f, 0.26f, 1f);
    private static final Color BLOCK_T = new Color(1.00f, 0.68f, 0.18f, 1f);
    private static final Color BLOCK_Z = new Color(1.00f, 0.22f, 0.20f, 1f);

    // =========================================================
    // Text colors
    // =========================================================

    private static final Color TEXT_WHITE       = new Color(1f, 1f, 1f, 1f);
    private static final Color TEXT_HINT        = new Color(1f, 1f, 1f, 0.38f);
    private static final Color TEXT_SHADOW      = new Color(0f, 0f, 0f, 0.50f);
    private static final Color TEXT_DISABLED    = new Color(0.75f, 0.82f, 0.90f, 0.45f);
    private static final Color TEXT_DISABLED_SH = new Color(0f, 0f, 0f, 0.28f);

    // =========================================================
    // Rendering
    // =========================================================

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;

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
        batch = new SpriteBatch();

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

    public void render(OrthographicCamera camera, GameLayout layout,
                       GameSession session, float delta) {

        delta = Math.min(delta, 1f / 30f);
        time += delta;

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Passe 1: Filled
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawTopBottomPanels(layout);
        drawTopAdBar(layout);
        drawTopBottomBoxes(layout, session);

        drawNextBoxScanline(layout.nextMainBox);
        drawNextBoxScanline(layout.nextQueueBox1);
        drawNextBoxScanline(layout.nextQueueBox2);
        drawNextBoxScanline(layout.nextQueueBox3);
        drawNextBoxScanline(layout.nextQueueBox4);

        drawHoldPiecePreview(layout, session);
        drawNextQueuePreviews(layout, session);
        shapeRenderer.end();

        // Passe 2: Line
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawTopBottomPanelOutlines(layout);
        drawTopAdBarOutline(layout.topAdBounds);
        drawTopBottomBoxOutlines(layout, session);
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

    private void drawTopAdBar(GameLayout layout) {
        Rectangle ad = layout.topAdBounds;
        float pulse = (MathUtils.sin(time * 2.8f) + 1f) * 0.5f;

        drawBeveledPanelFilled(
            ad, 6f,
            new Color(0.03f, 0.08f, 0.18f, 1f),
            new Color(0.01f, 0.04f, 0.10f, 1f)
        );

        shapeRenderer.setColor(0.20f, 0.80f, 1f, 0.04f + pulse * 0.03f);
        drawExpandedBeveledGlow(ad, 2f, 5f);
    }

    private void drawTopAdBarOutline(Rectangle ad) {
        float pulse = (MathUtils.sin(time * 2.8f) + 1f) * 0.5f;

        drawBeveledOutline(ad, 6f,
            new Color(0.20f, 0.85f, 1f, 0.45f + pulse * 0.20f));

        Rectangle inner = insetRect(ad, 2f);
        drawBeveledOutline(inner, 5f,
            new Color(0.40f, 1f, 1f, 0.20f + pulse * 0.12f));
    }

    private void drawTopBottomPanelOutlines(GameLayout layout) {
        drawArcadeShellOutline(layout.topShellBounds);
        drawArcadeShellOutline(layout.bottomShellBounds);
    }

    private void drawTopBottomBoxes(GameLayout layout, GameSession session) {
        boolean holdDisabled = session.isHoldUsedThisTurn();

        if (holdDisabled) {
            drawArcadeBoxDisabled(layout.holdBox);
        } else {
            drawArcadeBox(layout.holdBox);
        }

        drawArcadeBox(layout.levelBox);
        drawArcadeBox(layout.goalBox);
        drawArcadeBox(layout.nextMainBox);
        drawArcadeBox(layout.nextQueueBox1);
        drawArcadeBox(layout.nextQueueBox2);
        drawArcadeBox(layout.nextQueueBox3);
        drawArcadeBox(layout.nextQueueBox4);
    }

    private void drawTopBottomBoxOutlines(GameLayout layout, GameSession session) {
        boolean holdDisabled = session.isHoldUsedThisTurn();

        if (holdDisabled) {
            drawArcadeBoxOutlineDisabled(layout.holdBox);
        } else {
            drawArcadeBoxOutline(layout.holdBox);
        }

        drawArcadeBoxOutline(layout.levelBox);
        drawArcadeBoxOutline(layout.goalBox);
        drawArcadeBoxOutline(layout.nextMainBox);
        drawArcadeBoxOutline(layout.nextQueueBox1);
        drawArcadeBoxOutline(layout.nextQueueBox2);
        drawArcadeBoxOutline(layout.nextQueueBox3);
        drawArcadeBoxOutline(layout.nextQueueBox4);
    }

    private void drawArcadeShell(Rectangle bounds) {
        drawBeveledPanelFilled(bounds, BEVEL, PANEL_OUTER, PANEL_INNER);

        Rectangle inner = insetRect(bounds, 4f);
        drawBeveledPanelFilled(
            inner, INNER_BEVEL,
            new Color(PANEL_INNER),
            new Color(0.015f, 0.04f, 0.10f, 1f)
        );

        shapeRenderer.setColor(PANEL_HIGHLIGHT);
        shapeRenderer.rect(bounds.x + 10f, bounds.y + bounds.height - 18f,
            bounds.width - 20f, 6f);

        float pulse = (MathUtils.sin(time * 2.2f) + 1f) * 0.5f;

        shapeRenderer.setColor(0.20f, 0.80f, 1f, 0.02f + pulse * 0.02f);
        drawExpandedBeveledGlow(bounds, 6f, BEVEL + 2f);

        shapeRenderer.setColor(0.20f, 0.90f, 1f, 0.03f + pulse * 0.03f);
        drawExpandedBeveledGlow(bounds, 3f, BEVEL + 1f);
    }

    private void drawArcadeShellOutline(Rectangle bounds) {
        float pulse = (MathUtils.sin(time * 2.2f) + 1f) * 0.5f;

        drawBeveledOutline(bounds, BEVEL,
            new Color(0.20f, 0.80f, 1f, 0.35f + pulse * 0.20f));

        Rectangle inner = insetRect(bounds, 3f);
        drawBeveledOutline(inner, INNER_BEVEL,
            new Color(0.05f, 0.10f, 0.20f, 0.70f));
    }

    private void drawArcadeBox(Rectangle bounds) {
        float pulse = (MathUtils.sin(time * 2.0f) + 1f) * 0.5f;

        drawBeveledPanelFilled(bounds, 6f, BOX_BG, BOX_BG_INNER);

        Rectangle inner = insetRect(bounds, 3f);
        drawBeveledPanelFilled(
            inner, 4f,
            new Color(BOX_BG_INNER),
            new Color(0.01f, 0.03f, 0.08f, 1f)
        );

        shapeRenderer.setColor(1f, 1f, 1f, 0.04f + pulse * 0.01f);
        shapeRenderer.rect(bounds.x + 8f, bounds.y + bounds.height - 12f,
            bounds.width - 16f, 3f);

        shapeRenderer.setColor(0f, 0f, 0f, 0.18f);
        shapeRenderer.rect(bounds.x + 6f, bounds.y + 4f,
            bounds.width - 12f, 3f);

        shapeRenderer.setColor(0.35f, 0.95f, 1f, 0.04f + pulse * 0.03f);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + 6f,
            2f, bounds.height - 12f);

        shapeRenderer.setColor(0.30f, 0.95f, 1f, 0.03f + pulse * 0.03f);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + bounds.height - 5f,
            bounds.width - 8f, 2f);
    }

    private void drawArcadeBoxDisabled(Rectangle bounds) {
        drawArcadeBox(bounds);

        Rectangle inner = insetRect(bounds, 2f);

        shapeRenderer.setColor(0.02f, 0.04f, 0.08f, 0.30f);
        drawBeveledPanelFilled(inner, 5f,
            new Color(0.02f, 0.04f, 0.08f, 0.18f),
            new Color(0.01f, 0.02f, 0.05f, 0.30f));

        shapeRenderer.setColor(0f, 0f, 0f, 0.22f);
        shapeRenderer.rect(bounds.x + 6f, bounds.y + 6f,
            bounds.width - 12f, bounds.height - 12f);

        shapeRenderer.setColor(0.65f, 0.78f, 0.90f, 0.06f);
        shapeRenderer.triangle(
            bounds.x + 10f, bounds.y + bounds.height - 12f,
            bounds.x + bounds.width - 24f, bounds.y + bounds.height - 12f,
            bounds.x + bounds.width - 10f, bounds.y + 12f
        );

        shapeRenderer.setColor(0f, 0f, 0f, 0.10f);
        shapeRenderer.rect(bounds.x + 5f, bounds.y + bounds.height * 0.5f - 8f,
            bounds.width - 10f, 16f);
    }

    private void drawArcadeBoxOutline(Rectangle bounds) {
        float t = (MathUtils.sin(time * 1.6f) + 1f) * 0.5f;
        float alpha = 0.35f + t * 0.25f;

        drawBeveledOutline(bounds, 6f,
            new Color(BOX_OUTLINE.r, BOX_OUTLINE.g, BOX_OUTLINE.b, alpha));

        Rectangle inner = insetRect(bounds, 1.5f);
        drawBeveledOutline(inner, 5f,
            new Color(0.35f, 0.95f, 1f, 0.08f + t * 0.10f));
    }

    private void drawArcadeBoxOutlineDisabled(Rectangle bounds) {
        drawBeveledOutline(bounds, 6f,
            new Color(0.40f, 0.55f, 0.68f, 0.28f));

        Rectangle inner = insetRect(bounds, 1.5f);
        drawBeveledOutline(inner, 5f,
            new Color(0.28f, 0.40f, 0.52f, 0.18f));

        shapeRenderer.setColor(0.75f, 0.85f, 0.95f, 0.10f);
        shapeRenderer.line(bounds.x + 12f, bounds.y + bounds.height - 12f,
            bounds.x + bounds.width - 12f, bounds.y + 12f);
    }

    // =========================================================
    // Efeitos
    // =========================================================

    private void drawNextBoxScanline(Rectangle box) {
        float period = 2.6f;
        float t = (time % period) / period;

        float lineY = box.y + 3f + t * (box.height - 6f);
        float width = box.width - 6f;
        float startX = box.x + 3f;

        shapeRenderer.setColor(0.30f, 0.95f, 1f, 0.18f);
        shapeRenderer.rect(startX, lineY, width, 2f);

        shapeRenderer.setColor(0.15f, 0.70f, 1f, 0.08f);
        shapeRenderer.rect(startX, lineY + 2f, width, 3f);

        shapeRenderer.setColor(0.50f, 1f, 1f, 0.05f);
        shapeRenderer.rect(startX, lineY - 1f, width, 1f);
    }

    // =========================================================
    // Previews
    // =========================================================

    private void drawHoldPiecePreview(GameLayout layout, GameSession session) {
        Tetromino held = session.getHeldTetromino();
        if (held == null) return;

        float alpha = session.isHoldUsedThisTurn() ? 0.38f : 1f;
        drawPreviewPieceInBox(held, layout.holdBox, 18f, alpha);
    }

    private void drawNextQueuePreviews(GameLayout layout, GameSession session) {
        Array<Tetromino> nextQueue = session.getNextQueue();
        float mainSize = 20f;
        float queueSize = 16f;

        if (nextQueue.size > 0) drawPreviewPieceInBox(nextQueue.get(0), layout.nextMainBox, mainSize, 1.00f);
        if (nextQueue.size > 1) drawPreviewPieceInBox(nextQueue.get(1), layout.nextQueueBox1, queueSize, 0.85f);
        if (nextQueue.size > 2) drawPreviewPieceInBox(nextQueue.get(2), layout.nextQueueBox2, queueSize, 0.70f);
        if (nextQueue.size > 3) drawPreviewPieceInBox(nextQueue.get(3), layout.nextQueueBox3, queueSize, 0.55f);
        if (nextQueue.size > 4) drawPreviewPieceInBox(nextQueue.get(4), layout.nextQueueBox4, queueSize, 0.40f);
    }

    private void drawPreviewPieceInBox(Tetromino tetromino, Rectangle box, float previewCell, float alpha) {
        int[][] shape = tetromino.getCells();
        Color color = getColorForCell(tetromino.getColorId());

        int minR = shape.length, maxR = -1;
        int minC = shape[0].length, maxC = -1;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    if (r < minR) minR = r;
                    if (r > maxR) maxR = r;
                    if (c < minC) minC = c;
                    if (c > maxC) maxC = c;
                }
            }
        }

        int cellsW = maxC - minC + 1;
        int cellsH = maxR - minR + 1;

        float margin = 6f;
        float maxW = box.width - margin * 2f;
        float maxH = box.height - margin * 2f;

        float scaleX = maxW / (cellsW * previewCell);
        float scaleY = maxH / (cellsH * previewCell);
        float scale = Math.min(1f, Math.min(scaleX, scaleY));

        float cell = previewCell * scale;
        float pieceW = cellsW * cell;
        float pieceH = cellsH * cell;

        float startX = box.x + (box.width - pieceW) / 2f;
        float startY = box.y + (box.height - pieceH) / 2f;

        for (int r = minR; r <= maxR; r++) {
            for (int c = minC; c <= maxC; c++) {
                if (shape[r][c] == 0) continue;
                float x = startX + (c - minC) * cell + 1f;
                float y = startY + (r - minR) * cell + 1f;
                drawPreviewBlock(x, y, cell - 2f, color, alpha);
            }
        }
    }

    private void drawPreviewBlock(float x, float y, float size, Color c, float alpha) {
        if (size <= 8f) {
            shapeRenderer.setColor(c.r, c.g, c.b, alpha);
            shapeRenderer.rect(x, y, size, size);
            return;
        }

        shapeRenderer.setColor(c.r * 0.20f, c.g * 0.20f, c.b * 0.20f, 0.95f * alpha);
        shapeRenderer.rect(x, y, size, size);

        shapeRenderer.setColor(c.r * 0.45f, c.g * 0.45f, c.b * 0.45f, 0.95f * alpha);
        shapeRenderer.rect(x + 2f, y + 2f, size - 4f, size - 4f);

        shapeRenderer.setColor(c.r * 0.72f, c.g * 0.72f, c.b * 0.72f, 0.98f * alpha);
        shapeRenderer.rect(x + 4f, y + 4f, size - 8f, size - 8f);

        float coreInset = Math.min(6f, size * 0.28f);
        shapeRenderer.setColor(c.r, c.g, c.b, alpha);
        shapeRenderer.rect(x + coreInset, y + coreInset, size - coreInset * 2f, size - coreInset * 2f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.18f * alpha);
        shapeRenderer.rect(x + 3f, y + size - 7f, Math.max(3f, size * 0.35f), 2f);
    }

    // =========================================================
    // Text
    // =========================================================

    private void drawHudText(GameLayout layout, GameSession session) {
        batch.begin();
        drawScore(layout, session);
        drawTopStats(layout, session);
        drawBottomLabels(layout, session);
        drawHints(layout);
        batch.end();
    }

    private void drawScore(GameLayout layout, GameSession session) {
        Rectangle ad = layout.topAdBounds;
        float baseY = ad.y + ad.height / 2f + 8f;

        String text = "SCORE  " + session.getScore();
        float pulse = (MathUtils.sin(time * 4.5f) + 1f) * 0.5f;
        float alpha = 0.60f + pulse * 0.24f;

        neon(labelFont, text, ad.x, baseY, ad.width,
            new Color(0f, 1f, 1f, 1f),
            new Color(0.88f, 1f, 1f, 1f),
            alpha);

        float offset = MathUtils.sin(time * 2.4f) * 3f;
        labelFont.setColor(1f, 1f, 1f, 0.14f);
        labelFont.draw(batch, text, ad.x, baseY + offset, ad.width, Align.center, false);
    }

    private void drawTopStats(GameLayout layout, GameSession session) {
        boolean holdUsed = session.isHoldUsedThisTurn();

        if (holdUsed) {
            drawShadowedCenteredText("HOLD", titleFont, TEXT_DISABLED, TEXT_DISABLED_SH,
                layout.holdBox.x,
                layout.holdBox.y + layout.holdBox.height + 14f,
                layout.holdBox.width);
        } else {
            drawShadowedCenteredText("HOLD", titleFont, TEXT_WHITE, TEXT_SHADOW,
                layout.holdBox.x,
                layout.holdBox.y + layout.holdBox.height + 14f,
                layout.holdBox.width);
        }

        drawShadowedCenteredText("LEVEL", labelFont, TEXT_WHITE, TEXT_SHADOW,
            layout.levelBox.x,
            layout.levelBox.y + layout.levelBox.height + 14f,
            layout.levelBox.width);

        float lvlPulse = (MathUtils.sin(time * 3.0f) + 1f) * 0.5f;
        neon(valueFont,
            String.valueOf(session.getLevel()),
            layout.levelBox.x,
            layout.levelBox.y + 42f,
            layout.levelBox.width,
            new Color(1f, 0.82f, 0.10f, 1f),
            new Color(1f, 0.96f, 0.58f, 1f),
            0.70f + lvlPulse * 0.22f);

        drawShadowedCenteredText("GOAL", labelFont, TEXT_WHITE, TEXT_SHADOW,
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
            0.70f + lvlPulse * 0.22f);
    }

    private void drawBottomLabels(GameLayout layout, GameSession session) {
        float nextRowX = layout.nextMainBox.x;
        float nextRowWidth = (layout.nextQueueBox4.x + layout.nextQueueBox4.width) - layout.nextMainBox.x;

        float pulse = (MathUtils.sin(time * 2.6f) + 1f) * 0.5f;
        float alpha = 0.72f + pulse * 0.20f;

        neon(titleFont,
            "NEXT",
            nextRowX,
            layout.bottomShellBounds.y + layout.bottomShellBounds.height - 12f,
            nextRowWidth,
            new Color(0.10f, 0.85f, 1f, 1f),
            new Color(0.82f, 1f, 1f, 1f),
            alpha);
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

    private void neon(BitmapFont font, String text,
                      float x, float y, float width,
                      Color glowColor, Color mainColor, float alpha) {
        float gr = glowColor.r, gg = glowColor.g, gb = glowColor.b;
        float mr = mainColor.r, mg = mainColor.g, mb = mainColor.b;

        font.setColor(gr, gg, gb, alpha * 0.10f);
        font.draw(batch, text, x, y + 5f, width, Align.center, false);
        font.draw(batch, text, x - 5f, y, width, Align.center, false);
        font.draw(batch, text, x + 5f, y, width, Align.center, false);
        font.draw(batch, text, x, y - 5f, width, Align.center, false);

        font.setColor(gr, gg, gb, alpha * 0.20f);
        font.draw(batch, text, x - 2f, y, width, Align.center, false);
        font.draw(batch, text, x + 2f, y, width, Align.center, false);
        font.draw(batch, text, x, y + 2f, width, Align.center, false);

        font.setColor(gr, gg, gb, alpha * 0.42f);
        font.draw(batch, text, x, y, width, Align.center, false);

        font.setColor(mr, mg, mb, alpha);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

    private void drawShadowedCenteredText(String text, BitmapFont font, Color color,
                                          float x, float y, float width) {
        drawShadowedCenteredText(text, font, color, TEXT_SHADOW, x, y, width);
    }

    private void drawShadowedCenteredText(String text, BitmapFont font, Color color, Color shadowColor,
                                          float x, float y, float width) {
        font.setColor(shadowColor);
        font.draw(batch, text, x + 1.5f, y - 1.5f, width, Align.center, false);
        font.setColor(color);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

    private Rectangle insetRect(Rectangle r, float amount) {
        return new Rectangle(
            r.x + amount,
            r.y + amount,
            r.width - amount * 2f,
            r.height - amount * 2f
        );
    }

    private void drawBeveledPanelFilled(Rectangle r, float cut, Color outer, Color inner) {
        float x = r.x;
        float y = r.y;
        float w = r.width;
        float h = r.height;

        shapeRenderer.setColor(outer);
        shapeRenderer.rect(x + cut, y, w - cut * 2f, h);
        shapeRenderer.rect(x, y + cut, w, h - cut * 2f);
        shapeRenderer.triangle(x, y + cut, x + cut, y, x + cut, y + cut);
        shapeRenderer.triangle(x + w - cut, y, x + w, y + cut, x + w - cut, y + cut);
        shapeRenderer.triangle(x, y + h - cut, x + cut, y + h, x + cut, y + h - cut);
        shapeRenderer.triangle(x + w - cut, y + h - cut, x + w, y + h - cut, x + w - cut, y + h);

        Rectangle innerRect = insetRect(r, 3f);
        float ix = innerRect.x;
        float iy = innerRect.y;
        float iw = innerRect.width;
        float ih = innerRect.height;
        float ic = Math.max(2f, cut - 2f);

        shapeRenderer.setColor(inner);
        shapeRenderer.rect(ix + ic, iy, iw - ic * 2f, ih);
        shapeRenderer.rect(ix, iy + ic, iw, ih - ic * 2f);
        shapeRenderer.triangle(ix, iy + ic, ix + ic, iy, ix + ic, iy + ic);
        shapeRenderer.triangle(ix + iw - ic, iy, ix + iw, iy + ic, ix + iw - ic, iy + ic);
        shapeRenderer.triangle(ix, iy + ih - ic, ix + ic, iy + ih, ix + ic, iy + ih - ic);
        shapeRenderer.triangle(ix + iw - ic, iy + ih - ic, ix + iw, iy + ih - ic, ix + iw - ic, iy + ih);

        shapeRenderer.setColor(1f, 1f, 1f, 0.035f);
        shapeRenderer.triangle(x + cut, y + h - 2f, x + w - cut, y + h - 2f, x + cut, y + h - 10f);

        shapeRenderer.setColor(0f, 0f, 0f, 0.10f);
        shapeRenderer.triangle(x + w - cut, y + 2f, x + w - 2f, y + h - cut, x + 14f, y + 2f);
    }

    private void drawExpandedBeveledGlow(Rectangle r, float expand, float cut) {
        Rectangle e = new Rectangle(
            r.x - expand,
            r.y - expand,
            r.width + expand * 2f,
            r.height + expand * 2f
        );

        float x = e.x;
        float y = e.y;
        float w = e.width;
        float h = e.height;
        float c = cut;

        shapeRenderer.rect(x + c, y, w - c * 2f, h);
        shapeRenderer.rect(x, y + c, w, h - c * 2f);
        shapeRenderer.triangle(x, y + c, x + c, y, x + c, y + c);
        shapeRenderer.triangle(x + w - c, y, x + w, y + c, x + w - c, y + c);
        shapeRenderer.triangle(x, y + h - c, x + c, y + h, x + c, y + h - c);
        shapeRenderer.triangle(x + w - c, y + h - c, x + w, y + h - c, x + w - c, y + h);
    }

    private void drawBeveledOutline(Rectangle r, float cut, Color color) {
        shapeRenderer.setColor(color);

        float x1 = r.x + cut,           y1 = r.y;
        float x2 = r.x + r.width - cut, y2 = r.y;
        float x3 = r.x + r.width,       y3 = r.y + cut;
        float x4 = r.x + r.width,       y4 = r.y + r.height - cut;
        float x5 = r.x + r.width - cut, y5 = r.y + r.height;
        float x6 = r.x + cut,           y6 = r.y + r.height;
        float x7 = r.x,                 y7 = r.y + r.height - cut;
        float x8 = r.x,                 y8 = r.y + cut;

        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.line(x2, y2, x3, y3);
        shapeRenderer.line(x3, y3, x4, y4);
        shapeRenderer.line(x4, y4, x5, y5);
        shapeRenderer.line(x5, y5, x6, y6);
        shapeRenderer.line(x6, y6, x7, y7);
        shapeRenderer.line(x7, y7, x8, y8);
        shapeRenderer.line(x8, y8, x1, y1);
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

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        labelFont.dispose();
        valueFont.dispose();
        hintFont.dispose();
    }
}
