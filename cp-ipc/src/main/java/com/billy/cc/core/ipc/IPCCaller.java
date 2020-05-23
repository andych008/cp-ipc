package com.billy.cc.core.ipc;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.BundleCompat;

import java.util.HashMap;

import static com.billy.cc.core.ipc.IPCProvider.ARG_EXTRAS_CALLBACK;
import static com.billy.cc.core.ipc.IPCProvider.ARG_EXTRAS_REQUEST;

/**
 * 通过ContentProvider实现的跨进程方法调用(适用于组件化框架)
 *
 * @author 喵叔catuncle    2020/5/15 0015
 */
public class IPCCaller {

    volatile static IPCSupport support;

    /**
     * 同步调用
     */
    public static Bundle call(Context context, String pkg, IPCRequest request) {
        Bundle extras = new Bundle();
        extras.putParcelable(ARG_EXTRAS_REQUEST, request);
        Bundle remoteResult = doIpc(context, pkg, extras);
        if (remoteResult != null) {
            remoteResult.setClassLoader(IPCCaller.class.getClassLoader());
        }
        return remoteResult;
    }

    /**
     * 异步调用(不监听回调)
     */
    public static void callAsync(Context context, String pkg, IPCRequest request) {
        callAsync(context, pkg, request, null);
    }

    /**
     * 异步调用(回调线程：不做处理，默认在binder线程)
     */
    public static void callAsync(Context context, String pkg, IPCRequest request, final ICallback callback) {
        Bundle extras = new Bundle();
        extras.putParcelable(ARG_EXTRAS_REQUEST, request);
        if (callback != null) {
            BundleCompat.putBinder(extras, ARG_EXTRAS_CALLBACK, new IRemoteCallback.Stub() {
                @Override
                public void callback(Bundle remoteResult) {
                    if (remoteResult != null) {
                        remoteResult.setClassLoader(getClass().getClassLoader());
                    }
                    callback.onResult(remoteResult);
                }
            });
        }
        doIpc(context, pkg, extras);
    }

    /**
     * 异步调用(回调线程：通过handler指定)
     */
    public static void callAsyncCallbackOnHandler(Context context, String pkg, IPCRequest request, final Handler callbackHandler) {
        Bundle extras = new Bundle();
        extras.putParcelable(ARG_EXTRAS_REQUEST, request);
        if (callbackHandler != null) {
            BundleCompat.putBinder(extras, ARG_EXTRAS_CALLBACK, new IRemoteCallback.Stub() {
                @Override
                public void callback(Bundle remoteResult) {
                    if (remoteResult != null) {
                        remoteResult.setClassLoader(getClass().getClassLoader());
                    }
                    Message msg = callbackHandler.obtainMessage();
                    msg.setData(remoteResult);
                    msg.sendToTarget();
                }
            });
        }
        doIpc(context, pkg, extras);
    }

    /**
     * doIpc()本身是同步调用，而且是跨进程的。所以，如果在主线程执行callAsync()可以通过一个Dispatcher线程来执行，从而减少主线程的耗时。
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private static Bundle doIpc(Context context, String pkg, Bundle extras) {
        Bundle result = Bundle.EMPTY;
        Uri uri = Uri.parse(String.format(IPCCaller.support.uriFormat(), pkg));
        try {
            int tryMax = 5;
            do {
                String method = "";
                String arg = "{}";
                if (CP_Util.VERBOSE_LOG) {
                    IPCRequest request = extras.getParcelable(ARG_EXTRAS_REQUEST);
                    method = String.format("%s#%s", request.getComponentName(), request.getActionName());
                    HashMap<String, Object> params = request.getParams();
                    if (params != null) {
                        arg = params.toString();
                    }
                }
                result = context.getContentResolver().call(uri, method, arg, extras);//extras非空表示异步请求
                if (result != null) {
                    break;
                } else {
                    if (CP_Util.VERBOSE_LOG) {
                        CP_Util.verboseLog("tryMax = %s", tryMax);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (tryMax-- > 0);
        } catch (Exception e) {
            CP_Util.logError("ipc call error : %s", e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 开关debug模式（打印日志），默认为关闭状态
     */
    public static void enableDebug(boolean enable) {
        CP_Util.DEBUG = enable;
    }

    /**
     * 开关调用过程详细日志，默认为关闭状态
     */
    public static void enableVerboseLog(boolean enable) {
        CP_Util.VERBOSE_LOG = enable;
    }

    public static void setSupport(IPCSupport support) {
        CP_Util.log("IPCSupport = %s", support.getClass().getName());
        IPCCaller.support = support;
    }


    public interface ICallback {
        void onResult(Bundle resultBundle);
    }

    public interface IPCSupport {

        /**
         * 约定访问IPCProvider的uri格式
         * <hr/>
         * <p>
         *     假设包名为 com.example.application
         *     <br/>
         *     provider的android:authorities="com.example.application.provider"
         *     <br/>
         *     那么uriFormat就定义为"content://%s.provider";
         * </p>
         */
        String uriFormat();

        /**
         * 任务放入指定线程
         */
        void threadPool(Runnable runnable);

        /**
         * 执行任务
         */
        void runAction(IPCRequest request, Bundle remoteResult);
    }
}
