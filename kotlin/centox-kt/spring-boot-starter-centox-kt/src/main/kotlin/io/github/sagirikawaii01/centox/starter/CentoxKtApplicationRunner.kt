package io.github.sagirikawaii01.centox.starter

import io.github.sagirikawaii01.centox.orika.Orika
import ma.glasnost.orika.MapperFacade
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/7 17:36
 * @since
 */
@Configuration
@ConditionalOnClass(MapperFacade::class)
@ConditionalOnBean(MapperFacade::class)
@AutoConfigureAfter(OrikaAutoConfigure::class)
open class CentoxKtApplicationRunner : ApplicationRunner {

    @Resource
    private lateinit var orikaMapperFacade: MapperFacade

    override fun run(args: ApplicationArguments?) {
        Orika.init(orikaMapperFacade)
    }
}