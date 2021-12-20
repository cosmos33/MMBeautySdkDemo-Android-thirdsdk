package com.cosmos.thirdlive;

import android.content.Context;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.beautyutils.SyncReadByteFromGPUFilter;

public class AgoraLiveBeautyManager extends BeautyManager {
    private TransOesTextureFilter transOesTextureFilter;
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private byte[] frameData;

    public AgoraLiveBeautyManager(Context context) {
        super(context, cosmosAppid);
    }

    @Override
    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotation) {
        if (transOesTextureFilter == null) {
            transOesTextureFilter = new TransOesTextureFilter();
        }
        int tempWidth = texWidth;
        int tempHeight = texHeight;
        if (cameraRotation == 90 || cameraRotation == 270) {
            tempHeight = texWidth;
            tempWidth = texHeight;
        }
        return renderWithTexture(transOesTextureFilter.newTextureReady(texture, texWidth, texHeight), tempWidth, tempHeight, mFrontCamera);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (syncReadByteFromGPUFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_90);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_270);
                syncReadByteFromGPUFilter = new SyncReadByteFromGPUFilter();
            }
            int rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
            syncReadByteFromGPUFilter.newTextureReady(rotateTexture, getDownSampleSize(texWidth), getDownSampleSize(texHeight), true);
            if (syncReadByteFromGPUFilter.byteBuffer != null) {
                if (frameData == null || frameData.length != syncReadByteFromGPUFilter.byteBuffer.remaining()) {
                    frameData = new byte[syncReadByteFromGPUFilter.byteBuffer.remaining()];
                }
                syncReadByteFromGPUFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(mFrontCamera);
                int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                        dataMode,
                        frameData,
                        getDownSampleSize(texWidth), getDownSampleSize(texHeight),
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA, getScaleFactor()
                ));
                return revertRotateFilter.rotateTexture(beautyTexture, texWidth, texHeight);
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

    }
}
