package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.billy.cc.core.ipc.CP_Util;
import com.billy.cc.core.ipc.IPCCaller;
import com.billy.cc.core.ipc.IPCRequest;
import com.example.base.MyIPCRequest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.billy.cc.core.ipc.IPCProvider.ARG_EXTRAS_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * MyIPCRequest单元测试
 *
 * @author 喵叔catuncle    2020/5/16 0016
 */
@RunWith(AndroidJUnit4.class)
public class MyIPCRequestTest {
    private static final String TARGET_APP = "com.example.target";


    @BeforeClass
    public static void startHost() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        try {
            appContext.getPackageManager().getPackageInfo(TARGET_APP, 0);
        } catch (Exception e) {
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

        assertEquals("com.example.myapplication", appContext.getPackageName());
    }

    @Test
    public void testCmdAsync() throws Exception {
        printLine();
        Context appContext = InstrumentationRegistry.getTargetContext();
        final String[] ret = {null};

        IPCRequest request = MyIPCRequest.createCancelCmd("testId_testCmdAsync");

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
        assertEquals("null - cmd_action_cancel", ret[0]);
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
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        CP_Util.log("\n-------------------------[ %s ]-----------------------------\n", stackTrace[1].getMethodName());
    }
}
