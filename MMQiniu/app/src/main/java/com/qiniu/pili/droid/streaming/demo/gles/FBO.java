package com.qiniu.pili.droid.streaming.demo.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.qiniu.pili.droid.streaming.demo.R;
import com.qiniu.pili.droid.streaming.demo.filter.CameraFilterBeauty;
import com.qiniu.pili.droid.streaming.demo.filter.NormalFilter;
import com.qiniu.pili.droid.streaming.demo.utils.Config;

import java.nio.ByteBuffer;

public class FBO {
    private static final String TAG = "FBO";
    private boolean mEnable = Config.FILTER_ENABLED;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private static int[] mCurveArrays = new int[]{
            R.raw.cross_1, R.raw.cross_2, R.raw.cross_3, R.raw.cross_4, R.raw.cross_5,
            R.raw.cross_6, R.raw.cross_7, R.raw.cross_8, R.raw.cross_9, R.raw.cross_10,
            R.raw.cross_11,
    };
    private int mCurveIndex = 10;

    // Used for off-screen rendering.
    private int mOffscreenTexture;
    private int mFramebuffer;
    private FullFrameRect mFullScreen;
    private FullFrameRect mNormal;

    public void updateSurfaceSize(int width, int height) {
        if (!mEnable) {
            return;
        }

        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    public void initialize(Context context) {
        if (!mEnable) {
            return;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);
        }
        if (mNormal != null) {
            mNormal.release(false);
        }

        /**
         * Create a new full frame renderer with beauty filter.
         * There are two another filter, you can have a try.
         */
//        mFullScreen = new FullFrameRect(new CameraFilterToneCurve(context,
//                context.getResources().openRawResource(mCurveArrays[mCurveIndex])));
//        mFullScreen = new FullFrameRect(new CameraFilterMosaic(context));
        mFullScreen = new FullFrameRect(new CameraFilterBeauty(context));
        mNormal = new FullFrameRect(new NormalFilter(context));

        mOffscreenTexture = 0;
    }

    public void release() {
        if (!mEnable) {
            return;
        }
        mFullScreen.release(true);
        mNormal.release(true);
    }

    /**
     * Prepares the off-screen framebuffer.
     */
    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("start");
        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mOffscreenTexture = values[0];   // expected > 0
        Log.i(TAG, "prepareFramebuffer mOffscreenTexture:" + mOffscreenTexture);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture);
        GlUtil.checkGlError("glBindTexture");

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0);

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // Switch back to the default framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GlUtil.checkGlError("glBindFramebuffer");
    }

    private ByteBuffer byteBuffer;
    private int lastTexWidth = -1;
    private int lastTexHeight = -1;

    /**
     * ?????????????????????????????????????????????????????????????????????????????????texId??? oes texture??? ?????????????????????270??????????????????
     * ???????????????????????????????????????????????????????????????????????????270????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????????????????
     * ??????????????????????????????oes texture?????????matrix?????????????????????
     *
     * @param texId
     * @param texWidth
     * @param texHeight
     * @param onDrawFrameListener
     * @param isFrontCamera
     * @return
     */
    public int drawFrame(int texId, int texWidth, int texHeight, OnDrawFrameListener onDrawFrameListener,
                         boolean isFrontCamera) {

        if (!mEnable) {
            return texId;
        }
        //camera????????????????????????90?????????
        int renderWidth = texHeight;
        int renderHeight = texWidth;

        GLES20.glViewport(0, 0, renderWidth, renderHeight);
        if (mOffscreenTexture == 0) {
            prepareFramebuffer(renderWidth, renderHeight);
        }
        //???????????????????????????
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        mNormal.getFilter().setTextureSize(renderWidth, renderHeight);
        if (isFrontCamera) {
            mNormal.drawFrame(texId, 270);
        } else {
            mNormal.drawFrame(texId, 90);
        }

        int resultTextureId = mOffscreenTexture;

        //????????????SDK?????????????????????
        boolean needDataBuffer = false;
        if (onDrawFrameListener != null) {
            //????????????
            resultTextureId = onDrawFrameListener.onDrawFrame(mOffscreenTexture, texWidth, texHeight, renderWidth, renderHeight);
            //?????????????????????
//            if (needDataBuffer) {
//                //??????buffer
//                if (lastTexWidth != renderWidth || lastTexHeight != renderHeight) {
//                    byteBuffer = ByteBuffer.allocate(renderWidth * renderHeight * 4);
//                    lastTexWidth = renderWidth;
//                    lastTexHeight = renderHeight;
//                }
//                byteBuffer.position(0);
//
//                //??????????????????
//                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
//                GLES20.glViewport(0, 0, renderWidth, renderHeight);
//                mNormal.getFilter().setTextureSize(renderWidth, renderHeight);
//                if (resultTextureId == mOffscreenTexture) {
//                    //????????????????????????buffer???????????????
//                    GLES30.glReadPixels(0, 0, renderWidth, renderHeight, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
//                } else {
//                    //?????????????????????????????????????????????
//                    //?????????????????????????????????mOffscreenTexture???????????????buffer
//                    mNormal.drawFrame(resultTextureId, 0);
//                    GLES30.glReadPixels(0, 0, renderWidth, renderHeight, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
//                }
//                resultTextureId = mOffscreenTexture;
//            }
        }
        //??????????????????????????????
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        return resultTextureId;
    }

    public interface OnDrawFrameListener {
        int onDrawFrame(int texId, int dataWidth, int dataHeight, int textureWidth, int textureHeight);
    }
}
