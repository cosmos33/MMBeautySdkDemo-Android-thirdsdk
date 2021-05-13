package com.cosmos.thirdlive;

import android.content.Context;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;

public class TencentPushBeautyManager extends BeautyManager {

    public TencentPushBeautyManager(Context context) {
        super(context, cosmosAppid);
    }

    @Override
    public int renderWithBytesTexture(byte[] datas, int texture, int dataWidth, int dataHeight, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotaion) {
        if (!resourceReady) {
            return texture;
        }
        CameraDataMode dataMode = new CameraDataMode(mFrontCamera, cameraRotaion);
        return renderModuleManager.renderFrame(texture, new MMRenderFrameParams(
                dataMode,
                datas,
                dataWidth,
                dataHeight,
                texWidth,
                texHeight,
                ImageFrame.MMFormat.FMT_NV21
        ));
    }
}
