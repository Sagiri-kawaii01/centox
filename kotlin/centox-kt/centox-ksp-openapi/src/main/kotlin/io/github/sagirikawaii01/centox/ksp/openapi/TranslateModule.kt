package io.github.sagirikawaii01.centox.ksp.openapi

import com.github.victools.jsonschema.generator.Module
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/15 16:44
 * @since
 */
class TranslateModule(
    val translate: MutableMap<String, String>
): Module {
    override fun applyToConfigBuilder(builder: SchemaGeneratorConfigBuilder) {
        builder.forFields().withInstanceAttributeOverride { _, p1, _ ->
            translate.putIfAbsent(p1.name, "")
        }
    }
}