/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.biz_live.yunxin.live.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.biz_live.R;
import com.netease.biz_live.yunxin.live.audience.utils.StringUtils;
import com.netease.biz_live.yunxin.live.chatroom.ChatRoomMsgCreator;
import com.netease.biz_live.yunxin.live.chatroom.custom.AnchorCoinChangedAttachment;
import com.netease.biz_live.yunxin.live.chatroom.custom.PkStatusAttachment;
import com.netease.biz_live.yunxin.live.chatroom.custom.PunishmentStatusAttachment;
import com.netease.biz_live.yunxin.live.chatroom.model.AudienceInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.RewardGiftInfo;
import com.netease.biz_live.yunxin.live.constant.ErrorCode;
import com.netease.biz_live.yunxin.live.constant.LiveTimeDef;
import com.netease.biz_live.yunxin.live.constant.LiveType;
import com.netease.biz_live.yunxin.live.dialog.AnchorListDialog;
import com.netease.biz_live.yunxin.live.dialog.AudienceConnectDialog;
import com.netease.biz_live.yunxin.live.dialog.ChoiceDialog;
import com.netease.biz_live.yunxin.live.liveroom.AnchorPk;
import com.netease.biz_live.yunxin.live.liveroom.AnchorPkDelegate;
import com.netease.biz_live.yunxin.live.liveroom.AnchorSeatDelegate;
import com.netease.biz_live.yunxin.live.liveroom.AnchorSeatManager;
import com.netease.biz_live.yunxin.live.liveroom.LiveRoomCallback;
import com.netease.biz_live.yunxin.live.liveroom.NERTCAnchorBaseLiveRoomDelegate;
import com.netease.biz_live.yunxin.live.liveroom.NERTCAnchorLiveRoom;
import com.netease.biz_live.yunxin.live.liveroom.NERTCLiveRoom;
import com.netease.biz_live.yunxin.live.liveroom.msg.PkInfo;
import com.netease.biz_live.yunxin.live.liveroom.state.LiveState;
import com.netease.biz_live.yunxin.live.model.JoinInfo;
import com.netease.biz_live.yunxin.live.model.LiveInfo;
import com.netease.biz_live.yunxin.live.model.SeatMemberInfo;
import com.netease.biz_live.yunxin.live.model.message.MsgPkStart;
import com.netease.biz_live.yunxin.live.model.message.MsgPunishStart;
import com.netease.biz_live.yunxin.live.model.message.MsgReward;
import com.netease.biz_live.yunxin.live.model.response.AnchorQueryInfo;
import com.netease.biz_live.yunxin.live.network.LiveInteraction;
import com.netease.biz_live.yunxin.live.ui.widget.AnchorActionView;
import com.netease.biz_live.yunxin.live.ui.widget.LinkSeatsAudienceRecycleView;
import com.netease.biz_live.yunxin.live.ui.widget.PKControlView;
import com.netease.biz_live.yunxin.live.ui.widget.PKVideoView;
import com.netease.biz_live.yunxin.live.utils.ClickUtils;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avsignalling.builder.InviteParamBuilder;
import com.netease.nimlib.sdk.avsignalling.event.InvitedEvent;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.demo.basic.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.ResourceSingleObserver;

/**
 * ??????????????????????????????
 */
public class InteractionLiveActivity extends LiveBaseActivity implements NERTCAnchorBaseLiveRoomDelegate, AnchorPkDelegate {

    private static final String LOG_TAG = "NERTCLiveRoomImpl";

    private PKControlView pkControlView;

    private ImageView ivPkRequest;

    private LinearLayout llyPkProgress;

    //???????????????
    private AnchorActionView anchorActionView;

    private boolean isReceiver;//?????????PK?????????

    /**
     * ??????????????????
     */
    private AnchorSeatDelegate seatDelegate;


    /**
     * pk????????????
     */
    private LiveInfo pkLiveInfo;

    private ChoiceDialog pkRequestDialog;

    private ChoiceDialog pkInviteedDialog;

    private ChoiceDialog stopPkDialog;

    private AnchorListDialog anchorListDialog;

    private PKControlView.WrapperCountDownTimer countDownTimer;

    private PKVideoView pkVideoView;

    private AnchorPk pkService;

    private AnchorSeatManager seatManager;

    private LinkSeatsAudienceRecycleView seatsView;


    public static void startAnchorActivity(Context context) {
        context.startActivity(new Intent(context, InteractionLiveActivity.class));
    }

    @Override
    protected void initContainer() {
        LayoutInflater.from(this).inflate(R.layout.pk_live_anchor_layout, flyContainer, true);
        pkControlView = findViewById(R.id.pk_control_view);
        llyPkProgress = findViewById(R.id.lly_pk_progress);
        ivPkRequest = findViewById(R.id.iv_request_pk);
        seatsView = findViewById(R.id.audience_seats_view);
        anchorActionView = findViewById(R.id.view_action);
        seatsView.setUseScene(LinkSeatsAudienceRecycleView.UseScene.ANCHOR);
    }

    protected void setListener() {
        super.setListener();
        ivConnect.setOnClickListener(view -> showConnectDialog());
        ivPkRequest.setOnClickListener(v -> {
            if (pkService.getLiveCurrentState().getStatus() == LiveState.STATE_LIVE_ON) {
                showAnchorListDialog();
            } else if (pkService.getLiveCurrentState().getStatus() == LiveState.STATE_PKING) {
                showStopPkDialog();
            } else {
                ToastUtils.showShort("????????????????????????????????????");
                ALog.d(LOG_TAG, "state error status = " + pkService.getLiveCurrentState().getStatus());
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        initSeatDelegate();
        liveRoom.setDelegate(this);
        pkService = liveRoom.getService(AnchorPk.class);
        seatManager = liveRoom.getService(AnchorSeatManager.class);
        //????????????????????????
        NetworkUtils.registerNetworkStatusChangedListener(new NetworkUtils.OnNetworkStatusChangedListener() {
            @Override
            public void onDisconnected() {
                ALog.i(LOG_TAG, "network disconnected");
            }

            @Override
            public void onConnected(NetworkUtils.NetworkType networkType) {
                ALog.i(LOG_TAG, "network onConnected");
                fetchSeatsInfo();
            }
        });
    }

    /**
     * ????????????dialog
     */
    private void showConnectDialog() {
        if (ClickUtils.isFastClick()) {
            return;
        }
        redPoint.setVisibility(View.GONE);
        anchorActionView.hide();
        AudienceConnectDialog audienceConnectDialog = new AudienceConnectDialog();
        Bundle bundle = new Bundle();
        bundle.putString(AudienceConnectDialog.ROOM_ID, liveInfo.liveCid);
        audienceConnectDialog.setArguments(bundle);
        audienceConnectDialog.show(getSupportFragmentManager(), "audienceConnectDialog");
    }

    /**
     * ??????????????????
     */
    private void fetchSeatsInfo() {
        if (liveInfo == null) {
            return;
        }
        LiveInteraction.queryAnchorRoomInfo(liveInfo.accountId, liveInfo.liveCid).subscribe(
                new ResourceSingleObserver<BaseResponse<AnchorQueryInfo>>() {
                    @Override
                    public void onSuccess(@NonNull BaseResponse<AnchorQueryInfo> response) {
                        if (response.isSuccessful()) {
                            AnchorQueryInfo queryInfo = response.data;
                            List<SeatMemberInfo> tempMembers = new ArrayList<>(seatsView.getMemberList());
                            if (queryInfo.seatList != null && queryInfo.seatList.size() > 0) {
                                //?????????????????????????????????
                                for (SeatMemberInfo member : tempMembers) {
                                    if (!queryInfo.seatList.contains(member)) {
                                        onUserExitSeat(member);
                                    }
                                }

                                //??????????????????????????????
                                List<Long> uids = new ArrayList<>();
                                for (SeatMemberInfo member : queryInfo.seatList) {
                                    if (!seatsView.contains(member.accountId)) {
                                        onUserEnterSeat(member);
                                    }
                                    uids.add(member.avRoomUid);
                                }
                                //??????????????????
                                seatManager.updateSeatsStream(uids);
                            } else {
                                //??????????????????
                                for (SeatMemberInfo member : tempMembers) {
                                    onUserExitSeat(member);
                                }
                                seatManager.updateSeatsStream(null);
                            }
                        } else if (response.code == ErrorCode.CREATE_LIVE_NOT_EXIST) {
                            //??????????????????????????????????????????
                            finish();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                }
        );
    }

    /**
     * ???????????????????????????
     */
    private void initSeatDelegate() {
        seatDelegate = new AnchorSeatDelegate() {
            @Override
            public void onSeatApplyRequest(int index, SeatMemberInfo member) {
                if (!isConnectDialogShowing()) {
                    showAudienceApply();
                    redPoint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSeatApplyRequestCanceled(SeatMemberInfo member) {

            }

            @Override
            public void onSeatPickRejected(SeatMemberInfo member) {
                ToastUtils.showLong(R.string.biz_live_audience_reject_link_seats_invited);
            }

            @Override
            public void onSeatEntered(SeatMemberInfo member) {
                onUserEnterSeat(member);
            }

            @Override
            public void onSeatLeft(SeatMemberInfo member) {
                onUserExitSeat(member);
            }

            @Override
            public void onSeatMuteStateChanged(SeatMemberInfo member) {
                seatsView.updateItem(member);
            }
        };
    }

    /**
     * ????????????
     *
     * @param member
     */
    private void onUserEnterSeat(SeatMemberInfo member) {
        seatsView.appendItem(member);
        ivPkRequest.setVisibility(View.GONE);
        roomMsgView.appendItem(ChatRoomMsgCreator.createSeatEnter(member.nickName));
    }

    /**
     * ??????????????????
     *
     * @param member
     */
    private void onUserExitSeat(SeatMemberInfo member) {
        seatsView.remove(member);
        roomMsgView.appendItem(ChatRoomMsgCreator.createSeatExit(member.nickName));
        if (!seatsView.haveMemberInSeats()) {
            ivPkRequest.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ????????????
     */
    private void cancelRequest() {
        pkService.cancelPkRequest(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                cancelSuccess();
            }

            @Override
            public void onFailed(int code) {
                if (code != ResponseCode.RES_INVITE_HAS_ACCEPT) {
                    cancelSuccess();
                } else {
                    ToastUtils.showShort("??????????????????????????????");
                }
            }

            @Override
            public void onException(Throwable exception) {
                cancelSuccess();
            }
        }, true);
    }

    /**
     * ????????????
     */
    private void cancelSuccess() {
        anchorActionView.hide();
        if (pkRequestDialog != null && pkRequestDialog.isShowing()) {
            pkRequestDialog.dismiss();
        }
    }


    protected void initLiveRoom(NERtcOption option) {
        liveRoom = (NERTCAnchorLiveRoom) NERTCLiveRoom.sharedInstance(true);
        liveRoom.init(this, BuildConfig.APP_KEY, option);
        liveRoom.enableLocalVideo(true);
        liveRoom.setupLocalView(videoView);
    }

    protected void createLiveRoom(int type, String requestAccId, String requestNickname) {
        preview.setCreateEnable(false);
        String accId = String.valueOf(System.currentTimeMillis());
        String topic = type == LiveType.PK_LIVING ? "" : preview.getTopic();
        String parentLiveCid = type == LiveType.PK_LIVING ? liveInfo.liveCid : "";
        LiveInteraction.createLiveRoom(accId, topic, parentLiveCid, preview.getLiveCoverPic(), type).subscribe(new ResourceSingleObserver<BaseResponse<LiveInfo>>() {
            @Override
            public void onSuccess(BaseResponse<LiveInfo> response) {
                if (response.code == 200) {
                    if (type == LiveType.PK_LIVING) {
                        pkLiveInfo = response.data;
                        ALog.i(LOG_TAG, "pk liveCid = " + pkLiveInfo.liveCid);
                        startPkLive(requestAccId, requestNickname);
                    } else {
                        liveInfo = response.data;
                        ALog.i(LOG_TAG, "single liveCid = " + liveInfo.liveCid);
                        joinChatRoom(liveInfo);
                    }
                } else {
                    ToastUtils.showShort("??????????????? " + response.msg);
                }

            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showShort("????????????");
            }
        });
    }


    private void startPkLive(String requestAccid, String requestNickname) {
        if (pkLiveInfo == null) {
            return;
        }
        if (pkRequestDialog == null) {
            pkRequestDialog = new ChoiceDialog(this)
                    .setTitle("??????PK")
                    .setNegative("??????", null);
            pkRequestDialog.setCancelable(false);
        }
        pkRequestDialog.setContent("????????????" + "???" + requestNickname + "???" + "??????PK???")
                .setPositive("??????", v -> {
                    isReceiver = false;
                    pkService.requestPk(pkLiveInfo.imAccid, requestAccid, pkLiveInfo.liveCid, pkLiveInfo.liveConfig.pushUrl, pkLiveInfo.nickname, new LiveRoomCallback<Void>() {
                        @Override
                        public void onSuccess() {
                            anchorActionView.setText("?????????" + requestNickname + "???PK????????????")
                                    .setColorButton("??????", v1 -> {
                                        cancelRequest();
                                    }).show();

                        }

                        @Override
                        public void onError(int code, String msg) {
                            ToastUtils.showShort("???????????? code???" + code);
                        }
                    });
                });
        if (!pkRequestDialog.isShowing()) {
            pkRequestDialog.show();
        }
    }


    /**
     * ????????????
     */
    private void acceptPKRequest(InvitedEvent invitedEvent, PkInfo pkInfo) {
        pkService.acceptPk(pkInfo.pkLiveCid, invitedEvent.getFromAccountId(), invitedEvent.getRequestId(),
                invitedEvent.getToAccountId(), new LiveRoomCallback<Void>() {
                    @Override
                    public void onSuccess() {
                        llyPkProgress.setVisibility(View.VISIBLE);
                        //????????????
                        ivConnect.setEnabled(false);
                    }

                    @Override
                    public void onError(int code, String msg) {

                    }
                });
    }

    /**
     * ????????????
     *
     * @param invitedEvent
     */
    private void rejectPkRequest(InvitedEvent invitedEvent) {
        InviteParamBuilder paramBuilder = new InviteParamBuilder(invitedEvent.getChannelBaseInfo().getChannelId(),
                invitedEvent.getFromAccountId(), invitedEvent.getRequestId());
        pkService.rejectPkRequest(paramBuilder, new LiveRoomCallback<Void>() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String msg) {

            }
        });
    }

    private void stopPk() {
        LiveInteraction.stopPk(liveInfo.liveCid).subscribe(new ResourceSingleObserver<BaseResponse<Boolean>>() {
            @Override
            public void onSuccess(BaseResponse<Boolean> booleanBaseResponse) {
                if (booleanBaseResponse.code != 200) {
                    ToastUtils.showShort("???????????? code = " + booleanBaseResponse.code);
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showShort("stop pk error");
            }
        });
    }

    /**
     * ??????PK dialog
     */
    private void showStopPkDialog() {
        if (stopPkDialog == null) {
            stopPkDialog = new ChoiceDialog(this);
            stopPkDialog.setTitle("??????PK");
            stopPkDialog.setContent("PK?????????????????????????????????????????????????????????");
            stopPkDialog.setPositive("????????????", v -> stopPk());
            stopPkDialog.setNegative("??????", null);
        }

        stopPkDialog.show();
    }

    /**
     * ???????????????????????????
     */
    private void showAnchorListDialog() {
        if (anchorListDialog != null && anchorListDialog.isVisible()) {
            return;
        }
        if (anchorListDialog == null) {
            anchorListDialog = new AnchorListDialog();
        }
        anchorListDialog.setSelectAnchorListener(liveInfo -> {
            isReceiver = false;
            createLiveRoom(LiveType.PK_LIVING, liveInfo.imAccid, liveInfo.nickname);
        });
        anchorListDialog.show(getSupportFragmentManager(), "anchorListDialog");
    }


    private void showAudienceApply() {
        if (!anchorActionView.isShowing()) {
            anchorActionView.setText("????????????????????????")
                    .setBlackButton(true, "??????", v -> {
                        //???????????????????????????????????????
//                        redPoint.setVisibility(View.GONE);
                    })
                    .setColorButton("????????????", v -> showConnectDialog())
                    .show();
        }
    }


    @Override
    public void onPkStart(MsgPkStart.StartPkBody startPKBody) {
        llyPkProgress.setVisibility(View.GONE);
        String otherNickname;
        String otherAvatar;
        long otherUid;
        if (isReceiver) {
            otherAvatar = startPKBody.inviterAvatar;
            otherNickname = startPKBody.inviterNickname;
            otherUid = startPKBody.inviterRoomUid;
        } else {
            otherAvatar = startPKBody.inviteeAvatar;
            otherNickname = startPKBody.inviteeNickname;
            otherUid = startPKBody.inviteeRoomUid;
        }
        PkStatusAttachment attachment = new PkStatusAttachment(startPKBody.pkStartTime, startPKBody.currentTime, otherNickname, otherAvatar);
        anchor.notifyPkStatus(attachment);


        ImageLoader.with(this).circleLoad(R.drawable.icon_stop_pk, ivPkRequest);
        if (pkVideoView == null) {
            pkVideoView = new PKVideoView(this);
        }
        pkControlView.getVideoContainer().removeAllViews();
        pkControlView.getVideoContainer().addView(pkVideoView);
        liveRoom.setupLocalView(pkVideoView.getLocalVideo());
        liveRoom.setupRemoteView(pkVideoView.getRemoteVideo(), otherUid, true);
        pkVideoView.getRemoteVideo().setMirror(true);
        videoView.setVisibility(View.GONE);
        pkControlView.setVisibility(View.VISIBLE);
        // pk ??????????????????
        pkControlView.reset();

        // ????????????????????????
        pkControlView.updatePkAnchorInfo(otherNickname, otherAvatar);
        // ???????????????
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        countDownTimer = pkControlView.createCountDownTimer(LiveTimeDef.TYPE_PK, attachment.getLeftTime(LiveTimeDef.TOTAL_TIME_PK, 0));
        countDownTimer.start();
        //????????????
        ivConnect.setEnabled(false);
    }


    @Override
    public void onPunishStart(MsgPunishStart.PunishBody punishBody) {
        // ?????? pk ????????????
        int anchorWin;// ?????????????????? pk ??????
        if (punishBody.inviteeRewards == punishBody.inviterRewards) {
            anchorWin = 0;
        } else if (isReceiver) {
            anchorWin = punishBody.inviteeRewards > punishBody.inviterRewards ? 1 : -1;
        } else {
            anchorWin = punishBody.inviteeRewards < punishBody.inviterRewards ? 1 : -1;
        }
        // ??????pk??????
        pkControlView.handleResultFlag(true, anchorWin);

        anchor.notifyPkStatus(new PkStatusAttachment(anchorWin));
        // ?????? ??????????????????
        PunishmentStatusAttachment attachment1 = new PunishmentStatusAttachment(punishBody.pkPulishmentTime, punishBody.currentTime, anchorWin);
        anchor.notifyPunishmentStatus(attachment1);
        // ?????????????????????
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        if (anchorWin != 0) {
            countDownTimer = pkControlView.createCountDownTimer(LiveTimeDef.TYPE_PUNISHMENT, attachment1.getLeftTime(LiveTimeDef.TOTAL_TIME_PUNISHMENT, 0));
            countDownTimer.start();
        }
    }

    @Override
    public void onPkEnd(boolean isFromUser, String nickname) {
        anchor.notifyPunishmentStatus(new PunishmentStatusAttachment());
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
        ImageLoader.with(this).circleLoad(R.drawable.icon_pk, ivPkRequest);
        pkControlView.getVideoContainer().removeAllViews();
        pkControlView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        liveRoom.setupLocalView(videoView);
        if (isFromUser && (pkLiveInfo == null || !TextUtils.equals(nickname, pkLiveInfo.nickname))) {
            ToastUtils.showShort("???" + nickname + "????????????PK");
        }
        if (stopPkDialog != null && stopPkDialog.isShowing()) {
            stopPkDialog.dismiss();
        }
        //????????????
        ivConnect.setEnabled(true);
    }

    @Override
    public void onPkRequestCancel(boolean byUser) {
        if (pkInviteedDialog != null && pkInviteedDialog.isShowing()) {
            pkInviteedDialog.dismiss();
            if (byUser) {
                ToastUtils.showShort("??????????????????");
            } else {
                ToastUtils.showShort("????????????");
            }
        }
    }

    @Override
    public void receivePkRequest(InvitedEvent invitedEvent, PkInfo pkInfo) {
        isReceiver = true;
        if (pkLiveInfo == null) {
            pkLiveInfo = new LiveInfo();
        }
        pkLiveInfo.liveCid = pkInfo.pkLiveCid;
        if (pkInviteedDialog == null) {
            pkInviteedDialog = new ChoiceDialog(this)
                    .setTitle("??????PK");
            pkInviteedDialog.setCancelable(false);
        }
        pkInviteedDialog.setContent("???" + pkInfo.inviterNickname + "???" + "???????????????PK??????????????????")
                .setPositive("??????", v -> acceptPKRequest(invitedEvent, pkInfo))
                .setNegative("??????", v -> rejectPkRequest(invitedEvent));
        if (!pkInviteedDialog.isShowing()) {
            pkInviteedDialog.show();
        }
        if (anchorListDialog != null && anchorListDialog.isVisible()) {
            anchorListDialog.dismiss();
        }
        if (pkRequestDialog != null && pkRequestDialog.isShowing()) {
            pkRequestDialog.dismiss();
        }
    }

    @Override
    public void pkRequestRejected(String userId) {
        ToastUtils.showShort("?????????????????????PK??????");
        anchorActionView.hide();
    }

    @Override
    public void onAccept() {
        anchorActionView.hide();
        llyPkProgress.setVisibility(View.VISIBLE);
        //????????????
        ivConnect.setEnabled(false);
    }


    @Override
    public void onUserReward(MsgReward.RewardBody reward) {
        if (pkService.getLiveCurrentState().getStatus() == LiveState.STATE_PKING) {
            long selfPkCoinCount;
            long otherPkCoinCount;
            long rewardCoinTotal;
            List<AudienceInfo> selfRewardPkList;
            List<AudienceInfo> otherRewardPkList;
            if (isReceiver) {
                selfPkCoinCount = reward.inviteeRewardPKCoinTotal;
                otherPkCoinCount = reward.inviterRewardPKCoinTotal;
                rewardCoinTotal = reward.inviteeRewardCoinTotal;
                selfRewardPkList = reward.inviteeRewardPkList;
                otherRewardPkList = reward.rewardPkList;
            } else {
                selfPkCoinCount = reward.inviterRewardPKCoinTotal;
                otherPkCoinCount = reward.inviteeRewardPKCoinTotal;
                rewardCoinTotal = reward.rewardCoinTotal;
                selfRewardPkList = reward.rewardPkList;
                otherRewardPkList = reward.inviteeRewardPkList;
            }
            // pk ???????????????????????????
            AnchorCoinChangedAttachment attachment2 = new AnchorCoinChangedAttachment(
                    reward.fromUserAvRoomUid,
                    rewardCoinTotal,
                    new RewardGiftInfo((int) reward.giftId, reward.nickname), selfPkCoinCount,
                    otherPkCoinCount, selfRewardPkList, otherRewardPkList);
            anchor.notifyCoinChanged(attachment2);

            pkControlView.updateScore(selfPkCoinCount, otherPkCoinCount);
            pkControlView.updateRanking(selfRewardPkList, otherRewardPkList);
            tvCoinCount.setText(StringUtils.getCoinCount(rewardCoinTotal));
        } else {
            super.onUserReward(reward);
        }
    }

    @Override
    public <T> T getDelegateService(Class<T> tClass) {
        if (tClass.equals(AnchorPkDelegate.class)) {
            return (T) this;
        }
        if (tClass.equals(AnchorSeatDelegate.class)) return (T) seatDelegate;
        return null;
    }

    @Override
    public void preJoinRoom(String liveCid, boolean isPk, String parentLiveCid) {
        if (TextUtils.isEmpty(liveCid) || !isPk) {
            liveCid = liveInfo.liveCid;
        }
        ALog.i(LOG_TAG, "preJoinRoom liveCid = " + liveCid + "\n status = " + pkService.getLiveCurrentState().getStatus());
        LiveInteraction.joinLiveRoom(liveCid, parentLiveCid, isPk ? 3 : 2).subscribe(new ResourceSingleObserver<BaseResponse<JoinInfo>>() {
            @Override
            public void onSuccess(BaseResponse<JoinInfo> joinInfoBaseResponse) {
                ALog.i(LOG_TAG, "preJoinRoom sucess code = " + joinInfoBaseResponse.code);
                if (joinInfoBaseResponse.code == 200) {
                    JoinInfo joinInfo = joinInfoBaseResponse.data;
                    liveRoom.joinRtcChannel(joinInfo.avRoomCheckSum, joinInfo.avRoomCName, joinInfo.avRoomUid, joinInfo.avRoomCid);
                } else {
                    ToastUtils.showShort("preJoinRoom failed error code =" + joinInfoBaseResponse.code);
                    preJoinError();
                }
            }

            @Override
            public void onError(Throwable e) {
                ALog.w(LOG_TAG, "preJoinRoom error ", e);
                preJoinError();
            }
        });
    }

    /**
     * ???????????????????????????????????????
     */
    private void preJoinError() {
        //??????????????????pk?????????,?????????????????????
        if (pkService.getLiveCurrentState().getStatus() != LiveState.STATE_PKING) {
            pkService.getLiveCurrentState().release();
            llyPkProgress.setVisibility(View.GONE);
            ToastUtils.showShort("??????PK????????????");
        } else {
            ToastUtils.showShort("???????????????");
            finish();
        }
    }

    @Override
    public void onTimeOut(int code) {
        if (code == ErrorCode.ERROR_CODE_TIME_OUT_ACCEPTED) {
            preJoinError();
            return;
        }
        if (isReceiver) {
            isReceiver = false;
            if (pkInviteedDialog != null && pkInviteedDialog.isShowing()) {
                pkInviteedDialog.dismiss();
                ToastUtils.showShort("???????????????");
            }
        } else {
            anchorActionView.hide();
            ToastUtils.showShort("?????????????????????????????????");
        }
    }

    @Override
    public void onUserBusy(String userId) {
        ToastUtils.showShort("??????????????????PK??????????????????");
        anchorActionView.hide();
    }

}
