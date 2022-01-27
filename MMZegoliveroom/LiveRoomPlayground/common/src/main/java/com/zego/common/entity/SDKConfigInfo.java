package com.zego.common.entity;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.zego.common.BR;


/**
 * Created by zego on 2019/3/20.
 */

public class SDKConfigInfo extends BaseObservable {

    private boolean enableCamera = true;
    private boolean enableMic = true;
    private boolean speaker = true;
    private boolean enableMirror = false;

    @Bindable
    public boolean isSpeaker() {
        return speaker;
    }

    public void setSpeaker(boolean mute) {
        this.speaker = mute;
        notifyPropertyChanged(BR.speaker);
    }

    @Bindable
    public boolean isEnableCamera() {
        return enableCamera;
    }

    public void setEnableCamera(boolean enableCamera) {
        this.enableCamera = enableCamera;
        notifyPropertyChanged(BR.enableCamera);
    }

    @Bindable
    public boolean isEnableMic() {
        return enableMic;
    }

    public void setEnableMic(boolean enableMic) {
        this.enableMic = enableMic;
        notifyPropertyChanged(BR.enableMic);
    }

    public void setEnableMirror(boolean enableMirror) {
        this.enableMirror = enableMirror;
        notifyPropertyChanged(BR.enableMirror);
    }

    @Bindable
    public boolean isEnableMirror() {
        return enableMirror;
    }
}
