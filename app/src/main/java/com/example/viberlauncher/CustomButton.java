package com.example.viberlauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.graphics.RectF;

import androidx.appcompat.widget.AppCompatTextView;

public class CustomButton extends AppCompatTextView {
    long touchTime;
    Paint paint;
    Drawable drawable;

    enum Shape {
        Circle,
        Rectangle,
    }
    Shape shape;
    RectF rc;

    public CustomButton(Context context) {
        super(context);
        touchTime = 0;
        paint = new Paint();
        drawable = null;
        shape = Shape.Rectangle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setARGB(255, 72, 191, 83);
        if (shape == Shape.Circle) {
            canvas.drawCircle((float) Math.abs(getLeft() - getRight()) / 2, (float) Math.abs(getTop() - getBottom()) / 2, (float) getWidth() / 2, paint);
        } else {
            float r = Math.min(rc.height(), rc.width())/2;
            canvas.drawRoundRect(rc, r, r, paint);
        }

        if (drawable != null) {
            drawable.setBounds((int)rc.left + getPaddingLeft(), (int)rc.top + getPaddingTop(), (int)rc.right - getPaddingRight(), (int)rc.bottom - getPaddingBottom());
            drawable.draw(canvas);
        }
        super.setTextColor(Color.WHITE);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(150,150);
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        Rect rc = new Rect();
        super.getDrawingRect(rc);
        this.rc = new RectF(rc);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPressure() > 1.0)
                    touchTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchTime = 0;
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - touchTime > 200 && isInBounds(event.getX(), event.getY())) {
                    performClick();
                    touchTime = 0;
                }
                break;
            }
        return true;
    }

    boolean isInBounds(float x, float y) {
        return x > 0.0 && x < getWidth() && y > 0.0 && y < getHeight();
    }
}
