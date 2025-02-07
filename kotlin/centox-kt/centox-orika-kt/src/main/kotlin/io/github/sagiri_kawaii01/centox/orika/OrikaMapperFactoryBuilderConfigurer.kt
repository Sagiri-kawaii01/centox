package io.github.sagiri_kawaii01.centox.orika

import ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.4
 */
interface OrikaMapperFactoryBuilderConfigurer {

    /**
     * Configures the [MapperFactoryBuilder].
     *
     * @param orikaMapperFactoryBuilder The [MapperFactoryBuilder].
     */
    fun configure(orikaMapperFactoryBuilder: MapperFactoryBuilder<*, *>)
    fun configure(orikaMapperFactoryBuilder: DataClassMapperFactory.MapperFactoryBuilder<*, *>)


}
