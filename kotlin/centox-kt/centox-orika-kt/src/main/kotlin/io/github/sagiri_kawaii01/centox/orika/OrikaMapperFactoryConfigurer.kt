package io.github.sagiri_kawaii01.centox.orika

import ma.glasnost.orika.MapperFactory

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.4
 */
interface OrikaMapperFactoryConfigurer {

    /**
     * Configures the [MapperFactory].
     *
     * @param orikaMapperFactory The [MapperFactory].
     */
    fun configure(orikaMapperFactory: MapperFactory)

}
