package im.zego.expressample.mmbeauty.demo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;


import org.json.JSONObject;

import java.util.Date;


import im.zego.expressample.mmbeauty.demo.GetAppIDConfig;
import com.cosmos.thirdlive.ZegoBeautyManager;

import im.zego.expressample.mmbeauty.demo.process.VideoFilterByProcess;
import im.zego.expressample.mmbeauty.demo.process.VideoFilterByProcess2;
import im.zego.expressample.mmbeauty.demo.view.CustomDialog;
import im.zego.expresssample.mmbeauty.demo.R;
import im.zego.expresssample.mmbeauty.demo.databinding.ActivityFuBaseBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoCustomVideoCaptureHandler;
import im.zego.zegoexpress.callback.IZegoCustomVideoProcessHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoCustomVideoProcessConfig;
import im.zego.zegoexpress.entity.ZegoUser;


/**
 * 带美颜的推流界面
 */

public class FUBeautyActivity extends Activity{
    private ActivityFuBaseBinding binding;

    private ViewStub mBottomViewStub;
    protected ZegoBeautyManager beautyManager;

    // 房间 ID
    private String mRoomID = "";

    private ZegoExpressEngine engine;
    private ZegoVideoBufferType videoBufferType;
    boolean isFront = true;//是否前置摄像头

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_fu_base);

        mBottomViewStub = (ViewStub) findViewById(R.id.fu_base_bottom);
        mBottomViewStub.setInflatedId(R.id.fu_base_bottom);

        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        beautyManager = new ZegoBeautyManager(this);
        mBottomViewStub.setLayoutResource(R.layout.layout_fu_beauty);
        mBottomViewStub.inflate();

        mRoomID = getIntent().getStringExtra("roomID");

        videoBufferType=ZegoVideoBufferType.getZegoVideoBufferType(getIntent().getIntExtra("videoBufferType", 0));

        // 初始化SDK
        initSDK();
        findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFront = !isFront;
                ((VideoFilterByProcess2)videoFilterByProcess).setFrontCamera(isFront);
                ZegoExpressEngine.getEngine().useFrontCamera(isFront);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        if (videoCaptureFromCamera != null) {
            videoCaptureFromCamera.onStop(ZegoPublishChannel.MAIN);
        }
        if(videoFilterByProcess!=null&&videoBufferType==ZegoVideoBufferType.SURFACE_TEXTURE){
            ((VideoFilterByProcess)videoFilterByProcess).stopAndDeAllocate();
        }
        if(videoFilterByProcess!=null&&videoBufferType==ZegoVideoBufferType.GL_TEXTURE_2D){
            ((VideoFilterByProcess2)videoFilterByProcess).stopAndDeAllocate();
        }

        ZegoExpressEngine.getEngine().setCustomVideoCaptureHandler(null);
        // 停止预览
        ZegoExpressEngine.getEngine().stopPreview();

        // 在退出页面时停止推流
        ZegoExpressEngine.getEngine().stopPublishingStream();

        // 登出房间
        ZegoExpressEngine.getEngine().logoutRoom(mRoomID);

        ZegoExpressEngine.getEngine().setEventHandler(null);
        ZegoExpressEngine.destroyEngine(null);

    }

    IZegoCustomVideoCaptureHandler videoCaptureFromCamera;
    IZegoCustomVideoProcessHandler videoFilterByProcess;
    /**
     * 初始化SDK逻辑
     * 初始化成功后登录房间并推流
     */
    private void initSDK() {

        Log.i("ZegoExpressEngine Version", ZegoExpressEngine.getVersion());

        // 设置外部滤镜---必须在初始化 ZEGO SDK 之前设置，否则不会回调   SyncTexture
        engine=ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.LIVE, this.getApplication(), null);
        if(!VideoFilterMainUI.useExpressCustomCapture&&videoBufferType == ZegoVideoBufferType.SURFACE_TEXTURE){
            videoFilterByProcess =new VideoFilterByProcess(beautyManager);
            ((VideoFilterByProcess)videoFilterByProcess).setFrontCamera(isFront);
        }else if(!VideoFilterMainUI.useExpressCustomCapture&&videoBufferType == ZegoVideoBufferType.GL_TEXTURE_2D){
            videoFilterByProcess =new VideoFilterByProcess2(beautyManager);
            ((VideoFilterByProcess2)videoFilterByProcess).setFrontCamera(isFront);
        }
        ZegoExpressEngine.getEngine().useFrontCamera(isFront);
        if(VideoFilterMainUI.useExpressCustomCapture) {
            ZegoCustomVideoCaptureConfig zegoCustomVideoCaptureConfig = new ZegoCustomVideoCaptureConfig();
            zegoCustomVideoCaptureConfig.bufferType = videoBufferType;
            engine.enableCustomVideoCapture(true, zegoCustomVideoCaptureConfig);

            ZegoExpressEngine.getEngine().setCustomVideoCaptureHandler(videoCaptureFromCamera);
        }else{
            ZegoCustomVideoProcessConfig zegoCustomVideoProcessConfig =new ZegoCustomVideoProcessConfig();
            zegoCustomVideoProcessConfig.bufferType =videoBufferType;
            engine.enableCustomVideoProcessing(true,zegoCustomVideoProcessConfig);
            ZegoExpressEngine.getEngine().setCustomVideoProcessHandler(videoFilterByProcess);
        }
        // }
        // 初始化成功，登录房间并推流
        startPublish();

    }

    public void startPublish() {

        // 防止用户点击，弹出加载对话框
        CustomDialog.createDialog("登录房间中...", this).show();

        String randomSuffix = String.valueOf(new Date().getTime() % (new Date().getTime() / 1000));
        String userID = "user" + randomSuffix;
        String userName = "user" + randomSuffix;
        ZegoExpressEngine.getEngine().loginRoom(mRoomID, new ZegoUser(userID, userName));

        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                CustomDialog.createDialog(FUBeautyActivity.this).cancel();
                if (errorCode == 0) {
                    ZegoCanvas preCanvas =new ZegoCanvas(binding.preview);
                    preCanvas.viewMode= ZegoViewMode.ASPECT_FILL;
                    ZegoExpressEngine.getEngine().startPreview(preCanvas);
                    // 开始推流
                    ZegoExpressEngine.getEngine().startPublishingStream(roomID);
                } else {

                    Toast.makeText(FUBeautyActivity.this, "login room failure", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                // 推流状态更新，errorCode 非0 则说明推流失败
                // 推流常见错误码请看文档: <a>https://doc.zego.im/CN/308.html</a>

                if (errorCode == 0) {

                    Toast.makeText(FUBeautyActivity.this, getString(R.string.tx_publish_success), Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(FUBeautyActivity.this, getString(R.string.tx_publish_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
