package io.github.sagiri_kawaii01.centox.starter

import io.github.sagiri_kawaii01.centox.orika.DataClassConstructorResolverStrategy
import io.github.sagiri_kawaii01.centox.orika.DataClassMapperFactory
import io.github.sagiri_kawaii01.centox.orika.OrikaMapperFactoryBuilderConfigurer
import io.github.sagiri_kawaii01.centox.orika.OrikaMapperFactoryConfigurer
import io.github.sagiri_kawaii01.centox.orika.convert.StringToListIntegerConverter
import io.github.sagiri_kawaii01.centox.orika.convert.StringToListStringConverter
import ma.glasnost.orika.MapperFacade
import ma.glasnost.orika.MapperFactory
import ma.glasnost.orika.impl.DefaultMapperFactory
import ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import javax.annotation.Resource


/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/4 15:01
 * @since
 */
@Configuration(proxyBeanMethods = false)
open class OrikaAutoConfigure {

    @Resource
    lateinit var properties: CentoxKtProperties

    /**
     * Provides the [MapperFactoryBuilder].
     *
     * @param orikaProperties The configuration properties for Orika.
     * @param orikaMapperFactoryBuilderConfigurers The configurers for [MapperFactoryBuilder].
     * @return The [MapperFactoryBuilder].
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(10)
    open fun orikaMapperFactoryBuilderK(
        orikaMapperFactoryBuilderConfigurers: List<OrikaMapperFactoryBuilderConfigurer>,
    ): DataClassMapperFactory.MapperFactoryBuilder<*, *> {
        val orikaMapperFactoryBuilder = DataClassMapperFactory.Builder()
        orikaMapperFactoryBuilder.constructorResolverStrategy(DataClassConstructorResolverStrategy())
        properties.orika.useBuiltinConverters.also { orikaMapperFactoryBuilder.useBuiltinConverters(it) }
        properties.orika.useAutoMapping.also { orikaMapperFactoryBuilder.useAutoMapping(it) }
        properties.orika.mapNulls.also { orikaMapperFactoryBuilder.mapNulls(it) }
        properties.orika.dumpStateOnException.also { orikaMapperFactoryBuilder.dumpStateOnException(it) }
        properties.orika.favorExtension?.also { orikaMapperFactoryBuilder.favorExtension(it) }
        properties.orika.captureFieldContext.also { orikaMapperFactoryBuilder.captureFieldContext(it) }
        orikaMapperFactoryBuilderConfigurers.forEach { it.configure(orikaMapperFactoryBuilder) }
        log.debug("Providing the {}: {}", MapperFactoryBuilder::class.simpleName, orikaMapperFactoryBuilder)
        return orikaMapperFactoryBuilder
    }

    /**
     * Provides the [MapperFactory].
     *
     * @param orikaMapperFactoryBuilder The [MapperFactoryBuilder].
     * @param orikaMapperFactoryConfigurers The configurers for [MapperFactory].
     * @return The [MapperFactory].
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(10)
    @ConditionalOnProperty(prefix = "ibdw.orika", name = ["lan"], havingValue = "kotlin")
    open fun orikaMapperFactoryK(
        orikaMapperFactoryBuilder: DataClassMapperFactory.MapperFactoryBuilder<*, *>,
        orikaMapperFactoryConfigurers: List<OrikaMapperFactoryConfigurer>,
    ): MapperFactory {
        val orikaMapperFactory = orikaMapperFactoryBuilder.build()
        orikaMapperFactoryConfigurers.forEach { it.configure(orikaMapperFactory) }
        log.debug("Providing the {}: {}", MapperFactory::class.simpleName, orikaMapperFactory)
        orikaMapperFactory.converterFactory.registerConverter(StringToListStringConverter())
        orikaMapperFactory.converterFactory.registerConverter(StringToListIntegerConverter())
        return orikaMapperFactory
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(10)
    @ConditionalOnProperty(prefix = "ibdw.orika", name = ["lan"], havingValue = "java", matchIfMissing = true)
    open fun orikaMapperFactoryBuilderJ(
        orikaMapperFactoryBuilderConfigurers: List<OrikaMapperFactoryBuilderConfigurer>,
    ): MapperFactoryBuilder<*, *> {
        val orikaMapperFactoryBuilder = DefaultMapperFactory.Builder()
        properties.orika.useBuiltinConverters.also { orikaMapperFactoryBuilder.useBuiltinConverters(it) }
        properties.orika.useAutoMapping.also { orikaMapperFactoryBuilder.useAutoMapping(it) }
        properties.orika.mapNulls.also { orikaMapperFactoryBuilder.mapNulls(it) }
        properties.orika.dumpStateOnException.also { orikaMapperFactoryBuilder.dumpStateOnException(it) }
        properties.orika.favorExtension.also { orikaMapperFactoryBuilder.favorExtension(it) }
        properties.orika.captureFieldContext.also { orikaMapperFactoryBuilder.captureFieldContext(it) }
        orikaMapperFactoryBuilderConfigurers.forEach { it.configure(orikaMapperFactoryBuilder) }
        log.debug("Providing the {}: {}", MapperFactoryBuilder::class.simpleName, orikaMapperFactoryBuilder)
        return orikaMapperFactoryBuilder
    }

    /**
     * Provides the [MapperFactory].
     *
     * @param orikaMapperFactoryBuilder The [MapperFactoryBuilder].
     * @param orikaMapperFactoryConfigurers The configurers for [MapperFactory].
     * @return The [MapperFactory].
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(10)
    @ConditionalOnProperty(prefix = "ibdw.orika", name = ["lan"], havingValue = "java", matchIfMissing = true)
    open fun orikaMapperFactoryJ(
        orikaMapperFactoryBuilder: MapperFactoryBuilder<*, *>,
        orikaMapperFactoryConfigurers: List<OrikaMapperFactoryConfigurer>,
    ): MapperFactory {
        val orikaMapperFactory = orikaMapperFactoryBuilder.build()
        orikaMapperFactoryConfigurers.forEach { it.configure(orikaMapperFactory) }
        log.debug("Providing the {}: {}", MapperFactory::class.simpleName, orikaMapperFactory)
        return orikaMapperFactory
    }

    /**
     * Provides the [MapperFacade].
     *
     * @param orikaMapperFactory The [MapperFactory].
     * @return The [MapperFacade].
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(100)
    open fun orikaMapperFacade(orikaMapperFactory: MapperFactory): MapperFacade {
        val orikaMapperFacade = orikaMapperFactory.mapperFacade
        log.debug("Providing the {}: {}", MapperFacade::class.simpleName, orikaMapperFacade)
        return orikaMapperFacade
    }

    companion object {

        /**
         * The logger.
         */
        private val log: Logger = LoggerFactory.getLogger(OrikaAutoConfigure::class.java)

    }
}