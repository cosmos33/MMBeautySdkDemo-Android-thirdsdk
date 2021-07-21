package im.zego.expressample.mmbeauty.demo;

public class GetAppIDConfig {
    /** Please fill appID and appSign */

    /**
     * Please register from ZEGO management console, https://console-express.zego.im/acount/register
     * The format is 123456789L
     * for example:
     *   public static final long appID = 123456789L;
     */
    public static final long appID = 1476983321; // TODO mmbeauty 配置zego后台申请的appid

    /**
     * 64 characters, Please register from ZEGO management console, https://console-express.zego.im/acount/register
     * The format is "0123456789012345678901234567890123456789012345678901234567890123"
     * for example:
     *   public static final String appSign = "0123456789012345678901234567890123456789012345678901234567890123;
     */
    public static final String appSign = "cb6b90e69ca86d0691b6ed1ce111d17cda19db7c9ed1920cead0b0ad36514fe3";// TODO mmbeauty 配置zego后台申请的appSign
}
