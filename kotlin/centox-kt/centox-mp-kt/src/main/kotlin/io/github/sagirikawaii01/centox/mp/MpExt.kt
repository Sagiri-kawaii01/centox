package io.github.sagirikawaii01.centox.mp

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/10 14:50
 * @since
 */
inline fun <reified T: Any> queryWrapper(): KtQueryWrapper<T> {
    return KtQueryWrapper(T::class.java)
}

inline fun <reified T: Any> updateWrapper(): KtUpdateWrapper<T> {
    return KtUpdateWrapper(T::class.java)
}