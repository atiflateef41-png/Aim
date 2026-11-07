package com.ff.overlay;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public class AimView extends View {
    public Paint boxPaint = new Paint();
    public Paint fovPaint = new Paint();

    public AimView(Context c) {
        super(c);
        boxPaint.setColor(Color.RED); boxPaint.setStyle(Paint.Style.STROKE); boxPaint.setStrokeWidth(4f);
        fovPaint.setColor(Color.WHITE); fovPaint.setStyle(Paint.Style.STROKE); fovPaint.setStrokeWidth(2f); fovPaint.setAlpha(100);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(); int h = getHeight();
        canvas.drawCircle(w / 2f, h / 2f, 150f, fovPaint); // FOV Circle
        // Screen Capture process boxes yahan draw karega (future step)
    }

    public void requestRedraw() { postInvalidateOnAnimation(); }
}
