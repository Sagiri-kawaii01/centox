package io.github.sagiri_kawaii01.centox.orika.convert

import com.alibaba.fastjson2.to
import ma.glasnost.orika.CustomConverter
import ma.glasnost.orika.MappingContext
import ma.glasnost.orika.metadata.Type

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/4/19 13:50
 * @since
 */
class StringToListIntegerConverter: CustomConverter<String, List<Int>>() {
    override fun convert(
        source: String?,
        destinationType: Type<out List<Int>>?,
        mappingContext: MappingContext?
    ): List<Int> {
        if (source.isNullOrBlank()) {
            return emptyList()
        }
        if (source.startsWith("[") && source.endsWith("]")) {
            try {
                return source.to()
            } catch (ignored: Exception) {}
        }
        try {
            return source.split(",").map { it.trim().toInt() }
        } catch (ignored: Exception) {}
        return emptyList()
    }
}