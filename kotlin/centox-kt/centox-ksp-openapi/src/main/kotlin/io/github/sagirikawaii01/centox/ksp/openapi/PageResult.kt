package io.github.sagirikawaii01.centox.ksp.openapi

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/19 11:45
 * @since
 */
data class PageResult<T>(
    val data: T,
    val currentPage: Int,
    val pageSize: Int,
    val pageCount: Int,
    val dataCount: Int,
    val timestamp: Long
)