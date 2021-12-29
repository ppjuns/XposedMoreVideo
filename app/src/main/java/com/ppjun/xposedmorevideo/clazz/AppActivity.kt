package com.ppjun.xposedmorevideo.clazz

import com.ppjun.xposedmorevideo.core.AllMethodMapper
import com.ppjun.xposedmorevideo.core.ClassMapper

/**
 * @author ppjun <953386166@qq.com>
 * @description
 * @date 2021-12-29
 */
class AppActivity (classLoader: ClassLoader) :
    ClassMapper(classLoader, "android.app.Activity") {

    val onCreate by lazy {
        AllMethodMapper(clazz, "onCreate")
    }

}