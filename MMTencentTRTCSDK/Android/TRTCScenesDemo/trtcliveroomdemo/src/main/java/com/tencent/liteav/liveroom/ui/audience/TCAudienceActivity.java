package com.tencent.liteav.liveroom.ui.audience;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.beauty.view.BeautyPanel;
import com.tencent.liteav.demo.beauty.BeautyParams;
import com.tencent.liteav.liveroom.R;
import com.tencent.liteav.liveroom.model.TRTCLiveRoom;
import com.tencent.liteav.liveroom.model.TRTCLiveRoomCallback;
import com.tencent.liteav.liveroom.model.TRTCLiveRoomDef;
import com.tencent.liteav.liveroom.model.TRTCLiveRoomDelegate;
import com.tencent.liteav.liveroom.ui.common.adapter.TCUserAvatarListAdapter;
import com.tencent.liteav.liveroom.ui.common.msg.TCChatEntity;
import com.tencent.liteav.liveroom.ui.common.msg.TCChatMsgListAdapter;
import com.tencent.liteav.liveroom.ui.common.utils.TCConstants;
import com.tencent.liteav.liveroom.ui.common.utils.TCUtils;
import com.tencent.liteav.liveroom.ui.widget.InputTextMsgDialog;
import com.tencent.liteav.liveroom.ui.widget.danmaku.TCDanmuMgr;
import com.tencent.liteav.liveroom.ui.widget.like.TCHeartLayout;
import com.tencent.liteav.liveroom.ui.widget.video.TCVideoView;
import com.tencent.liteav.liveroom.ui.widget.video.TCVideoViewMgr;
import com.tencent.liteav.login.model.ProfileManager;
import com.tencent.liteav.login.model.UserModel;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import master.flame.danmaku.controller.IDanmakuView;

/**
 * Module:   TCAudienceActivity
 * <p>
 * Function: ??????????????????
 * <p>
 * <p>
 * 1. MLVB ????????????????????????????????????{@link TCAudienceActivity#enterRoom()} ??? {@link TCAudienceActivity#exitRoom()}
 * <p>
 * 2. MLVB ????????????????????????????????????{@link TCAudienceActivity#startLinkMic()} ??? {@link TCAudienceActivity#stopLinkMic()}
 * <p>
 * 3. ????????????????????????????????????
 **/
public class TCAudienceActivity extends AppCompatActivity implements View.OnClickListener, InputTextMsgDialog.OnTextSendListener {
    private static final String TAG               = TCAudienceActivity.class.getSimpleName();

    private static final long   LINK_MIC_INTERVAL = 3 * 1000;    //??????????????????

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ConstraintLayout mRootView;              // ??????Windows???Root View
    private TXCloudVideoView            mVideoViewAnchor;       // ????????????????????????View
    private TXCloudVideoView            mVideoViewPKAnchor;     // ??????PK???????????????View
    private Guideline mGuideLineVertical;     // ConstraintLayout??????????????????
    private Guideline                   mGuideLineHorizontal;   // ConstraintLayout??????????????????
    private TextView                    mTextAnchorLeave;       // ?????????????????????????????????
    private ImageView                   mImageBackground;       // ?????????????????????????????????
    private InputTextMsgDialog          mInputTextMsgDialog;    // ???????????????
    private ListView                    mListIMMessage;         // ?????????????????????????????????
    private TCHeartLayout               mHeartLayout;           // ??????????????????????????????????????????&?????????
    private Button                      mButtonLinkMic;         // ????????????
    private Button                      mButtonSwitchCamera;    // ?????????????????????
    private ImageView                   mImageAnchorAvatar;     // ????????????????????????
    private TextView                    mTextAnchorName;        // ????????????????????????
    private TextView                    mMemberCount;           // ??????????????????????????????
    private RecyclerView mRecyclerUserAvatar;    // ?????????????????????????????????
    private AlertDialog mDialogError;           // ???????????????Dialog
    private IDanmakuView                mDanmuView;             // ?????????????????????
    private TCDanmuMgr                  mDanmuMgr;              // ??????????????????
    private TCVideoViewMgr              mVideoViewMgr;          // ???????????????????????????????????????
    private BeautyPanel                 mBeautyControl;         // ?????????????????????
    private RelativeLayout              mPKContainer;
    private Toast                       mToastNotice;
    private Timer                       mNoticeTimer;
    private TCChatMsgListAdapter        mChatMsgListAdapter;    // mListIMMessage????????????
    private TCUserAvatarListAdapter     mUserAvatarListAdapter; // mUserAvatarList????????????
    private TRTCLiveRoom                mLiveRoom;              // MLVB ??????
    private TCLikeFrequencyControl      mLikeFrequencyControl;  //????????????????????????
    private ArrayList<TCChatEntity>     mArrayListChatEntity = new ArrayList<>();   // ??????????????????

    private boolean     mShowLog;
    private long        mLastLinkMicTime;            // ????????????????????????????????????????????????
    private long        mCurrentAudienceCount;       // ??????????????????
    private boolean     isEnterRoom      = false;    // ????????????????????????????????????
    private boolean     isUseCDNPlay     = false;    // ?????????????????????CDN??????
    private boolean     mIsAnchorEnter   = false;    // ??????????????????????????????
    private boolean     mIsBeingLinkMic  = false;    // ?????????????????????????????????
    private int         mRoomId          = 0;
    private int         mCurrentStatus = TRTCLiveRoomDef.ROOM_STATUS_NONE;
    private String      mAnchorAvatarURL;            // ????????????????????????
    private String      mAnchorNickname;             // ????????????
    private String      mAnchorId;                   // ??????id
    private String      mSelfUserId      = "";       // ??????id
    private String      mSelfNickname    = "";       // ????????????
    private String      mSelfAvatar      = "";       // ????????????
    private String      mCoverUrl        = "";       // ?????????????????????URL
    private Runnable    mGetAudienceRunnable;

    //????????????????????????????????????
    private Runnable mShowAnchorLeave = new Runnable() {
        @Override
        public void run() {
            if (mTextAnchorLeave != null) {
                mTextAnchorLeave.setVisibility(mIsAnchorEnter ? View.GONE : View.VISIBLE);
                mImageBackground.setVisibility(mIsAnchorEnter ? View.GONE : View.VISIBLE);
            }
        }
    };

    // trtclive ??????
    private TRTCLiveRoomDelegate mTRTCLiveRoomDelegate = new TRTCLiveRoomDelegate() {
        @Override
        public void onError(int code, String message) {

        }

        @Override
        public void onWarning(int code, String message) {

        }

        @Override
        public void onDebugLog(String message) {

        }

        @Override
        public void onRoomInfoChange(TRTCLiveRoomDef.TRTCLiveRoomInfo roomInfo) {
            int oldStatus = mCurrentStatus;
            mCurrentStatus = roomInfo.roomStatus;
            // ??????CDN?????????????????????????????????????????????????????????????????????
            if (isUseCDNPlay) {
                return;
            }
            // ???PK??????????????????????????????
            // ???PK?????????????????????????????????
            setAnchorViewFull(mCurrentStatus != TRTCLiveRoomDef.ROOM_STATUS_PK);
            Log.d(TAG, "onRoomInfoChange: " + mCurrentStatus);
            if (oldStatus == TRTCLiveRoomDef.ROOM_STATUS_PK
                    && mCurrentStatus != TRTCLiveRoomDef.ROOM_STATUS_PK) {
                // ??????????????????PK????????????????????????????????????
                TCVideoView videoView = mVideoViewMgr.getPKUserView();
                mVideoViewPKAnchor = videoView.getPlayerVideo();
                if (mPKContainer.getChildCount() != 0) {
                    mPKContainer.removeView(mVideoViewPKAnchor);
                    videoView.addView(mVideoViewPKAnchor);
                    mVideoViewMgr.clearPKView();
                    mVideoViewPKAnchor = null;
                }
            } else if (mCurrentStatus == TRTCLiveRoomDef.ROOM_STATUS_PK) {
                TCVideoView videoView = mVideoViewMgr.getPKUserView();
                mVideoViewPKAnchor = videoView.getPlayerVideo();
                videoView.removeView(mVideoViewPKAnchor);
                mPKContainer.addView(mVideoViewPKAnchor);
            }
        }

        @Override
        public void onRoomDestroy(String roomId) {
            showErrorAndQuit(0, getString(R.string.trtcliveroom_warning_room_disband));
        }

        @Override
        public void onAnchorEnter(final String userId) {
            if (userId.equals(mAnchorId)) {
                // ???????????????????????????
                mIsAnchorEnter = true;
                mTextAnchorLeave.setVisibility(View.GONE);
                mVideoViewAnchor.setVisibility(View.VISIBLE);
                mImageBackground.setVisibility(View.GONE);
                mLiveRoom.startPlay(userId, mVideoViewAnchor, new TRTCLiveRoomCallback.ActionCallback() {
                    @Override
                    public void onCallback(int code, String msg) {
                        if (code != 0) {
                            onAnchorExit(userId);
                        }
                    }
                });
            } else {
                TCVideoView view = mVideoViewMgr.applyVideoView(userId);
                view.showKickoutBtn(false);
                mLiveRoom.startPlay(userId, view.getPlayerVideo(), null);
            }
        }

        @Override
        public void onAnchorExit(String userId) {
            if (userId.equals(mAnchorId)) {
                mVideoViewAnchor.setVisibility(View.GONE);
                mImageBackground.setVisibility(View.VISIBLE);
                mTextAnchorLeave.setVisibility(View.VISIBLE);
                mLiveRoom.stopPlay(userId, null);
            } else {
                // ??????PK??????????????????????????????????????????????????????
                mVideoViewMgr.recycleVideoView(userId);
                mLiveRoom.stopPlay(userId, null);
            }
        }

        @Override
        public void onAudienceEnter(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
            Log.d(TAG, "onAudienceEnter: " + userInfo);
            handleAudienceJoinMsg(userInfo);
        }

        @Override
        public void onAudienceExit(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
            Log.d(TAG, "onAudienceExit: " + userInfo);
            handleAudienceQuitMsg(userInfo);
        }

        @Override
        public void onRequestJoinAnchor(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo, String reason, int timeout) {

        }

        @Override
        public void onKickoutJoinAnchor() {
            ToastUtils.showLong(R.string.trtcliveroom_warning_kick_out_by_anchor);
            stopLinkMic();
        }

        @Override
        public void onRequestRoomPK(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo, int timeout) {

        }

        @Override
        public void onQuitRoomPK() {

        }

        @Override
        public void onRecvRoomTextMsg(String message, TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
            handleTextMsg(userInfo, message);
        }

        @Override
        public void onRecvRoomCustomMsg(String cmd, String message, TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
            int type = Integer.valueOf(cmd);
            switch (type) {
                case TCConstants.IMCMD_PRAISE:
                    handlePraiseMsg(userInfo);
                    break;
                case TCConstants.IMCMD_DANMU:
                    handleDanmuMsg(userInfo, message);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * ??????????????????????????????
     *
     * @param errorCode
     * @param errorMsg
     */
    protected void showErrorAndQuit(int errorCode, String errorMsg) {
        if (mDialogError == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TRTCLiveRoomDialogTheme)
                    .setTitle(R.string.trtcliveroom_error)
                    .setMessage(errorMsg)
                    .setNegativeButton(R.string.trtcliveroom_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDialogError.dismiss();
                            exitRoom();
                            finish();
                        }
                    });

            mDialogError = builder.create();
        }
        if (mDialogError.isShowing()) {
            mDialogError.dismiss();
        }
        mDialogError.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.TRTCLiveRoomBeautyTheme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.trtcliveroom_activity_audience);

        Intent intent = getIntent();
        isUseCDNPlay = intent.getBooleanExtra(TCConstants.USE_CDN_PLAY, false);
        mRoomId = intent.getIntExtra(TCConstants.GROUP_ID, 0);
        mAnchorId = intent.getStringExtra(TCConstants.PUSHER_ID);
        mAnchorNickname = intent.getStringExtra(TCConstants.PUSHER_NAME);
        mCoverUrl = intent.getStringExtra(TCConstants.COVER_PIC);
        mAnchorAvatarURL = intent.getStringExtra(TCConstants.PUSHER_AVATAR);

        UserModel userModel = ProfileManager.getInstance().getUserModel();
        mSelfNickname = userModel.userName;
        mSelfUserId = userModel.userId;
        mSelfAvatar = userModel.userAvatar;

        List<TCVideoView> videoViewList = new ArrayList<>();
        videoViewList.add((TCVideoView) findViewById(R.id.video_view_link_mic_1));
        videoViewList.add((TCVideoView) findViewById(R.id.video_view_link_mic_2));
        videoViewList.add((TCVideoView) findViewById(R.id.video_view_link_mic_3));
        mVideoViewMgr = new TCVideoViewMgr(videoViewList, null);

        // ????????? liveRoom ??????
        mLiveRoom = TRTCLiveRoom.sharedInstance(this);
        mLiveRoom.setDelegate(mTRTCLiveRoomDelegate);

        initView();
        enterRoom();
        mHandler.postDelayed(mShowAnchorLeave, 3000);
    }


    private void initView() {
        mVideoViewAnchor = (TXCloudVideoView) findViewById(R.id.video_view_anchor);
        mVideoViewAnchor.setLogMargin(10, 10, 45, 55);
        mListIMMessage = (ListView) findViewById(R.id.lv_im_msg);
        mListIMMessage.setVisibility(View.VISIBLE);
        mHeartLayout = (TCHeartLayout) findViewById(R.id.heart_layout);
        mTextAnchorName = (TextView) findViewById(R.id.tv_anchor_broadcasting_time);
        mTextAnchorName.setText(TCUtils.getLimitString(mAnchorNickname, 10));

        findViewById(R.id.iv_anchor_record_ball).setVisibility(View.GONE);

        mRecyclerUserAvatar = (RecyclerView) findViewById(R.id.rv_audience_avatar);
        mRecyclerUserAvatar.setVisibility(View.VISIBLE);
        mUserAvatarListAdapter = new TCUserAvatarListAdapter(this, mAnchorId);
        mRecyclerUserAvatar.setAdapter(mUserAvatarListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerUserAvatar.setLayoutManager(linearLayoutManager);

        mInputTextMsgDialog = new InputTextMsgDialog(this, R.style.TRTCLiveRoomInputDialog);
        mInputTextMsgDialog.setmOnTextSendListener(this);

        mImageAnchorAvatar = (ImageView) findViewById(R.id.iv_anchor_head);

        mImageAnchorAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog();
            }
        });

        mMemberCount = (TextView) findViewById(R.id.tv_room_member_counts);

        mCurrentAudienceCount++;
        mMemberCount.setText(String.format(Locale.CHINA, "%d", mCurrentAudienceCount));
        mChatMsgListAdapter = new TCChatMsgListAdapter(this, mListIMMessage, mArrayListChatEntity);
        mListIMMessage.setAdapter(mChatMsgListAdapter);

        mDanmuView = (IDanmakuView) findViewById(R.id.anchor_danmaku_view);
        mDanmuView.setVisibility(View.VISIBLE);
        mDanmuMgr = new TCDanmuMgr(this);
        mDanmuMgr.setDanmakuView(mDanmuView);

        mImageBackground = (ImageView) findViewById(R.id.audience_background);
        mImageBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TCUtils.showPicWithUrl(TCAudienceActivity.this
                , mImageBackground, mCoverUrl, R.drawable.trtcliveroom_bg_cover);

        mButtonLinkMic = (Button) findViewById(R.id.audience_btn_linkmic);
        mButtonLinkMic.setVisibility(View.VISIBLE);
        mButtonLinkMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsBeingLinkMic) {
                    long curTime = System.currentTimeMillis();
                    if (curTime < mLastLinkMicTime + LINK_MIC_INTERVAL) {
                        Toast.makeText(getApplicationContext(), R.string.trtcliveroom_tips_rest, Toast.LENGTH_SHORT).show();
                    } else {
                        mLastLinkMicTime = curTime;
                        startLinkMic();
                    }
                } else {
                    stopLinkMic();
                }
            }
        });

        mButtonSwitchCamera = (Button) findViewById(R.id.audience_btn_switch_cam);
        mButtonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBeingLinkMic) {
                    mLiveRoom.switchCamera();
                }
            }
        });

        //????????????
        mBeautyControl = (BeautyPanel) findViewById(R.id.beauty_panel);
        mBeautyControl.setBeautyManager(mLiveRoom.getBeautyManager());

        mGuideLineVertical = (Guideline) findViewById(R.id.gl_vertical);
        mGuideLineHorizontal = (Guideline) findViewById(R.id.gl_horizontal);
        mPKContainer = (RelativeLayout) findViewById(R.id.pk_container);
        mRootView = (ConstraintLayout) findViewById(R.id.root);
        TCUtils.showPicWithUrl(TCAudienceActivity.this, mImageAnchorAvatar, mAnchorAvatarURL, R.drawable.trtcliveroom_bg_cover);
        mTextAnchorLeave = (TextView) findViewById(R.id.tv_anchor_leave);
    }

    private void setAnchorViewFull(boolean isFull) {
        if (isFull) {
            ConstraintSet set = new ConstraintSet();
            set.clone(mRootView);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.applyTo(mRootView);
        } else {
            ConstraintSet set = new ConstraintSet();
            set.clone(mRootView);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.BOTTOM, mGuideLineHorizontal.getId(), ConstraintSet.BOTTOM);
            set.connect(mVideoViewAnchor.getId(), ConstraintSet.END, mGuideLineVertical.getId(), ConstraintSet.END);
            set.applyTo(mRootView);
        }
    }

    /**
     * ??????????????????
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmuMgr != null) {
            mDanmuMgr.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmuMgr != null) {
            mDanmuMgr.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveRoom.showVideoDebugLog(false);
        mHandler.removeCallbacks(mGetAudienceRunnable);
        mHandler.removeCallbacks(mShowAnchorLeave);
        if (mDanmuMgr != null) {
            mDanmuMgr.destroy();
            mDanmuMgr = null;
        }
        exitRoom();
        mVideoViewMgr.recycleVideoView();
        mVideoViewMgr = null;
        stopLinkMic();
        hideNoticeToast();
    }

    /**
     * ??????????????????
     */
    private void enterRoom() {
        if (isEnterRoom) {
            return;
        }
        mLiveRoom.enterRoom(mRoomId, new TRTCLiveRoomCallback.ActionCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (code == 0) {
                    //????????????
                    ToastUtils.showShort(R.string.trtcliveroom_tips_enter_room_success);
                    isEnterRoom = true;
                    getAudienceList();
                } else {
                    ToastUtils.showLong(getString(R.string.trtcliveroom_tips_enter_room_fail, code));
                    exitRoom();
                    finish();
                }
            }
        });
    }

    private void getAudienceList() {
        mGetAudienceRunnable = new Runnable() {
            @Override
            public void run() {
                mLiveRoom.getAudienceList(new TRTCLiveRoomCallback.UserListCallback() {
                    @Override
                    public void onCallback(int code, String msg, List<TRTCLiveRoomDef.TRTCLiveUserInfo> list) {
                        if (code == 0) {
                            for (TRTCLiveRoomDef.TRTCLiveUserInfo info : list) {
                                mUserAvatarListAdapter.addItem(info);
                            }
                            mCurrentAudienceCount += list.size();
                            mMemberCount.setText(String.format(Locale.CHINA, "%d", mCurrentAudienceCount));
                        } else {
                            mHandler.postDelayed(mGetAudienceRunnable, 2000);
                        }
                    }
                });
            }
        };
        // ???????????????????????????????????????????????????????????????????????????
        mHandler.postDelayed(mGetAudienceRunnable, 2000);
    }

    private void exitRoom() {
        if (isEnterRoom && mLiveRoom != null) {
            mLiveRoom.exitRoom(null);
            isEnterRoom = false;
        }
    }

    /**
     * ??????????????????
     */
    private void startLinkMic() {
        if (!TCUtils.checkRecordPermission(TCAudienceActivity.this)) {
            showNoticeToast(getString(R.string.trtcliveroom_tips_start_camera_audio));
            return;
        }

        mButtonLinkMic.setEnabled(false);
        mButtonLinkMic.setBackgroundResource(R.drawable.trtcliveroom_linkmic_off);

        showNoticeToast(getString(R.string.trtcliveroom_wait_anchor_accept));

        mLiveRoom.requestJoinAnchor(getString(R.string.trtcliveroom_request_link_mic_anchor, mSelfUserId), new TRTCLiveRoomCallback.ActionCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (code == 0) {
                    // ????????????
                    hideNoticeToast();
                    ToastUtils.showShort(getString(R.string.trtcliveroom_anchor_accept_link_mic));
                    joinPusher();
                    return;
                }
                if (code == -1) {
                    // ????????????
                    ToastUtils.showShort(msg);
                } else {
                    //????????????
                    ToastUtils.showShort(getString(R.string.trtcliveroom_error_request_link_mic, msg));
                }
                mButtonLinkMic.setEnabled(true);
                hideNoticeToast();
                mIsBeingLinkMic = false;
                mButtonLinkMic.setBackgroundResource(R.drawable.trtcliveroom_linkmic_on);
            }
        });
    }

    private void joinPusher() {
        TCVideoView videoView = mVideoViewMgr.applyVideoView(mSelfUserId);

        BeautyParams beautyParams = new BeautyParams();
        mLiveRoom.getBeautyManager().setBeautyStyle(beautyParams.mBeautyStyle);
        mLiveRoom.getBeautyManager().setBeautyLevel(beautyParams.mBeautyLevel);
        mLiveRoom.getBeautyManager().setWhitenessLevel(beautyParams.mWhiteLevel);
        mLiveRoom.getBeautyManager().setRuddyLevel(beautyParams.mRuddyLevel);
        mLiveRoom.startCameraPreview(true, videoView.getPlayerVideo(), new TRTCLiveRoomCallback.ActionCallback() {
            @Override
            public void onCallback(int code, String msg) {
                if (code == 0) {
                    mLiveRoom.startPublish(mSelfUserId + "_stream", new TRTCLiveRoomCallback.ActionCallback() {
                        @Override
                        public void onCallback(int code, String msg) {
                            if (code == 0) {
                                mButtonLinkMic.setEnabled(true);
                                mIsBeingLinkMic = true;
                                if (mButtonSwitchCamera != null) {
                                    mButtonSwitchCamera.setVisibility(View.VISIBLE);
                                }
                            } else {
                                stopLinkMic();
                                mButtonLinkMic.setEnabled(true);
                                mButtonLinkMic.setBackgroundResource(R.drawable.trtcliveroom_linkmic_on);
                                Toast.makeText(TCAudienceActivity.this, getString(R.string.trtcliveroom_fail_link_mic, msg), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void stopLinkMic() {
        mIsBeingLinkMic = false;
        //????????????Button
        if (mButtonLinkMic != null) {
            mButtonLinkMic.setEnabled(true);
            mButtonLinkMic.setBackgroundResource(R.drawable.trtcliveroom_linkmic_on);
        }
        //?????????????????????Button
        if (mButtonSwitchCamera != null) {
            mButtonSwitchCamera.setVisibility(View.INVISIBLE);
        }
        // ??????
        mLiveRoom.stopCameraPreview();
        mLiveRoom.stopPublish(null);
        if (mVideoViewMgr != null) {
            mVideoViewMgr.recycleVideoView(mSelfUserId);
        }
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     */
    public void handleAudienceJoinMsg(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
        //?????????????????? ??????false???????????????????????????????????????????????????
        if (!mUserAvatarListAdapter.addItem(userInfo)) {
            return;
        }

        mCurrentAudienceCount++;
        mMemberCount.setText(String.format(Locale.CHINA, "%d", mCurrentAudienceCount));
        //?????????????????????????????????
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(getString(R.string.trtcliveroom_notification));
        if (TextUtils.isEmpty(userInfo.userName)) {
            entity.setContent(getString(R.string.trtcliveroom_user_join_live, userInfo.userId));
        } else {
            entity.setContent(getString(R.string.trtcliveroom_user_join_live, userInfo.userName));
        }
        entity.setType(TCConstants.MEMBER_ENTER);
        notifyMsg(entity);
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     */
    public void handleAudienceQuitMsg(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
        if (mCurrentAudienceCount > 0) {
            mCurrentAudienceCount--;
        } else {
            Log.d(TAG, "????????????????????????????????????????????????");
        }

        mMemberCount.setText(String.format(Locale.CHINA, "%d", mCurrentAudienceCount));

        mUserAvatarListAdapter.removeItem(userInfo.userId);

        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(getString(R.string.trtcliveroom_notification));
        if (TextUtils.isEmpty(userInfo.userName)) {
            entity.setContent(getString(R.string.trtcliveroom_user_quit_live, userInfo.userId));
        } else {
            entity.setContent(getString(R.string.trtcliveroom_user_quit_live, userInfo.userName));
        }
        entity.setType(TCConstants.MEMBER_EXIT);
        notifyMsg(entity);
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     */
    public void handlePraiseMsg(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo) {
        TCChatEntity entity = new TCChatEntity();

        entity.setSenderName(getString(R.string.trtcliveroom_notification));
        if (TextUtils.isEmpty(userInfo.userName)) {
            entity.setContent(getString(R.string.trtcliveroom_user_click_like, userInfo.userId));
        } else {
            entity.setContent(getString(R.string.trtcliveroom_user_click_like, userInfo.userName));
        }
        if (mHeartLayout != null) {
            mHeartLayout.addFavor();
        }
        entity.setType(TCConstants.MEMBER_ENTER);
        notifyMsg(entity);
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     * @param text
     */
    public void handleDanmuMsg(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo, String text) {
        handleTextMsg(userInfo, text);
        if (mDanmuMgr != null) {
            //?????????????????????????????????????????????????????????????????????
            mDanmuMgr.addDanmu(userInfo.userAvatar, userInfo.userName, text);
        }
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     * @param text
     */
    public void handleTextMsg(TRTCLiveRoomDef.TRTCLiveUserInfo userInfo, String text) {
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(userInfo.userName);
        entity.setContent(text);
        entity.setType(TCConstants.TEXT_TYPE);
        notifyMsg(entity);
    }

    /**
     * ????????????????????????
     *
     * @param entity
     */
    private void notifyMsg(final TCChatEntity entity) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mArrayListChatEntity.size() > 1000) {
                    while (mArrayListChatEntity.size() > 900) {
                        mArrayListChatEntity.remove(0);
                    }
                }

                mArrayListChatEntity.add(entity);
                mChatMsgListAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * ????????????
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            exitRoom();
            finish();
        } else if (id == R.id.btn_like) {
            if (mHeartLayout != null) {
                mHeartLayout.addFavor();
            }

            //????????????????????????
            if (mLikeFrequencyControl == null) {
                mLikeFrequencyControl = new TCLikeFrequencyControl();
                mLikeFrequencyControl.init(2, 1);
            }
            if (mLikeFrequencyControl.canTrigger()) {
                //???ChatRoom??????????????????
                mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_PRAISE), "", null);
            }
        } else if (id == R.id.btn_message_input) {
            showInputMsgDialog();
        }
    }

    private void showLog() {
        mShowLog = !mShowLog;
        mLiveRoom.showVideoDebugLog(mShowLog);
        if (mVideoViewAnchor != null) {
            mVideoViewAnchor.showLog(mShowLog);
        }
        if (mVideoViewPKAnchor != null) {
            mVideoViewPKAnchor.showLog(mShowLog);
        }

        mVideoViewMgr.showLog(mShowLog);
    }


    /**
     * ??????????????????
     */
    private void showInputMsgDialog() {
        WindowManager              windowManager = getWindowManager();
        Display                    display       = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp            = mInputTextMsgDialog.getWindow().getAttributes();

        lp.width = (display.getWidth()); //????????????
        mInputTextMsgDialog.getWindow().setAttributes(lp);
        mInputTextMsgDialog.setCancelable(true);
        mInputTextMsgDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mInputTextMsgDialog.show();
    }

    /**
     * TextInputDialog????????????
     *
     * @param msg       ????????????
     * @param danmuOpen ??????????????????
     */
    @Override
    public void onTextSend(String msg, boolean danmuOpen) {
        if (msg.length() == 0) {
            return;
        }
        byte[] byte_num = msg.getBytes(StandardCharsets.UTF_8);
        if (byte_num.length > 160) {
            Toast.makeText(this, R.string.trtcliveroom_tips_input_content, Toast.LENGTH_SHORT).show();
            return;
        }

        //????????????
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(getString(R.string.trtcliveroom_me));
        entity.setContent(msg);
        entity.setType(TCConstants.TEXT_TYPE);
        notifyMsg(entity);

        if (danmuOpen) {
            if (mDanmuMgr != null) {
                mDanmuMgr.addDanmu(mSelfAvatar, mSelfNickname, msg);
            }

            mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_DANMU), msg, new TRTCLiveRoomCallback.ActionCallback() {
                @Override
                public void onCallback(int code, String msg) {

                }
            });
        } else {
            mLiveRoom.sendRoomTextMsg(msg, new TRTCLiveRoomCallback.ActionCallback() {
                @Override
                public void onCallback(int code, String msg) {
                    if (code == 0) {
                        ToastUtils.showShort(R.string.trtcliveroom_message_send_success);
                    } else {
                        ToastUtils.showShort(getString(R.string.trtcliveroom_message_send_fail, code, msg));
                    }
                }
            });
        }
    }


    private void showNoticeToast(String text) {
        if (mToastNotice == null) {
            mToastNotice = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        }

        if (mNoticeTimer == null) {
            mNoticeTimer = new Timer();
        }

        mToastNotice.setText(text);
        mNoticeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mToastNotice.show();
            }
        }, 0, 3000);
    }

    private void hideNoticeToast() {
        if (mToastNotice != null) {
            mToastNotice.cancel();
            mToastNotice = null;
        }
        if (mNoticeTimer != null) {
            mNoticeTimer.cancel();
            mNoticeTimer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                joinPusher();
                break;
            default:
                break;
        }
    }
}
