package com.ppjun.xposedmorevideo.bean

import com.google.gson.annotations.SerializedName

/**
 * @author ppjun <953386166@qq.com>
 * @description
 * @date 2021-12-29
 */
class VideoExam {
    @SerializedName("examNum")
    var examNum: Int = 0

    @SerializedName("videoExamVos")
    var videoExamVos: List<VideoExamVos>? = null

}