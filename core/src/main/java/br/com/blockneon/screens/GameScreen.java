package br.com.blockneon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.viewport.ExtendViewport;


import br.com.blockneon.Main;
import br.com.blockneon.model.Board;

public class GameScreen implements Screen {

    // =========================================================
    // Setup / Configuração
    // =========================================================
    private final Game game;
    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final GameLayout layout;
    private final GameSession session;
    private final BackgroundRenderer backgroundRenderer;
    private final BoardRenderer boardRenderer;
    private final HudRenderer hudRenderer;

    // =========================================================
    // Audio / Áudio
    // =========================================================
    private Sound moveSound;
    private Sound dropSound;
    private Sound clearSound;
    private Music bgMusic;

    // =========================================================
    // Animation / Animação
    // =========================================================

    private float time = 0f;

    /**
     * Creates the screen and its dependencies.
     * Cria a tela e suas dependências.
     */
    public GameScreen(Game game) {
        this.game = game;

        camera   = new OrthographicCamera();
        viewport = new ExtendViewport(GameLayout.WORLD_WIDTH, GameLayout.WORLD_HEIGHT, camera);

        layout   = new GameLayout();
        session  = new GameSession(new Board());

        backgroundRenderer = new BackgroundRenderer();
        boardRenderer      = new BoardRenderer();
        hudRenderer        = new HudRenderer();
    }

    /**
     * Initializes the screen when shown.
     * Inicializa a tela quando exibida.
     */
    @Override
    public void show() {
        ((Main) game).adBridge.showBanner();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();
        layout.update(viewport.getWorldWidth(), viewport.getWorldHeight());

        loadAudio();
        session.resetRun();
        setupInput();
        ((Main) game).adBridge.showBanner(); // banner aparece quando o jogo começa
    }

    /**
     * Configures gesture input for mobile controls.
     * Configura o input por gestos para controles mobile.
     */
    private void setupInput() {
        Gdx.input.setInputProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
            private float accX = 0f;
            private float accY = 0f;

            @Override
            public boolean tap(float x, float y, int count, int button) {
                if (session.tryRotate()) playSound(moveSound, 0.55f);
                return true;
            }

            @Override
            public boolean longPress(float x, float y) {
                if (session.tryHoldPiece()) playSound(moveSound, 0.55f);
                return true;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                accX += deltaX;
                accY += deltaY;

                if (accX >= 60f) {
                    if (session.tryMoveRight()) playSound(moveSound, 0.45f);
                    accX = 0f;
                } else if (accX <= -60f) {
                    if (session.tryMoveLeft()) playSound(moveSound, 0.45f);
                    accX = 0f;
                }

                if (accY >= 80f) {
                    session.softDropOneStep();
                    accY = 0f;
                }

                return true;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                if (velocityY > 1500f && Math.abs(velocityY) > Math.abs(velocityX)) {
                    session.hardDrop();
                }
                return true;
            }
        }));
    }

    /**
     * Loads audio assets used by the screen.
     * Carrega os recursos de áudio usados pela tela.
     */
    private void loadAudio() {
        try {
            moveSound  = Gdx.audio.newSound(Gdx.files.internal("sounds/move.wav"));
            dropSound  = Gdx.audio.newSound(Gdx.files.internal("sounds/drop.wav"));
            clearSound = Gdx.audio.newSound(Gdx.files.internal("sounds/clear.wav"));
            bgMusic    = Gdx.audio.newMusic(Gdx.files.internal("sounds/bgm.ogg"));

            bgMusic.setLooping(true);
            bgMusic.setVolume(0.45f);
            bgMusic.play();
        } catch (Exception e) {
            Gdx.app.log("Audio", "Sound assets not found. Running without audio.");
        }
    }

    /**
     * Plays a sound effect if available.
     * Reproduz um efeito sonoro se disponível.
     */
    private void playSound(Sound sound, float volume) {
        if (sound != null) sound.play(volume);
    }

    /**
     * Main render loop.
     * Loop principal de renderização.
     */
    @Override
    public void render(float delta) {
        time += delta;

        handleKeyboardInput();
        session.update(delta);

        // ── Game Over ─────────────────────────────────────────
        if (session.isGameOver()) {
            if (bgMusic != null) bgMusic.stop();

            int finalScore = session.getScore();
            boolean newRecord = ((Main) game).scoreManager.submit(finalScore);
            int bestScore = ((Main) game).scoreManager.getBestScore();

            game.setScreen(new GameOverScreen(game, finalScore, bestScore, newRecord));
            dispose();
            return;
        }

        // ── Combo feedback ──────────────────────────────────
        if (session.consumeComboNotify()) {
            backgroundRenderer.notifyCombo(session.getComboCount());
        }
        if (session.consumeComboReset()) {
            backgroundRenderer.resetCombo();
        }

        // ── Audio flags ──────────────────────────────────────
        if (session.consumeDropSound())  playSound(dropSound,  0.60f);
        if (session.consumeClearSound()) playSound(clearSound, 1.00f);

        // ── Theme update ─────────────────────────────────────
        backgroundRenderer.updateTheme(session.getScore());

        // ── Draw ─────────────────────────────────────────────
        Gdx.gl.glClearColor(0.00f, 0.01f, 0.06f, 1f);// ── Black background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        backgroundRenderer.render(camera, layout, time, delta);
        boardRenderer.render(camera, layout, session, time);
        hudRenderer.render(camera, layout, session, delta);
    }

    /**
     * Handles desktop keyboard controls.
     * Processa os controles de teclado no desktop.
     */
    private void handleKeyboardInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (session.tryMoveLeft())  playSound(moveSound, 0.45f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (session.tryMoveRight()) playSound(moveSound, 0.45f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (session.tryRotate())    playSound(moveSound, 0.55f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            session.hardDrop();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            if (session.tryHoldPiece()) playSound(moveSound, 0.55f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            session.softDropOneStep();
        }
    }

    /**
     * Updates viewport and layout on resize.
     * Atualiza viewport e layout no resize.
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.update();
        layout.update(viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    @Override
    public void pause() {
        ((Main) game).adBridge.hideBanner();
        if (bgMusic != null) bgMusic.pause();
    }

    @Override
    public void resume() {
        ((Main) game).adBridge.showBanner();
        if (bgMusic != null) bgMusic.play();
    }

    @Override
    public void hide() {
        ((Main) game).adBridge.hideBanner();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        backgroundRenderer.dispose();
        boardRenderer.dispose();
        hudRenderer.dispose();

        if (moveSound  != null) moveSound.dispose();
        if (dropSound  != null) dropSound.dispose();
        if (clearSound != null) clearSound.dispose();
        if (bgMusic    != null) bgMusic.dispose();
    }
}
