package com.ff.overlay;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public class AimView extends View {
    private final StreamerMode streamer;
    public Paint boxPaint = new Paint();
    public boolean aimVisible = true;

    public AimView(Context c, StreamerMode s) {
        super(c);
        streamer = s;
        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3f);
    }

    @Override protected void onDraw(Canvas canvas) {
        if (streamer.isRecording() || !aimVisible) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            return;
        }
        super.onDraw(canvas);
    }

    public void requestRedraw() { postInvalidateOnAnimation(); }
}
