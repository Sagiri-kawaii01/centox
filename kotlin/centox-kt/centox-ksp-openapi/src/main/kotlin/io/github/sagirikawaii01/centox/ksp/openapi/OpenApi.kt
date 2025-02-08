package io.github.sagirikawaii01.centox.ksp.openapi

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.*
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationModule
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationOption
import kotlinx.coroutines.runBlocking
import org.reflections.Reflections
import org.reflections.scanners.Scanner
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.util.LinkedList
import java.util.Queue
import kotlin.reflect.KClass

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/11 17:02
 * @since
 */

lateinit var schemaGenerator: SchemaGenerator
class OpenApi() {
    var packageName: String = ""
    @field:JsonIgnore
    val enumMap: MutableMap<String, KClass<out EnumDoc>> = mutableMapOf()
    val openapi: String = "3.0.0"
    var info: Info? = null
    var servers: MutableList<Server> = mutableListOf()
    var paths: MutableMap<String, PathItem> = mutableMapOf()
    var components: Components = Components()
    var tags: MutableList<Tag> = mutableListOf()

    private fun translate(translate: Map<String, String>): Map<String, String> {
        val modifiedTranslate = translate.toMutableMap()

        schemaGenerator = SchemaGenerator(SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(JacksonModule())
            .with(TranslateModule(modifiedTranslate))
            .build())
        resolve(modifiedTranslate)

        return modifiedTranslate
    }

    fun execResolve(translate: Map<String, String> = emptyMap(),
                    returnTypes: Map<String, ReturnType>) {

        val modifiedTranslate = translate(translate)

        val reflections = Reflections(ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packageName))
            .setScanners(Scanners.TypesAnnotated)
        )

        reflections.getTypesAnnotatedWith(EnumField::class.java).forEach {
            @Suppress("UNCHECKED_CAST")
            enumMap[it.getAnnotation(EnumField::class.java).fieldName] = it.kotlin as KClass<out EnumDoc>
        }

        schemaGenerator = SchemaGenerator(SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(JacksonModule())
            .with(JavaxValidationModule(JavaxValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED))
            .with(DescriptionModule())
            .with(EnumModule(enumMap))
            .with(RefModule(this))
            .with(ResponseFieldRequiredModule(returnTypes))
            .build())

        resolve(modifiedTranslate)
    }

    private fun resolve(translate: Map<String, String> = emptyMap()) {
        for (value in paths.values) {
            value.get?.resolve(translate)
            value.post?.resolve(translate)
            value.put?.resolve(translate)
            value.delete?.resolve(translate)
        }
        if (translate.isNotEmpty()) {
            for (value in paths.values) {
                val operations = mutableListOf<Operation>()
                if (value.get != null) {
                    operations.add(value.get!!)
                }
                if (value.post != null) {
                    operations.add(value.post!!)
                }
                if (value.put != null) {
                    operations.add(value.put!!)
                }
                if (value.delete != null) {
                    operations.add(value.delete!!)
                }
            }
        }
        components.resolve(translate)
    }

}

class Info {
    var title: String = "未命名"
    var version: String = "1.0"
    var description: String? = null
    var termsOfService: String? = null
    var contact: Contact? = null
    var license: License? = null
}

class Contact {
    var name: String? = null
    var url: String? = null
    var email: String? = null
}

class License {
    var name: String? = null
    var url: String? = null
}

class Server {
    var url: String = ""
    var description: String? = null
    var variables: MutableMap<String, ServerVariable> = mutableMapOf()
}

class ServerVariable {
    var enum: MutableList<String> = mutableListOf()
    var default: String = ""
    var description: String? = null
}

class Operation {
    var tags: MutableList<String> = mutableListOf()
    var summary: String? = null
    @field:JsonProperty("x-apifox-folder")
    @get:JsonProperty("x-apifox-folder")
    var xApiFoxFolder: String? = null
    var description: String? = null
    var operationId: String? = null
    var parameters: MutableList<Parameter> = mutableListOf()
    var requestBody: RequestBody? = null
    var responses: MutableMap<String, Response> = mutableMapOf()
    var callbacks: MutableMap<String, Callback> = mutableMapOf()
    var deprecated: Boolean = false
    var security: MutableList<Map<String, MutableList<String>>> = mutableListOf()
    var servers: MutableList<Server> = mutableListOf()

    fun resolve(translate: Map<String, String>) {
        for (parameter in parameters) {
            val enumStr = parameter.resolve()
            if (translate.containsKey(parameter.name)) {
                if (parameter.description.isNullOrBlank()) {
                    parameter.description = translate[parameter.name]
                } else if (!parameter.description!!.contains(translate[parameter.name]!!)) {
                    parameter.description = "${translate[parameter.name]}, ${parameter.description}"
                }
            }
            parameter.description = if (parameter.description.isNullOrBlank()) {
                enumStr
            } else {
                if (enumStr.isBlank()) {
                    parameter.description
                } else {
                    "${parameter.description}; [$enumStr]"
                }
            }
        }
        requestBody?.resolve(translate)
        responses.values.forEach {
            it.resolve(translate)
        }
    }
}

class PathItem {
    var summary: String? = null
    var description: String? = null
    var get: Operation? = null
    var put: Operation? = null
    var post: Operation? = null
    var delete: Operation? = null
    var options: Operation? = null
    var head: Operation? = null
    var patch: Operation? = null
    var trace: Operation? = null
    var servers: MutableList<Server> = mutableListOf()
    var parameters: MutableList<Parameter> = mutableListOf()
}


class Components {
    var schemas: MutableMap<String, ObjectNode> = mutableMapOf()
    var schemasClasses: MutableSet<String> = mutableSetOf()
    var responses: MutableMap<String, Response> = mutableMapOf()
    var parameters: MutableMap<String, Parameter> = mutableMapOf()
    var examples: MutableMap<String, Example> = mutableMapOf()
    var requestBodies: MutableMap<String, RequestBody> = mutableMapOf()
    var headers: MutableMap<String, Header> = mutableMapOf()
    var securitySchemes: MutableMap<String, SecurityScheme> = mutableMapOf()
    var links: MutableMap<String, Link> = mutableMapOf()
    var callbacks: MutableMap<String, Callback> = mutableMapOf()

    fun resolve(translate: Map<String, String>) {
        for (className in schemasClasses) {
            val simpleName = className.substring(className.lastIndexOf(".") + 1)
            if (!schemas.containsKey(simpleName)) {
                val schema = schemaGenerator.generateSchema(Class.forName(className))
                schemas[simpleName] = schema
                fieldsPostHandle(schema, translate)
                modifySelfRef(schema, "#/components/schemas/$simpleName")
                schema.remove("nullable")
            }
        }
    }

    private fun modifySelfRef(node: JsonNode, path: String) {
        if (node.isObject) {
            val objectNode = node as ObjectNode
            // 检查并修改当前 ObjectNode
            if (node.has("\$ref") && "#" == node["\$ref"].asText()) {
                objectNode.put("\$ref", path)
            }
            // 递归检查每个子节点
            node.fields().forEachRemaining { entry -> modifySelfRef(entry.value, path) }
        } else if (node.isArray) {
            // 遍历数组中的每个元素
            for (item in node) {
                modifySelfRef(item, path)
            }
        }
    }
}

class MediaType {
    var className: String? = null
    var schema: ObjectNode? = null
    var example: Any? = null
    var examples: MutableMap<String, Example> = mutableMapOf()

    fun resolve(translate: Map<String, String>) {
        if (null != className) {
            this.schema = schemaGenerator.generateSchema(Class.forName(className))
            fieldsPostHandle(this.schema!!, translate)
            this.schema?.remove("nullable")
        }
    }
}

class Response {
    var description: String = ""
    var headers: MutableMap<String, Header> = mutableMapOf()
    var content: MutableMap<String, MediaType> = mutableMapOf()
    var links: MutableMap<String, Link> = mutableMapOf()

    fun resolve(translate: Map<String, String>) {
        content.values.forEach {
            it.resolve(translate)
            if (it.schema != null) {
                val required = if (it.schema!!["required"] == null) {
                    it.schema!!.putArray("required")
                } else {
                    it.schema!!["required"] as ArrayNode
                }
                required.add("status")
                required.add("message")
                (it.schema!!["properties"]["message"]!! as ObjectNode).remove("nullable")
                (it.schema!!["properties"]["status"]!! as ObjectNode).remove("nullable")
                (it.schema!!["properties"]!! as ObjectNode).remove("nullable")
            }
        }
    }
}

class RequestBody {
    var description: String? = null
    var content: MutableMap<String, MediaType> = mutableMapOf()
    var required: Boolean = false

    fun resolve(translate: Map<String, String>) {
        content.values.forEach { media ->
            media.resolve(translate)
            val queue: Queue<ObjectNode> = LinkedList()
            queue.add(media.schema)
            while (queue.isNotEmpty()) {
                val node = queue.poll()
                val type = node["type"]
                val required = if (node["required"] != null && node["required"] is ArrayNode) {
                    (node["required"] as ArrayNode).map { it.asText() }.toSet()
                } else {
                    emptySet()
                }
                if (type != null) {
                    if (type.textValue() == "object") {
                        node["properties"].fields()?.forEach { field ->
                            val item = node["properties"][field.key]
                            queue.add(item as ObjectNode)
                            if (required.contains(field.key)) {
                                item.remove("nullable")
                            }
                        }
                    } else if (type.textValue() == "array") {
                        if (node["items"] is ArrayNode) {
                            val array = node["items"] as ArrayNode
                            array.forEach { item ->
                                queue.add(item as ObjectNode)
                            }
                        } else if (node["items"] is ObjectNode) {
                            (node["items"] as ObjectNode).remove("nullable")
                        }
                    }
                }
            }
        }
    }
}

class Parameter {
    var type: String = ""
    var description: String? = null
    var required: Boolean? = null
    var name: String? = null
    var `in`: String? = null
    var schema: Any? = null
    var bearerFormat: String? = null
    var openIdConnectUrl: String? = null

    fun resolve(): String {
        if (description?.startsWith("enum:") == true) {
            description = description!!.substring(5)
            val split = description!!.split(":")
            val enumClass = split[0]
            description = split[1]
            return (Class.forName(enumClass).enumConstants[0] as EnumDoc).docs().joinToString("; ") { "${it.key}: ${it.nameZh}" }
        }
        return ""
    }
}

class Example {
    var summary: String? = null
    var description: String? = null
    var value: Any? = null
    var externalValue: String? = null
}

class Header {
    var type: String = ""
    var description: String? = null
    var name: String? = null
    var `in`: String? = null
    var scheme: String? = null
    var bearerFormat: String? = null
    var openIdConnectUrl: String? = null
}
class SecurityScheme {
    var type: String = ""
    var description: String? = null
    var name: String? = null
    var `in`: String? = null
    var scheme: String? = null
    var bearerFormat: String? = null
    var openIdConnectUrl: String? = null
}
class Link {
    var operationRef: String? = null
    var operationId: String? = null
    var parameters: MutableMap<String, Any> = mutableMapOf()
    var requestBody: Any? = null
    var description: String? = null
    var server: Server? = null
}
class Callback

class Tag {
    var name: String = ""
    var description: String? = null
}

private fun fieldsPostHandle(node: ObjectNode, translate: Map<String, String>): Boolean {
    val ret = if (null == node["required"] || (node["required"].isBoolean && !node["required"].asBoolean())) {
        node.put("nullable", true)
        false
    } else {
        node["required"].isBoolean && node["required"].asBoolean()
    }
    val requiredFields = mutableListOf<String>()
    if (null != node["type"]) {
        if (node["type"].textValue() == "object") {
            node["properties"].fields()?.forEach { field ->
                if (field.key == "file" && node["properties"][field.key]["properties"] == null) {
                    val file = (node["properties"][field.key] as ObjectNode)
                    file.put("type", "string")
                    file.put("format", "binary")
                }
                fieldHandle(node["properties"][field.key] as ObjectNode, translate, field.key)
                if (fieldsPostHandle(node["properties"][field.key] as ObjectNode, translate)) {
                    requiredFields.add(field.key)
                }
            }
        } else if (node["type"].textValue() == "array") {
            fieldsPostHandle(node["items"] as ObjectNode, translate)
        }
    }

    if (requiredFields.isNotEmpty()) {
        val required = node.putArray("required")
        requiredFields.forEach {
            required.add(it)
        }
    }

    return ret
}

private fun fieldHandle(node: ObjectNode,
                        translate: Map<String, String>,
                        key: String) {
    node.put("title", translate[key])
    if (null != node["format"] && node["format"].textValue() == "date-time") {
        node.put("pattern", "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\$")
        node.put("description", "yyyy-MM-dd HH:mm:ss")
    }
}