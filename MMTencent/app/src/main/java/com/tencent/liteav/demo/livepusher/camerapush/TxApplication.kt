package com.tencent.liteav.demo.livepusher.camerapush

import android.app.Application
import android.content.Context
import com.tencent.rtmp.TXLiveBase

class TxApplication : Application() {
    var licenceUrl =
        "http://license.vod2.myqcloud.com/license/v1/01d65cf7f3dd34339328c7219f7a5517/TXLiveSDK.licence" // TODO mmbeauty这里需要修改为腾讯云后台申请LicenseUrl
    var licenseKey = "ddbbe1446d9fb08dfe993081fda61760" // TODO mmbeauty 这里需要修改为腾讯云后台申请的key

    override fun onCreate() {
        super.onCreate()
        TXLiveBase.setConsoleEnabled(true)
        TXLiveBase.getInstance()
            .setLicence(this, licenceUrl, licenseKey)
    }
}