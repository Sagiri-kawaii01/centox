package io.github.sagirikawaii01.centox.orika;
import ma.glasnost.orika.*;
import ma.glasnost.orika.Properties;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.BuiltinConverters;
import ma.glasnost.orika.impl.*;
import ma.glasnost.orika.impl.generator.CodeGenerationStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.MapperGenerator;
import ma.glasnost.orika.impl.generator.ObjectFactoryGenerator;
import ma.glasnost.orika.inheritance.DefaultSuperTypeResolverStrategy;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;
import ma.glasnost.orika.metadata.*;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.unenhance.BaseUnenhancer;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;
import ma.glasnost.orika.util.Ordering;
import ma.glasnost.orika.util.SortedCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Boolean.valueOf;
import static java.lang.System.getProperty;
import static ma.glasnost.orika.OrikaSystemProperties.*;
import static ma.glasnost.orika.OrikaSystemProperties.CAPTURE_FIELD_CONTEXT;
import static ma.glasnost.orika.StateReporter.DIVIDER;
import static ma.glasnost.orika.StateReporter.humanReadableSizeInMemory;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.4
 */
public class DataClassMapperFactory implements MapperFactory, StateReporter.Reportable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataClassMapperFactory.class);

    protected final MapperFacade mapperFacade;
    protected final MapperGenerator mapperGenerator;
    protected final DataClassFactoryGenerator objectFactoryGenerator;

    protected final ConcurrentHashMap<MapperKey, ClassMap<Object, Object>> classMapRegistry;
    protected final SortedCollection<Mapper<Object, Object>> mappersRegistry;
    protected final SortedCollection<Filter<Object, Object>> filtersRegistry;
    protected final MappingContextFactory contextFactory;
    protected final MappingContextFactory nonCyclicContextFactory;
    protected final ConcurrentHashMap<Type<? extends Object>, ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>> objectFactoryRegistry;
    protected final ConcurrentHashMap<Type<?>, Set<Type<?>>> explicitAToBRegistry;
    protected final ConcurrentHashMap<Type<?>, Set<Type<?>>> dynamicAToBRegistry;
    protected final List<DefaultFieldMapper> defaultFieldMappers;
    protected final UnenhanceStrategy unenhanceStrategy;
    protected final UnenhanceStrategy userUnenahanceStrategy;
    protected final ConverterFactory converterFactory;
    protected final CompilerStrategy compilerStrategy;
    protected final PropertyResolverStrategy propertyResolverStrategy;
    protected final Map<java.lang.reflect.Type, Type<?>> concreteTypeRegistry;
    /** @see MapperFactoryBuilder#alwaysCreateMultipleMapperWrapper */
    protected boolean alwaysCreateMultipleMapperWrapper;
    protected final ClassMapBuilderFactory classMapBuilderFactory;
    protected ClassMapBuilderFactory chainClassMapBuilderFactory;
    protected final Map<MapperKey, Set<ClassMap<Object, Object>>> usedMapperMetadataRegistry;

    protected final boolean useAutoMapping;
    protected final boolean useBuiltinConverters;
    protected final boolean favorExtension;
    protected volatile boolean isBuilt = false;
    protected volatile boolean isBuilding = false;

    protected final DataClassExceptionUtility exceptionUtil;

    protected DataClassMapperFactory(MapperFactoryBuilder<?, ?> builder) {

        this.converterFactory = new ConverterFactoryFacade(builder.converterFactory);
        this.compilerStrategy = builder.compilerStrategy;
        this.classMapRegistry = new ConcurrentHashMap<>();
        this.mappersRegistry = new SortedCollection<Mapper<Object, Object>>(Ordering.MAPPER);
        this.filtersRegistry = new SortedCollection<Filter<Object, Object>>(Ordering.FILTER);
        this.explicitAToBRegistry = new ConcurrentHashMap<Type<?>, Set<Type<?>>>();
        this.dynamicAToBRegistry = new ConcurrentHashMap<Type<?>, Set<Type<?>>>();
        this.usedMapperMetadataRegistry = new ConcurrentHashMap<MapperKey, Set<ClassMap<Object, Object>>>();
        this.objectFactoryRegistry = new ConcurrentHashMap<Type<? extends Object>, ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>>();
        this.defaultFieldMappers = new CopyOnWriteArrayList<DefaultFieldMapper>();
        this.userUnenahanceStrategy = builder.unenhanceStrategy;
        this.unenhanceStrategy = buildUnenhanceStrategy(builder.unenhanceStrategy, builder.superTypeStrategy);
        this.contextFactory = builder.mappingContextFactory;
        this.nonCyclicContextFactory = new NonCyclicMappingContext.Factory(this.contextFactory.getGlobalProperties());
        this.exceptionUtil = new DataClassExceptionUtility(this, builder.dumpStateOnException);
        this.mapperFacade = buildMapperFacade(contextFactory, unenhanceStrategy);
        this.concreteTypeRegistry = new ConcurrentHashMap<java.lang.reflect.Type, Type<?>>();
        this.alwaysCreateMultipleMapperWrapper = builder.alwaysCreateMultipleMapperWrapper;

        if (builder.classMaps != null) {
            for (final ClassMap<?, ?> classMap : builder.classMaps) {
                registerClassMap(classMap);
            }
        }

        this.propertyResolverStrategy = builder.propertyResolverStrategy;
        this.classMapBuilderFactory = builder.classMapBuilderFactory;
        this.classMapBuilderFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.classMapBuilderFactory.setMapperFactory(this);
        addClassMapBuilderFactory(new ClassMapBuilderForArrays.Factory());
        addClassMapBuilderFactory(new ClassMapBuilderForLists.Factory());
        addClassMapBuilderFactory(new ClassMapBuilderForMaps.Factory());

        this.mapperGenerator = new MapperGenerator(this, builder.compilerStrategy);
        this.objectFactoryGenerator = new DataClassFactoryGenerator(this, builder.constructorResolverStrategy, builder.compilerStrategy);
        this.useAutoMapping = builder.useAutoMapping;
        this.favorExtension = builder.favorExtension;
        this.useBuiltinConverters = builder.useBuiltinConverters;

        builder.codeGenerationStrategy.setMapperFactory(this);

        Map<Object, Object> props = this.contextFactory.getGlobalProperties();
        props.put(Properties.SHOULD_MAP_NULLS, builder.mapNulls);
        props.put(Properties.CODE_GENERATION_STRATEGY, builder.codeGenerationStrategy);
        props.put(Properties.COMPILER_STRATEGY, builder.compilerStrategy);
        props.put(Properties.PROPERTY_RESOLVER_STRATEGY, builder.propertyResolverStrategy);
        props.put(Properties.UNENHANCE_STRATEGY, unenhanceStrategy);
        props.put(Properties.MAPPER_FACTORY, this);
        props.put(Properties.FILTERS, this.filtersRegistry);
        props.put(Properties.CAPTURE_FIELD_CONTEXT, builder.captureFieldContext);

        /*
         * Register default concrete types for common collection types; these
         * can be overridden as needed by user code.
         */
        for (Map.Entry<Class, Class> concreteTypeMap : DefaultConcreteTypeMap.getAll()) {
            this.registerConcreteType(concreteTypeMap.getKey(), concreteTypeMap.getValue());
        }

    }

    protected void addClassMapBuilderFactory(ClassMapBuilderFactory factory) {
        factory.setChainClassMapBuilderFactory(chainClassMapBuilderFactory);
        chainClassMapBuilderFactory = factory;
        factory.setPropertyResolver(this.propertyResolverStrategy);
        factory.setMapperFactory(this);
    }


    public static abstract class MapperFactoryBuilder<F extends DataClassMapperFactory, B extends MapperFactoryBuilder<F, B>> {

        protected UnenhanceStrategy unenhanceStrategy;

        protected SuperTypeResolverStrategy superTypeStrategy;

        protected ConstructorResolverStrategy constructorResolverStrategy;

        protected CompilerStrategy compilerStrategy;

        protected Set<ClassMap<?, ?>> classMaps;

        protected ConverterFactory converterFactory;

        protected PropertyResolverStrategy propertyResolverStrategy;

        protected ClassMapBuilderFactory classMapBuilderFactory;

        protected MappingContextFactory mappingContextFactory;

        protected CodeGenerationStrategy codeGenerationStrategy;

        protected Boolean useBuiltinConverters;

        protected Boolean useAutoMapping;

        protected Boolean mapNulls;

        protected Boolean dumpStateOnException;

        protected Boolean favorExtension;

        protected Boolean captureFieldContext;

        protected boolean alwaysCreateMultipleMapperWrapper;

        public MapperFactoryBuilder() {
            converterFactory = UtilityResolver.getDefaultConverterFactory();
            constructorResolverStrategy = UtilityResolver.getDefaultConstructorResolverStrategy();
            compilerStrategy = UtilityResolver.getDefaultCompilerStrategy();
            propertyResolverStrategy = UtilityResolver.getDefaultPropertyResolverStrategy();
            classMapBuilderFactory = UtilityResolver.getDefaultClassMapBuilderFactory();
            mappingContextFactory = UtilityResolver.getDefaultMappingContextFactory();

            useBuiltinConverters = valueOf(getProperty(USE_BUILTIN_CONVERTERS, "true"));
            useAutoMapping = valueOf(getProperty(USE_AUTO_MAPPING, "true"));
            mapNulls = valueOf(getProperty(MAP_NULLS, "true"));
            dumpStateOnException = valueOf(getProperty(DUMP_STATE_ON_EXCEPTION, "false"));
            favorExtension = valueOf(getProperty(FAVOR_EXTENSION, "false"));
            captureFieldContext = valueOf(getProperty(CAPTURE_FIELD_CONTEXT, "false"));
            alwaysCreateMultipleMapperWrapper = valueOf(
                    getProperty("ma.glasnost.orika.alwaysCreateMultipleMapperWrapper", "false"));
            codeGenerationStrategy = new DefaultCodeGenerationStrategy();
        }

        protected abstract B self();

        public B classMaps(Set<ClassMap<?, ?>> classMaps) {
            this.classMaps = classMaps;
            return self();
        }

        public B unenhanceStrategy(UnenhanceStrategy unenhanceStrategy) {
            this.unenhanceStrategy = unenhanceStrategy;
            return self();
        }

        public B superTypeResolverStrategy(SuperTypeResolverStrategy superTypeStrategy) {
            this.superTypeStrategy = superTypeStrategy;
            return self();
        }

        public B constructorResolverStrategy(ConstructorResolverStrategy constructorResolverStrategy) {
            this.constructorResolverStrategy = constructorResolverStrategy;
            return self();
        }

        public B converterFactory(ConverterFactory converterFactory) {
            this.converterFactory = converterFactory;
            return self();
        }


        public B compilerStrategy(CompilerStrategy compilerStrategy) {
            this.compilerStrategy = compilerStrategy;
            return self();
        }


        public B propertyResolverStrategy(PropertyResolverStrategy propertyResolverStrategy) {
            this.propertyResolverStrategy = propertyResolverStrategy;
            return self();
        }

        public B classMapBuilderFactory(ClassMapBuilderFactory classMapBuilderFactory) {
            this.classMapBuilderFactory = classMapBuilderFactory;
            return self();
        }

        public B mappingContextFactory(MappingContextFactory mappingContextFactory) {
            this.mappingContextFactory = mappingContextFactory;
            return self();
        }

        public B useAutoMapping(boolean useAutoMapping) {
            this.useAutoMapping = useAutoMapping;
            return self();
        }

        public B useBuiltinConverters(boolean useBuiltinConverters) {
            this.useBuiltinConverters = useBuiltinConverters;
            return self();
        }

        @Deprecated
        public B usedBuiltinConverters(boolean useBuiltinConverters) {
            this.useBuiltinConverters = useBuiltinConverters;
            return self();
        }

        public B mapNulls(boolean mapNulls) {
            this.mapNulls = mapNulls;
            return self();
        }

        public B dumpStateOnException(boolean dumpStateOnException) {
            this.dumpStateOnException = dumpStateOnException;
            return self();
        }

        public B favorExtension(boolean favorExtension) {
            this.favorExtension = favorExtension;
            return self();
        }

        public B captureFieldContext(boolean captureFieldContext) {
            this.captureFieldContext = captureFieldContext;
            return self();
        }

        public CodeGenerationStrategy getCodeGenerationStrategy() {
            return codeGenerationStrategy;
        }

        public B codeGenerationStrategy(CodeGenerationStrategy codeGenerationStrategy) {
            this.codeGenerationStrategy  = codeGenerationStrategy ;
            return self();
        }

        public abstract F build();

    }

    public static class Builder extends MapperFactoryBuilder<DataClassMapperFactory, Builder> {

        @Override
        public DataClassMapperFactory build() {
            return new DataClassMapperFactory(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

    }

    protected UnenhanceStrategy buildUnenhanceStrategy(UnenhanceStrategy unenhanceStrategy, SuperTypeResolverStrategy superTypeStrategy) {

        BaseUnenhancer unenhancer = new BaseUnenhancer();

        if (unenhanceStrategy != null) {
            unenhancer.addUnenhanceStrategy(unenhanceStrategy);
        }

        if (superTypeStrategy != null) {
            unenhancer.addSuperTypeResolverStrategy(superTypeStrategy);
        }

        /*
         * This strategy produces super-types whenever the proposed class type
         * is not accessible to the compilerStrategy and/or the current thread
         * context class-loader; it is added last as a fail-safe in case a
         * suggested type cannot be used. It is automatically included, as
         * there's no case when skipping it would be desired....
         */
        final SuperTypeResolverStrategy inaccessibleTypeStrategy = new DefaultSuperTypeResolverStrategy() {

            public boolean isTypeAccessible(Type<?> type) {

                try {
                    compilerStrategy.assureTypeIsAccessible(type.getRawType());
                    return true;
                } catch (CompilerStrategy.SourceCodeGenerationException e) {
                    return false;
                }
            }

            @Override
            public boolean isAcceptable(Type<?> type) {
                return isTypeAccessible(type) && !java.lang.reflect.Proxy.class.equals(type.getRawType());
            }

        };

        unenhancer.addSuperTypeResolverStrategy(inaccessibleTypeStrategy);

        return unenhancer;
    }


    protected MapperFacade buildMapperFacade(MappingContextFactory contextFactory, UnenhanceStrategy unenhanceStrategy) {
        return new MapperFacadeImpl(this, contextFactory, unenhanceStrategy, exceptionUtil);
    }

    @SuppressWarnings("unchecked")
    public <A, B> Mapper<A, B> lookupMapper(MapperKey mapperKey) {
        MappingContext context = contextFactory.getContext();
        try {
            return (Mapper<A, B>) lookupMapper(mapperKey, context);
        } finally {
            contextFactory.release(context);
        }
    }


    @SuppressWarnings("unchecked")
    public Mapper<Object, Object> lookupMapper(MapperKey mapperKey, MappingContext context) {

        Mapper<?, ?> mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
        if (internalMapperMustBeGenerated(mapper, mapperKey)) {
            mapper = null;
        }
        if (mapper == null && useAutoMapping) {
            synchronized (this) {
                mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
                boolean internalMapperMustBeGenerated = internalMapperMustBeGenerated(mapper, mapperKey);
                if (internalMapperMustBeGenerated) {
                    mapper = null;
                }
                if (mapper == null) {
                    try {
                        /*
                         * We shouldn't create a mapper for an immutable type;
                         * although it will succeed in generating an empty
                         * mapper, it won't actually result in a valid mapping,
                         * so it's better to throw an exception to indicate more
                         * clearly that something went wrong. However, there is
                         * a possibility that a custom ObjectFactory was
                         * registered for the immutable type, which would be
                         * valid.
                         */
                        if (mapperKey.getBType().isImmutable() && !objectFactoryRegistry.containsKey(mapperKey.getBType())) {
                            throw new MappingException("No converter registered for conversion from " + mapperKey.getAType() + " to "
                                    + mapperKey.getBType() + ", nor any ObjectFactory which can generate " + mapperKey.getBType()
                                    + " from " + mapperKey.getAType());
                        }

                        LOGGER.debug("No mapper registered for {}: attempting to generate", mapperKey);

                        ClassMapBuilder<?, ?> builder = classMap(mapperKey.getAType(), mapperKey.getBType()).byDefault();
                        for (MapperKey key : discoverUsedMappers(builder)) {
                            builder.use(key.getAType(), key.getBType());
                        }
                        final ClassMap<?, ?> classMap = builder.toClassMap();

                        buildObjectFactories(classMap, context);
                        mapper = buildMapper(classMap, true, context);
                        initializeUsedMappers(mapper, classMap, context);
                        if (internalMapperMustBeGenerated || alwaysCreateMultipleMapperWrapper) {
                            // regenerate MultipleMapperWrapper.
                            mapper = getRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), false);
                        }
                    } catch (MappingException e) {
                        e.setSourceType(mapperKey.getAType());
                        e.setDestinationType(mapperKey.getBType());
                        throw exceptionUtil.decorate(e);
                    }
                }
            }

        }
        return (Mapper<Object, Object>) mapper;
    }

    private boolean internalMapperMustBeGenerated(Mapper<?, ?> mapper, MapperKey mapperKey) {
        boolean internalMapperMustBeGenerated = false;
        if (mapperKey.getBType().isConcrete() && mapper instanceof MultipleMapperWrapper) {
            MultipleMapperWrapper mapperWrapper = (MultipleMapperWrapper) mapper;
            Mapper<Object, Object> internalMapper = mapperWrapper.findMapperFor(mapperKey);
            if (internalMapper == null) {
                internalMapperMustBeGenerated = true;
            }
        }
        return internalMapperMustBeGenerated;
    }


    public boolean existsRegisteredMapper(Type<?> sourceType, Type<?> destinationType, boolean includeAutoGeneratedMappers) {
        return getRegisteredMapper(sourceType, destinationType, includeAutoGeneratedMappers) != null;
    }

    @SuppressWarnings("unchecked")
    protected <A, B> Mapper<A, B> getRegisteredMapper(MapperKey mapperKey) {
        return getRegisteredMapper((Type<A>) mapperKey.getAType(), (Type<B>) mapperKey.getBType(), false);
    }

    @SuppressWarnings("unchecked")
    private <A, B> Mapper<A, B> getRegisteredMapper(Type<A> typeA, Type<B> typeB, boolean includeAutoGeneratedMappers) {
        List<Mapper<A, B>> foundMappers = new ArrayList<Mapper<A, B>>();

        boolean objFactoryBExists = customObjectFactoryForDestinationExists(typeA, typeB);
        boolean objFactoryAExists = customObjectFactoryForDestinationExists(typeB, typeA);

        for (Mapper<?, ?> mapper : mappersRegistry) {
            if ((mapper.getAType().equals(typeA) && mapper.getBType().equals(typeB))
                    || (mapper.getAType().equals(typeB) && mapper.getBType().equals(typeA))) {
                foundMappers.add((Mapper<A, B>) mapper);
            } else if ((mapper.getAType().isAssignableFrom(typeA) && mapper.getBType().isAssignableFrom(typeB))
                    || (mapper.getBType().isAssignableFrom(typeA) && mapper.getAType().isAssignableFrom(typeB))
                    || (mapper.getAType().isAssignableFrom(typeA) && typeB.isAssignableFrom(mapper.getBType()) && objFactoryBExists)
                    || (mapper.getBType().isAssignableFrom(typeA) && typeB.isAssignableFrom(mapper.getAType()) && objFactoryAExists)) {
                if (!favorsExtension(mapper) || !canBeExtended(typeA, typeB, mapper)) {
                    if (includeAutoGeneratedMappers || !(mapper instanceof GeneratedMapperBase)) {
                        foundMappers.add((Mapper<A, B>) mapper);
                    } else if (!((GeneratedMapperBase) mapper).isFromAutoMapping()) {
                        foundMappers.add((Mapper<A, B>) mapper);
                    }
                }
            }
        }
        if ((objFactoryBExists || objFactoryAExists) && foundMappers.size() > 1) {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Found Multiple Mappers:\n");
                for (Mapper<A, B> mapper : foundMappers) {
                    msg.append("\t");
                    msg.append(mapper.getAType());
                    msg.append(" <-> ");
                    msg.append(mapper.getBType());
                    msg.append("\n");
                }
                LOGGER.debug(msg.toString());
            }
            return (Mapper<A, B>) new MultipleMapperWrapper((Type<Object>) typeA, (Type<Object>) typeB, (List) foundMappers);
        } else if (foundMappers.size() > 0) {
            if (alwaysCreateMultipleMapperWrapper) {
                return (Mapper<A, B>) new MultipleMapperWrapper((Type<Object>) typeA, (Type<Object>) typeB, (List) foundMappers);
            }
            return foundMappers.get(0);
        }

        return null;
    }

    private boolean favorsExtension(Mapper<?, ?> mapper) {
        return mapper.favorsExtension() == null ? favorExtension : mapper.favorsExtension();
    }

    private boolean canBeExtended(Type<?> typeA, Type<?> typeB, Mapper<?, ?> mapper) {
        boolean extensible;
        try {
            compilerStrategy.assureTypeIsAccessible(typeA.getRawType());
            compilerStrategy.assureTypeIsAccessible(typeB.getRawType());
            extensible = true;
        } catch (CompilerStrategy.SourceCodeGenerationException e) {
            extensible = false;
        }
        return extensible;
    }

    public MapperFacade getMapperFacade() {
        if (!isBuilt) {
            synchronized (mapperFacade) {
                if (!isBuilt) {
                    build();
                }
            }
        }
        return mapperFacade;
    }

    public <D> void registerObjectFactory(ObjectFactory<D> objectFactory, Type<D> destinationType) {
        registerObjectFactory(objectFactory, destinationType, TypeFactory.TYPE_OF_OBJECT);
    }

    public <D, S> void registerObjectFactory(ObjectFactory<D> objectFactory, Type<D> destinationType, Type<S> sourceType) {
        ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> localCache = objectFactoryRegistry.get(destinationType);
        if (localCache == null) {
            localCache = new ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>();
            ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> existing = objectFactoryRegistry.putIfAbsent(
                    destinationType, localCache);
            if (existing != null) {
                localCache = existing;
            }
        }
        localCache.put(sourceType, objectFactory);
        if (isBuilding || isBuilt) {
            mapperFacade.factoryModified(this);
        }
    }

    @Deprecated
    public void registerMappingHint(ma.glasnost.orika.MappingHint... hints) {

        DefaultFieldMapper[] mappers = new DefaultFieldMapper[hints.length];
        for (int i = 0, len = hints.length; i < len; ++i) {
            mappers[i] = new ma.glasnost.orika.MappingHint.DefaultFieldMappingConverter(hints[i]);
        }
        registerDefaultFieldMapper(mappers);
    }

    public void registerDefaultFieldMapper(DefaultFieldMapper... mappers) {
        this.defaultFieldMappers.addAll(Arrays.asList(mappers));
    }

    public void registerConcreteType(Type<?> abstractType, Type<?> concreteType) {
        this.concreteTypeRegistry.put(abstractType, concreteType);
    }

    public void registerConcreteType(Class<?> abstractType, Class<?> concreteType) {
        this.concreteTypeRegistry.put(abstractType, TypeFactory.valueOf(concreteType));
    }

    public <T> ObjectFactory<T> lookupObjectFactory(Type<T> targetType) {
        return lookupObjectFactory(targetType, TypeFactory.TYPE_OF_OBJECT);
    }


    public <T, S> ObjectFactory<T> lookupObjectFactory(Type<T> targetType, Type<S> sourceType) {
        MappingContext context = contextFactory.getContext();
        try {
            return lookupObjectFactory(targetType, sourceType, context);
        } finally {
            contextFactory.release(context);
        }
    }

    protected <T, S> ObjectFactory<T> lookupExistingObjectFactory(final Type<T> destinationType, final Type<S> sourceType,
                                                                  final MappingContext context) {

        if (destinationType == null || sourceType == null) {
            return null;
        }

        ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> objFactoryCacheForDestType = objectFactoryRegistry
                .get(destinationType);
        if (objFactoryCacheForDestType != null) {
            ObjectFactory<T> result = findObjectFactory(objFactoryCacheForDestType, sourceType, false);
            if (result != null) {
                return result;
            }
        }

        Set<Type<? extends Object>> objFactoryDestTypes = getKeys(objectFactoryRegistry);
        for (Type<? extends Object> objFactoryDestType : objFactoryDestTypes) {
            if (destinationType.isAssignableFrom(objFactoryDestType)) {
                ObjectFactory<T> result = findObjectFactory(objectFactoryRegistry.get(objFactoryDestType), sourceType, true);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private <K, V> Set<K> getKeys(ConcurrentHashMap<K, V> concurrentHashMap) {
        Map<K, V> map = concurrentHashMap;
        return map.keySet();
    }

    @SuppressWarnings("unchecked")
    private <T, S> ObjectFactory<T> findObjectFactory(
            ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> objFactoryCachePerSrcType,
            final Type<S> sourceType, boolean onlyCustomObjectFactories) {
        Type<?> checkSourceType = sourceType;
        ObjectFactory<T> result;
        do {
            result = (ObjectFactory<T>) objFactoryCachePerSrcType.get(checkSourceType);
            if (result != null && onlyCustomObjectFactories && !isCustomObjectFactory(result)) {
                result = null;
            }
            checkSourceType = checkSourceType.getSuperType();
        } while (result == null && !TypeFactory.TYPE_OF_OBJECT.equals(checkSourceType));
        if (result == null) {
            result = (ObjectFactory<T>) objFactoryCachePerSrcType.get(TypeFactory.TYPE_OF_OBJECT);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T, S> ObjectFactory<T> lookupObjectFactory(final Type<T> destinationType, final Type<S> sourceType, final MappingContext context) {

        if (destinationType == null || sourceType == null) {
            return null;
        }

        Type<T> targetType = destinationType;
        ObjectFactory<T> result = lookupExistingObjectFactory(targetType, sourceType, context);

        if (result == null) {
            // Check if we can use default constructor...
            synchronized (this) {
                if (!targetType.isConcrete()) {
                    targetType = (Type<T>) resolveConcreteType(targetType, targetType);
                }
                if (targetType == null) {
                    throw new IllegalStateException(String.format(
                            "Cannot create ObjectFactory for \n\t destinationType = %s\n\t sourceType = %s",
                            destinationType,
                            sourceType));
                }

                Constructor<?>[] constructors = targetType.getRawType().getDeclaredConstructors();
                if (useAutoMapping || !isBuilt) {
                    if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
                        /*
                         * Use the default constructor in the case where it is
                         * the only option
                         */
                        result = new DefaultConstructorObjectFactory<T>(targetType.getRawType());
                    } else {
                        try {
                            result = (ObjectFactory<T>) objectFactoryGenerator.build(targetType, sourceType, context);
                        } catch (MappingException e) {
                            for (Constructor<?> c : constructors) {
                                if (c.getParameterTypes().length == 0) {
                                    result = new DefaultConstructorObjectFactory<T>(targetType.getRawType());
                                    break;
                                }
                            }
                            if (result == null) {
                                throw exceptionUtil.decorate(e);
                            }
                        }
                    }

                    ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> localCache = objectFactoryRegistry.get(targetType);
                    if (localCache == null) {
                        localCache = new ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>();
                        ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> existing = objectFactoryRegistry.putIfAbsent(
                                targetType, localCache);
                        if (existing != null) {
                            localCache = existing;
                        }
                    }

                    ObjectFactory<T> existing = (ObjectFactory<T>) localCache.putIfAbsent(sourceType, result);
                    if (existing != null) {
                        result = existing;
                    }

                } else {
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 0) {
                            result = new DefaultConstructorObjectFactory<T>(targetType.getRawType());
                            break;
                        }
                    }
                }

            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> lookupConcreteDestinationType(Type<S> sourceType, Type<D> destinationType, MappingContext context) {

        /*
         * Check for a pre-resolved type
         */
        Type<? extends D> concreteType = context == null ? null : context.getConcreteClass(sourceType, destinationType);

        if (concreteType != null) {
            return concreteType;
        }

        /*
         * Look for some (custom) ObjectFactories.
         * If there is an Object Factory, then the ObjectFactory will generate dynamically the concrete type.
         * So return the destinationType even if it is not "concrete", because we cannot exactly say what the concrete type will be.
         */
        if (customObjectFactoryForDestinationExists(sourceType, destinationType)) {
            return destinationType;
        }

        /*
         * Look for a match in the explicitly registered types
         */
        Set<Type<?>> destinationSet = explicitAToBRegistry.get(sourceType);
        if (destinationSet != null && !destinationSet.isEmpty()) {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && type.isConcrete()) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !destinationType.isConcrete()) {
                        return (Type<? extends D>) type;
                    }
                }
            }
        }

        /*
         * Return the original destinationType if it's concrete
         */
        if (destinationType.isConcrete()) {
            return destinationType;
        }

        /*
         * Look for a match in the dynamically registered types
         */
        destinationSet = dynamicAToBRegistry.get(sourceType);
        if (destinationSet != null && !destinationSet.isEmpty()) {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && type.isConcrete()) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !destinationType.isConcrete()) {
                        return (Type<? extends D>) type;
                    }
                }
            }
        } else {
            /*
             * Try the registered mappers for a possible type match
             */
            Mapper<S, D> registeredMapper = getRegisteredMapper(sourceType, destinationType, true);
            if (registeredMapper != null) {
                concreteType = (Type<? extends D>) (registeredMapper.getAType().isAssignableFrom(sourceType) ? registeredMapper.getBType()
                        : registeredMapper.getAType());
                if (!concreteType.isConcrete()) {
                    concreteType = (Type<? extends D>) resolveConcreteType(concreteType, destinationType);
                } else {
                    return null;
                }
            } else {
                concreteType = (Type<? extends D>) resolveConcreteType(destinationType, destinationType);
            }
        }

        if (concreteType == null) {
            concreteType = (Type<? extends D>) resolveConcreteType(destinationType, destinationType);
        }

        return concreteType;
    }

    private <S, D> boolean customObjectFactoryForDestinationExists(Type<S> sourceType, Type<D> destinationType) {
        Set<Type<? extends Object>> objFactoryDestTypes = getKeys(objectFactoryRegistry);
        for (Type<? extends Object> objFactoryDestType : objFactoryDestTypes) {
            if (destinationType.isAssignableFrom(objFactoryDestType)
                    && objectFactoryRegistry.get(objFactoryDestType).containsKey(sourceType)) {
                ObjectFactory<? extends Object> objectFactory = objectFactoryRegistry.get(objFactoryDestType).get(sourceType);
                if (isCustomObjectFactory(objectFactory)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCustomObjectFactory(ObjectFactory<? extends Object> objectFactory) {
        if (objectFactory instanceof GeneratedObjectFactory) {
            return false;
        }
        if (objectFactory.getClass().equals(DefaultConstructorObjectFactory.class)) {
            return false;
        }
        return true;
    }

    protected Type<?> resolveConcreteType(Type<?> type, Type<?> originalType) {

        Type<?> concreteType = concreteTypeOf(type);

        if (concreteType != null && !concreteType.isAssignableFrom(originalType)) {
            if (originalType.isConcrete()) {
                concreteType = originalType;
            } else {
                concreteType = concreteTypeOf(originalType);
            }
        }

        return concreteType;
    }

    private Type<?> concreteTypeOf(Type<?> type) {
        Type<?> concreteType = this.concreteTypeRegistry.get(type);
        if (concreteType == null) {
            concreteType = this.concreteTypeRegistry.get(type.getRawType());
            if (concreteType != null) {
                concreteType = TypeFactory.resolveValueOf(concreteType.getRawType(), type);
            }
        }
        return concreteType;
    }

    @SuppressWarnings("unchecked")
    public synchronized <A, B> void registerClassMap(ClassMap<A, B> classMap) {
        classMapRegistry.put(new MapperKey(classMap.getAType(), classMap.getBType()), (ClassMap<Object, Object>) classMap);
        if (isBuilding || isBuilt) {
            MappingContext context = contextFactory.getContext();
            try {
                if (classMap.getUsedMappers().isEmpty()) {
                    classMap = classMap.copyWithUsedMappers(discoverUsedMappers(classMap));
                }
                GeneratedMapperBase generatedMapper = buildMapper(classMap, /** isAutoGenerated == **/
                        isBuilding, context);

                buildObjectFactories(classMap, context);
                initializeUsedMappers(generatedMapper, classMap, context);
                mapperFacade.factoryModified(this);
            } finally {
                contextFactory.release(context);
            }
        }
    }

    public <A, B> void registerClassMap(ClassMapBuilder<A, B> builder) {
        registerClassMap(builder.toClassMap());
    }

    public synchronized void build() {

        if (!isBuilding && !isBuilt) {
            isBuilding = true;

            MappingContext context = contextFactory.getContext();
            try {
                if (useBuiltinConverters) {
                    BuiltinConverters.register(converterFactory);
                }
                converterFactory.setMapperFacade(mapperFacade);

                for (Map.Entry<MapperKey, ClassMap<Object, Object>> classMapEntry : classMapRegistry.entrySet()) {
                    ClassMap<Object, Object> classMap = classMapEntry.getValue();
                    if (classMap.getUsedMappers().isEmpty()) {
                        classMapEntry.setValue(classMap.copyWithUsedMappers(discoverUsedMappers(classMap)));
                    }
                }

                buildClassMapRegistry();

                Map<ClassMap<?, ?>, GeneratedMapperBase> generatedMappers = new HashMap<ClassMap<?, ?>, GeneratedMapperBase>();
                for (ClassMap<?, ?> classMap : classMapRegistry.values()) {
                    generatedMappers.put(classMap, buildMapper(classMap, false, context));
                }

                Set<Map.Entry<ClassMap<?, ?>, GeneratedMapperBase>> generatedMapperEntries = generatedMappers.entrySet();
                for (Map.Entry<ClassMap<?, ?>, GeneratedMapperBase> generatedMapperEntry : generatedMapperEntries) {
                    buildObjectFactories(generatedMapperEntry.getKey(), context);
                    initializeUsedMappers(generatedMapperEntry.getValue(), generatedMapperEntry.getKey(), context);
                }

            } finally {
                contextFactory.release(context);
            }

            isBuilt = true;
            isBuilding = false;
        }
    }

    public Set<ClassMap<Object, Object>> lookupUsedClassMap(MapperKey mapperKey) {
        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(mapperKey);
        if (usedClassMapSet == null) {
            usedClassMapSet = Collections.emptySet();
        }
        return usedClassMapSet;
    }


    private void buildClassMapRegistry() {

        // prepare a map for classmap (stored as set)
        Map<MapperKey, ClassMap<Object, Object>> classMapsDictionary = new HashMap<MapperKey, ClassMap<Object, Object>>();

        for (final ClassMap<Object, Object> classMap : classMapRegistry.values()) {
            classMapsDictionary.put(new MapperKey(classMap.getAType(), classMap.getBType()), classMap);
        }

        for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
            MapperKey key = new MapperKey(classMap.getAType(), classMap.getBType());

            Set<ClassMap<Object, Object>> usedClassMapSet = new LinkedHashSet<ClassMap<Object, Object>>();

            for (final MapperKey parentMapperKey : classMap.getUsedMappers()) {
                ClassMap<Object, Object> usedClassMap = classMapsDictionary.get(parentMapperKey);
                if (usedClassMap == null) {
                    throw exceptionUtil.newMappingException("Cannot find class mapping using mapper : " + classMap.getMapperClassName());
                }
                usedClassMapSet.add(usedClassMap);
            }
            usedMapperMetadataRegistry.put(key, usedClassMapSet);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private <S, D> void buildObjectFactories(ClassMap<S, D> classMap, MappingContext context) {
        Type<?> aType = classMap.getAType();
        Type<?> bType = classMap.getBType();

        if (classMap.getConstructorA() != null && lookupExistingObjectFactory(aType, TypeFactory.TYPE_OF_OBJECT, context) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(aType, bType, context);
            registerObjectFactory(objectFactory, (Type<Object>) aType);
        }

        if (classMap.getConstructorB() != null && lookupExistingObjectFactory(bType, TypeFactory.TYPE_OF_OBJECT, context) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(bType, aType, context);
            registerObjectFactory(objectFactory, (Type<Object>) bType);
        }
    }

    private Set<MapperKey> discoverUsedMappers(MappedTypePair<?, ?> classMapBuilder) {
        Set<MapperKey> mappers = new LinkedHashSet<MapperKey>();

        for (ClassMap<?, ?> map : classMapRegistry.values()) {
            if (map.getAType().isAssignableFrom(classMapBuilder.getAType()) && map.getBType().isAssignableFrom(classMapBuilder.getBType())) {
                if (!map.getAType().equals(classMapBuilder.getAType()) || !map.getBType().equals(classMapBuilder.getBType())) {
                    MapperKey key = new MapperKey(map.getAType(), map.getBType());
                    mappers.add(key);
                }
            } else if (map.getAType().isAssignableFrom(classMapBuilder.getBType())
                    && map.getBType().isAssignableFrom(classMapBuilder.getAType())) {
                if (!map.getAType().equals(classMapBuilder.getBType()) || !map.getBType().equals(classMapBuilder.getAType())) {
                    MapperKey key = new MapperKey(map.getBType(), map.getAType());
                    mappers.add(key);
                }
            }
        }
        return mappers;
    }

    private void initializeUsedMappers(Mapper<?, ?> mapper, ClassMap<?, ?> classMap, MappingContext context) {

        Set<Mapper<Object, Object>> parentMappers = new LinkedHashSet<Mapper<Object, Object>>();

        if (!classMap.getUsedMappers().isEmpty()) {
            for (MapperKey parentMapperKey : classMap.getUsedMappers()) {
                collectUsedMappers(classMap, parentMappers, parentMapperKey, context);
            }
        }

        parentMappers.remove(mapper);

        for (Mapper<Object, Object> curParrentMapper : parentMappers) {
            if (!GeneratedMapperBase.isUsedMappersInitialized(curParrentMapper)) {
                initializeUsedMappers(curParrentMapper, getClassMap(new MapperKey(
                        curParrentMapper.getAType(),
                        curParrentMapper.getBType())), context);
            }
        }

        Mapper<Object, Object>[] usedMappers = parentMappers.toArray(new Mapper[parentMappers.size()]);
        parentMappers.clear();
        for (int i=0, len=usedMappers.length; i < len; ++i) {
            boolean exists = false;
            for (int j=0; j < len; ++j) {
                if( i != j && GeneratedMapperBase.isUsedMapperOf(usedMappers[i], usedMappers[j])) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                parentMappers.add(usedMappers[i]);
            }
        }
        if (parentMappers.size() < usedMappers.length) {
            usedMappers = parentMappers.toArray(new Mapper[parentMappers.size()]);
        }

        /*
         * Flip any used mappers which are specified in the wrong direction
         */
        for (int i = 0; i < usedMappers.length; ++i) {
            Mapper<Object, Object> usedMapper = usedMappers[i];
            if (usedMapper.getAType().isAssignableFrom(classMap.getBType()) && usedMapper.getBType().isAssignableFrom(classMap.getAType())) {
                usedMappers[i] = ReversedMapper.reverse(usedMapper);
            }
        }
        mapper.setUsedMappers(usedMappers);
    }

    private void collectUsedMappers(ClassMap<?, ?> classMap, Set<Mapper<Object, Object>> parentMappers, MapperKey parentMapperKey, MappingContext context) {
        Mapper<Object, Object> parentMapper = lookupMapper(parentMapperKey, context);
        if (parentMapper == null) {
            throw exceptionUtil.newMappingException("Cannot find used mappers for : " + classMap.getMapperClassName());
        }
        if (parentMapper instanceof MultipleMapperWrapper) {
            MultipleMapperWrapper multiMapperWrapper = (MultipleMapperWrapper) parentMapper;
            Collection<Mapper<Object, Object>> fromMultipleMappers = multiMapperWrapper.getMappersRegistry();
            for (Mapper<Object, Object> fromMultipleMapper : fromMultipleMappers) {
                if (fromMultipleMapper.getAType().isAssignableFrom(classMap.getAType())
                        && fromMultipleMapper.getBType().isAssignableFrom(classMap.getBType())) {
                    parentMappers.add(fromMultipleMapper);
                } else if (fromMultipleMapper.getAType().isAssignableFrom(classMap.getBType())
                        && fromMultipleMapper.getBType().isAssignableFrom(classMap.getAType())) {
                    parentMappers.add(fromMultipleMapper);
                }
            }
        } else {
            parentMappers.add(parentMapper);
        }

        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(parentMapperKey);
        if (usedClassMapSet != null) {
            for (ClassMap<Object, Object> cm : usedClassMapSet) {
                collectUsedMappers(cm, parentMappers, new MapperKey(cm.getAType(), cm.getBType()), context);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private GeneratedMapperBase buildMapper(ClassMap<?, ?> classMap, boolean isAutoGenerated, MappingContext context) {

        register(classMap.getAType(), classMap.getBType(), isAutoGenerated);
        register(classMap.getBType(), classMap.getAType(), isAutoGenerated);

        final MapperKey mapperKey = new MapperKey(classMap.getAType(), classMap.getBType());
        final GeneratedMapperBase mapper = mapperGenerator.build(classMap, context);
        mapper.setMapperFacade(mapperFacade);
        mapper.setFromAutoMapping(isAutoGenerated);
        if (classMap.getCustomizedMapper() != null) {
            final Mapper<Object, Object> customizedMapper = (Mapper<Object, Object>) classMap.getCustomizedMapper();
            mapper.setCustomMapper(customizedMapper);
        }
        mappersRegistry.remove(mapper);
        mappersRegistry.add(mapper);
        classMapRegistry.put(mapperKey, (ClassMap<Object, Object>) classMap);

        return mapper;
    }

    protected <S, D> void register(Type<S> sourceType, Type<D> destinationType, boolean isAutoGenerated) {

        ConcurrentHashMap<Type<?>, Set<Type<?>>> registry = isAutoGenerated ? dynamicAToBRegistry : explicitAToBRegistry;

        Set<Type<?>> destinationSet = registry.get(sourceType);
        if (destinationSet == null) {
            destinationSet = new TreeSet<Type<?>>();
            Set<Type<?>> existing = registry.putIfAbsent(sourceType, destinationSet);
            if (existing != null) {
                destinationSet = existing;
            }
        }
        destinationSet.add(destinationType);
    }

    @SuppressWarnings("unchecked")
    public <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey) {
        return (ClassMap<A, B>) classMapRegistry.get(mapperKey);
    }

    public Set<Type<? extends Object>> lookupMappedClasses(Type<?> type) {

        TreeSet<Type<?>> mappedClasses = new TreeSet<Type<?>>();
        Set<Type<? extends Object>> types = explicitAToBRegistry.get(type);
        if (types != null) {
            mappedClasses.addAll(types);
        }
        types = dynamicAToBRegistry.get(type);
        if (types != null) {
            mappedClasses.addAll(types);
        }

        return mappedClasses;
    }

    public ConverterFactory getConverterFactory() {
        return converterFactory;
    }

    public <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass) {
        registerObjectFactory(objectFactory, TypeFactory.<T> valueOf(targetClass));
    }

    protected ClassMapBuilderFactory getClassMapBuilderFactory() {
        if (!classMapBuilderFactory.isInitialized()) {
            classMapBuilderFactory.setDefaultFieldMappers(defaultFieldMappers.toArray(new DefaultFieldMapper[defaultFieldMappers.size()]));
        }
        return classMapBuilderFactory;
    }

    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Type<B> bType) {
        ClassMapBuilderFactory classMapBuilderFactory = chainClassMapBuilderFactory.chooseClassMapBuilderFactory(aType, bType);

        if (classMapBuilderFactory != null) {
            return classMapBuilderFactory.map(aType, bType);
        } else {
            return getClassMapBuilderFactory().map(aType, bType);
        }
    }

    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Type<B> bType) {
        return classMap(TypeFactory.<A> valueOf(aType), bType);
    }

    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Class<B> bType) {
        return classMap(aType, TypeFactory.<B> valueOf(bType));
    }

    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Class<B> bType) {
        return classMap(TypeFactory.<A> valueOf(aType), TypeFactory.<B> valueOf(bType));
    }

    @SuppressWarnings("unchecked")
    public synchronized <A, B> void registerMapper(Mapper<A, B> mapper) {
        this.mappersRegistry.add((Mapper<Object, Object>) mapper);
        mapper.setMapperFacade(this.mapperFacade);
        register(mapper.getAType(), mapper.getBType(), false);
        register(mapper.getBType(), mapper.getAType(), false);
        if (isBuilding || isBuilt) {
            mapperFacade.factoryModified(this);
        }
    }

    public <S, D> BoundMapperFacade<S, D> getMapperFacade(Type<S> sourceType, Type<D> destinationType) {
        return getMapperFacade(sourceType, destinationType, true);
    }

    public <S, D> BoundMapperFacade<S, D> getMapperFacade(Type<S> sourceType, Type<D> destinationType, boolean containsCycles) {
        getMapperFacade();
        MappingContextFactory ctxFactory = containsCycles ? contextFactory : nonCyclicContextFactory;
        return new DataClassBoundMapperFacade<S, D>(this, ctxFactory, sourceType, destinationType);
    }

    public <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType) {
        return getMapperFacade(TypeFactory.valueOf(aType), TypeFactory.valueOf(bType));
    }

    public <A, B> BoundMapperFacade<A, B> getMapperFacade(Class<A> aType, Class<B> bType, boolean containsCycles) {
        return getMapperFacade(TypeFactory.valueOf(aType), TypeFactory.valueOf(bType), containsCycles);
    }


    public UnenhanceStrategy getUserUnenhanceStrategy() {
        return userUnenahanceStrategy;
    }

    @SuppressWarnings("unchecked")
    public void registerFilter(Filter<?, ?> filter) {
        this.filtersRegistry.add((Filter<Object, Object>) filter);
    }

    public void reportCurrentState(StringBuilder out) {
        out.append(DIVIDER);
        out.append("\nRegistered object factories: ")
                .append(objectFactoryRegistry.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(objectFactoryRegistry))
                .append(")");
        for (Map.Entry<Type<? extends Object>, ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>> entry : objectFactoryRegistry.entrySet()) {
            out.append("\n  [").append(entry.getKey()).append("] : ").append(entry.getValue());
        }
        out.append(DIVIDER);
        out.append("\nRegistered mappers: ")
                .append(mappersRegistry.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(mappersRegistry))
                .append(")");
        int index = 0;
        for (Mapper<Object, Object> mapper : mappersRegistry) {
            out.append("\n  [").append(index++).append("] : ").append(mapper);
        }
        out.append(DIVIDER);
        out.append("\nRegistered concrete types: ")
                .append(concreteTypeRegistry.size())
                .append(" (approximate size: ")
                .append(humanReadableSizeInMemory(concreteTypeRegistry))
                .append(")");
        for (Map.Entry<java.lang.reflect.Type, Type<?>> entry : concreteTypeRegistry.entrySet()) {
            out.append("\n  [").append(entry.getKey()).append("] : ").append(entry.getValue());
        }
    }

    private class ConverterFactoryFacade implements ConverterFactory {
        private ConverterFactory delegate;

        public ConverterFactoryFacade(ConverterFactory delegate) {
            this.delegate = delegate;
        }

        public void setMapperFacade(MapperFacade mapperFacade) {
            delegate.setMapperFacade(mapperFacade);
        }

        public Converter<Object, Object> getConverter(Type<?> sourceType, Type<?> destinationType) {
            return delegate.getConverter(sourceType, destinationType);
        }

        public Converter<Object, Object> getConverter(String converterId) {
            return delegate.getConverter(converterId);
        }

        public <S, D> void registerConverter(Converter<S, D> converter) {
            delegate.registerConverter(converter);
            if (isBuilding || isBuilt) {
                mapperFacade.factoryModified(DataClassMapperFactory.this);
            }
        }

        public <S, D> void registerConverter(String converterId, Converter<S, D> converter) {
            delegate.registerConverter(converterId, converter);
            if (isBuilding || isBuilt) {
                mapperFacade.factoryModified(DataClassMapperFactory.this);
            }
        }

        public boolean hasConverter(String converterId) {
            return delegate.hasConverter(converterId);
        }

        public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
            return delegate.canConvert(sourceType, destinationType);
        }
    }
}
