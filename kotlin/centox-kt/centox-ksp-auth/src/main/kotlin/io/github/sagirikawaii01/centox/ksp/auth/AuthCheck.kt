package io.github.sagirikawaii01.centox.ksp.auth

/**
 * token验证及授权逻辑
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/14 15:52
 * @since 1.0.0
 */
interface AuthCheck {
    fun check(token: String?): Token?
}