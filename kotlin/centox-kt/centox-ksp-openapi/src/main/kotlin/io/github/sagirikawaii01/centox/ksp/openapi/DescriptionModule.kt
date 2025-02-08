package io.github.sagirikawaii01.centox.ksp.openapi

import com.github.victools.jsonschema.generator.*

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/11 16:57
 * @since
 */
class DescriptionModule: Module {
    override fun applyToConfigBuilder(configBuilder: SchemaGeneratorConfigBuilder) {

        // 定义一个自定义的属性处理器
        configBuilder.forFields().withInstanceAttributeOverride { p0, p1, _ ->
            val anno = p1.getAnnotation(Description::class.java)
            if (anno != null) {
                p0.put("description", anno.value)
            }
        }
    }
}