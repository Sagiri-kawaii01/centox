package io.github.sagiri_kawaii01.centox.starter

import io.github.sagiri_kawaii01.centox.mp.DslConfig
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:28
 * @since
 */
@ConfigurationProperties("centox")
class CentoxKtProperties {
    var dslPackageName: String? = null
}