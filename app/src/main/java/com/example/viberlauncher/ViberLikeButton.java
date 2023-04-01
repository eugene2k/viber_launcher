package com.example.viberlauncher;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.loader.ResourcesLoader;
import android.content.res.loader.ResourcesProvider;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;

import java.util.ResourceBundle;

public class ViberLikeButton extends View {
    long touchTime;
    Paint paint;

    public ViberLikeButton(Context context) {
        super(context);
        touchTime = 0;
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable d = ResourcesCompat.getDrawable(Resources.getSystem(),android.R.drawable.sym_action_call,null);
        d.draw(canvas);

        paint.setARGB(255, 72,191,83);
        canvas.drawCircle(75,75,65, paint);
        d.setBounds(20,20,130,130);
        d.setTint(Color.WHITE);
        d.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(150,150);
    }

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
        if (x > 0.0 && x < getWidth() && y > 0.0 && y < getHeight()) {
            return true;
        }
        return false;
    }
}
