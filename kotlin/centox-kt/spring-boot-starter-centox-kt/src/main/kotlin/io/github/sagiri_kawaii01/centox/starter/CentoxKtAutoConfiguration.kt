package io.github.sagiri_kawaii01.centox.starter

import io.github.sagiri_kawaii01.centox.mp.DslConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import javax.annotation.Resource

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:27
 * @since
 */
@EnableConfigurationProperties(CentoxKtProperties::class)
class CentoxKtAutoConfiguration {
    @Resource
    lateinit var properties: CentoxKtProperties

    @Bean
    @ConditionalOnClass(name = ["com.baomidou.mybatisplus.core.handlers.MetaObjectHandler"])
    @ConditionalOnMissingBean
    fun dslConfig(): DslConfig {
        return DslConfig(properties.dslPackageName!!)
    }
}