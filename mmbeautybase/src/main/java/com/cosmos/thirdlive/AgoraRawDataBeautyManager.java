package com.cosmos.thirdlive;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.EGLSurface;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.gl.EGLHelper;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.thirdlive.utils.FaceInfoCreatorPBOSubFilter;

import project.android.imageprocessing.input.NV21PreviewInput;

/**
 * 声网接入美颜sdk管理类
 */
public class AgoraRawDataBeautyManager extends BeautyManager {
    private NV21PreviewInput yuvToTexture;
    private RotateFilter rotateFilter;
    private RotateFilter rotateFilter1;
    private RotateFilter rotateFilter2;

    public AgoraRawDataBeautyManager(Context context) {
        super(context, cosmosAppid);
    }

    public Bitmap renderWithRawData(byte[] data, int width, int height, int rotation, boolean mFrontCamera) {
        if (!EGLHelper.Companion.getInstance().checkContext()) {
            EGLHelper.Companion.getInstance().init();
            EGLSurface eglSurface = EGLHelper.Companion.getInstance().genEglSurface(null);
            EGLHelper.Companion.getInstance().makeCurrent(eglSurface);
        }
        if (yuvToTexture == null) {
            yuvToTexture = new NV21PreviewInput();
            yuvToTexture.setRenderSize(width, height);
            faceInfoCreatorPBOFilter = new FaceInfoCreatorPBOSubFilter(width, height);
            emptyFilter = new Empty2Filter();
            emptyFilter.setRenderSize(width, height);
            rotateFilter = new RotateFilter(RotateFilter.ROTATE_HORIZONTAL);
            rotateFilter1 = new RotateFilter(RotateFilter.ROTATE_270);
            rotateFilter2 = new RotateFilter(RotateFilter.ROTATE_90);
        }
        yuvToTexture.updateYUVBuffer(data, width * height);
        yuvToTexture.onDrawFrame();
        int textOutID = yuvToTexture.getTextOutID();
        int rotateTexture = rotateFilter2.rotateTexture(textOutID, width, height);
        int texWidth = width;
        int texHeight = height;
        if (rotation / 90 == 1 || rotation / 90 == 3) {
            texWidth = height;
            texHeight = width;
        }
        CameraDataMode cameraDataMode = new CameraDataMode(mFrontCamera, 90);
        int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                cameraDataMode,
                data,
                width,
                height,
                texWidth,
                texHeight,
                ImageFrame.MMFormat.FMT_NV21
        ));
        int rotateTexId = rotateFilter.rotateTexture(beautyTexture, width, height);
        int rotateTexId1 = rotateFilter1.rotateTexture(rotateTexId, width, height);
        faceInfoCreatorPBOFilter.newTextureReady(rotateTexId1, emptyFilter, true);
        if (faceInfoCreatorPBOFilter.byteBuffer != null) {
            Bitmap curBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            curBmp.copyPixelsFromBuffer(faceInfoCreatorPBOFilter.byteBuffer);
            return curBmp;
        }
        return null;
    }

    @Override
    public void textureDestoryed() {
        super.textureDestoryed();
        if (yuvToTexture != null) {
            yuvToTexture.destroy();
            yuvToTexture = null;
        }
        if (rotateFilter != null) {
            rotateFilter.destory();
            rotateFilter = null;
        }
        if (rotateFilter1 != null) {
            rotateFilter1.destory();
            rotateFilter1 = null;
        }
        if (rotateFilter2 != null) {
            rotateFilter2.destory();
            rotateFilter2 = null;
        }
    }


}
