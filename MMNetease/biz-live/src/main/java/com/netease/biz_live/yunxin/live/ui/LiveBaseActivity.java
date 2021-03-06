/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.biz_live.yunxin.live.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.biz_live.BuildConfig;
import com.netease.biz_live.R;
import com.netease.biz_live.yunxin.live.audience.utils.InputUtils;
import com.netease.biz_live.yunxin.live.audience.utils.StringUtils;
import com.netease.biz_live.yunxin.live.chatroom.control.Anchor;
import com.netease.biz_live.yunxin.live.chatroom.control.ChatRoomNotify;
import com.netease.biz_live.yunxin.live.chatroom.control.SkeletonChatRoomNotify;
import com.netease.biz_live.yunxin.live.chatroom.custom.AnchorCoinChangedAttachment;
import com.netease.biz_live.yunxin.live.chatroom.model.AudienceInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.LiveChatRoomInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.RewardGiftInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.RoomMsg;
import com.netease.biz_live.yunxin.live.constant.LiveType;
import com.netease.biz_live.yunxin.live.dialog.AnchorMoreDialog;
import com.netease.biz_live.yunxin.live.dialog.ChoiceDialog;
import com.netease.biz_live.yunxin.live.dialog.DumpDialog;
import com.netease.biz_live.yunxin.live.dialog.LiveSettingDialog;
import com.netease.biz_live.yunxin.live.liveroom.LiveRoomCallback;
import com.netease.biz_live.yunxin.live.liveroom.NERTCAnchorBaseLiveRoomDelegate;
import com.netease.biz_live.yunxin.live.liveroom.NERTCAnchorLiveRoom;
import com.netease.biz_live.yunxin.live.liveroom.NERTCLiveRoom;
import com.netease.biz_live.yunxin.live.model.LiveInfo;
import com.netease.biz_live.yunxin.live.model.message.MsgReward;
import com.netease.biz_live.yunxin.live.network.LiveInteraction;
import com.netease.biz_live.yunxin.live.ui.widget.AnchorPreview;
import com.netease.biz_live.yunxin.live.ui.widget.AudiencePortraitRecyclerView;
import com.netease.biz_live.yunxin.live.ui.widget.ChatRoomMsgRecyclerView;
import com.netease.lava.nertc.sdk.NERtc;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.lava.nertc.sdk.video.NERtcVideoConfig;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.demo.basic.BaseActivity;
import com.netease.yunxin.nertc.demo.basic.StatusBarConfig;
import com.netease.yunxin.nertc.demo.utils.ViewUtils;

import java.util.List;

import io.reactivex.observers.ResourceSingleObserver;

/**
 * Pk????????????????????????
 */
public abstract class LiveBaseActivity extends BaseActivity implements NERTCAnchorBaseLiveRoomDelegate {

    private static final String LOG_TAG = "LiveBaseActivity";

    NERtcVideoView videoView;

    NERTCAnchorLiveRoom liveRoom;

    //*******************????????????*******************
    private int videoProfile = NERtcConstants.VideoProfile.HD720P;//???????????????

    private NERtcVideoConfig.NERtcVideoFrameRate frameRate = NERtcVideoConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_30;//??????

    private int audioScenario = NERtcConstants.AudioScenario.MUSIC;//????????????

    /**
     * ????????????
     */
    protected BeautyControl beautyControl;


    /**
     * ?????????????????????
     */
    protected LiveInfo liveInfo;


    protected int cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;//?????????FACE_BACK = 0, FACE_FRONT = 1

    //????????????
    protected AnchorPreview preview;
    //***************************?????????UI**********************

    private ConstraintLayout clyLivingView;

    /**
     * ?????????????????????
     */
    protected ChatRoomMsgRecyclerView roomMsgView;
    /**
     * ??????????????????
     */
    private AudiencePortraitRecyclerView audiencePortraitView;
    /**
     * ????????????
     */
    private TextView tvAudienceCount;

    /**
     * ????????????
     */
    protected TextView tvCoinCount;

    private TextView tvNickName;//????????????

    private ImageView ivPortrait;//????????????

    private EditText edtInput;

    private TextView tvInput;

    private ImageView ivBeauty;

    protected ImageView ivConnect;

    protected View redPoint;

    private ImageView ivMusic;

    private ImageView ivMore;

    //***************************?????????UI end**********************


    //????????????
    protected FrameLayout flyContainer;

    //????????????
    protected AudioControl audioControl;

    /**
     * ?????????
     */
    protected Anchor anchor = Anchor.getInstance();

    protected boolean isLiveStart = false;

    /**
     * ?????????????????????
     */
    private ChatRoomNotify chatRoomNotify = new SkeletonChatRoomNotify() {

        @Override
        public void onJoinRoom(boolean success, int code) {
            if (success) {
                startLiveRoom(liveInfo);
            } else {
                ToastUtils.showLong("????????????????????? code = " + code);
                finish();
            }
        }

        @Override
        public void onMsgArrived(RoomMsg msg) {
            roomMsgView.appendItem(msg.message);
        }

        @Override
        public void onUserCountChanged(int count) {
            super.onUserCountChanged(count);
            tvAudienceCount.setText(StringUtils.getAudienceCount(count));
        }

        @Override
        public void onAudienceChanged(List<AudienceInfo> infoList) {
            audiencePortraitView.updateAll(infoList);
        }

        @Override
        public void onRoomDestroyed(LiveChatRoomInfo roomInfo) {
            super.onRoomDestroyed(roomInfo);
            stopLiveErrorNetwork();
        }

        @Override
        public void onKickedOut() {
            super.onKickedOut();
            stopLiveErrorNetwork();
        }

        @Override
        public void onAnchorLeave() {
            super.onAnchorLeave();
            stopLiveErrorNetwork();
        }
    };

    /**
     * ????????????
     */
    private void stopLiveErrorNetwork() {
        ToastUtils.showLong("?????????????????????????????????");
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ?????????????????????????????????????????????
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.live_anchor_base_layout);
        // ??????????????????
        paddingStatusBarHeight(findViewById(R.id.preview_anchor));
        paddingStatusBarHeight(findViewById(R.id.cly_anchor_info));
        paddingStatusBarHeight(findViewById(R.id.fly_container));

        requestPermissionsIfNeeded();
        //???????????????
        audioControl = new AudioControl(this);
        audioControl.initMusicAndEffect();
    }

    /**
     * ????????????
     */
    private void requestPermissionsIfNeeded() {
        final List<String> missedPermissions = NERtc.checkPermission(this);
        if (missedPermissions.size() > 0) {
            PermissionUtils.permission(missedPermissions.toArray(new String[0])).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    if (CollectionUtils.isEqualCollection(granted, missedPermissions)) {
                        initView();
                    }
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                    ToastUtils.showShort("????????????");
                    finish();
                }
            }).request();
        } else {
            initView();
        }
    }

    private void initView() {
        videoView = findViewById(R.id.videoView);
        clyLivingView = findViewById(R.id.cly_anchor_info);
        audiencePortraitView = findViewById(R.id.rv_anchor_portrait_list);
        tvAudienceCount = findViewById(R.id.tv_audience_count);
        tvNickName = findViewById(R.id.tv_anchor_nickname);
        roomMsgView = findViewById(R.id.crv_msg_list);
        preview = findViewById(R.id.preview_anchor);
        // ????????????
        ivPortrait = findViewById(R.id.iv_anchor_portrait);
        // ??????????????????
        tvCoinCount = findViewById(R.id.tv_anchor_coin_count);
        tvInput = findViewById(R.id.tv_room_msg_input);
        edtInput = findViewById(R.id.et_room_msg_input);
        ivBeauty = findViewById(R.id.iv_beauty);
        ivConnect = findViewById(R.id.iv_connect);
        redPoint = findViewById(R.id.view_red_point);
        ivMusic = findViewById(R.id.iv_music);
        ivMore = findViewById(R.id.iv_more);
        flyContainer = findViewById(R.id.fly_container);

        clyLivingView.post(() ->
                InputUtils.registerSoftInputListener(LiveBaseActivity.this, new InputUtils.InputParamHelper() {
                    @Override
                    public int getHeight() {
                        return clyLivingView.getHeight();
                    }

                    @Override
                    public EditText getInputView() {
                        return edtInput;
                    }
                }));
        initContainer();
        initData();
        setListener();
    }

    protected abstract void initContainer();

    protected void setListener() {
        ivMusic.setOnClickListener(v -> showAudioControlDialog());
        ivBeauty.setOnClickListener(view -> showBeautyDialog());
        tvInput.setOnClickListener(view -> InputUtils.showSoftInput(edtInput));
        preview.btnLiveCreate.setOnClickListener(view -> createLiveRoom(LiveType.NORMAL_LIVING, null, null));
        preview.llyBeauty.setOnClickListener(v -> showBeautyDialog());
        preview.llyFilter.setOnClickListener(view -> showFilterDialog());
        preview.llySetting.setOnClickListener(view -> showSettingDialog());
        preview.ivClose.setOnClickListener(v -> onBackPressed());
        preview.ivSwitchCamera.setOnClickListener(v -> switchCamera());
        ivMore.setOnClickListener(v -> showLiveMoreDialog());

        if (BuildConfig.DEBUG) {
            ivMore.setOnLongClickListener(v -> {
                DumpDialog.showDialog(getSupportFragmentManager());
                return true;
            });
        }

        edtInput.setOnEditorActionListener((v, actionId, event) -> {
            if (v == edtInput) {
                String input = edtInput.getText().toString();
                InputUtils.hideSoftInput(edtInput);
                anchor.sendTextMsg(input);
                return true;
            }
            return false;
        });
    }

    protected void initData() {
        initLiveRoom(null);
        beautyControl = new BeautyControl(this);
        beautyControl.initFaceUI();
        startPreview();
        //????????????
        beautyControl.openBeauty();
    }


    /**
     * ???????????????
     */
    protected void switchCamera() {
        liveRoom.switchCamera();
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        beautyControl.switchCamera(cameraFacing);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        // ?????????????????????????????????
        if (!ViewUtils.isInView(edtInput, x, y) && isLiveStart) {
            InputUtils.hideSoftInput(edtInput);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * ??????
     */
    private void startPreview() {
        liveRoom.startVideoPreview();
    }

    protected abstract void initLiveRoom(NERtcOption option);

    /**
     * ????????????
     *
     * @param type            pk3  ?????????2
     * @param requestAccId
     * @param requestNickname
     */
    protected abstract void createLiveRoom(int type, String requestAccId, String requestNickname);

    /**
     * ????????????
     */
    protected void stopLive() {
        if (isLiveStart && liveRoom != null) {
            String liveCid = liveInfo.liveCid;
            LiveInteraction.stopLive(liveCid).subscribe(new ResourceSingleObserver<BaseResponse<Boolean>>() {
                @Override
                public void onSuccess(BaseResponse<Boolean> booleanBaseResponse) {
                    //do nothing
                    isLiveStart = false;
                }

                @Override
                public void onError(Throwable e) {

                }
            });
        }
        //?????????
        if (anchor != null && chatRoomNotify != null) {
            anchor.registerNotify(chatRoomNotify, false);
        }

        if (liveRoom != null) {
            liveRoom.stopLive();
            liveRoom = null;
        }

    }


    /**
     * ???????????????
     *
     * @param liveInfo
     */
    protected void joinChatRoom(LiveInfo liveInfo) {
        LiveChatRoomInfo liveChatRoomInfo = new LiveChatRoomInfo(liveInfo.chatRoomId, liveInfo.accountId, String.valueOf(liveInfo.avRoomUid));
        anchor.joinRoom(liveChatRoomInfo);
        anchor.registerNotify(chatRoomNotify, true);
    }

    /**
     * ???????????????
     *
     * @param liveInfo
     */
    private void startLiveRoom(LiveInfo liveInfo) {
        liveRoom.stopVideoPreview();
        liveRoom.createRoom(liveInfo, videoProfile, frameRate, audioScenario,
                cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT,
                new LiveRoomCallback<Void>() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(int code, String msg) {

                    }
                });
    }

    @Override
    public void onRoomLiveStart() {
        preview.setVisibility(View.GONE);
        clyLivingView.setVisibility(View.VISIBLE);
        tvNickName.setText(liveInfo.nickname);
        ImageLoader.with(getApplicationContext()).circleLoad(liveInfo.avatar, ivPortrait);
        tvCoinCount.setText("0??????");
        isLiveStart = true;
        flyContainer.setVisibility(View.VISIBLE);
    }


    @Override
    public void onUserReward(MsgReward.RewardBody reward) {
        // ???????????????????????????????????????
        anchor.notifyCoinChanged(new AnchorCoinChangedAttachment(reward.fromUserAvRoomUid,
                reward.rewardCoinTotal, new RewardGiftInfo((int) reward.giftId, reward.nickname)));
        tvCoinCount.setText(StringUtils.getCoinCount(reward.rewardCoinTotal));
    }

    @Override
    public void onAudioEffectFinished(int effectId) {
        if (audioControl != null) {
            audioControl.onEffectFinish(effectId);
        }
    }

    @Override
    public void onAudioMixingFinished() {
        if (audioControl != null) {
            audioControl.onMixingFinished();
        }
    }

    @Override
    public void onError(boolean serious, int code, String msg) {
        if (serious) {
            ToastUtils.showShort(msg);
            finish();
        }
        ALog.d(LOG_TAG, msg + " code = " + code);
    }

    /**
     * ????????????dailog
     */
    protected void showAudioControlDialog() {
        if (audioControl != null) {
            audioControl.setLiveRoom(liveRoom);
            audioControl.showAudioControlDialog();
        }

    }

    /**
     * ????????????dialog
     */
    protected void showBeautyDialog() {
        beautyControl.showBeautyDialog();
    }


    protected boolean isConnectDialogShowing() {
        return getSupportFragmentManager().findFragmentByTag("audienceConnectDialog") != null && getSupportFragmentManager().findFragmentByTag("audienceConnectDialog").isVisible();
    }

    protected void showFilterDialog() {
        beautyControl.showFilterDialog();
    }

    protected void showSettingDialog() {
        LiveSettingDialog liveSettingDialog = new LiveSettingDialog();
        liveSettingDialog.setLiveSetting(videoProfile, frameRate, audioScenario);
        liveSettingDialog.setValueChangeListener(new LiveSettingDialog.LiveSettingChangeListener() {
            @Override
            public void videoProfileChange(int newValue) {
                videoProfile = newValue;
            }

            @Override
            public void frameRateChange(NERtcVideoConfig.NERtcVideoFrameRate frameRate) {
                LiveBaseActivity.this.frameRate = frameRate;
            }

            @Override
            public void audioScenarioChange(int audioScenario) {
                LiveBaseActivity.this.audioScenario = audioScenario;
            }
        });
        liveSettingDialog.show(getSupportFragmentManager(), "liveSettingDialog");
    }

    /**
     * ????????????????????????
     */
    protected void showLiveMoreDialog() {
        AnchorMoreDialog anchorMoreDialog = new AnchorMoreDialog(this);
        anchorMoreDialog.registerOnItemClickListener((itemView, item) -> {
            switch (item.id) {
                case AnchorMoreDialog.ITEM_CAMERA:
                    return liveRoom.enableLocalVideo(!item.enable);
                case AnchorMoreDialog.ITEM_MUTE:
                    return liveRoom.muteLocalAudio(item.enable);
                case AnchorMoreDialog.ITEM_RETURN:
                    return liveRoom.enableEarback(!item.enable, 100);
                case AnchorMoreDialog.ITEM_CAMERA_SWITCH:
                    switchCamera();
                    break;
                case AnchorMoreDialog.ITEM_SETTING:
                    ToastUtils.showShort("?????????????????????");
                    break;
                case AnchorMoreDialog.ITEM_DATA:
                    ToastUtils.showShort("???????????????????????????");
                    break;
                case AnchorMoreDialog.ITEM_FINISH:
                    onBackPressed();
                    break;
                case AnchorMoreDialog.ITEM_FILTER:
                    showFilterDialog();
                    break;
                default:
                    break;
            }
            return true;
        });
        anchorMoreDialog.show();
    }


    @Override
    public void onBackPressed() {
        if (isLiveStart) {
            ChoiceDialog closeDialog = new ChoiceDialog(this)
                    .setTitle("????????????")
                    .setContent("???????????????????????????")
                    .setNegative("??????", null)
                    .setPositive("??????", v -> {
                        finish();
                    });
            closeDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLive();
        if (liveRoom != null) {
            //????????????
            liveRoom.setVideoCallback(null, true);
        }
        if (beautyControl != null) {
            beautyControl.onDestroy();
            beautyControl = null;
        }
        AnchorMoreDialog.clearItem();
        NERTCLiveRoom.destroySharedInstance(true);
    }

    @Override
    protected StatusBarConfig provideStatusBarConfig() {
        return new StatusBarConfig.Builder()
                .statusBarDarkFont(false)
                .build();
    }
}
