package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.cosmos.thirdlive.AgoraBeautyManager;
import com.immomo.medialog.thread.MainThreadExecutor;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.processors.IPreprocessor;
import io.agora.capture.video.camera.VideoCaptureFrame;

public class PreprocessorMMBeauty implements IPreprocessor {
    private final static String TAG = PreprocessorMMBeauty.class.getSimpleName();

    private AgoraBeautyManager render;
    private Context mContext;
    private boolean mEnabled;
    private boolean isFrontCamera = true;

    public PreprocessorMMBeauty(Context context) {
        mContext = context;
        mEnabled = true;
    }

    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (render == null || !mEnabled) {
            return outFrame;
        }

        int renderTex = render.renderWithOESTexture(outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight(), isFrontCamera,outFrame.rotation);
        // The texture is transformed to texture2D by beauty module.
        if (renderTex != outFrame.textureId) {
            outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
            outFrame.textureId = renderTex;
        }
        return outFrame;
    }

    @Override
    public void initPreprocessor() {
        // only call once when app launched
        Log.e(TAG, "initPreprocessor: ");
        MainThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (render == null) {
                    render = new AgoraBeautyManager(mContext);
                }
            }
        });
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        // not called
        Log.d(TAG, "releasePreprocessor: ");
        //这个可以不写，在FUChatActivity中添加了
//        if (mFURenderer != null) {
//            mFURenderer.onSurfaceDestroyed();
//        }
        if (render != null) {
            render.textureDestoryed();
        }
    }

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }
}
