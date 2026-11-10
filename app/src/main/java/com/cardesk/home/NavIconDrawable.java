package com.cardesk.home;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/** Original outline icons for the configurable dock. */
public final class NavIconDrawable extends Drawable {
    private final String name;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public NavIconDrawable(String name) { this.name = name; }
    @Override public void draw(Canvas canvas) {
        int save = canvas.save(); canvas.translate(getBounds().left, getBounds().top);
        canvas.scale(getBounds().width() / 32f, getBounds().height() / 32f);
        paint.setColor(0xff273541); paint.setStrokeWidth(1.8f); paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND); paint.setStrokeCap(Paint.Cap.ROUND);
        Path path = new Path();
        switch (name) {
            case "返回":
                path.moveTo(11, 5); path.lineTo(4, 12); path.lineTo(11, 19);
                path.moveTo(4, 12); path.lineTo(23, 12); path.quadTo(28, 12, 28, 17);
                path.lineTo(28, 23); path.quadTo(28, 27, 23, 27); path.lineTo(14, 27); break;
            case "主页":
                path.moveTo(3, 14); path.lineTo(16, 3); path.lineTo(29, 14);
                path.moveTo(7, 11); path.lineTo(7, 28); path.lineTo(25, 28); path.lineTo(25, 11); break;
            case "应用":
                for (int x = 5; x <= 19; x += 14) for (int y = 5; y <= 19; y += 14)
                    canvas.drawRoundRect(x, y, x + 9, y + 9, 2, 2, paint);
                break;
            case "导航":
                path.moveTo(28, 4); path.lineTo(5, 14); path.lineTo(15, 17); path.lineTo(18, 28); path.close(); break;
            case "音乐":
                path.moveTo(13, 24); path.lineTo(13, 8); path.lineTo(27, 5); path.lineTo(27, 21);
                canvas.drawOval(5, 21, 13, 27, paint); canvas.drawOval(19, 18, 27, 24, paint); break;
            default:
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI * i / 3;
                    float x = 16 + (float) Math.cos(angle) * 13;
                    float y = 16 + (float) Math.sin(angle) * 13;
                    if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                }
                path.close(); canvas.drawCircle(16, 16, 5, paint);
        }
        canvas.drawPath(path, paint); canvas.restoreToCount(save);
    }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
