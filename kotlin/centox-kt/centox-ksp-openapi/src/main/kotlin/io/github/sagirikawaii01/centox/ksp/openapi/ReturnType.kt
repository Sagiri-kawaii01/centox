package io.github.sagirikawaii01.centox.ksp.openapi
/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/16 16:09
 * @since
 */
data class ReturnType(
    val requiredClass: MutableSet<String> = mutableSetOf(),
    val fields: MutableMap<String, Boolean> = mutableMapOf()
)