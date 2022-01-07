package com.cosmos.thirdlive;

import android.content.Context;

import com.cosmos.appbase.BeautyManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.beautyutils.SyncReadByteFromGPUFilter;
import com.momo.mcamera.util.ImageFrame;

/**
 * 腾讯LVBSDK接入美颜sdk管理类
 */
public class TencentLVBSDKBeautyManager extends BeautyManager {
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private byte[] frameData;

    public TencentLVBSDKBeautyManager(Context context) {
        super(context);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (rotateFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                syncReadByteFromGPUFilter = new SyncReadByteFromGPUFilter();
            }
            int rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
            syncReadByteFromGPUFilter.newTextureReady(rotateTexture, texWidth, texHeight, true);
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
                        texWidth,
                        texHeight,
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA
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
