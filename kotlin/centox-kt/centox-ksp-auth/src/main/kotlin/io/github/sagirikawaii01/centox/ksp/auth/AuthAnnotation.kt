package io.github.sagirikawaii01.centox.ksp.auth

/**
 * 指定对应注解为鉴权注解，并编译生成AOP代码
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/12 17:54
 * @since 1.0.0
 * @property header token header
 * @property prefix token前缀，不需要带空格
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class AuthAnnotation(
    val header: String = "Authorization",
    val prefix: String = "Bearer")
