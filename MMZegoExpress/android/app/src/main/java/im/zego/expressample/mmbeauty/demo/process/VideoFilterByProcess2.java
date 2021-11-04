package im.zego.expressample.mmbeauty.demo.process;
import android.util.Log;

import com.cosmos.thirdlive.ZegoBeautyManager;

import im.zego.expressample.mmbeauty.demo.application.ZegoApplication;
import im.zego.expressample.mmbeauty.demo.util.ZegoUtil;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoCustomVideoProcessHandler;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
/**
 * VideoFilterByProcess2
 * 通过Zego视频前处理，用户可以获取到Zego SDK采集到的摄像头数据。用户后续将数据塞给mmbeauty处理，最终将处理后的数据塞回Zego SDK进行推流。
 * 采用GL_TEXTURE_2D方式传递数据
 */
/**
 * VideoFilterByProcess2
 * Through the Zego video pre-processing, users can obtain the camera data collected by the Zego SDK. The user then stuffs the data to mmbeauty for processing, and finally stuffs the processed data back to Zego SDK for publishing stream.
 *Use GL_TEXTURE_2D to transfer data
 */
public class VideoFilterByProcess2 extends IZegoCustomVideoProcessHandler {

    // mm 美颜处理类
    private ZegoBeautyManager beautyManager;
    private boolean isFrontCamera = true;

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    public VideoFilterByProcess2(ZegoBeautyManager fuRenderer){
        Log.e(ZegoUtil.VIDEO_FILTER_TAG,"Zego Custom Video Process + Texture2D");
        this.beautyManager = fuRenderer;
    }

    @Override
    public void onStart(ZegoPublishChannel channel) {//切换摄像头会回调
        if (beautyManager == null) {
            beautyManager = new ZegoBeautyManager(ZegoApplication.zegoApplication);
        }
    }

    /**
     * 释放资源
     *
     */

    public void stopAndDeAllocate() {
    }

    @Override
    public void onStop(ZegoPublishChannel channel) {//切换摄像头会回调
        super.onStop(channel);
        if (beautyManager != null) {
            beautyManager.stopOrientationCallback();
            beautyManager.textureDestoryed();
            beautyManager = null;
        }
    }

    @Override
    public void onCapturedUnprocessedTextureData(int textureID, int width, int height, long referenceTimeMillisecond, ZegoPublishChannel channel) {
        int fuTextureId = beautyManager.renderWithTexture(textureID,width,height,isFrontCamera);

        ZegoExpressEngine.getEngine().sendCustomVideoProcessedTextureData(fuTextureId,width,height,referenceTimeMillisecond);
    }


}
