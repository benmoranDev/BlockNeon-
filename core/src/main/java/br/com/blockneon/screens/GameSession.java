package br.com.blockneon.screens;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import br.com.blockneon.model.ActivePiece;
import br.com.blockneon.model.Board;
import br.com.blockneon.model.Tetromino;

import com.badlogic.gdx.utils.Array;

public class GameSession {

    // =========================================================
    // Gameplay tuning
    // =========================================================

    private static final float LOCK_DELAY          = 0.50f;
    public  static final float LINE_FLASH_DURATION = 0.18f;

    private static final int NEXT_QUEUE_SIZE = 5;
    private static final int LINES_PER_LEVEL = 10;

    // =========================================================
    // Combo tuning
    // =========================================================

    private static final float COMBO_WINDOW = 4.0f;

    // =========================================================
    // Core state
    // =========================================================

    private final Board board;
    private ActivePiece activePiece;

    private final Array<Tetromino> nextQueue = new Array<>();
    private final Array<Tetromino> bag       = new Array<>();

    private Tetromino heldTetromino    = null;
    private boolean   holdUsedThisTurn = false;

    private float lockTimer = 0f;

    private int score             = 0;
    private int linesClearedTotal = 0;
    private int level             = 1;

    // =========================================================
    // Pause state
    // =========================================================

    private boolean paused = false;

    // =========================================================
    // Drop — acumulador frame-rate independent
    // =========================================================

    private float dropAccumulator = 0f;
    private float dropInterval    = 1.0f;

    // =========================================================
    // FX state
    // =========================================================

    private final Array<Integer> flashRows = new Array<>();
    private float lineFlashTimer = 0f;

    // =========================================================
    // Audio flags
    // =========================================================

    private boolean pendingDropSound  = false;
    private boolean pendingClearSound = false;

    // =========================================================
    // Combo state
    // =========================================================

    private int   comboCount       = 0;
    private float comboWindowTimer = 0f;

    private boolean pendingComboNotify = false;
    private boolean pendingComboReset  = false;

    // =========================================================
    // Game over
    // =========================================================

    private boolean gameOver = false;

    // =========================================================
    // Constructor
    // =========================================================

    public GameSession(Board board) {
        this.board = board;
    }

    // =========================================================
    // Lifecycle
    // =========================================================

    public void resetRun() {
        board.clear();
        activePiece = null;
        gameOver    = false;
        paused      = false;

        nextQueue.clear();
        bag.clear();

        heldTetromino    = null;
        holdUsedThisTurn = false;

        lockTimer       = 0f;
        dropAccumulator = 0f;

        score             = 0;
        linesClearedTotal = 0;
        level             = 1;

        dropInterval = calcDropInterval(level);

        flashRows.clear();
        lineFlashTimer = 0f;

        pendingDropSound  = false;
        pendingClearSound = false;

        comboCount         = 0;
        comboWindowTimer   = 0f;
        pendingComboNotify = false;
        pendingComboReset  = false;

        ensureNextQueueFilled();
        spawnPiece();
    }

    // =========================================================
    // Update principal
    // =========================================================

    public void update(float delta) {
        if (gameOver || paused) return;

        delta = Math.min(delta, 1f / 30f);

        updateDrop(delta);
        updateLock(delta);
        updateLineFlash(delta);
        updateComboWindow(delta);
    }

    // =========================================================
    // Pause
    // =========================================================

    public void pauseGame() {
        if (gameOver) return;
        paused = true;
    }

    public void resumeGame() {
        if (gameOver) return;
        paused = false;

        // evita avanço instantâneo no retorno
        dropAccumulator = 0f;
        lockTimer = 0f;
    }

    public void togglePause() {
        if (gameOver) return;

        if (paused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    // =========================================================
    // Drop automático — gravidade frame-rate independent
    // =========================================================

    private void updateDrop(float delta) {
        if (activePiece == null) return;

        dropAccumulator += delta;

        while (dropAccumulator >= dropInterval) {
            dropAccumulator -= dropInterval;

            if (!tryStepDownInternal()) {
                break;
            } else {
                lockTimer = 0f;
            }
        }
    }

    // =========================================================
    // Lock delay
    // =========================================================

    private void updateLock(float delta) {
        if (activePiece == null) return;

        if (isGrounded()) {
            lockTimer += delta;
            if (lockTimer >= LOCK_DELAY) {
                pendingDropSound = true;
                lockAndSpawn();
            }
        } else {
            lockTimer = 0f;
        }
    }

    // =========================================================
    // Drop interval — Tetris Guideline
    // =========================================================

    private float calcDropInterval(int lvl) {
        return Math.max(0.05f,
            (float) Math.pow(0.8 - (lvl - 1) * 0.007, lvl - 1));
    }

    // =========================================================
    // Line flash timer
    // =========================================================

    private void updateLineFlash(float delta) {
        if (lineFlashTimer <= 0f) return;

        lineFlashTimer -= delta;
        if (lineFlashTimer <= 0f) {
            lineFlashTimer = 0f;
            flashRows.clear();
        }
    }

    // =========================================================
    // Combo window
    // =========================================================

    private void updateComboWindow(float delta) {
        if (comboWindowTimer <= 0f) return;

        comboWindowTimer -= delta;
        if (comboWindowTimer <= 0f) {
            comboWindowTimer  = 0f;
            comboCount        = 0;
            pendingComboReset = true;
        }
    }

    // =========================================================
    // Bag / Queue
    // =========================================================

    private void refillBag() {
        bag.clear();
        for (Tetromino t : Tetromino.values()) {
            bag.add(t);
        }
        bag.shuffle();
    }

    private Tetromino drawFromBag() {
        if (bag.size == 0) refillBag();
        return bag.pop();
    }

    private void ensureNextQueueFilled() {
        while (nextQueue.size < NEXT_QUEUE_SIZE) {
            nextQueue.add(drawFromBag());
        }
    }

    // =========================================================
    // Spawn
    // =========================================================

    private void spawnPiece() {
        ensureNextQueueFilled();

        Tetromino next = nextQueue.removeIndex(0);
        activePiece = new ActivePiece(next, Board.ROWS - 2, 3);

        ensureNextQueueFilled();

        dropAccumulator  = 0f;
        lockTimer        = 0f;
        holdUsedThisTurn = false;

        if (!board.canPlace(activePiece)) {
            activePiece = null;
            gameOver    = true;
            paused      = false;
        }
    }

    // =========================================================
    // Hold
    // =========================================================

    public boolean tryHoldPiece() {
        if (paused || activePiece == null || holdUsedThisTurn) return false;

        Tetromino current = activePiece.getTetromino();

        if (heldTetromino == null) {
            heldTetromino = current;
            spawnPiece();
            holdUsedThisTurn = true;
            return true;
        }

        Tetromino swap = heldTetromino;
        heldTetromino  = current;
        ActivePiece swapped = new ActivePiece(swap, Board.ROWS - 2, 3);

        if (board.canPlace(swapped)) {
            activePiece      = swapped;
            dropAccumulator  = 0f;
            lockTimer        = 0f;
            holdUsedThisTurn = true;
            return true;
        }

        heldTetromino = swap;
        return false;
    }

    // =========================================================
    // Movement
    // =========================================================

    public boolean tryMoveLeft() {
        if (paused || activePiece == null) return false;
        activePiece.moveLeft();
        if (!board.canPlace(activePiece)) {
            activePiece.moveRight();
            return false;
        }
        lockTimer = 0f;
        return true;
    }

    public boolean tryMoveRight() {
        if (paused || activePiece == null) return false;
        activePiece.moveRight();
        if (!board.canPlace(activePiece)) {
            activePiece.moveLeft();
            return false;
        }
        lockTimer = 0f;
        return true;
    }

    public boolean tryRotate() {
        if (paused || activePiece == null) return false;
        activePiece.rotateRight();
        if (!board.canPlace(activePiece)) {
            activePiece.rotateLeft();
            return false;
        }
        lockTimer = 0f;
        return true;
    }

    public void softDropOneStep() {
        if (paused || activePiece == null) return;

        if (tryStepDownInternal()) {
            score += 1;
            lockTimer = 0f;
        }
    }

    public void hardDrop() {
        if (paused || activePiece == null) return;

        int cells = 0;
        while (tryStepDownInternal()) {
            cells++;
        }

        score += cells * 2;
        pendingDropSound = true;
        lockAndSpawn();
    }

    private boolean tryStepDownInternal() {
        activePiece.moveDown();
        if (!board.canPlace(activePiece)) {
            activePiece.moveUp();
            return false;
        }
        return true;
    }

    private boolean isGrounded() {
        if (activePiece == null) return false;

        activePiece.moveDown();
        boolean can = board.canPlace(activePiece);
        activePiece.moveUp();
        return !can;
    }

    // =========================================================
    // Lock & spawn
    // =========================================================

    private void lockAndSpawn() {
        board.lockPiece(activePiece);

        Array<Integer> clearedRows = board.clearLinesWithInfo();
        int cleared = clearedRows.size;

        if (cleared > 0) {
            flashRows.clear();
            flashRows.addAll(clearedRows);
            lineFlashTimer = LINE_FLASH_DURATION;

            linesClearedTotal += cleared;
            score += cleared * 100 * cleared * level;

            comboCount++;
            comboWindowTimer   = COMBO_WINDOW;
            pendingComboNotify = true;
            pendingClearSound  = true;
        }

        refreshLevel();
        spawnPiece();
    }

    // =========================================================
    // Level & interval
    // =========================================================

    private void refreshLevel() {
        int newLevel = 1 + (linesClearedTotal / LINES_PER_LEVEL);
        if (newLevel != level) {
            level = newLevel;
            dropInterval = calcDropInterval(level);
            dropAccumulator = 0f;
        }
    }

    // =========================================================
    // Audio consumption
    // =========================================================

    public boolean consumeDropSound() {
        boolean v = pendingDropSound;
        pendingDropSound = false;
        return v;
    }

    public boolean consumeClearSound() {
        boolean v = pendingClearSound;
        pendingClearSound = false;
        return v;
    }

    // =========================================================
    // Getters
    // =========================================================

    public Board getBoard() {
        return board;
    }

    public ActivePiece getActivePiece() {
        return activePiece;
    }

    public Array<Tetromino> getNextQueue() {
        return nextQueue;
    }

    public Tetromino getHeldTetromino() {
        return heldTetromino;
    }

    public Array<Integer> getFlashRows() {
        return flashRows;
    }

    public float getLineFlashTimer() {
        return lineFlashTimer;
    }

    public int getScore() {
        return score;
    }

    public int getLinesClearedTotal() {
        return linesClearedTotal;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHoldUsedThisTurn() {
        return holdUsedThisTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getComboCount() {
        return comboCount;
    }

    public float getComboWindowTimer() {
        return comboWindowTimer;
    }

    public int getGoalLines() {
        int r = LINES_PER_LEVEL - (linesClearedTotal % LINES_PER_LEVEL);
        return r == 0 ? LINES_PER_LEVEL : r;
    }

    public boolean consumeComboNotify() {
        boolean v = pendingComboNotify;
        pendingComboNotify = false;
        return v;
    }

    public boolean consumeComboReset() {
        boolean v = pendingComboReset;
        pendingComboReset = false;
        return v;
    }
}
