package com.billy.cc.core.ipc;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BundleCompat;

import com.billy.cc.core.ipc.inner.InnerProvider;

import java.util.Arrays;

/**
 * 通过Provider#call()实现跨进程方法调用
 *
 * @author 喵叔catuncle    2020/5/20 0020
 */
public class IPCProvider extends InnerProvider {

	public static final String ARG_EXTRAS_REQUEST = "request";
    public static final String ARG_EXTRAS_CALLBACK = "callback";
    public static final String ARG_EXTRAS_RESULT = "result";

    private Handler mainHandler;

    @Override
    public boolean onCreate() {
        mainHandler = new Handler(Looper.getMainLooper());
        return super.onCreate();
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable final Bundle extras) {
        CP_Util.log("receive call from other process. method = %s, arg = %s, extras = %s", method, arg, extras);
        if (extras != null) {
            extras.setClassLoader(getClass().getClassLoader());
            CP_Util.log("receive call from other process. extras.keySet() = %s", Arrays.asList(extras.keySet().toArray()));
            IPCRequest request = extras.getParcelable(ARG_EXTRAS_REQUEST);

            if (request != null) {
                IRemoteCallback callback = getRemoteCallback(extras);

                Task task = new Task(request, callback);

                if (request.isMainThreadSyncCall()) {
                    mainHandler.post(task);
                } else {
                    IPCCaller.support.threadPool(task);
                }

                if (callback != null) {
                    CP_Util.log("dispatch call ...");
                    return Bundle.EMPTY;
                } else {
                    task.wait4Result();//异步转同步
                    return task.syncRet;
                }
            } else {
                CP_Util.logError("receive call from other process. request = null null null");
                return Bundle.EMPTY;
            }
        } else {
            CP_Util.logError("receive call from other process. extras = null null null");
            return Bundle.EMPTY;
        }
    }

    private IRemoteCallback getRemoteCallback(Bundle extras) {
        IRemoteCallback callback = null;
        if (extras.containsKey(ARG_EXTRAS_CALLBACK)) {
            IBinder iBinder = (BundleCompat.getBinder(extras, ARG_EXTRAS_CALLBACK));
            callback = IRemoteCallback.Stub.asInterface(iBinder);
        }
        return callback;
    }

    /**
     * 把请求包装成任务，交给TaskDispatcher来分发处理
     */
    static class Task implements Runnable {
        private IPCRequest request;
        private IRemoteCallback callback;
        private Bundle syncRet = Bundle.EMPTY;

        Task(IPCRequest request, IRemoteCallback callback) {
            this.request = request;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (CP_Util.VERBOSE_LOG) {
                CP_Util.verboseLog("IPC Task run with: %s", request);
            }

            Bundle remoteResult = new Bundle();
            IPCCaller.support.runAction(request, remoteResult);

            if (callback != null) {
                try {
                    callback.callback(remoteResult);
                } catch (RemoteException e) {
                    CP_Util.printStackTrace(e);
                    CP_Util.log("remote doCallback failed!");
                }
            } else {
                setResult4Waiting(remoteResult);
            }
        }

        synchronized void wait4Result() {
            this.syncRet = new Bundle();
            try {
                CP_Util.verboseLog("start waiting >>>");
                // FIXME: 假设同步调用最长等5秒 有没有更好的方案？跨进程调用更通用。
                this.wait(5000);
                CP_Util.verboseLog("end waiting <<<");
            } catch (InterruptedException ignored) {
                if (CP_Util.VERBOSE_LOG) {
                    CP_Util.logError("wait4Result interrupted");
                }
                syncRet = Bundle.EMPTY;
            }
        }

        private synchronized void setResult4Waiting(Bundle remoteResult) {
            if (CP_Util.VERBOSE_LOG) {
                CP_Util.verboseLog("setResult4Waiting remoteResult = %s", remoteResult);
            }
            try {
                syncRet = remoteResult;
                this.notifyAll();
            } catch (Exception e) {
                CP_Util.printStackTrace(e);
            }
        }
    }

}
