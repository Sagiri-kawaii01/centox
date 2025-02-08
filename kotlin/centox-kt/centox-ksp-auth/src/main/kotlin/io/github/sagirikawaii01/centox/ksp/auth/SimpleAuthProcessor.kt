package io.github.sagirikawaii01.centox.ksp.auth

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import io.github.sagirikawaii01.centox.ksp.auth.AuthAnnotation
import org.springframework.boot.autoconfigure.SpringBootApplication


/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/12 17:54
 * @since
 */
class SimpleAuthProcessor(environment: SymbolProcessorEnvironment): SymbolProcessor {
    private var log: KSPLogger = environment.logger
    private var codeGenerator = environment.codeGenerator
    private var fullPackagePath = ""
    private var annoClassName: String? = null
    private var annoClassSimpleName = ""
    private var tokenHeader = ""
    private var tokenPrefix = ""
    private var rolePropertyName = ""

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val roleClass = resolver.getSymbolsWithAnnotation(AuthRole::class.qualifiedName!!).toList().filter { it is KSValueParameter }

        if (roleClass.size > 1) {
            throw AuthCodeGenerationException("Only one class can be annotated with @AuthRole")
        } else if (roleClass.isNotEmpty()) {
            roleClass.filter { it.validate() }
                .forEach { it.accept(RoleVisitorVoid(), Unit) }
        }
        val ret = roleClass.filter { !it.validate() }.toMutableList()

        val annoClass = resolver.getSymbolsWithAnnotation(AuthAnnotation::class.qualifiedName!!).toList().filter { it is KSClassDeclaration }
        if (annoClass.size > 1) {
            throw AuthCodeGenerationException("Only one class can be annotated with @AuthAnnotation")
        } else if (annoClass.isNotEmpty()) {
            annoClass.filter { it.validate() }
                .forEach { it.accept(AnnotationVisitorVoid(), Unit) }
        }
        ret.addAll(annoClass.filter { !it.validate() })

        val appClass = resolver.getSymbolsWithAnnotation(SpringBootApplication::class.qualifiedName!!).toList()
        if (appClass.size > 1) {
            throw AuthCodeGenerationException("Only one class can be annotated with @SpringbootApplication")
        } else if (appClass.isNotEmpty()) {
            appClass.filter { it is KSClassDeclaration && it.validate() }
                .forEach { it.accept(ApplicationVisitorVoid(), Unit) }
        }
        ret.addAll(appClass.filter { !it.validate() })
        return ret
    }

    inner class RoleVisitorVoid: KSVisitorVoid() {
        override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) {
            rolePropertyName = valueParameter.name!!.getShortName()
            if ("kotlin.Array" != valueParameter.type.resolve().declaration.qualifiedName!!.asString()) {
                throw AuthCodeGenerationException("The property annotated with @AuthRole must be an array")
            }
            val type = valueParameter.type.resolve().arguments[0].type!!
            var isRole = false
            for (superType in (type.resolve().declaration as KSClassDeclaration).superTypes) {
                if (superType.resolve().declaration.qualifiedName!!.asString() == "io.github.sagirikawaii01.centox.ksp.auth.Role") {
                    isRole = true
                    break
                }
            }
            if (!isRole) {
                throw AuthCodeGenerationException("The property annotated with @AuthRole must be an array of Role")
            }
        }
    }

    inner class AnnotationVisitorVoid: KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            annoClassName = classDeclaration.qualifiedName?.asString()
            annoClassSimpleName = classDeclaration.simpleName.asString()
            for (annotation in classDeclaration.annotations) {
                if (annotation.shortName.asString() == "AuthAnnotation") {
                    tokenHeader = annotation.arguments[0].value.toString()
                    tokenPrefix = annotation.arguments[1].value.toString()
                }
            }

        }
    }

    inner class ApplicationVisitorVoid: KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            fullPackagePath = "${classDeclaration.packageName.asString()}.cfg"
        }
    }

    override fun finish() {
        if (rolePropertyName.isNotBlank() && annoClassSimpleName.isNotBlank()) {
            generateAspect()
            generateFilter()
        }

        super.finish()
    }

    private fun generateAspect() {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = fullPackagePath,
            fileName = "CentoxRolePermissionAspect"
        )

        val code = StringBuilder().apply {
            append("""
                package $fullPackagePath

                import org.aspectj.lang.JoinPoint
                import org.aspectj.lang.annotation.Aspect
                import org.aspectj.lang.annotation.Before
                import org.aspectj.lang.reflect.MethodSignature
                import org.slf4j.MDC
                import io.github.sagirikawaii01.centox.x.exception.UnauthenticatedException
                import io.github.sagirikawaii01.centox.x.exception.UnauthorizedException
                import org.springframework.stereotype.Component
                import io.github.sagirikawaii01.centox.ksp.auth.Role
                import io.github.sagirikawaii01.centox.ksp.auth.Token
                import io.github.sagirikawaii01.centox.ksp.auth.AuthCheck
                import $annoClassName
                
                @Aspect
                @Component
                open class CentoxRolePermissionAspect(private val authCheck: AuthCheck) {
                    @Before("@annotation(${annoClassName}) || @within(${annoClassName})")
                    fun before(jp: JoinPoint) {
                        val token = MDC.get("token")
                        if (null == token) {
                            throw UnauthenticatedException()
                        } else {
                            var tokenObj: Token? = null
                            try {
                                tokenObj = this.authCheck.check(token.replaceFirst("$tokenPrefix", "").trim())
                            } catch (e: Exception) {
                                throw UnauthenticatedException()
                            }
                            
                            if (null == tokenObj) {
                                throw UnauthenticatedException()
                            }
                            
                            val signature = jp.signature
                            val value = ((signature as MethodSignature).method.getAnnotation(${annoClassSimpleName}::class.java)).${rolePropertyName}.toList()
                            val roles = tokenObj.getRoles()
                            
                            if (value.isNotEmpty()) {
                                for (role in value) {
                                    if (roles.contains(role)) {
                                        return
                                    }
                                }
                            } else {
                                return
                            }
                            
                            throw UnauthorizedException()
                        }
                    }
                }
            """.trimIndent())
        }

        file.write(code.toString().toByteArray())
        file.close()
    }

    private fun generateFilter() {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = fullPackagePath,
            fileName = "CentoxRolePermissionFilter"
        )

        val code = StringBuilder().apply {
            append("""
                package $fullPackagePath
                
                import java.io.IOException
                import javax.servlet.Filter
                import javax.servlet.FilterChain
                import javax.servlet.ServletException
                import javax.servlet.http.HttpServletRequest
                import javax.servlet.http.HttpServletResponse
                import org.slf4j.MDC
                import org.springframework.web.filter.OncePerRequestFilter
                import org.springframework.stereotype.Component
                
                @Component
                open class CentoxRolePermissionFilter: OncePerRequestFilter(), Filter {
                
                    @Throws(ServletException::class, IOException::class)
                    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
                        try {
                            MDC.put("token", request.getHeader("$tokenHeader"))
                            filterChain.doFilter(request, response)
                        } finally {
                            MDC.clear()
                        }
                    }
                }
            """.trimIndent())
        }

        file.write(code.toString().toByteArray())
        file.close()
    }
}