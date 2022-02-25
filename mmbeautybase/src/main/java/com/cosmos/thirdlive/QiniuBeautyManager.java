package com.cosmos.thirdlive;

import android.content.Context;
import android.opengl.GLES20;

import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.appbase.orientation.BeautySdkOrientationSwitchListener;
import com.cosmos.appbase.orientation.ScreenOrientationManager;
import com.cosmos.baseutil.app.AppContext;
import com.cosmos.beauty.Constants;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.beautyutils.SyncReadByteFromGPUFilter;
import com.momo.mcamera.util.ImageFrame;

/**
 * 七牛接入美颜sdk管理类
 */
public class QiniuBeautyManager extends BeautyManager {
    private TransOesTextureFilter transOesTextureFilter;
    private RotateFilter rotateFilter;
    private RotateFilter rotateRevertFilter;
    private RotateFilter backRotateFilter;
    private RotateFilter backHorizontalRotateFilter;
    private BeautySdkOrientationSwitchListener orientationListener;
    private byte[] frameData;

    public QiniuBeautyManager(Context context) {
        super(context);
        orientationListener = new BeautySdkOrientationSwitchListener();
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        screenOrientationManager.setAngleChangedListener(orientationListener);
        if (!screenOrientationManager.isListening()) {
            screenOrientationManager.start();
        }
    }

    @Override
    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int rotation) {
        if (transOesTextureFilter == null) {
            transOesTextureFilter = new TransOesTextureFilter();
        }
        int tempWidth = texHeight;
        int tempHeight = texWidth;
        return renderWithTexture(texture,transOesTextureFilter.newTextureReady(texture, texWidth, texHeight), tempWidth, tempHeight, mFrontCamera);
    }

    public int renderWithTexture(int originTexture,int transTexture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (rotateFilter == null) {
            rotateFilter = new RotateFilter(RotateFilter.ROTATE_90);
            rotateRevertFilter = new RotateFilter(RotateFilter.ROTATE_180);
            backRotateFilter = new RotateFilter(RotateFilter.ROTATE_270);
            backHorizontalRotateFilter = new RotateFilter(RotateFilter.ROTATE_HORIZONTAL);
            syncReadByteFromGPUFilter = new SyncReadByteFromGPUFilter();
        }
        if (renderModuleManager != null) {
            int rotateTexture;
            float currentAngle = orientationListener.getCurrentAngle();
            if (mFrontCamera) {
                if (currentAngle == 0) {
                    rotateTexture = rotateFilter.rotateTexture(transTexture, texWidth, texHeight);
                } else {
                    rotateTexture = backRotateFilter.rotateTexture(transTexture, texWidth, texHeight);
                }
            } else {
                rotateTexture = backRotateFilter.rotateTexture(transTexture, texWidth, texHeight);
            }
            syncReadByteFromGPUFilter.newTextureReady(rotateTexture, texWidth, texHeight, true);

            if (syncReadByteFromGPUFilter.byteBuffer != null) {
                if (frameData == null || frameData.length != syncReadByteFromGPUFilter.byteBuffer.remaining()) {
                    frameData = new byte[syncReadByteFromGPUFilter.byteBuffer.remaining()];
                }
                syncReadByteFromGPUFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(false);
                dataMode.setCameraRotationDegree(0);
                MMRenderFrameParams renderFrameParams =
                        new MMRenderFrameParams(
                                dataMode, frameData,
                                texWidth,
                                texHeight,
                                texWidth,
                                texHeight,
                                ImageFrame.MMFormat.FMT_RGBA
                        );
                rotateTexture =renderModuleManager.renderFrame(rotateTexture, renderFrameParams);
                if (!mFrontCamera) {
                    rotateTexture = backHorizontalRotateFilter.rotateTexture(rotateTexture, texWidth, texHeight);
                } else {
                    if (currentAngle != 0) {
                        rotateTexture = rotateRevertFilter.rotateTexture(rotateTexture,texWidth,texHeight);
                    }
                }
                return rotateTexture;
            }
        }
        //重置当前渲染环境状态
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        return originTexture;
    }

    @Override
    public int renderWithBytesTexture(byte[] datas, int texture, int dataWidth, int dataHeight, int texWidth, int texHeight, boolean mFrontCamera) {
        if (datas != null && renderModuleManager != null) {
            MMRenderFrameParams renderFrameParams =
                    new MMRenderFrameParams(
                            new CameraDataMode(
                                    mFrontCamera,
                                    Constants.RotationDegree.Degree90
                            ), datas,
                            dataWidth,
                            dataHeight,
                            texWidth,
                            texHeight,
                            ImageFrame.MMFormat.FMT_NV21
                    );
            return renderModuleManager.renderFrame(texture,renderFrameParams);
        }
        return texture;
    }

    public void stopOrientationCallback() {
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        if (screenOrientationManager.isListening()) {
            screenOrientationManager.stop();
        }
        ScreenOrientationManager.release();
    }

    @Override
    public void textureDestoryed() {
        stopOrientationCallback();
        super.textureDestoryed();
        if (rotateFilter != null) {
            rotateFilter.destory();
            rotateFilter = null;
        }
    }
}
