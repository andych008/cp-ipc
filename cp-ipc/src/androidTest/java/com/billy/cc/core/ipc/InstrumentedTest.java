package com.billy.cc.core.ipc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static com.billy.cc.core.ipc.IPCProvider.ARG_EXTRAS_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * IPCCaller单元测试
 *
 * @author 喵叔catuncle    2020/5/16 0016
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    private static final String TARGET_APP = "com.example.target";
    private static final String TARGET_COMPONENT = "demo.ComponentA";


    @BeforeClass
    public static void startHost() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            appContext.getPackageManager().getPackageInfo(TARGET_APP, 0);
        } catch (PackageManager.NameNotFoundException e) {
            fail("请先安装app-target，以配合本单元测试的运行");
            e.printStackTrace();
            throw e;
        }


        //初始化IPCCaller
        IPCCaller.setSupport(new IPCCaller.IPCSupport() {
            @Override
            public String uriFormat() {
                return "content://%s.provider";
            }

            @Override
            public void threadPool(Runnable runnable) {
            }

            @Override
            public void runAction(IPCRequest request, Bundle remoteResult) {
            }
        });
    }

    @Test
    public void useAppContext() throws Exception {
        printLine();
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.billy.cc.core.ipc.test", appContext.getPackageName());
    }


    @Test
    public void call() throws Exception {
        printLine();
        Context appContext = InstrumentationRegistry.getTargetContext();

        HashMap<String, Object> params = new HashMap<String, Object>();
        IPCRequest request = new IPCRequest.Builder()
                .initTask(TARGET_COMPONENT, "getInfo", params, "testId_asyncCall")
                .build();
        Bundle resultBundle = IPCCaller.call(appContext, TARGET_APP, request);
        printResult(resultBundle);
        String ret = resultBundle.getString(ARG_EXTRAS_RESULT);
        assertEquals(TARGET_COMPONENT + " - " + "getInfo", ret);
    }

    @Test
    public void callAsync() throws Exception {
        printLine();
        Context appContext = InstrumentationRegistry.getTargetContext();
        final String[] ret = {null};

        HashMap<String, Object> params = new HashMap<String, Object>();
        IPCRequest request = new IPCRequest.Builder()
                .initTask(TARGET_COMPONENT, "getInfo", params, "testId_asyncCall")
                .build();
        IPCCaller.callAsync(appContext, TARGET_APP, request, new IPCCaller.ICallback() {
            @Override
            public void onResult(Bundle resultBundle) {
                printResult(resultBundle);
                assertTrue(Thread.currentThread().getName().contains("Binder"));//callback默认在binder线程, Binder:xxx
                ret[0] = resultBundle.getString(ARG_EXTRAS_RESULT);
                synchronized (ret) {
                    ret.notify();
                }
            }
        });

        synchronized (ret) {
            ret.wait();
        }
        assertEquals("demo.ComponentA - getInfo", ret[0]);
    }

    @Test
    public void callAsyncMainThread() throws Exception {
        printLine();
        Context appContext = InstrumentationRegistry.getTargetContext();
        final String[] ret = {null};

        HashMap<String, Object> params = new HashMap<String, Object>();
        IPCRequest request = new IPCRequest.Builder()
                .initTask(TARGET_COMPONENT, "getInfo", params, "testId_asyncCall_MainThread")
                .mainThreadSyncCall(true)
                .build();
        IPCCaller.callAsync(appContext, TARGET_APP, request, new IPCCaller.ICallback() {
            @Override
            public void onResult(Bundle resultBundle) {
                printResult(resultBundle);
                assertTrue(Thread.currentThread().getName().contains("Binder"));//callback默认在binder线程, Binder:xxx
                ret[0] = resultBundle.getString(ARG_EXTRAS_RESULT);
                synchronized (ret) {
                    ret.notify();
                }
            }
        });

        synchronized (ret) {
            ret.wait();
        }
        assertEquals("demo.ComponentA - getInfo", ret[0]);
    }

    @Test
    public void callAsync2() throws Exception {
        printLine();
        final String callbackThreadName = "unit_test_callback";
        HandlerThread thread = new HandlerThread(callbackThreadName);
        thread.start();

        Context appContext = InstrumentationRegistry.getTargetContext();
        final String[] ret = {null};

        HashMap<String, Object> params = new HashMap<String, Object>();
        IPCRequest request = new IPCRequest.Builder()
                .initTask(TARGET_COMPONENT, "getInfo", params, "testId_asyncCall2")
                .build();
        IPCCaller.callAsyncCallbackOnHandler(appContext, TARGET_APP, request, new Handler(thread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                Bundle resultBundle = msg.getData();
                printResult(resultBundle);
                assertEquals(callbackThreadName, Thread.currentThread().getName());//callback在通过Handler指定的线程
                ret[0] = resultBundle.getString(ARG_EXTRAS_RESULT);
                synchronized (ret) {
                    ret.notify();
                }
            }
        });

        synchronized (ret) {
            ret.wait();
        }
        assertEquals("demo.ComponentA - getInfo", ret[0]);
    }


    private void printResult(Bundle resultBundle) {
        if (resultBundle != null) {
            StringBuilder sb = new StringBuilder();
            for (String key : resultBundle.keySet()) {
                sb.append(String.format("[%s : %s]\n", key, resultBundle.get(key)));
            }
            CP_Util.log("resultBundle = %s", sb);
        }
    }

    private void printLine() {
        StackTraceElement[] stackTrace =  Thread.currentThread().getStackTrace();
        CP_Util.log("\n-------------------------[ %s ]-----------------------------\n", stackTrace[3].getMethodName());
    }
}
