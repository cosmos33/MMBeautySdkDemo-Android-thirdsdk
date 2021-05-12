package com.qiniu.pili.droid.streaming.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.qiniu.pili.droid.streaming.StreamingEnv;
import com.qiniu.pili.droid.streaming.demo.service.KeepAppAliveService;
import com.qiniu.pili.droid.streaming.demo.utils.AppStateTracker;
import com.qiniu.pili.droid.streaming.demo.utils.Util;

public class StreamingApplication extends Application {

    private boolean mIsServiceAlive;
    private Intent mServiceIntent;
    private Context context;

    private static class StreamingApplicationHolder {
        static StreamingApplication streamingApplication = new StreamingApplication();
    }

    public static StreamingApplication getInstance() {
        return StreamingApplicationHolder.streamingApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
        /**
         * init must be called before any other func
         */
        StreamingEnv.init(context, Util.getUserId(context));

        /**
         * track app background state to avoid possibly stopping microphone recording
         * in screen streaming mode on Android P+
         */
        AppStateTracker.track(this, new AppStateTracker.AppStateChangeListener() {
            @Override
            public void appTurnIntoForeground() {
                stopService();
            }

            @Override
            public void appTurnIntoBackGround() {
                startService();
            }

            @Override
            public void appDestroyed() {
                stopService();
            }
        });
    }

    /**
     * start foreground service to make process not turn to idle state
     * <p>
     * on Android P+, it doesn't allow recording audio to protect user's privacy in idle state.
     */
    private void startService() {
        if (mServiceIntent == null) {
            mServiceIntent = new Intent(context, KeepAppAliveService.class);
        }
        context.startService(mServiceIntent);
        mIsServiceAlive = true;
    }

    private void stopService() {
        if (mIsServiceAlive) {
            context.stopService(mServiceIntent);
            mServiceIntent = null;
            mIsServiceAlive = false;
        }
    }
}
