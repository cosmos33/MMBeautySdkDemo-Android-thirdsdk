package com.cosmos.thirdlive;

import android.content.Context;

import com.cosmos.appbase.BeautyManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.momo.mcamera.util.ImageFrame;

public class AgoraLiveBeautyManager extends BeautyManager {

    public AgoraLiveBeautyManager(Context context) {
        super(context);
    }

    @Override
    public int renderWithBytesTexture(byte[] datas, int texture, int dataWidth, int dataHeight, int texWidth, int texHeight, boolean mFrontCamera) {
        if (!resourceReady) {
            return texture;
        }
        CameraDataMode dataMode = new CameraDataMode(mFrontCamera, 90);
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
