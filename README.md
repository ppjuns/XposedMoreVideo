# 多多短视频数据抓取


> 全网原创多多短视频首页视频数据抓取
>
> 平台：Android
>
> 版本：1.0.0
>
> 包名：com.guzhen.video.morevideo

## 探索
安装进入应用首页，使用 adb shell dumpsys activity activities | grep mResume 命令获取当前活动窗口，得知是一个 MainActivity。我们可以使用 jadx 打开 apk 查看源码。发现是一个 MainActivity 显示一个 WebViewContainerFragment，得知底部三个 tab 都属于这个 webview 的。

### 思路一，这里我们尝试获取webview加载都url。
使用 xposed 来 hook 对应 fragment 所在 webview，叫 DWebView 的 loadUrl 方法。
我们可以得到一个url
[https://hh.yingzhongshare.com/hh-frontend/video/home](https://hh.yingzhongshare.com/hh-frontend/video/home)，直接用浏览器打开会显示网络不给力的错误状态，和我们app打开的效果不一样。我们只能尝试另外一种思路了。

### 思路二，使用httpcanary抓包
使用httpcanary，我们可以抓到网页请求的http链接。从众多请求中过滤出返回json格式的，其中找到了一个可疑的请求[/hh-video-service/api/video/nextGroup](/hh-video-service/api/video/nextGroup)
它的返回值是已经加密过的
```json
{
	"costTime": 18,
	"result": {
		"status": 1,
		"errorcode": 0,
		"msg": "处理成功"
	},
	"data": {
		"examNum": 20,
		"encryptedData": "nVtwa7jT4C+O9iB5UQKbESUhuJF+DNBJ83==",
		"currTime": 1640744136940
	}
}
```
看encryptedData的值感觉是编码过的，像是base64编码后的子，我们尝试在在线的base64编码网站进行解码发现是行不通的。我们需要找到对应的js文件查看解码的逻辑。回到httpcanary抓包工具里面，我们过滤一下相应是application/javascript的mine-type的请求，发现了一个js文件[https://hh.yingzhongshare.com/hh-frontend/_next/static/chunks/pages/video/home-73535424cc4fedac917b.js](https://hh.yingzhongshare.com/hh-frontend/_next/static/chunks/pages/video/home-73535424cc4fedac917b.js)，从他的路径不难发现他和视频有关系，我们直接打开，然后尝试用请求的url搜索，发现了关键信息
```js
case 3:
    return Be = !0,
    We || (We = JSON.parse(R.a.getItem("recentVideoIds", "[]"))),
    t.next = 7,
    Object(C.a)({
        url: "/hh-video-service/api/video/nextGroup",
        aes: !0,
        data: {
            newly: e && this.isNewly(),
            recentVideoIds: We
        }
    });
```
我们可以猜测这是视频请求的地方。接着我们继续以encryptedData为关键词进行搜索
```js
function f(t, e, a) {
    if (e && t.aes && window._dsbridge && a) {
        var i = e.currTime,
        n = e.encryptedData,
        s = u.a.call("dt", {
            content: n,
            rn: i
        });
        if (s) try {
            return JSON.parse(s)
        } catch(o) {
            console.error("\u89e3\u6790\u6570\u636e\u5f02\u5e38")
        }
    }
```
发现了一个解密的地方，如果使用了aes和_dsbridge，就先解密得到s，在解析json格式的字符串s。从请求的代码aes: !0和返回的结果，可知是使用了aes加密的。现在我们只要找到u.a.call调用就可以解码拿到明文了。

尝试一，用call和dt作为关键字来搜索没找到对应js的function。（还去其他的js文件搜索了也没有发现）

尝试二，既然请求时使用了aes加密，那结果也有可能用aes来解密，从代码中看到传入了结果返回的时间戳，难道是时间戳就是aes的密钥，我们再次打开aes在线加解密网站，发现还是无法解密。

尝试三，回去jadx查找aes或者dt方法，我们找到在 BaseWebInterface 类里面的dt方法，因为该方法和js交互没有混淆。
```java
    @JavascriptInterface
    public String dt(JSONObject jSONObject) {
        akm d;
        String optString = jSONObject.optString("content");
        String optString2 = jSONObject.optString("rn");
        if (!TextUtils.isEmpty(optString) && !TextUtils.isEmpty(optString2) && (d = alt.a().b().d()) != null && !TextUtils.isEmpty(d.a)) {
            try {
                return com.xmiles.base.utils.a.b(optString, d.a, optString2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
```
其中的content和rn对应着js里面的密文和时间戳。现在只要我们知道d.a是啥，再经过 com.xmiles.base.utils.a.b 方法就可以解密密文了。根据alt.a().b().d()，从源码中我们可以知道这是获取userid的，而util.a.b方法
```java
public static String b(String str, String str2, String str3) throws Exception {
        String a = a(str3);
        SecretKeySpec secretKeySpec = new SecretKeySpec(a(str2).getBytes(), "AES");
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding ");
        instance.init(2, secretKeySpec, new IvParameterSpec(a.getBytes()));
        return new String(instance.doFinal(Base64.decode(str, 0)), StandardCharsets.UTF_8);
    }

    private static String a(String str) {
        int length = str.length() - 16;
        if (length >= 0) {
            return length > 0 ? str.substring(0, 16) : str;
        }
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < (-length); i++) {
            sb.append("0");
        }
        sb.append(str);
        return sb.toString();
    }
```
上面的思路是格式化时间戳，和userid，用格式后userid作为密钥对base64编码后的密文进行aes解密，最后返回json格式的明文。

我们可以hook BaseWebInterface类的 dt 方法，获取返回的 json 字符串根据里面examId去重后获取视频和题目数据。
```kotlin
  CnClazz.BaseWebInterface.decode.setHook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                Log.d(TAG, param.args[0].toString())
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                     val result = param.result.toString()
                Log.d(TAG, result)
                if (TextUtils.isEmpty(result)) {
                    return
                }
                val gson = Gson()
                val videoExam = try {
                    gson.fromJson<VideoExam>(result, VideoExam::class.java)
                } catch (e: Exception) {
                    null
                }
                if (videoExam?.videoExamVos != null && videoExam.videoExamVos!!.isNotEmpty()) {
                    videoExam.videoExamVos?.forEach {
                        if (!videoExamIdList.contains(it.examId)) {
                            index++
                            videoCount++
                            videoExamIdList.add(it.examId)
                            saveFile.appendText(gson.toJson(it))
                            saveFile.appendText("\r\n")
                        }
                    }
                    swipeCount++
                    Log.d(TAG, "滑动${swipeCount}次，本次新增${index}条视频，目前共${videoCount}条视频")
                    index = 0
                }
            }
  })
```
最后，我们可以使用 autojs 实现每隔 4s 下拉刷新列表，并把数据写进文件中。

以上内容仅供学习参考，如有侵权请告知，我们会立即联系并且删除。
[源码地址](https://github.com/ppjuns/XposedMoreVideo)
