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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.InputStream;

/** Independent implementation based on the observed layout, not the original app's code. */
public class MainActivity extends Activity {
    private static final int PICK_WALLPAPER = 101;
    private SharedPreferences prefs;
    private FrameLayout root;
    private LinearLayout clockBlock;
    private ImageView wallpaper;
    private VideoView videoWallpaper;
    private static final int PICK_VIDEO = 102;
    private static final int INK = 0xff273541;
    private static final String[] CARD_NAMES = {"导航", "音乐", "车辆", "行程", "胎压", "常用应用"};
    private boolean active;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        buildHome();
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    @Override public void onConfigurationChanged(android.content.res.Configuration configuration) {
        super.onConfigurationChanged(configuration);
        rebuildHome();
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
        root.setBackground(new WallpaperDrawable());
        wallpaper = new ImageView(this); wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));
        videoWallpaper = new VideoView(this);
        root.addView(videoWallpaper, new FrameLayout.LayoutParams(-1, -1));
        restoreWallpaper();

        clockBlock = new LinearLayout(this); clockBlock.setOrientation(LinearLayout.VERTICAL); clockBlock.setGravity(Gravity.CENTER_HORIZONTAL);
        TextClock clock = new TextClock(this); clock.setFormat12Hour("HH:mm"); clock.setFormat24Hour("HH:mm"); clock.setTextSize(72); clock.setTextColor(Color.WHITE); clock.setShadowLayer(dp(4), dp(2), dp(2), 0x80000000);
        TextClock date = new TextClock(this); date.setFormat12Hour("yyyy年MM月dd日 EEEE"); date.setFormat24Hour("yyyy年MM月dd日 EEEE"); date.setTextSize(18); date.setTextColor(0xccffffff);
        clockBlock.addView(clock); clockBlock.addView(date); clockBlock.setVisibility(prefs.getBoolean("clock", false) ? View.VISIBLE : View.GONE);
        root.addView(clockBlock, position(-2, -2, Gravity.TOP | Gravity.START, 60, 80, 0, 0));

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(12), 0, dp(12), 0);
        TextClock smallClock = new TextClock(this); smallClock.setFormat12Hour("HH:mm"); smallClock.setFormat24Hour("HH:mm"); smallClock.setTextColor(INK); smallClock.setTextSize(13);
        top.addView(smallClock); TextView topNote = text("   ♫  音乐未连接", 12, INK); top.addView(topNote, new LinearLayout.LayoutParams(0, -2, 1));
        TextView status = text("状态栏", 12, INK); status.setOnClickListener(v -> showSettings()); top.addView(status);
        if (prefs.getBoolean("top_bar", true)) root.addView(top, position(-1, 36, Gravity.TOP, 0, 0, 0, 0));

        HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout cards = new LinearLayout(this); cards.setPadding(dp(2), dp(8), dp(2), dp(8)); cards.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(cards); root.addView(scroll, position(-1, 108, Gravity.BOTTOM, 0, 0, 0, prefs.getBoolean("bottom_bar", true) ? 48 : 0));
        for (String name : cardOrder()) if (prefs.getBoolean("card_" + name, true)) cards.addView(makeHomeCard(name));
        if (prefs.getBoolean("bottom_bar", true)) root.addView(makeBottomBar(), position(-1, 48, Gravity.BOTTOM, 0, 0, 0, 0));
        root.setOnLongClickListener(view -> { showSettings(); return true; });
        setContentView(root);
    }

    private List<String> cardOrder() {
        List<String> result = new ArrayList<>();
        for (String name : prefs.getString("card_order", "导航,音乐,车辆,行程,胎压,常用应用").split(","))
            for (String valid : CARD_NAMES) if (valid.equals(name) && !result.contains(name)) result.add(name);
        for (String name : CARD_NAMES) if (!result.contains(name)) result.add(name);
        return result;
    }

    private View makeHomeCard(String name) {
        switch (name) {
            case "导航": return navigationCard();
            case "音乐": return card("♫", "音乐", "选择音乐应用", () -> showApps("选择音乐应用"));
            case "车辆": return card("🚘", "车辆", "车控尚未连接", () -> toast("车机接口尚未接入"));
            case "行程": return card("—", "行程", "里程 / 电耗 未连接", () -> toast("行程数据尚未接入"));
            case "胎压": return card("—", "胎压", "-- / -- kPa  未连接", () -> toast("胎压数据尚未接入"));
            default: return card("▦", "常用应用", "打开应用列表", () -> showApps("全部应用"));
        }
    }

    private View navigationCard() {
        LinearLayout group = new LinearLayout(this); group.setGravity(Gravity.CENTER);
        group.setBackground(shape(0xbbeef3f7, 8));
        int width = Math.max(dp(190), (int) (getResources().getDisplayMetrics().widthPixels * .223f));
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(width, dp(90)); layout.setMargins(dp(4), 0, dp(4), 0); group.setLayoutParams(layout);
        String[] icons = {"⌕", "⌂", "P"}; String[] labels = {"搜索", "回家", "停车场"};
        for (int i = 0; i < labels.length; i++) {
            LinearLayout item = new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL); item.setGravity(Gravity.CENTER);
            TextView icon = text(icons[i], 26, INK); icon.setGravity(Gravity.CENTER); item.addView(icon);
            TextView label = text(labels[i], 12, INK); label.setGravity(Gravity.CENTER); item.addView(label);
            final int index = i;
            item.setOnClickListener(v -> {
                if (index != 0) toast("当前仅打开地图；目的地快捷导航尚未接入");
                chooseNavigation();
            });
            item.setOnLongClickListener(v -> { showCardEditor(); return true; });
            group.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
        }
        return group;
    }

    private View makeBottomBar() {
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setBackgroundColor(0x99e2eaf0);
        String[] names = {"返回", "主页", "导航", "音乐", "应用", "设置"};
        for (String name : names) {
            if (!prefs.getBoolean("nav_" + name, true)) continue;
            String custom = prefs.getString("nav_app_label_" + name, name);
            TextView button = text(custom, 14, INK); button.setGravity(Gravity.CENTER); button.setContentDescription(custom);
            button.setOnLongClickListener(v -> { configureNav(name); return true; });
            button.setOnClickListener(v -> {
                String component = prefs.getString("nav_app_" + name, "");
                if (!component.isEmpty()) { launchComponent(component); return; }
                switch (name) {
                    case "返回": onBackPressed(); break;
                    case "主页": break;
                    case "导航": chooseNavigation(); break;
                    case "音乐": showApps("选择音乐应用"); break;
                    case "应用": showApps("全部应用"); break;
                    default: showSettings();
                }
            });
            bar.addView(button, new LinearLayout.LayoutParams(0, -1, 1));
        }
        return bar;
    }

    private View card(String icon, String title, String subtitle, Runnable action) {
        LinearLayout box = new LinearLayout(this); box.setGravity(Gravity.CENTER_VERTICAL); box.setPadding(dp(20), 0, dp(16), 0);
        box.setBackground(shape(0xbbeef3f7, 8));
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int width = Math.max(dp(190), (int) (screenWidth * .223f));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(90)); params.setMargins(dp(4), 0, dp(4), 0); box.setLayoutParams(params);
        TextView symbol = text(icon, 28, INK); symbol.setGravity(Gravity.CENTER); symbol.setBackground(shape(0x33ffffff, 8));
        box.addView(symbol, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(dp(12), 0, 0, 0);
        TextView heading = text(title, 16, INK); heading.setTypeface(null, Typeface.BOLD);
        TextView detail = text(subtitle, 11, 0xff526472); detail.setMaxLines(2);
        labels.addView(heading); labels.addView(detail); box.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        box.setContentDescription(title + "，" + subtitle); box.setOnClickListener(v -> action.run());
        box.setOnLongClickListener(v -> { showCardEditor(); return true; }); return box;
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
        String[] packages = {"com.autonavi.amapauto", "com.autonavi.minimap", "com.baidu.BaiduMap", "com.tencent.wecarnavi"};
        for (String name : packages) if (getPackageManager().getLaunchIntentForPackage(name) != null) { launch(name); return; }
        showApps("选择地图应用");
    }
    private void chooseWallpaper() {
        pickWallpaper(false);
    }
    private void pickWallpaper(boolean video) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType(video ? "video/*" : "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(intent, video ? PICK_VIDEO : PICK_WALLPAPER); } catch (ActivityNotFoundException e) { toast("系统未提供文件选择器"); }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if ((request != PICK_WALLPAPER && request != PICK_VIDEO) || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            prefs.edit().putString("wallpaper", uri.toString()).putBoolean("video_wallpaper", request == PICK_VIDEO).apply();
            restoreWallpaper();
        } catch (SecurityException e) { toast("无法保存图片访问权限，请选择其他文件"); }
    }
    private void restoreWallpaper() {
        videoWallpaper.stopPlayback(); videoWallpaper.setVisibility(View.GONE); wallpaper.setImageDrawable(null);
        String path = prefs.getString("wallpaper", "");
        if (path.isEmpty()) return;
        try {
            Uri uri = Uri.parse(path);
            if (prefs.getBoolean("video_wallpaper", false)) {
                videoWallpaper.setVisibility(View.VISIBLE);
                videoWallpaper.setOnPreparedListener(player -> {
                    player.setLooping(true); player.setVolume(0, 0);
                    if (active) videoWallpaper.start();
                });
                videoWallpaper.setOnErrorListener((player, what, extra) -> {
                    videoWallpaper.setVisibility(View.GONE); toast("视频无法播放，请更换视频或恢复默认壁纸"); return true;
                });
                videoWallpaper.setVideoURI(uri);
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = true;
                try (InputStream input = getContentResolver().openInputStream(uri)) { BitmapFactory.decodeStream(input, null, options); }
                int width = Math.max(1, getResources().getDisplayMetrics().widthPixels);
                int height = Math.max(1, getResources().getDisplayMetrics().heightPixels);
                options.inSampleSize = 1;
                while (options.outWidth / options.inSampleSize > width * 2 || options.outHeight / options.inSampleSize > height * 2) options.inSampleSize *= 2;
                options.inJustDecodeBounds = false;
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    Bitmap image = BitmapFactory.decodeStream(input, null, options);
                    if (image == null) throw new IllegalArgumentException("Unsupported image");
                    wallpaper.setImageBitmap(image);
                }
            }
        } catch (Exception e) { videoWallpaper.setVisibility(View.GONE); toast("壁纸不可用，请重新选择"); }
    }
    @Override protected void onResume() { super.onResume(); active = true; if (videoWallpaper != null && videoWallpaper.getVisibility() == View.VISIBLE) videoWallpaper.start(); }
    @Override protected void onPause() { active = false; if (videoWallpaper != null) videoWallpaper.pause(); super.onPause(); }
    @Override protected void onDestroy() { if (videoWallpaper != null) videoWallpaper.stopPlayback(); super.onDestroy(); }
    @Override public void onBackPressed() { showSettings(); }

    private void launchComponent(String value) {
        android.content.ComponentName component = android.content.ComponentName.unflattenFromString(value);
        if (component == null) { toast("快捷方式无效，请重新配置"); return; }
        try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
        catch (RuntimeException e) { toast("应用无法打开，请长按按键重新配置"); }
    }
    private void configureNav(String slot) {
        List<ResolveInfo> apps = launchableApps();
        String[] labels = new String[apps.size() + 1]; labels[0] = "恢复默认功能";
        for (int i = 0; i < apps.size(); i++) labels[i + 1] = apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("配置按键：" + slot).setItems(labels, (dialog, index) -> {
            SharedPreferences.Editor edit = prefs.edit();
            if (index == 0) edit.remove("nav_app_" + slot).remove("nav_app_label_" + slot);
            else {
                ResolveInfo app = apps.get(index - 1);
                edit.putString("nav_app_" + slot, new android.content.ComponentName(app.activityInfo.packageName, app.activityInfo.name).flattenToString());
                edit.putString("nav_app_label_" + slot, labels[index]);
            }
            edit.apply(); rebuildHome();
        }).setNegativeButton("取消", null).show();
    }
    private void rebuildHome() { if (videoWallpaper != null) videoWallpaper.stopPlayback(); buildHome(); hideSystemBars(); }

    private void showCardEditor() {
        List<String> order = cardOrder();
        String[] labels = new String[order.size()];
        for (int i = 0; i < order.size(); i++) labels[i] = (prefs.getBoolean("card_" + order.get(i), true) ? "显示  " : "隐藏  ") + order.get(i);
        new AlertDialog.Builder(this).setTitle("卡片管理 · 点击配置").setItems(labels, (dialog, index) -> {
            String name = order.get(index);
            new AlertDialog.Builder(this).setTitle(name).setItems(new String[]{"显示 / 隐藏", "向左移动", "向右移动"}, (d, action) -> {
                if (action == 0) prefs.edit().putBoolean("card_" + name, !prefs.getBoolean("card_" + name, true)).apply();
                else {
                    int target = action == 1 ? index - 1 : index + 1;
                    if (target >= 0 && target < order.size()) Collections.swap(order, index, target);
                    prefs.edit().putString("card_order", android.text.TextUtils.join(",", order)).apply();
                }
                rebuildHome(); showCardEditor();
            }).setNegativeButton("取消", null).show();
        }).setNegativeButton("完成", null).show();
    }

    private void addSettingButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this); button.setText(label); button.setOnClickListener(v -> action.run()); parent.addView(button);
    }
    private void addToggle(LinearLayout parent, String label, String key, boolean fallback) {
        Switch toggle = new Switch(this); toggle.setText(label); toggle.setPadding(dp(8), dp(10), dp(8), dp(10)); toggle.setChecked(prefs.getBoolean(key, fallback));
        toggle.setOnCheckedChangeListener((button, checked) -> { prefs.edit().putBoolean(key, checked).apply(); rebuildHome(); }); parent.addView(toggle);
    }
    private void showSettings() {
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24), dp(12), dp(24), dp(12));
        addToggle(content, "显示大时钟", "clock", false);
        addToggle(content, "显示应用顶部栏", "top_bar", true);
        addToggle(content, "显示应用底部栏", "bottom_bar", true);
        addSettingButton(content, "选择图片壁纸", this::chooseWallpaper);
        addSettingButton(content, "选择视频壁纸（静音循环）", () -> pickWallpaper(true));
        addSettingButton(content, "恢复默认壁纸", () -> { prefs.edit().remove("wallpaper").remove("video_wallpaper").apply(); rebuildHome(); });
        addSettingButton(content, "卡片显示与排序", this::showCardEditor);
        addSettingButton(content, "底栏按键配置", () -> new AlertDialog.Builder(this).setTitle("选择按键（也可长按底栏按键）")
            .setItems(new String[]{"返回", "主页", "导航", "音乐", "应用", "设置"}, (d, i) -> configureNav(new String[]{"返回", "主页", "导航", "音乐", "应用", "设置"}[i])).show());
        addSettingButton(content, "底栏按键显示", () -> {
            String[] names = {"返回", "主页", "导航", "音乐", "应用", "设置"}; boolean[] checks = new boolean[names.length];
            for (int i = 0; i < names.length; i++) checks[i] = prefs.getBoolean("nav_" + names[i], true);
            new AlertDialog.Builder(this).setTitle("显示按键").setMultiChoiceItems(names, checks, (d, i, checked) -> checks[i] = checked)
                .setPositiveButton("保存", (d, which) -> { SharedPreferences.Editor edit = prefs.edit(); for (int i = 0; i < names.length; i++) edit.putBoolean("nav_" + names[i], checks[i]); edit.apply(); rebuildHome(); })
                .setNegativeButton("取消", null).show();
        });
        Button system = new Button(this); system.setText("打开系统设置"); system.setOnClickListener(v -> { try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (ActivityNotFoundException e) { toast("系统设置不可用"); } }); content.addView(system);
        TextView note = text("长按桌面可随时返回设置。当前仅在本应用内请求隐藏系统栏；退出后恢复由系统管理。底栏返回键打开桌面设置，不是跨应用全局返回。车辆和媒体数据尚未接入。仅停车测试。", 13, 0xffaaaaaa); note.setPadding(0, dp(12), 0, 0); content.addView(note);
        ScrollView scroll = new ScrollView(this); scroll.addView(content);
        new AlertDialog.Builder(this).setTitle("桌面设置").setView(scroll).setPositiveButton("完成", null).show();
    }
}
