package io.github.sagirikawaii01.centox.ksp.openapi

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isDefault
import com.google.devtools.ksp.symbol.*
import org.springframework.web.bind.annotation.*
import kotlin.reflect.KClass

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/14 15:23
 * @since 1.0.0
 */
fun StringBuilder.line(str: String) {
    this.append(str)
    this.append("\n")
}

fun StringBuilder.nextLine() {
    this.append("\n")
}

/**
 * 获取某个普通类型（包含泛型）的依赖全限定名
 * @param type
 */
internal fun getGenericsDependencies(type: KSTypeReference): Set<String> {
    if (type.resolve().arguments.isEmpty()) {
        return setOf(type.resolve().declaration.qualifiedName!!.asString())
    }
    val ret = mutableSetOf<String>()
    for (argument in type.resolve().arguments) {
        ret.addAll(getGenericsDependencies(argument.type!!))
    }
    return ret
}

// 从方法获取其接口注解并得到所有的method:path字符串
@OptIn(KspExperimental::class)
internal fun KSFunctionDeclaration.paths(): Set<String> {
    val basePath = mutableSetOf<String>()
    (this.parent as KSClassDeclaration).getAnnotationsByType(RequestMapping::class).toList()[0].path.forEach {
        basePath.add("/${it.removeSuffix("/").removePrefix("/")}")
    }
    (this.parent as KSClassDeclaration).getAnnotationsByType(RequestMapping::class).toList()[0].value.forEach {
        basePath.add("/${it.removeSuffix("/").removePrefix("/")}")
    }

    val path = mutableSetOf<String>()
    val get = this.getAnnotationsByType(GetMapping::class).toList()
    if (get.isNotEmpty()) {
        basePath.forEach {  base ->
            get[0].path.toMutableList().apply {
                this.addAll(get[0].value)
            }.forEach { sub ->
                path.add("GET:$base/${sub.let {
                    if (sub.contains("{")) {
                        sub.substring(0, sub.indexOf("{"))
                    } else {
                        sub
                    }
                }.removePrefix("/").removePrefix("/")}")
            }
        }
    }

    val post = this.getAnnotationsByType(PostMapping::class).toList()
    if (post.isNotEmpty()) {
        basePath.forEach {  base ->
            post[0].path.toMutableList().apply {
                this.addAll(post[0].value)
            }.forEach { sub ->
                path.add("POST:$base/${sub.let {
                    if (sub.contains("{")) {
                        sub.substring(0, sub.indexOf("{"))
                    } else {
                        sub
                    }
                }.removePrefix("/").removePrefix("/")}")
            }
        }
    }

    val put = this.getAnnotationsByType(PutMapping::class).toList()
    if (put.isNotEmpty()) {
        basePath.forEach {  base ->
            put[0].path.toMutableList().apply {
                this.addAll(put[0].value)
            }.forEach { sub ->
                path.add("PUT:$base/${sub.let {
                    if (sub.contains("{")) {
                        sub.substring(0, sub.indexOf("{"))
                    } else {
                        sub
                    }
                }.removePrefix("/").removePrefix("/")}")
            }
        }
    }

    val delete = this.getAnnotationsByType(DeleteMapping::class).toList()
    if (delete.isNotEmpty()) {
        basePath.forEach {  base ->
            delete[0].path.toMutableList().apply {
                this.addAll(delete[0].value)
            }.forEach { sub ->
                path.add("DELETE:$base/${sub.let {
                    if (sub.contains("{")) {
                        sub.substring(0, sub.indexOf("{"))
                    } else {
                        sub
                    }
                }.removePrefix("/").removePrefix("/")}")
            }
        }
    }

    val req = this.getAnnotationsByType(RequestMapping::class).toList()
    if (req.isNotEmpty()) {
        basePath.forEach {  base ->
            req[0].path.forEach { sub ->
                req[0].method.let {
                    if (it.isEmpty()) {
                        arrayOf(RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT)
                    } else {
                        it
                    }
                }.forEach {
                    path.add("${it.name}:$base/${sub.let {
                        if (sub.contains("{")) {
                            sub.substring(0, sub.indexOf("{"))
                        } else {
                            sub
                        }
                    }.removePrefix("/").removePrefix("/")}")
                }

            }
        }
    }

    return path
}

/**
 * 获取某个注解类型（包含参数）的依赖全限定名
 *
 * @param annotation
 * @return
 */
internal fun getAnnotationDependencies(annotation: KSAnnotation): Set<String> {
    val ret = mutableSetOf<String>()
    val qualifiedName = annotation.annotationType.resolve().declaration.qualifiedName!!.asString()
    if (!qualifiedName.startsWith("io.github.sagirikawaii01.centox.ksp.openapi.ApiParams")) {
        ret.add(qualifiedName)
    }
    annotation.arguments.forEach {
        if (it.value!!.javaClass.isEnum) {
            ret.add("${it.value!!::class.qualifiedName}.*")
        } else if (it.value!!.javaClass.isArray || Collection::class.java.isAssignableFrom(it.value!!.javaClass)) {
            if ((it.value as List<*>).isNotEmpty() && (((it.value as List<*>)[0] as KSType).declaration as KSClassDeclaration).classKind == ClassKind.ENUM_ENTRY) {
                ret.add("${((((it.value as List<*>)[0] as KSType).declaration as KSClassDeclaration).parent as KSDeclaration).qualifiedName!!.asString()}.*")
            }
        } else {
            if (it.value is KSType) {
                val declaration = (it.value as KSType).declaration as KSClassDeclaration
                if (declaration.classKind == ClassKind.ENUM_CLASS || declaration.classKind == ClassKind.CLASS) {
                    ret.add(declaration.qualifiedName!!.asString())
                }
            } else {
                ret.add("${it.value!!::class.qualifiedName}")
            }
        }
    }
    return ret
}

/**
 * Is collection
 *
 * @return
 */
internal fun KSClassDeclaration.isCollection(): Boolean {
    for (superType in this.superTypes) {
        if (superType.resolve().declaration.qualifiedName!!.asString() == Collection::class.qualifiedName) {
            return true
        }
    }
    return false
}

/**
 * 获取带泛型的类型字符串
 * @return "YourClass<Type1, Type2, ...>"
 */
internal fun getGenericsTypeString(type: KSTypeReference): String {
    if (type.resolve().arguments.isEmpty()) {
        return type.resolve().declaration.simpleName.asString()
    }
    var ret = "${type.resolve().declaration.simpleName.asString()}<"
    for (argument in type.resolve().arguments) {
        ret = "$ret${getGenericsTypeString(argument.type!!)}, "
    }
    ret = ret.removeRange(ret.length - 2, ret.length)
    return "$ret>"
}

/**
 * 获取带泛型的类型字符串
 * @return "YourClass<Type1, Type2, ...>"
 */
internal fun KSTypeReference.typeString(): String {
    return getGenericsTypeString(this)
}

/**
 * 获取注解的原始字符串（但参数只能为基本类型、数组、列表、枚举）
 * @return
 */
internal fun KSAnnotation.originCode(): String {
    var code = "@${this.shortName.asString()}"
    val map = mutableMapOf<String, String>()

    this.arguments.forEach { argument ->
        if (argument.isDefault()) {
            return@forEach
        }
        if (argument.value is Array<*> && (argument.value as Array<*>).isNotEmpty()) {
            map[argument.name!!.asString()] = arrayAnnotationCode(argument.value as Array<*>)
        } else if (argument.value is List<*> && (argument.value as List<*>).isNotEmpty()) {
            map[argument.name!!.asString()] = arrayAnnotationCode(argument.value as List<*>)
        } else {
            map[argument.name!!.asString()] = valueAnnotationCode(argument.value!!)
        }
    }
    if (map.isNotEmpty()) {
        code = "$code("
        map.forEach { (k, v) ->
            code = "$code$k = $v, "
        }
        code = "${code.removeSuffix(", ")})"
    }
    return code
}

internal fun KSTypeReference.qualifiedName(): String {
    return this.resolve().declaration.qualifiedName!!.asString()
}

@OptIn(KspExperimental::class)
internal fun <T : Annotation> KSAnnotated.hasAnnotation(annotationKClass: KClass<T>): Boolean {
    return this.getAnnotationsByType(annotationKClass).toList().isNotEmpty()
}

internal fun KSTypeReference.simpleName(): String {
    return this.resolve().declaration.simpleName.asString()
}

private fun arrayAnnotationCode(data: Array<*>): String {
    return iteratorAnnotationCode(data.iterator())
}

private fun arrayAnnotationCode(data: List<*>): String {
    return iteratorAnnotationCode(data.iterator())
}

internal fun iteratorAnnotationCode(data: Iterator<*>): String {
    return if (!data.hasNext()) {
        "[]"
    } else {
        var str = ""
        data.forEach {
            str = "$str, ${typeAnnotationCode(it as KSType)}"
        }
        "[${str.replaceFirst(", ", "")}]"
    }
}

private fun typeAnnotationCode(type: KSType): String {
    return if ((type.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_ENTRY) {
        type.declaration.simpleName.asString()
    } else if (type.declaration.qualifiedName!!.asString() == String::class.qualifiedName) {
        "\"$type\""
    } else if (type.declaration.qualifiedName!!.asString() == Boolean::class.qualifiedName) {
        "$type"
    } else {
        throw CodeGenerationException("注解参数只能是字符串、基本类型或枚举类型: ${type.declaration.simpleName.asString()}")
    }
}

internal fun getGenericsType(type: KSTypeReference): String {
    if (type.resolve().arguments.isEmpty()) {
        return type.resolve().declaration.simpleName.asString()
    }
    var ret = "${type.resolve().declaration.simpleName.asString()}<"
    for (argument in type.resolve().arguments) {
        ret = "$ret${getGenericsType(argument.type!!)}, "
    }
    ret = ret.removeRange(ret.length - 2, ret.length)
    return "$ret>"
}

private fun valueAnnotationCode(value: Any): String {
    return when (value) {
        is String -> {
            "\"$value\""
        }
        is Boolean, is Number -> {
            value.toString()
        }
        is ArrayList<*> -> {
            arrayAnnotationCode(value as List<*>)
        }
        is KSType -> {
            val declaration = value.declaration as KSClassDeclaration
            if (declaration.classKind == ClassKind.CLASS || declaration.classKind == ClassKind.ENUM_CLASS) {
                "${declaration.simpleName.asString()}::class"
            } else {
                throw CodeGenerationException("注解参数只能是字符串、基本类型或枚举类型: ${value::class.simpleName}")
            }
        }
        else -> throw CodeGenerationException("注解参数只能是字符串、基本类型或枚举类型: ${value::class.simpleName}")
    }
}
