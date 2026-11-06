package com.cardesk.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Independent implementation based on the observed layout, not the original app's code. */
public class MainActivity extends Activity {
    private static final int PICK_WALLPAPER = 101;
    private SharedPreferences prefs;
    private FrameLayout root;
    private LinearLayout clockBlock;
    private ImageView wallpaper;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        buildHome();
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private FrameLayout.LayoutParams position(int width, int height, int gravity, int left, int top, int right, int bottom) {
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(width < 0 ? width : dp(width), height < 0 ? height : dp(height), gravity);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom)); return p;
    }

    private void buildHome() {
        root = new FrameLayout(this);
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(28, 47, 61), Color.rgb(12, 20, 30), Color.rgb(31, 43, 51)});
        root.setBackground(background);
        wallpaper = new ImageView(this); wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));
        restoreWallpaper();
        View tint = new View(this); tint.setBackgroundColor(0x44000000); root.addView(tint, new FrameLayout.LayoutParams(-1, -1));

        clockBlock = new LinearLayout(this); clockBlock.setOrientation(LinearLayout.VERTICAL); clockBlock.setGravity(Gravity.CENTER_HORIZONTAL);
        TextClock clock = new TextClock(this); clock.setFormat12Hour("HH:mm"); clock.setFormat24Hour("HH:mm"); clock.setTextSize(72); clock.setTextColor(Color.WHITE); clock.setShadowLayer(dp(4), dp(2), dp(2), 0x80000000);
        TextClock date = new TextClock(this); date.setFormat12Hour("yyyy年MM月dd日 EEEE"); date.setFormat24Hour("yyyy年MM月dd日 EEEE"); date.setTextSize(18); date.setTextColor(0xccffffff);
        clockBlock.addView(clock); clockBlock.addView(date); clockBlock.setVisibility(prefs.getBoolean("clock", true) ? View.VISIBLE : View.GONE);
        root.addView(clockBlock, position(-2, -2, Gravity.TOP | Gravity.START, 60, 80, 0, 0));

        TextView title = text("CAR DESK", 13, 0xbfffffff); title.setLetterSpacing(.2f);
        root.addView(title, position(-2, -2, Gravity.TOP | Gravity.END, 0, 24, 32, 0));

        HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout cards = new LinearLayout(this); cards.setPadding(dp(10), dp(8), dp(10), dp(8)); cards.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(cards); root.addView(scroll, position(-1, 116, Gravity.BOTTOM, 18, 0, 18, 18));
        cards.addView(card("◉", "导航", "打开地图应用", this::chooseNavigation));
        cards.addView(card("♫", "音乐", "选择并打开音乐应用", () -> showApps("选择音乐应用")));
        cards.addView(card("▦", "全部应用", "查看已安装的应用", () -> showApps("全部应用")));
        cards.addView(card("▧", "壁纸", "选择本地图片", this::chooseWallpaper));
        cards.addView(card("⚙", "设置", "桌面与系统设置", this::showSettings));
        root.setOnLongClickListener(view -> { showSettings(); return true; });
        setContentView(root);
    }

    private View card(String icon, String title, String subtitle, Runnable action) {
        LinearLayout box = new LinearLayout(this); box.setGravity(Gravity.CENTER_VERTICAL); box.setPadding(dp(20), 0, dp(16), 0);
        box.setBackground(shape(0xb21a2028, 18));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(220), dp(95)); params.setMargins(dp(6), 0, dp(6), 0); box.setLayoutParams(params);
        TextView symbol = text(icon, 28, Color.WHITE); symbol.setGravity(Gravity.CENTER); symbol.setBackground(shape(0x663f6277, 12));
        box.addView(symbol, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(dp(12), 0, 0, 0);
        TextView heading = text(title, 16, Color.WHITE); heading.setTypeface(null, Typeface.BOLD);
        TextView detail = text(subtitle, 11, 0xffabb8c1); detail.setMaxLines(2);
        labels.addView(heading); labels.addView(detail); box.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        box.setContentDescription(title + "，" + subtitle); box.setOnClickListener(v -> action.run()); return box;
    }

    private List<ResolveInfo> launchableApps() {
        Intent query = new Intent(Intent.ACTION_MAIN); query.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = new ArrayList<>(pm.queryIntentActivities(query, 0));
        Collections.sort(list, (a, b) -> a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        return list;
    }
    private void showApps(String heading) {
        PackageManager pm = getPackageManager(); List<ResolveInfo> apps = launchableApps();
        ScrollView scroll = new ScrollView(this); GridLayout grid = new GridLayout(this); grid.setColumnCount(5); grid.setPadding(dp(12), dp(12), dp(12), dp(12));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(heading).setView(scroll).setNegativeButton("关闭", null).create();
        for (ResolveInfo info : apps) {
            if (getPackageName().equals(info.activityInfo.packageName)) continue;
            LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(8), dp(8), dp(8), dp(8));
            ImageView icon = new ImageView(this); Drawable drawable = info.loadIcon(pm); icon.setImageDrawable(drawable);
            tile.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
            TextView label = text(info.loadLabel(pm).toString(), 12, Color.WHITE); label.setGravity(Gravity.CENTER); label.setMaxLines(2);
            tile.addView(label, new LinearLayout.LayoutParams(dp(110), dp(42)));
            tile.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                try { startActivity(intent); } catch (RuntimeException e) { toast("启动失败：" + e.getClass().getSimpleName()); }
            }); grid.addView(tile);
        }
        scroll.addView(grid); dialog.show();
        if (apps.isEmpty()) toast("未查询到可启动应用；Android 11 以上可能受应用可见性限制");
    }
    private void launch(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) { toast("应用不可启动或当前系统限制了访问"); return; }
        try { startActivity(intent); } catch (RuntimeException e) { toast("启动失败：" + e.getClass().getSimpleName()); }
    }
    private void chooseNavigation() {
        String[] packages = {"com.autonavi.amapauto", "com.baidu.BaiduMap", "com.tencent.wecarnavi"};
        for (String name : packages) if (getPackageManager().getLaunchIntentForPackage(name) != null) { launch(name); return; }
        showApps("选择地图应用");
    }
    private void chooseWallpaper() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(intent, PICK_WALLPAPER); } catch (ActivityNotFoundException e) { toast("系统未提供文件选择器"); }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_WALLPAPER || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            wallpaper.setImageURI(uri); prefs.edit().putString("wallpaper", uri.toString()).apply();
        } catch (SecurityException e) { toast("无法保存图片访问权限，请选择其他文件"); }
    }
    private void restoreWallpaper() {
        String path = prefs.getString("wallpaper", "");
        if (!path.isEmpty()) try { wallpaper.setImageURI(Uri.parse(path)); } catch (RuntimeException e) { toast("壁纸不可用，请重新选择"); }
    }
    private void showSettings() {
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24), dp(12), dp(24), dp(12));
        Switch clock = new Switch(this); clock.setText("显示桌面时钟"); clock.setChecked(prefs.getBoolean("clock", true));
        clock.setOnCheckedChangeListener((button, checked) -> { prefs.edit().putBoolean("clock", checked).apply(); clockBlock.setVisibility(checked ? View.VISIBLE : View.GONE); }); content.addView(clock);
        Button wall = new Button(this); wall.setText("选择壁纸"); wall.setOnClickListener(v -> chooseWallpaper()); content.addView(wall);
        Button system = new Button(this); system.setText("打开系统设置"); system.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (ActivityNotFoundException e) { toast("系统设置不可用"); } }); content.addView(system);
        TextView note = text("此版本为独立基础实现。车辆控制、仪表导航、媒体播放控制及默认桌面接管尚未实现。请在停车状态测试。", 13, 0xffaaaaaa); note.setPadding(0, dp(12), 0, 0); content.addView(note);
        new AlertDialog.Builder(this).setTitle("桌面设置").setView(content).setPositiveButton("完成", null).show();
    }
}
