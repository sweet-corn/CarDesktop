package com.cardesk.home;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Light, compact single-choice picker shared by map and music. */
public final class ChoicePanel {
    public static final class Item {
        public final String name, subtitle;
        public final Drawable icon;
        public final boolean selected;
        public final Runnable action;
        public Item(String name, String subtitle, Drawable icon, boolean selected, Runnable action) {
            this.name = name; this.subtitle = subtitle; this.icon = icon; this.selected = selected; this.action = action;
        }
    }
    public static void show(Activity activity, String title, String hint, List<Item> items) {
        float scale = Math.min(activity.getResources().getDisplayMetrics().widthPixels / 1200f,
            activity.getResources().getDisplayMetrics().heightPixels / 675f);
        android.content.Context context = new android.view.ContextThemeWrapper(activity, android.R.style.Theme_Material_Light_NoActionBar);
        Dialog dialog = new Dialog(context); dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel = new LinearLayout(context); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(px(18, scale), px(14, scale), px(18, scale), px(14, scale)); panel.setBackground(shape(0xfff8f9fa, px(18, scale)));
        LinearLayout header = new LinearLayout(context); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = text(context, title, 19, scale, 0xff273541); header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        TextView close = text(context, "关闭 ✕", 13, scale, 0xff526472); close.setPadding(px(12, scale), px(8, scale), px(12, scale), px(8, scale));
        close.setOnClickListener(v -> dialog.dismiss()); header.addView(close); panel.addView(header);
        TextView description = text(context, hint, 11, scale, 0xff78838d); description.setPadding(0, px(4, scale), 0, px(8, scale)); panel.addView(description);
        ScrollView scroll = new ScrollView(context); LinearLayout list = new LinearLayout(context); list.setOrientation(LinearLayout.VERTICAL);
        if (items.isEmpty()) {
            TextView empty = text(context, "没有找到可选应用", 14, scale, 0xff78838d); empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, px(70, scale)));
        }
        for (Item item : items) {
            LinearLayout row = new LinearLayout(context); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(px(12, scale), px(5, scale), px(12, scale), px(5, scale));
            row.setBackground(shape(item.selected ? 0xffe0ebfc : 0xffedf0f2, px(10, scale)));
            if (item.icon != null) {
                ImageView icon = new ImageView(context); icon.setImageDrawable(item.icon); icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                row.addView(icon, new LinearLayout.LayoutParams(px(34, scale), px(34, scale)));
            } else {
                TextView icon = text(context, "♫", 22, scale, 0xff087bff); icon.setGravity(Gravity.CENTER);
                row.addView(icon, new LinearLayout.LayoutParams(px(34, scale), px(34, scale)));
            }
            LinearLayout labels = new LinearLayout(context); labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(px(12, scale), 0, px(4, scale), 0);
            TextView name = text(context, item.name, 14, scale, 0xff273541); name.setSingleLine(true); name.setEllipsize(android.text.TextUtils.TruncateAt.END); labels.addView(name);
            if (item.subtitle != null && !item.subtitle.isEmpty()) labels.addView(text(context, item.subtitle, 10, scale, 0xff78838d));
            row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
            TextView check = text(context, item.selected ? "✓" : "", 18, scale, 0xff087bff); row.addView(check);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, px(52, scale)); params.setMargins(0, px(3, scale), 0, px(3, scale)); list.addView(row, params);
            row.setOnClickListener(v -> { dialog.dismiss(); item.action.run(); });
        }
        scroll.addView(list); panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        dialog.setContentView(panel); dialog.show(); Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int width = Math.min(px(510, scale), (int) (activity.getResources().getDisplayMetrics().widthPixels * .90f));
            int height = Math.min(px(130 + Math.max(1, items.size()) * 58, scale), (int) (activity.getResources().getDisplayMetrics().heightPixels * .75f));
            window.setLayout(width, height);
        }
    }
    private static int px(float value, float scale) { return Math.max(1, (int) (value * scale + .5f)); }
    private static GradientDrawable shape(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    private static TextView text(android.content.Context context, String value, int size, float scale, int color) {
        TextView t = new TextView(context); t.setText(value); t.setTextColor(color);
        t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, size * scale * context.getResources().getConfiguration().fontScale); return t;
    }
}
