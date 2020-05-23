package com.example.myapplication;

import android.app.Application;

import com.billy.cc.core.ipc.IPCCaller;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * 跨进程通信的client端
 *
 * @author 喵叔catuncle    2020/5/16 0016
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                    .methodCount(1)         // (Optional) How many method line to show. Default 2
                    .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
                    .tag("Andy")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build();

            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, String tag, @NotNull String message, Throwable t) {
                    Logger.log(priority, tag, message, t);
                }
            });
        }

        IPCCaller.enableDebug(true);

        IPCCaller.enableVerboseLog(true);
    }
}
