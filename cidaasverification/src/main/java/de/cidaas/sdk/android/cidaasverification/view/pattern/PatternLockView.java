package de.cidaas.sdk.android.cidaasverification.view.pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

import de.cidaas.sdk.android.cidaasverification.R;

/**
 * Minimal 3×3 pattern lock: records an ordered list of dot indices (0–8) with Android-style “jumped over” dots inserted.
 */
public class PatternLockView extends View {

    public static final int MIN_PATTERN_LENGTH = 4;

    private final ArrayList<Integer> selected = new ArrayList<>();
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();

    private float cellSize;
    private float[][] centers = new float[9][2];
    private boolean drawing;

    public PatternLockView(Context context) {
        super(context);
        init(context);
    }

    public PatternLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PatternLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(ContextCompat.getColor(context, R.color.cidaasverification_pattern_dot));
        dotHighlightPaint.setStyle(Paint.Style.FILL);
        dotHighlightPaint.setColor(ContextCompat.getColor(context, R.color.cidaasverification_pattern_active));
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(ContextCompat.getColor(context, R.color.cidaasverification_pattern_inner));
        innerSelectedPaint.setStyle(Paint.Style.FILL);
        innerSelectedPaint.setColor(Color.WHITE);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(dp(3f));
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setColor(ContextCompat.getColor(context, R.color.cidaasverification_pattern_path));
    }

    /**
     * Applies dot and path colors from the host theme ({@code colorPrimary}, {@code colorOutline},
     * {@code colorSurface}, {@code colorOnPrimary}). Call after inflation with an {@code Activity} context
     * so Material theme attributes resolve correctly.
     */
    public void applyThemedColors(@NonNull Context themedContext) {
        int primary = MaterialColors.getColor(
                themedContext,
                com.google.android.material.R.attr.colorPrimary,
                ContextCompat.getColor(themedContext, R.color.cidaasverification_pattern_active));
        int onPrimary = MaterialColors.getColor(
                themedContext,
                com.google.android.material.R.attr.colorOnPrimary,
                Color.WHITE);
        int outline = MaterialColors.getColor(
                themedContext,
                com.google.android.material.R.attr.colorOutline,
                ContextCompat.getColor(themedContext, R.color.cidaasverification_pattern_dot));
        int surface = MaterialColors.getColor(
                themedContext,
                com.google.android.material.R.attr.colorSurface,
                ContextCompat.getColor(themedContext, R.color.cidaasverification_pattern_inner));

        dotPaint.setColor(outline);
        dotHighlightPaint.setColor(primary);
        pathPaint.setColor(primary);
        innerPaint.setColor(surface);
        innerSelectedPaint.setColor(onPrimary);
        invalidate();
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) {
            return;
        }
        int side = Math.min(w, h);
        cellSize = side / 4f;
        float marginX = (w - 3 * cellSize) / 2f;
        float marginY = (h - 3 * cellSize) / 2f;
        int idx = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                centers[idx][0] = marginX + col * cellSize + cellSize / 2f;
                centers[idx][1] = marginY + row * cellSize + cellSize / 2f;
                idx++;
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (cellSize <= 0f) {
            return;
        }
        float rBig = cellSize * 0.28f;
        float rInner = cellSize * 0.10f;
        for (int i = 0; i < 9; i++) {
            float cx = centers[i][0];
            float cy = centers[i][1];
            boolean on = selected.contains(i);
            canvas.drawCircle(cx, cy, rBig, on ? dotHighlightPaint : dotPaint);
            canvas.drawCircle(cx, cy, rInner, on ? innerSelectedPaint : innerPaint);
        }
        if (selected.size() > 1) {
            linePath.reset();
            int first = selected.get(0);
            linePath.moveTo(centers[first][0], centers[first][1]);
            for (int k = 1; k < selected.size(); k++) {
                int j = selected.get(k);
                linePath.lineTo(centers[j][0], centers[j][1]);
            }
            canvas.drawPath(linePath, pathPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                drawing = true;
                int hit0 = hitTest(event.getX(), event.getY());
                if (hit0 >= 0) {
                    selected.clear();
                    appendIndex(hit0);
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!drawing) {
                    return true;
                }
                int hit = hitTest(event.getX(), event.getY());
                if (hit >= 0) {
                    appendIndex(hit);
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drawing = false;
                invalidate();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private int hitTest(float x, float y) {
        if (cellSize <= 0f) {
            return -1;
        }
        float thr = cellSize * 0.35f;
        for (int i = 0; i < 9; i++) {
            float dx = x - centers[i][0];
            float dy = y - centers[i][1];
            if (dx * dx + dy * dy <= thr * thr) {
                return i;
            }
        }
        return -1;
    }

    private void appendIndex(int idx) {
        if (selected.isEmpty()) {
            selected.add(idx);
            return;
        }
        int last = selected.get(selected.size() - 1);
        if (last == idx) {
            return;
        }
        if (selected.contains(idx)) {
            return;
        }
        int mid = middleBetween(last, idx);
        if (mid >= 0 && !selected.contains(mid)) {
            selected.add(mid);
        }
        selected.add(idx);
    }

    /**
     * Indices of cells that are “between” {@code a} and {@code b} on a standard 3×3 lock (Android-style).
     */
    private static int middleBetween(int a, int b) {
        int[][] pairs = {
                {0, 2, 1}, {0, 6, 3}, {0, 8, 4},
                {1, 7, 4},
                {2, 8, 5}, {2, 6, 4},
                {3, 5, 4},
                {6, 8, 7},
        };
        for (int[] t : pairs) {
            if ((t[0] == a && t[1] == b) || (t[0] == b && t[1] == a)) {
                return t[2];
            }
        }
        return -1;
    }

    public void clearPattern() {
        selected.clear();
        invalidate();
    }

    @NonNull
    public List<Integer> getSelectedCells() {
        return new ArrayList<>(selected);
    }

    public boolean isPatternLongEnough() {
        return selected.size() >= MIN_PATTERN_LENGTH;
    }
}
