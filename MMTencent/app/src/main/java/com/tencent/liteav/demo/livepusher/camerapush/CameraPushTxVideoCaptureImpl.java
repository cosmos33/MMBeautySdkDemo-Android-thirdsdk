package com.tencent.liteav.demo.livepusher.camerapush;

import android.content.Context;

import androidx.multidex.BuildConfig;

import com.core.glcore.cv.MMCVInfo;
import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.listener.OnFilterResourcePrepareListener;
import com.cosmos.appbase.listener.OnStickerResourcePrepareListener;
import com.cosmos.appbase.utils.FilterUtils;
import com.cosmos.beauty.CosmosBeautySDK;
import com.cosmos.beauty.inter.OnAuthenticationStateListener;
import com.cosmos.beauty.inter.OnBeautyResourcePreparedListener;
import com.cosmos.beauty.model.AuthResult;
import com.cosmos.beauty.model.BeautySDKInitConfig;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beauty.module.IMMRenderModuleManager;
import com.cosmos.beauty.module.beauty.IBeautyModule;
import com.cosmos.beauty.module.beauty.SimpleBeautyType;
import com.cosmos.beauty.module.lookup.ILookupModule;
import com.cosmos.beauty.module.sticker.DetectRect;
import com.cosmos.beauty.module.sticker.IStickerModule;
import com.cosmos.beauty.module.sticker.MaskLoadCallback;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.thirdlive.TencentBeautyManager;
import com.immomo.resdownloader.utils.MainThreadExecutor;
import com.mm.mmutil.toast.Toaster;
import com.momo.mcamera.mask.MaskModel;
import com.tencent.liteav.demo.livepusher.camerapush.model.CameraPushImpl;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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
        if (tencentBeautyManager == null) {
            tencentBeautyManager = new TencentBeautyManager(mContext);
        }
        return tencentBeautyManager.renderWithTexture(texture, texWidth, texHeight, true);
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
