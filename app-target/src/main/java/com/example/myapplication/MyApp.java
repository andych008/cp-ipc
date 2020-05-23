package com.example.myapplication;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import com.billy.cc.core.ipc.CP_Util;
import com.billy.cc.core.ipc.IPCCaller;
import com.billy.cc.core.ipc.IPCRequest;
import com.example.base.MyIPCRequest;

import static com.billy.cc.core.ipc.IPCProvider.ARG_EXTRAS_RESULT;

/**
 * 跨进程通信的server端
 *
 * @author 喵叔catuncle    2020/5/16 0016
 */
public class MyApp extends Application {

    //任务处理线程（建议用线程池，HandlerThread只是demo）
    private Handler workerHandler;

    public void prepare() {
        HandlerThread worker = new HandlerThread("CP_WORKER");
        worker.start();
        workerHandler = new Handler(worker.getLooper());

        //任务怎么处理应该分发给业务方决定
        IPCCaller.setSupport(new IPCCaller.IPCSupport() {
            @Override
            public String uriFormat() {
                return "content://%s.provider";
            }

            @Override
            public void threadPool(Runnable runnable) {
                workerHandler.post(runnable);
            }

            @Override
            public void runAction(IPCRequest request, Bundle remoteResult) {
                String component = request.getComponentName();
                String action = request.getActionName();
                Bundle extra = request.getExtra();
                if (extra != null) {
                    boolean isCmd = extra.getBoolean(MyIPCRequest.EXTRAS_IS_CMD);
                    CP_Util.log("extra.isCmd --- %s", isCmd);
                }

                // 2020/5/15 0015 mock
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    remoteResult.putString(ARG_EXTRAS_RESULT, component + " - " + action);
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IPCCaller.enableDebug(true);

        IPCCaller.enableVerboseLog(true);

        prepare();
    }
}
