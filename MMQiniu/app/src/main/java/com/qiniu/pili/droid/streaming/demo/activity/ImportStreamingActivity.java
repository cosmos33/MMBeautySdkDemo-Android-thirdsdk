package com.qiniu.pili.droid.streaming.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qiniu.pili.droid.streaming.AVCodecType;
import com.qiniu.pili.droid.streaming.StreamStatusCallback;
import com.qiniu.pili.droid.streaming.StreamingManager;
import com.qiniu.pili.droid.streaming.StreamingProfile;
import com.qiniu.pili.droid.streaming.StreamingSessionListener;
import com.qiniu.pili.droid.streaming.StreamingState;
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener;
import com.qiniu.pili.droid.streaming.demo.R;
import com.qiniu.pili.droid.streaming.demo.core.ExtAudioCapture;
import com.qiniu.pili.droid.streaming.demo.core.ExtVideoCapture;
import com.qiniu.pili.droid.streaming.demo.plain.EncodingConfig;
import com.qiniu.pili.droid.streaming.demo.utils.Config;
import com.qiniu.pili.droid.streaming.demo.utils.Util;

import java.net.URISyntaxException;
import java.util.List;

public class ImportStreamingActivity extends Activity {
    private static final String TAG = "ImportStreamingActivity";

    private SurfaceView mSurfaceView;
    private TextView mLogTextView;
    private TextView mStatusTextView;
    private TextView mStatView;
    private Button mShutterButton;

    private String mStatusMsgContent;
    private String mLogContent = "\n";

    private boolean mShutterButtonPressed = false;
    private String mPublishUrl;
    private boolean mIsQuicEnabled;
    private boolean mIsReady;

    private ExtAudioCapture mExtAudioCapture;
    private ExtVideoCapture mExtVideoCapture;

    private EncodingConfig mEncodingConfig;

    private StreamingManager mStreamingManager;
    private StreamingProfile mProfile;

    // ???????????????????????????
    private Handler mSubThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ??????????????????????????????
        mEncodingConfig = (EncodingConfig) getIntent().getSerializableExtra(Config.NAME_ENCODING_CONFIG);

        Intent intent = getIntent();
        mPublishUrl = intent.getStringExtra(Config.PUBLISH_URL);
        mIsQuicEnabled = intent.getBooleanExtra(Config.TRANSFER_MODE_QUIC, false);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mSubThreadHandler = new Handler(handlerThread.getLooper());

        // ?????????????????????
        initView();
        // ?????????????????????????????????
        initExtCapture();
        // ????????? StreamingProfile???StreamingProfile ????????????????????????????????????????????? https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4
        initEncodingProfile();
        // ????????? MediaStreamingManager???????????????????????? https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#6
        initStreamingManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mExtAudioCapture.startCapture();
        mStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mExtAudioCapture.stopCapture();
        mStreamingManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubThreadHandler != null) {
            mSubThreadHandler.getLooper().quit();
        }
        // ???????????? Manager ?????????
        mStreamingManager.destroy();
    }

    /**
     * ??????????????????????????? demo ?????????????????????
     */
    public void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(Config.SCREEN_ORIENTATION);
        setContentView(R.layout.activity_import_streaming);
        mSurfaceView = (SurfaceView) findViewById(R.id.ext_camerapreview_surfaceview);
        mLogTextView = (TextView) findViewById(R.id.log_info);
        mStatusTextView = (TextView) findViewById(R.id.streamingStatus);
        mStatView = (TextView) findViewById(R.id.stream_status);
        mShutterButton = (Button) findViewById(R.id.toggleRecording_button);

        mShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsReady) {
                    Toast.makeText(ImportStreamingActivity.this, "????????? READY ???????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mShutterButtonPressed) {
                    stopStreamingInternal();
                } else {
                    startStreamingInternal();
                }
            }
        });
    }

    /**
     * ???????????????????????????
     */
    private void initExtCapture() {
        mExtVideoCapture = new ExtVideoCapture(mSurfaceView);
        mExtVideoCapture.setOnPreviewFrameCallback(mOnPreviewFrameCallback);
        mExtAudioCapture = new ExtAudioCapture();
        mExtAudioCapture.setOnAudioFrameCapturedListener(mOnAudioFrameCapturedListener);
    }

    /**
     * ???????????????????????? {@link StreamingProfile}
     */
    private void initEncodingProfile() {
        mProfile = new StreamingProfile();
        // ??????????????????
        try {
            mProfile.setPublishUrl(mPublishUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        // ???????????? QUIC ?????????
        // QUIC ????????? UDP ????????????????????????????????????????????????????????????????????????????????? TCP ??????????????????????????????????????????????????????
        mProfile.setQuicEnable(mIsQuicEnabled);

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        StreamingProfile.AudioProfile aProfile = null;
        // ??????????????????????????????????????????GOP ?????? H264 Profile ?????????????????????????????????????????????????????????
        StreamingProfile.VideoProfile vProfile = null;

        if (!mEncodingConfig.mIsAudioOnly) {
            // ????????????????????????
            if (mEncodingConfig.mIsVideoQualityPreset) {
                // ?????????????????????????????????
                // ???????????????????????? https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4 ??? 4.2 ??????
                mProfile.setVideoQuality(mEncodingConfig.mVideoQualityPreset);
            } else {
                // ??????????????????????????????????????????????????????????????????????????????????????????
                vProfile = new StreamingProfile.VideoProfile(
                        mEncodingConfig.mVideoQualityCustomFPS,
                        mEncodingConfig.mVideoQualityCustomBitrate * 1024,
                        mEncodingConfig.mVideoQualityCustomMaxKeyFrameInterval,
                        mEncodingConfig.mVideoQualityCustomProfile
                );
            }

            // ????????????????????????
            if (mEncodingConfig.mIsVideoSizePreset) {
                // ???????????????????????????
                // ???????????????????????? https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4 ??? 4.7 ??????
                mProfile.setEncodingSizeLevel(mEncodingConfig.mVideoSizePreset);
            } else {
                // ????????????????????????????????????????????????????????????????????????????????????
                mProfile.setPreferredVideoEncodingSize(mEncodingConfig.mVideoSizeCustomWidth, mEncodingConfig.mVideoSizeCustomHeight);
            }

            // ???????????? Orientation
            mProfile.setEncodingOrientation(mEncodingConfig.mVideoOrientationPortrait ? StreamingProfile.ENCODING_ORIENTATION.PORT : StreamingProfile.ENCODING_ORIENTATION.LAND);
            // ???????????????????????????????????????
            // QUALITY_PRIORITY ???????????????????????????????????????????????????????????????????????????
            // BITRATE_PRIORITY ???????????????????????????????????????????????????
            mProfile.setEncoderRCMode(mEncodingConfig.mVideoRateControlQuality ? StreamingProfile.EncoderRCModes.QUALITY_PRIORITY : StreamingProfile.EncoderRCModes.BITRATE_PRIORITY);
            // ??????????????????????????????
            mProfile.setFpsControllerEnable(mEncodingConfig.mVideoFPSControl);
            mProfile.setYuvFilterMode(mEncodingConfig.mYuvFilterMode);
            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????? 150kbps ~ 2000kbps ????????????????????????
            mProfile.setBitrateAdjustMode(mEncodingConfig.mBitrateAdjustMode);
            if (mEncodingConfig.mBitrateAdjustMode == StreamingProfile.BitrateAdjustMode.Auto) {
                mProfile.setVideoAdaptiveBitrateRange(mEncodingConfig.mAdaptiveBitrateMin * 1024, mEncodingConfig.mAdaptiveBitrateMax * 1024);
            }
        }

        // ????????????????????????
        if (mEncodingConfig.mIsAudioQualityPreset) {
            // ?????????????????????????????????
            // ???????????????????????? https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4 ??? 4.3 ??????
            mProfile.setAudioQuality(mEncodingConfig.mAudioQualityPreset);
        } else {
            // ?????????????????????????????????
            aProfile = new StreamingProfile.AudioProfile(
                    mEncodingConfig.mAudioQualityCustomSampleRate,
                    mEncodingConfig.mAudioQualityCustomBitrate * 1024
            );
        }

        // ????????????????????????????????????
        if (aProfile != null || vProfile != null) {
            StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);
            mProfile.setAVProfile(avProfile);
        }

        // ???????????????
        mProfile.setDnsManager(Util.getMyDnsManager(this))
                .setStreamStatusConfig(new StreamingProfile.StreamStatusConfig(3))
                .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000));
    }

    /**
     * ????????????????????????
     */
    private void initStreamingManager() {
        mStreamingManager = new StreamingManager(this, AVCodecType.HW_VIDEO_YUV_AS_INPUT_WITH_HW_AUDIO_CODEC);
        mStreamingManager.prepare(mProfile);
        mStreamingManager.setStreamingSessionListener(mStreamingSessionListener);
        mStreamingManager.setStreamStatusCallback(mStreamStatusCallback);
        mStreamingManager.setStreamingStateListener(mStreamingStateChangedListener);
    }

    private ExtVideoCapture.OnPreviewFrameCallback mOnPreviewFrameCallback = new ExtVideoCapture.OnPreviewFrameCallback() {
        @Override
        public void onPreviewFrameCaptured(byte[] data, int width, int height, int orientation, boolean mirror, int fmt, long tsInNanoTime) {
            mStreamingManager.inputVideoFrame(data, width, height, orientation, false, fmt, tsInNanoTime);
        }
    };

    private ExtAudioCapture.OnAudioFrameCapturedListener mOnAudioFrameCapturedListener = new ExtAudioCapture.OnAudioFrameCapturedListener() {
        @Override
        public void onAudioFrameCaptured(byte[] audioData) {
            long timestamp = System.nanoTime();
            mStreamingManager.inputAudioFrame(audioData, timestamp, false);
        }
    };

    /**
     * ???????????????????????????????????????
     */
    private StreamingSessionListener mStreamingSessionListener = new StreamingSessionListener() {
        /**
         * ????????????????????????????????????
         *
         * @param code ?????????
         * @return true ??????????????????????????????????????????????????????
         */
        @Override
        public boolean onRecordAudioFailedHandled(int code) {
            return false;
        }

        /**
         * ????????????????????????????????????????????????????????????????????????????????????
         *
         * ??????????????????????????????????????? StreamingState#DISCONNECTED ??????????????????????????????????????????????????????
         *
         * @param code ?????????
         * @return true ?????????????????????????????????????????????????????????????????????????????? StreamingState#SHUTDOWN ????????????
         */
        @Override
        public boolean onRestartStreamingHandled(int code) {
            Log.i(TAG, "onRestartStreamingHandled");
            return false;
        }

        /**
         * ?????????????????????????????????????????????????????????????????????
         */
        @Override
        public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
            return null;
        }

        /**
         * ??????????????????????????????????????????????????????????????????
         */
        @Override
        public int onPreviewFpsSelected(List<int[]> list) {
            return 0;
        }
    };

    /**
     * ???????????????????????????????????????????????????????????????????????????
     * <p>
     * ????????????????????? UI ?????????UI ????????????????????????????????????
     */
    private StreamStatusCallback mStreamStatusCallback = new StreamStatusCallback() {
        @Override
        public void notifyStreamStatusChanged(final StreamingProfile.StreamStatus streamStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatView.setText("bitrate:" + streamStatus.totalAVBitrate / 1024 + " kbps"
                            + "\naudio:" + streamStatus.audioFps + " fps"
                            + "\nvideo:" + streamStatus.videoFps + " fps");
                }
            });
        }
    };

    /**
     * ??????????????????????????????
     */
    private StreamingStateChangedListener mStreamingStateChangedListener = new StreamingStateChangedListener() {
        @Override
        public void onStateChanged(StreamingState streamingState, Object extra) {
            Log.i(TAG, "StreamingState streamingState:" + streamingState + ",extra:" + extra);
            switch (streamingState) {
                case PREPARING:
                    mStatusMsgContent = getString(R.string.string_state_preparing);
                    break;
                case READY:
                    /**
                     * ??????????????????????????????????????? READY ??????????????????
                     */
                    mIsReady = true;
                    mStatusMsgContent = getString(R.string.string_state_ready);
                    break;
                case CONNECTING:
                    mStatusMsgContent = getString(R.string.string_state_connecting);
                    break;
                case STREAMING:
                    mStatusMsgContent = getString(R.string.string_state_streaming);
                    setShutterButtonEnabled(true);
                    setShutterButtonPressed(true);
                    break;
                case SHUTDOWN:
                    mStatusMsgContent = getString(R.string.string_state_ready);
                    setShutterButtonEnabled(true);
                    setShutterButtonPressed(false);
                    break;
                case IOERROR:
                    /**
                     * ??? `startStreaming` ???????????????????????????????????????????????????
                     * ???????????????????????????????????????????????????????????????
                     */
                    mLogContent += "IOERROR\n";
                    mStatusMsgContent = getString(R.string.string_state_ready);
                    setShutterButtonEnabled(true);
                    startStreamingInternal(2000);
                    break;
                case DISCONNECTED:
                    /**
                     * ??????????????????????????????????????????????????????????????? `onRestartStreamingHandled` ???????????????????????????
                     */
                    mLogContent += "DISCONNECTED\n";
                    break;
                case UNKNOWN:
                    mStatusMsgContent = getString(R.string.string_state_ready);
                    break;
                case SENDING_BUFFER_EMPTY:
                case SENDING_BUFFER_FULL:
                case AUDIO_RECORDING_FAIL:
                    break;
                case INVALID_STREAMING_URL:
                    Log.e(TAG, "Invalid streaming url:" + extra);
                    break;
                case UNAUTHORIZED_STREAMING_URL:
                    Log.e(TAG, "Unauthorized streaming url:" + extra);
                    mLogContent += "Unauthorized Url\n";
                    break;
                case UNAUTHORIZED_PACKAGE:
                    mLogContent += "Unauthorized package\n";
                    break;
                case START_VIDEO_ENCODER_FAIL:
                case VIDEO_ENCODER_ERROR:
                case START_AUDIO_ENCODER_FAIL:
                case AUDIO_ENCODER_ERROR:
                    /**
                     * ????????? START_VIDEO_ENCODER_FAIL???VIDEO_ENCODER_ERROR???START_AUDIO_ENCODER_FAIL???
                     * AUDIO_ENCODER_ERROR ??????????????????????????????????????????????????????????????????????????????????????????
                     * ???????????????????????????????????????????????????????????????????????? {@link AVCodecType} ???????????????????????????
                     */
                    mStatusMsgContent = getString(R.string.string_state_ready);
                    setShutterButtonEnabled(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImportStreamingActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLogTextView != null) {
                        mLogTextView.setText(mLogContent);
                    }
                    mStatusTextView.setText(mStatusMsgContent);
                }
            });
        }
    };

    /**
     * ????????????
     * ?????????????????????????????????????????? onStateChanged.READY ??????????????????????????????
     */
    private void startStreamingInternal() {
        startStreamingInternal(0);
    }

    private void startStreamingInternal(long delayMillis) {
        if (mStreamingManager == null) {
            return;
        }
        setShutterButtonEnabled(false);
        // startStreaming ?????????????????????????????????????????????
        if (mSubThreadHandler != null) {
            mSubThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final boolean res = mStreamingManager.startStreaming();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setShutterButtonPressed(res);
                            if (!res) {
                                setShutterButtonEnabled(true);
                            }
                        }
                    });
                }
            }, delayMillis);
        }
    }

    /**
     * ????????????
     */
    private void stopStreamingInternal() {
        if (mShutterButtonPressed) {
            // disable the shutter button before stopStreaming
            setShutterButtonEnabled(false);
            boolean res = mStreamingManager.stopStreaming();
            if (!res) {
                mShutterButtonPressed = true;
                setShutterButtonEnabled(true);
            }
            setShutterButtonPressed(mShutterButtonPressed);
        }
    }

    private void setShutterButtonEnabled(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShutterButton.setFocusable(enable);
                mShutterButton.setClickable(enable);
                mShutterButton.setEnabled(enable);
            }
        });
    }

    private void setShutterButtonPressed(final boolean pressed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShutterButtonPressed = pressed;
                mShutterButton.setPressed(pressed);
            }
        });
    }
}
