package com.qiniu.pili.droid.streaming.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Random;

public class Util {

    public static boolean isSupportScreenCapture() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isSupportHWEncode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static void showToast(Activity activity, String msg) {
        showToast(activity, msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(final Activity activity, final String msg, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, duration).show();
            }
        });
    }

    public static String syncRequest(String appServerUrl) {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(appServerUrl).openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(10000);
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int length = httpConn.getContentLength();
            if (length <= 0) {
                length = 16 * 1024;
            }
            InputStream is = httpConn.getInputStream();
            byte[] data = new byte[length];
            int read = is.read(data);
            is.close();
            if (read <= 0) {
                return null;
            }
            return new String(data, 0, read);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ??????????????? DNS ?????????????????????
     * <p>
     * ??????????????? 114 dns ????????????????????????????????????????????????????????????????????? ip ??????????????????????????????????????????????????????????????????????????????
     * ??????????????????????????? DNS ????????????????????????????????? 114 dns ??????
     */
    public static DnsManager getMyDnsManager(Context context) {
        IResolver r0 = null;
        IResolver r1 = new DnspodFree();
        IResolver r2 = AndroidDnsServer.defaultResolver(context);
        try {
            r0 = new Resolver(InetAddress.getByName("119.29.29.29"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new DnsManager(NetworkInfo.normal, new IResolver[]{r0, r1, r2});
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE);
        String userId = preferences.getString(Config.KEY_USER_ID, "");
        if ("".equals(userId)) {
            userId = userId();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Config.KEY_USER_ID, userId);
            editor.apply();
        }
        return userId;
    }

    private static String userId() {
        Random r = new Random();
        return System.currentTimeMillis() + "" + r.nextInt(999);
    }
}
