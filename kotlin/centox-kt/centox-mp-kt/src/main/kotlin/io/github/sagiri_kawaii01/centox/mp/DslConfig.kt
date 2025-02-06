package io.github.sagiri_kawaii01.centox.mp

import com.baomidou.mybatisplus.annotation.TableField
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy
import net.bytebuddy.dynamic.scaffold.TypeValidation
import net.bytebuddy.matcher.ElementMatchers.named
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.AssignableTypeFilter
import kotlin.reflect.KClass

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:21
 * @since
 */
open class DslConfig(
    packageName: String
) {
    init {
        val classes = findImplementations(packageName, SqlDsl::class)
        ByteBuddyAgent.install()
        val anno = AnnotationDescription.Builder.ofType(TableField::class.java)
            .define("exist", false)
            .build()
        for (clazz in classes) {
            ByteBuddy()
                .with(TypeValidation.DISABLED)
                .redefine(clazz)
                .field(named("\$\$delegate_0"))
                .annotateField(anno)
                .make()
                .load(clazz.classLoader, ClassReloadingStrategy.fromInstalledAgent())
        }
    }

    private fun findImplementations(basePackage: String, interfaceClass: KClass<*>): List<Class<*>> {
        val provider = ClassPathScanningCandidateComponentProvider(false)
        provider.addIncludeFilter(object : AssignableTypeFilter(interfaceClass.java) {
            override fun match(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
                return super.match(metadataReader, metadataReaderFactory) && !metadataReader.classMetadata.isInterface
            }
        })

        return provider.findCandidateComponents(basePackage)
            .map { Class.forName(it.beanClassName) }
    }
}