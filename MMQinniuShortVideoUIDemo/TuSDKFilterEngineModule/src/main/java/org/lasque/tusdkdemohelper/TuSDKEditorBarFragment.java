///**
// * TuSDK
// * <p>
// * TuSDKEditorBarFragment.java
// *
// * @author H.ys
// * @Date 2019/4/30 15:38
// * @Copyright (c) 2019 tusdk.com. All rights reserved.
// */
//package org.lasque.tusdkdemohelper;
//
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//
//import androidx.annotation.Nullable;
//import androidx.annotation.UiThread;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.viewpager.widget.ViewPager;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.lasque.tusdk.api.engine.TuSdkFilterEngine;
//import org.lasque.tusdk.api.video.preproc.filter.TuSDKVideoProcesser;
//import org.lasque.tusdk.core.TuSdkContext;
//import org.lasque.tusdk.core.activity.TuSdkFragment;
//import org.lasque.tusdk.core.seles.SelesParameters;
//import org.lasque.tusdk.core.utils.TLog;
//import org.lasque.tusdk.core.utils.ThreadHelper;
//import org.lasque.tusdk.core.utils.json.JsonHelper;
//import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
//import org.lasque.tusdk.video.editor.TuSdkMediaComicEffectData;
//import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;
//import org.lasque.tusdk.video.editor.TuSdkMediaFilterEffectData;
//import org.lasque.tusdk.video.editor.TuSdkMediaPlasticFaceEffect;
//import org.lasque.tusdk.video.editor.TuSdkMediaSkinFaceEffect;
//import org.lasque.tusdk.video.editor.TuSdkMediaStickerEffectData;
//import org.lasque.tusdkdemohelper.tusdk.BeautyPlasticRecyclerAdapter;
//import org.lasque.tusdkdemohelper.tusdk.BeautyRecyclerAdapter;
//import org.lasque.tusdkdemohelper.tusdk.FilterRecyclerAdapter;
//import org.lasque.tusdkdemohelper.tusdk.MonsterFaceFragment;
//import org.lasque.tusdkdemohelper.tusdk.StickerFragment;
//import org.lasque.tusdkdemohelper.tusdk.StickerGroupCategories;
//import org.lasque.tusdkdemohelper.tusdk.TabPagerIndicator;
//import org.lasque.tusdkdemohelper.tusdk.TabViewPagerAdapter;
//import org.lasque.tusdkdemohelper.tusdk.filter.FilterConfigSeekbar;
//import org.lasque.tusdkdemohelper.tusdk.filter.FilterConfigView;
//import org.lasque.tusdkdemohelper.tusdk.model.PropsItemMonster;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//
//import static org.lasque.tusdk.video.editor.TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeFilter;
//import static org.lasque.tusdk.video.editor.TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypePlasticFace;
//import static org.lasque.tusdk.video.editor.TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeSkinFace;
//
///**
// * ?????????????????????
// */
//public class TuSDKEditorBarFragment extends TuSdkFragment {
//
//    public static TuSDKEditorBarFragment newInstance(String[] mFilterGroup, String[] mCartoonFilterGroup,boolean hasMonsterFace) {
//        TuSDKEditorBarFragment fragment = new TuSDKEditorBarFragment();
//        Bundle bundle = new Bundle();
//        bundle.putStringArray("FilterGroup", mFilterGroup);
//        bundle.putStringArray("CartoonFilterGroup", mCartoonFilterGroup);
//        bundle.putBoolean("hasMonsterFace",hasMonsterFace);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//    public static TuSDKEditorBarFragment newInstance(String[] mFilterGroup, String[] mCartoonFilterGroup){
//        return newInstance(mFilterGroup,mCartoonFilterGroup,false);
//    }
//
//    private String[] mFilterGroup;
//
//    private String[] mCartoonFilterGroup;
//
//    private boolean mHasMonsterFace = false;
//
//    public void setFilterEngine(TuSdkFilterEngine filterEngine) {
//        this.mFilterEngine = filterEngine;
//
//        ThreadHelper.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                changeFilter(0);
//            }
//        }, 500);
//    }
//
//    // TuSDK Filter Engine
//    private TuSdkFilterEngine mFilterEngine;
//
//    // ??????????????????
//    private FilterConfigView mFilterConfigView;
//
//    // ???????????????
//    private RecyclerView mFilterRecyclerView;
//
//    // ????????????Adapter
//    private FilterRecyclerAdapter mFilterAdapter;
//
//    // ???????????????
//    private View mFilterBottomView;
//
//    // ????????????
//    private RelativeLayout mStickerLayout;
//    // ????????????
//    private ImageView mStickerCancel;
//    // ????????????pager???
//    private ViewPager mViewPager;
//    // ??????Tab
//    private TabPagerIndicator mTabPagerIndicator;
//    // ??????Tab?????????
//    private TabViewPagerAdapter mStickerPagerAdapter;
//    // ????????????
//    private List<StickerGroupCategories> mStickerGroupCategoriesList;
//
//    // ??????????????????
//    private RecyclerView mCartoonRecyclerView;
//
//    // ????????????
//    private View mCartoonLayout;
//
//    // ????????????Adapter
//    private FilterRecyclerAdapter mCartoonAdapter;
//
//    //???????????????
//    private View mBeautyPlasticBottomView;
//    //???????????????
//    private RecyclerView mBeautyPlasticRecyclerView;
//    // ??????????????????
//    private FilterConfigView mBeautyConfigView;
//
//    private View.OnClickListener mCartoonButtonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            mCartoonLayout.setVisibility(mCartoonLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//            mFilterBottomView.setVisibility(View.GONE);
//            mBeautyPlasticBottomView.setVisibility(View.GONE);
//            mStickerLayout.setVisibility(View.GONE);
//            mFilterConfigView.setVisibility(View.GONE);
//            mBeautyConfigView.setVisibility(View.GONE);
//        }
//    };
//
//    private View.OnClickListener mBeautyPlasticButtonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            mBeautyPlasticRecyclerView.setAdapter(mBeautyPlasticRecyclerAdapter);
//            mBeautyPlasticBottomView.setVisibility(mBeautyPlasticBottomView.getVisibility() == View.VISIBLE
//                    ? View.GONE : View.VISIBLE);
//            mCartoonLayout.setVisibility(View.GONE);
//            mFilterBottomView.setVisibility(View.GONE);
//            mStickerLayout.setVisibility(View.GONE);
//            mFilterConfigView.setVisibility(View.GONE);
//            mBeautyConfigView.setVisibility(View.GONE);
//        }
//    };
//
//    private View.OnClickListener mFilterButtonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            // ?????? ???????????????
//            mFilterBottomView.setVisibility(mFilterBottomView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//            // ?????????????????????
//            mBeautyPlasticBottomView.setVisibility(View.GONE);
//            // ?????????????????????
//            mStickerLayout.setVisibility(View.GONE);
//            // ??????????????????
//            mCartoonLayout.setVisibility(View.GONE);
//            // ?????????????????????
//            mFilterConfigView.setVisibility(View.GONE);
//            // ????????????????????????
//            mBeautyConfigView.setVisibility(View.GONE);
//        }
//    };
//
//    private View.OnClickListener mBeautySkinButtonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            mBeautyPlasticRecyclerView.setAdapter(mBeautySkinRecyclerAdapter);
//            mBeautyPlasticBottomView.setVisibility(mBeautyPlasticBottomView.getVisibility() == View.VISIBLE
//                    ? View.GONE : View.VISIBLE);
//            mCartoonLayout.setVisibility(View.GONE);
//            mFilterBottomView.setVisibility(View.GONE);
//            mStickerLayout.setVisibility(View.GONE);
//            mFilterConfigView.setVisibility(View.GONE);
//            mBeautyConfigView.setVisibility(View.GONE);
//        }
//    };
//
//
//    private View.OnClickListener mStickerButtonClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            mStickerLayout.setVisibility((mStickerLayout.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE);
//            mFilterBottomView.setVisibility(View.GONE);
//            mBeautyPlasticBottomView.setVisibility(View.GONE);
//            mCartoonLayout.setVisibility(View.GONE);
//            mFilterConfigView.setVisibility(View.GONE);
//            mBeautyConfigView.setVisibility(View.GONE);
//        }
//    };
//
//
//    /**
//     * ??????????????????  Float ????????????
//     */
//    private HashMap<String, Float> mDefaultBeautyPercentParams = new HashMap<String, Float>() {
//        {
//            put("eyeSize", 0.3f);
//            put("chinSize", 0.2f);
//            put("noseSize", 0.2f);
//            put("mouthWidth", 0.5f);
//            put("archEyebrow", 0.5f);
//            put("jawSize", 0.5f);
//            put("eyeAngle", 0.5f);
//            put("eyeDis", 0.5f);
//        }
//    };
//
//    /**
//     * ???????????????
//     */
//    private List<String> mBeautyPlastics = new ArrayList() {
//        {
//            add("reset");
//            add("eyeSize");
//            add("chinSize");
//            add("noseSize");
//            add("mouthWidth");
//            add("archEyebrow");
//            add("jawSize");
//            add("eyeAngle");
//            add("eyeDis");
//        }
//    };
//
//    /**
//     * ??????????????????
//     */
//    private BeautyPlasticRecyclerAdapter mBeautyPlasticRecyclerAdapter;
//
//    /**
//     * ???????????????
//     */
//    private BeautyRecyclerAdapter mBeautySkinRecyclerAdapter;
//
//    public static int getLayoutId() {
//        return TuSdkContext.getLayoutResId("tusdk_parent_wrap_layout");
//    }
//
//    private View mParentView;
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        this.setRootViewLayoutId(getLayoutId());
//        mFilterGroup = getArguments().getStringArray("FilterGroup");
//        mCartoonFilterGroup = getArguments().getStringArray("CartoonFilterGroup");
//        mHasMonsterFace = getArguments().getBoolean("hasMonsterFace",false);
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
//
//    @Override
//    protected void initCreateView() {
//
//    }
//
//    @Override
//    protected void loadView(ViewGroup viewGroup) {
//        loadView();
//    }
//
//    @Override
//    protected void viewDidLoad(ViewGroup viewGroup) {
//
//    }
//
//    private void loadView() {
//        initTuSDKViews();
//    }
//
//    private void initTuSDKViews() {
//        // ???????????????
//        mBeautyPlasticBottomView = this.getViewById("lsq_beauty_bottom_view");
//        // ???????????????
//        mBeautyPlasticRecyclerView = this.getViewById("lsq_beauty_recyclerView");
//        // ??????????????????
//        mBeautyConfigView = this.getViewById("lsq_beauty_config_view");
//        if (mBeautyConfigView != null)
//            mBeautyConfigView.setIgnoredKeys(new String[]{});
//
//
//        // ???????????????
//        mFilterConfigView = this.getViewById("lsq_filter_config_view");
//        // ????????????
//        mFilterRecyclerView = this.getViewById("lsq_filter_list_view");
//        // ????????????
//        mFilterBottomView = this.getViewById("lsq_filter_group_bottom_view_wrap");
//        mFilterConfigView.setSeekBarDelegate(mFilterConfigViewSeekBarDelegate);
//
//
//        // ????????????
//        mStickerLayout = this.getViewById("lsq_record_sticker_layout");
//        // ??????Pager???
//        mViewPager = this.getViewById("lsq_viewPager");
//        // ??????Tab
//        mTabPagerIndicator = this.getViewById("lsq_TabIndicator");
//        // ??????????????????
//        mStickerCancel = this.getViewById("lsq_cancel_button");
//
//
//        // ??????????????????
//        mCartoonLayout = this.getViewById("lsq_cartoon_view");
//        // ??????????????????
//        mCartoonRecyclerView = this.getViewById("lsq_cartoon_recycler_view");
//
//        // ??????????????????
//        prepareStickerViews();
//
//        // ??????????????????
//        prepareFilterViews();
//
//        // ??????????????????
//        prepareBeautySkinViews();
//
//        // ?????????????????????
//        prepareBeautyPlasticViews();
//
//        // ??????????????????
//        prepareCartoonViews();
//    }
//
//    /**
//     * ????????????????????????
//     */
//    private void prepareCartoonViews() {
//        mCartoonRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        mCartoonAdapter = new FilterRecyclerAdapter();
//        mCartoonRecyclerView.setAdapter(mCartoonAdapter);
//        mCartoonAdapter.isShowImageParameter(false);
//        mCartoonAdapter.setFilterList(Arrays.asList(mCartoonFilterGroup));
//
//        mCartoonAdapter.setItemCilckListener(new FilterRecyclerAdapter.ItemClickListener() {
//
//            @Override
//            public void onItemClick(int position) {
//
//                // ???????????????????????????
//                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypeFilter);
//                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeComic);
//
//                TuSdkMediaComicEffectData effectData = new TuSdkMediaComicEffectData(mCartoonAdapter.getFilterList().get(position));
//                mFilterEngine.addMediaEffectData(effectData);
//
//            }
//        });
//
//    }
//
//    /********************** ????????? ***********************
//
//     /**
//     * ????????????????????????
//     */
//    @UiThread
//    public void prepareBeautyPlasticViews() {
//        mBeautyPlasticRecyclerAdapter = new BeautyPlasticRecyclerAdapter(getContext(), mBeautyPlastics);
//        mBeautyPlasticRecyclerAdapter.setOnBeautyPlasticItemClickListener(beautyPlasticItemClickListener);
//
//        // ??????Bar
//        mBeautyConfigView.setSeekBarDelegate(mBeautyConfigDelegate);
//        mBeautyConfigView.setVisibility(View.GONE);
//        mBeautyPlasticRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//    }
//
//    /**
//     * ?????????Item????????????
//     */
//    BeautyPlasticRecyclerAdapter.OnBeautyPlasticItemClickListener beautyPlasticItemClickListener = new BeautyPlasticRecyclerAdapter.OnBeautyPlasticItemClickListener() {
//        @Override
//        public void onItemClick(View v, int position) {
//            mBeautyConfigView.setVisibility(View.VISIBLE);
//            switchBeautyPlasticConfig(position);
//        }
//
//        @Override
//        public void onClear() {
//
//            mBeautyConfigView.setVisibility(View.GONE);
//
//            android.app.AlertDialog.Builder adBuilder = new android.app.AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
//            adBuilder.setTitle("???????????????");
//            adBuilder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            adBuilder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    clearBeautyPlastic();
//                    dialog.dismiss();
//                }
//            });
//            adBuilder.show();
//        }
//    };
//
//    /**
//     * ??????????????????
//     *
//     * @param position
//     */
//    private void switchBeautyPlasticConfig(int position) {
//        if (mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace).size() == 0) {
//
//            // ?????????????????????????????????
//            TuSdkMediaPlasticFaceEffect plasticFaceEffect = new TuSdkMediaPlasticFaceEffect();
//            mFilterEngine.addMediaEffectData(plasticFaceEffect);
//            for (SelesParameters.FilterArg arg : plasticFaceEffect.getFilterArgs()) {
//                if (arg.equalsKey("eyeSize")) {// ??????
//                    arg.setMaxValueFactor(0.85f);
//                }
//                if (arg.equalsKey("chinSize")) {// ??????
//                    arg.setMaxValueFactor(0.8f);
//                }
//                if (arg.equalsKey("noseSize")) {// ??????
//                    arg.setMaxValueFactor(0.6f);
//                }
//
//            }
//            for (String key : mDefaultBeautyPercentParams.keySet()) {
//                TLog.e("key -- %s", mDefaultBeautyPercentParams.get(key));
//                submitPlasticFaceParamter(key, mDefaultBeautyPercentParams.get(key));
//            }
//        }
//        TuSdkMediaEffectData effectData = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace).get(0);
//        SelesParameters.FilterArg filterArg = effectData.getFilterArg(mBeautyPlastics.get(position));
//
//        TLog.e("filterArg -- %s", filterArg.getPrecentValue());
//
//        mBeautyConfigView.setFilterArgs(null, Arrays.asList(filterArg));
//        mBeautyConfigView.setVisibility(View.VISIBLE);
//
//    }
//
//    /**
//     * ???????????????
//     */
//    private void clearBeautyPlastic() {
//
//        mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace);
//    }
//
//    /**
//     * ??????
//     */
//    @UiThread
//    private void prepareBeautySkinViews() {
//        mBeautySkinRecyclerAdapter = new BeautyRecyclerAdapter(getContext());
//        mBeautySkinRecyclerAdapter.setOnSkinItemClickListener(mOnBeautyItemClickListener);
//
//        // ??????Bar
//        mBeautyConfigView.setSeekBarDelegate(mBeautyConfigDelegate);
//        mBeautyConfigView.setVisibility(View.GONE);
//        mBeautyPlasticRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//    }
//
//    /**
//     * ??????????????????
//     */
//    private FilterConfigView.FilterConfigViewSeekBarDelegate mBeautyConfigDelegate = new FilterConfigView.FilterConfigViewSeekBarDelegate() {
//
//        @Override
//        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {
//            submitPlasticFaceParamter(arg.getKey(), seekbar.getSeekbar().getProgress());
//        }
//    };
//
//    /**
//     * ???????????????
//     *
//     * @param key
//     * @param progress
//     */
//    private void submitPlasticFaceParamter(String key, float progress) {
//        List<TuSdkMediaEffectData> filterEffects = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace);
//
//        if (filterEffects.size() == 0) return;
//
//        // ??????????????????????????????
//        TuSdkMediaPlasticFaceEffect filterEffect = (TuSdkMediaPlasticFaceEffect) filterEffects.get(0);
//        filterEffect.submitParameter(key, progress);
//    }
//
//    BeautyRecyclerAdapter.OnBeautyItemClickListener mOnBeautyItemClickListener = new BeautyRecyclerAdapter.OnBeautyItemClickListener() {
//        @Override
//        public void onChangeSkin(View v, String key, boolean useSkinNatural) {
//            mBeautyConfigView.setVisibility(View.VISIBLE);
//            switchConfigSkin(useSkinNatural);
//
//            // ??????key????????????????????????
//            TuSdkMediaEffectData effectData = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace).get(0);
//            SelesParameters.FilterArg filterArg = effectData.getFilterArg(key);
//            mBeautyConfigView.setFilterArgs(effectData, Arrays.asList(filterArg));
//        }
//
//        @Override
//        public void onClear() {
//            mBeautyConfigView.setVisibility(View.GONE);
//            mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace);
//        }
//    };
//
//    /**
//     * ????????????????????????
//     *
//     * @param useSkinNatural true ??????(??????)?????? false ????????????
//     */
//    private void switchConfigSkin(boolean useSkinNatural) {
//        TuSdkMediaSkinFaceEffect skinFaceEffect = new TuSdkMediaSkinFaceEffect(useSkinNatural);
//
//        // ??????
//        SelesParameters.FilterArg whiteningArgs = skinFaceEffect.getFilterArg("whitening");
//        whiteningArgs.setMaxValueFactor(0.6f);//?????????????????????
//        // ??????
//        SelesParameters.FilterArg smoothingArgs = skinFaceEffect.getFilterArg("smoothing");
//        smoothingArgs.setMaxValueFactor(0.7f);//?????????????????????
//
//        if (mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace).size() == 0) {
//
//            whiteningArgs.setPrecentValue(0.3f);//??????????????????
//
//            smoothingArgs.setPrecentValue(0.6f);//??????????????????
//            mFilterEngine.addMediaEffectData(skinFaceEffect);
//        } else {
//            TuSdkMediaSkinFaceEffect oldSkinFaceEffect = (TuSdkMediaSkinFaceEffect) mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace).get(0);
//            mFilterEngine.addMediaEffectData(skinFaceEffect);
//
//            for (SelesParameters.FilterArg filterArg : oldSkinFaceEffect.getFilterArgs()) {
//                SelesParameters.FilterArg arg = skinFaceEffect.getFilterArg(filterArg.getKey());
//                arg.setPrecentValue(filterArg.getPrecentValue());
//            }
//
//            skinFaceEffect.submitParameters();
//        }
//
//    }
//
//    /**
//     * ?????????????????????
//     */
//    private void prepareFilterViews() {
//
//        // ????????????????????????
//        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        // ????????????????????????
//        mFilterAdapter = new FilterRecyclerAdapter();
//        // ?????????????????????
//        mFilterRecyclerView.setAdapter(mFilterAdapter);
//        // ??????????????????
//        mFilterAdapter.setFilterList(Arrays.asList(mFilterGroup));
//        // ?????????????????????
//        mFilterAdapter.setItemCilckListener(new FilterRecyclerAdapter.ItemClickListener() {
//
//            @Override
//            public void onItemClick(int position) {
//                changeFilter(position);
//            }
//        });
//
//    }
//
//    /**
//     * ????????????
//     */
//    public void changeFilter(int postion) {
//
//        if (mFilterEngine == null || mFilterAdapter == null) return;
//
//        mFilterAdapter.setCurrentPosition(postion);
//
//        // ???????????????????????????
//        mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypeFilter);
//        mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeComic);
//
//        TuSdkMediaFilterEffectData effectData = new TuSdkMediaFilterEffectData(mFilterAdapter.getFilterList().get(postion));
//        mFilterEngine.addMediaEffectData(effectData);
//    }
//
//    // ????????????view
//    private void prepareStickerViews() {
//        mStickerCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // ????????????
//                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdKMediaEffectDataTypeSticker);
//                TabViewPagerAdapter.mStickerGroupId = 0;
//                mViewPager.getAdapter().notifyDataSetChanged();
//                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeMonsterFace);
//            }
//        });
//
//        TabViewPagerAdapter.mStickerGroupId = 0;
//        List<Fragment> fragments = new ArrayList<>();
//        mStickerGroupCategoriesList = getRawStickGroupList();
//        List<String> tabTitles = new ArrayList<>();
//        for (StickerGroupCategories categories : mStickerGroupCategoriesList) {
//            StickerFragment stickerFragment = StickerFragment.newInstance(categories);
//            stickerFragment.setOnStickerItemClickListener(onStickerItemClickListener);
//            fragments.add(stickerFragment);
//            tabTitles.add(categories.getCategoryName());
//        }
//        //??????????????????,????????????????????????????????????,?????????????????????
//        if (mHasMonsterFace){
//            MonsterFaceFragment monsterFaceFragment = MonsterFaceFragment.newInstance();
//            monsterFaceFragment.setOnStickerItemClickListener(onMonsterItemClickListener);
//            fragments.add(monsterFaceFragment);
//            tabTitles.add("?????????");
//        }
//        mStickerPagerAdapter = new TabViewPagerAdapter(getFragmentManager(),fragments);
//        mViewPager.setAdapter(mStickerPagerAdapter);
//        mTabPagerIndicator.setViewPager(mViewPager,0);
//        mTabPagerIndicator.setDefaultVisibleCounts(tabTitles.size());
//        mTabPagerIndicator.setTabItems(tabTitles);
//    }
//
//    /**
//     * ????????????
//     *
//     * @return
//     */
//    private List<StickerGroupCategories> getRawStickGroupList() {
//        List<StickerGroupCategories> list = new ArrayList<StickerGroupCategories>();
//        try {
//            InputStream stream = getResources().openRawResource(TuSdkContext.getRawResId("customstickercategories"));
//
//            if (stream == null) return null;
//
//            byte buffer[] = new byte[stream.available()];
//            stream.read(buffer);
//            String json = new String(buffer, "UTF-8");
//
//            JSONObject jsonObject = JsonHelper.json(json);
//            JSONArray jsonArray = jsonObject.getJSONArray("categories");
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject item = jsonArray.getJSONObject(i);
//                StickerGroupCategories categories = new StickerGroupCategories();
//                categories.setCategoryName(item.getString("categoryName"));
//                List<StickerGroup> groupList = new ArrayList<StickerGroup>();
//                JSONArray jsonArrayGroup = item.getJSONArray("stickers");
//                for (int j = 0; j < jsonArrayGroup.length(); j++) {
//                    JSONObject itemGroup = jsonArrayGroup.getJSONObject(j);
//                    StickerGroup group = new StickerGroup();
//                    group.groupId = itemGroup.optLong("id");
//                    group.previewName = itemGroup.optString("previewImage");
//                    group.name = itemGroup.optString("name");
//                    groupList.add(group);
//                }
//                categories.setStickerGroupList(groupList);
//                list.add(categories);
//            }
//            return list;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * ??????????????????
//     */
//    private StickerFragment.OnStickerItemClickListener onStickerItemClickListener = new StickerFragment.OnStickerItemClickListener() {
//        @Override
//        public void onStickerItemClick(StickerGroup itemData) {
//
//            mFilterEngine.removeAllLiveSticker();
//
//            if (itemData != null) {
//                TuSdkMediaStickerEffectData stickerEffectData = new TuSdkMediaStickerEffectData(itemData);
//                mFilterEngine.addMediaEffectData(stickerEffectData);
//            }
//        }
//    };
//
//    /**
//     * ?????????????????????
//     */
//    private MonsterFaceFragment.OnMonsterItemClickListener onMonsterItemClickListener = new MonsterFaceFragment.OnMonsterItemClickListener() {
//        @Override
//        public void onMonsterItemClick(PropsItemMonster itemData) {
//
//            if (itemData!=null){
//                mFilterEngine.addMediaEffectData(itemData.effect());
//            }
//        }
//    };
//
//    /**
//     * ??????????????????
//     */
//    private FilterConfigView.FilterConfigViewSeekBarDelegate mFilterConfigViewSeekBarDelegate = new FilterConfigView.FilterConfigViewSeekBarDelegate() {
//        @Override
//        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {
//
//            List<TuSdkMediaEffectData> filterEffects = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeFilter);
//
//            float progress = seekbar.getSeekbar().getProgress();
//            if (arg.getKey().equals("whitening")) {
//                progress = progress * 0.6f;
//            } else if (arg.equalsKey("mixied") || arg.equalsKey("smoothing")) {
//                progress = progress * 0.7f;
//            }
//
//            // ??????????????????????????????
//            TuSdkMediaFilterEffectData filterEffect = (TuSdkMediaFilterEffectData) filterEffects.get(0);
//            filterEffect.submitParameter(arg.getKey(), progress);
//        }
//    };
//
//    public View.OnClickListener getCartoonButtonClick() {
//        return mCartoonButtonClick;
//    }
//
//    public View.OnClickListener getBeautyPlasticButtonClick() {
//        return mBeautyPlasticButtonClick;
//    }
//
//    public View.OnClickListener getFilterButtonClick() {
//        return mFilterButtonClick;
//    }
//
//    public View.OnClickListener getBeautySkinButtonClick() {
//        return mBeautySkinButtonClick;
//    }
//
//    public View.OnClickListener getStickerButtonClick() {
//        return mStickerButtonClick;
//    }
//
//    public TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate getMediaEffectDelegate() {
//        return mMediaEffectDelegate;
//    }
//
//    /**
//     * ??????????????????
//     */
//    private TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate mMediaEffectDelegate = new TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate() {
//
//        /**
//         * ????????????????????????
//         * @param mediaEffectData
//         */
//        @Override
//        public void didApplyingMediaEffect(final TuSdkMediaEffectData mediaEffectData) {
//
//            ThreadHelper.post(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    switch (mediaEffectData.getMediaEffectType())
//                    {
//
//                        case TuSdkMediaEffectDataTypeFilter: {
//
//                            // ???????????????????????????????????????
//                            mFilterConfigView.setFilterArgs(mediaEffectData, mediaEffectData.getFilterArgs());
//                            mFilterConfigView.setVisibility(View.VISIBLE);
//
//                        }
//                        break;
//
//                        default:
//                            break;
//                    }
//
//                }
//            });
//
//        }
//
//        @Override
//        public void didRemoveMediaEffect(List<TuSdkMediaEffectData> list) {
//
//        }
//    };
//}
