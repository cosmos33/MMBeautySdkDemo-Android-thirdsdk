package com.tencent.liteav.demo.livepusher.camerapush;

import android.content.Context;
import android.os.Looper;

import com.cosmos.thirdlive.TencentBeautyManager;
import com.immomo.resdownloader.utils.MainThreadExecutor;
import com.tencent.liteav.demo.livepusher.camerapush.model.CameraPushImpl;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class CameraPushTxVideoCaptureImpl extends CameraPushImpl implements TXLivePusher.VideoCustomProcessListener {
    private TXCloudVideoView mPusherView;
    private TencentBeautyManager tencentBeautyManager;
    public CameraPushTxVideoCaptureImpl(Context context, TXCloudVideoView pusherView, int screenRotation) {
        super(context, pusherView);
    }

    @Override
    protected void showViewLog(boolean enable) {

    }

    @Override
    protected void startPreview(TXCloudVideoView mPusherView) {
        this.mPusherView = mPusherView;
        mLivePusher.getConfig().setVideoFPS(30);
        mLivePusher.setMirror(true);
        //调用下边方法解决预览卡顿！！！！！！！！！！！！！
        //调用下边方法解决预览卡顿！！！！！！！！！！！！！
        //调用下边方法解决预览卡顿！！！！！！！！！！！！！
        mLivePusher.getConfig().enableHighResolutionCaptureMode(false);
        mLivePusher.getConfig().setFrontCamera(true);
//        mLivePusher.getConfig().setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1280_720);
        mLivePusher.setVideoProcessListener(this);
        mLivePusher.startCameraPreview(mPusherView);
    }
    @Override
    public void switchCamera() {
        super.switchCamera();
        mLivePusher.setMirror(mFrontCamera);
    }
    @Override
    protected void setVisibility(int visibility) {
        if (mPusherView != null) {
            mPusherView.setVisibility(visibility);
        }
    }

    @Override
    public int onTextureCustomProcess(int texture, int texWidth, int texHeight) {
        if (Looper.myLooper() == null) {
            MainThreadExecutor.post(() -> {
                if (tencentBeautyManager == null) {
                    tencentBeautyManager = new TencentBeautyManager(mContext);
                }
            });
        }
        if (tencentBeautyManager != null) {
            return tencentBeautyManager.renderWithTexture(texture, texWidth, texHeight, true);
        }
        return texture;
    }

    @Override
    public void onDetectFacePoints(float[] floats) {

    }

    @Override
    public void onTextureDestoryed() {
        if (tencentBeautyManager != null) {
            tencentBeautyManager.textureDestoryed();
        }

    }
}
