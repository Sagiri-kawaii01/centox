package io.github.sagirikawaii01.centox.orika.convert

import com.alibaba.fastjson2.to
import ma.glasnost.orika.CustomConverter
import ma.glasnost.orika.MappingContext
import ma.glasnost.orika.metadata.Type

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/19 13:35
 * @since
 */
class StringToListStringConverter: CustomConverter<String, List<String>>() {
    override fun convert(
        source: String?,
        destinationType: Type<out List<String>>?,
        mappingContext: MappingContext?
    ): List<String> {
        if (source.isNullOrBlank()) {
            return emptyList()
        }
        if (source.startsWith("[") && source.endsWith("]")) {
            try {
                val tmp: List<Any> = source.to()
                return mutableListOf<String>().apply {
                    tmp.forEach {
                        this.add(it.toString())
                    }
                }
            } catch (ignored: Exception) {}
        }
        return source.split(",").map { it.trim() }
    }
}