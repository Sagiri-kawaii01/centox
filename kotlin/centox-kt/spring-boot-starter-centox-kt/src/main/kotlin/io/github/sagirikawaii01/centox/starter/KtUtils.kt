package io.github.sagirikawaii01.centox.starter

import com.alibaba.fastjson2.JSON
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.Throws

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:40
 * @since
 */
val TimePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val DatePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val DateTimePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/**
 * To json
 * 空对象返回 {}
 */
fun Any?.toJson(): String {
    this?.let {
        return JSON.toJSONString(this)
    }?:let {
        return "{}"
    }
}

/**
 * Uuid
 */
fun uuid(): String {
    return UUID.randomUUID().toString()
}

/**
 * 时间戳转LocalDateTime
 */
fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.ofEpochSecond(this)
    val zone = ZoneId.systemDefault()
    return LocalDateTime.ofInstant(instant, zone)
}

/**
 * Now
 * @return
 */
fun now(): LocalDateTime {
    return LocalDateTime.now()
}

@OptIn(ExperimentalContracts::class)
inline fun Any?.isNull(): Boolean {
    contract {
        returns(true) implies (this@isNull == null)
        returns(false) implies (this@isNull != null)
    }
    return null == this
}

@OptIn(ExperimentalContracts::class)
inline fun Any?.isNotNull(): Boolean {
    contract {
        returns(false) implies (this@isNotNull == null)
        returns(true) implies (this@isNotNull != null)
    }
    return null != this
}

/**
 * 为所有类添加log扩展属性
 */
val <reified T> T.logger: Logger
    inline get() = LoggerFactory.getLogger(T::class.java)

fun today(): LocalDate {
    return LocalDate.now()
}

fun doNothing() {

}

/**
 * To format string
 * @param pattern HH:mm:ss
 * @return
 */
fun LocalTime.toFormatString(pattern: DateTimeFormatter = TimePattern): String {
    return this.format(pattern)
}

/**
 * To format string
 * @param pattern yyyy-MM-dd
 * @return
 */
fun LocalDate.toFormatString(pattern: DateTimeFormatter = DatePattern): String {
    return this.format(pattern)
}

/**
 * To format string
 * @param pattern yyyy-MM-dd HH:mm:ss
 * @return
 */
fun LocalDateTime.toFormatString(pattern: DateTimeFormatter = DateTimePattern): String {
    return this.format(pattern)
}

/**
 * To local date time
 * @param pattern yyyy-MM-dd HH:mm:ss
 * @return
 */
fun String.toLocalDateTime(pattern: DateTimeFormatter = DateTimePattern): LocalDateTime {
    return LocalDateTime.parse(this, pattern)
}

/**
 * To local date
 * @param pattern yyyy-MM-dd
 * @return
 */
fun String.toLocalDate(pattern: DateTimeFormatter = DatePattern): LocalDate {
    return LocalDate.parse(this, pattern)
}

/**
 * To local time
 * @param pattern HH:mm:ss
 * @return
 */
fun String.toLocalTime(pattern: DateTimeFormatter = TimePattern): LocalTime {
    return LocalTime.parse(this, pattern)
}

@OptIn(ExperimentalContracts::class)
inline fun String?.isNotNullOrBlank(): Boolean {
    contract {
        returns(false) implies (this@isNotNullOrBlank == null)
    }
    return !this.isNullOrBlank()
}
