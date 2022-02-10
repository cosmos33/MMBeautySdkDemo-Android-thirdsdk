package com.zego.videofilter.videoFilter;

import android.content.Context;

import com.zego.zegoavkit2.videofilter.ZegoVideoFilter;
import com.zego.zegoavkit2.videofilter.ZegoVideoFilterFactory;

/**
 * Created by robotding on 16/12/3.
 */

public class VideoFilterFactoryDemo extends ZegoVideoFilterFactory {
    private FilterType type = FilterType.FilterType_SurfaceTexture;
    private Context context ;
    private VideoFilterSurfaceTextureDemo mFilter = null;

    public void switchCamera(boolean frontCamra) {
        mFilter.switchCamera(frontCamra);
    }

    // 前处理传递数据的类型枚举
    public enum FilterType {
        FilterType_SurfaceTexture,
    }

    public VideoFilterFactoryDemo(Context context) {
        this.context = context;
    }

    // 创建外部滤镜实例
    public ZegoVideoFilter create() {
//        switch (type) {
//            case FilterType_Mem:
//                mFilter = new VideoFilterMemDemo(mFunRender);
//                break;
//            case FilterType_SurfaceTexture:
                mFilter = new VideoFilterSurfaceTextureDemo(context);
//                break;
//            case FilterType_HybridMem:
//                mFilter = new VideoFilterHybridDemo(context);
//                break;
//            case FilterType_SyncTexture:
//                mFilter = new VideoFilterGlTexture2dDemo(mFunRender);
//                break;
//            case FilterType_ASYNCI420Mem:
//                mFilter = new VideoFilterI420MemDemo(mFunRender);
//                break;
//        }

        return mFilter;
    }

    // 销毁外部滤镜实例
    public void destroy(ZegoVideoFilter vf) {
        mFilter = null;
    }
}
