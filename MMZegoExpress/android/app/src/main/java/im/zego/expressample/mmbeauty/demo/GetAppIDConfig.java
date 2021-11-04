package im.zego.expressample.mmbeauty.demo;

public class GetAppIDConfig {
    /** Please fill appID and appSign */

    /**
     * Please register from ZEGO management console, https://console-express.zego.im/acount/register
     * The format is 123456789L
     * for example:
     *   public static final long appID = 123456789L;
     */
    public static final long appID = 2514125864L; // TODO mmbeauty 配置zego后台申请的appid

    /**
     * 64 characters, Please register from ZEGO management console, https://console-express.zego.im/acount/register
     * The format is "0123456789012345678901234567890123456789012345678901234567890123"
     * for example:
     *   public static final String appSign = "0123456789012345678901234567890123456789012345678901234567890123;
     */
    public static final String appSign = "4142fbee882fbd642e6d22fe5f97ef9a9ffd324574ccd752eccaa098cf38f879";// TODO mmbeauty 配置zego后台申请的appSign
}
