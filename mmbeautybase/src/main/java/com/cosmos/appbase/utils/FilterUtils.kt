package com.cosmos.appbase.utils

import android.content.Context
import com.cosmos.appbase.listener.OnStickerResourcePrepareListener
import com.mm.mmutil.FileUtil
import com.mm.mmutil.app.AppContext
import com.mm.mmutil.task.ThreadUtils
import java.io.File

object FilterUtils {
    val MOMENT_FILTER_FILE = "filterData"

    fun getBeautyDirectory(): File? {
        return File(
            AppContext.getContext().filesDir?.absolutePath,
            "/beauty"
        )
    }

    fun getFilterHomeDir(): File {
        var dir = File(
            getBeautyDirectory(),
            MOMENT_FILTER_FILE
        );
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir;
    }

    fun prepareStikcerResource(
        context: Context?,
        onStickerResourcePrepareListener: OnStickerResourcePrepareListener
    ) {
        ThreadUtils.execute(
            ThreadUtils.TYPE_RIGHT_NOW
        ) {
            val filterDir = context?.filesDir?.absolutePath + "/facemasksource"
            if (!File(filterDir).exists()) {
                File(filterDir).mkdirs()
                var file = File(filterDir, "facemask.zip")
                file.createNewFile()
                FileUtil.copyAssets(context, "facemask.zip", file)
                FileUtil.unzip(
                    File(filterDir, "facemask.zip").absolutePath,
                    filterDir,
                    false
                )
            }
            onStickerResourcePrepareListener.onStickerReady(filterDir)
        }

    }
}