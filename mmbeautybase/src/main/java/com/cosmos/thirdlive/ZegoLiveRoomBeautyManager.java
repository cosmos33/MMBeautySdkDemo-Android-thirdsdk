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
import com.momo.mcamera.util.ImageFrame;

import java.nio.ByteBuffer;

import project.android.imageprocessing.input.NewNV21PreviewInput;

/**
 * 即构接入美颜sdk管理类
 */
public class ZegoLiveRoomBeautyManager extends BeautyManager {

    public ZegoLiveRoomBeautyManager(Context context) {
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
