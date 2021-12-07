## 本工程为MM美颜SDK接入第三方直播、视频sdk demo
> ### 接入MM美颜sdk需要修改的部分（可以通过全局搜索**TODO mmbeauty**来快速定位）：
> - applicationId ""      // 配置applicationid
> - signingConfigs.config //配置keystore
> - cosmosAppid.          //配置appid


### 1.MMAgoraDemoDroid 为接入声网1v1视频sdk demo
### 2.MMAgoraLiveDemo 为接入声网直播sdk demo
> MMAgoraLiveDemo除需要配置以上三个部分之外，还需要在string_configs.xml中配置在声网后台申请的agora_app_id以及agora_access_token
### 3.MMZegoExpress 为接入zego自定义视频前处理 demo
> MMZegoExpress除需要配置以上三个部分之外，还需要在GetAppIDConfig中配置在即构后台申请的appID以及appSign
### 4.MMQiniu 为接入七牛直播sdk demo
### 5.MMQinniuShortVideoUIDemo 为接入七牛短视频sdk demo
### 6.MMQiniuRTC_v3.x 为接入七牛RTC sdk demo
### 7.MMTencent 为接入腾讯直播sdk demo
> MMTencent除需要配置以上三个部分之外，还需要在TxApplication中配置在腾讯直播后台申请的licenceUrl以及licenseKey
### 8.MMTencentTRTCSDK为接入腾讯TRTC sdk demo
> MMTencentTRTCSDK除需要配置以上三部分之外，还需要在GenerateTestUserSig中配置在腾讯后台申请的SDKAPPID以及SECRETKEY
### 9.MMTencentLVBSDK为接入腾讯LVB sdk demo
### 10.MMNetease为接入云信互动直播 sdk demo

