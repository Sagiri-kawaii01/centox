package io.github.sagirikawaii01.centox.ksp.openapi

import com.github.victools.jsonschema.generator.CustomDefinition
import com.github.victools.jsonschema.generator.Module
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.impl.DefinitionKey
import com.github.victools.jsonschema.generator.impl.SchemaGenerationContextImpl



/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/12 17:44
 * @since
 */
class RefModule(val openApi: OpenApi): Module {
    override fun applyToConfigBuilder(configBuilder: SchemaGeneratorConfigBuilder) {
        configBuilder.forFields().withCustomDefinitionProvider { scope, context ->
            val ct = context as SchemaGenerationContextImpl
            for (key in ct.definedTypes) {
                if (ct.getReferences(key).isNotEmpty() && key.type.typeName == scope.declarationDetails.schemaTargetType.typeName) {
                    var type = scope.declarationDetails.schemaTargetType
                    while (type.typeParameters.isNotEmpty()) {
                        type = type.typeParameters[0]
                    }
                    openApi.components.schemasClasses.add(type.typeName)
                }
            }
            null
        }
     }

}