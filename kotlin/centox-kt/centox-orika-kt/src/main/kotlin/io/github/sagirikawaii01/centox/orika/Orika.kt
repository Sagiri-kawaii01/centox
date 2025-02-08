package io.github.sagirikawaii01.centox.orika

import ma.glasnost.orika.MapperFacade
import kotlin.reflect.KClass

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.1
 */
object Orika {
    lateinit var orikaMapperFacade: MapperFacade

    fun init(orikaMapperFacade: MapperFacade) {
        this.orikaMapperFacade = orikaMapperFacade
    }
}

infix fun <T, R : Any> T.convertTo(target: KClass<R>): R = Orika.orikaMapperFacade.map(this, target.java)

infix fun <T, R: Any> List<T>?.convertTo(target: KClass<R>): List<R> {
    return if (this.isNullOrEmpty()) {
        emptyList()
    } else {
        Orika.orikaMapperFacade.mapAsList(this, target.java)
    }
}

infix fun <T, R: Any> Set<T>?.convertTo(target: KClass<R>): Set<R> {
    return if (this.isNullOrEmpty()) {
        emptySet()
    } else {
        Orika.orikaMapperFacade.mapAsSet(this, target.java)
    }
}

infix fun <T, R: Any> T.copyTo(target: R): R {
    Orika.orikaMapperFacade.map(this, target)
    return target
}

infix fun <T, R: Any> T.copyTo(target: List<R>): List<R> {
    target.forEach {
        this copyTo it
    }
    return target
}

infix fun <T, R: Any> T.copyTo(target: Set<R>): Set<R> {
    target.forEach {
        this copyTo it
    }
    return target
}
