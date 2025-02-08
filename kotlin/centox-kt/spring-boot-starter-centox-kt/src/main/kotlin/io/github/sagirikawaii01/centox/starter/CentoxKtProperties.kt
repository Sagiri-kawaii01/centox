package io.github.sagirikawaii01.centox.starter

import io.github.sagirikawaii01.centox.mp.DslConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:28
 * @since
 */
@ConfigurationProperties("centox")
class CentoxKtProperties {
    var dslPackageName: String? = null

    @NestedConfigurationProperty
    var orika: CentoxOrikaProperties = CentoxOrikaProperties()
}

class CentoxOrikaProperties {
    var useBuiltinConverters = true
    var useAutoMapping = true
    var mapNulls = false
    var dumpStateOnException = false
    var favorExtension = false
    var captureFieldContext = false
}