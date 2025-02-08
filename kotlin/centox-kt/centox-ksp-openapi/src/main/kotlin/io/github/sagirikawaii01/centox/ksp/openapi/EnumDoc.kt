package io.github.sagirikawaii01.centox.ksp.openapi

/**
 * 枚举类想要生成文档需要实现此接口
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/7 10:51
 * @since 1.1.19
 */
interface EnumDoc {
    fun docs(): List<DocEnum>
}