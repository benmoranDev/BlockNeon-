package br.com.blockneon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import br.com.blockneon.Main;
import br.com.blockneon.controllers.GameInputController;
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
    // Input
    // =========================================================
    private GameInputController inputController;
    private final Vector3 touchPoint = new Vector3();
    private boolean pauseTouchConsumed = false;

    // =========================================================
    // Audio / Áudio
    // =========================================================
    private Sound moveSound;
    private Sound dropSound;
    private Sound clearSound;
    private Music bgMusic;

    // Ajuste de Pause | Pause Adjustment
    // Isso evita um “tranco” logo no primeiro frame após sair do pause, especialmente em Android.
    private boolean skipNextUpdateAfterResume = false;

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
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();
        layout.update(viewport.getWorldWidth(), viewport.getWorldHeight());

        loadAudio();
        session.resetRun();
        setupInput();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        ((Main) game).adBridge.showBanner();
    }

    /**
     * Configures integrated gameplay + pause input.
     * Configura input integrado de gameplay + pause.
     */
    private void setupInput() {
        inputController = new GameInputController(session);

        InputAdapter uiAndKeyInput = new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (pointer != 0) return false;

                viewport.unproject(touchPoint.set(screenX, screenY, 0f));

                // =========================
                // Pause popup input
                // =========================
                if (session.isPaused()) {
                    Rectangle resumeBounds = hudRenderer.getPauseResumeButtonBounds(layout);
                    Rectangle panelBounds = hudRenderer.getPausePanelBounds(layout);

                    if (resumeBounds.contains(touchPoint.x, touchPoint.y)) {
                        pauseTouchConsumed = true;
                        togglePauseFromUI();
                        return true;
                    }

                    if (panelBounds.contains(touchPoint.x, touchPoint.y)) {
                        pauseTouchConsumed = true;
                        return true;
                    }

                    pauseTouchConsumed = true;
                    return true;
                }

                // =========================
                // HUD pause button
                // =========================
                if (layout.pauseButtonBounds.contains(touchPoint.x, touchPoint.y)) {
                    pauseTouchConsumed = true;
                    togglePauseFromUI();
                    return true;
                }

                pauseTouchConsumed = false;
                return inputController.touchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (pointer != 0) return false;

                if (pauseTouchConsumed || session.isPaused()) {
                    return true;
                }

                return inputController.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (pointer != 0) return false;

                if (pauseTouchConsumed) {
                    pauseTouchConsumed = false;
                    return true;
                }

                if (session.isPaused()) {
                    return true;
                }

                return inputController.touchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE ||
                    keycode == Input.Keys.P ||
                    keycode == Input.Keys.BACK) {
                    togglePauseFromUI();
                    return true;
                }
                return false;
            }
        };

        GestureDetector gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                if (pauseTouchConsumed || session.isPaused()) {
                    return true;
                }
                return inputController.onFling(velocityX, velocityY);
            }
        });

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiAndKeyInput);
        multiplexer.addProcessor(gestureDetector);

        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Toggle pause via UI or key.
     * Alterna pause via botão ou tecla.
     */
    private void togglePauseFromUI() {
        session.togglePause();

        if (session.isPaused()) {
            if (bgMusic != null) {
                bgMusic.pause();
            }
            ((Main) game).adBridge.hideBanner();
        } else {
            if (bgMusic != null) {
                bgMusic.play();
            }
            ((Main) game).adBridge.showBanner();
        }
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
        if (sound != null) {
            sound.play(volume);
        }
    }

    /**
     * Main render loop.
     * Loop principal de renderização.
     */
    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 30f);

        if (!session.isPaused()) {
            if (skipNextUpdateAfterResume) {
                skipNextUpdateAfterResume = false;
            } else {
                time += delta;
                handleKeyboardInput();
                session.update(delta);
            }
        }

        if (session.isGameOver()) {
            if (bgMusic != null) {
                bgMusic.stop();
            }

            int finalScore = session.getScore();
            boolean newRecord = ((Main) game).scoreManager.submit(finalScore);
            int bestScore = ((Main) game).scoreManager.getBestScore();

            game.setScreen(new GameOverScreen(game, finalScore, bestScore, newRecord));
            dispose();
            return;
        }

        if (!session.isPaused()) {
            if (session.consumeComboNotify()) {
                backgroundRenderer.notifyCombo(session.getComboCount());
            }
            if (session.consumeComboReset()) {
                backgroundRenderer.resetCombo();
            }

            if (session.consumeDropSound()) {
                playSound(dropSound, 0.60f);
            }
            if (session.consumeClearSound()) {
                playSound(clearSound, 1.00f);
            }

            backgroundRenderer.updateTheme(session.getScore());
        }

        Gdx.gl.glClearColor(0.00f, 0.01f, 0.06f, 1f);
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
        if (session.isPaused()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (session.tryMoveLeft()) {
                playSound(moveSound, 0.45f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (session.tryMoveRight()) {
                playSound(moveSound, 0.45f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (session.tryRotate()) {
                playSound(moveSound, 0.55f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            session.hardDrop();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            if (session.tryHoldPiece()) {
                playSound(moveSound, 0.55f);
            }
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
        session.pauseGame();
        ((Main) game).adBridge.hideBanner();

        if (bgMusic != null) {
            bgMusic.pause();
        }
    }

    @Override
    public void resume() {
        if (!session.isGameOver() && !session.isPaused()) {
            ((Main) game).adBridge.showBanner();
        }

        if (bgMusic != null && !session.isPaused()) {
            bgMusic.play();
        }
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
