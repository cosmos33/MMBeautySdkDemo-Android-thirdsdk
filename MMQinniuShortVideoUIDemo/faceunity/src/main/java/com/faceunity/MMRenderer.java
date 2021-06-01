package com.faceunity;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cosmos.appbase.gl.EGLHelper;
import com.cosmos.thirdlive.QiniuShortVideoBeautyManager;
import com.faceunity.gles.FullFrameRect;
import com.faceunity.gles.Texture2dProgram;

public class MMRenderer {
    private static final String TAG = MMRenderer.class.getSimpleName();
    private Context mContext;
    private Handler mFuItemHandler;

    private volatile int mInputImageOrientation = 270;
    private volatile int mInputPropOrientation = 270;
    private volatile int mCurrentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private QiniuShortVideoBeautyManager qiniuBeautyManager;
    private FullFrameRect mFullScreenFUDisplay;
    private HandlerThread mFuItemHandlerThread;

    /**
     * 创建及初始化
     */
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated");
        onSurfaceDestroyed();

        mFuItemHandlerThread = new HandlerThread("FUItemHandlerThread");
        mFuItemHandlerThread.start();
        mFuItemHandler = new Handler(mFuItemHandlerThread.getLooper());
        EGLHelper.Companion.getInstance().init();
        mFuItemHandler.post(new Runnable() {
            @Override
            public void run() {
                if (qiniuBeautyManager == null) {
                    qiniuBeautyManager = new QiniuShortVideoBeautyManager(mContext);
                }
            }
        });
    }

    private MMRenderer(Context context) {
        this.mContext = context;
    }

    /**
     * 销毁相关的资源
     */
    public void onSurfaceDestroyed() {
        Log.e(TAG, "onSurfaceDestroyed");
        mFuItemHandler = null;
    }


    /**
     * 双输入接口(fuDualInputToTexture)(处理后的画面数据并不会回写到数组)，由于省去相应的数据拷贝性能相对最优，推荐使用。
     *
     * @param img NV21数据
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(byte[] img, int tex, int w, int h) {
        if (tex <= 0 || img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame date null");
            return 0;
        }

        int resultTex = tex;
        if (qiniuBeautyManager != null) {
            resultTex = qiniuBeautyManager.renderWithBytesTexture(img, tex, w, h, w, h, mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        return resultTex;
    }

    /**
     * camera切换时需要调用
     *
     * @param currentCameraType     前后置摄像头ID
     * @param inputImageOrientation
     * @param inputPropOrientation
     */
    public void onCameraChange(final int currentCameraType, final int inputImageOrientation
            , final int inputPropOrientation) {
        if (mCurrentCameraType == currentCameraType && mInputImageOrientation == inputImageOrientation &&
                mInputPropOrientation == inputPropOrientation) {
            return;
        }
        mCurrentCameraType = currentCameraType;
        mInputImageOrientation = inputImageOrientation;
        mInputPropOrientation = inputPropOrientation;
    }

    /*----------------------------------Builder---------------------------------------*/

    /**
     * FURenderer Builder
     */
    public static class Builder {

        private Context context;
        private int inputImageRotation = 270;
        private int inputPropRotation = 270;
        private int currentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * 道具方向
         *
         * @param inputPropRotation
         * @return
         */
        public Builder inputPropOrientation(int inputPropRotation) {
            this.inputPropRotation = inputPropRotation;
            return this;
        }

        public MMRenderer build() {
            MMRenderer MMRenderer = new MMRenderer(context);
            MMRenderer.mInputImageOrientation = inputImageRotation;
            MMRenderer.mInputPropOrientation = inputPropRotation;
            MMRenderer.mCurrentCameraType = currentCameraType;

            return MMRenderer;
        }
    }

    /*-------------------七牛视频------------------*/

    private static boolean isInit;
    private float[] mMvpMtx270 = new float[16];
    private float[] mMvpMtx90 = new float[16];
    /* --------------FBO----------------*/
    private int fboId[];

    {
        Matrix.setIdentityM(mMvpMtx270, 0);
        Matrix.rotateM(mMvpMtx270, 0, 270, 0, 0, 1);
    }

    {
        Matrix.setIdentityM(mMvpMtx90, 0);
        Matrix.rotateM(mMvpMtx90, 0, 90, 0, 0, 1);
    }

    private boolean isActive;

    public void loadItems() {
        if (!isInit) {
            isInit = true;
        }

        mFullScreenFUDisplay = new FullFrameRect(new Texture2dProgram(
                Texture2dProgram.ProgramType.TEXTURE_2D));

        onSurfaceCreated();
        isActive = true;
    }

    public void destroyItems() {
        isActive = false;

        onSurfaceDestroyed();

        deleteFBO();
        if (qiniuBeautyManager != null) {
            qiniuBeautyManager.textureDestoryed();
            qiniuBeautyManager = null;
        }
    }


    // 使用 FBO，先对原始纹理做旋转，保持和相机数据的方向一致，然后FU绘制，最后转正输出
    public int onDrawFrameByFBO(byte[] cameraNV21Byte, int texId, int texWidth, int texHeight) {
        if (!isActive) {
            return texId;
        }
        createFBO(texWidth, texHeight);
        Log.d("sss", "mInputProp=" + mInputPropOrientation);

        int[] originalViewPort = new int[4];
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, originalViewPort, 0);
        if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
            GLES20.glViewport(0, 0, texWidth, texHeight);
            // On some special device, the front camera orientation is 90, so we need to use another matrix
            mFullScreenFUDisplay.drawFrame(texId, mInputPropOrientation == 270 ? mMvpMtx270 : mMvpMtx90);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(originalViewPort[0], originalViewPort[1], originalViewPort[2], originalViewPort[3]);

            int fuTex = onDrawFrame(cameraNV21Byte, fboTex[0], texHeight, texWidth);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[1]);
            GLES20.glViewport(0, 0, texWidth, texHeight);
            mFullScreenFUDisplay.drawFrame(fuTex, mInputPropOrientation == 270 ? mMvpMtx90 : mMvpMtx270);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(originalViewPort[0], originalViewPort[1], originalViewPort[2], originalViewPort[3]);

            return fboTex[1];
        } else {
            //如果是后置摄像头先旋转90度,在左右镜像
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
            GLES20.glViewport(0, 0, texWidth, texHeight);
            mFullScreenFUDisplay.drawFrame(texId, mMvpMtx90);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(originalViewPort[0], originalViewPort[1], originalViewPort[2], originalViewPort[3]);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[1]);
            GLES20.glViewport(0, 0, texWidth, texHeight);
            float[] matrix = new float[16];
            Matrix.setIdentityM(matrix, 0);
            matrix[5] = -1.0f;
            mFullScreenFUDisplay.drawFrame(fboTex[0], matrix);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(originalViewPort[0], originalViewPort[1], originalViewPort[2], originalViewPort[3]);

            int fuTex = onDrawFrame(cameraNV21Byte, fboTex[1], texHeight, texWidth);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[2]);
            GLES20.glViewport(0, 0, texWidth, texHeight);
            mFullScreenFUDisplay.drawFrame(fuTex, mMvpMtx270);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(originalViewPort[0], originalViewPort[1], originalViewPort[2], originalViewPort[3]);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[3]);
            GLES20.glViewport(0, 0, texWidth, texHeight);
            float[] mMvpMtx = new float[16];
            Matrix.setIdentityM(mMvpMtx, 0);
            mMvpMtx[0] = -1.0f;
            mFullScreenFUDisplay.drawFrame(fboTex[2], mMvpMtx);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(originalViewPort[0], originalViewPort[1], originalViewPort[2], originalViewPort[3]);
            return fboTex[3];
        }
    }

    private int fboTex[];
    private int renderBufferId[];

    private int fboWidth, fboHeight;

    private void createFBO(int width, int height) {
        if (fboTex != null && (fboWidth != width || fboHeight != height)) {
            deleteFBO();
        }

        fboWidth = width;
        fboHeight = height;

        if (fboTex == null) {
            fboId = new int[4];
            fboTex = new int[4];
            renderBufferId = new int[4];

//generate fbo id
            GLES20.glGenFramebuffers(4, fboId, 0);
//generate texture
            GLES20.glGenTextures(4, fboTex, 0);
//generate render buffer
            GLES20.glGenRenderbuffers(4, renderBufferId, 0);

            for (int i = 0; i < fboId.length; i++) {
//Bind Frame buffer
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[i]);
//Bind texture
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTex[i]);
//Define texture parameters
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//Bind render buffer and define buffer dimension
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId[i]);
                GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
//Attach texture FBO color attachment
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex[i], 0);
//Attach render buffer to depth attachment
                GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId[i]);
//we are done, reset
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }
    }

    private void deleteFBO() {
        if (fboId == null || fboTex == null || renderBufferId == null) {
            return;
        }
        GLES20.glDeleteFramebuffers(2, fboId, 0);
        GLES20.glDeleteTextures(2, fboTex, 0);
        GLES20.glDeleteRenderbuffers(2, renderBufferId, 0);
        fboId = null;
        fboTex = null;
        renderBufferId = null;
    }
}
