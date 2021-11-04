package com.cosmos.thirdlive;

import android.content.Context;
import android.opengl.GLES20;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.appbase.TransYUVTextureFilter;
import com.cosmos.appbase.orientation.BeautySdkOrientationSwitchListener;
import com.cosmos.appbase.orientation.ScreenOrientationManager;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;
import com.mm.mmutil.app.AppContext;

/**
 * 即构接入美颜sdk管理类
 */
public class ZegoBeautyManager extends BeautyManager {
    private TransOesTextureFilter transOesTextureFilter;
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private RotateFilter backRotateFilter;
    private RotateFilter backRevertRotateFilter;
    private BeautySdkOrientationSwitchListener orientationListener;
    private TransYUVTextureFilter transYUVTextureFilter;

    public ZegoBeautyManager(Context context) {
        super(context, cosmosAppid);
        orientationListener = new BeautySdkOrientationSwitchListener();
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        screenOrientationManager.setAngleChangedListener(orientationListener);
        if (!screenOrientationManager.isListening()) {
            screenOrientationManager.start();
        }
    }

//    public int renderWithYUVTexture(int[] texture, int texWidth, int texHeight, boolean mFrontCamera) {
//        if (transYUVTextureFilter == null) {
//            transYUVTextureFilter = new TransYUVTextureFilter();
//        }
//        int yuvTex = transYUVTextureFilter.newTextureReady(texture, texWidth, texHeight);
//        return renderWithTexture(yuvTex, texWidth, texHeight, mFrontCamera);
//    }

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
            if (faceInfoCreatorPBOFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                backRotateFilter = new RotateFilter(RotateFilter.ROTATE_180);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                backRevertRotateFilter = new RotateFilter(RotateFilter.ROTATE_180);
                faceInfoCreatorPBOFilter = new FaceInfoCreatorPBOFilter(texWidth, texHeight);
                emptyFilter = new Empty2Filter();
                emptyFilter.setWidth(texWidth);
                emptyFilter.setHeight(texHeight);
            }
            float currentAngle = orientationListener.getCurrentAngle();
            int rotateTexture = texture;
            RotateFilter temp;
            RotateFilter tempRevert;
            if (!mFrontCamera && currentAngle != 0) {//后置相机，手机水平
                temp = backRotateFilter;
                tempRevert = backRevertRotateFilter;
            }else {
                temp = rotateFilter;
                tempRevert = revertRotateFilter;
            }
            if ((currentAngle == 0 && mFrontCamera) || (!mFrontCamera)) {
                rotateTexture = temp.rotateTexture(texture, texWidth, texHeight);
            }
            faceInfoCreatorPBOFilter.newTextureReady(rotateTexture, emptyFilter, true);

            if (faceInfoCreatorPBOFilter.byteBuffer != null) {
                byte[] frameData = new byte[faceInfoCreatorPBOFilter.byteBuffer.remaining()];
                faceInfoCreatorPBOFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(false);
                int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                        dataMode,
                        frameData,
                        texWidth,
                        texHeight,
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA
                ));
                GLES20.glGetError();
                if ((currentAngle == 0 && mFrontCamera) || (!mFrontCamera)) {
                    return tempRevert.rotateTexture(beautyTexture, texWidth, texHeight);
                }
                return beautyTexture;
            }
        }
        return texture;
    }

    public void stopOrientationCallback() {
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        if (screenOrientationManager.isListening()) {
            screenOrientationManager.stop();
        }
        ScreenOrientationManager.release();
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
