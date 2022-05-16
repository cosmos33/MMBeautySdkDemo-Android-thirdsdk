package com.cosmos.appbase;

import android.content.Context;

import com.cosmos.appbase.listener.OnResPrepareListener;
import com.cosmos.appbase.utils.FilterUtils;
import com.cosmos.baseutil.thread.MomoMainThreadExecutor;
import com.cosmos.baseutil.toast.Toaster;
import com.cosmos.beauty.CosmosBeautySDK;
import com.cosmos.beauty.model.AuthResult;
import com.cosmos.beauty.module.IMMRenderModuleManager;
import com.cosmos.beauty.module.beauty.IBeautyModule;
import com.cosmos.beauty.module.beauty.SimpleBeautyType;
import com.cosmos.beauty.module.lookup.ILookupModule;
import com.cosmos.beauty.module.sticker.DetectRect;
import com.cosmos.beauty.module.sticker.IStickerModule;
import com.cosmos.beauty.module.sticker.MaskLoadCallback;
import com.cosmos.beautyutils.SyncReadByteFromGPUFilter;
import com.momo.mcamera.mask.MaskModel;

import org.jetbrains.annotations.NotNull;

import java.io.File;

abstract public class BeautyManager implements IMMRenderModuleManager.IDetectFaceCallback, IMMRenderModuleManager.IDetectGestureCallback {
    protected static String license = "";// TODO mmbeauty 这里配置license
    protected final float SCALE_FACTOR = 1f;//修改为1的整数倍（比如2），会适当减少整个渲染的耗时，但是会对美颜精度产生一定程度影响，请根据接入方的接受程度修改该值
    protected final float DOWN_SAMPLE_RATIO = 1 / SCALE_FACTOR;
    protected IMMRenderModuleManager renderModuleManager;
    protected boolean authSuccess = false;
    protected boolean stickerSuccess;
    protected IBeautyModule iBeautyModule;
    protected ILookupModule iLookupModule;
    protected IStickerModule iStickerModule;
    protected boolean resourceReady = false;
    protected Context context;
    protected SyncReadByteFromGPUFilter syncReadByteFromGPUFilter;
    protected TransOesTextureFilter transOesTextureFilter;

    public BeautyManager(Context context) {
        this.context = context.getApplicationContext();
        initSDK();
    }

    protected int getDownSampleSize(int originSize) {
        return (int) (originSize * DOWN_SAMPLE_RATIO);
    }

    protected float getScaleFactor() {
        return SCALE_FACTOR;
    }

    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotation) {
        return texture;
    }

    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        return texHeight;
    }

    public int renderWithBytesTexture(byte[] datas, int texture, int dataWidth, int dataHeight, int texWidth, int texHeight, boolean mFrontCamera) {
        return texture;
    }

    public int renderWithBytesTexture(byte[] datas, int texture, int dataWidth, int dataHeight, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotaion) {
        return texture;
    }

    public int renderWithBytesAndOesTexture(byte[] bytes, int texture, int texWidth, int texHeight, boolean mFrontCamera, int rotation) {
        return texture;
    }

    public void textureDestoryed() {
        if (transOesTextureFilter != null) {
            transOesTextureFilter.destroy();
            transOesTextureFilter = null;
        }
        if (syncReadByteFromGPUFilter != null) {
            syncReadByteFromGPUFilter.destroy();
            syncReadByteFromGPUFilter = null;
        }
        if (renderModuleManager != null) {
            if (iBeautyModule != null) {
                renderModuleManager.unRegisterModule(iBeautyModule);
            }
            if (iLookupModule != null) {
                renderModuleManager.unRegisterModule(iLookupModule);
            }
            if (iStickerModule != null) {
                renderModuleManager.unRegisterModule(iStickerModule);
            }
            renderModuleManager.destroyModuleChain();
            renderModuleManager.release();
        }

    }

    private void initSDK() {
        FilterUtils.INSTANCE.prepareModelsResource(context, new OnResPrepareListener() {
            public void onResReady(String rootPath) {
                AuthResult result = CosmosBeautySDK.INSTANCE.init(context, license, rootPath);
                if (!result.isSucceed()) {
                    Toaster.show(String.format("授权失败:%s", result.getMsg()));
                } else {
                    authSuccess = true;
                    checkResouceReady();
                }
            }
        });

        FilterUtils.INSTANCE.prepareStikcerResource(context, new OnResPrepareListener() {
            public void onResReady(String rootPath) {
                stickerSuccess = true;
                checkResouceReady();
            }
        });
    }

    private void checkResouceReady() {
        if (authSuccess && stickerSuccess) {
            MomoMainThreadExecutor.post(new Runnable() {
                @Override
                public void run() {
                    renderModuleManager = CosmosBeautySDK.INSTANCE.createRenderModuleManager();
                    renderModuleManager.prepare(true, BeautyManager.this, BeautyManager.this);
                    initRender();
                    resourceReady = true;
                }
            });
        }
    }

    protected void initRender() {
        iBeautyModule = CosmosBeautySDK.INSTANCE.createBeautyModule();
        renderModuleManager.registerModule(iBeautyModule);
        iBeautyModule.setValue(SimpleBeautyType.BIG_EYE, 1f);
        iBeautyModule.setValue(SimpleBeautyType.SKIN_SMOOTH, 1.0f);
        iBeautyModule.setValue(SimpleBeautyType.SKIN_WHITENING, 1.0f);
        iBeautyModule.setValue(SimpleBeautyType.THIN_FACE, 1f);

        iLookupModule = CosmosBeautySDK.INSTANCE.createLoopupModule();
        renderModuleManager.registerModule(iLookupModule);

        iStickerModule = CosmosBeautySDK.INSTANCE.createStickerModule();
        renderModuleManager.registerModule(iStickerModule);
        iStickerModule.addMaskModel(
                new File(context.getFilesDir().getAbsolutePath() + "/facemasksource/facemask/", "rainbow_engine"),
                new MaskLoadCallback() {

                    @Override
                    public void onMaskLoadSuccess(MaskModel maskModel) {
                        if (maskModel == null) {
                            Toaster.show("贴纸加载失败");
                        }
                    }
                });
    }

    @Override
    public void onDetectFace(int faceCount) {

    }

    @Override
    public void onDetectGesture(@NotNull String type, @NotNull DetectRect detect) {

    }

    @Override
    public void onGestureMiss() {

    }
}