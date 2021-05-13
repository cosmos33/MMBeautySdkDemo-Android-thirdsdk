package com.cosmos.thirdlive;

import android.content.Context;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;

/**
 * 腾讯直播接入美颜sdk管理类
 */
public class TencentBeautyManager extends BeautyManager {
    private RotateFilter rotateFilter;

    public TencentBeautyManager(Context context) {
        super(context, cosmosAppid);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (rotateFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                faceInfoCreatorPBOFilter = new FaceInfoCreatorPBOFilter(texWidth, texHeight);
                emptyFilter = new Empty2Filter();
                emptyFilter.setWidth(texWidth);
                emptyFilter.setHeight(texHeight);
            }
            int rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
            faceInfoCreatorPBOFilter.newTextureReady(rotateTexture, emptyFilter, true);
            if (faceInfoCreatorPBOFilter.byteBuffer != null) {
                byte[] frameData = new byte[faceInfoCreatorPBOFilter.byteBuffer.remaining()];
                faceInfoCreatorPBOFilter.byteBuffer.get(frameData);
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
                return rotateFilter.rotateTexture(beautyTexture, texWidth, texHeight);
            }
        }
        return texture;
    }
}
