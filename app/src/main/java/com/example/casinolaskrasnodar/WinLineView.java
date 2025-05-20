package com.example.casinolaskrasnodar;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.animation.ArgbEvaluator;
import android.view.View;



import java.util.ArrayList;
import java.util.List;

// WinLineView.java
public class WinLineView extends View {
    private Paint linePaint;
    private List<int[][]> winningLines = new ArrayList<>();
    private int cellWidth, cellHeight;
    private int rows = 3, cols = 5;

    public WinLineView(Context context) {
        super(context);
        init();
    }

    public WinLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.YELLOW);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setPathEffect(new DashPathEffect(new float[]{20,20}, 0));
    }

    public void setCellSize(int width, int height) {
        this.cellWidth = width;
        this.cellHeight = height;
    }

    public void setWinningLines(List<int[][]> lines) {
        this.winningLines = lines;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int[][] line : winningLines) {
            Path path = new Path();
            boolean first = true;
            for (int[] pos : line) {
                float x = (pos[1] + 0.5f) * cellWidth;
                float y = (pos[0] + 0.5f) * cellHeight;
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, linePaint);
        }
    }

    public void startBlinkAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            linePaint.setAlpha((int) (255 * alpha));
            invalidate();
        });
        animator.start();
    }

    public void startSuperAnimation() {
        ValueAnimator colorAnim = ObjectAnimator.ofInt(
                this, "backgroundColor",
                Color.TRANSPARENT, Color.YELLOW, Color.TRANSPARENT
        );
        colorAnim.setDuration(1500);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(3);
        colorAnim.start();
    }


}