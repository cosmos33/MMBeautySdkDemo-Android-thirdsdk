package com.cosmos.appbase.utils

import android.content.Context
import com.cosmos.appbase.listener.OnResPrepareListener
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
            onResPrepareListener: OnResPrepareListener
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
            onResPrepareListener.onResReady(filterDir)
        }

    }

    fun prepareModelsResource(
            context: Context?,
            onResPrepareListener: OnResPrepareListener
    ) {
        ThreadUtils.execute(
                ThreadUtils.TYPE_RIGHT_NOW
        ) {
            val filterDir = context?.filesDir?.absolutePath + "/models"
            if (!File(filterDir).exists()) {
                File(filterDir).mkdirs()
                var file = File(filterDir, "model-all.zip")
                file.createNewFile()
                FileUtil.copyAssets(context, "model-all.zip", file)
                FileUtil.unzip(
                        File(filterDir, "model-all.zip").absolutePath,
                        filterDir,
                        false
                )
            }
            onResPrepareListener.onResReady(filterDir)
        }

    }
}