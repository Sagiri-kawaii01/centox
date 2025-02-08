package io.github.sagirikawaii01.centox.orika;

import com.thoughtworks.paranamer.*
import ma.glasnost.orika.constructor.ConstructorResolverStrategy
import ma.glasnost.orika.impl.Specifications
import ma.glasnost.orika.metadata.*
import java.lang.reflect.Constructor
import java.util.*

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/3/4 14:20
 * @since 1.2.4
 */
open class DataClassConstructorResolverStrategy: ConstructorResolverStrategy {
    private var paranamer: Paranamer =
        CachingParanamer(AdaptiveParanamer(BytecodeReadingParanamer(), AnnotationParanamer()))

    override fun <T, A, B> resolve(classMap: ClassMap<A, B>, sourceType: Type<T>): ConstructorResolverStrategy.ConstructorMapping<T> {
        val aToB = classMap.bType == sourceType
        val targetClass = if (aToB) classMap.bType else classMap.aType
        val sourceClass = if (aToB) classMap.aType else classMap.bType
        val declaredParameterNames = if (aToB) classMap.constructorB else classMap.constructorA

        val targetParameters = getTargetParams(classMap.fieldsMapping, aToB, declaredParameterNames)
        val byDefault = declaredParameterNames == null
        var foundDeclaredConstructor = false

        val constructors = targetClass.rawType.declaredConstructors as Array<Constructor<T>>
        val constructorsByMatchedParams = TreeMap<Int, ConstructorResolverStrategy.ConstructorMapping<T>>()
        var i = 0
        for ((index, constructor) in constructors.withIndex()) {
            val constructorMapping = ConstructorResolverStrategy.ConstructorMapping<T>()
            constructorMapping.declaredParameters = declaredParameterNames
            val genericParamTypes = constructor.genericParameterTypes
            try {
                val parameterNames = mapTargetParamNames(paranamer.lookupParameterNames(constructor))
                constructorMapping.isParameterNameInfoAvailable = true
                if (targetParameters.keys.containsAll(listOf(*parameterNames))) {
                    foundDeclaredConstructor = true
                    constructorMapping.constructor = constructor
                    mapConstructorArgs(
                        constructorMapping,
                        targetParameters,
                        parameterNames,
                        genericParamTypes,
                        byDefault
                    )
                    constructorsByMatchedParams[parameterNames.size * 1000] = constructorMapping
                }
                i = index
            } catch (e: ParameterNamesNotFoundException) {
                /*
                 * Could not find parameter names of the constructors; attempt to match constructors
                 * based on the types of the destination properties
                 */
                if (targetParameters.size >= genericParamTypes.size) {
                    matchByDestParamTypes(
                        constructorMapping,
                        targetParameters,
                        genericParamTypes,
                        byDefault,
                        constructorsByMatchedParams
                    )
                    constructorMapping.constructor = constructor
                }
            }
        }
        if (constructorsByMatchedParams.size == 0) {
            val constructorMapping = ConstructorResolverStrategy.ConstructorMapping<T>()
            constructorMapping.declaredParameters = declaredParameterNames
            constructorMapping.constructor = constructors[i]
            val parameterNames = mapTargetParamNames(paranamer.lookupParameterNames(constructors[i]))
            val genericParamTypes = constructors[i].genericParameterTypes
            mapConstructorArgs(
                constructorMapping,
                targetParameters,
                parameterNames,
                genericParamTypes,
                byDefault
            )
            constructorsByMatchedParams[parameterNames.size * 1000] = constructorMapping
        }
        return prepareMatchedConstructorMapping(
            constructorsByMatchedParams,
            targetClass,
            sourceClass,
            declaredParameterNames,
            foundDeclaredConstructor,
            constructors
        )
    }

    /**
     * Maps parameter names from target constructor.
     *
     * @param parameterNames Original parameter names.
     * @return Changed parameter names.
     */
    private fun mapTargetParamNames(parameterNames: Array<String>): Array<String> {
        return parameterNames
    }

    private fun <T> prepareMatchedConstructorMapping(
        constructorsByMatchedParams: TreeMap<Int, ConstructorResolverStrategy.ConstructorMapping<T>>,
        targetClass: Type<*>,
        sourceClass: Type<*>,
        declaredParameterNames: Array<String>?,
        foundDeclaredConstructor: Boolean,
        constructors: Array<Constructor<T>>
    ): ConstructorResolverStrategy.ConstructorMapping<T> {
        if (constructorsByMatchedParams.size > 0) {
            return constructorsByMatchedParams[constructorsByMatchedParams.lastKey()]!!
        } else if (declaredParameterNames != null) {
            return throwNotMatchedTargetConstructorEx(
                targetClass,
                sourceClass,
                declaredParameterNames,
                foundDeclaredConstructor
            )
        } else {
            /*
             * User didn't specify any constructor, and we couldn't find any that seem compatible;
             * TODO: can we really do anything in this case? maybe we should just throw an error
             * describing some alternative options like creating a Converter or declaring their own
             * custom ObjectFactory...
             */
            val defaultMapping = ConstructorResolverStrategy.ConstructorMapping<T>()
            defaultMapping.constructor = if (constructors.isEmpty()) null else constructors[0]
            return defaultMapping
        }
    }

    private fun <T> throwNotMatchedTargetConstructorEx(
        targetClass: Type<*>,
        sourceClass: Type<*>,
        declaredParameterNames: Array<String>,
        foundDeclaredConstructor: Boolean
    ): ConstructorResolverStrategy.ConstructorMapping<T> {
        val errMsg: String
        val declaredParamNamesTxt = declaredParameterNames.contentToString()
        errMsg = if (foundDeclaredConstructor) {
            "Declared constructor for " +
                    targetClass +
                    "(" + declaredParamNamesTxt + ")" +
                    " could not be matched to the source fields of " + sourceClass
        } else {
            "No constructors found for " + targetClass +
                    " matching the specified constructor parameters " + (if (declaredParameterNames.isEmpty()) "(no-arg constructor)" else "($declaredParamNamesTxt)")
        }
        throw IllegalStateException(errMsg)
    }

    private fun <T> matchByDestParamTypes(
        constructorMapping: ConstructorResolverStrategy.ConstructorMapping<T>,
        targetParameters: Map<String, FieldMap>,
        genericParamTypes: Array<java.lang.reflect.Type>,
        byDefault: Boolean,
        constructorsByMatchedParams: TreeMap<Int, ConstructorResolverStrategy.ConstructorMapping<T>>
    ) {
        val targetTypes: MutableList<FieldMap> = ArrayList(targetParameters.values)
        var matchScore = 0
        var exactMatches = 0
        val parameterTypes: Array<Type<*>?> = arrayOfNulls(genericParamTypes.size)
        for (i in genericParamTypes.indices) {
            val param = genericParamTypes[i]

            parameterTypes[i] = TypeFactory.valueOf<Any>(param)
            val iter = targetTypes.iterator()
            while (iter.hasNext()) {
                val fieldMap = iter.next()
                val targetType = fieldMap.destination.type
                if ((parameterTypes[i] == targetType && ++exactMatches != 0)
                    || parameterTypes[i]!!.isAssignableFrom(targetType)
                ) {
                    ++matchScore

                    val parameterName = fieldMap.destination.name
                    val existingField = targetParameters[parameterName]
                    val argumentMap = mapConstructorArgument(existingField!!, parameterTypes[i]!!, byDefault)
                    constructorMapping.mappedFields.add(argumentMap)

                    iter.remove()
                    break
                }
            }
        }
        constructorMapping.parameterTypes = parameterTypes
        constructorsByMatchedParams[matchScore * 1000 + exactMatches] = constructorMapping
    }

    private fun <T> mapConstructorArgs(
        constructorMapping: ConstructorResolverStrategy.ConstructorMapping<T>,
        targetParameters: Map<String, FieldMap>,
        parameterNames: Array<String>,
        genericParameterTypes: Array<java.lang.reflect.Type>,
        byDefault: Boolean
    ) {
        val parameterTypes: Array<Type<*>?> = arrayOfNulls(genericParameterTypes.size)
        for (i in parameterNames.indices) {
            val parameterName = parameterNames[i]
            parameterTypes[i] = TypeFactory.valueOf<Any>(genericParameterTypes[i])
            val existingField = targetParameters[parameterName]
            val argumentMap = mapConstructorArgument(existingField, parameterTypes[i]!!, byDefault)
            constructorMapping.mappedFields.add(argumentMap)
        }
        constructorMapping.parameterTypes = parameterTypes
    }

    private fun getTargetParams(
        fieldMaps: Set<FieldMap>,
        aToB: Boolean,
        declaredParameterNames: Array<String>?
    ): Map<String, FieldMap> {
        val targetParameters: MutableMap<String, FieldMap> = LinkedHashMap()
        if (declaredParameterNames != null) {
            /*
             * An override to the property names was provided
             */
            val fields: MutableSet<FieldMap> = HashSet(fieldMaps)
            for (arg in declaredParameterNames) {
                val iter = fields.iterator()
                while (iter.hasNext()) {
                    var fieldMap = iter.next()
                    if (fieldMap.`is`(Specifications.aMappingOfTheRequiredClassProperty())) {
                        continue
                    }
                    if (!aToB) {
                        fieldMap = fieldMap.flip()
                    }
                    if (fieldMap.destination.name == arg) {
                        targetParameters[arg] = fieldMap
                        iter.remove()
                    }
                }
            }
        } else {
            /*
             * Determine the set of constructor argument names from the field mapping.
             */
            for (fieldMap in fieldMaps) {
                if (fieldMap.`is`(Specifications.aMappingOfTheRequiredClassProperty())) {
                    continue
                }
                targetParameters[fieldMap.destination.name] = if (aToB) {
                    fieldMap
                } else {
                    fieldMap.flip()
                }
            }
        }
        return targetParameters
    }

    private fun mapConstructorArgument(existing: FieldMap?, argumentType: Type<*>, byDefault: Boolean): FieldMap {
        val destProp = if (null == existing) {
            Property.Builder()
                .name("")
                .getter("")
                .type(argumentType)
                .build()
        } else {
            Property.Builder()
                .name(existing!!.destination.name)
                .getter(existing.destination.name)
                .type(argumentType)
                .build()
        }
        return FieldMap(
            existing?.source ?: Property.Builder()
                .name("")
                .getter("")
                .type(argumentType)
                .build(), destProp, null,
            null, MappingDirection.A_TO_B, false, existing?.converterId,
            byDefault, null, null
        )
    }
}