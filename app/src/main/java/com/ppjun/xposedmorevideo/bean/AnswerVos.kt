package com.ppjun.xposedmorevideo.bean

import com.google.gson.annotations.SerializedName

/**
 * @author ppjun <953386166@qq.com>
 * @description
 * @date 2021-12-29
 */
class AnswerVos {
    @SerializedName("answerName")
    var answerName = ""

    @SerializedName("correct")
    var correct = false

    @SerializedName("answerId")
    var answerId = ""
}