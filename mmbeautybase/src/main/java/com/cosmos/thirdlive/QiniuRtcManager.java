package com.cosmos.thirdlive;

import android.content.Context;
import android.opengl.GLES20;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beautyutils.RotateFilter;

/**
 * 七牛RTC接入美颜sdk管理类
 */
public class QiniuRtcManager extends BeautyManager {
    private RotateFilter rotateFilter;
    private RotateFilter mirrorFilter;
    private RotateFilter resetFilter;
    private RotateFilter resetMirrorFilter;

    private RotateFilter backRotateFilter;

    public QiniuRtcManager(Context context) {
        super(context, cosmosAppid);
    }

    public int renderWithBytesTexture(byte[] data, int texture, int dataWidth, int dataHeight, boolean mFrontCamera, int cameraRotaion) {
        if (!resourceReady) {
            return 0;
        }
        if (rotateFilter == null) {
            rotateFilter = new RotateFilter(RotateFilter.ROTATE_90);
            mirrorFilter = new RotateFilter(RotateFilter.ROTATE_HORIZONTAL);
            resetFilter = new RotateFilter(RotateFilter.ROTATE_90);
            resetMirrorFilter = new RotateFilter(RotateFilter.ROTATE_HORIZONTAL);
            backRotateFilter = new RotateFilter(RotateFilter.ROTATE_270);
        }
        int rotateTexture;
        if (mFrontCamera) {
            rotateTexture = mirrorFilter.rotateTexture(rotateFilter.rotateTexture(texture, dataWidth, dataHeight),dataWidth,dataHeight);
        } else {
            rotateTexture = mirrorFilter.rotateTexture(backRotateFilter.rotateTexture(texture,dataWidth,dataHeight),dataWidth,dataHeight);
        }
        int texWidth;
        int texHeight;
        if (cameraRotaion == 90 || cameraRotaion == 270) {
            texWidth = dataHeight;
            texHeight = dataWidth;
        } else {
            texWidth = dataWidth;
            texHeight = dataHeight;
        }
        //美颜sdk处理
        CameraDataMode dataMode = new CameraDataMode(mFrontCamera,90);
        MMRenderFrameParams renderFrameParams =
                new MMRenderFrameParams(
                        dataMode, data,
                        dataWidth,
                        dataHeight,
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_NV21
                );
        int beautyTexId = renderModuleManager.renderFrame(rotateTexture, renderFrameParams);
        if (mFrontCamera) {
            beautyTexId = resetMirrorFilter.rotateTexture(resetFilter.rotateTexture(beautyTexId,dataWidth,dataHeight),dataWidth,dataHeight);
        } else {
            beautyTexId = rotateFilter.rotateTexture(mirrorFilter.rotateTexture(beautyTexId,dataWidth,dataHeight),dataWidth,dataHeight);
        }
        GLES20.glGetError();
        return beautyTexId;
    }

    @Override
    public void textureDestoryed() {
        super.textureDestoryed();
        if (rotateFilter != null) {
            rotateFilter.destory();
            rotateFilter = null;
        }
        if (mirrorFilter != null) {
            mirrorFilter.destory();
            mirrorFilter = null;
        }
        if (resetFilter != null) {
            resetFilter.destory();
            resetFilter = null;
        }
        if (resetMirrorFilter != null) {
            resetMirrorFilter.destory();
            resetMirrorFilter = null;
        }
        if (backRotateFilter != null) {
            backRotateFilter.destory();
            backRotateFilter = null;
        }
    }
}
