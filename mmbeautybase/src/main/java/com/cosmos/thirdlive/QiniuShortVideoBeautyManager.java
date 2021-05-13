package com.cosmos.thirdlive;

import android.content.Context;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.orientation.BeautySdkOrientationSwitchListener;
import com.cosmos.appbase.orientation.ScreenOrientationManager;
import com.cosmos.beauty.Constants;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CameraDataMode;
import com.cosmos.beautyutils.RotateFilter;
import com.mm.mmutil.app.AppContext;

/**
 * 七牛接入美颜sdk管理类
 */
public class QiniuShortVideoBeautyManager extends BeautyManager {
    private RotateFilter rotate90Filter;
    private RotateFilter rotate180Filter;
    private RotateFilter backRotateFilter;
    private RotateFilter backHorizontalRotateFilter;
    private RotateFilter rotate270Filter;
    private RotateFilter rotateVerticalFilter;
    private BeautySdkOrientationSwitchListener orientationListener;

    public QiniuShortVideoBeautyManager(Context context) {
        super(context, cosmosAppid);
        orientationListener = new BeautySdkOrientationSwitchListener();
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        screenOrientationManager.setAngleChangedListener(orientationListener);
        if (!screenOrientationManager.isListening()) {
            screenOrientationManager.start();
        }
    }


    @Override
    public int renderWithBytesTexture(byte[] datas, int texture, int dataWidth, int dataHeight, int texWidth, int texHeight, boolean mFrontCamera) {
        if (datas != null && renderModuleManager != null) {
            if (rotate90Filter == null) {
                rotate90Filter = new RotateFilter(RotateFilter.ROTATE_90);
                rotate180Filter = new RotateFilter(RotateFilter.ROTATE_180);
                rotate270Filter = new RotateFilter(RotateFilter.ROTATE_270);
                rotateVerticalFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                backHorizontalRotateFilter = new RotateFilter(RotateFilter.ROTATE_HORIZONTAL);
            }
            int rotateTexture = texture;
            float currentAngle = orientationListener.getCurrentAngle();
            int cameraRation = Constants.RotationDegree.Degree0;
            if (mFrontCamera) {
                if (currentAngle == 0) {
                    rotateTexture = rotate90Filter.rotateTexture(texture, texWidth, texHeight);
                    cameraRation = Constants.RotationDegree.Degree90;
                } else {
                    rotateTexture = rotate180Filter.rotateTexture(texture, texWidth, texHeight);
                    cameraRation = Constants.RotationDegree.Degree0;
                }
            } else {
                rotateTexture = rotate270Filter.rotateTexture(texture, texWidth, texHeight);
                rotateTexture = backHorizontalRotateFilter.rotateTexture(rotateTexture, texWidth, texHeight);
                cameraRation = Constants.RotationDegree.Degree90;
            }
            MMRenderFrameParams renderFrameParams =
                    new MMRenderFrameParams(
                            new CameraDataMode(
                                    mFrontCamera,
                                    cameraRation
                            ), datas,
                            dataWidth,
                            dataHeight,
                            texWidth,
                            texHeight,
                            ImageFrame.MMFormat.FMT_NV21
                    );
            rotateTexture = renderModuleManager.renderFrame(rotateTexture, renderFrameParams);
            if (!mFrontCamera) {
                rotateTexture = backHorizontalRotateFilter.rotateTexture(rotateTexture, texWidth, texHeight);
                rotateTexture = rotate90Filter.rotateTexture(rotateTexture, texWidth, texHeight);
            } else {
                if (currentAngle != 0) {
                    rotateTexture = rotate180Filter.rotateTexture(rotateTexture, texWidth, texHeight);
                } else {
                    rotateTexture = rotate270Filter.rotateTexture(rotateTexture, texWidth, texHeight);
                }
            }
            return rotateTexture;
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
        stopOrientationCallback();
        super.textureDestoryed();
        if (rotate90Filter != null) {
            rotate90Filter.destory();
            rotate90Filter = null;
        }
    }
}
