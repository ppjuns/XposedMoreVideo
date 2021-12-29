package com.ppjun.xposedmorevideo.hook

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.ppjun.xposedmorevideo.bean.VideoExam
import com.ppjun.xposedmorevideo.clazz.CnClazz
import de.robv.android.xposed.XC_MethodHook
import java.io.File

/**
 * @author ppjun <953386166@qq.com>
 * @description
 * @date 2021-12-28
 */
class BaseWebInterfaceHook {

    companion object {
        private const val TAG = "BaseWebInterface"
    }

    init {
        var index = 0
        var swipeCount = 0
        var videoCount = 0
        val videoExamIdList = ArrayList<Int>()
        val saveFile = File("/sdcard/moreVideo.txt")
        if (saveFile.exists()) {
            saveFile.delete()
        }
        saveFile.createNewFile()

        CnClazz.BaseWebInterface.encode.setHook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                Log.d(TAG, "encode ${param.args[0]}")
            }
        })

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

        CnClazz.AppActivity.onCreate.setHook(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                val className = param.thisObject.javaClass.name
                if (!className.startsWith("com.xmiles")) {
                    Log.d(TAG, "${param.thisObject.javaClass.name} finish")
                    (param.thisObject as Activity).finish()
                }
            }
        })
    }
}
