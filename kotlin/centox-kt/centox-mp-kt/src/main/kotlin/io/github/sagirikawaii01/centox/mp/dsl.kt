package io.github.sagirikawaii01.centox.mp

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KProperty

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/8/21 13:51
 * @since
 */
interface SqlDsl<T> {
    infix fun <K> KProperty<K?>.set(value: String?): _Condition
    infix fun KProperty<Int?>.set(value: Int?): _Condition
    infix fun KProperty<Double?>.set(value: Double?): _Condition
    infix fun KProperty<Long?>.set(value: Long?): _Condition
    infix fun KProperty<Float?>.set(value: Float?): _Condition
    infix fun KProperty<Boolean?>.set(value: Boolean?): _Condition
    infix fun KProperty<LocalDate?>.set(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.set(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.set(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.eq(value: String?): _Condition
    infix fun KProperty<Int?>.eq(value: Int?): _Condition
    infix fun KProperty<Double?>.eq(value: Double?): _Condition
    infix fun KProperty<Long?>.eq(value: Long?): _Condition
    infix fun KProperty<Float?>.eq(value: Float?): _Condition
    infix fun KProperty<Boolean?>.eq(value: Boolean?): _Condition
    infix fun KProperty<LocalDate?>.eq(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.eq(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.eq(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.ne(value: String?): _Condition
    infix fun KProperty<Int?>.ne(value: Int?): _Condition
    infix fun KProperty<Double?>.ne(value: Double?): _Condition
    infix fun KProperty<Long?>.ne(value: Long?): _Condition
    infix fun KProperty<Float?>.ne(value: Float?): _Condition
    infix fun KProperty<Boolean?>.ne(value: Boolean?): _Condition
    infix fun KProperty<LocalDate?>.ne(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.ne(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.ne(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.ge(value: String?): _Condition
    infix fun KProperty<Int?>.ge(value: Int?): _Condition
    infix fun KProperty<Double?>.ge(value: Double?): _Condition
    infix fun KProperty<Long?>.ge(value: Long?): _Condition
    infix fun KProperty<Float?>.ge(value: Float?): _Condition
    infix fun KProperty<LocalDate?>.ge(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.ge(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.ge(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.gt(value: String?): _Condition
    infix fun KProperty<Int?>.gt(value: Int?): _Condition
    infix fun KProperty<Double?>.gt(value: Double?): _Condition
    infix fun KProperty<Long?>.gt(value: Long?): _Condition
    infix fun KProperty<Float?>.gt(value: Float?): _Condition
    infix fun KProperty<LocalDate?>.gt(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.gt(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.gt(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.le(value: String?): _Condition
    infix fun KProperty<Int?>.le(value: Int?): _Condition
    infix fun KProperty<Double?>.le(value: Double?): _Condition
    infix fun KProperty<Long?>.le(value: Long?): _Condition
    infix fun KProperty<Float?>.le(value: Float?): _Condition
    infix fun KProperty<LocalDate?>.le(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.le(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.le(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.lt(value: String?): _Condition
    infix fun KProperty<Int?>.lt(value: Int?): _Condition
    infix fun KProperty<Double?>.lt(value: Double?): _Condition
    infix fun KProperty<Long?>.lt(value: Long?): _Condition
    infix fun KProperty<Float?>.lt(value: Float?): _Condition
    infix fun KProperty<LocalDate?>.lt(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.lt(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.lt(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.like(value: String?): _Condition
    infix fun KProperty<LocalDate?>.like(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.like(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.like(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.notLike(value: String?): _Condition
    infix fun KProperty<LocalDate?>.notLike(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.notLike(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.notLike(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.likeLeft(value: String?): _Condition
    infix fun KProperty<LocalDate?>.likeLeft(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.likeLeft(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.likeLeft(value: LocalTime?): _Condition
    infix fun <K> KProperty<K?>.likeRight(value: String?): _Condition
    infix fun KProperty<LocalDate?>.likeRight(value: LocalDate?): _Condition
    infix fun KProperty<LocalDateTime?>.likeRight(value: LocalDateTime?): _Condition
    infix fun KProperty<LocalTime?>.likeRight(value: LocalTime?): _Condition
    infix fun <E> KProperty<E>.`in`(value: Collection<E>): _Condition
    infix fun <E> KProperty<E>.notIn(value: Collection<E>): _Condition
    infix fun <E> KProperty<E>.between(value: Pair<E, E>): _Condition
    infix fun <E> KProperty<E>.notBetween(value: Pair<E, E>): _Condition
    fun orderByAsc(vararg properties: KProperty<*>)
    fun orderByDesc(vararg properties: KProperty<*>)
    fun last(sql: String)
    fun KProperty<*>.isNull(): _Condition
    fun KProperty<*>.notNull(): _Condition

    fun and(condition: Boolean = true, init: T.() -> Unit)
    fun or(condition: Boolean = true, init: T.() -> Unit)
    fun dsl(): SqlDsl<T>
}

@Suppress("ClassName")
enum class _WrapperDslType {
    Eq,
    Neq,
    Gt,
    Gte,
    Lt,
    Lte,
    Like,
    NotLike,
    LikeLeft,
    LikeRight,
    In,
    NotIn,
    Between,
    NotBetween,
    IsNull,
    IsNotNull,
    And,
    AndEnd,
    Or,
    OrEnd,
    Set,
    Asc,
    Desc,
    Last
    ;

    fun isSubType(): Boolean {
        return this in arrayOf(And, AndEnd, Or, OrEnd)
    }
}


@Suppress("ClassName")
abstract class _WrapperDsl<T: Any>: SqlDsl<T> {
    abstract fun getQueue(): MutableList<_Condition>


    override fun <K> KProperty<K?>.set(value: String?): _Condition {
        return setR(value)
    }

    override fun KProperty<Int?>.set(value: Int?): _Condition {
        return setR(value)
    }

    override fun KProperty<Double?>.set(value: Double?): _Condition {
        return setR(value)
    }

    override fun KProperty<Long?>.set(value: Long?): _Condition {
        return setR(value)
    }

    override fun KProperty<Float?>.set(value: Float?): _Condition {
        return setR(value)
    }

    override fun KProperty<Boolean?>.set(value: Boolean?): _Condition {
        return setR(value)
    }

    override fun KProperty<LocalDate?>.set(value: LocalDate?): _Condition {
        return setR(value)
    }

    override fun KProperty<LocalDateTime?>.set(value: LocalDateTime?): _Condition {
        return setR(value)
    }

    override fun KProperty<LocalTime?>.set(value: LocalTime?): _Condition {
        return setR(value)
    }

    override fun <K> KProperty<K?>.eq(value: String?): _Condition {
        return eqR(value)
    }

    override fun KProperty<Int?>.eq(value: Int?): _Condition {
        return eqR(value)
    }

    override fun KProperty<Double?>.eq(value: Double?): _Condition {
        return eqR(value)
    }

    override fun KProperty<Long?>.eq(value: Long?): _Condition {
        return eqR(value)
    }

    override fun KProperty<Float?>.eq(value: Float?): _Condition {
        return eqR(value)
    }

    override fun KProperty<Boolean?>.eq(value: Boolean?): _Condition {
        return eqR(value)
    }

    override fun KProperty<LocalDate?>.eq(value: LocalDate?): _Condition {
        return eqR(value)
    }

    override fun KProperty<LocalDateTime?>.eq(value: LocalDateTime?): _Condition {
        return eqR(value)
    }

    override fun KProperty<LocalTime?>.eq(value: LocalTime?): _Condition {
        return eqR(value)
    }

    override fun <K> KProperty<K?>.ne(value: String?): _Condition {
        return neR(value)
    }

    override fun KProperty<Int?>.ne(value: Int?): _Condition {
        return neR(value)
    }

    override fun KProperty<Double?>.ne(value: Double?): _Condition {
        return neR(value)
    }

    override fun KProperty<Long?>.ne(value: Long?): _Condition {
        return neR(value)
    }

    override fun KProperty<Float?>.ne(value: Float?): _Condition {
        return neR(value)
    }

    override fun KProperty<Boolean?>.ne(value: Boolean?): _Condition {
        return neR(value)
    }

    override fun KProperty<LocalDate?>.ne(value: LocalDate?): _Condition {
        return neR(value)
    }

    override fun KProperty<LocalDateTime?>.ne(value: LocalDateTime?): _Condition {
        return neR(value)
    }

    override fun KProperty<LocalTime?>.ne(value: LocalTime?): _Condition {
        return neR(value)
    }

    override fun <K> KProperty<K?>.ge(value: String?): _Condition {
        return geR(value)
    }

    override fun KProperty<Int?>.ge(value: Int?): _Condition {
        return geR(value)
    }

    override fun KProperty<Double?>.ge(value: Double?): _Condition {
        return geR(value)
    }

    override fun KProperty<Long?>.ge(value: Long?): _Condition {
        return geR(value)
    }

    override fun KProperty<Float?>.ge(value: Float?): _Condition {
        return geR(value)
    }

    override fun KProperty<LocalDate?>.ge(value: LocalDate?): _Condition {
        return geR(value)
    }

    override fun KProperty<LocalDateTime?>.ge(value: LocalDateTime?): _Condition {
        return geR(value)
    }

    override fun KProperty<LocalTime?>.ge(value: LocalTime?): _Condition {
        return geR(value)
    }

    override fun <K> KProperty<K?>.gt(value: String?): _Condition {
        return gtR(value)
    }

    override fun KProperty<Int?>.gt(value: Int?): _Condition {
        return gtR(value)
    }

    override fun KProperty<Double?>.gt(value: Double?): _Condition {
        return gtR(value)
    }

    override fun KProperty<Long?>.gt(value: Long?): _Condition {
        return gtR(value)
    }

    override fun KProperty<Float?>.gt(value: Float?): _Condition {
        return gtR(value)
    }

    override fun KProperty<LocalDate?>.gt(value: LocalDate?): _Condition {
        return gtR(value)
    }

    override fun KProperty<LocalDateTime?>.gt(value: LocalDateTime?): _Condition {
        return gtR(value)
    }

    override fun KProperty<LocalTime?>.gt(value: LocalTime?): _Condition {
        return gtR(value)
    }

    override fun <K> KProperty<K?>.le(value: String?): _Condition {
        return leR(value)
    }

    override fun KProperty<Int?>.le(value: Int?): _Condition {
        return leR(value)
    }

    override fun KProperty<Double?>.le(value: Double?): _Condition {
        return leR(value)
    }

    override fun KProperty<Long?>.le(value: Long?): _Condition {
        return leR(value)
    }

    override fun KProperty<Float?>.le(value: Float?): _Condition {
        return leR(value)
    }

    override fun KProperty<LocalDate?>.le(value: LocalDate?): _Condition {
        return leR(value)
    }

    override fun KProperty<LocalDateTime?>.le(value: LocalDateTime?): _Condition {
        return leR(value)
    }

    override fun KProperty<LocalTime?>.le(value: LocalTime?): _Condition {
        return leR(value)
    }

    override fun <K> KProperty<K?>.lt(value: String?): _Condition {
        return ltR(value)
    }

    override fun KProperty<Int?>.lt(value: Int?): _Condition {
        return ltR(value)
    }

    override fun KProperty<Double?>.lt(value: Double?): _Condition {
        return ltR(value)
    }

    override fun KProperty<Long?>.lt(value: Long?): _Condition {
        return ltR(value)
    }

    override fun KProperty<Float?>.lt(value: Float?): _Condition {
        return ltR(value)
    }

    override fun KProperty<LocalDate?>.lt(value: LocalDate?): _Condition {
        return ltR(value)
    }

    override fun KProperty<LocalDateTime?>.lt(value: LocalDateTime?): _Condition {
        return ltR(value)
    }

    override fun KProperty<LocalTime?>.lt(value: LocalTime?): _Condition {
        return ltR(value)
    }

    override fun <K> KProperty<K?>.like(value: String?): _Condition {
        return likeR(value)
    }

    override fun KProperty<LocalDate?>.like(value: LocalDate?): _Condition {
        return likeR(value)
    }

    override fun KProperty<LocalDateTime?>.like(value: LocalDateTime?): _Condition {
        return likeR(value)
    }

    override fun KProperty<LocalTime?>.like(value: LocalTime?): _Condition {
        return likeR(value)
    }

    override fun <K> KProperty<K?>.notLike(value: String?): _Condition {
        return notLikeR(value)
    }

    override fun KProperty<LocalDate?>.notLike(value: LocalDate?): _Condition {
        return notLikeR(value)
    }

    override fun KProperty<LocalDateTime?>.notLike(value: LocalDateTime?): _Condition {
        return notLikeR(value)
    }

    override fun KProperty<LocalTime?>.notLike(value: LocalTime?): _Condition {
        return notLikeR(value)
    }

    override fun <K> KProperty<K?>.likeLeft(value: String?): _Condition {
        return likeLeftR(value)
    }

    override fun KProperty<LocalDate?>.likeLeft(value: LocalDate?): _Condition {
        return likeLeftR(value)
    }

    override fun KProperty<LocalDateTime?>.likeLeft(value: LocalDateTime?): _Condition {
        return likeLeftR(value)
    }

    override fun KProperty<LocalTime?>.likeLeft(value: LocalTime?): _Condition {
        return likeLeftR(value)
    }

    override fun <K> KProperty<K?>.likeRight(value: String?): _Condition {
        return likeRightR(value)
    }

    override fun KProperty<LocalDate?>.likeRight(value: LocalDate?): _Condition {
        return likeRightR(value)
    }

    override fun KProperty<LocalDateTime?>.likeRight(value: LocalDateTime?): _Condition {
        return likeRightR(value)
    }

    override fun KProperty<LocalTime?>.likeRight(value: LocalTime?): _Condition {
        return likeRightR(value)
    }


    override fun <E> KProperty<E>.notBetween(value: Pair<E, E>): _Condition {
        return notBetweenR(value)
    }

    override fun <E> KProperty<E>.between(value: Pair<E, E>): _Condition {
        return betweenR(value)
    }

    override fun <E> KProperty<E>.notIn(value: Collection<E>): _Condition {
        return notInR(value)
    }

    override fun <E> KProperty<E>.`in`(value: Collection<E>): _Condition {
        return inR(value)
    }

    override fun KProperty<*>.isNull(): _Condition {
        return isNullR()
    }

    override fun KProperty<*>.notNull(): _Condition {
        return notNullR()
    }

    private fun <E> KProperty<E>.setR(value: E): _Condition {
        return _Condition(_WrapperDslType.Set, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.eqR(value: E): _Condition {
        return _Condition(_WrapperDslType.Eq, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.neR(value: E): _Condition {
        return _Condition(_WrapperDslType.Neq, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.gtR(value: E): _Condition {
        return _Condition(_WrapperDslType.Gt, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.geR(value: E): _Condition {
        return _Condition(_WrapperDslType.Gte, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.ltR(value: E): _Condition {
        return _Condition(_WrapperDslType.Lt, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.leR(value: E): _Condition {
        return _Condition(_WrapperDslType.Lte, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.likeR(value: E): _Condition {
        return _Condition(_WrapperDslType.Like, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.notLikeR(value: E): _Condition {
        return _Condition(_WrapperDslType.NotLike, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.likeLeftR(value: E): _Condition {
        return _Condition(_WrapperDslType.LikeLeft, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.likeRightR(value: E): _Condition {
        return _Condition(_WrapperDslType.LikeRight, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.inR(value: Collection<E>): _Condition {
        return _Condition(_WrapperDslType.In, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.notInR(value: Collection<E>): _Condition {
        return _Condition(_WrapperDslType.NotIn, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.betweenR(value: Pair<E, E>): _Condition {
        return _Condition(_WrapperDslType.Between, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.notBetweenR(value: Pair<E, E>): _Condition {
        return _Condition(_WrapperDslType.NotBetween, this, value).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.isNullR(): _Condition {
        return _Condition(_WrapperDslType.IsNull, this, null).also {
            getQueue().add(it)
        }
    }

    private fun <E> KProperty<E>.notNullR(): _Condition {
        return _Condition(_WrapperDslType.IsNotNull, this, null).also {
            getQueue().add(it)
        }
    }

    protected fun existConditionTrue(queue: List<_Condition>): Boolean {
        var c = false
        var i = 0
        while (i < queue.size && !c) {
            if (!queue[i].type.isSubType()) {
                if (queue[i].condition) {
                    c = true
                }
                i++
            } else {
                val j = if (queue[i].type == _WrapperDslType.Or) {
                    i + findMatchingIndex(queue.subList(i, queue.size), _WrapperDslType.Or, _WrapperDslType.OrEnd) + 1
                } else {
                    i + findMatchingIndex(queue.subList(i, queue.size), _WrapperDslType.And, _WrapperDslType.AndEnd) + 1
                }
                if (queue[i].condition && i + 2 < j) {
                    c = existConditionTrue(queue.subList(i + 1, j - 1))
                }
                i = j
            }
        }
        return c
    }

    protected fun findMatchingIndex(queue: List<_Condition>, type: _WrapperDslType, target: _WrapperDslType): Int {
        val stack = mutableListOf<_WrapperDslType>()
        for (i in queue.indices) {
            if (queue[i].type == type) {
                stack.add(type)
            } else if (queue[i].type == target) {
                stack.removeAt(stack.size - 1)
            }
            if (stack.isEmpty()) {
                return i
            }
        }
        return -1
    }
}

@Suppress("ClassName")
class _Condition(
    val type: _WrapperDslType,
    val property: KProperty<*>?,
    val value: Any?,
    var condition: Boolean = true
) {

    infix fun on(condition: Boolean) {
        this.condition = condition
    }
}
