package io.github.sagiri_kawaii01.centox.mp

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 15:18
 * @since
 */

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import kotlin.reflect.KProperty

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/8/19 17:41
 * @since
 */
@Suppress("ClassName")
enum class _Type {
    Query,
    Update
}

@Suppress("ClassName")
class _SqlWrapperDsl<T : Any>(
    private val queryWrapperBuild: () -> KtQueryWrapper<T>,
    private val updateWrapperBuild: () -> KtUpdateWrapper<T>
): _WrapperDsl<T>() {

    private val _queryWrapper: ThreadLocal<KtQueryWrapper<T>> = ThreadLocal()
    private val _updateWrapper: ThreadLocal<KtUpdateWrapper<T>> = ThreadLocal()
    private val type: ThreadLocal<_Type> = ThreadLocal()
    private val _queue: ThreadLocal<MutableList<_Condition>> = ThreadLocal()
    private lateinit var instance: T

    override fun getQueue(): MutableList<_Condition> {
        return _queue.get()
    }

    override fun and(condition: Boolean, init: T.() -> Unit) {
        if (condition) {
            _queue.get().add(_Condition(_WrapperDslType.And, null, null, true))
            init(instance)
            _queue.get().add(_Condition(_WrapperDslType.AndEnd, null, null))
        }
    }

    override fun or(condition: Boolean, init: T.() -> Unit) {
        if (condition) {
            _queue.get().add(_Condition(_WrapperDslType.Or, null, null, true))
            init(instance)
            _queue.get().add(_Condition(_WrapperDslType.OrEnd, null, null))
        }
    }

    override fun orderByAsc(vararg properties: KProperty<*>) {
        _queue.get().add(_Condition(_WrapperDslType.Asc, null, properties.toList()))
    }

    override fun orderByDesc(vararg properties: KProperty<*>) {
        _queue.get().add(_Condition(_WrapperDslType.Desc, null, properties.toList()))
    }

    override fun last(sql: String) {
        _queue.get().add(_Condition(_WrapperDslType.Last, null, sql))
    }

    private fun wrapperAnalyze(wrapper: Wrapper<T>, queue: List<_Condition>): Wrapper<T> {
        var tmpWrapper = wrapper
        var i = 0
        while (i < queue.size) {
            if (this.type.get() == _Type.Query) {
                when (queue[i].type) {
                    _WrapperDslType.And -> {
                        val j = i + findMatchingIndex(queue.subList(i, queue.size),
                            _WrapperDslType.And,
                            _WrapperDslType.AndEnd
                        ) + 1
                        if (existConditionTrue(queue.subList(i, queue.size))) {
                            tmpWrapper = (tmpWrapper as KtQueryWrapper<T>).and {
                                wrapperAnalyze(it, queue.subList(i + 1, j - 1))
                            }
                        }
                        i = j
                    }
                    _WrapperDslType.Or -> {
                        val j = i + findMatchingIndex(queue.subList(i, queue.size),
                            _WrapperDslType.Or,
                            _WrapperDslType.OrEnd
                        ) + 1
                        if (existConditionTrue(queue.subList(i, queue.size))) {
                            tmpWrapper = (tmpWrapper as KtQueryWrapper<T>).or {
                                wrapperAnalyze(it, queue.subList(i + 1, j - 1))
                            }
                        }
                        i = j
                    }
                    else -> {
                        tmpWrapper = wrapperAnalyzeLine(tmpWrapper, queue[i])
                        i += 1
                    }
                }
            } else {
                when (queue[i].type) {
                    _WrapperDslType.And -> {
                        val j = i + findMatchingIndex(queue.subList(i, queue.size),
                            _WrapperDslType.And,
                            _WrapperDslType.AndEnd
                        ) + 1
                        if (existConditionTrue(queue.subList(i, queue.size))) {
                            tmpWrapper = (tmpWrapper as KtUpdateWrapper<T>).and {
                                wrapperAnalyze(it, queue.subList(i + 1, j - 1))
                            }
                        }
                        i = j
                    }
                    _WrapperDslType.Or -> {
                        val j = i + findMatchingIndex(queue.subList(i, queue.size),
                            _WrapperDslType.Or,
                            _WrapperDslType.OrEnd
                        ) + 1
                        if (existConditionTrue(queue.subList(i, queue.size))) {
                            tmpWrapper = (tmpWrapper as KtUpdateWrapper<T>).or {
                                wrapperAnalyze(it, queue.subList(i + 1, j - 1))
                            }
                        }
                        i = j

                    }
                    else -> {
                        tmpWrapper = wrapperAnalyzeLine(tmpWrapper, queue[i])
                        i += 1
                    }
                }
            }

        }
        return tmpWrapper
    }

    private fun wrapperAnalyzeLine(wrapper: Wrapper<T>, condition: _Condition): Wrapper<T> {
        if (!condition.condition) {
            return wrapper
        }
        return if (this.type.get() == _Type.Query) {
            when(condition.type) {
                _WrapperDslType.Eq -> (wrapper as KtQueryWrapper<T>).eq(condition.property, condition.value)
                _WrapperDslType.Neq -> (wrapper as KtQueryWrapper<T>).ne(condition.property, condition.value)
                _WrapperDslType.Gt -> (wrapper as KtQueryWrapper<T>).gt(condition.property, condition.value)
                _WrapperDslType.Gte -> (wrapper as KtQueryWrapper<T>).ge(condition.property, condition.value)
                _WrapperDslType.Lt -> (wrapper as KtQueryWrapper<T>).lt(condition.property, condition.value)
                _WrapperDslType.Lte -> (wrapper as KtQueryWrapper<T>).le(condition.property, condition.value)
                _WrapperDslType.Like -> (wrapper as KtQueryWrapper<T>).like(condition.property, condition.value)
                _WrapperDslType.NotLike -> (wrapper as KtQueryWrapper<T>).notLike(condition.property, condition.value)
                _WrapperDslType.LikeLeft -> (wrapper as KtQueryWrapper<T>).likeLeft(condition.property, condition.value)
                _WrapperDslType.LikeRight -> (wrapper as KtQueryWrapper<T>).likeRight(condition.property, condition.value)
                _WrapperDslType.In -> (wrapper as KtQueryWrapper<T>).`in`(condition.property, condition.value as Collection<*>)
                _WrapperDslType.NotIn -> (wrapper as KtQueryWrapper<T>).notIn(condition.property, condition.value as Collection<*>)
                _WrapperDslType.Between -> {
                    val pair = condition.value as Pair<*, *>
                    (wrapper as KtQueryWrapper<T>).between(condition.property, pair.first, pair.second)
                }
                _WrapperDslType.NotBetween -> {
                    val pair = condition.value as Pair<*, *>
                    (wrapper as KtQueryWrapper<T>).notBetween(condition.property, pair.first, pair.second)
                }
                _WrapperDslType.IsNull -> (wrapper as KtQueryWrapper<T>).isNull(condition.property)
                _WrapperDslType.IsNotNull -> (wrapper as KtQueryWrapper<T>).isNotNull(condition.property)
                _WrapperDslType.Asc -> @Suppress("UNCHECKED_CAST") (wrapper as KtQueryWrapper<T>).orderByAsc(condition.value as List<KProperty<*>>)
                _WrapperDslType.Desc -> @Suppress("UNCHECKED_CAST") (wrapper as KtQueryWrapper<T>).orderByDesc(condition.value as List<KProperty<*>>)
                _WrapperDslType.Last -> (wrapper as KtQueryWrapper<T>).last(condition.value.toString())
                else -> wrapper
            }
        } else {
            when(condition.type) {
                _WrapperDslType.Eq -> (wrapper as KtUpdateWrapper<T>).eq(condition.property, condition.value)
                _WrapperDslType.Neq -> (wrapper as KtUpdateWrapper<T>).ne(condition.property, condition.value)
                _WrapperDslType.Gt -> (wrapper as KtUpdateWrapper<T>).gt(condition.property, condition.value)
                _WrapperDslType.Gte -> (wrapper as KtUpdateWrapper<T>).ge(condition.property, condition.value)
                _WrapperDslType.Lt -> (wrapper as KtUpdateWrapper<T>).lt(condition.property, condition.value)
                _WrapperDslType.Lte -> (wrapper as KtUpdateWrapper<T>).le(condition.property, condition.value)
                _WrapperDslType.Like -> (wrapper as KtUpdateWrapper<T>).like(condition.property, condition.value)
                _WrapperDslType.NotLike -> (wrapper as KtUpdateWrapper<T>).notLike(condition.property, condition.value)
                _WrapperDslType.LikeLeft -> (wrapper as KtUpdateWrapper<T>).likeLeft(condition.property, condition.value)
                _WrapperDslType.LikeRight -> (wrapper as KtUpdateWrapper<T>).likeRight(condition.property, condition.value)
                _WrapperDslType.In -> (wrapper as KtUpdateWrapper<T>).`in`(condition.property, condition.value as Collection<*>)
                _WrapperDslType.NotIn -> (wrapper as KtUpdateWrapper<T>).notIn(condition.property, condition.value as Collection<*>)
                _WrapperDslType.Between -> {
                    val pair = condition.value as Pair<*, *>
                    (wrapper as KtUpdateWrapper<T>).between(condition.property, pair.first, pair.second)
                }
                _WrapperDslType.NotBetween -> {
                    val pair = condition.value as Pair<*, *>
                    (wrapper as KtUpdateWrapper<T>).notBetween(condition.property, pair.first, pair.second)
                }
                _WrapperDslType.IsNull -> (wrapper as KtUpdateWrapper<T>).isNull(condition.property)
                _WrapperDslType.IsNotNull -> (wrapper as KtUpdateWrapper<T>).isNotNull(condition.property)
                _WrapperDslType.Last -> (wrapper as KtQueryWrapper<T>).last(condition.value.toString())
                _WrapperDslType.Set -> (wrapper as KtUpdateWrapper<T>).set(condition.property, condition.value)
                else -> wrapper
            }
        }
    }

    fun build(init: T.() -> Unit, value: T, type: _Type): Wrapper<T> {
        instance = value
        this.type.set(type)
        if (_queue.get() == null) {
            _queue.set(mutableListOf())
        }
        _queue.get().clear()
        if (_queryWrapper.get() == null) {
            _queryWrapper.set(queryWrapperBuild())
        }
        if (_updateWrapper.get() == null) {
            _updateWrapper.set(updateWrapperBuild())
        }
        _queryWrapper.get().clear()
        _updateWrapper.get().clear()
        value.init()
        return wrapperAnalyze(if (type == _Type.Query) _queryWrapper.get() else _updateWrapper.get(), _queue.get())
    }

    override fun dsl(): SqlDsl<T> {
        return this
    }

}

inline fun <reified T : SqlDsl<T>> query(noinline init: T.() -> Unit): Wrapper<T> {
    return (T::class.java.newInstance().dsl() as _SqlWrapperDsl<T>).build(init,
        _SqlDslCache.getInstance(T::class.java),
        _Type.Query
    )
}

inline fun <reified T : SqlDsl<T>> update(noinline init: T.() -> Unit): Wrapper<T> {
    return (T::class.java.newInstance().dsl() as _SqlWrapperDsl<T>).build(init,
        _SqlDslCache.getInstance(T::class.java),
        _Type.Update
    )
}

@Suppress("ClassName")
object _SqlDslCache {
    private val cache = mutableMapOf<Class<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : SqlDsl<T>> getInstance(clazz: Class<T>): T {
        return cache.getOrPut(clazz) {
            clazz.newInstance()
        } as T
    }
}
