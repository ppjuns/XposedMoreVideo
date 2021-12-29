package com.ppjun.xposedmorevideo.clazz

/**
 * @description
 * @author ppjun
 * @date 2021-08-31 13:26
 */
object CnClazz {
    lateinit var classLoader: ClassLoader
    val BaseWebInterface by lazy { BaseWebInterface(classLoader) }
    val AppActivity by lazy { AppActivity(classLoader) }
}