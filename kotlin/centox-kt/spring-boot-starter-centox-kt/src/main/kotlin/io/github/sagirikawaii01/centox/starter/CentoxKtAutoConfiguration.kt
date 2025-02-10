package io.github.sagirikawaii01.centox.starter

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import io.github.sagirikawaii01.centox.mp.DslConfig
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:27
 * @since
 */
@EnableConfigurationProperties(CentoxKtProperties::class)
@ConditionalOnClass(MetaObjectHandler::class)
@AutoConfigureBefore(MybatisPlusAutoConfiguration::class)
open class CentoxKtAutoConfiguration {
    @Resource
    lateinit var properties: CentoxKtProperties

    @Bean
    @ConditionalOnMissingBean
    open fun dslConfig(): DslConfig {
        return DslConfig(properties.dslPackageName!!)
    }
}