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

    private static final float WORLD_WIDTH   = 480f;
    private static final float WORLD_HEIGHT  = 800f;

    // =========================================================
    // Layout
    // =========================================================

    private static final float PANEL_WIDTH    = 320f;
    private static final float PANEL_HEIGHT   = 380f;
    private static final float BUTTON_WIDTH   = 240f;
    private static final float BUTTON_HEIGHT  = 54f;
    private static final float BUTTON_SPACING = 14f;

    // =========================================================
    // Core
    // =========================================================

    private final Game              game;
    private final int               finalScore;
    private final OrthographicCamera camera;
    private final ExtendViewport     viewport;
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

    private float   time         = 0f;
    private float   fadeAlpha    = 1f;
    private boolean fadeInDone   = false;
    private boolean fadingOut    = false;
    private boolean goToMenu     = false;
    private boolean goToGame     = false;

    private float   scoreDisplay = 0f;
    private boolean scoreCounted = false;

    // =========================================================
    // Layout bounds
    // =========================================================

    private final Rectangle panelBounds = new Rectangle();
    private final Rectangle retryBounds = new Rectangle();
    private final Rectangle menuBounds  = new Rectangle();
    private final Vector3   touchPoint  = new Vector3();

    // =========================================================
    // Partículas
    // =========================================================

    private static final int PARTICLE_COUNT = 60;
    private final float[] pX     = new float[PARTICLE_COUNT];
    private final float[] pY     = new float[PARTICLE_COUNT];
    private final float[] pVX    = new float[PARTICLE_COUNT];
    private final float[] pVY    = new float[PARTICLE_COUNT];
    private final float[] pLife  = new float[PARTICLE_COUNT];
    private final float[] pSize  = new float[PARTICLE_COUNT];
    private final Color[] pColor = new Color[PARTICLE_COUNT];
    private boolean particlesInit = false;

    // =========================================================
    // Background — blocos neon caindo
    // =========================================================

    private static final int    BLOCK_COUNT = 28;
    private final float[]   bX       = new float[BLOCK_COUNT];
    private final float[]   bY       = new float[BLOCK_COUNT];
    private final float[]   bSize    = new float[BLOCK_COUNT];
    private final float[]   bSpeed   = new float[BLOCK_COUNT];
    private final float[]   bAlpha   = new float[BLOCK_COUNT];
    private final boolean[] bOutline = new boolean[BLOCK_COUNT];
    private final Color[]   bColor   = new Color[BLOCK_COUNT];
    private boolean blocksInit = false;

    private static final Color[] BLOCK_PALETTE = {
        new Color(0f,    1f,    1f,    1f),
        new Color(1f,    0.25f, 1f,    1f),
        new Color(1f,    0.10f, 0.20f, 1f),
        new Color(0.35f, 1f,    0.50f, 1f),
        new Color(1f,    0.85f, 0f,    1f),
        new Color(0.55f, 0.55f, 1f,    1f),
    };

    // =========================================================
    // Dados do jogo / mood
    // =========================================================

    private final int     bestScore;
    private final boolean newRecord;
    private final float   mood;

    // =========================================================
    // Palette base
    // =========================================================

    private static final Color PANEL_GLASS       = new Color(0.04f, 0.12f, 0.22f, 0.72f);
    private static final Color PANEL_GLASS_INNER = new Color(0.10f, 0.26f, 0.40f, 0.14f);

    // =========================================================
    // Constructor
    // =========================================================

    public GameOverScreen(Game game, int finalScore, int bestScore, boolean newRecord) {
        this.game       = game;
        this.finalScore = finalScore;
        this.bestScore  = bestScore;
        this.newRecord  = newRecord;
        this.mood       = MathUtils.clamp(finalScore / 5000f, 0f, 1f);

        camera        = new OrthographicCamera();
        viewport      = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        FontManager fonts = ((Main) game).fontManager;
        titleFont = fonts.gameOverTitleFont;
        scoreFont = fonts.gameOverScoreFont;
        labelFont = fonts.gameOverLabelFont;
        hintFont  = fonts.gameOverHintFont;

        ((Main) game).adBridge.showBanner();
    }

    // =========================================================
    // Screen lifecycle
    // =========================================================

    @Override
    public void show() {
        ((Main) game).adBridge.showBanner();

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();
        syncViewportSize();
        updateLayout();
        initBlocks();

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
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) startFadeOut(true);
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK)  startFadeOut(false);
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
        float usableH = vh - ((Main) game).adBridge.getBannerHeightWorld();

        float panelX = (vw - PANEL_WIDTH) / 2f;
        float panelY = (usableH - PANEL_HEIGHT) / 2f + 10f;
        panelBounds.set(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        float btnX   = (vw - BUTTON_WIDTH) / 2f;
        float retryY = panelY + 70f;
        float menuY  = retryY - BUTTON_HEIGHT - BUTTON_SPACING;
        retryBounds.set(btnX, retryY, BUTTON_WIDTH, BUTTON_HEIGHT);
        menuBounds.set(btnX,  menuY,  BUTTON_WIDTH, BUTTON_HEIGHT);
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
        delta = Math.min(delta, 1f / 30f);
        time += delta;

        updateFade(delta);
        updateScoreCounter(delta);
        updateParticles(delta);
        updateBlocks(delta);

        Gdx.gl.glClearColor(0.00f, 0.01f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ── Passe 1: fundo + partículas + blocos (Filled) ────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawSpaceBackground();
        drawParticles();
        drawBlocksFilled();
        shapeRenderer.end();

        // ── Passe 1b: contornos dos blocos (Line) ────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawBlocksOutline();
        shapeRenderer.end();

        // ── Passe 2: painel + botões (Filled) ────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawPanel();
        drawButtons();
        shapeRenderer.end();

        // ── Passe 3: contornos painel/botões (Line) ───────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawPanelOutlines();
        drawButtonOutlines();
        shapeRenderer.end();

        // ── Textos neon ───────────────────────────────────────
        drawTexts();

        // ── Fade overlay ──────────────────────────────────────
        drawFadeOverlay();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (fadingOut && fadeAlpha >= 1f) {
            game.setScreen(goToGame ? new GameScreen(game) : new MainMenuScreen(game));
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
                initParticles();
            }
        } else if (fadingOut) {
            fadeAlpha += delta * 2.2f;
            if (fadeAlpha > 1f) fadeAlpha = 1f;
        }
    }

    private void updateScoreCounter(float delta) {
        if (scoreCounted) return;
        float speed = finalScore / 1.5f;
        scoreDisplay = Math.min(scoreDisplay + speed * delta, finalScore);
        if (scoreDisplay >= finalScore) { scoreDisplay = finalScore; scoreCounted = true; }
    }

    // =========================================================
    // Partículas
    // =========================================================

    private void initParticles() {
        if (particlesInit) return;
        float cx = vw * 0.5f;
        float cy = vh * 0.5f + 40f;
        Color[] palette = {
            new Color(0.00f, 0.90f, 1.00f, 1f),
            new Color(0.20f, 0.65f, 1.00f, 1f),
            new Color(0.00f, 1.00f, 0.75f, 1f),
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
            pVY[i]   -= 180f * delta;
            pVX[i]   *= (1f - delta * 0.8f);
        }
    }

    private void drawParticles() {
        if (!particlesInit) return;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            if (pLife[i] <= 0f) continue;
            float a = Math.min(pLife[i], 1f);
            Color c = pColor[i];
            shapeRenderer.setColor(c.r, c.g, c.b, a * 0.18f);
            float hs = pSize[i] * 2.5f;
            shapeRenderer.rect(pX[i] - hs * 0.5f, pY[i] - hs * 0.5f, hs, hs);
            shapeRenderer.setColor(c.r, c.g, c.b, a * 0.85f);
            float s = pSize[i];
            shapeRenderer.rect(pX[i] - s * 0.5f, pY[i] - s * 0.5f, s, s);
        }
    }

    // =========================================================
    // Blocos neon caindo
    // =========================================================

    private void initBlocks() {
        if (blocksInit) return;
        for (int i = 0; i < BLOCK_COUNT; i++) resetBlock(i, true);
        blocksInit = true;
    }

    private void resetBlock(int i, boolean scatter) {
        bX[i]       = MathUtils.random(16f, vw - 16f);
        bY[i]       = scatter
            ? MathUtils.random(0f, vh)
            : MathUtils.random(vh + 20f, vh + 180f);
        bSize[i]    = MathUtils.random(8f, 20f);
        bSpeed[i]   = MathUtils.random(40f, 150f);
        bOutline[i] = MathUtils.randomBoolean(0.45f);
        bAlpha[i]   = bOutline[i]
            ? MathUtils.random(0.25f, 0.50f)
            : MathUtils.random(0.10f, 0.25f);
        bColor[i]   = BLOCK_PALETTE[MathUtils.random(0, BLOCK_PALETTE.length - 1)];
    }

    private void updateBlocks(float delta) {
        if (!blocksInit) return;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            bY[i] -= bSpeed[i] * delta;
            if (bY[i] < -30f) resetBlock(i, false);
        }
    }

    private void drawBlocksFilled() {
        if (!blocksInit) return;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            if (bOutline[i]) continue;
            Color c = bColor[i];
            float s = bSize[i];
            shapeRenderer.setColor(c.r, c.g, c.b, bAlpha[i] * 0.20f);
            shapeRenderer.rect(bX[i] - 2f, bY[i] - 2f, s + 4f, s + 4f);
            shapeRenderer.setColor(c.r, c.g, c.b, bAlpha[i] * 0.60f);
            shapeRenderer.rect(bX[i], bY[i], s, s);
        }
    }

    private void drawBlocksOutline() {
        if (!blocksInit) return;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            if (!bOutline[i]) continue;
            Color c = bColor[i];
            shapeRenderer.setColor(c.r, c.g, c.b, bAlpha[i]);
            shapeRenderer.rect(bX[i], bY[i], bSize[i], bSize[i]);
        }
    }

    // =========================================================
    // Draw — fundo espacial
    // =========================================================

    private void drawSpaceBackground() {
        int bands = 16;
        for (int i = 0; i < bands; i++) {
            float t     = (float) i / (bands - 1);
            float curve = (float) Math.pow(1f - t, 2f);
            float bandH = vh / bands + 1f;
            float bandY = i * (vh / bands);
            shapeRenderer.setColor(0.01f, 0.03f + curve * 0.10f, 0.05f + curve * 0.22f, 1f);
            shapeRenderer.rect(0, bandY, vw, bandH);
        }
        float pulse = (MathUtils.sin(time * 1.8f) + 1f) * 0.5f;
        float hR    = vw * 0.70f;
        shapeRenderer.setColor(0.04f, 0.18f, 0.55f, 0.06f + pulse * 0.03f);
        shapeRenderer.ellipse(vw / 2f - hR / 2f, vh / 2f - hR / 2f, hR, hR);
    }

    // =========================================================
    // Draw — painel com brilho de luz intenso
    // =========================================================

    private void drawPanel() {
        float pulse      = (MathUtils.sin(time * 2.4f) + 1f) * 0.5f;
        float pulseSlow  = (MathUtils.sin(time * 0.9f) + 1f) * 0.5f;
        float pulseFast  = (MathUtils.sin(time * 4.2f) + 1f) * 0.5f;

        float cx = panelBounds.x + panelBounds.width  * 0.5f;
        float cy = panelBounds.y + panelBounds.height * 0.5f;

        // ── Camada 1: halo muito largo — "luz ambiente" ao redor do painel ──
        // Simula a luz que vaza para fora, iluminando o fundo
        int haloLayers = 10;
        for (int i = haloLayers; i >= 1; i--) {
            float t       = (float) i / haloLayers;
            float expand  = i * 14f;                          // cada layer expande 14px
            float alpha   = (0.055f - t * 0.048f)            // decai de centro p/ fora
                * (0.75f + pulseSlow * 0.25f);      // respira devagar

            // cor varia entre ciano e azul conforme o tempo
            float hue = 0.52f + MathUtils.sin(time * 0.4f + t) * 0.08f;
            float rC  = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC  = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC  = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);

            shapeRenderer.setColor(rC, gC, bC, alpha);
            shapeRenderer.rect(
                panelBounds.x - expand,
                panelBounds.y - expand,
                panelBounds.width  + expand * 2f,
                panelBounds.height + expand * 2f);
        }

        // ── Camada 2: corona brilhante logo na borda externa ──
        // 4 retângulos concêntricos de alta intensidade
        float[] coronaExpand = { 8f, 5f, 3f, 1.5f };
        float[] coronaAlpha  = { 0.06f, 0.10f, 0.16f, 0.22f };
        for (int i = 0; i < 4; i++) {
            float ex = coronaExpand[i];
            float ca = coronaAlpha[i] * (0.8f + pulse * 0.2f);
            shapeRenderer.setColor(0.20f, 0.85f, 1.00f, ca);
            shapeRenderer.rect(
                panelBounds.x - ex, panelBounds.y - ex,
                panelBounds.width  + ex * 2f,
                panelBounds.height + ex * 2f);
        }

        // ── Corpo de vidro ────────────────────────────────────
        shapeRenderer.setColor(PANEL_GLASS);
        shapeRenderer.rect(panelBounds.x, panelBounds.y,
            panelBounds.width, panelBounds.height);

        // ── Camada interna (profundidade de vidro) ────────────
        shapeRenderer.setColor(PANEL_GLASS_INNER);
        shapeRenderer.rect(panelBounds.x + 4f, panelBounds.y + 4f,
            panelBounds.width - 8f, panelBounds.height - 8f);

        // ── Reflexo de luz no topo do vidro (flare horizontal) ──
        // Simula um ponto de luz incidindo no canto superior
        float flareW = panelBounds.width * (0.55f + pulseFast * 0.15f);
        float flareX = cx - flareW * 0.5f;
        float flareY = panelBounds.y + panelBounds.height - 6f;

        // camada mais suave e larga
        shapeRenderer.setColor(1f, 1f, 1f, 0.04f + pulseFast * 0.03f);
        shapeRenderer.rect(flareX - 20f, flareY - 1f, flareW + 40f, 8f);
        // camada média
        shapeRenderer.setColor(0.75f, 0.95f, 1f, 0.08f + pulseFast * 0.04f);
        shapeRenderer.rect(flareX, flareY, flareW, 4f);
        // linha brilhante fina no topo
        shapeRenderer.setColor(1f, 1f, 1f, 0.22f + pulseFast * 0.10f);
        shapeRenderer.rect(flareX + 16f, flareY + 2f, flareW - 32f, 1.5f);

        // ── Vidragem interna: reflexo diagonal suave ──────────
        // Canto superior-esquerdo → diagonal para baixo-direita
        float refW = panelBounds.width * 0.38f;
        float refH = panelBounds.height * 0.55f;
        shapeRenderer.setColor(1f, 1f, 1f, 0.025f + pulseSlow * 0.012f);
        shapeRenderer.triangle(
            panelBounds.x + 6f,        panelBounds.y + panelBounds.height - 6f,
            panelBounds.x + 6f + refW, panelBounds.y + panelBounds.height - 6f,
            panelBounds.x + 6f,        panelBounds.y + panelBounds.height - 6f - refH);
    }

    // =========================================================
    // Draw — contornos do painel com brilho intenso
    // =========================================================

    private void drawPanelOutlines() {
        float pulse = (MathUtils.sin(time * 2.4f) + 1f) * 0.5f;
        float r     = 12f;
        int   segs  = 6;

        // ── Glow externo em camadas — mais passes = mais brilho ──
        for (int pass = 0; pass < 4; pass++) {
            float expand = (pass + 1) * 4.5f;
            float phase  = time * 0.5f + pass * MathUtils.PI * 0.5f;
            float raw    = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue    = 0.50f + raw * 0.18f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);

            // alpha mais alto nos passes internos (mais perto da borda)
            float a = (0.12f - pass * 0.025f) * (0.75f + pulse * 0.25f);
            shapeRenderer.setColor(rC, gC, bC, Math.max(a, 0f));
            roundedRect(
                panelBounds.x - expand, panelBounds.y - expand,
                panelBounds.width  + expand * 2f,
                panelBounds.height + expand * 2f,
                r + expand, segs);
        }

        // ── Borda principal arco-íris — linha mais brilhante ──
        drawRainbowBorderEdges(panelBounds, r, segs, 0.70f + pulse * 0.28f);

        // ── Segunda linha interna branca — reforça o brilho ───
        shapeRenderer.setColor(1f, 1f, 1f, 0.18f + pulse * 0.10f);
        roundedRect(panelBounds.x + 1.5f, panelBounds.y + 1.5f,
            panelBounds.width - 3f, panelBounds.height - 3f,
            r - 1.5f, segs);
    }

    // =========================================================
    // Draw — botões
    // =========================================================

    private void drawButtons() {
        float pulse = (MathUtils.sin(time * 4.5f) + 1f) * 0.5f;
        drawButton(retryBounds, 0.04f, 0.16f, 0.24f, 0.20f, 0.85f, 1.00f, pulse);
        drawButton(menuBounds,  0.04f, 0.10f, 0.20f, 0.15f, 0.60f, 1.00f, pulse);
    }

    private void drawButton(Rectangle b,
                            float br, float bg, float bb,
                            float lr, float lg, float lb,
                            float pulse) {
        // Glow externo
        shapeRenderer.setColor(lr, lg, lb, 0.04f + pulse * 0.04f);
        shapeRenderer.rect(b.x - 5f, b.y - 5f, b.width + 10f, b.height + 10f);

        // Corpo
        shapeRenderer.setColor(br, bg, bb, 0.88f);
        shapeRenderer.rect(b.x, b.y, b.width, b.height);

        // Tint interior
        shapeRenderer.setColor(lr, lg, lb, 0.06f);
        shapeRenderer.rect(b.x + 4f, b.y + 4f, b.width - 8f, b.height - 8f);

        // Reflexo de luz no topo do botão
        float fw = b.width * 0.5f;
        shapeRenderer.setColor(1f, 1f, 1f, 0.07f + pulse * 0.04f);
        shapeRenderer.rect(b.x + b.width * 0.25f, b.y + b.height - 5f, fw, 4f);
        shapeRenderer.setColor(1f, 1f, 1f, 0.14f + pulse * 0.06f);
        shapeRenderer.rect(b.x + b.width * 0.25f + 6f, b.y + b.height - 4f, fw - 12f, 1.5f);
    }

    private void drawButtonOutlines() {
        float pulse = (MathUtils.sin(time * 4.5f) + 1f) * 0.5f;
        // Borda glow extra nos botões
        shapeRenderer.setColor(0.20f, 0.85f, 1.00f, 0.12f + pulse * 0.08f);
        roundedRect(retryBounds.x - 2f, retryBounds.y - 2f,
            retryBounds.width + 4f, retryBounds.height + 4f, 8f, 5);
        shapeRenderer.setColor(0.15f, 0.60f, 1.00f, 0.10f + pulse * 0.08f);
        roundedRect(menuBounds.x - 2f, menuBounds.y - 2f,
            menuBounds.width + 4f, menuBounds.height + 4f, 8f, 5);

        drawRainbowBorderEdges(retryBounds, 6f, 5, 0.55f + pulse * 0.25f);
        drawRainbowBorderEdges(menuBounds,  6f, 5, 0.48f + pulse * 0.22f);
    }

    // =========================================================
    // Helpers — cantos arredondados + borda arco-íris
    // =========================================================

    private void roundedRect(float x, float y, float w, float h, float r, int segs) {
        float step = 90f / segs;
        for (int i = 0; i < segs; i++) {
            float a1 = i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + w - r + MathUtils.cosDeg(a1) * r, y + h - r + MathUtils.sinDeg(a1) * r,
                x + w - r + MathUtils.cosDeg(a2) * r, y + h - r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        for (int i = 0; i < segs; i++) {
            float a1 = 90f + i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + r + MathUtils.cosDeg(a1) * r, y + h - r + MathUtils.sinDeg(a1) * r,
                x + r + MathUtils.cosDeg(a2) * r, y + h - r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        for (int i = 0; i < segs; i++) {
            float a1 = 180f + i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + r + MathUtils.cosDeg(a1) * r, y + r + MathUtils.sinDeg(a1) * r,
                x + r + MathUtils.cosDeg(a2) * r, y + r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        for (int i = 0; i < segs; i++) {
            float a1 = 270f + i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + w - r + MathUtils.cosDeg(a1) * r, y + r + MathUtils.sinDeg(a1) * r,
                x + w - r + MathUtils.cosDeg(a2) * r, y + r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        shapeRenderer.rectLine(x + r, y + h, x + w - r, y + h, 1.2f);
        shapeRenderer.rectLine(x + r, y,     x + w - r, y,     1.2f);
        shapeRenderer.rectLine(x,     y + r, x,         y + h - r, 1.2f);
        shapeRenderer.rectLine(x + w, y + r, x + w,     y + h - r, 1.2f);
    }

    private void drawRainbowBorderEdges(Rectangle rect, float r, int segs, float alpha) {
        float x = rect.x, y = rect.y, w = rect.width, h = rect.height;
        float basePhase = time * 0.55f;
        float lineW     = 1.2f;

        for (int edge = 0; edge < 4; edge++) {
            float phase = basePhase + edge * (MathUtils.PI2 / 4f);
            float raw   = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue   = 0.38f + raw * 0.30f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            shapeRenderer.setColor(rC, gC, bC, alpha);
            switch (edge) {
                case 0: shapeRenderer.rectLine(x + r, y + h, x + w - r, y + h, lineW); break;
                case 1: shapeRenderer.rectLine(x + w, y + r, x + w, y + h - r, lineW); break;
                case 2: shapeRenderer.rectLine(x + r, y, x + w - r, y, lineW);         break;
                case 3: shapeRenderer.rectLine(x, y + r, x, y + h - r, lineW);         break;
            }
        }

        float[] cornerAngles = { 0f, 90f, 180f, 270f };
        float[] cornerX      = { x + w - r, x + r,     x + r,     x + w - r };
        float[] cornerY      = { y + h - r, y + h - r, y + r,     y + r     };
        float step = 90f / segs;

        for (int c = 0; c < 4; c++) {
            float phase = basePhase + c * (MathUtils.PI2 / 4f);
            float raw   = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue   = 0.38f + raw * 0.30f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            shapeRenderer.setColor(rC, gC, bC, alpha);
            for (int i = 0; i < segs; i++) {
                float a1 = cornerAngles[c] + i * step;
                float a2 = a1 + step;
                shapeRenderer.rectLine(
                    cornerX[c] + MathUtils.cosDeg(a1) * r,
                    cornerY[c] + MathUtils.sinDeg(a1) * r,
                    cornerX[c] + MathUtils.cosDeg(a2) * r,
                    cornerY[c] + MathUtils.sinDeg(a2) * r, lineW);
            }
        }
    }

    // =========================================================
    // Draw — textos neon 2.5D
    // =========================================================

    private void drawTexts() {
        float pulse      = (MathUtils.sin(time * 3.2f) + 1f) * 0.5f;
        float pulseB     = (MathUtils.sin(time * 3.2f + MathUtils.PI) + 1f) * 0.5f;
        float scorePulse = (MathUtils.sin(time * 2.0f) + 1f) * 0.5f;

        batch.begin();

        float titleY = panelBounds.y + panelBounds.height - 24f;
        neon(titleFont, "GAME",
            0f, titleY, vw,
            new Color(1f, 0.30f, 0.40f, 1f),
            new Color(1f, 0.82f, 0.88f, 1f),
            0.75f + pulse * 0.22f);

        neon(titleFont, "OVER",
            0f, titleY - 46f, vw,
            new Color(1f, 0.05f, 0.22f, 1f),
            new Color(1f, 0.60f, 0.72f, 1f),
            0.75f + pulseB * 0.22f);

        float scoreLabelY = panelBounds.y + panelBounds.height - 132f;
        neon(labelFont, "SCORE",
            0f, scoreLabelY, vw,
            new Color(0f,    0.90f, 1f,    1f),
            new Color(0.75f, 0.96f, 1f,    1f),
            0.50f + scorePulse * 0.22f);

        float  scoreY   = scoreLabelY - 52f;
        String scoreStr = String.valueOf((int) scoreDisplay);

        Color scoreGlow = scoreCounted
            ? new Color(0f,    0.95f, 1f,    1f)
            : new Color(1f,    0.80f, 0f,    1f);
        Color scoreMain = scoreCounted
            ? new Color(0.88f, 1f,    1f,    1f)
            : new Color(1f,    0.96f, 0.55f, 1f);

        neon(scoreFont, scoreStr,
            0f, scoreY, vw,
            scoreGlow, scoreMain,
            0.82f + pulse * 0.18f);

        float infoY = scoreY - 42f;
        if (newRecord) {
            float rp = (MathUtils.sin(time * 5.5f) + 1f) * 0.5f;
            neon(labelFont, "  NEW RECORD  ",
                0f, infoY, vw,
                new Color(1f, 0.85f, 0.05f, 1f),
                new Color(1f, 1f,    0.62f, 1f),
                0.62f + rp * 0.36f);
        } else {
            neon(hintFont, "BEST  " + bestScore,
                0f, infoY, vw,
                new Color(0.45f, 0.75f, 1f, 1f),
                new Color(0.80f, 0.95f, 1f, 1f),
                0.45f + scorePulse * 0.18f);
        }

        neon(labelFont, "PLAY AGAIN",
            retryBounds.x,
            retryBounds.y + retryBounds.height / 2f + 8f,
            retryBounds.width,
            new Color(0f,    0.95f, 1f, 1f),
            new Color(0.85f, 1f,    1f, 1f),
            0.72f + pulse * 0.24f);

        neon(labelFont, "MAIN MENU",
            menuBounds.x,
            menuBounds.y + menuBounds.height / 2f + 8f,
            menuBounds.width,
            new Color(0.15f, 0.55f, 1f, 1f),
            new Color(0.75f, 0.92f, 1f, 1f),
            0.68f + pulseB * 0.24f);

        float hintPulse = (MathUtils.sin(time * 1.4f) + 1f) * 0.5f;
        neon(hintFont, "Tap to play again",
            0f, menuBounds.y - 26f, vw,
            new Color(0.40f, 0.65f, 1f, 1f),
            new Color(0.78f, 0.92f, 1f, 1f),
            0.22f + hintPulse * 0.20f);

        batch.end();
    }

    // =========================================================
    // Helper — neon 2.5D
    // =========================================================

    private void neon(BitmapFont font, String text,
                      float x, float y, float width,
                      Color glowColor, Color mainColor, float alpha) {

        float gr = glowColor.r, gg = glowColor.g, gb = glowColor.b;
        float mr = mainColor.r, mg = mainColor.g, mb = mainColor.b;

        int   extrudeSteps  = 6;
        float extrudeOffset = 1.2f;

        for (int s = extrudeSteps; s >= 1; s--) {
            float ox  = s * extrudeOffset;
            float oy  = -s * extrudeOffset;
            float t   = (float) s / extrudeSteps;
            float dim = MathUtils.lerp(0.55f, 0.08f, t);
            float aEx = alpha * MathUtils.lerp(0.70f, 0.25f, t);
            font.setColor(mr * dim, mg * dim, mb * dim, aEx);
            font.draw(batch, text, x + ox, y + oy, width, Align.center, false);
        }

        font.setColor(gr, gg, gb, alpha * 0.10f);
        font.draw(batch, text, x,      y + 6f, width, Align.center, false);
        font.draw(batch, text, x - 6f, y,      width, Align.center, false);
        font.draw(batch, text, x + 6f, y,      width, Align.center, false);
        font.draw(batch, text, x,      y - 6f, width, Align.center, false);

        font.setColor(gr, gg, gb, alpha * 0.28f);
        font.draw(batch, text, x - 2f, y,      width, Align.center, false);
        font.draw(batch, text, x + 2f, y,      width, Align.center, false);
        font.draw(batch, text, x,      y + 2f, width, Align.center, false);
        font.draw(batch, text, x,      y - 2f, width, Align.center, false);

        font.setColor(gr, gg, gb, alpha * 0.55f);
        font.draw(batch, text, x, y, width, Align.center, false);

        font.setColor(mr, mg, mb, alpha);
        font.draw(batch, text, x, y, width, Align.center, false);
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
