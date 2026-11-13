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
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private boolean buildingHome;
    private float homeScale = 1f;
    private MusicSession musicSession;
    private ImageView albumArt;
    private ImageView musicAppIcon;
    private TextView musicTitle;
    private TextView playButton;
    private TextView nextButton;
    private TextView topMusic;
    private boolean musicAuthorizationPending;
    private boolean musicAutoAuthorizationAttempted;
    private TextView speedLabel;
    private SpeedMonitor speedMonitor;
    private String speedText = "-- km/h · GPS未连接";
    private String vehicleSpeedProbe = "尚未检查";
    private VehicleBridge vehicleBridge;
    private final TextView[] tyreReadings = new TextView[4];
    private TextView tripDistance, tripElectric, tripFuel, vehicleDataStatus;
    private AdbConnectionManager adbConnection;
    private final android.content.BroadcastReceiver musicUpdates = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context context, Intent intent) {
            if (active && musicSession != null) musicSession.refresh();
        }
    };
    private boolean musicReceiverRegistered;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        musicSession = new MusicSession(this, this::updateMusic);
        vehicleBridge = new VehicleBridge(this, snapshot -> updateVehicleCards());
        adbConnection = new AdbConnectionManager(this, vehicleBridge);
        speedMonitor = new SpeedMonitor(this, value -> { speedText = value; if (speedLabel != null) speedLabel.setText(formatSpeed(value)); });
        java.util.concurrent.ExecutorService probe = java.util.concurrent.Executors.newSingleThreadExecutor();
        probe.execute(() -> {
            String result = SpeedMonitor.probeVehicleApi(getApplicationContext());
            runOnUiThread(() -> vehicleSpeedProbe = result);
        });
        probe.shutdown();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
        buildHome();
        // One attempt on launch; the system owns the RSA authorization prompt.
        if (prefs.getBoolean("adb_auto", true)) {
            adbConnection.connect(prefs.getString("adb_host", "127.0.0.1"), prefs.getInt("adb_port", 5555));
        }
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

    private int dp(float value) { return (int) (value * (buildingHome ? homeScale : getResources().getDisplayMetrics().density) + .5f); }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value);
        if (buildingHome) t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, size * homeScale * getResources().getConfiguration().fontScale);
        else t.setTextSize(size);
        t.setTextColor(color); return t;
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private CharSequence formatSpeed(String value) {
        int split = value.indexOf(" km/h");
        if (split < 0) return value;
        String number = value.substring(0, split);
        String suffix = value.substring(split).trim().replace(" · ", "\n");
        android.text.SpannableString result = new android.text.SpannableString(number + "\n" + suffix);
        result.setSpan(new android.text.style.RelativeSizeSpan(.31f), number.length() + 1, result.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new android.text.style.StyleSpan(Typeface.NORMAL), number.length() + 1, result.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }
    private FrameLayout.LayoutParams position(int width, int height, int gravity, int left, int top, int right, int bottom) {
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(width < 0 ? width : dp(width), height < 0 ? height : dp(height), gravity);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom)); return p;
    }

    private void buildHome() {
        java.util.Arrays.fill(tyreReadings, null);
        tripDistance = null; tripElectric = null; tripFuel = null; vehicleDataStatus = null;
        albumArt = null; musicAppIcon = null; musicTitle = null; playButton = null; nextButton = null;
        // Use a 1200 x 675 design canvas, independent of the phone/car density.
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        homeScale = Math.min(metrics.widthPixels / 1200f, metrics.heightPixels / 675f);
        buildingHome = true;
        root = new FrameLayout(this);
        root.setBackground(new WallpaperDrawable());
        wallpaper = new ImageView(this); wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));
        videoWallpaper = new VideoView(this);
        root.addView(videoWallpaper, new FrameLayout.LayoutParams(-1, -1));
        restoreWallpaper();

        clockBlock = new LinearLayout(this); clockBlock.setOrientation(LinearLayout.VERTICAL); clockBlock.setGravity(Gravity.CENTER_HORIZONTAL);
        TextClock clock = new TextClock(this); clock.setFormat12Hour("HH:mm"); clock.setFormat24Hour("HH:mm"); clock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 72 * homeScale); clock.setTextColor(Color.WHITE); clock.setShadowLayer(dp(4), dp(2), dp(2), 0x80000000);
        TextClock date = new TextClock(this); date.setFormat12Hour("yyyy年MM月dd日 EEEE"); date.setFormat24Hour("yyyy年MM月dd日 EEEE"); date.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 18 * homeScale); date.setTextColor(0xccffffff);
        clockBlock.addView(clock); clockBlock.addView(date); clockBlock.setVisibility(prefs.getBoolean("clock", true) ? View.VISIBLE : View.GONE);
        root.addView(clockBlock, position(-2, -2, Gravity.TOP | Gravity.START, 270, 80, 0, 0));

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(12), 0, dp(12), 0);
        TextClock smallClock = new TextClock(this); smallClock.setFormat12Hour("HH:mm"); smallClock.setFormat24Hour("HH:mm"); smallClock.setTextColor(INK); smallClock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 16 * homeScale);
        top.addView(smallClock); topMusic = text("   ♫  音乐未连接", 12, INK); topMusic.setSingleLine(true); topMusic.setEllipsize(android.text.TextUtils.TruncateAt.END); top.addView(topMusic, new LinearLayout.LayoutParams(0, -2, 1));
        TextView status = text("状态栏", 12, INK); status.setOnClickListener(v -> showSettings()); top.addView(status);
        if (prefs.getBoolean("top_bar", true)) root.addView(top, position(-1, 36, Gravity.TOP, 0, 0, 0, 0));
        speedLabel = text(speedText, 44, INK); speedLabel.setGravity(Gravity.CENTER);
        speedLabel.setTypeface(null, Typeface.BOLD);
        speedLabel.setPadding(dp(12), dp(8), dp(12), dp(8));
        speedLabel.setBackground(shape(0xddeff6fc, 16));
        speedLabel.setText(formatSpeed(speedText));
        speedLabel.setOnClickListener(v -> requestSpeedPermission());
        if (prefs.getBoolean("show_speed", true)) root.addView(speedLabel, position(220, 140, Gravity.TOP | Gravity.START, 24, 80, 0, 0));

        HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout cards = new LinearLayout(this); cards.setPadding(dp(2), dp(8), dp(2), dp(8)); cards.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(cards); root.addView(scroll, position(-1, 108, Gravity.BOTTOM, 0, 0, 0, prefs.getBoolean("bottom_bar", true) ? 48 : 0));
        for (String name : cardOrder()) if (prefs.getBoolean("card_" + name, true)) cards.addView(makeHomeCard(name));
        if (prefs.getBoolean("bottom_bar", true)) root.addView(makeBottomBar(), position(-1, 48, Gravity.BOTTOM, 0, 0, 0, 0));
        root.setOnLongClickListener(view -> { showSettings(); return true; });
        setContentView(root);
        buildingHome = false;
        updateMusic();
        updateVehicleCards();
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
            case "音乐": return musicCard();
            case "车辆": return card("vehicle", "车辆", "车控尚未连接", () -> toast("车机接口尚未接入"));
            case "行程": return tripCard();
            case "胎压": return tyreCard();
            default: return card("▦", "常用应用", "打开应用列表", () -> showApps("全部应用"));
        }
    }

    private LinearLayout dataCard(int width) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(7), dp(12), dp(7)); box.setBackground(shape(0xbbeef3f7, 8));
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(dp(width), dp(90)); layout.setMargins(dp(4), 0, dp(4), 0); box.setLayoutParams(layout);
        box.setOnClickListener(v -> showVehicleBridge());
        box.setOnLongClickListener(v -> { showCardEditor(); return true; }); return box;
    }
    private View tyreCard() {
        LinearLayout box = dataCard(190);
        TextView title = text("四轮胎压 · kPa", 12, INK); box.addView(title);
        String[] positions = {"左前", "右前", "左后", "右后"};
        for (int row = 0; row < 2; row++) {
            LinearLayout line = new LinearLayout(this); line.setGravity(Gravity.CENTER_VERTICAL);
            for (int col = 0; col < 2; col++) {
                int i = row * 2 + col;
                TextView label = text(positions[i] + "  --", 14, INK); label.setGravity(Gravity.CENTER_VERTICAL);
                tyreReadings[i] = label; line.addView(label, new LinearLayout.LayoutParams(0, -1, 1));
            }
            box.addView(line, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        return box;
    }
    private View tripCard() {
        LinearLayout box = dataCard(240);
        LinearLayout values = new LinearLayout(this); values.setGravity(Gravity.CENTER_VERTICAL);
        tripDistance = text("-- km", 20, INK); tripDistance.setTypeface(null, Typeface.BOLD);
        tripElectric = text("--", 15, INK); tripElectric.setTypeface(null, Typeface.BOLD);
        tripDistance.setSingleLine(true); tripElectric.setSingleLine(true);
        tripElectric.setEllipsize(android.text.TextUtils.TruncateAt.END);
        values.addView(tripDistance, new LinearLayout.LayoutParams(0, -2, 1)); values.addView(tripElectric, new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(values);
        tripFuel = text("最近百公里电耗 / 油耗", 10, 0xff526472); tripFuel.setSingleLine(true); box.addView(tripFuel);
        vehicleDataStatus = text("未连接", 10, 0xff526472); vehicleDataStatus.setSingleLine(true); box.addView(vehicleDataStatus);
        return box;
    }
    private void updateVehicleCards() {
        org.json.JSONObject data = vehicleBridge.data();
        String[] positions = {"左前", "右前", "左后", "右后"};
        for (int i = 0; i < 4; i++) if (tyreReadings[i] != null) tyreReadings[i].setText(positions[i] + "  " + VehicleCards.pressure(data, i + 1));
        if (tripDistance != null) tripDistance.setText(VehicleCards.number(data, "trip_distance_raw", 1000000) + " km");
        if (tripElectric != null) tripElectric.setText(VehicleCards.number(data, "electric_recent_raw", 1000) + " kWh/100km");
        if (tripFuel != null) tripFuel.setText("最近百公里油耗  " + VehicleCards.number(data, "fuel_recent_raw", 1000) + " L/100km");
        if (vehicleDataStatus != null) vehicleDataStatus.setText(data == null ? "未连接 · 点击检查ADB" : "本次里程 / 最近能耗 · 点击诊断");
    }

    private View navigationCard() {
        LinearLayout group = new LinearLayout(this); group.setGravity(Gravity.CENTER);
        group.setBackground(shape(0xbbeef3f7, 8));
        int width = dp(215);
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
            item.setOnLongClickListener(v -> { selectNavigation(); return true; });
            group.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
        }
        group.setOnLongClickListener(v -> { selectNavigation(); return true; });
        return group;
    }

    private View makeBottomBar() {
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setBackgroundColor(0x99e2eaf0);
        LinearLayout system = new LinearLayout(this); system.setGravity(Gravity.CENTER_VERTICAL);
        String[] systemNames = {"返回", "主页", "多任务", "应用"};
        for (String name : systemNames) {
            FrameLayout button = new FrameLayout(this);
            ImageView icon = new ImageView(this); icon.setImageDrawable(new NavIconDrawable(name));
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            button.addView(icon, new FrameLayout.LayoutParams(dp(21), dp(21), Gravity.CENTER));
            button.setContentDescription(name);
            button.setBackgroundResource(android.R.drawable.list_selector_background);
            button.setOnClickListener(v -> {
                if ("应用".equals(name)) { showApps("所有应用"); return; }
                if ("主页".equals(name)) { toast("已在桌面"); return; }
                if ("多任务".equals(name)) { openSystemRecents(); return; }
                int action = android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
                if (!NavigationAccessibilityService.navigate(action)) {
                    if (NavigationAccessibilityService.isConnected()) toast("当前系统不支持此导航操作");
                    else requestNavigationAccess();
                }
            });
            system.addView(button, new LinearLayout.LayoutParams(0, -1, 1));
        }
        bar.addView(system, new LinearLayout.LayoutParams(0, -1, .24f));
        LinearLayout climate = new LinearLayout(this); climate.setGravity(Gravity.CENTER_VERTICAL);
        String[] climateItems = {"风量", "‹", "--°", "›", "‹", "风向", "›", "‹", "--°", "›", "循环"};
        for (String item : climateItems) {
            View control;
            if ("风量".equals(item) || "风向".equals(item) || "循环".equals(item)) {
                FrameLayout container = new FrameLayout(this);
                ImageView image = new ImageView(this); image.setImageDrawable(new NavIconDrawable(item));
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                container.addView(image, new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER)); control = container;
            } else {
                TextView label = text(item, item.contains("°") ? 16 : 20, INK); label.setGravity(Gravity.CENTER); control = label;
            }
            control.setAlpha(.5f); control.setContentDescription("空调未连接：" + item);
            control.setOnClickListener(v -> toast("空调接口未连接；当前仅展示布局，不控制车辆"));
            climate.addView(control, new LinearLayout.LayoutParams(0, -1, item.contains("°") ? 1.5f : 1f));
        }
        bar.addView(climate, new LinearLayout.LayoutParams(0, -1, .52f));
        LinearLayout shortcuts = new LinearLayout(this); shortcuts.setGravity(Gravity.CENTER_VERTICAL);
        for (String name : navOrder()) {
            if (!prefs.getBoolean("nav_" + name, true)) continue;
            String custom = prefs.getString("nav_app_label_" + name, name);
            LinearLayout button = new LinearLayout(this); button.setGravity(Gravity.CENTER);
            button.setBackgroundResource(android.R.drawable.list_selector_background); button.setContentDescription(custom);
            String component = prefs.getString("nav_app_" + name, "");
            Drawable icon = null;
            android.content.ComponentName target = android.content.ComponentName.unflattenFromString(component);
            if (target != null) try { icon = getPackageManager().getActivityIcon(target); } catch (PackageManager.NameNotFoundException ignored) { }
            if (icon != null) {
                ImageView image = new ImageView(this); image.setImageDrawable(icon);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                button.addView(image, new LinearLayout.LayoutParams(dp(21), dp(21)));
            } else {
                ImageView symbol = new ImageView(this); symbol.setImageDrawable(new NavIconDrawable(name));
                symbol.setScaleType(ImageView.ScaleType.FIT_CENTER);
                button.addView(symbol, new LinearLayout.LayoutParams(dp(21), dp(21)));
            }
            button.setOnLongClickListener(v -> { configureNav(name); return true; });
            button.setOnClickListener(v -> {
                String selected = prefs.getString("nav_app_" + name, "");
                if (!selected.isEmpty()) { launchComponent(selected); return; }
                switch (name) {
                    case "返回": onBackPressed(); break;
                    case "主页": break;
                    case "导航": chooseNavigation(); break;
                    case "音乐": openMusic(); break;
                    case "应用": showApps("全部应用"); break;
                    default: showSettings();
                }
            });
            shortcuts.addView(button, new LinearLayout.LayoutParams(0, -1, 1));
        }
        bar.addView(shortcuts, new LinearLayout.LayoutParams(0, -1, .24f));
        return bar;
    }

    private void requestNavigationAccess() {
        new AlertDialog.Builder(this).setTitle("启用系统导航按键")
            .setMessage("全局返回和多任务需要手动启用“CarDesk 导航按键”无障碍服务。服务不读取屏幕内容，仅执行你点击的导航操作。Home按键返回本桌面，不更改系统默认桌面。")
            .setPositiveButton("前往设置", (d, i) -> { try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); } catch (ActivityNotFoundException e) { toast("系统未提供无障碍设置"); } })
            .setNegativeButton("取消", null).show();
    }

    private void openSystemRecents() {
        if (NavigationAccessibilityService.navigate(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)) return;
        adbConnection.openRecents(prefs.getString("adb_host", "127.0.0.1"), prefs.getInt("adb_port", 5555), result -> {
            if (isFinishing() || isDestroyed()) return;
            if (!result.contains("CAR_DESK_RECENTS_RESULT:0")) {
                new AlertDialog.Builder(this).setTitle("多任务请求失败")
                    .setMessage(result.isEmpty() ? "ADB没有返回执行结果，请检查连接。" : result)
                    .setPositiveButton("检查ADB", (d, i) -> showAdbConnection())
                    .setNeutralButton("无障碍设置", (d, i) -> requestNavigationAccess())
                    .setNegativeButton("关闭", null).show();
            }
        });
    }

    private List<String> navOrder() {
        String[] defaults = {"导航", "音乐", "设置"};
        List<String> result = new ArrayList<>();
        for (String name : prefs.getString("nav_order", "导航,音乐,设置").split(","))
            for (String valid : defaults) if (valid.equals(name) && !result.contains(name)) result.add(name);
        for (String name : defaults) if (!result.contains(name)) result.add(name);
        return result;
    }
    private void editNavOrder() {
        List<String> order = navOrder();
        String[] labels = new String[order.size()];
        for (int i = 0; i < order.size(); i++) labels[i] = (prefs.getBoolean("nav_" + order.get(i), true) ? "显示  " : "隐藏  ") + order.get(i);
        new AlertDialog.Builder(this).setTitle("右侧自定义按钮").setItems(labels, (dialog, index) -> {
            String name = order.get(index);
            new AlertDialog.Builder(this).setTitle(name).setItems(new String[]{"显示 / 隐藏", "向左移动", "向右移动", "绑定应用 / 恢复默认"}, (d, action) -> {
                if (action == 3) { configureNav(name); return; }
                if (action == 0) prefs.edit().putBoolean("nav_" + name, !prefs.getBoolean("nav_" + name, true)).apply();
                else {
                    int target = index + (action == 1 ? -1 : 1);
                    if (target >= 0 && target < order.size()) Collections.swap(order, index, target);
                    prefs.edit().putString("nav_order", android.text.TextUtils.join(",", order)).apply();
                }
                rebuildHome(); editNavOrder();
            }).setNegativeButton("取消", null).show();
        }).setNegativeButton("完成", null).show();
    }

    private View card(String icon, String title, String subtitle, Runnable action) {
        LinearLayout box = new LinearLayout(this); box.setGravity(Gravity.CENTER_VERTICAL); box.setPadding(dp(20), 0, dp(16), 0);
        box.setBackground(shape(0xbbeef3f7, 8));
        int width = dp(190);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(90)); params.setMargins(dp(4), 0, dp(4), 0); box.setLayoutParams(params);
        if ("vehicle".equals(icon)) {
            ImageView symbol = new ImageView(this); symbol.setImageDrawable(new CarIconDrawable());
            symbol.setPadding(dp(8), dp(8), dp(8), dp(8)); symbol.setContentDescription("车辆");
            box.addView(symbol, new LinearLayout.LayoutParams(dp(44), dp(44)));
        } else {
            TextView symbol = text(icon, 28, INK); symbol.setGravity(Gravity.CENTER);
            box.addView(symbol, new LinearLayout.LayoutParams(dp(44), dp(44)));
        }
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(dp(12), 0, 0, 0);
        TextView heading = text(title, 16, INK); heading.setTypeface(null, Typeface.BOLD);
        TextView detail = text(subtitle, 11, 0xff526472); detail.setSingleLine(true); detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
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
        AppsPanel.show(this, heading, launchableApps());
    }
    private void launch(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) { toast("应用不可启动或当前系统限制了访问"); return; }
        try { startActivity(intent); } catch (RuntimeException e) { toast("启动失败：" + e.getClass().getSimpleName()); }
    }
    private void chooseNavigation() {
        String selected = prefs.getString("navigation_app", "");
        if (!selected.isEmpty() && getPackageManager().getLaunchIntentForPackage(selected) != null) { launch(selected); return; }
        selectNavigation();
    }
    private void selectNavigation() {
        String[] packages = {"com.autonavi.amapauto", "com.autonavi.minimap", "com.baidu.BaiduMap", "com.baidu.navi", "com.tencent.map", "com.tencent.wecarnavi"};
        String[] names = {"高德地图车机版", "高德地图", "百度地图", "百度导航", "腾讯地图", "腾讯车载导航"};
        List<ChoicePanel.Item> choices = new ArrayList<>();
        for (int i = 0; i < packages.length; i++) if (getPackageManager().getLaunchIntentForPackage(packages[i]) != null) {
            String name = packages[i]; Drawable icon = null;
            try { icon = getPackageManager().getApplicationIcon(name); } catch (PackageManager.NameNotFoundException ignored) { }
            choices.add(new ChoicePanel.Item(names[i], "", icon, name.equals(prefs.getString("navigation_app", "")),
                () -> prefs.edit().putString("navigation_app", name).apply()));
        }
        ChoicePanel.show(this, "默认导航", "选择高德、百度或腾讯地图，设置自动保存", choices);
    }

    private View musicCard() {
        LinearLayout box = new LinearLayout(this); box.setGravity(Gravity.CENTER_VERTICAL); box.setPadding(dp(16), dp(8), dp(12), dp(8));
        box.setBackground(shape(0xbbeef3f7, 8));
        int width = dp(235);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(width, dp(90)); layout.setMargins(dp(4), 0, dp(4), 0); box.setLayoutParams(layout);
        albumArt = new ImageView(this); albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP); albumArt.setContentDescription("打开音乐应用");
        albumArt.setBackground(shape(0x33587896, 8)); albumArt.setClipToOutline(true);
        albumArt.setOnClickListener(v -> openMusic()); box.addView(albumArt, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(10), 0, 0, 0);
        LinearLayout controls = new LinearLayout(this); controls.setGravity(Gravity.CENTER_VERTICAL);
        playButton = text("▶", 23, INK); playButton.setGravity(Gravity.CENTER); playButton.setOnClickListener(v -> controlMusic(false));
        nextButton = text("▶|", 21, INK); nextButton.setGravity(Gravity.CENTER); nextButton.setContentDescription("下一曲"); nextButton.setOnClickListener(v -> controlMusic(true));
        controls.addView(playButton, new LinearLayout.LayoutParams(0, dp(38), 1)); controls.addView(nextButton, new LinearLayout.LayoutParams(0, dp(38), 1));
        LinearLayout selector = new LinearLayout(this); selector.setGravity(Gravity.CENTER); selector.setContentDescription("选择音乐应用"); selector.setOnClickListener(v -> selectMusic());
        musicAppIcon = new ImageView(this); selector.addView(musicAppIcon, new LinearLayout.LayoutParams(dp(22), dp(22)));
        selector.addView(text("⌄", 15, INK)); controls.addView(selector, new LinearLayout.LayoutParams(0, dp(38), 1));
        content.addView(controls);
        musicTitle = text("选择音乐应用", 11, INK); musicTitle.setSingleLine(true); musicTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        musicTitle.setOnClickListener(v -> openMusic()); content.addView(musicTitle);
        box.addView(content, new LinearLayout.LayoutParams(0, -2, 1));
        box.setOnLongClickListener(v -> { selectMusic(); return true; }); return box;
    }

    private boolean hasMusicAccess() {
        String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        android.content.ComponentName own = new android.content.ComponentName(this, MusicNotificationListener.class);
        if (enabled != null) for (String item : enabled.split(":")) if (own.equals(android.content.ComponentName.unflattenFromString(item))) return true;
        return false;
    }
    private void requestMusicAccess() {
        if (hasMusicAccess() || musicAuthorizationPending) return;
        musicAuthorizationPending = true;
        adbConnection.grantMusicAccess(prefs.getString("adb_host", "127.0.0.1"), prefs.getInt("adb_port", 5555), result -> {
                    musicAuthorizationPending = false;
                    if (isFinishing() || isDestroyed()) return;
                    if (hasMusicAccess()) {
                        android.service.notification.NotificationListenerService.requestRebind(new android.content.ComponentName(this, MusicNotificationListener.class));
                        if (active) musicSession.start(prefs.getString("music_app", ""));
                        updateMusic();
                    } else new AlertDialog.Builder(this).setTitle("音乐授权未生效")
                        .setMessage("系统尚未允许通知监听。\n" + (result.isEmpty() ? "命令无输出，请检查ADB连接和系统限制。" : result))
                        .setPositiveButton("关闭", null)
                        .setNeutralButton("检查ADB", (d, i) -> showAdbConnection()).show();
                 });
    }
    private void selectMusic() {
        List<ChoicePanel.Item> choices = new ArrayList<>(); String selected = prefs.getString("music_app", "");
        choices.add(new ChoicePanel.Item("自动跟随当前播放", "使用当前活跃的媒体会话", null, selected.isEmpty(), () -> setMusicApp("")));
        for (ResolveInfo app : MusicApps.find(this, launchableApps())) {
            String name = app.activityInfo.packageName;
            choices.add(new ChoicePanel.Item(app.loadLabel(getPackageManager()).toString(), "音乐播放器", app.loadIcon(getPackageManager()),
                name.equals(selected), () -> setMusicApp(name)));
        }
        ChoicePanel.show(this, "默认音乐", "仅列出识别到的音频播放器；缺少车机播放器时可使用自动跟随", choices);
    }
    private void setMusicApp(String packageName) {
        prefs.edit().putString("music_app", packageName).apply();
        musicSession.start(packageName);
        if (!hasMusicAccess()) requestMusicAccess();
    }
    private void openMusic() {
        String name = prefs.getString("music_app", "");
        android.media.session.MediaController controller = musicSession.controller();
        if (name.isEmpty() && controller != null) name = controller.getPackageName();
        if (name.isEmpty()) selectMusic(); else launch(name);
    }
    private void controlMusic(boolean next) {
        if (!hasMusicAccess()) { requestMusicAccess(); return; }
        android.media.session.MediaController controller = musicSession.controller();
        if (controller == null) { toast("请先在音乐应用播放一首歌曲，再返回桌面"); openMusic(); return; }
        android.media.session.PlaybackState state = controller.getPlaybackState();
        boolean playing = state != null && state.getState() == android.media.session.PlaybackState.STATE_PLAYING;
        long action = next ? android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT : playing ? android.media.session.PlaybackState.ACTION_PAUSE : android.media.session.PlaybackState.ACTION_PLAY;
        try {
            if (next) controller.getTransportControls().skipToNext();
            else if (state != null && (state.getActions() & action) == 0 && (state.getActions() & android.media.session.PlaybackState.ACTION_PLAY_PAUSE) != 0) {
                controller.dispatchMediaButtonEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                controller.dispatchMediaButtonEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            }
            else if (playing) controller.getTransportControls().pause();
            else controller.getTransportControls().play();
        } catch (RuntimeException e) { toast("音乐控制失败，请重新打开音乐应用"); }
    }
    private void updateMusic() {
        android.media.session.MediaController controller = musicSession.controller();
        android.media.MediaMetadata metadata = controller == null ? null : controller.getMetadata();
        android.media.session.PlaybackState state = controller == null ? null : controller.getPlaybackState();
        String title = hasMusicAccess() ? (controller == null ? "未找到媒体会话，请先播放" : "已连接 · 等待歌曲信息") : "需要开启通知使用权";
        if (metadata != null) {
            String track = metadata.getString(android.media.MediaMetadata.METADATA_KEY_TITLE);
            String artist = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST);
            if (track != null) title = track + (artist == null || artist.isEmpty() ? "" : " · " + artist);
        }
        if (topMusic != null) topMusic.setText("   ♫  " + title);
        if (musicTitle == null) return;
        musicTitle.setText(title);
        boolean playing = state != null && state.getState() == android.media.session.PlaybackState.STATE_PLAYING;
        playButton.setText(playing ? "Ⅱ" : "▶"); playButton.setContentDescription(playing ? "暂停" : "播放");
        Bitmap cover = metadata == null ? null : metadata.getBitmap(android.media.MediaMetadata.METADATA_KEY_ART);
        if (cover == null && metadata != null) cover = metadata.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART);
        String name = prefs.getString("music_app", ""); if (name.isEmpty() && controller != null) name = controller.getPackageName();
        Drawable icon = getDrawable(android.R.drawable.ic_media_play);
        try { if (!name.isEmpty()) icon = getPackageManager().getApplicationIcon(name); } catch (PackageManager.NameNotFoundException ignored) { }
        musicAppIcon.setImageDrawable(icon);
        if (cover == null) albumArt.setImageDrawable(icon); else albumArt.setImageBitmap(cover);
        nextButton.setAlpha(controller != null ? 1f : .45f);
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
    @Override protected void onResume() {
        super.onResume(); active = true;
        if (!musicAutoAuthorizationAttempted && prefs.getBoolean("adb_auto", true)) {
            musicAutoAuthorizationAttempted = true;
            if (!hasMusicAccess()) requestMusicAccess();
        }
        Intent filterIntent = new Intent(MusicNotificationListener.ACTION_CHANGED);
        android.content.IntentFilter filter = new android.content.IntentFilter(filterIntent.getAction());
        if (android.os.Build.VERSION.SDK_INT >= 33) registerReceiver(musicUpdates, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(musicUpdates, filter);
        musicReceiverRegistered = true;
        musicSession.start(prefs.getString("music_app", ""));
        vehicleBridge.start();
        if (prefs.getBoolean("show_speed", true)) speedMonitor.start();
        if (videoWallpaper != null && videoWallpaper.getVisibility() == View.VISIBLE) videoWallpaper.start();
    }
    @Override protected void onPause() {
        active = false;
        if (musicReceiverRegistered) { unregisterReceiver(musicUpdates); musicReceiverRegistered = false; }
        musicSession.stop(); if (videoWallpaper != null) videoWallpaper.pause(); super.onPause();
        vehicleBridge.stop();
        speedMonitor.stop();
    }
    @Override protected void onDestroy() { adbConnection.close(); vehicleBridge.close(); speedMonitor.stop(); musicSession.stop(); if (videoWallpaper != null) videoWallpaper.stopPlayback(); super.onDestroy(); }
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
    private void rebuildHome() {
        if (videoWallpaper != null) videoWallpaper.stopPlayback(); buildHome(); hideSystemBars();
        if (active) { if (prefs.getBoolean("show_speed", true)) speedMonitor.start(); else speedMonitor.stop(); }
    }
    private void requestSpeedPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this).setTitle("GPS车速")
                .setMessage("需要精确定位权限和开启GPS。仅在桌面前台获取速度；定位失效时显示 --，不可替代原车仪表。")
                .setPositiveButton("授权", (d, i) -> requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 103))
                .setNegativeButton("取消", null).show();
        } else {
            try { startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); }
            catch (ActivityNotFoundException e) { toast("系统未提供定位设置"); }
        }
    }
    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);
        if (request == 103 && active && prefs.getBoolean("show_speed", true)) speedMonitor.start();
    }

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

    private void showSettings() {
        SettingsPanel panel = new SettingsPanel(this, prefs, this::rebuildHome);
        panel.category("显示设置");
        panel.toggle("桌面大时钟", "默认显示时间与日期；保留已保存的开关", "clock", true);
        panel.toggle("实时车速", "左上角显示GPS速度，仅供辅助参考", "show_speed", true);
        panel.toggle("顶部状态栏", "显示应用提供的时间与音乐信息", "top_bar", true);
        panel.toggle("底部导航栏", "显示应用内的快捷按钮", "bottom_bar", true);
        panel.category("壁纸设置");
        panel.action("图片壁纸", "从本地选择一张图片", "选择图片", this::chooseWallpaper);
        panel.action("动态壁纸", "本地视频静音循环播放", "选择视频", () -> pickWallpaper(true));
        panel.action("默认壁纸", "恢复内置浅色背景", "恢复", () -> { prefs.edit().remove("wallpaper").remove("video_wallpaper").apply(); rebuildHome(); });
        panel.category("桌面卡片");
        panel.action("卡片管理", "选择显示的卡片并调整顺序", "管理", this::showCardEditor);
        panel.category("按键设置");
        panel.action("右侧功能按钮", "显示、排序并绑定应用；也可长按按键", "管理", this::editNavOrder);
        panel.category("导航与音乐");
        panel.action("导航与音乐真实分屏", "实验：系统多窗口，需停车配置；非桌面嵌入", "设置", this::showSplitSettings);
        panel.action("默认导航应用", "选择高德、百度或腾讯地图", "选择", this::selectNavigation);
        panel.action("默认音乐应用", "指定播放器或跟随当前播放", "选择", this::selectMusic);
        panel.action("音乐通知使用权", "自动ADB授权；可访问通知和媒体信息，不清除通知", "授权", this::requestMusicAccess);
        panel.action("音乐连接诊断", "检查权限和当前媒体会话", "查看", () -> new AlertDialog.Builder(new android.view.ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog_Alert)).setTitle("音乐连接诊断")
            .setMessage("通知使用权：" + (hasMusicAccess() ? "已开启" : "未开启") + "\n" + musicSession.diagnostics())
            .setPositiveButton("关闭", null).show());
        panel.category("系统设置");
        panel.toggle("启动时自动连接ADB", "自动尝试本机调试，首次在系统弹窗始终允许", "adb_auto", true);
        panel.action("无线ADB连接", "本机TCP调试连接、密钥授权与辅助进程", "连接", this::showAdbConnection);
        panel.action("车辆只读桥接", "查看辅助进程连接与车辆接口原始数据", "查看", this::showVehicleBridge);
        panel.action("系统导航授权", "启用全局返回与系统多任务，不读取屏幕", "设置", this::requestNavigationAccess);
        panel.action("车速定位权限", "开启精确定位与GPS，点击车速也可设置", "设置", this::requestSpeedPermission);
        panel.action("车辆车速接口检测", "对比应用直读与ADB辅助进程结果", "查看", () -> new AlertDialog.Builder(this).setTitle("车速接口检测")
            .setMessage("应用直接读取：" + vehicleSpeedProbe + "\n\nADB辅助进程：" + (vehicleBridge.data() == null ? "未收到桥接数据" : vehicleBridge.data().optString("speed_raw", vehicleBridge.data().optString("speed_raw_error", "未返回")))
                + "\n\n当前仍使用GPS；原始值需核实单位及实时性。")
            .setPositiveButton("关闭", null).show());
        panel.action("Android 系统设置", "打开设备的系统设置页面", "打开", () -> { try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (ActivityNotFoundException e) { toast("系统设置不可用"); } });
        panel.show();
    }
    private void showSplitSettings() {
        List<ChoicePanel.Item> items = new ArrayList<>();
        String map = prefs.getString("navigation_app", ""), music = prefs.getString("music_app", "");
        if (music.isEmpty() && musicSession.controller() != null) music = musicSession.controller().getPackageName();
        final String player = music;
        if (map.isEmpty()) {
            items.add(new ChoicePanel.Item("先选择默认地图", "需指定导航应用", null, false, this::selectNavigation));
        }
        if (player.isEmpty()) {
            items.add(new ChoicePanel.Item("先选择音乐播放器", "需指定音乐应用或先播放歌曲", null, false, this::selectMusic));
        }
        if (!map.isEmpty() && !player.isEmpty()) {
            items.add(new ChoicePanel.Item("音乐左 / 地图右", "比例由系统决定，暂不支持指定", null, false, () -> launchSystemSplit(player, map, 50)));
            items.add(new ChoicePanel.Item("地图左 / 音乐右", "比例由系统决定，暂不支持指定", null, false, () -> launchSystemSplit(map, player, 50)));
        }
        ChoicePanel.show(this, "真实分屏（实验）", "仅导航和音乐。使用系统分屏，桌面底栏不会覆盖其他应用；Home可返回桌面", items);
    }
    private void launchSystemSplit(String left, String right, int percent) {
        Intent a = getPackageManager().getLaunchIntentForPackage(left), b = getPackageManager().getLaunchIntentForPackage(right);
        if (a == null || b == null || a.getComponent() == null || b.getComponent() == null) { toast("应用不可启动，请重新选择"); return; }
        new AlertDialog.Builder(this).setTitle("启动系统分屏")
            .setMessage("请仅在停车时配置。将启动两个真实应用，比例和触控由DiLink多窗口管理；不支持时可能只打开其中一个应用。可用系统Home或最近任务退出分屏。")
            .setPositiveButton("启动", (d, i) -> adbConnection.startSplit(prefs.getString("adb_host", "127.0.0.1"), prefs.getInt("adb_port", 5555),
                a.getComponent(), b.getComponent(), getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, percent, result -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (!result.contains("LEFT_RESULT:0") || !result.contains("RIGHT_RESULT:0") || result.contains("Error") || result.contains("Exception"))
                        new AlertDialog.Builder(this).setTitle("系统分屏返回结果").setMessage(result.isEmpty() ? "未收到结果" : result).setPositiveButton("关闭", null).show();
                }))
            .setNegativeButton("取消", null).show();
    }
    private void showVehicleBridge() {
        new AlertDialog.Builder(this).setTitle("车辆只读桥接（实验）")
            .setMessage(vehicleBridge.diagnostics() + "\n\nADB状态与启动输出：\n" + adbConnection.diagnostics() + "\n\n桥接运行30分钟后退出，需手动重新连接。接口数据为未确认单位的原始值，不用于仪表或车控。")
            .setPositiveButton("无线ADB", (dialog, which) -> showAdbConnection())
            .setNeutralButton("刷新", (dialog, which) -> showVehicleBridge())
            .setNegativeButton("关闭", null).show();
    }
    private void showAdbConnection() {
        android.content.Context themed = new android.view.ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        LinearLayout content = new LinearLayout(themed); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(20), dp(8), dp(20), dp(12));
        TextView note = new TextView(themed); note.setText("Android 10传统TCP ADB（非配对码/TLS）。仅连接本机，不启用调试、不更改系统设置。首次需允许调试密钥。5555只是常见默认值，请填写车机实际端口。");
        note.setText("默认启动时自动连接，无需填写。首次请在系统调试授权弹窗勾选“始终允许”并确认。只有默认连接失败时才需要展开高级配置。仅支持本机传统TCP ADB，不支持配对码/TLS。");
        note.setTextColor(INK); note.setTextSize(13); content.addView(note);
        TextView advanced = new TextView(themed); advanced.setText("高级配置：地址与端口 ▾"); advanced.setTextColor(0xff087bff); advanced.setPadding(0, dp(12), 0, dp(8)); content.addView(advanced);
        LinearLayout advancedFields = new LinearLayout(themed); advancedFields.setOrientation(LinearLayout.VERTICAL); advancedFields.setVisibility(View.GONE); content.addView(advancedFields);
        android.widget.EditText host = new android.widget.EditText(themed); host.setSingleLine(); host.setHint("本机地址");
        host.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        host.setText(prefs.getString("adb_host", "127.0.0.1")); advancedFields.addView(host);
        android.widget.EditText port = new android.widget.EditText(themed); port.setSingleLine(); port.setHint("TCP端口");
        port.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); port.setText(String.valueOf(prefs.getInt("adb_port", 5555))); advancedFields.addView(port);
        advanced.setOnClickListener(v -> advancedFields.setVisibility(advancedFields.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        TextView status = new TextView(themed); status.setTextColor(INK); status.setTextSize(13); status.setPadding(0, dp(12), 0, dp(8)); status.setText(adbConnection.state()); content.addView(status);
        LinearLayout actions = new LinearLayout(themed);
        android.widget.Button connect = new android.widget.Button(themed); connect.setText("连接并启动");
        android.widget.Button disconnect = new android.widget.Button(themed); disconnect.setText("断开ADB");
        actions.addView(connect, new LinearLayout.LayoutParams(0, -2, 1)); actions.addView(disconnect, new LinearLayout.LayoutParams(0, -2, 1)); content.addView(actions);
        connect.setOnClickListener(v -> {
            int number;
            try { number = Integer.parseInt(port.getText().toString().trim()); if (number < 1 || number > 65535) throw new NumberFormatException(); }
            catch (NumberFormatException error) { port.setError("请输入1–65535"); return; }
            String address = host.getText().toString().trim(); if (address.isEmpty()) { host.setError("请输入本机地址"); return; }
            prefs.edit().putString("adb_host", address).putInt("adb_port", number).apply(); adbConnection.connect(address, number);
        });
        disconnect.setOnClickListener(v -> adbConnection.disconnect());
        adbConnection.observe(() -> status.setText(adbConnection.state()));
        ScrollView scroll = new ScrollView(themed); scroll.addView(content);
        AlertDialog dialog = new AlertDialog.Builder(themed).setTitle("应用内无线ADB").setView(scroll)
            .setPositiveButton("关闭页面", null).setNeutralButton("桥接诊断", (d, which) -> showVehicleBridge()).create();
        dialog.setOnDismissListener(d -> adbConnection.observe(null)); dialog.show();
    }
}
