package com.cardesk.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Uses music intent handlers, app categories and known players, not every launcher app. */
final class MusicApps {
    static List<ResolveInfo> find(Context context, List<ResolveInfo> launchers) {
        PackageManager pm = context.getPackageManager(); Set<String> packages = new HashSet<>();
        collect(packages, pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC), 0));
        collect(packages, pm.queryIntentActivities(new Intent(Intent.ACTION_VIEW).setDataAndType(android.net.Uri.parse("content://com.cardesk.home/audio/sample"), "audio/mpeg"), 0));
        List<ResolveInfo> services = pm.queryIntentServices(new Intent("android.media.browse.MediaBrowserService"), 0);
        for (ResolveInfo service : services) if (service.serviceInfo != null) packages.add(service.serviceInfo.packageName);
        String[] known = {"com.netease.cloudmusic", "com.netease.cloudmusic.car",
            "com.tencent.qqmusic", "com.tencent.qqmusiccar", "com.tencent.qqmusiccar2", "com.kugou.android", "com.kugou.android.auto",
            "cn.kuwo.player", "cn.kuwo.kwmusiccar", "cn.kuwo.kwmusiccaz", "com.spotify.music", "com.apple.android.music", "com.maxmpz.audioplayer", "com.byd.music"};
        java.util.Collections.addAll(packages, known);
        Set<String> seen = new HashSet<>(); List<ResolveInfo> result = new ArrayList<>();
        for (ResolveInfo app : launchers) {
            String name = app.activityInfo.packageName; ApplicationInfo info = app.activityInfo.applicationInfo;
            if (context.getPackageName().equals(name) || !seen.add(name)) continue;
            if (name.startsWith("cn.kuwo.") || packages.contains(name)
                    || (android.os.Build.VERSION.SDK_INT >= 26 && info.category == ApplicationInfo.CATEGORY_AUDIO)) result.add(app);
        }
        return result;
    }
    private static void collect(Set<String> result, List<ResolveInfo> apps) {
        for (ResolveInfo app : apps) if (app.activityInfo != null) result.add(app.activityInfo.packageName);
    }
}
