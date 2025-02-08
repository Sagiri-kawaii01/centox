package io.github.sagirikawaii01.centox.ksp.openapi

import com.alibaba.fastjson2.JSON
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.*
import kotlin.reflect.KClass

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/12 10:36
 * @since
 */
class EnumModule(
    val enumMap: MutableMap<String, KClass<out EnumDoc>>
): Module {
    override fun applyToConfigBuilder(configBuilder: SchemaGeneratorConfigBuilder) {
        configBuilder.forFields().withInstanceAttributeOverride { p0, p1, _ ->
            if (enumMap.containsKey(p1.name)) {
                setEnumDoc(enumMap[p1.name]!!.java.enumConstants, p0)
            }
            val anno = p1.getAnnotation(See::class.java)
            if (anno != null) {
                setEnumDoc(anno.enumClass.java.enumConstants, p0)
            }
        }
    }

    private fun setEnumDoc(enums: Array<out EnumDoc>, node: ObjectNode) {
        if (enums.isNotEmpty()) {
            val docs = enums[0].docs()
            node.put("enum", JSON.toJSONString(docs.map { it.key }))
            val enumArray = node.putArray("enum")

            val apiFoxNode = node.putObject("x-apifox")
                .putObject("enumDescriptions")
            docs.forEach { (k, v) ->
                apiFoxNode.put(k.toString(), v)
                when (k) {
                    is Int -> enumArray.add(k)
                    is Double -> enumArray.add(k)
                    is Long -> enumArray.add(k)
                    is String -> enumArray.add(k)
                }
            }

        }
    }
}