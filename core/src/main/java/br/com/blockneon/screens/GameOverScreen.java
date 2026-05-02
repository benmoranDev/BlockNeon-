package br.com.blockneon.screens;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import br.com.blockneon.Main;
import br.com.blockneon.ui.FontManager;


public class GameOverScreen implements Screen {

    // =========================================================
    // Viewport
    // =========================================================

    private static final float WORLD_WIDTH  = 480f;
    private static final float WORLD_HEIGHT = 800f;

    // =========================================================
    // Layout
    // =========================================================

    private static final float PANEL_WIDTH   = 320f;
    private static final float PANEL_HEIGHT  = 380f;
    private static final float BUTTON_WIDTH  = 240f;
    private static final float BUTTON_HEIGHT = 54f;
    private static final float BUTTON_SPACING = 14f;

    // =========================================================
    // Core
    // =========================================================

    private final Game         game;
    private final int          finalScore;
    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shapeRenderer;

    private float vw = WORLD_WIDTH;
    private float vh = WORLD_HEIGHT;

    // =========================================================
    // Fonts
    // =========================================================

    private final BitmapFont titleFont;
    private final BitmapFont scoreFont;
    private final BitmapFont labelFont;
    private final BitmapFont hintFont;

    // =========================================================
    // State
    // =========================================================

    private float   time           = 0f;
    private float   fadeAlpha      = 1f;
    private boolean fadeInDone     = false;
    private boolean fadingOut      = false;
    private boolean goToMenu       = false;
    private boolean goToGame       = false;

    // Animação do score — conta de 0 até finalScore
    private float   scoreDisplay   = 0f;
    private boolean scoreCounted   = false;

    // =========================================================
    // Layout bounds
    // =========================================================

    private final Rectangle panelBounds      = new Rectangle();
    private final Rectangle retryBounds      = new Rectangle();
    private final Rectangle menuBounds       = new Rectangle();
    private final Vector3 touchPoint       = new Vector3();

    // =========================================================
    // Background — estrelas
    // =========================================================

    private static final int STAR_COUNT = 130;
    private final float[] starX     = new float[STAR_COUNT];
    private final float[] starY     = new float[STAR_COUNT];
    private final float[] starSize  = new float[STAR_COUNT];
    private final float[] starAlpha = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private boolean starsInit = false;

    // Partículas de explosão ao entrar na tela
    private static final int  PARTICLE_COUNT = 60;
    private final float[] pX     = new float[PARTICLE_COUNT];
    private final float[] pY     = new float[PARTICLE_COUNT];
    private final float[] pVX    = new float[PARTICLE_COUNT];
    private final float[] pVY    = new float[PARTICLE_COUNT];
    private final float[] pLife  = new float[PARTICLE_COUNT];
    private final float[] pSize  = new float[PARTICLE_COUNT];
    private final Color[] pColor = new Color[PARTICLE_COUNT];
    private boolean particlesInit = false;

    private final int     bestScore;
    private final boolean newRecord;

    // =========================================================
    // Palette — igual BackgroundRenderer / MainMenuScreen
    // =========================================================

    private static final Color PANEL_GLASS       = new Color(0.04f, 0.12f, 0.22f, 0.72f);
    private static final Color PANEL_GLASS_INNER = new Color(0.10f, 0.26f, 0.40f, 0.14f);
    private static final Color PANEL_OUTLINE     = new Color(0.30f, 0.90f, 1.00f, 0.65f);
    private static final Color PANEL_GLOW        = new Color(0.20f, 0.80f, 1.00f, 0.07f);

    // =========================================================
    // Constructor
    // =========================================================

    public GameOverScreen(Game game, int finalScore, int bestScore, boolean newRecord) {
        this.game       = game;
        this.finalScore = finalScore;
        this.bestScore  = bestScore;
        this.newRecord  = newRecord;

        camera        = new OrthographicCamera();
        viewport      = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // ── Fontes via FontManager ────────────────────────────
        FontManager fonts = ((Main) game).fontManager;
        titleFont = fonts.gameOverTitleFont;
        scoreFont = fonts.gameOverScoreFont;
        labelFont = fonts.gameOverLabelFont;
        hintFont  = fonts.gameOverHintFont;
        // ─────────────────────────────────────────────────────
    }

    // =========================================================
    // Screen lifecycle
    // =========================================================

    @Override
    public void show() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();
        syncViewportSize();
        updateLayout();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (!fadeInDone || fadingOut) return false;
                viewport.unproject(touchPoint.set(screenX, screenY, 0f));
                handleTouch(touchPoint.x, touchPoint.y);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (!fadeInDone || fadingOut) return false;
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    startFadeOut(true);
                }
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    startFadeOut(false);
                }
                return true;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.update();
        syncViewportSize();
        updateLayout();
    }

    private void syncViewportSize() {
        vw = viewport.getWorldWidth();
        vh = viewport.getWorldHeight();
    }

    // =========================================================
    // Layout
    // =========================================================

    private void updateLayout() {
        float panelX = (vw - PANEL_WIDTH)  / 2f;
        float panelY = (vh - PANEL_HEIGHT) / 2f + 10f;
        panelBounds.set(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        float btnX    = (vw - BUTTON_WIDTH) / 2f;
        float retryY  = panelY + 70f;
        float menuY   = retryY - BUTTON_HEIGHT - BUTTON_SPACING;
        retryBounds.set(btnX, retryY, BUTTON_WIDTH, BUTTON_HEIGHT);
        menuBounds.set(btnX, menuY,   BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    // =========================================================
    // Input
    // =========================================================

    private void handleTouch(float x, float y) {
        if (retryBounds.contains(x, y)) { startFadeOut(true);  return; }
        if (menuBounds.contains(x, y))  { startFadeOut(false); }
    }

    private void startFadeOut(boolean retry) {
        if (fadingOut) return;
        fadingOut = true;
        goToGame  = retry;
        goToMenu  = !retry;
    }

    // =========================================================
    // Render
    // =========================================================

    @Override
    public void render(float delta) {
        time += delta;

        updateFade(delta);
        updateScoreCounter(delta);
        updateParticles(delta);

        Gdx.gl.glClearColor(0.00f, 0.01f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ── Passe 1: fundo ────────────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawSpaceBackground();
        drawStars();
        drawMountains();
        drawDotGrid();
        drawParticles();
        shapeRenderer.end();

        // ── Passe 2: painel + botões (Filled) ─────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawPanel();
        drawButtons();
        shapeRenderer.end();

        // ── Passe 3: contornos (Line) ─────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawPanelOutlines();
        drawButtonOutlines();
        shapeRenderer.end();

        // ── Textos ────────────────────────────────────────────
        drawTexts();

        // ── Fade overlay ──────────────────────────────────────
        drawFadeOverlay();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Transição ─────────────────────────────────────────
        if (fadingOut && fadeAlpha >= 1f) {
            if (goToGame) {
                game.setScreen(new GameScreen(game));
            } else {
                game.setScreen(new MainMenuScreen(game));
            }
            dispose();
        }
    }

    // =========================================================
    // Update
    // =========================================================

    private void updateFade(float delta) {
        if (!fadeInDone) {
            fadeAlpha -= delta * 1.6f;
            if (fadeAlpha <= 0f) {
                fadeAlpha  = 0f;
                fadeInDone = true;
                initParticles(); // dispara partículas ao entrar
            }
        } else if (fadingOut) {
            fadeAlpha += delta * 2.2f;
            if (fadeAlpha > 1f) fadeAlpha = 1f;
        }
    }

    /**
     * Animates score counter from 0 to finalScore over ~1.5s.
     * Anima o contador do score de 0 até finalScore em ~1.5s.
     */
    private void updateScoreCounter(float delta) {
        if (scoreCounted) return;
        float speed = finalScore / 1.5f;
        scoreDisplay = Math.min(scoreDisplay + speed * delta, finalScore);
        if (scoreDisplay >= finalScore) {
            scoreDisplay = finalScore;
            scoreCounted = true;
        }
    }

    // =========================================================
    // Particles — explosão ao entrar
    // =========================================================

    private void initParticles() {
        if (particlesInit) return;

        float cx = vw * 0.5f;
        float cy = vh * 0.5f + 40f;

        Color[] palette = {
            new Color(0.00f, 0.90f, 1.00f, 1f),
            new Color(0.65f, 0.25f, 1.00f, 1f),
            new Color(1.00f, 0.10f, 0.85f, 1f),
            new Color(0.20f, 1.00f, 0.55f, 1f),
            new Color(1.00f, 0.88f, 0.20f, 1f),
        };

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(60f, 320f);
            pX[i]     = cx;
            pY[i]     = cy;
            pVX[i]    = MathUtils.cos(angle) * speed;
            pVY[i]    = MathUtils.sin(angle) * speed;
            pLife[i]  = MathUtils.random(0.6f, 1.8f);
            pSize[i]  = MathUtils.random(2f, 6f);
            pColor[i] = palette[i % palette.length];
        }
        particlesInit = true;
    }

    private void updateParticles(float delta) {
        if (!particlesInit) return;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            if (pLife[i] <= 0f) continue;
            pLife[i] -= delta;
            pX[i]    += pVX[i] * delta;
            pY[i]    += pVY[i] * delta;
            pVY[i]   -= 180f * delta; // gravidade
            pVX[i]   *= (1f - delta * 0.8f); // arrasto
        }
    }

    private void drawParticles() {
        if (!particlesInit) return;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            if (pLife[i] <= 0f) continue;
            float a = Math.min(pLife[i], 1f);
            Color c = pColor[i];

            // Halo
            shapeRenderer.setColor(c.r, c.g, c.b, a * 0.18f);
            float hs = pSize[i] * 2.5f;
            shapeRenderer.rect(pX[i] - hs * 0.5f, pY[i] - hs * 0.5f, hs, hs);

            // Núcleo
            shapeRenderer.setColor(c.r, c.g, c.b, a * 0.85f);
            float s = pSize[i];
            shapeRenderer.rect(pX[i] - s * 0.5f, pY[i] - s * 0.5f, s, s);
        }
    }

    // =========================================================
    // Draw — fundo (igual MainMenuScreen)
    // =========================================================

    private void drawSpaceBackground() {
        shapeRenderer.setColor(0.00f, 0.01f, 0.06f, 1f);
        shapeRenderer.rect(0f, 0f, vw, vh);

        shapeRenderer.setColor(0.01f, 0.04f, 0.14f, 0.75f);
        shapeRenderer.rect(0f, 0f, vw, vh * 0.60f);

        shapeRenderer.setColor(0.03f, 0.08f, 0.20f, 0.45f);
        shapeRenderer.rect(0f, 0f, vw, vh * 0.32f);

        float nb = (MathUtils.sin(time * 0.5f) + 1f) * 0.5f;

        // Nebulosa vermelha-laranja — tom de derrota
        for (int i = 6; i >= 1; i--) {
            float r = 110f * i;
            shapeRenderer.setColor(0.60f, 0.08f, 0.05f,
                (0.022f + nb * 0.006f) / i);
            shapeRenderer.ellipse(vw * 0.15f - r * 0.5f, vh * 0.65f - r * 0.5f, r, r);
        }

        // Nebulosa magenta
        for (int i = 6; i >= 1; i--) {
            float r = 90f * i;
            shapeRenderer.setColor(0.55f, 0.05f, 0.50f,
                (0.018f + nb * 0.005f) / i);
            shapeRenderer.ellipse(vw * 0.80f - r * 0.5f, vh * 0.30f - r * 0.5f, r, r);
        }
    }

    private void drawStars() {
        if (!starsInit) {
            for (int i = 0; i < STAR_COUNT; i++) {
                starX[i]     = MathUtils.random(0f, vw);
                starY[i]     = MathUtils.random(vh * 0.10f, vh);
                starSize[i]  = MathUtils.random(0.7f, 2.5f);
                starAlpha[i] = MathUtils.random(0.25f, 1.0f);
                starPhase[i] = MathUtils.random(0f, MathUtils.PI2);
            }
            starsInit = true;
        }

        for (int i = 0; i < STAR_COUNT; i++) {
            float flicker = (MathUtils.sin(time * 1.8f + starPhase[i]) + 1f) * 0.5f;
            float a  = starAlpha[i] * (0.55f + flicker * 0.45f);
            float sz = starSize[i];

            if (sz > 1.8f) {
                shapeRenderer.setColor(0.70f, 0.88f, 1f, a * 0.22f);
                shapeRenderer.rect(starX[i] - sz, starY[i] - sz, sz * 3f, sz * 3f);
            }
            shapeRenderer.setColor(0.88f, 0.94f, 1f, a);
            shapeRenderer.rect(starX[i], starY[i], sz, sz);
        }
    }

    private void drawMountains() {
        drawMountainLayer(0.16f, 0.10f, 0.06f, 0.02f, 0.04f, 42);
        drawMountainLayer(0.12f, 0.06f, 0.03f, 0.01f, 0.02f, 28);
    }

    private void drawMountainLayer(float heightRatio, float baseRatio,
                                   float r, float g, float b, int seed) {
        float baseY = vh * baseRatio;
        float maxH  = vh * heightRatio;
        int   peaks = 9;
        float stepW = vw / (peaks - 1);

        for (int i = 0; i < peaks - 1; i++) {
            float x1 = i * stepW;
            float x2 = (i + 1) * stepW;
            float xm = (x1 + x2) * 0.5f;
            float hm = maxH * (0.60f + 0.40f * ((MathUtils.sin(seed + i * 2.3f + 0.8f) + 1f) * 0.5f));
            float h1 = maxH * (0.35f + 0.65f * ((MathUtils.sin(seed + i * 1.7f) + 1f) * 0.5f));
            float h2 = maxH * (0.35f + 0.65f * ((MathUtils.sin(seed + (i + 1) * 1.7f) + 1f) * 0.5f));

            shapeRenderer.setColor(r, g, b, 0.92f);
            shapeRenderer.triangle(x1, baseY, xm, baseY + hm, x1, baseY + h1);
            shapeRenderer.triangle(x2, baseY, xm, baseY + hm, x2, baseY + h2);
            shapeRenderer.rect(x1, 0f, stepW, baseY);
        }
    }

    private void drawDotGrid() {
        float dotSize = 1.4f;
        float cellW   = 22f;
        float cellH   = 22f;
        float pulse   = (MathUtils.sin(time * 1.6f) + 1f) * 0.5f;

        for (float x = cellW * 0.5f; x < vw; x += cellW) {
            for (float y = cellH * 0.5f; y < vh; y += cellH) {
                float dx   = Math.abs(x - vw * 0.5f) / (vw * 0.5f);
                float dy   = Math.abs(y - vh * 0.5f) / (vh * 0.5f);
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / 1.414f;
                float a    = (0.07f + pulse * 0.04f) * (1f - dist * 0.65f);

                shapeRenderer.setColor(0.40f, 0.85f, 1f, a);
                shapeRenderer.rect(x - dotSize * 0.5f, y - dotSize * 0.5f, dotSize, dotSize);
            }
        }
    }

    // =========================================================
    // Draw — painel de vidro
    // =========================================================

    private void drawPanel() {
        // Halo externo pulsante vermelho — sinaliza derrota
        float pulse = (MathUtils.sin(time * 2.4f) + 1f) * 0.5f;
        shapeRenderer.setColor(0.90f, 0.10f, 0.15f, 0.06f + pulse * 0.04f);
        shapeRenderer.rect(
            panelBounds.x - 12f, panelBounds.y - 12f,
            panelBounds.width + 24f, panelBounds.height + 24f);

        // Halo ciano suave
        shapeRenderer.setColor(PANEL_GLOW);
        shapeRenderer.rect(
            panelBounds.x - 6f, panelBounds.y - 6f,
            panelBounds.width + 12f, panelBounds.height + 12f);

        // Corpo de vidro
        shapeRenderer.setColor(PANEL_GLASS);
        shapeRenderer.rect(panelBounds.x, panelBounds.y,
            panelBounds.width, panelBounds.height);

        // Camada interna
        shapeRenderer.setColor(PANEL_GLASS_INNER);
        shapeRenderer.rect(
            panelBounds.x + 4f, panelBounds.y + 4f,
            panelBounds.width - 8f, panelBounds.height - 8f);

        // Reflexo de vidro no topo
        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(
            panelBounds.x + 10f,
            panelBounds.y + panelBounds.height - 10f,
            panelBounds.width - 20f, 2f);

        // Divisória abaixo do título
        shapeRenderer.setColor(0.90f, 0.20f, 0.25f, 0.18f);
        shapeRenderer.rect(
            panelBounds.x + 20f,
            panelBounds.y + panelBounds.height - 80f,
            panelBounds.width - 40f, 1.5f);

        // Divisória acima dos botões
        shapeRenderer.setColor(0.18f, 0.85f, 1.00f, 0.12f);
        shapeRenderer.rect(
            panelBounds.x + 20f,
            retryBounds.y + BUTTON_HEIGHT + 18f,
            panelBounds.width - 40f, 1.5f);
    }

    private void drawPanelOutlines() {
        float pulse = (MathUtils.sin(time * 2.4f) + 1f) * 0.5f;

        // Borda vermelha pulsante — acento de derrota
        shapeRenderer.setColor(0.90f, 0.15f, 0.20f, 0.30f + pulse * 0.20f);
        shapeRenderer.rect(panelBounds.x, panelBounds.y,
            panelBounds.width, panelBounds.height);

        // Borda ciano interna suave
        shapeRenderer.setColor(PANEL_OUTLINE.r, PANEL_OUTLINE.g, PANEL_OUTLINE.b, 0.20f);
        shapeRenderer.rect(
            panelBounds.x + 4f, panelBounds.y + 4f,
            panelBounds.width - 8f, panelBounds.height - 8f);
    }

    // =========================================================
    // Draw — botões
    // =========================================================

    private void drawButtons() {
        float pulse = (MathUtils.sin(time * 4.5f) + 1f) * 0.5f;

        // ── Retry — ciano ──
        shapeRenderer.setColor(0.20f, 0.85f, 1.00f, 0.05f + pulse * 0.06f);
        shapeRenderer.rect(retryBounds.x - 5f, retryBounds.y - 5f,
            retryBounds.width + 10f, retryBounds.height + 10f);

        shapeRenderer.setColor(0.04f, 0.16f, 0.24f, 0.88f);
        shapeRenderer.rect(retryBounds.x, retryBounds.y,
            retryBounds.width, retryBounds.height);

        shapeRenderer.setColor(0.18f, 0.95f, 1f, 0.08f);
        shapeRenderer.rect(retryBounds.x + 4f, retryBounds.y + 4f,
            retryBounds.width - 8f, retryBounds.height - 8f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(retryBounds.x + 10f,
            retryBounds.y + retryBounds.height - 8f,
            retryBounds.width - 20f, 2f);

        // ── Menu — roxo ──
        shapeRenderer.setColor(0.60f, 0.10f, 0.80f, 0.04f + pulse * 0.05f);
        shapeRenderer.rect(menuBounds.x - 5f, menuBounds.y - 5f,
            menuBounds.width + 10f, menuBounds.height + 10f);

        shapeRenderer.setColor(0.10f, 0.06f, 0.18f, 0.88f);
        shapeRenderer.rect(menuBounds.x, menuBounds.y,
            menuBounds.width, menuBounds.height);

        shapeRenderer.setColor(0.70f, 0.25f, 1f, 0.06f);
        shapeRenderer.rect(menuBounds.x + 4f, menuBounds.y + 4f,
            menuBounds.width - 8f, menuBounds.height - 8f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.04f);
        shapeRenderer.rect(menuBounds.x + 10f,
            menuBounds.y + menuBounds.height - 8f,
            menuBounds.width - 20f, 2f);
    }

    private void drawButtonOutlines() {
        shapeRenderer.setColor(PANEL_OUTLINE.r, PANEL_OUTLINE.g, PANEL_OUTLINE.b, 0.55f);
        shapeRenderer.rect(retryBounds.x, retryBounds.y,
            retryBounds.width, retryBounds.height);

        shapeRenderer.setColor(0.65f, 0.25f, 1.00f, 0.40f);
        shapeRenderer.rect(menuBounds.x, menuBounds.y,
            menuBounds.width, menuBounds.height);
    }

    // =========================================================
    // Draw — textos
    // =========================================================

    private void drawTexts() {
        float pulse = (MathUtils.sin(time * 4.5f) + 1f) * 0.5f;

        batch.begin();

        // ── Título "GAME OVER" ──
        float titleY = panelBounds.y + panelBounds.height - 22f;

        titleFont.setColor(0.90f, 0.10f, 0.15f, 0.22f);
        titleFont.draw(batch, "GAME OVER", 0f, titleY + 5f, vw, Align.center, false);

        titleFont.setColor(1.00f, 0.88f, 0.88f, 1f);
        titleFont.draw(batch, "GAME OVER", 0f, titleY, vw, Align.center, false);

        // ── Label "SCORE" ──
        float scoreLabelY = panelBounds.y + panelBounds.height - 108f;
        labelFont.setColor(0.70f, 0.88f, 1f, 0.65f);
        labelFont.draw(batch, "SCORE", 0f, scoreLabelY, vw, Align.center, false);

        // ── Valor do score animado ──
        float scoreY = scoreLabelY - 50f;
        String scoreStr = String.valueOf((int) scoreDisplay);

        scoreFont.setColor(0.25f, 1f, 1f, 0.18f);
        scoreFont.draw(batch, scoreStr, 0f, scoreY + 4f, vw, Align.center, false);

        scoreFont.setColor(scoreCounted
            ? new Color(0.82f, 1f, 1f, 1f)
            : new Color(1f, 1f, 0.70f, 1f));
        scoreFont.draw(batch, scoreStr, 0f, scoreY, vw, Align.center, false);

        // ── NEW RECORD / BEST SCORE ──────────────── << ADICIONAR AQUI
        if (newRecord) {
            float recordPulse = (MathUtils.sin(time * 6f) + 1f) * 0.5f;
            labelFont.setColor(1f, 0.90f + recordPulse * 0.10f, 0.20f, 0.80f + recordPulse * 0.20f);
            labelFont.draw(batch, "\u2605  NEW RECORD  \u2605",
                0f, scoreY - 36f, vw, Align.center, false);
        } else {
            hintFont.setColor(0.60f, 0.80f, 1f, 0.55f);
            hintFont.draw(batch, "BEST  " + bestScore,
                0f, scoreY - 36f, vw, Align.center, false);
        }
        // ─────────────────────────────────────────────────────────

        // ── Labels dos botões ──
        labelFont.setColor(0.82f, 1f, 1f, 1f);
        labelFont.draw(batch, "PLAY AGAIN",
            retryBounds.x,
            retryBounds.y + retryBounds.height / 2f + 7f,
            retryBounds.width, Align.center, false);

        labelFont.setColor(1f, 0.82f, 1f, 1f);
        labelFont.draw(batch, "MAIN MENU",
            menuBounds.x,
            menuBounds.y + menuBounds.height / 2f + 7f,
            menuBounds.width, Align.center, false);

        // ── Dica de teclado ──
        float hintY = menuBounds.y - 28f;
        hintFont.setColor(1f, 1f, 1f, 0.28f + pulse * 0.32f);
        hintFont.draw(batch, "ENTER = Play Again  \u2022  ESC = Menu",
            0f, hintY, vw, Align.center, false);

        batch.end();
    }
    // =========================================================
    // Fade overlay
    // =========================================================

    private void drawFadeOverlay() {
        if (fadeAlpha <= 0f) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);
        shapeRenderer.rect(0f, 0f, vw, vh);
        shapeRenderer.end();
    }

    // =========================================================
    // Lifecycle
    // =========================================================

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
