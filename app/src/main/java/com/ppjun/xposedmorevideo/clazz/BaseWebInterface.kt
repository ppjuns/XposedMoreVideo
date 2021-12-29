package com.ppjun.xposedmorevideo.clazz

import com.ppjun.xposedmorevideo.core.ClassMapper
import com.ppjun.xposedmorevideo.core.MethodMapper
import org.json.JSONObject

/**
 * @author ppjun <953386166@qq.com>
 * @description
 * @date 2021-12-29
 */
class BaseWebInterface(classLoader: ClassLoader) :
    ClassMapper(classLoader, "com.xmiles.business.web.BaseWebInterface") {
    val decode by lazy {
        MethodMapper(clazz, "dt", JSONObject::class.java)
    }

    val encode by lazy {
        MethodMapper(clazz, "et", JSONObject::class.java)
    }
}