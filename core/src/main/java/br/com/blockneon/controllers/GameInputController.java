package br.com.blockneon.controllers;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.TimeUtils;
import br.com.blockneon.screens.GameSession;


public class GameInputController extends InputAdapter {

    private static final float TOUCH_SLOP = 14f;
    private static final float HORIZONTAL_STEP_PX = 22f;
    private static final float SOFT_DROP_TRIGGER_PX = 22f;
    private static final float HARD_DROP_DRAG_PX = 150f;
    private static final float HARD_DROP_FLING_VELOCITY = 900f;
    private static final float VERTICAL_DOMINANCE_RATIO = 1.20f;
    private static final float HORIZONTAL_DOMINANCE_RATIO = 1.10f;
    private static final long TAP_MAX_MS = 180L;

    private final GameSession session;

    private boolean touching = false;
    private float startX;
    private float startY;
    private float lastX;
    private float lastY;
    private long touchDownMs;

    private float horizontalAccumulator = 0f;
    private float verticalAccumulator = 0f;

    private DragIntent dragIntent = DragIntent.NONE;
    private boolean movedEnoughForGesture = false;
    private boolean hardDropTriggered = false;

    public GameInputController(GameSession session) {
        this.session = session;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) return false;

        if (session.isPaused()) {
            resetGestureState();
            return false;
        }

        touching = true;
        startX = screenX;
        startY = screenY;
        lastX = screenX;
        lastY = screenY;
        touchDownMs = TimeUtils.millis();

        horizontalAccumulator = 0f;
        verticalAccumulator = 0f;
        dragIntent = DragIntent.NONE;
        movedEnoughForGesture = false;
        hardDropTriggered = false;

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!touching || pointer != 0) return false;

        if (session.isPaused()) {
            resetGestureState();
            return false;
        }

        float totalDx = screenX - startX;
        float totalDy = screenY - startY;

        float absTotalDx = Math.abs(totalDx);
        float absTotalDy = Math.abs(totalDy);
        float dist2 = totalDx * totalDx + totalDy * totalDy;

        float dx = screenX - lastX;
        float dy = screenY - lastY;

        lastX = screenX;
        lastY = screenY;

        if (!movedEnoughForGesture) {
            if (dist2 < TOUCH_SLOP * TOUCH_SLOP) {
                return true;
            }
            movedEnoughForGesture = true;
        }

        if (dragIntent == DragIntent.NONE) {
            if (absTotalDx >= TOUCH_SLOP && absTotalDx > absTotalDy * HORIZONTAL_DOMINANCE_RATIO) {
                dragIntent = DragIntent.HORIZONTAL;
            } else if (absTotalDy >= TOUCH_SLOP && absTotalDy > absTotalDx * VERTICAL_DOMINANCE_RATIO) {
                dragIntent = DragIntent.SOFT_DROP;
            } else {
                return true;
            }
        }

        if (dragIntent == DragIntent.HORIZONTAL) {
            horizontalAccumulator += dx;

            while (horizontalAccumulator <= -HORIZONTAL_STEP_PX) {
                session.tryMoveLeft();
                horizontalAccumulator += HORIZONTAL_STEP_PX;
            }

            while (horizontalAccumulator >= HORIZONTAL_STEP_PX) {
                session.tryMoveRight();
                horizontalAccumulator -= HORIZONTAL_STEP_PX;
            }

            return true;
        }

        if (dragIntent == DragIntent.SOFT_DROP) {
            // No Android, arrastar para baixo normalmente aumenta Y em screen coords.
            verticalAccumulator += dy;

            if (!hardDropTriggered && totalDy >= HARD_DROP_DRAG_PX) {
                session.hardDrop();
                hardDropTriggered = true;
                resetGestureState();
                return true;
            }

            while (verticalAccumulator >= SOFT_DROP_TRIGGER_PX) {
                session.softDropOneStep();
                verticalAccumulator -= SOFT_DROP_TRIGGER_PX;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) return false;

        if (session.isPaused()) {
            resetGestureState();
            return false;
        }

        if (!hardDropTriggered) {
            long elapsed = TimeUtils.timeSinceMillis(touchDownMs);
            float totalDx = screenX - startX;
            float totalDy = screenY - startY;
            float absDx = Math.abs(totalDx);
            float absDy = Math.abs(totalDy);

            if (elapsed <= TAP_MAX_MS && absDx < TOUCH_SLOP && absDy < TOUCH_SLOP) {
                session.tryRotate();
            }
        }

        resetGestureState();
        return true;
    }

    public boolean onFling(float velocityX, float velocityY) {
        if (session.isPaused()) {
            resetGestureState();
            return false;
        }

        float absVx = Math.abs(velocityX);
        float absVy = Math.abs(velocityY);

        if (absVy >= HARD_DROP_FLING_VELOCITY && absVy > absVx * 1.15f && velocityY > 0f) {
            session.hardDrop();
            resetGestureState();
            return true;
        }

        return false;
    }

    private void resetGestureState() {
        touching = false;
        dragIntent = DragIntent.NONE;
        horizontalAccumulator = 0f;
        verticalAccumulator = 0f;
        movedEnoughForGesture = false;
        hardDropTriggered = false;
    }
}
