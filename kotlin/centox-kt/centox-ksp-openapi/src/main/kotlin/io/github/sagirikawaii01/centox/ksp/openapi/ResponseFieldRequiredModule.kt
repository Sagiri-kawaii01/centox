package io.github.sagirikawaii01.centox.ksp.openapi

import com.github.victools.jsonschema.generator.Module
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/16 16:15
 * @since
 */
class ResponseFieldRequiredModule(
    private val returnTypes: Map<String, ReturnType>
): Module {
    override fun applyToConfigBuilder(builder: SchemaGeneratorConfigBuilder) {
        builder.forFields().withInstanceAttributeOverride { node, field, _ ->
            if (returnTypes.containsKey(field.declaredType.erasedType.name) || returnTypes.containsKey(field.declarationDetails.schemaTargetType.erasedType.name)) {
                val returnType = returnTypes[field.declarationDetails.schemaTargetType.erasedType.name] ?: returnTypes[field.declaredType.erasedType.name]!!
                // 最外层封装的 result
                if (field.name == "result") {
                    node.put("required", returnType.fields[field.name] == true || returnType.requiredClass.contains(field.declarationDetails.schemaTargetType.erasedType.name))
                } else {
                    // 普通字段
                    node.put("required", returnType.fields[field.name])
                }
            }

        }
    }
}