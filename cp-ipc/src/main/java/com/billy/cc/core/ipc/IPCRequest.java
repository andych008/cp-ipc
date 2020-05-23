package com.billy.cc.core.ipc;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * IPC请求对象
 *
 * @author 喵叔catuncle    2020/5/20 0020
 */
public class IPCRequest implements Parcelable {

    public static final class Builder {

        private String componentName;
        private String actionName;
        private HashMap<String, Object> params;
        private String callId;
        private boolean isMainThreadSyncCall;
        private Bundle extra;

        public Builder() {
        }

        public Builder initTask(String componentName, String actionName, HashMap<String, Object> params, String callId) {
            this.componentName = componentName;
            this.actionName = actionName;
            this.params = params;
            this.callId = callId;
            return this;
        }

        public Builder mainThreadSyncCall(boolean isMainThreadSyncCall) {
            this.isMainThreadSyncCall = isMainThreadSyncCall;
            return this;
        }

        public Builder extra(Bundle extra) {
            this.extra = extra;
            return this;
        }

        public IPCRequest build() {
            return new IPCRequest(this);
        }
    }


    private String componentName;
    private String actionName;
    private HashMap<String, Object> params;
    private String callId;
    private boolean isMainThreadSyncCall;
    //请求对象扩展(供使用方定义自己特有的功能)
    protected Bundle extra;

    public IPCRequest(Builder builder) {
        this.componentName = builder.componentName;
        this.actionName = builder.actionName;
        this.params = builder.params;
        this.callId = builder.callId;
        this.isMainThreadSyncCall = builder.isMainThreadSyncCall;
        this.extra = builder.extra;
    }


    protected IPCRequest(Parcel in) {
        componentName = in.readString();
        actionName = in.readString();
        callId = in.readString();
        isMainThreadSyncCall = in.readByte() != 0;
        params = (HashMap<String, Object>) in.readSerializable();
        extra = in.readBundle(getClass().getClassLoader());
    }

    @Override
    public String toString() {
        return "IPCRequest{" +
                "componentName='" + componentName + '\'' +
                ", actionName='" + actionName + '\'' +
                ", params=" + params +
                ", callId='" + callId + '\'' +
                ", isMainThreadSyncCall=" + isMainThreadSyncCall +
                ", extra=" + bundle2Str(extra) +
                '}';
    }

    private String bundle2Str(Bundle bundle) {
        if (bundle != null) {
            StringBuilder sb = new StringBuilder();
            for (String key : bundle.keySet()) {
                sb.append(String.format("[%s : %s]", key, bundle.get(key)));
            }
            return sb.toString();
        }
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(componentName);
        dest.writeString(actionName);
        dest.writeString(callId);
        dest.writeByte((byte) (isMainThreadSyncCall ? 1 : 0));
        dest.writeSerializable(params);
        dest.writeBundle(extra);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IPCRequest> CREATOR = new Creator<IPCRequest>() {
        @Override
        public IPCRequest createFromParcel(Parcel in) {
            return new IPCRequest(in);
        }

        @Override
        public IPCRequest[] newArray(int size) {
            return new IPCRequest[size];
        }
    };

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public boolean isMainThreadSyncCall() {
        return isMainThreadSyncCall;
    }

    public void setMainThreadSyncCall(boolean mainThreadSyncCall) {
        isMainThreadSyncCall = mainThreadSyncCall;
    }

    public Bundle getExtra() {
        return extra;
    }

    public void setExtra(Bundle extra) {
        this.extra = extra;
    }
}
