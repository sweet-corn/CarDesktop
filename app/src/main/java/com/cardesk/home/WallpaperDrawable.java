package com.cardesk.home;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Original geometric artwork, drawn at the current display size. */
public final class WallpaperDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override public void draw(Canvas canvas) {
        float w = getBounds().width(), h = getBounds().height();
        canvas.drawColor(0xffc2d0dc);
        sphere(canvas, w * .08f, h * .08f, w * .44f, 0xffdae5eb, 0xff91a8bd);
        sphere(canvas, w * .85f, h * .42f, w * .60f, 0xffe0eaf1, 0xffa4b9cd);
        sphere(canvas, -w * .06f, h * .73f, w * .24f, 0xfff1f4f5, 0xffc5d2de);
    }

    private void sphere(Canvas canvas, float x, float y, float radius, int light, int dark) {
        paint.setShader(new RadialGradient(x - radius * .35f, y - radius * .4f,
            radius * 1.5f, light, dark, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, paint);
        paint.setShader(null);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}
