
package com.lht.pay;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import java.util.Map;

public class RNLhtPayModule extends ReactContextBaseJavaModule {

  public static String WX_APPID = "";
  public static String universalLinks = "";

  private final ReactApplicationContext reactContext;

  public RNLhtPayModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNLhtPay";
  }

  @ReactMethod
  public  void WXIstall(String install, final Callback promise){

  }


  @ReactMethod
  public void setAlipaySandbox(Boolean isSandbox) {
    if(isSandbox){
      EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
    }else {
      EnvUtils.setEnv(EnvUtils.EnvEnum.ONLINE);
    }
  }

  @ReactMethod
  public void alipay(final String orderInfo, final Callback promise) {
    Runnable payRunnable = new Runnable() {
      @Override
      public void run() {
        PayTask alipay = new PayTask(getCurrentActivity());
        Map<String, String> result = alipay.payV2(orderInfo, true);
        WritableMap map = Arguments.createMap();
        map.putString("memo", result.get("memo"));
        map.putString("result", result.get("result"));
        map.putString("resultStatus", result.get("resultStatus"));
        promise.invoke(map);
      }
    };
    // 必须异步调用
    Thread payThread = new Thread(payRunnable);
    payThread.start();
  }

  @ReactMethod
  public void setWxId(String id, String link) {
    WX_APPID = id;
    universalLinks = link;
  }

  @ReactMethod
  public void wxPay(ReadableMap params, final Callback callback) {
    IWXAPI api = WXAPIFactory.createWXAPI(getCurrentActivity(), WX_APPID);
    //data  根据服务器返回的json数据创建的实体类对象
    PayReq req = new PayReq();
    req.appId = WX_APPID;
    req.partnerId = params.getString("partnerId");
    req.prepayId = params.getString("prepayId");
    req.packageValue = params.getString("packageValue");
    req.nonceStr = params.getString("nonceStr");
    req.timeStamp = params.getString("timeStamp");
    req.sign = params.getString("sign");
    api.registerApp(WX_APPID);
    XWXPayEntryActivity.callback = new WXPayCallBack() {
      @Override
      public void callBack(WritableMap result) {
        callback.invoke(result);
      }
    };

    //发起请求
    api.sendReq(req);
  }
}