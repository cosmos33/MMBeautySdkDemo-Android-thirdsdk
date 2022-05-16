package com.cosmos.thirdlive;

import android.content.Context;
import android.opengl.GLES20;

import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.appbase.TransYUVTextureFilter;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.beautyutils.SyncReadByteFromGPUFilter;
import com.momo.mcamera.util.ImageFrame;

/**
 * 即构接入美颜sdk管理类
 */
public class ZegoBeautyManager extends BeautyManager {
    private TransOesTextureFilter transOesTextureFilter;
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private RotateFilter backRotateFilter;
    private RotateFilter backRevertRotateFilter;
    private TransYUVTextureFilter transYUVTextureFilter;
    private byte[] frameData;

    public ZegoBeautyManager(Context context) {
        super(context);
    }

    @Override
    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotation) {
        if (transOesTextureFilter == null) {
            transOesTextureFilter = new TransOesTextureFilter();
        }
        int tempWidth = texWidth;
        int tempHeight = texHeight;
        return renderWithTexture(transOesTextureFilter.newTextureReady(texture, texWidth, texHeight), tempWidth, tempHeight, mFrontCamera);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (syncReadByteFromGPUFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_180);
                backRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_180);
                backRevertRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                syncReadByteFromGPUFilter = new SyncReadByteFromGPUFilter();
            }
            int rotateTexture = texture;
            RotateFilter temp;
            RotateFilter tempRevert;
            if (mFrontCamera) {
                temp = rotateFilter;
                tempRevert = revertRotateFilter;
            } else {
                temp = backRotateFilter;
                tempRevert = backRevertRotateFilter;
            }
            rotateTexture = temp.rotateTexture(texture, texWidth, texHeight);
            syncReadByteFromGPUFilter.newTextureReady(rotateTexture, getDownSampleSize(texWidth), getDownSampleSize(texHeight), true);

            if (syncReadByteFromGPUFilter.byteBuffer != null) {
                if (frameData == null || frameData.length != syncReadByteFromGPUFilter.byteBuffer.remaining()) {
                    frameData = new byte[syncReadByteFromGPUFilter.byteBuffer.remaining()];
                }
                syncReadByteFromGPUFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(false);
                int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                        dataMode,
                        frameData,
                        getDownSampleSize(texWidth), getDownSampleSize(texHeight),
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA, getScaleFactor()
                ));
                GLES20.glGetError();
                return tempRevert.rotateTexture(beautyTexture, texWidth, texHeight);
            }
        }
        return texture;
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
        if (transYUVTextureFilter != null) {
            transYUVTextureFilter.destroy();
            transYUVTextureFilter = null;
        }

    }
}
