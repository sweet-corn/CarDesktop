package com.cardesk.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

/** Compact, independently scaled light app picker. */
public final class AppsPanel {
    public static void show(Activity activity, String title, List<ResolveInfo> apps) {
        float scale = Math.min(activity.getResources().getDisplayMetrics().widthPixels / 1200f,
            activity.getResources().getDisplayMetrics().heightPixels / 675f);
        android.content.Context context = new android.view.ContextThemeWrapper(activity, android.R.style.Theme_Material_Light_NoActionBar);
        Dialog dialog = new Dialog(context); dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel = new LinearLayout(context); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(px(18, scale), px(12, scale), px(18, scale), px(14, scale));
        GradientDrawable bg = new GradientDrawable(); bg.setColor(0xfff8f9fa); bg.setCornerRadius(px(18, scale)); panel.setBackground(bg);
        LinearLayout header = new LinearLayout(context); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = text(context, title, 20, scale); header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        TextView close = text(context, "关闭  ✕", 14, scale); close.setPadding(px(14, scale), px(10, scale), px(14, scale), px(10, scale));
        close.setOnClickListener(v -> dialog.dismiss()); header.addView(close); panel.addView(header);
        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(true);
        GridLayout grid = new GridLayout(context); grid.setColumnCount(6); grid.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        PackageManager manager = activity.getPackageManager(); int shown = 0;
        for (ResolveInfo info : apps) {
            if (activity.getPackageName().equals(info.activityInfo.packageName)) continue;
            LinearLayout tile = new LinearLayout(context); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER);
            GradientDrawable tileBg = new GradientDrawable(); tileBg.setColor(0xffeef0f2); tileBg.setCornerRadius(px(12, scale)); tile.setBackground(tileBg);
            ImageView icon = new ImageView(context); icon.setImageDrawable(info.loadIcon(manager));
            tile.addView(icon, new LinearLayout.LayoutParams(px(42, scale), px(42, scale)));
            TextView label = text(context, info.loadLabel(manager).toString(), 12, scale); label.setGravity(Gravity.CENTER); label.setMaxLines(1);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END); label.setPadding(px(4, scale), px(5, scale), px(4, scale), 0);
            tile.addView(label, new LinearLayout.LayoutParams(-1, -2));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(); params.width = 0; params.height = px(96, scale);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); params.setMargins(px(4, scale), px(4, scale), px(4, scale), px(4, scale));
            grid.addView(tile, params); shown++;
            tile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                try { activity.startActivity(intent); dialog.dismiss(); }
                catch (RuntimeException e) { Toast.makeText(activity, "启动失败，请检查应用是否可用", Toast.LENGTH_SHORT).show(); }
            });
        }
        scroll.addView(grid); panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        if (shown == 0) { TextView empty = text(context, "没有找到可启动的应用", 15, scale); empty.setGravity(Gravity.CENTER); panel.addView(empty); }
        dialog.setContentView(panel); dialog.show(); Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout((int) (activity.getResources().getDisplayMetrics().widthPixels * .88f),
                (int) (activity.getResources().getDisplayMetrics().heightPixels * .80f));
        }
    }
    private static int px(float value, float scale) { return Math.max(1, (int) (value * scale + .5f)); }
    private static TextView text(android.content.Context context, String value, int size, float scale) {
        TextView view = new TextView(context); view.setText(value); view.setTextColor(Color.rgb(39, 53, 65));
        view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, size * scale * context.getResources().getConfiguration().fontScale); return view;
    }
}
