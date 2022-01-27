package com.cosmos.thirdlive;

import android.content.Context;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.appbase.TransYUVTextureFilter;
import com.cosmos.appbase.gl.EGLHelper;
import com.cosmos.appbase.orientation.BeautySdkOrientationSwitchListener;
import com.cosmos.appbase.orientation.ScreenOrientationManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.beautyutils.SyncReadByteFromGPUFilter;
import com.cosmos.thirdlive.utils.PBOSubFilter;
import com.mm.mmutil.app.AppContext;
import com.momo.mcamera.mask.beauty.DrawFaceFilter;
import com.momo.mcamera.util.ImageFrame;

import java.nio.ByteBuffer;

import project.android.imageprocessing.input.NewNV21PreviewInput;

/**
 * 即构接入美颜sdk管理类
 */
public class ZegoLiveRoomBeautyManager extends BeautyManager {
    private TransOesTextureFilter transOesTexture;
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private BeautySdkOrientationSwitchListener orientationListener;

    public ZegoLiveRoomBeautyManager(Context context) {
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
    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotation) {
        if (transOesTexture == null) {
            transOesTexture = new TransOesTextureFilter();
        }
        int tempWidth = texWidth;
        int tempHeight = texHeight;
        return renderWithTexture(transOesTexture.newTextureReady(texture, texWidth, texHeight), tempWidth, tempHeight, mFrontCamera);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (syncReadByteFromGPUFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                syncReadByteFromGPUFilter = new SyncReadByteFromGPUFilter();
            }
            float currentAngle = orientationListener.getCurrentAngle();
            int rotateTexture = texture;
            if (currentAngle == 0) {
                rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
            }
            syncReadByteFromGPUFilter.newTextureReady(rotateTexture, texWidth,texHeight, true);

            if (syncReadByteFromGPUFilter.byteBuffer != null) {
                byte[] frameData = new byte[syncReadByteFromGPUFilter.byteBuffer.remaining()];
                syncReadByteFromGPUFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(false);
                int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                        dataMode,
                        frameData,
                        texWidth,
                        texHeight,
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA
                ));
                if (currentAngle != 0) {
                    return beautyTexture;
                }
                return revertRotateFilter.rotateTexture(beautyTexture, texWidth, texHeight);
            }
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
        super.textureDestoryed();
        if (rotateFilter != null) {
            rotateFilter.destory();
            rotateFilter = null;
        }
        if (revertRotateFilter != null) {
            revertRotateFilter.destory();
            revertRotateFilter = null;
        }

    }
}
