package com.tencent.liteav.demo.livepusher.camerapush;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.core.glcore.cv.MMCVInfo;
import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.AspectFrameLayout;
import com.cosmos.appbase.camera.CameraImpl;
import com.cosmos.appbase.camera.CameraManager;
import com.cosmos.appbase.camera.ICamera;
import com.cosmos.appbase.camera.callback.OnPreviewDataCallback;
import com.cosmos.appbase.filter.DirectDrawer;
import com.cosmos.appbase.filter.FBOHelper;
import com.cosmos.appbase.gl.EGLHelper;
import com.cosmos.appbase.gl.GLUtils;
import com.cosmos.appbase.listener.OnFilterResourcePrepareListener;
import com.cosmos.appbase.listener.OnStickerResourcePrepareListener;
import com.cosmos.appbase.utils.FilterUtils;
import com.cosmos.beauty.CosmosBeautySDK;
import com.cosmos.beauty.inter.OnAuthenticationStateListener;
import com.cosmos.beauty.inter.OnBeautyResourcePreparedListener;
import com.cosmos.beauty.model.AuthResult;
import com.cosmos.beauty.model.BeautySDKInitConfig;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beauty.module.IMMRenderModuleManager;
import com.cosmos.beauty.module.beauty.IBeautyModule;
import com.cosmos.beauty.module.beauty.SimpleBeautyType;
import com.cosmos.beauty.module.lookup.ILookupModule;
import com.cosmos.beauty.module.sticker.DetectRect;
import com.cosmos.beauty.module.sticker.IStickerModule;
import com.cosmos.beauty.module.sticker.MaskLoadCallback;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.thirdlive.TencentPushBeautyManager;
import com.immomo.resdownloader.utils.MainThreadExecutor;
import com.mm.mmutil.toast.Toaster;
import com.momo.mcamera.mask.MaskModel;
import com.tencent.liteav.demo.livepusher.BuildConfig;
import com.tencent.liteav.demo.livepusher.camerapush.model.CameraPushImpl;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.tencent.rtmp.TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE;


public class CameraPushCustomVideoCaptureImpl extends CameraPushImpl implements OnPreviewDataCallback{
    CameraManager cameraManager;
    HandlerThread cameraHanderThread;
    Handler cameraHander;
    int screenRotation;
    private boolean authSuccess = false;
    private boolean filterResouceSuccess = false;
    private boolean cvModelSuccess = false;
    private boolean stickerSuccess;
    private AspectFrameLayout aspectFrameLayout;
    private SurfaceView surfaceView;
    private int textureId = 0;
    private SurfaceTexture surfaceTexture;
    boolean isFrontCamera = false;
    private volatile boolean surfaceDestory = true;
    private TencentPushBeautyManager tencentPushBeautyManager;

    public CameraPushCustomVideoCaptureImpl(Context context, TXCloudVideoView pusherView, int screenRotation) {
        super(context, pusherView);
        //??????????????????,?????????????????????
        //???????????????????????????????????????camera????????????
        cameraHanderThread = new HandlerThread("camera thread");
        cameraHanderThread.start();

        cameraHander = new Handler(cameraHanderThread.getLooper());
        cameraManager = new CameraManager(cameraHander, new CameraImpl());
        cameraManager.init(context);
        mLivePusher.getConfig().setCustomModeType(CUSTOM_MODE_VIDEO_CAPTURE);
        this.screenRotation = screenRotation;
    }

    private void initCamera() {
        textureId = GLUtils.INSTANCE.generateTexure();
        surfaceTexture = new SurfaceTexture(textureId);
        cameraManager.open(true);
        cameraManager.setPreviewSize(new Size(1280, 720));
        cameraManager.setPreviewFps(30, 30);
        cameraManager.preview(surfaceTexture, this, screenRotation);
        cameraManager.autoFocus();
        isFrontCamera = true;
        textureWidth = 1280;
        textureHeight = 720;
    }

    @Override
    protected void showViewLog(boolean enable) {
//        if (mPusherView instanceof TXCloudVideoView) {
//            ((TXCloudVideoView) mPusherView).showLog(enable);
//        }
    }

    @Override
    protected void startPreview(TXCloudVideoView mPusherView) {
//        mLivePusher.startCameraPreview((TXCloudVideoView) mPusherView);
        if (aspectFrameLayout == null) {
            aspectFrameLayout = new AspectFrameLayout((mPusherView).getContext());
            surfaceView = new SurfaceView((mPusherView).getContext());
            (mPusherView).addView(aspectFrameLayout);

            ViewGroup.LayoutParams aspectLayoutParams = aspectFrameLayout.getLayoutParams();
            aspectLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            aspectLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            aspectFrameLayout.setLayoutParams(aspectLayoutParams);

            aspectFrameLayout.addView(surfaceView);
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            surfaceView.setLayoutParams(layoutParams);
            aspectFrameLayout.setAspectRatio(720 * 1.0 / 1280);
            surfaceView.getHolder().setFixedSize(720, 1280);
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    surfaceDestory = false;
                    tencentPushBeautyManager = new TencentPushBeautyManager(mContext);
                    initCamera();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    surfaceDestory = true;
                    cameraManager.stopPreview();
                    cameraManager.release(new ICamera.ReleaseCallBack() {
                        @Override
                        public void onCameraRelease() {
                            cameraHander.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (tencentPushBeautyManager != null) {
                                        tencentPushBeautyManager.textureDestoryed();
                                    }
                                }
                            });
                        }
                    });
                    surfaceTexture.release();
                    directDrawer = null;
                }
            });
        }
    }

    @Override
    protected void setVisibility(int visibility) {
//        if (mPusherView instanceof TXCloudVideoView) {
//            ((TXCloudVideoView) mPusherView).setVisibility(visibility);
//        }
    }

    private DirectDrawer directDrawer;

    private FBOHelper fboHelper;
    private EGLSurface eglSurface;
    private int textureWidth, textureHeight;
    private float[] mtx = new float[16];

    private RotateFilter rotateFilter;
    private int txTexureId;

    @Override
    public void onPreviewData(@NotNull byte[] data) {
        if (cameraManager.getPreviewSize() == null || surfaceDestory) {
            return;
        }
        int width = textureWidth;
        int height = textureHeight;
        int orientation = cameraManager.getOrientation(cameraManager.getCurrentCameraId());

        if (orientation == 90 || orientation == 270) {
            width = textureHeight;
            height = textureWidth;
        }
        if (fboHelper == null) {
            EGLHelper.Companion.getInstance().init();
            eglSurface = EGLHelper.Companion.getInstance().genEglSurface(surfaceView.getHolder());
            EGLHelper.Companion.getInstance().makeCurrent(eglSurface);

            fboHelper = new FBOHelper(textureWidth, textureHeight);
            fboHelper.setNeedFlip(
                    isFrontCamera,
                    cameraManager.getOrientation(cameraManager.getCurrentCameraId())
            );
            directDrawer = new DirectDrawer();
        }
        EGLHelper.Companion.getInstance().makeCurrent(eglSurface);
        int resultTexture = fboHelper.update(data, orientation, cameraManager.getPreviewSize());
        resultTexture = tencentPushBeautyManager.renderWithBytesTexture(data,resultTexture,textureWidth,textureHeight,width,height,isFrontCamera,cameraManager.getCameraRotation());
        if (rotateFilter == null) {
            rotateFilter = new RotateFilter(RotateFilter.ROTATE_180);
        }
        txTexureId = rotateFilter.rotateTexture(resultTexture, width, height);
        //todo?????????????????????texture??????180????????????????????????????????????????????????
        mLivePusher.sendCustomVideoTexture(txTexureId, width, height);

        surfaceTexture.getTransformMatrix(mtx);
        directDrawer.draw(resultTexture, mtx, orientation, width, height);
        surfaceTexture.updateTexImage();
        EGLHelper.Companion.getInstance().swapBuffers(eglSurface);
    }

    public void switchCamera() {
        cameraManager.switchCamera(new ICamera.ReleaseCallBack() {
            @Override
            public void onCameraRelease() {

            }
        });
        isFrontCamera = !isFrontCamera;
        fboHelper.setNeedFlip(
                isFrontCamera,
                cameraManager.getOrientation(cameraManager.getCurrentCameraId())
        );
    }

    @Override
    public void stopPush() {
        super.stopPush();
        cameraManager.release(null);
    }

    private OnDataAvailableListener onDataAvailableListener;

    public void setOnDataAvailableListener(OnDataAvailableListener onDataAvailableListener) {
        this.onDataAvailableListener = onDataAvailableListener;
    }

    public interface OnDataAvailableListener {
        int onDataUpdate(boolean isFront, int cameraDataRotation, int width, int height, int formta, int textureInput, byte[] data);
    }
}
