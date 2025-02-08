package io.github.sagirikawaii01.centox.ksp.openapi

import kotlin.reflect.KClass

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/8 14:56
 * @since
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class See(val enumClass: KClass<out EnumDoc>)