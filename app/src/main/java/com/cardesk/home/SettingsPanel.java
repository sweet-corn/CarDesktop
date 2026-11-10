package com.cardesk.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Light three-region settings panel with a two-column content area. */
public final class SettingsPanel {
    private static final int INK = 0xff30363e;
    private static final int BLUE = 0xff087bff;
    private final Activity activity;
    private final Context context;
    private final Dialog dialog;
    private final SharedPreferences prefs;
    private final Runnable rebuild;
    private final List<String> categories = new ArrayList<>();
    private final List<List<View>> pages = new ArrayList<>();
    private final List<TextView> tabs = new ArrayList<>();
    private LinearLayout content;

    public SettingsPanel(Activity activity, SharedPreferences prefs, Runnable rebuild) {
        this.activity = activity; this.prefs = prefs; this.rebuild = rebuild;
        context = new android.view.ContextThemeWrapper(activity, android.R.style.Theme_Material_Light_NoActionBar);
        dialog = new Dialog(context); dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private int dp(float n) { return (int) (n * context.getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable shape = new GradientDrawable(); shape.setColor(color); shape.setCornerRadius(dp(radius)); return shape;
    }
    private TextView label(String value, int size, int color) {
        TextView view = new TextView(context); view.setText(value); view.setTextSize(size); view.setTextColor(color); return view;
    }
    public void category(String name) { categories.add(name); pages.add(new ArrayList<>()); }
    private LinearLayout card(String title, String detail) {
        LinearLayout box = new LinearLayout(context); box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(10)); box.setBackground(background(0xffeceeef, 14));
        box.addView(label(title, 14, INK));
        TextView subtitle = label(detail, 11, 0xff7d858e); subtitle.setPadding(0, dp(5), 0, dp(7));
        box.addView(subtitle, new LinearLayout.LayoutParams(-1, 0, 1));
        pages.get(pages.size() - 1).add(box); return box;
    }
    public void action(String title, String detail, String button, Runnable action) {
        LinearLayout box = card(title, detail);
        TextView control = label(button, 12, BLUE); control.setGravity(Gravity.CENTER);
        control.setBackground(background(0xffe0eaff, 8)); control.setPadding(dp(14), dp(8), dp(14), dp(8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2); params.gravity = Gravity.END;
        box.addView(control, params); control.setOnClickListener(v -> action.run());
        box.setOnClickListener(v -> action.run());
    }
    public void toggle(String title, String detail, String key, boolean fallback) {
        LinearLayout box = card(title, detail);
        Switch control = new Switch(context); control.setContentDescription(title); control.setChecked(prefs.getBoolean(key, fallback));
        android.content.res.ColorStateList colors = new android.content.res.ColorStateList(
            new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}}, new int[]{BLUE, 0xffcbd0d5});
        control.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE)); control.setTrackTintList(colors);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(36)); params.gravity = Gravity.END;
        box.addView(control, params);
        control.setOnCheckedChangeListener((button, checked) -> { prefs.edit().putBoolean(key, checked).apply(); rebuild.run(); });
    }
    private void select(int index) {
        content.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setTextColor(i == index ? BLUE : INK);
            tabs.get(i).setBackground(background(i == index ? 0xffe9edf2 : Color.TRANSPARENT, 12));
        }
        List<View> views = pages.get(index);
        for (int i = 0; i < views.size(); i += 2) {
            LinearLayout row = new LinearLayout(context);
            for (int j = 0; j < 2; j++) {
                View card = i + j < views.size() ? views.get(i + j) : new View(context);
                if (card.getParent() != null) ((android.view.ViewGroup) card.getParent()).removeView(card);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(125), 1);
                params.setMargins(dp(4), dp(4), dp(4), dp(4)); row.addView(card, params);
            }
            content.addView(row);
        }
    }
    public void show() {
        LinearLayout panel = new LinearLayout(context); panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setBackground(background(0xfff8f9fa, 20));
        ScrollView navigationScroll = new ScrollView(context);
        LinearLayout navigation = new LinearLayout(context); navigation.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < categories.size(); i++) {
            final int index = i;
            TextView tab = label(categories.get(i), 15, INK); tab.setGravity(Gravity.CENTER);
            navigation.addView(tab, new LinearLayout.LayoutParams(-1, dp(58)));
            tab.setOnClickListener(v -> select(index)); tabs.add(tab);
        }
        navigationScroll.addView(navigation); panel.addView(navigationScroll, new LinearLayout.LayoutParams(0, -1, .17f));
        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(true);
        content = new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL); scroll.addView(content);
        panel.addView(scroll, new LinearLayout.LayoutParams(0, -1, .62f));
        ScrollView aboutScroll = new ScrollView(context);
        LinearLayout about = new LinearLayout(context); about.setOrientation(LinearLayout.VERTICAL); about.setGravity(Gravity.CENTER_HORIZONTAL);
        about.setPadding(dp(12), dp(16), dp(12), dp(12));
        TextView title = label("CarDesk", 21, INK); about.addView(title);
        String version = "";
        try { version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName; } catch (android.content.pm.PackageManager.NameNotFoundException ignored) { }
        TextView info = label("车机桌面\nv" + version + "\n\n目标适配\n驱逐舰 05\nDiLink 3.0 · Android 10\n1920 × 1080\n\n当前设备\nAndroid " + android.os.Build.VERSION.RELEASE
            + "\n" + activity.getResources().getDisplayMetrics().widthPixels + " × " + activity.getResources().getDisplayMetrics().heightPixels
            + "\n\n车控接口未连接\n请在停车状态设置", 12, 0xff858c94);
        info.setGravity(Gravity.CENTER); info.setLineSpacing(dp(5), 1); info.setPadding(0, dp(12), 0, dp(18)); about.addView(info);
        TextView close = label("返回桌面", 14, BLUE); close.setPadding(dp(16), dp(12), dp(16), dp(12));
        close.setBackground(background(0xffe0eaff, 10)); close.setOnClickListener(v -> dialog.dismiss()); about.addView(close);
        aboutScroll.addView(about); panel.addView(aboutScroll, new LinearLayout.LayoutParams(0, -1, .21f));
        dialog.setContentView(panel); select(0); dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes(); params.dimAmount = .18f;
            params.width = (int) (activity.getResources().getDisplayMetrics().widthPixels * .97f);
            params.height = (int) (activity.getResources().getDisplayMetrics().heightPixels * .82f);
            window.setAttributes(params);
        }
    }
}
