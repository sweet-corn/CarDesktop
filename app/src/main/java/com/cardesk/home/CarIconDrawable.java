package com.cardesk.home;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/** Density-independent monochrome car outline, not a system emoji. */
public final class CarIconDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override public void draw(Canvas canvas) {
        int saved = canvas.save();
        canvas.translate(getBounds().left, getBounds().top);
        canvas.scale(getBounds().width() / 32f, getBounds().height() / 32f);
        paint.setColor(0xff273541); paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2); paint.setStrokeJoin(Paint.Join.ROUND); paint.setStrokeCap(Paint.Cap.ROUND);
        Path outline = new Path();
        outline.moveTo(5, 15); outline.lineTo(9, 6); outline.lineTo(23, 6); outline.lineTo(27, 15);
        outline.lineTo(28, 25); outline.lineTo(4, 25); outline.close();
        canvas.drawPath(outline, paint);
        canvas.drawLine(5, 15, 27, 15, paint);
        canvas.drawLine(8, 20, 11, 20, paint); canvas.drawLine(21, 20, 24, 20, paint);
        canvas.drawLine(7, 25, 7, 28, paint); canvas.drawLine(25, 25, 25, 28, paint);
        canvas.restoreToCount(saved);
    }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
