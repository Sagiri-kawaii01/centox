package io.github.sagirikawaii01.centox.ksp.auth

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/14 15:49
 * @since 1.0.0
 */
interface Token {
    fun getRoles(): List<Role>
}