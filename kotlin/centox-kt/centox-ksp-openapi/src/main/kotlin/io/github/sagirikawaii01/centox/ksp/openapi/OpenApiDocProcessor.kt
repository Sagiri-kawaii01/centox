package io.github.sagirikawaii01.centox.ksp.openapi

import com.alibaba.fastjson2.JSON
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import io.github.sagirikawaii01.centox.core.annotation.BodyParam
import io.github.sagirikawaii01.centox.core.annotation.Pageable
import io.github.sagirikawaii01.centox.ksp.openapi.ApiParams
import io.github.sagirikawaii01.centox.log.annotation.ApiName
import io.github.sagirikawaii01.centox.log.annotation.Log
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths


/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/11 17:00
 * @since 1.0.0
 */
class OpenApiDocProcessor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    private var log: KSPLogger = environment.logger
    private var codeGenerator = environment.codeGenerator
    private val config = readConfig()
    private val openApi = OpenApi()
    private val tagNameSet = mutableSetOf<String>()
    private var packageName: String? = environment.options["packageName"]
    private var currentPage: String? = environment.options["currentPage"]
    private lateinit var resolver: Resolver
    private var translate: Map<String, String> = emptyMap()
    private var i = 0
    private var j = 0
    private val returnField: MutableMap<String, ReturnType> = mutableMapOf<String, ReturnType>().apply {
        this["io.github.sagirikawaii01.centox.ksp.openapi.PageResult"] = ReturnType(
            fields = mutableMapOf(
                "data" to true,
                "currentPage" to true,
                "pageSize" to true,
                "dataCount" to true,
                "pageCount" to true,
                "timestamp" to true
            )
        )
    }

    init {
        openApi.info = Info().apply {
            title = config.projectName
            version = config.version
        }
        openApi.packageName = packageName!!
        // 获取翻译文件
        val paths = environment.options["translate.yml.paths"]
        if (!paths.isNullOrBlank()) {
            for (path in paths.split("|")) {
                val filePath = path.removeSuffix("translate.yml").removeSuffix("/") + "/translate.yml"
                if (File(filePath).exists()) {
                    try {
                        val ins = Files.newInputStream(Paths.get(filePath))
                        translate = Yaml().load(ins)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver
        // 扫描 controller
        var symbols = resolver.getSymbolsWithAnnotation(RestController::class.qualifiedName!!).toList()
        val ret = symbols.filter { !it.validate() }.toMutableList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(OpenApiDocVisitor(), Unit) }

        symbols = resolver.getSymbolsWithAnnotation(ApiParams::class.qualifiedName!!).toList()
        ret.addAll(symbols.filter { !it.validate() }.toList())

        return ret
    }

    inner class OpenApiDocVisitor: KSVisitorVoid() {
        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // 扫描日志注解，得到模块名，即文件夹目录，名称中的斜杠会时目录分级
            val logAnno = classDeclaration.getAnnotationsByType(Log::class).toList()
            var tag: String? = null
            if (logAnno.isNotEmpty()) {
                tag = logAnno[0].module
                tagNameSet.add(tag)
            }

            // 扫描全部的方法
            for (declaredFunction in classDeclaration.getDeclaredFunctions().filter { it.simpleName.asString() != "<init>" }) {
                // 得到请求的参数类型
                val mediaType = getMediaType(declaredFunction)
                // 生成参数和返回值的class，用来生成json schema
                var className = "${classDeclaration.packageName.asString()}.doc.ApiParams$i"

                var pageType = 0
                // 判断分页类型，0没注解即不能分页，1必须分页，2都可
                val pageable = declaredFunction.getAnnotationsByType(Pageable::class).toList()
                if (pageable.isNotEmpty()) {
                    pageType = 1
                    if (pageable[0].enablePaginateNot) {
                        pageType = 2
                    }
                }

                // 有参数才生成class
                if ("none" != mediaType) {
                    // 是否有@RequestBody注解
                    val body = declaredFunction.getRequestBody()
                    // 有可以直接用对应的class，没有需要扫描参数生成class
                    if (null == body) {
                        val generatedClass = codeGenerator.createNewFile(
                            dependencies = Dependencies.ALL_FILES,
                            packageName = "${classDeclaration.packageName.asString()}.doc",
                            fileName = "ApiParams$i"
                        )
                        if (null == packageName) {
                            packageName = classDeclaration.packageName.asString()
                        }



                        // 写入class
                        generatedClass.appendText(createClassByParameters(declaredFunction.parameters, i++, "${classDeclaration.packageName.asString()}.doc", pageType))
                        generatedClass.close()
                    } else {
                        className = body.qualifiedName()
                    }
                }

                var returnClassName = "io.github.sagirikawaii01.centox.x.pojo.ApiResponse"
                var returnPageClassName = ""

                // 有返回值才生成class
                if (declaredFunction.returnType!!.simpleName() != "Unit") {
                    if (pageType in arrayOf(0, 2)) {
                        returnClassName = "${classDeclaration.packageName.asString()}.doc.ApiReturn$j"
                        val generatedClass = codeGenerator.createNewFile(
                            dependencies = Dependencies.ALL_FILES,
                            packageName = "${classDeclaration.packageName.asString()}.doc",
                            fileName = "ApiReturn$j"
                        )
                        if (null == packageName) {
                            packageName = classDeclaration.packageName.asString()
                        }

                        generatedClass.appendText(createClassByReturnType(declaredFunction.returnType!!, j++, "${classDeclaration.packageName.asString()}.doc"))
                        generatedClass.close()
                    }

                    if (pageType in arrayOf(1, 2)) {
                        if (pageType == 1) {
                            returnClassName = "${classDeclaration.packageName.asString()}.doc.ApiReturn$j"
                        } else {
                            returnPageClassName = "${classDeclaration.packageName.asString()}.doc.ApiReturn$j"
                        }
                        returnField["io.github.sagirikawaii01.centox.ksp.openapi.PageResult"]!!.requiredClass.add("${classDeclaration.packageName.asString()}.doc.ApiReturn$j")
                        val generatedClass = codeGenerator.createNewFile(
                            dependencies = Dependencies.ALL_FILES,
                            packageName = "${classDeclaration.packageName.asString()}.doc",
                            fileName = "ApiReturn$j"
                        )
                        if (null == packageName) {
                            packageName = classDeclaration.packageName.asString()
                        }

                        generatedClass.appendText(createClassByReturnType(declaredFunction.returnType!!, j++, "${classDeclaration.packageName.asString()}.doc", true))
                        generatedClass.close()
                    }

                    if (!declaredFunction.returnType!!.resolve().isMarkedNullable) {
                        returnField.putIfAbsent(returnClassName, ReturnType())
                        returnField[returnClassName]!!.fields["result"] = true
                    }

                    val fullName = declaredFunction.returnType!!.qualifiedName()
                    returnTypeRequiredResolve(declaredFunction.returnType!!, fullName, declaredFunction.returnType!!.resolve().isMarkedNullable)
                }

                // 请求路径
                val paths = declaredFunction.paths()
                paths.forEach { path ->
                    val (method, realPath) = path.split(":")
                    if (!openApi.paths.containsKey(realPath)) {
                        openApi.paths[realPath] = PathItem()
                    }

                    // 这里是openapi格式doc的操作，为不同类型请求的路径添加请求参数className
                    val operation = Operation().apply {
                        if (mediaType != "none") {
                            this.requestBody = RequestBody().apply {
                                this.content[mediaType] = MediaType().apply {
                                    this.className = className
                                }
                            }
                        }
                    }

                    // 接口名称，同样从日志注解@ApiName得到
                    operation.summary = declaredFunction.getApiName()

                    // 填充接口目录结构
                    if (null != tag) {
                        operation.xApiFoxFolder = tag
                        operation.tags.add(tag)
                    }

                    if (pageType in arrayOf(0, 2)) {
                        // 返回值文档
                        operation.responses["200"] = Response().apply {
                            this.description = "成功"
                            this.content[org.springframework.http.MediaType.APPLICATION_JSON_VALUE] = MediaType().apply {
                                this.className = returnClassName
                            }
                        }
                    }

                    if (pageType == 1) {
                        operation.responses["200"] = Response().apply {
                            this.description = "分页"
                            this.content[org.springframework.http.MediaType.APPLICATION_JSON_VALUE] = MediaType().apply {
                                this.className = returnClassName
                            }
                        }
                    }

                    if (pageType == 2) {
                        operation.responses["x-200:分页"] = Response().apply {
                            this.description = "分页"
                            this.content[org.springframework.http.MediaType.APPLICATION_JSON_VALUE] = MediaType().apply {
                                this.className = returnPageClassName
                            }
                        }
                    }

                    when (method) {
                        "GET" -> openApi.paths[realPath]!!.get = operation
                        "POST" -> openApi.paths[realPath]!!.post = operation
                        "PUT" -> openApi.paths[realPath]!!.put = operation
                        "DELETE" -> openApi.paths[realPath]!!.delete = operation
                    }
                    operation.parameters = getParams(declaredFunction)

                    // 接口注释
                    val desc = declaredFunction.getAnnotationsByType(Description::class).toList()
                    if (desc.isNotEmpty()) {
                        operation.description = desc[0].value
                    }

                    // 是否废弃
                    val deprecated = declaredFunction.getAnnotationsByType(Deprecated::class).toList()
                    if (deprecated.isNotEmpty()) {
                        operation.deprecated = true
                    }
                }
            }
        }


    }

    private fun returnTypeRequiredResolve(type: KSTypeReference, parentClassName: String, nullable: Boolean) {
        if ((type.resolve().declaration as KSClassDeclaration).isCollection()) {
            return returnTypeRequiredResolve(type.resolve().arguments[0].type!!, parentClassName, nullable)
        }
        val classDeclaration = type.resolve().declaration as KSClassDeclaration
        val fullName = classDeclaration.qualifiedName!!.asString()
        if (!returnField.containsKey(fullName)) {
            val m = ReturnType()
            returnField[fullName] = m
            classDeclaration.getAllProperties().forEach {
                val required = !it.type.resolve().isMarkedNullable
                if (it.packageName.asString().startsWith(packageName!!)) {
                    returnTypeRequiredResolve(it.type, fullName, !required)
                }
                m.fields[it.simpleName.getShortName()] = required
            }
        }
        if (!nullable) {
            returnField[fullName]!!.requiredClass.add(parentClassName)
        }
    }

    // 前期处理完毕后，开始生成文档，但schema需要运行时才能生成，所以这里用json序列化OpenApi
    override fun finish() {

        tagNameSet.forEach {
            openApi.tags.add(Tag().apply {
                this.name = it
            })
        }

        val file = codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "$packageName.doc",
            "ApiDoc"
        )

        file.appendText(StringBuilder().apply {
            append("""
                |package $packageName.doc
                |import io.github.sagirikawaii01.centox.ksp.openapi.OpenApi
                |import org.springframework.stereotype.Component
                |import com.alibaba.fastjson2.to
                |import com.fasterxml.jackson.module.kotlin.jsonMapper
                |import com.fasterxml.jackson.module.kotlin.kotlinModule
                |import io.github.sagirikawaii01.centox.ksp.openapi.ReturnType
                |import com.alibaba.fastjson2.JSON
                |
                |@Component
                |class ApiDoc {
                |   private var docJson = "${openApi.toJson().replace("\"", "\\\"")}"
                |   
                |   private val translate = "${translate.toJson().replace("\"", "\\\"")}"
                |   
                |   private val returnTypes = "${returnField.toJson().replace("\"", "\\\"")}"
                |   
                |   init {
                |       val apiDoc = docJson.to<OpenApi>()
                |       
                |       val rts = mutableMapOf<String, ReturnType>()
                |       val rtsJson = JSON.parseObject(returnTypes)
                |       rtsJson.keys.forEach {
                |           val obj = rtsJson.getJSONObject(it)
                |           val requiredClass = mutableSetOf<String>()
                |           val fields = mutableMapOf<String, Boolean>()
                |           for (any in obj.getJSONArray("requiredClass")) {
                |               requiredClass.add(any as String)
                |           }
                |           obj.getJSONObject("fields").forEach { (k, v) ->
                |               fields[k] = v.toString().toBoolean()
                |           }
                |           rts[it] = ReturnType(requiredClass, fields)
                |       }
                |
                |       apiDoc.execResolve(translate.to(), rts)
                |
                |       docJson = jsonMapper {
                |           addModule(kotlinModule())
                |       }.writeValueAsString(apiDoc).replace("\${'$'}defs", "components/schemas")
                |   }
                |   
                |   fun getDocJsonAndClear(): String {
                |       val temp = docJson
                |       docJson = ""
                |       return temp
                |   }
                |}
            """.trimMargin())
        }.toString())
        file.close()
        super.finish()
    }

    private fun readConfig(): Config {
        return Config(environment.options["projectName"] ?: "未命名", environment.options["version"] ?: "1.0")
    }

    private fun createClassByReturnType(returnType: KSTypeReference,
                                        index: Int,
                                        packageName: String,
                                        page: Boolean = false): String {
        val code = StringBuilder()
        val dependencies = getGenericsDependencies(returnType)

        return code.apply {
            // package
            line("package $packageName")
            nextLine()

            // dependencies
            dependencies.forEach {
                line("import $it")
            }
            if (page) {
                line("import io.github.sagirikawaii01.centox.ksp.openapi.PageResult")
            }
            nextLine()
            append("""
                |data class ApiReturn$index (
                |   val status: Long,
                |   val message: String,
                |   val result: ${if (page) {"PageResult<"} else {""}}${getGenericsType(returnType)}${if (page) {">"} else {""}}
                |)
            """.trimMargin())
        }.toString()
    }

    private fun createClassByParameters(parameters: List<KSValueParameter>,
                                        index: Int,
                                        packageName: String,
                                        pageType: Int = 0): String {
        val code = StringBuilder()
        val dependencies = findParametersDependencies(parameters)

        code.apply {
            // package
            line("package $packageName")
            nextLine()

            // dependencies
            line("import io.github.sagirikawaii01.centox.ksp.openapi.ApiParams")
            line("import javax.validation.constraints.*")

            dependencies.forEach {
                line("import $it")
            }

            nextLine()

            // annotation
            line("@ApiParams")

            // class
            line("data class ApiParams$index (")

            // fields
            filtrateSpringParameters(parameters).forEach { param ->
                param.annotations.forEach {
                    if (it.annotationType.resolve().declaration.packageName.asString() in arrayOf(
                            "javax.validation.constraints",
                            "io.github.sagirikawaii01.centox.ksp.openapi"
                    )) {
                        line("    ${it.originCode().replace("@", "@field:")}")
                    }
                }
                line("    val ${param.name!!.asString()}: ${param.type.typeString()},")
            }

            if (pageType == 1) {
                line("    @field:NotNull")
                line("    val currentPage: Int?,")
                line("    @field:NotNull")
                line("    val pageSize: Int?,")
            }

            if (pageType == 2) {
                line("    val currentPage: Int?,")
                line("    val pageSize: Int?,")
            }

            // end
            line(")")
        }
        return code.toString()
    }

    private fun findParametersDependencies(parameters: List<KSValueParameter>): Set<String> {
        return mutableSetOf<String>().apply {
            parameters.forEach { param ->
                this.addAll(getGenericsDependencies(param.type))
                param.annotations.iterator().forEach {
                    this.addAll(getAnnotationDependencies(it))
                }
            }
        }
    }

}

private fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

/**
 * 过滤掉 Spring 注入的参数
 */
private fun filtrateSpringParameters(list: List<KSValueParameter>): List<KSValueParameter> {
    return list.filter {
        if (it.type.typeString() in arrayOf("HttpServletRequest", "HttpServletResponse")) {
            false
        } else {
            var spring = true
            for (annotation in it.annotations) {
                if (annotation.annotationType.qualifiedName() == "org.springframework.web.bind.annotation.RequestHeader" || annotation.annotationType.qualifiedName() == "org.springframework.web.bind.annotation.PathVariable") {
                    spring = false
                    break
                }
            }
            spring
        }
    }
}

private fun getMediaType(function: KSFunctionDeclaration): String {
    // BodyParam只能用于json参数
    if (function.hasAnnotation(BodyParam::class)) {
        return org.springframework.http.MediaType.APPLICATION_JSON_VALUE
    }
    // 非GET请求的Pageable注解等同于BodyParam
    if ((function.hasMethod("POST") ||
            function.hasMethod("PUT") ||
            function.hasMethod("DELETE")) && function.hasAnnotation(Pageable::class)) {
        return org.springframework.http.MediaType.APPLICATION_JSON_VALUE
    }
    filtrateSpringParameters(function.parameters).forEach {
        // 有文件参数一定是form-data
        if (it.type.resolve().declaration.qualifiedName!!.asString() == "org.springframework.web.multipart.MultipartFile") {
            return org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
        }
        // 使用spring默认的json注解
        if (it.hasAnnotation(org.springframework.web.bind.annotation.RequestBody::class)) {
            return org.springframework.http.MediaType.APPLICATION_JSON_VALUE
        }
    }
    return "none"
}

private fun isNullable(param: KSValueParameter): Boolean {
    param.annotations.forEach {
        if (it.annotationType.qualifiedName() in arrayOf(
            "javax.validation.constraints.NotNull",
            "javax.validation.constraints.NotBlank"
        )) {
            return false
        }
    }
    return true
}

@OptIn(KspExperimental::class)
private fun getParams(function: KSFunctionDeclaration): MutableList<Parameter> {
    val ret = mutableListOf<Parameter>()
    val pageable = function.getAnnotationsByType(Pageable::class).toList()
    var pageType = 0
    if (pageable.isNotEmpty()) {
        pageType = 1
        if (pageable[0].enablePaginateNot) {
            pageType = 2
        }
    }

    function.parameters.filter { it.type.hasAnnotation(PathVariable::class) }.forEach {
        val desc = it.getAnnotationsByType(Description::class).toList()
        ret.add(Parameter().apply {
            this.name = it.name!!.asString()
            this.required = !isNullable(it)
            this.`in` = "path"
            setInfo(it)
            if (desc.isNotEmpty()) {
                this.description = desc[0].value
            }
        })
    }
    if (function.hasMethod("GET")) {
        if (pageType == 1) {
            ret.add(Parameter().apply {
                this.name = "currentPage"
                this.required = true
                this.`in` = "query"
                this.type = "integer"
                this.schema = mapOf("type" to "integer")
            })
            ret.add(Parameter().apply {
                this.name = "pageSize"
                this.required = true
                this.`in` = "query"
                this.type = "integer"
                this.schema = mapOf("type" to "integer")
            })
        }

        if (pageType == 2) {
            ret.add(Parameter().apply {
                this.name = "currentPage"
                this.required = false
                this.`in` = "query"
                this.type = "integer"
                this.schema = mapOf("type" to "integer")
            })
            ret.add(Parameter().apply {
                this.name = "pageSize"
                this.required = false
                this.`in` = "query"
                this.type = "integer"
                this.schema = mapOf("type" to "integer")
            })
        }

        filtrateSpringParameters(function.parameters).forEach { paramType ->
            val desc = paramType.getAnnotationsByType(Description::class).toList()
            ret.add(Parameter().apply {
                this.name = paramType.name!!.asString()
                this.required = !isNullable(paramType)
                this.`in` = "query"
                setInfo(paramType)
                if (desc.isNotEmpty()) {
                    this.description = desc[0].value
                }
                paramType.annotations.toList().find { it.annotationType.qualifiedName() == "io.github.sagirikawaii01.centox.ksp.openapi" }?.let {
                    this.description = "enum:${(it.arguments[0].value as KSType).declaration.qualifiedName!!.asString()}:${if (this.description == null) { "" } else { this.description }}"
                }
            })
        }
    }
    return ret
}

private fun Parameter.setInfo(it: KSValueParameter) {
    if (it.type.typeString() in arrayOf("Int", "Long")) {
        this.type = "integer"
        this.schema = mapOf("type" to "integer")
    } else if (it.type.typeString() in arrayOf("Double", "Float")) {
        this.type = "number"
        this.schema = mapOf("type" to "number")
    } else if (it.type.typeString().startsWith("List<")) {
        this.type = "array"
        this.schema = mapOf("type" to "array")
    } else {
        this.type = "string"
        this.schema = mapOf("type" to "string")
    }
}

private fun KSFunctionDeclaration.getRequestBody(): KSTypeReference? {
    val list = this.parameters.filter { it.hasAnnotation(org.springframework.web.bind.annotation.RequestBody::class) }
    return if (list.isEmpty()) {
        null
    } else {
        list[0].type
    }
}

@OptIn(KspExperimental::class)
private fun KSFunctionDeclaration.getApiName(): String? {
    return if (this.hasAnnotation(ApiName::class)) {
        this.getAnnotationsByType(ApiName::class).toList()[0].value
    } else {
        null
    }
}

@OptIn(KspExperimental::class)
private fun KSFunctionDeclaration.hasMethod(method:String): Boolean {
    val methods = mutableListOf<String>()
    this.getAnnotationsByType(RequestMapping::class).toList().forEach {  anno ->
        anno.method.forEach {
            methods.add(it.name)
        }
    }
    if (methods.isNotEmpty() && methods.contains(method)) {
        return true
    }
    return when (method) {
        "GET" -> this.hasAnnotation(GetMapping::class)
        "POST" -> this.hasAnnotation(PostMapping::class)
        "PUT" -> this.hasAnnotation(PutMapping::class)
        "DELETE" -> this.hasAnnotation(DeleteMapping::class)
        else -> false
    }
}


private fun Any.toJson(): String {
    return JSON.toJSONString(this)
}