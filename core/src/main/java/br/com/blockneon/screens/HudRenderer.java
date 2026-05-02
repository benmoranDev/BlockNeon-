package br.com.blockneon.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import br.com.blockneon.model.Tetromino;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;


public class HudRenderer {

    // =========================================================
    // Panel palette / Paleta dos painéis
    // =========================================================

    private static final Color PANEL_OUTER = new Color(0.36f, 0.32f, 0.46f, 1f);
    private static final Color PANEL_INNER = new Color(0.21f, 0.18f, 0.28f, 1f);
    private static final Color PANEL_HIGHLIGHT = new Color(1f, 1f, 1f, 0.06f);
    private static final Color PANEL_OUTLINE = new Color(0.60f, 0.56f, 0.78f, 0.90f);
    private static final Color BOX_OUTLINE = new Color(0.46f, 0.42f, 0.62f, 0.95f);

    // =========================================================
    // Piece palette / Paleta das peças
    // =========================================================

    private static final Color BLOCK_I = new Color(0.20f, 0.88f, 1.00f, 1f);
    private static final Color BLOCK_J = new Color(0.28f, 0.46f, 1.00f, 1f);
    private static final Color BLOCK_L = new Color(1.00f, 0.58f, 0.12f, 1f);
    private static final Color BLOCK_O = new Color(1.00f, 0.83f, 0.18f, 1f);
    private static final Color BLOCK_S = new Color(0.34f, 0.94f, 0.26f, 1f);
    private static final Color BLOCK_T = new Color(0.88f, 0.28f, 1.00f, 1f);
    private static final Color BLOCK_Z = new Color(1.00f, 0.22f, 0.20f, 1f);

    // =========================================================
    // Text colors / Cores do texto
    // =========================================================

    private static final Color TEXT_WHITE = new Color(1f, 1f, 1f, 1f);
    private static final Color TEXT_HINT = new Color(1f, 1f, 1f, 0.42f);
    private static final Color TEXT_SHADOW = new Color(0f, 0f, 0f, 0.35f);
    private static final Color TEXT_VALUE = new Color(1f, 0.72f, 0.12f, 1f);

    // =========================================================
    // Rendering / Renderização
    // =========================================================

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;

    // =========================================================
    // Fonts / Fontes
    // =========================================================

    private final BitmapFont titleFont;
    private final BitmapFont labelFont;
    private final BitmapFont valueFont;
    private final BitmapFont hintFont;

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

    /**
     * Renders the whole HUD layer.
     * Renderiza toda a camada de HUD.
     */
    public void render(OrthographicCamera camera, GameLayout layout, GameSession session) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawTopBottomPanels(layout);
        drawTopBottomBoxes(layout);
        drawHoldPiecePreview(layout, session);
        drawNextQueuePreviews(layout, session);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawTopBottomPanelOutlines(layout);
        drawTopBottomBoxOutlines(layout);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        drawHudText(layout, session);
    }

    // =========================================================
    // Shells / Carcaças
    // =========================================================

    /**
     * Draws the top and bottom shell panels.
     * Desenha os painéis externos superior e inferior.
     */
    private void drawTopBottomPanels(GameLayout layout) {
        drawArcadeShell(layout.topShellBounds);
        drawArcadeShell(layout.bottomShellBounds);
    }

    /**
     * Draws the outlines of the top and bottom shells.
     * Desenha os contornos dos painéis superior e inferior.
     */
    private void drawTopBottomPanelOutlines(GameLayout layout) {
        drawArcadeShellOutline(layout.topShellBounds);
        drawArcadeShellOutline(layout.bottomShellBounds);
    }

    /**
     * Draws all HUD boxes.
     * Desenha todas as caixas do HUD.
     */
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

    /**
     * Draws the outlines of all HUD boxes.
     * Desenha os contornos de todas as caixas do HUD.
     */
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

    /**
     * Draws one arcade shell panel.
     * Desenha um painel externo arcade.
     */
    private void drawArcadeShell(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_OUTER);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(PANEL_INNER);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + 4f, bounds.width - 8f, bounds.height - 8f);

        shapeRenderer.setColor(PANEL_HIGHLIGHT);
        shapeRenderer.rect(bounds.x + 6f, bounds.y + bounds.height - 16f, bounds.width - 12f, 7f);
    }

    /**
     * Draws one arcade shell outline.
     * Desenha o contorno de um painel externo arcade.
     */
    private void drawArcadeShellOutline(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_OUTLINE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(0.10f, 0.09f, 0.16f, 0.70f);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);
    }

    /**
     * Draws one internal arcade box.
     * Desenha uma caixa interna arcade.
     */
    private void drawArcadeBox(Rectangle bounds) {
        shapeRenderer.setColor(0.20f, 0.17f, 0.25f, 1f);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(0.13f, 0.12f, 0.18f, 1f);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(bounds.x + 4f, bounds.y + bounds.height - 11f, bounds.width - 8f, 4f);
    }

    /**
     * Draws the outline of one internal box.
     * Desenha o contorno de uma caixa interna.
     */
    private void drawArcadeBoxOutline(Rectangle bounds) {
        shapeRenderer.setColor(BOX_OUTLINE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // =========================================================
    // Previews / Previews
    // =========================================================

    /**
     * Draws the hold piece preview.
     * Desenha o preview da peça em hold.
     */
    private void drawHoldPiecePreview(GameLayout layout, GameSession session) {
        Tetromino held = session.getHeldTetromino();
        if (held == null) return;

        drawPreviewPieceInBox(held, layout.holdBox, 18f);
    }

    /**
     * Draws all next queue previews.
     * Desenha todos os previews da fila next.
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
     * Draws one preview piece centered inside a box.
     * Desenha uma peça centralizada dentro de uma caixa.
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

                drawPreviewBlock(
                    startX + c * previewCell + 1f,
                    startY + r * previewCell + 1f,
                    previewCell - 2f,
                    color,
                    1f
                );
            }
        }
    }

    /**
     * Draws one preview block with depth.
     * Desenha um bloco de preview com profundidade.
     */
    private void drawPreviewBlock(float x, float y, float size, Color baseColor, float alpha) {
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
    }

    // =========================================================
    // Text / Texto
    // =========================================================

    /**
     * Draws all HUD text labels and values.
     * Desenha todos os textos e valores do HUD.
     */
    private void drawHudText(GameLayout layout, GameSession session) {
        batch.begin();

        drawScore(layout, session);
        drawTopStats(layout, session);
        drawBottomLabels(layout);
        drawHints(layout);

        batch.end();
    }

    /**
     * Draws the score text centered in the top shell.
     * Desenha o score centralizado no shell superior.
     */
    private void drawScore(GameLayout layout, GameSession session) {
        drawShadowedCenteredText(
            "SCORE " + session.getScore(),
            labelFont,
            TEXT_WHITE,
            layout.topShellBounds.x,
            layout.topShellBounds.y + layout.topShellBounds.height - 10f,
            layout.topShellBounds.width
        );
    }

    /**
     * Draws hold, level and goal labels/values.
     * Desenha os rótulos e valores de hold, level e goal.
     */
    private void drawTopStats(GameLayout layout, GameSession session) {
        drawShadowedCenteredText(
            "HOLD",
            titleFont,
            TEXT_WHITE,
            layout.holdBox.x,
            layout.holdBox.y + layout.holdBox.height + 14f,
            layout.holdBox.width
        );

        drawShadowedCenteredText(
            "LEVEL",
            labelFont,
            TEXT_WHITE,
            layout.levelBox.x,
            layout.levelBox.y + layout.levelBox.height + 14f,
            layout.levelBox.width
        );

        drawShadowedCenteredText(
            String.valueOf(session.getLevel()),
            valueFont,
            TEXT_VALUE,
            layout.levelBox.x,
            layout.levelBox.y + 42f,
            layout.levelBox.width
        );

        drawShadowedCenteredText(
            "GOAL",
            labelFont,
            TEXT_WHITE,
            layout.goalBox.x,
            layout.goalBox.y + layout.goalBox.height + 14f,
            layout.goalBox.width
        );

        drawShadowedCenteredText(
            String.valueOf(session.getGoalLines()),
            valueFont,
            TEXT_VALUE,
            layout.goalBox.x,
            layout.goalBox.y + 42f,
            layout.goalBox.width
        );
    }

    /**
     * Draws labels for the bottom queue section.
     * Desenha os rótulos da seção inferior da fila.
     */
    private void drawBottomLabels(GameLayout layout) {
        float nextRowX = layout.nextMainBox.x;
        float nextRowWidth = (layout.nextQueueBox4.x + layout.nextQueueBox4.width) - layout.nextMainBox.x;

        drawShadowedCenteredText(
            "NEXT",
            titleFont,
            TEXT_WHITE,
            nextRowX,
            layout.bottomShellBounds.y + layout.bottomShellBounds.height - 12f,
            nextRowWidth
        );
    }

    /**
     * Draws control hints in the bottom shell.
     * Desenha as dicas de controle no shell inferior.
     */
    private void drawHints(GameLayout layout) {
        hintFont.setColor(TEXT_HINT);
        hintFont.draw(
            batch,
            "Tap rotate   Swipe move   Fling drop   Long press / C hold",
            layout.bottomShellBounds.x + 8f,
            layout.bottomShellBounds.y + 16f,
            layout.bottomShellBounds.width - 16f,
            Align.center,
            true
        );
    }

    /**
     * Draws centered text with shadow.
     * Desenha texto centralizado com sombra.
     */
    private void drawShadowedCenteredText(String text, BitmapFont font, Color color, float x, float y, float width) {
        font.setColor(TEXT_SHADOW);
        font.draw(batch, text, x + 1.5f, y - 1.5f, width, Align.center, false);

        font.setColor(color);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

    // =========================================================
    // Helpers / Auxiliares
    // =========================================================

    /**
     * Returns the color for a given tetromino id.
     * Retorna a cor para um id de tetromino.
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
     * Releases renderer and font resources.
     * Libera os recursos de renderização e fontes.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        labelFont.dispose();
        valueFont.dispose();
        hintFont.dispose();
    }
}
