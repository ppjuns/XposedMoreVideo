package com.ppjun.xposedmorevideo.bean

import com.google.gson.annotations.SerializedName
import com.ppjun.xposedmorevideo.bean.AnswerVos

/**
 * @author ppjun <953386166@qq.com>
 * @description
 * @date 2021-12-29
 */
class VideoExamVos {
    @SerializedName("examId")
    var examId = 0

    @SerializedName("playUrl")
    var playUrl = ""

    @SerializedName("coverUrl")
    var coverUrl = ""

    @SerializedName("question")
    var question = ""

    @SerializedName("answerVos")
    var answerVos: List<AnswerVos>? = null
}