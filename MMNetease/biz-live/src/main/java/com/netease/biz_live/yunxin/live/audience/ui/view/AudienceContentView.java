/*
 * Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.biz_live.yunxin.live.audience.ui.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.biz_live.R;
import com.netease.biz_live.databinding.ViewIncludeRoomTopBinding;
import com.netease.biz_live.databinding.ViewItemAudienceLiveRoomInfoBinding;
import com.netease.biz_live.yunxin.live.audience.callback.SeatApplyAcceptEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatApplyRejectEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatCustomInfoChangeEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatEnterEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatKickedEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatLeaveEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatMuteStateChangeEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatPickRequestEvent;
import com.netease.biz_live.yunxin.live.audience.callback.SeatVideoOpenStateChangeEvent;
import com.netease.biz_live.yunxin.live.audience.ui.dialog.GiftDialog;
import com.netease.biz_live.yunxin.live.audience.ui.dialog.LinkSeatsStatusDialog;
import com.netease.biz_live.yunxin.live.audience.utils.AccountUtil;
import com.netease.biz_live.yunxin.live.audience.utils.AudienceDialogControl;
import com.netease.biz_live.yunxin.live.audience.utils.AudiencePKControl;
import com.netease.biz_live.yunxin.live.audience.utils.DialogHelperActivity;
import com.netease.biz_live.yunxin.live.audience.utils.InputUtils;
import com.netease.biz_live.yunxin.live.audience.utils.LinkedSeatsAudienceActionManager;
import com.netease.biz_live.yunxin.live.audience.utils.StringUtils;
import com.netease.biz_live.yunxin.live.chatroom.ChatRoomMsgCreator;
import com.netease.biz_live.yunxin.live.chatroom.control.Audience;
import com.netease.biz_live.yunxin.live.chatroom.control.ChatRoomNotify;
import com.netease.biz_live.yunxin.live.chatroom.control.SkeletonChatRoomNotify;
import com.netease.biz_live.yunxin.live.chatroom.custom.AnchorCoinChangedAttachment;
import com.netease.biz_live.yunxin.live.chatroom.custom.PkStatusAttachment;
import com.netease.biz_live.yunxin.live.chatroom.custom.PunishmentStatusAttachment;
import com.netease.biz_live.yunxin.live.chatroom.model.AudienceInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.LiveChatRoomInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.RewardGiftInfo;
import com.netease.biz_live.yunxin.live.chatroom.model.RoomMsg;
import com.netease.biz_live.yunxin.live.constant.ApiErrorCode;
import com.netease.biz_live.yunxin.live.constant.AudioActionType;
import com.netease.biz_live.yunxin.live.constant.ErrorCode;
import com.netease.biz_live.yunxin.live.constant.LiveStatus;
import com.netease.biz_live.yunxin.live.constant.VideoActionType;
import com.netease.biz_live.yunxin.live.gift.GiftCache;
import com.netease.biz_live.yunxin.live.gift.GiftRender;
import com.netease.biz_live.yunxin.live.gift.ui.GifAnimationView;
import com.netease.biz_live.yunxin.live.liveroom.LiveRoomCallback;
import com.netease.biz_live.yunxin.live.liveroom.NERTCAudienceLiveRoom;
import com.netease.biz_live.yunxin.live.liveroom.NERTCAudienceLiveRoomDelegate;
import com.netease.biz_live.yunxin.live.liveroom.impl.NERTCAudienceLiveRoomImpl;
import com.netease.biz_live.yunxin.live.model.LiveInfo;
import com.netease.biz_live.yunxin.live.model.SeatMemberInfo;
import com.netease.biz_live.yunxin.live.model.response.AnchorQueryInfo;
import com.netease.biz_live.yunxin.live.model.response.PkRecord;
import com.netease.biz_live.yunxin.live.network.LiveInteraction;
import com.netease.biz_live.yunxin.live.network.LiveServerApi;
import com.netease.biz_live.yunxin.live.ui.widget.LinkSeatsAudienceRecycleView;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.demo.basic.BaseActivity;
import com.netease.yunxin.nertc.demo.basic.StatusBarConfig;
import com.netease.yunxin.nertc.demo.user.UserCenterService;
import com.netease.yunxin.nertc.demo.user.UserModel;
import com.netease.yunxin.nertc.demo.utils.SpUtils;
import com.netease.yunxin.nertc.demo.utils.ViewUtils;
import com.netease.yunxin.nertc.module.base.ModuleServiceMgr;

import java.util.List;

import io.reactivex.observers.ResourceSingleObserver;

/**
 * Created by luc on 2020/11/19.
 * <p>
 * ?????????????????????????????????{@link FrameLayout} ????????? {@link TextureView} ?????? {@link ExtraTransparentView} ????????????????????????
 * <p>
 * TextureView ???????????????????????????
 * <p>
 * ExtraTransparentView ??????????????????????????????????????????????????????????????????????????????view ????????? {@link RecyclerView} ?????????????????????????????????
 * // * ????????????????????? R.layout.view_item_audience_live_room_info
 *
 * <p>
 * ?????? {@link #prepare(),#release()} ???????????????recyclerView ??? view ??? {@link androidx.recyclerview.widget.RecyclerView#onChildAttachedToWindow(View)},
 * {@link androidx.recyclerview.widget.RecyclerView#onChildDetachedFromWindow(View)} ?????????
 * ?????????{@link #renderData(LiveInfo)} ????????? {@link androidx.recyclerview.widget.RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}
 * ???????????? {@link androidx.recyclerview.widget.LinearLayoutManager} ??????????????????????????? renderData ?????????????????? prepare ?????????
 */
@SuppressLint("ViewConstructor")
public class AudienceContentView extends FrameLayout {
    private static final String TAG = AudienceContentView.class.getSimpleName();
    /**
     * ????????????
     */
    private final UserCenterService userCenterService = ModuleServiceMgr.getInstance().getService(UserCenterService.class);
    /**
     * ?????? View ?????? activity
     */
    private final BaseActivity activity;
    /**
     * ??????????????????????????????
     */
    private final Audience audienceControl = Audience.getInstance();

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    private final GiftRender giftRender = new GiftRender();
    /**
     * ????????????View
     */
    private CDNStreamTextureView videoView;
    /**
     * ???????????????????????????????????????????????????RTC???
     */
    private NERtcVideoView rtcVideoView;

    /**
     * ????????????????????????
     */
    private ExtraTransparentView horSwitchView;
    /**
     * ????????????????????????viewbinding ????????????:https://developer.android.com/topic/libraries/view-binding?hl=zh-cn#java
     */
    private ViewItemAudienceLiveRoomInfoBinding infoBinding;
    private ViewIncludeRoomTopBinding includeRoomTopBinding;
    /**
     * ?????????????????????
     */
    private LiveInfo liveInfo;

    /**
     * pk ??????????????????
     */
    private AudiencePKControl audiencePKControl;
    //
//    /**
//     * ????????????????????????????????????????????????
//     */
    private AudienceErrorStateView errorStateView;

    /**
     * ???????????????????????????????????????
     */
    private WaitAnchorAcceptView waitAnchorAcceptFloatLayer;

    /**
     * ????????????
     */
    private GiftDialog giftDialog;

    /**
     * ????????????????????????{@link #prepare()} ??????????????? true???
     * {@link #release()} ??????????????? false;
     */
    private boolean canRender;

    /**
     * ?????????????????????
     */
    private LinkedSeatsAudienceActionManager linkedSeatsAudienceActionManager;
    /**
     * ????????????
     */
    private AudienceDialogControl audienceDialogControl;
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     * ?????????????????????????????????????????????????????????????????????RTC????????????linkSeatsRv?????????????????????????????????isLinkingSeats????????????????????????????????????UI??????????????????
     */
    private LinkSeatsAudienceRecycleView linkSeatsRv;
    /**
     * ??????????????????????????????????????????????????????RTC?????????????????????????????????CDN???
     */
    private boolean isLinkingSeats = false;
    private boolean isPking = false;
    private boolean isFirstShowNormalUI = true;
    private boolean joinRoomSuccess = false;
    /**
     * ??????????????????
     */
    private NetworkUtils.OnNetworkStatusChangedListener onNetworkStatusChangedListener = new NetworkUtils.OnNetworkStatusChangedListener() {
        @Override
        public void onDisconnected() {
            ToastUtils.showLong(R.string.biz_live_network_error);
            ALog.d(TAG, "onDisconnected():" + System.currentTimeMillis());
            showCurrentUI(false, false);
            changeErrorState(true, AudienceErrorStateView.TYPE_ERROR);
            linkedSeatsAudienceActionManager.dismissAllDialog();
            if (giftDialog != null && giftDialog.isShowing()) {
                giftDialog.dismiss();
            }
        }

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
            ALog.d(TAG, "onConnected():" + System.currentTimeMillis());
            if (canRender && liveInfo != null) {
                initForLiveType(true);
            }
        }
    };

    private NERTCAudienceLiveRoomDelegate seatCallback = new NERTCAudienceLiveRoomDelegate() {
        @Override
        public void onError(boolean serious, int code, String msg) {
            ALog.d(TAG, "onError serious:" + serious + ",code:" + code + ",msg:" + msg);
            if (serious) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            } else {
                if (!isNetworkConnected(activity) && ErrorCode.ERROR_CODE_DISCONNECT == code && !activity.isFinishing()) {
                    activity.finish();
                    return;
                }
                if (!TextUtils.isEmpty(msg)) {
                    ToastUtils.showShort(msg);
                }
            }
        }

        @Override
        public void onSeatEntered(SeatEnterEvent event) {
            ALog.d(TAG, "onSeatEntered ");
            if (isPking) {
                if (AccountUtil.isCurrentUser(event.member.accountId)) {
                    linkedSeatsAudienceActionManager.leaveSeat(liveInfo.liveCid, new LiveRoomCallback<Void>() {
                        @Override
                        public void onError(int code, String msg) {
                            ToastUtils.showShort(msg);
                        }
                    });
                }
                showCurrentUI(false, true);
                return;
            }
            if (event.member == null) {
                return;
            }
            if (AccountUtil.isCurrentUser(event.member.accountId)) {
                //????????????????????????????????????
                DurationStatisticTimer.DurationUtil.setBeginTimeStamp(System.currentTimeMillis());
                showCurrentUI(true, false);
            } else {
                linkSeatsRv.appendItem(event.member);
                if (!isLinkingSeats) {
                    videoView.setLinkingSeats(true);
                }
            }
            infoBinding.crvMsgList.appendItem(ChatRoomMsgCreator.createSeatEnter(event.member.nickName));
        }

        @Override
        public void onSeatLeft(SeatLeaveEvent event) {
            ALog.d(TAG, "onSeatLeft ");
            if (event.member == null) {
                return;
            }
            if (AccountUtil.isCurrentUser(event.member.accountId)) {
                showCurrentUI(false, true);
            }
            linkSeatsRv.remove(event.member);
            infoBinding.crvMsgList.appendItem(ChatRoomMsgCreator.createSeatExit(event.member.nickName));
            if (!isLinkingSeats) {
                videoView.setLinkingSeats(linkSeatsRv.haveMemberInSeats() && !linkSeatsRv.contains(userCenterService.getCurrentUser().accountId));
            }
        }

        @Override
        public void onSeatKicked(SeatKickedEvent event) {
            ALog.d(TAG, "onSeatKicked ");
            ToastUtils.showShort(activity.getString(R.string.biz_live_anchor_kick_audience));
            showCurrentUI(false, true);
            linkedSeatsAudienceActionManager.onDestory();
        }

        /**
         * ???????????????????????????
         */
        @Override
        public void onSeatPickRequest(SeatPickRequestEvent event) {
            ALog.d(TAG, "onSeatPickRequest ");
            getAudienceDialogControl().showAnchorInviteDialog(activity, new AudienceDialogControl.JoinSeatsListener() {
                @Override
                public void acceptInvite() {
                    ALog.d(TAG, "???????????????????????????");
                    linkedSeatsAudienceActionManager.acceptSeatPick(liveInfo.liveCid, userCenterService.getCurrentUser().accountId, new LiveRoomCallback<Void>() {
                        @Override
                        public void onSuccess(Void parameter) {
                            super.onSuccess(parameter);
                            final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
                            PermissionUtils.permission(permissions).callback(new PermissionUtils.FullCallback() {
                                @Override
                                public void onGranted(@NonNull List<String> granted) {
                                    joinRtcAndShowRtcUI(event.member);
                                }

                                @Override
                                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                                    ToastUtils.showShort(activity.getString(R.string.biz_live_permission_error_tips));
                                    joinRtcAndShowRtcUI(event.member);
                                }
                            }).request();
                        }

                        @Override
                        public void onError(int code, String msg) {
                            ALog.d(TAG, "acceptSeatPick onError:" + msg);
                            ToastUtils.showShort(msg);
                        }
                    });

                }

                @Override
                public void rejectInvite() {
                    ALog.d(TAG, "???????????????????????????");
                    linkedSeatsAudienceActionManager.rejectSeatPick(liveInfo.liveCid, userCenterService.getCurrentUser().accountId, new LiveRoomCallback<Void>() {
                        @Override
                        public void onSuccess(Void parameter) {
                            super.onSuccess(parameter);
                            ToastUtils.showShort("??????????????????????????????????????????");
                        }

                        @Override
                        public void onError(int code, String msg) {
                            ALog.d(TAG, "rejectSeatPick onError:" + msg);
                            ToastUtils.showShort(msg);
                        }
                    });

                }
            });
        }

        /**
         * ?????????????????????
         */
        @Override
        public void onSeatApplyAccepted(SeatApplyAcceptEvent event) {
            ALog.d(TAG, "onSeatApplyAccepted");
            final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
            PermissionUtils.permission(permissions).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    joinRtcAndShowRtcUI(event.member);
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                    ToastUtils.showShort(activity.getString(R.string.biz_live_permission_error_tips));
                    joinRtcAndShowRtcUI(event.member);
                }
            }).request();
        }

        /**
         * ?????????????????????
         */
        @Override
        public void onSeatApplyRejected(SeatApplyRejectEvent event) {
            ALog.d(TAG, "onSeatApplyRejected");
            waitAnchorAcceptFloatLayer.setVisibility(GONE);
            infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_ENABLE);
            getAudienceDialogControl().showAnchorRejectDialog(activity);
        }

        @Override
        public void onSeatMuteStateChanged(SeatMuteStateChangeEvent event) {
            ALog.d(TAG, "onSeatMuteStateChanged");
            if (AccountUtil.isCurrentUser(event.member.accountId) && !AccountUtil.isCurrentUser(event.fromUser)) {
                //?????????????????????????????????????????????????????????
                if (LinkedSeatsAudienceActionManager.enableLocalVideo && event.member.video == VideoActionType.CLOSE) {
                    ToastUtils.showShort(activity.getString(R.string.biz_live_anchor_close_your_camera));
                    linkedSeatsAudienceActionManager.refreshLinkSeatDialog(LinkSeatsStatusDialog.CAMERA_POSITION, VideoActionType.CLOSE);
                    linkedSeatsAudienceActionManager.enableVideo(false);
                } else if (!LinkedSeatsAudienceActionManager.enableLocalVideo && event.member.video == VideoActionType.OPEN) {
                    ToastUtils.showShort(activity.getString(R.string.biz_live_anchor_open_your_camera));
                    linkedSeatsAudienceActionManager.refreshLinkSeatDialog(LinkSeatsStatusDialog.CAMERA_POSITION, VideoActionType.OPEN);
                    linkedSeatsAudienceActionManager.enableVideo(true);
                } else if (LinkedSeatsAudienceActionManager.enableLocalAudio && event.member.audio == AudioActionType.CLOSE) {
                    ToastUtils.showShort(activity.getString(R.string.biz_live_anchor_close_your_microphone));
                    linkedSeatsAudienceActionManager.refreshLinkSeatDialog(LinkSeatsStatusDialog.MICROPHONE_POSITION, AudioActionType.CLOSE);
                    linkedSeatsAudienceActionManager.enableAudio(false);
                } else if (!LinkedSeatsAudienceActionManager.enableLocalAudio && event.member.audio == AudioActionType.OPEN) {
                    ToastUtils.showShort(activity.getString(R.string.biz_live_anchor_open_your_microphone));
                    linkedSeatsAudienceActionManager.refreshLinkSeatDialog(LinkSeatsStatusDialog.MICROPHONE_POSITION, AudioActionType.OPEN);
                    linkedSeatsAudienceActionManager.enableAudio(true);
                }
                LinkedSeatsAudienceActionManager.enableLocalVideo = (event.member.video == VideoActionType.OPEN);
                LinkedSeatsAudienceActionManager.enableLocalAudio = (event.member.audio == AudioActionType.OPEN);
            }
            linkSeatsRv.updateItem(event.member);
        }


        @Override
        public void onSeatOpenStateChanged(SeatVideoOpenStateChangeEvent event) {
            ALog.d(TAG, "onSeatOpenStateChanged");
        }

        @Override
        public void onSeatCustomInfoChanged(SeatCustomInfoChangeEvent event) {

        }
    };


    private void showCurrentUI(boolean showRtcUI, boolean showCdnUI) {
        isLinkingSeats = showRtcUI;
        if (linkSeatsRv != null) {
            linkSeatsRv.setVisibility(showRtcUI ? VISIBLE : GONE);
        }
        if (showRtcUI) {
            //???????????????????????????
            addSelfToLinkSeatsRv(userCenterService.getCurrentUser());
            infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.LINK_SEATS_SETTING);
            // ??????RTC?????????
            if (rtcVideoView == null) {
                rtcVideoView = new NERtcVideoView(getContext());
                addView(rtcVideoView, 0, generateDefaultLayoutParams());
            }
            //???????????????RTC?????????
            linkedSeatsAudienceActionManager.setupRemoteView(rtcVideoView, liveInfo.avRoomUid);
            rtcVideoView.setVisibility(VISIBLE);
            if (videoView != null) {
                videoView.setVisibility(GONE);
                videoView.reset();
                videoView.release();
            }
            getAudienceDialogControl().dismissAnchorInviteDialog();
        } else {
            infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_ENABLE);
            if (rtcVideoView != null) {
                rtcVideoView.setVisibility(GONE);
            }
            if (showCdnUI) {
                if (videoView == null) {
                    videoView = new CDNStreamTextureView(getContext());
                    addView(videoView, 0, generateDefaultLayoutParams());
                }
                videoView.setVisibility(VISIBLE);
                // ???????????????????????????
                horSwitchView.toSelectedPosition();
                // ???????????????????????????
                videoView.prepare(liveInfo);
                // ?????????????????????????????????????????????
                infoBinding.crvMsgList.toLatestMsg();
            } else {
                if (videoView != null) {
                    videoView.setVisibility(GONE);
                }
            }
        }
        if (waitAnchorAcceptFloatLayer != null) {
            waitAnchorAcceptFloatLayer.setVisibility(GONE);
        }
    }

    /**
     * ?????????????????????
     */
    private final ChatRoomNotify roomNotify = new SkeletonChatRoomNotify() {

        @Override
        public void onJoinRoom(boolean success, int code) {
            super.onJoinRoom(success, code);
            joinRoomSuccess = success;
            if (joinRoomSuccess) {
                infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_ENABLE);
            } else {
                infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_DISABLE);
            }
            ALog.e("=====>", "onJoinRoom " + "success " + success + ", code " + code);
        }

        @Override
        public void onMsgArrived(RoomMsg msg) {
            infoBinding.crvMsgList.appendItem(msg.message);
        }

        @Override
        public void onGiftArrived(RewardGiftInfo giftInfo) {
            giftRender.addGift(GiftCache.getGift(giftInfo.giftId).dynamicIconResId);
        }

        @Override
        public void onUserCountChanged(int count) {
            super.onUserCountChanged(count);
            includeRoomTopBinding.tvAudienceCount.setText(StringUtils.getAudienceCount(count));
        }

        @Override
        public void onRoomDestroyed(LiveChatRoomInfo roomInfo) {
            if (!canRender) {
                return;
            }
            changeErrorState(true, AudienceErrorStateView.TYPE_FINISHED);
        }

        @Override
        public void onAnchorLeave() {
            if (!canRender) {
                return;
            }
            changeErrorState(true, AudienceErrorStateView.TYPE_FINISHED);
        }

        @Override
        public void onKickedOut() {
            if (!canRender) {
                return;
            }
            if (activity != null) {
                activity.finish();
                getContext().startActivity(new Intent(getContext(), DialogHelperActivity.class));
            }
        }

        @Override
        public void onAnchorCoinChanged(AnchorCoinChangedAttachment attachment) {
            super.onAnchorCoinChanged(attachment);
            includeRoomTopBinding.tvAnchorCoinCount.setText(StringUtils.getCoinCount(attachment.totalCoinCount));
            getAudiencePKControl().onAnchorCoinChanged(attachment);
        }

        @Override
        public void onPkStatusChanged(PkStatusAttachment pkStatus) {
            super.onPkStatusChanged(pkStatus);
            if (isLinkingSeats) {
                linkedSeatsAudienceActionManager.leaveSeat(liveInfo.liveCid, new LiveRoomCallback<Void>() {
                    @Override
                    public void onError(int code, String msg) {
                        ToastUtils.showShort(msg);
                    }
                });
                if (linkSeatsRv.haveMemberInSeats()) {
                    for (SeatMemberInfo memberInfo : linkSeatsRv.getMemberList()) {
                        linkSeatsRv.remove(memberInfo);
                    }
                }
                showCurrentUI(false, true);
            }
            isPking = true;
            getAudiencePKControl().onPKStatusChanged(pkStatus);
        }

        @Override
        public void onPunishmentStatusChanged(PunishmentStatusAttachment punishmentStatus) {
            getAudiencePKControl().onPunishmentStatusChanged(punishmentStatus);
            if (!punishmentStatus.isStartState()) {
                isPking = false;
                showCurrentUI(false, true);
            }
        }

        @Override
        public void onAudienceChanged(List<AudienceInfo> infoList) {
            includeRoomTopBinding.rvAnchorPortraitList.updateAll(infoList);
        }
    };

    /**
     * ??????????????????????????????
     */
    private final AudienceErrorStateView.ClickButtonListener clickButtonListener = new AudienceErrorStateView.ClickButtonListener() {
        @Override
        public void onBackClick(View view) {
            ALog.d(TAG, "onBackClick");
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }

        @Override
        public void onRetryClick(View view) {
            ALog.d(TAG, "onRetryClick");
            if (canRender && liveInfo != null) {
                if (joinRoomSuccess) {
                    initForLiveType(true);
                } else {
                    select(liveInfo, true);
                }

            }
        }
    };


    public AudienceContentView(@NonNull BaseActivity activity) {
        super(activity);
        this.activity = activity;
        initViews();
    }

    /**
     * ??????????????????????????? view
     */
    private void initViews() {
        // ?????? view ????????????
        setBackgroundColor(Color.parseColor("#ff201C23"));
        // ?????????????????? TextureView
        videoView = new CDNStreamTextureView(getContext());
        addView(videoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // ?????????????????????
        infoBinding = ViewItemAudienceLiveRoomInfoBinding.inflate(LayoutInflater.from(getContext()), this, false);
        includeRoomTopBinding = ViewIncludeRoomTopBinding.bind(infoBinding.getRoot());
//        infoContentView = LayoutInflater.from(getContext()).inflate(R.layout.view_item_audience_live_room_info, null);
        horSwitchView = new ExtraTransparentView(getContext(), infoBinding.getRoot());
        // ???????????????????????????????????????????????????
        horSwitchView.registerSelectedRunnable(() -> {
            infoBinding.crvMsgList.toLatestMsg();
        });
        addView(horSwitchView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // ???????????????????????? status bar ?????????????????????
        StatusBarConfig.paddingStatusBarHeight(activity, horSwitchView);

        // ????????????????????????
        errorStateView = new AudienceErrorStateView(getContext());
        addView(errorStateView);
        errorStateView.setVisibility(GONE);

        // ????????????????????????
        // ?????????????????? view
        GifAnimationView gifAnimationView = new GifAnimationView(getContext());
        int size = SpUtils.getScreenWidth(getContext());
        FrameLayout.LayoutParams layoutParams = generateDefaultLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.bottomMargin = SpUtils.dp2pix(getContext(), 166);
        addView(gifAnimationView, layoutParams);
        gifAnimationView.bringToFront();
        // ?????????????????? view
        giftRender.init(gifAnimationView);

        // ?????????????????????
        InputUtils.registerSoftInputListener(activity, new InputUtils.InputParamHelper() {
            @Override
            public int getHeight() {
                return AudienceContentView.this.getHeight();
            }

            @Override
            public EditText getInputView() {
                return infoBinding.etRoomMsgInput;
            }
        });
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param info ???????????????
     */
    public void renderData(LiveInfo info) {
        this.liveInfo = info;
        linkedSeatsAudienceActionManager = LinkedSeatsAudienceActionManager.getInstance(activity);
        // ?????????????????????????????????????????????
        errorStateView.renderInfo(info.avatar, info.nickname);
        videoView.setUp(canRender);
        // ???????????????
        infoBinding.etRoomMsgInput.setOnEditorActionListener((v, actionId, event) -> {
            String input = infoBinding.etRoomMsgInput.getText().toString();
            InputUtils.hideSoftInput(infoBinding.etRoomMsgInput);
            audienceControl.sendTextMsg(input);
            return true;
        });
        infoBinding.etRoomMsgInput.setVisibility(GONE);
        // ??????????????????
        includeRoomTopBinding.tvAudienceCount.setText(StringUtils.getAudienceCount(liveInfo.audienceCount));
        // ????????????
        ImageLoader.with(getContext().getApplicationContext()).circleLoad(info.avatar, includeRoomTopBinding.ivAnchorPortrait);
        // ????????????
        includeRoomTopBinding.tvAnchorNickname.setText(info.nickname);
        // ????????????
        infoBinding.ivRoomClose.setOnClickListener(v -> {
            // ???????????????????????????
            activity.finish();
        });

//        // ????????????
        infoBinding.ivRoomGift.setOnClickListener(v -> {
            if (giftDialog == null) {
                giftDialog = new GiftDialog(activity);
            }

            giftDialog.show(giftInfo -> {
                RewardGiftInfo rewardGiftInfo = new RewardGiftInfo(liveInfo.liveCid, userCenterService.getCurrentUser().accountId, userCenterService.getCurrentUser().nickname, liveInfo.accountId, giftInfo.giftId);
                LiveInteraction.rewardAnchor(getAudiencePKControl().isPk(), rewardGiftInfo).subscribe(new ResourceSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(@NonNull Boolean aBoolean) {
                        if (!aBoolean) {
                            ToastUtils.showShort("??????????????????");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ToastUtils.showShort("??????????????????");
                    }
                });
            });
        });

        // ?????????????????????
        infoBinding.tvRoomMsgInput.setOnClickListener(v -> InputUtils.showSoftInput(infoBinding.etRoomMsgInput));

        //????????????or??????????????????
        infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_ENABLE);
        infoBinding.btnMultiFunction.setOnButtonClickListener(new MultiFunctionButton.OnButtonClickListener() {
            @Override
            public void applySeat() {
                //????????????
                infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_DISABLE);
                linkedSeatsAudienceActionManager.applySeat(liveInfo.liveCid, new LiveRoomCallback<Void>() {
                    @Override
                    public void onSuccess() {
                        waitAnchorAcceptFloatLayer.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onError(int code, String msg) {
                        if (!TextUtils.isEmpty(msg)) {
                            ToastUtils.showShort(msg);
                        }
                        if (ApiErrorCode.HAD_APPLIED_SEAT == code) {
                            infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_DISABLE);
                            waitAnchorAcceptFloatLayer.setVisibility(VISIBLE);
                            return;
                        } else if (ApiErrorCode.DONT_APPLY_SEAT == code) {
                            waitAnchorAcceptFloatLayer.setVisibility(GONE);
                        }
                        //??????????????????
                        infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_ENABLE);
                    }
                });
            }

            @Override
            public void showLinkSeatsStatusDialog() {
                linkedSeatsAudienceActionManager.showLinkSeatsStatusDialog(liveInfo);
            }
        });

        linkSeatsRv = new LinkSeatsAudienceRecycleView(getContext());
        FrameLayout.LayoutParams params = new LayoutParams(SizeUtils.dp2px(88), LayoutParams.WRAP_CONTENT);
        params.topMargin = SizeUtils.dp2px(108);
        params.rightMargin = SizeUtils.dp2px(6);
        params.gravity = Gravity.TOP | Gravity.END;
        addView(linkSeatsRv, params);
        FrameLayout.LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(44));
        params2.topMargin = SizeUtils.dp2px(108);
        waitAnchorAcceptFloatLayer = new WaitAnchorAcceptView(getContext());
        waitAnchorAcceptFloatLayer.setAudienceLiveRoomDelegate(seatCallback);
        waitAnchorAcceptFloatLayer.setCancelApplySeatClickCallback(new WaitAnchorAcceptView.CancelApplySeatClickCallback() {
            @Override
            public void cancel() {
                infoBinding.btnMultiFunction.setType(MultiFunctionButton.Type.APPLY_SEAT_ENABLE);
            }
        });
        addView(waitAnchorAcceptFloatLayer, params2);
        waitAnchorAcceptFloatLayer.setVisibility(GONE);
        waitAnchorAcceptFloatLayer.setLiveInfo(liveInfo);
        linkSeatsRv.setVisibility(GONE);
        linkSeatsRv.setUseScene(LinkSeatsAudienceRecycleView.UseScene.AUDIENCE);
        linkSeatsRv.setLiveInfo(liveInfo);
    }


    /**
     * ??????????????????
     */
    public void prepare() {
        showCurrentUI(false, true);
        changeErrorState(false, -1);
        canRender = true;
    }

    /**
     * ????????????
     */
    public void select(LiveInfo liveInfo, boolean refreshLinkedSeatsUI) {
        this.liveInfo = liveInfo;
        linkedSeatsAudienceActionManager.setData(seatCallback, liveInfo);
        NERTCAudienceLiveRoomImpl liveRoomImpl = (NERTCAudienceLiveRoomImpl) NERTCAudienceLiveRoom.sharedInstance();
        liveRoomImpl.registerMsgCallback(true);
        // ???????????????
        try {
            audienceControl.joinRoom(new LiveChatRoomInfo(liveInfo.chatRoomId, liveInfo.accountId,
                    String.valueOf(liveInfo.roomUid), liveInfo.audienceCount));
        } catch (Exception e) {
            // ???????????????????????????????????????????????????
            if (activity != null) {
                activity.finish();
            }
        }
        audienceControl.registerNotify(roomNotify, true);
        // ?????????????????????????????????????????????
        initForLiveType(refreshLinkedSeatsUI);
    }

    /**
     * ??????????????????
     */
    public void release() {
        if (!canRender) {
            return;
        }
        canRender = false;
        // ?????????????????????
        videoView.release();
        // ??????????????????
        giftRender.release();
        // ??????????????????
        infoBinding.crvMsgList.clearAllInfo();
        if (audiencePKControl != null) {
            audiencePKControl.release();
        }
        // ??????????????????????????????RTC??????
        if (isLinkingSeats) {
            ALog.d(TAG, "release:" + liveInfo.toString());
            linkedSeatsAudienceActionManager.leaveSeat(liveInfo.liveCid, new LiveRoomCallback<Void>() {
                @Override
                public void onError(int code, String msg) {
                    ToastUtils.showShort(msg);
                }
            });
            isLinkingSeats = false;
            linkSeatsRv.remove(0);
        }
        linkedSeatsAudienceActionManager.onDestory();
        NERTCAudienceLiveRoomImpl liveRoomImpl = (NERTCAudienceLiveRoomImpl) NERTCAudienceLiveRoom.sharedInstance();
        liveRoomImpl.registerMsgCallback(false);
    }


    private void changeErrorState(boolean error, int type) {
        if (!canRender) {
            return;
        }
        if (error) {
            waitAnchorAcceptFloatLayer.setVisibility(GONE);
            showCurrentUI(false, false);
            videoView.reset();
            if (type == AudienceErrorStateView.TYPE_FINISHED) {
                release();
            } else {
                videoView.release();
            }
        }

        infoBinding.groupNormal.setVisibility(error ? GONE : VISIBLE);
        if (errorStateView != null) {
            errorStateView.setVisibility(error ? VISIBLE : GONE);
        }
        if (error && errorStateView != null) {
            errorStateView.updateType(type, clickButtonListener);
        }
    }

    private void initForLiveType(boolean refreshLinkedSeatsUI) {
        LiveInteraction.queryAnchorRoomInfo(liveInfo.accountId, liveInfo.liveCid)
                .subscribe(new ResourceSingleObserver<BaseResponse<AnchorQueryInfo>>() {
                    @Override
                    public void onSuccess(@NonNull BaseResponse<AnchorQueryInfo> response) {
                        if (!canRender) {
                            return;
                        }
                        if (response.isSuccessful()) {
                            AnchorQueryInfo anchorQueryInfo = response.data;
                            includeRoomTopBinding.tvAnchorCoinCount.setText(StringUtils.getCoinCount(anchorQueryInfo.coinTotal));
                            PkRecord record = anchorQueryInfo.pkRecord;
                            if (record != null && (record.status == LiveStatus.PK_LIVING || record.status == LiveStatus.PK_PUNISHMENT)) {
                                getAudiencePKControl().showPkMaskUI(canRender, anchorQueryInfo, liveInfo);
                            }
                            // ????????????????????????
                            if (anchorQueryInfo.seatList != null && !anchorQueryInfo.seatList.isEmpty()) {
                                boolean isInSeat = false;
                                for (SeatMemberInfo memberInfo : anchorQueryInfo.seatList) {

                                    // ??????????????????showCurrentUI??????addSelfToLinkSeatsRv??????
                                    if (!AccountUtil.isCurrentUser(memberInfo.accountId)) {
                                        linkSeatsRv.appendItem(memberInfo);
                                    } else {
                                        isInSeat = true;
                                    }
                                }
                                if (isInSeat) {
                                    if (isFirstShowNormalUI) {
                                        showCurrentUI(false, true);
                                    } else {
                                        // ?????????????????????????????????
                                        if (refreshLinkedSeatsUI) {
                                            linkSeatsRv.getAdapter().notifyDataSetChanged();
                                        }
                                        showCurrentUI(true, false);
                                    }

                                } else {
                                    showCurrentUI(false, true);
                                    //??????????????????????????????????????????????????????????????????????????????????????????CDN????????????
                                    videoView.setLinkingSeats(true);
                                }
                            } else {
                                showCurrentUI(false, true);
                            }
                            changeErrorState(false, -1);
                            isFirstShowNormalUI = false;
                        } else if (response.code == LiveServerApi.ERROR_CODE_ROOM_NOT_EXIST || response.code == LiveServerApi.ERROR_CODE_USER_NOT_IN_ROOM) {
                            changeErrorState(true, AudienceErrorStateView.TYPE_FINISHED);
                        } else {
                            changeErrorState(true, AudienceErrorStateView.TYPE_ERROR);
                            ALog.e(TAG, "?????????????????????????????????????????? " + response);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        ALog.e(TAG, "????????????????????????");
                        changeErrorState(true, AudienceErrorStateView.TYPE_ERROR);
                    }
                });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        // ?????????????????????????????????
        if (!ViewUtils.isInView(infoBinding.etRoomMsgInput, x, y)) {
            InputUtils.hideSoftInput(infoBinding.etRoomMsgInput);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void joinRtcAndShowRtcUI(SeatMemberInfo member) {
        if (member == null) {
            ToastUtils.showShort("joinRtcAndShowRtcUI error,member==null");
            return;
        }
        //????????????????????????CDN?????????RTC??????????????????????????????RTC???
        waitAnchorAcceptFloatLayer.setVisibility(GONE);
        errorStateView.setVisibility(GONE);
        initForLiveType(true);
        UserModel currentUser = userCenterService.getCurrentUser();
        if (currentUser != null) {
            String avRoomUid = currentUser.avRoomUid;
            if (!TextUtils.isEmpty(avRoomUid)) {
                try {
                    linkedSeatsAudienceActionManager.joinRtcChannel(member.avRoomCheckSum, member.avRoomCName, Long.parseLong(avRoomUid), member.avRoomCid);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                ALog.d(TAG, "joinRtcAndshowRtcUI avRoomUid??????");
            }
        }

    }


    /**
     * ????????????????????????????????????????????????
     */
    private void addSelfToLinkSeatsRv(UserModel currentUser) {
        int selfIndex = 0;
        SeatMemberInfo member = new SeatMemberInfo();
        member.accountId = currentUser.accountId;
        member.nickName = currentUser.nickname;
        member.avatar = currentUser.avatar;
        member.audio = AudioActionType.OPEN;
        member.video = VideoActionType.OPEN;
        linkSeatsRv.appendItem(selfIndex, member);
    }

    private AudiencePKControl getAudiencePKControl() {
        if (audiencePKControl == null) {
            audiencePKControl = new AudiencePKControl();
            audiencePKControl.init(activity, videoView, infoBinding.getRoot());
        }
        return audiencePKControl;
    }

    private AudienceDialogControl getAudienceDialogControl() {
        if (audienceDialogControl == null) {
            audienceDialogControl = new AudienceDialogControl();
        }
        return audienceDialogControl;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NetworkUtils.registerNetworkStatusChangedListener(onNetworkStatusChangedListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NetworkUtils.unregisterNetworkStatusChangedListener(onNetworkStatusChangedListener);
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
