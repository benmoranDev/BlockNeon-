package br.com.blockneon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class BackgroundRenderer {

    // =========================================================
    // Palette / Paleta
    // =========================================================

    private static final Color PANEL_GLASS       = new Color(0.04f, 0.12f, 0.22f, 0.72f);
    private static final Color PANEL_GLASS_INNER = new Color(0.10f, 0.26f, 0.40f, 0.14f);
    private static final Color PANEL_OUTLINE     = new Color(0.30f, 0.90f, 1.00f, 0.65f);
    private static final Color PANEL_GLOW        = new Color(0.20f, 0.80f, 1.00f, 0.07f);

    private static final Color COMBO_2 = new Color(0.20f, 1.00f, 0.60f, 1f);
    private static final Color COMBO_4 = new Color(0.20f, 0.80f, 1.00f, 1f);
    private static final Color COMBO_6 = new Color(0.00f, 0.70f, 1.00f, 1f); // era roxo
    private static final Color COMBO_8 = new Color(1.00f, 0.90f, 0.20f, 1f);

    // =========================================================
    // Combo state
    // =========================================================

    private int   comboCount = 0;
    private float comboTimer = 0f;
    private float comboPulse = 0f;

    private static final float COMBO_FLASH_DURATION = 0.45f;
    private static final float COMBO_PULSE_DURATION = 1.80f;

    // =========================================================
    // Theme
    // =========================================================

    private BackgroundStyle currentStyle = BackgroundStyle.SPACE;
    private BackgroundStyle targetStyle  = BackgroundStyle.SPACE;
    private float           themeBlend   = 1f;
    private static final float BLEND_SPEED = 0.6f;

    // ── SPACE — estrelas ──────────────────────────────────────
    private static final int STAR_COUNT = 160;
    private final float[] starX      = new float[STAR_COUNT];
    private final float[] starY      = new float[STAR_COUNT];
    private final float[] starSize   = new float[STAR_COUNT];
    private final float[] starAlpha  = new float[STAR_COUNT];
    private final float[] starPhase  = new float[STAR_COUNT];
    private boolean starsInit = false;

    // ── NEON_CITY — prédios ───────────────────────────────────
    private static final int BUILDING_COUNT = 18;
    private final float[] buildX     = new float[BUILDING_COUNT];
    private final float[] buildW     = new float[BUILDING_COUNT];
    private final float[] buildH     = new float[BUILDING_COUNT];
    private final Color[] buildColor = new Color[BUILDING_COUNT];
    private boolean buildingsInit = false;

    // ── GEOMETRIC — formas ───────────────────────────────────
    private static final int SHAPE_COUNT = 20;
    private final float[] shapeX      = new float[SHAPE_COUNT];
    private final float[] shapeY      = new float[SHAPE_COUNT];
    private final float[] shapeSize   = new float[SHAPE_COUNT];
    private final float[] shapeSpeed  = new float[SHAPE_COUNT];
    private final float[] shapeAngle  = new float[SHAPE_COUNT];
    private final Color[] shapeColor  = new Color[SHAPE_COUNT];
    private boolean shapesInit = false;

    // =========================================================
    // Renderer
    // =========================================================

    private final ShapeRenderer shapeRenderer;

    public BackgroundRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    // =========================================================
    // Public API
    // =========================================================

    public void notifyCombo(int count) {
        comboCount = count;
        comboTimer = COMBO_FLASH_DURATION;
        comboPulse = COMBO_PULSE_DURATION;
    }

    public void resetCombo() {
        comboCount = 0;
        comboPulse = 0f;
    }

    public void updateTheme(int score) {
        BackgroundStyle next;
        if      (score >= 30000) next = BackgroundStyle.GRADIENT;
        else if (score >= 15000) next = BackgroundStyle.GEOMETRIC;
        else if (score >= 5000)  next = BackgroundStyle.NEON_CITY;
        else                     next = BackgroundStyle.SPACE;

        if (next != targetStyle) {
            currentStyle = targetStyle;
            targetStyle  = next;
            themeBlend   = 0f;
        }
    }

    public void render(OrthographicCamera camera, GameLayout layout, float time, float delta) {
        updateCombo(delta);

        shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawSpace(layout, time, 1f);              // ← só isso
        // NÃO chamar drawTheme(currentStyle/targetStyle)
        drawSharedOverlay(layout, time);
        drawComboScreenFlash(layout);
        drawBoardAmbientGlow(layout, time);
        drawComboEdgeGlow(layout);
        drawHudShells(layout);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawHudShellOutlines(layout);
        drawComboBoardRing(layout);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // =========================================================
    // Theme dispatcher
    // =========================================================

    private void drawTheme(BackgroundStyle style, GameLayout layout,
                           float time, float alpha) {
        if (alpha <= 0f) return;
        switch (style) {
            case SPACE:     drawSpace(layout, time, alpha);     break;
            case NEON_CITY: drawNeonCity(layout, time, alpha);  break;
            case GEOMETRIC: drawGeometric(layout, time, alpha); break;
            case GRADIENT:  drawGradient(layout, time, alpha);  break;
        }
    }

    // =========================================================
    // Shared overlay
    // =========================================================

    private void drawSharedOverlay(GameLayout layout, float time) {
        float vw      = layout.viewportWidth;
        float vh      = layout.viewportHeight;
        float cellW   = 22f;
        float cellH   = 22f;
        float dotSize = 1.4f;
        float pulse   = (MathUtils.sin(time * 1.6f) + 1f) * 0.5f;

        for (float x = cellW * 0.5f; x < vw; x += cellW) {
            for (float y = cellH * 0.5f; y < vh; y += cellH) {
                float dx   = Math.abs(x - vw * 0.5f) / (vw * 0.5f);
                float dy   = Math.abs(y - vh * 0.5f) / (vh * 0.5f);
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / 1.414f;
                float a    = (0.07f + pulse * 0.04f) * (1f - dist * 0.65f);

                shapeRenderer.setColor(0.40f, 0.85f, 1f, a);
                shapeRenderer.rect(x - dotSize * 0.5f, y - dotSize * 0.5f,
                    dotSize, dotSize);
            }
        }
    }

    // =========================================================
    // Theme: SPACE
    // =========================================================

    private void drawSpace(GameLayout layout, float time, float alpha) {
        float vw = layout.viewportWidth;
        float vh = layout.viewportHeight;

        shapeRenderer.setColor(0.00f, 0.01f, 0.06f, alpha);
        shapeRenderer.rect(0f, 0f, vw, vh);

        shapeRenderer.setColor(0.01f, 0.04f, 0.14f, alpha * 0.80f);
        shapeRenderer.rect(0f, 0f, vw, vh * 0.65f);

        shapeRenderer.setColor(0.03f, 0.08f, 0.20f, alpha * 0.50f);
        shapeRenderer.rect(0f, 0f, vw, vh * 0.35f);

        // Nebulosa — apenas azul
        float nb = (MathUtils.sin(time * 0.5f) + 1f) * 0.5f;

        for (int i = 6; i >= 1; i--) {
            float r = 110f * i;
            shapeRenderer.setColor(0.05f, 0.20f, 0.70f,
                alpha * (0.022f + nb * 0.006f) / i);
            shapeRenderer.ellipse(vw * 0.12f - r * 0.5f,
                vh * 0.65f - r * 0.5f, r, r);
        }
        for (int i = 6; i >= 1; i--) {
            float r = 100f * i;
            shapeRenderer.setColor(0.05f, 0.15f, 0.55f,
                alpha * (0.018f + nb * 0.005f) / i);
            shapeRenderer.ellipse(vw * 0.82f - r * 0.5f,
                vh * 0.35f - r * 0.5f, r, r);
        }

        if (!starsInit) initStars(vw, vh);

        for (int i = 0; i < STAR_COUNT; i++) {
            float flicker = (MathUtils.sin(time * 1.8f + starPhase[i]) + 1f) * 0.5f;
            float a  = alpha * starAlpha[i] * (0.55f + flicker * 0.45f);
            float sz = starSize[i];

            if (sz > 1.8f) {
                shapeRenderer.setColor(0.70f, 0.88f, 1f, a * 0.25f);
                shapeRenderer.rect(starX[i] - sz, starY[i] - sz, sz * 3f, sz * 3f);
            }
            shapeRenderer.setColor(0.88f, 0.94f, 1f, a);
            shapeRenderer.rect(starX[i], starY[i], sz, sz);
        }
    }

    private void initStars(float vw, float vh) {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = MathUtils.random(0f, vw);
            starY[i]     = MathUtils.random(vh * 0.12f, vh);
            starSize[i]  = MathUtils.random(0.7f, 2.6f);
            starAlpha[i] = MathUtils.random(0.25f, 1.0f);
            starPhase[i] = MathUtils.random(0f, MathUtils.PI2);
        }
        starsInit = true;
    }

    // =========================================================
    // Theme: NEON CITY
    // =========================================================

    private void drawNeonCity(GameLayout layout, float time, float alpha) {
        float vw = layout.viewportWidth;
        float vh = layout.viewportHeight;

        // Fundo — azul escuro apenas
        shapeRenderer.setColor(0.01f, 0.02f, 0.10f, alpha);
        shapeRenderer.rect(0f, 0f, vw, vh);

        shapeRenderer.setColor(0.02f, 0.04f, 0.14f, alpha * 0.65f); // era roxo
        shapeRenderer.rect(0f, vh * 0.30f, vw, vh * 0.50f);

        // Lua
        float moonX = vw * 0.82f;
        float moonY = vh * 0.78f;
        for (int i = 5; i >= 1; i--) {
            float r = 18f * i;
            shapeRenderer.setColor(1f, 0.50f, 0.10f, alpha * 0.06f / i);
            shapeRenderer.ellipse(moonX - r, moonY - r, r * 2f, r * 2f);
        }
        shapeRenderer.setColor(1f, 0.70f, 0.30f, alpha * 0.90f);
        shapeRenderer.ellipse(moonX - 9f, moonY - 9f, 18f, 18f);

        // Reflexo no chão — ciano (era roxo)
        float gp = (MathUtils.sin(time * 1.4f) + 1f) * 0.5f;
        shapeRenderer.setColor(0.00f, 0.60f, 0.90f, alpha * (0.04f + gp * 0.02f)); // era roxo
        shapeRenderer.rect(0f, 0f, vw, vh * 0.16f);
        shapeRenderer.setColor(0f, 0.70f, 1f, alpha * (0.04f + gp * 0.025f));
        shapeRenderer.rect(0f, 0f, vw, vh * 0.08f);

        if (!buildingsInit) initBuildings(vw, vh);

        float pulse = (MathUtils.sin(time * 1.8f) + 1f) * 0.5f;

        for (int i = 0; i < BUILDING_COUNT; i++) {
            Color c = buildColor[i];

            shapeRenderer.setColor(c.r, c.g, c.b, alpha * 0.04f);
            shapeRenderer.rect(buildX[i] - 4f, 0f, buildW[i] + 8f, 18f);

            shapeRenderer.setColor(c.r * 0.08f, c.g * 0.08f, c.b * 0.10f, alpha * 0.95f);
            shapeRenderer.rect(buildX[i], 0f, buildW[i], buildH[i]);

            shapeRenderer.setColor(c.r, c.g, c.b, alpha * (0.55f + pulse * 0.25f));
            shapeRenderer.rect(buildX[i], buildH[i] - 2f, buildW[i], 2f);

            shapeRenderer.setColor(c.r, c.g, c.b, alpha * (0.20f + pulse * 0.12f));
            shapeRenderer.rect(buildX[i], 0f, 1.5f, buildH[i]);
            shapeRenderer.rect(buildX[i] + buildW[i] - 1.5f, 0f, 1.5f, buildH[i]);

            drawBuildingWindows(buildX[i], buildH[i], buildW[i], c, time, alpha);
        }

        // Linha de chão — ciano (era roxo)
        shapeRenderer.setColor(0.00f, 0.80f, 1.00f, alpha * (0.65f + pulse * 0.20f)); // era roxo
        shapeRenderer.rect(0f, 1f, vw, 2f);
        shapeRenderer.setColor(0f, 0.80f, 1.00f, alpha * (0.35f + pulse * 0.15f));
        shapeRenderer.rect(0f, 3f, vw, 1f);
    }

    private void initBuildings(float vw, float vh) {
        Color[] palette = {
            new Color(0.00f, 0.90f, 1.00f, 1f),  // ciano
            new Color(0.10f, 0.60f, 1.00f, 1f),  // azul (era magenta)
            new Color(0.20f, 0.60f, 1.00f, 1f),  // azul médio (era violeta)
            new Color(1.00f, 0.55f, 0.00f, 1f),  // laranja
            new Color(0.20f, 1.00f, 0.55f, 1f),  // verde
        };

        float x = 0f;
        for (int i = 0; i < BUILDING_COUNT; i++) {
            buildW[i]     = MathUtils.random(22f, 48f);
            buildH[i]     = MathUtils.random(vh * 0.18f, vh * 0.58f);
            buildX[i]     = x;
            buildColor[i] = palette[i % palette.length];
            x += buildW[i] + MathUtils.random(1f, 8f);
        }
        buildingsInit = true;
    }

    private void drawBuildingWindows(float bx, float bh, float bw,
                                     Color c, float time, float alpha) {
        int cols = Math.max(1, (int)(bw / 11f));
        int rows = Math.max(1, (int)(bh / 15f));

        for (int col = 0; col < cols; col++) {
            for (int row = 1; row <= rows; row++) {
                float flicker = MathUtils.sin(time * 2.8f + col * 1.5f + row * 1.1f);
                if (flicker < -0.20f) continue;

                float wx = bx + 4f + col * 11f;
                float wy = row * 13f;
                float wa = alpha * (0.22f + (flicker + 1f) * 0.14f);

                shapeRenderer.setColor(
                    Math.min(1f, c.r * 0.6f + 0.4f),
                    Math.min(1f, c.g * 0.4f + 0.3f),
                    0.15f,
                    wa
                );
                shapeRenderer.rect(wx, wy, 5f, 7f);
            }
        }
    }

    // =========================================================
    // Theme: GEOMETRIC
    // =========================================================

    private void drawGeometric(GameLayout layout, float time, float alpha) {
        float vw = layout.viewportWidth;
        float vh = layout.viewportHeight;

        // Fundo — azul escuro (era roxo)
        shapeRenderer.setColor(0.01f, 0.03f, 0.10f, alpha);
        shapeRenderer.rect(0f, 0f, vw, vh);

        for (int i = 4; i >= 1; i--) {
            float r = Math.min(vw, vh) * 0.55f * i;
            shapeRenderer.setColor(0.04f, 0.12f, 0.30f, alpha * 0.08f / i); // era roxo
            shapeRenderer.ellipse(vw * 0.5f - r * 0.5f, vh * 0.5f - r * 0.5f, r, r);
        }

        if (!shapesInit) initShapes(vw, vh);

        for (int i = 0; i < SHAPE_COUNT; i++) {
            float cx    = vw * 0.5f + MathUtils.cos(time * shapeSpeed[i] + shapeAngle[i]) * shapeX[i];
            float cy    = vh * 0.5f + MathUtils.sin(time * shapeSpeed[i] * 0.75f + shapeAngle[i]) * shapeY[i];
            float sz    = shapeSize[i];
            Color c     = shapeColor[i];
            float pulse = (MathUtils.sin(time * 2.2f + i * 0.8f) + 1f) * 0.5f;

            shapeRenderer.setColor(c.r, c.g, c.b, alpha * 0.03f);
            shapeRenderer.rect(cx - sz * 1.8f, cy - sz * 1.8f, sz * 3.6f, sz * 3.6f);

            shapeRenderer.setColor(c.r, c.g, c.b, alpha * (0.06f + pulse * 0.05f));
            shapeRenderer.rect(cx - sz, cy - sz, sz * 2f, sz * 2f);

            float d = sz * 0.65f;
            shapeRenderer.setColor(c.r, c.g, c.b, alpha * (0.09f + pulse * 0.07f));
            shapeRenderer.triangle(cx - d, cy, cx, cy + d, cx + d, cy);
            shapeRenderer.triangle(cx - d, cy, cx, cy - d, cx + d, cy);

            shapeRenderer.setColor(c.r, c.g, c.b, alpha * (0.30f + pulse * 0.25f));
            float core = sz * 0.22f;
            shapeRenderer.rect(cx - core, cy - core, core * 2f, core * 2f);
        }

        // Grade — ciano (era roxo)
        shapeRenderer.setColor(0.20f, 0.60f, 0.90f, alpha * 0.04f);
        for (float x = 0; x < vw; x += 38f) shapeRenderer.rect(x, 0f, 1f, vh);
        for (float y = 0; y < vh; y += 38f) shapeRenderer.rect(0f, y, vw, 1f);

        // Linhas diagonais — ciano (era roxo)
        shapeRenderer.setColor(0.15f, 0.65f, 1.00f, alpha * 0.03f);
        for (int i = -6; i < 14; i++) {
            float ox = i * (vw / 8f);
            shapeRenderer.rectLine(ox, 0f, ox + vh, vh, 1f);
        }
    }

    private void initShapes(float vw, float vh) {
        Color[] palette = {
            new Color(0.30f, 0.75f, 1.00f, 1f),  // azul claro
            new Color(0.00f, 0.85f, 1.00f, 1f),  // ciano (era violeta)
            new Color(0.15f, 1.00f, 0.65f, 1f),  // verde
            new Color(1.00f, 0.55f, 0.15f, 1f),  // laranja
            new Color(0.10f, 0.60f, 1.00f, 1f),  // azul médio (era magenta)
        };

        for (int i = 0; i < SHAPE_COUNT; i++) {
            shapeX[i]     = MathUtils.random(25f, vw * 0.46f);
            shapeY[i]     = MathUtils.random(25f, vh * 0.46f);
            shapeSize[i]  = MathUtils.random(10f, 34f);
            shapeSpeed[i] = MathUtils.random(0.10f, 0.45f);
            shapeAngle[i] = MathUtils.random(0f, MathUtils.PI2);
            shapeColor[i] = palette[i % palette.length];
        }
        shapesInit = true;
    }

    // =========================================================
    // Theme: GRADIENT
    // =========================================================

    private void drawGradient(GameLayout layout, float time, float alpha) {
        float vw    = layout.viewportWidth;
        float vh    = layout.viewportHeight;
        float pulse = (MathUtils.sin(time * 0.7f) + 1f) * 0.5f;
        float shift = (MathUtils.sin(time * 0.35f) + 1f) * 0.5f;
        float wave  = (MathUtils.sin(time * 1.1f) + 1f) * 0.5f;

        // Base — azul escuro (era roxo/magenta)
        shapeRenderer.setColor(
            0.02f + shift * 0.02f,
            0.03f + pulse * 0.04f,
            0.14f + shift * 0.08f,
            alpha
        );
        shapeRenderer.rect(0f, 0f, vw, vh);

        // Faixas de aurora — azul/ciano (era com R alto)
        int bands = 12;
        for (int i = 0; i < bands; i++) {
            float t  = (float) i / bands;
            float y  = vh * t;
            float bh = vh / bands + 2f;
            float s  = MathUtils.sin(time * 0.5f + t * MathUtils.PI * 2f);
            float a  = alpha * 0.035f * (s + 1f);

            shapeRenderer.setColor(
                0.02f + t * 0.05f + shift * 0.03f,  // R quase zero — sem roxo
                0.10f + t * 0.20f + pulse * 0.10f,
                0.50f + t * 0.30f + wave * 0.15f,
                a
            );
            shapeRenderer.rect(0f, y, vw, bh);
        }

        // Orbe central — azul (era magenta/roxo)
        float orbX = vw * (0.45f + shift * 0.10f);
        float orbY = vh * (0.42f + pulse * 0.08f);
        float orbR = 60f + wave * 20f;

        for (int i = 8; i >= 1; i--) {
            float r = orbR * i * 0.6f;
            shapeRenderer.setColor(
                0.10f + shift * 0.10f,  // era 0.50f + shift*0.30f → magenta
                0.30f + pulse * 0.20f,
                0.95f,
                alpha * 0.018f / i
            );
            shapeRenderer.ellipse(orbX - r * 0.5f, orbY - r * 0.5f, r, r);
        }

        // Orbe menor — ciano/verde (era magenta)
        float orb2X = vw * (0.70f + wave * 0.08f);
        float orb2Y = vh * (0.65f + shift * 0.06f);
        for (int i = 5; i >= 1; i--) {
            float r = 35f * i;
            shapeRenderer.setColor(
                0.10f + wave * 0.10f,   // era 0.80f → magenta
                0.50f + pulse * 0.20f,
                0.90f + wave * 0.10f,
                alpha * 0.020f / i
            );
            shapeRenderer.ellipse(orb2X - r * 0.5f, orb2Y - r * 0.5f, r, r);
        }
    }

    // =========================================================
    // Combo
    // =========================================================

    private void updateCombo(float delta) {
        if (comboTimer > 0f) comboTimer -= delta;
        if (comboPulse > 0f) comboPulse -= delta;
    }

    private Color getComboColor() {
        if (comboCount >= 8) return COMBO_8;
        if (comboCount >= 6) return COMBO_6;
        if (comboCount >= 4) return COMBO_4;
        return COMBO_2;
    }

    private void drawComboScreenFlash(GameLayout layout) {
        if (comboTimer <= 0f || comboCount < 2) return;

        float progress = comboTimer / COMBO_FLASH_DURATION;
        Color c = getComboColor();
        shapeRenderer.setColor(c.r, c.g, c.b, progress * 0.15f);
        shapeRenderer.rect(0, 0, layout.viewportWidth, layout.viewportHeight);
    }

    private void drawComboEdgeGlow(GameLayout layout) {
        if (comboPulse <= 0f || comboCount < 2) return;

        float progress = Math.min(comboPulse / COMBO_PULSE_DURATION, 1f);
        float wave     = (MathUtils.sin(comboPulse * 6f) + 1f) * 0.5f;
        float alpha    = progress * (0.28f + wave * 0.20f);
        float barW     = 6f + comboCount * 2.5f;
        Color c        = getComboColor();

        shapeRenderer.setColor(c.r, c.g, c.b, alpha);
        shapeRenderer.rect(layout.boardX - barW - 2f, layout.boardY,
            barW, GameLayout.BOARD_HEIGHT);
        shapeRenderer.setColor(c.r, c.g, c.b, alpha);
        shapeRenderer.rect(layout.boardX + GameLayout.BOARD_WIDTH + 2f, layout.boardY,
            barW, GameLayout.BOARD_HEIGHT);
    }

    private void drawComboBoardRing(GameLayout layout) {
        if (comboPulse <= 0f || comboCount < 2) return;

        float progress = Math.min(comboPulse / COMBO_PULSE_DURATION, 1f);
        float wave     = (MathUtils.sin(comboPulse * 5f) + 1f) * 0.5f;
        float alpha    = progress * (0.60f + wave * 0.28f);
        float pad      = 4f;
        Color c        = getComboColor();

        shapeRenderer.setColor(c.r, c.g, c.b, alpha);
        shapeRenderer.rect(
            layout.boardX - pad,
            layout.boardY - pad,
            GameLayout.BOARD_WIDTH  + pad * 2f,
            GameLayout.BOARD_HEIGHT + pad * 2f
        );
    }

    // =========================================================
    // Board ambient glow
    // =========================================================

    private void drawBoardAmbientGlow(GameLayout layout, float time) {
        float pulse = (MathUtils.sin(time * 2.0f) + 1f) * 0.5f;

        shapeRenderer.setColor(0.15f, 0.85f, 1.00f, 0.03f + pulse * 0.018f);
        shapeRenderer.rect(
            layout.boardX - 30f,
            layout.boardY - 20f,
            GameLayout.BOARD_WIDTH  + 60f,
            GameLayout.BOARD_HEIGHT + 40f
        );

        shapeRenderer.setColor(0.10f, 0.60f, 1.00f, 0.025f + pulse * 0.012f);
        shapeRenderer.rect(
            layout.boardX - 14f,
            layout.boardY - 10f,
            GameLayout.BOARD_WIDTH  + 28f,
            GameLayout.BOARD_HEIGHT + 20f
        );
    }

    // =========================================================
    // HUD shells
    // =========================================================

    private void drawHudShells(GameLayout layout) {
        drawGlassShell(layout.topShellBounds);
        drawGlassShell(layout.bottomShellBounds);
    }

    private void drawHudShellOutlines(GameLayout layout) {
        drawGlassShellOutline(layout.topShellBounds);
        drawGlassShellOutline(layout.bottomShellBounds);
    }

    private void drawGlassShell(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_GLOW);
        shapeRenderer.rect(bounds.x - 5f, bounds.y - 5f,
            bounds.width + 10f, bounds.height + 10f);

        shapeRenderer.setColor(PANEL_GLASS);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(PANEL_GLASS_INNER);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f,
            bounds.width - 6f, bounds.height - 6f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(bounds.x + 6f, bounds.y + bounds.height - 8f,
            bounds.width - 12f, 2f);
    }

    private void drawGlassShellOutline(Rectangle bounds) {
        shapeRenderer.setColor(PANEL_OUTLINE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        shapeRenderer.setColor(0.18f, 0.85f, 1.00f, 0.14f);
        shapeRenderer.rect(bounds.x + 2f, bounds.y + 2f,
            bounds.width - 4f, bounds.height - 4f);
    }

    // =========================================================
    // Dispose
    // =========================================================

    public void dispose() {
        shapeRenderer.dispose();
    }
}
