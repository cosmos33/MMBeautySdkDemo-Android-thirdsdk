package com.cosmos.thirdlive;

import android.content.Context;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;
import com.cosmos.thirdlive.utils.PBOFilter;

/**
 * 腾讯LVBSDK接入美颜sdk管理类
 */
public class TencentLVBSDKBeautyManager extends BeautyManager {
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private byte[] frameData;

    public TencentLVBSDKBeautyManager(Context context) {
        super(context, cosmosAppid);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (rotateFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                pboFilter = new PBOFilter(texWidth, texHeight);
            }
            int rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
            pboFilter.newTextureReady(rotateTexture, texWidth, texHeight, true);
            if (pboFilter.byteBuffer != null) {
                if (frameData == null || frameData.length != pboFilter.byteBuffer.remaining()) {
                    frameData = new byte[pboFilter.byteBuffer.remaining()];
                }
                pboFilter.byteBuffer.get(frameData);
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
