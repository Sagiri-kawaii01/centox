package io.github.sagirikawaii01.centox.orika;

import ma.glasnost.orika.*;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.4
 */
public class DataClassBoundMapperFacade<A, B> implements BoundMapperFacade<A, B> {

    final BoundStrategyCache aToB;
    final BoundStrategyCache bToA;
    final BoundStrategyCache aToBInPlace;
    final BoundStrategyCache bToAInPlace;

    protected volatile ObjectFactory<A> objectFactoryA;
    protected volatile ObjectFactory<B> objectFactoryB;


    protected final java.lang.reflect.Type rawAType;
    protected final java.lang.reflect.Type rawBType;
    protected final Type<A> aType;
    protected final Type<B> bType;
    protected final MapperFactory mapperFactory;
    protected final MappingContextFactory contextFactory;

    DataClassBoundMapperFacade(MapperFactory mapperFactory, MappingContextFactory contextFactory, java.lang.reflect.Type typeOfA, java.lang.reflect.Type typeOfB) {
        this.mapperFactory = mapperFactory;
        this.contextFactory = contextFactory;
        this.rawAType = typeOfA;
        this.rawBType = typeOfB;
        this.aType = TypeFactory.valueOf(typeOfA);
        this.bType = TypeFactory.valueOf(typeOfB);
        this.aToB = new BoundStrategyCache(aType, bType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), false);
        this.bToA = new BoundStrategyCache(bType, aType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), false);
        this.aToBInPlace = new BoundStrategyCache(aType, bType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), true);
        this.bToAInPlace = new BoundStrategyCache(bType, aType, mapperFactory.getMapperFacade(), mapperFactory.getUserUnenhanceStrategy(), true);
    }

    public Type<A> getAType() {
        return aType;
    }

    public Type<B> getBType() {
        return bType;
    }

    public B map(A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }

    public A mapReverse(B source) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapReverse(source, context);
        } finally {
            contextFactory.release(context);
        }
    }

    public B map(A instanceA, B instanceB) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(instanceA, instanceB, context);
        } finally {
            contextFactory.release(context);
        }
    }

    public A mapReverse(B instanceB, A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapReverse(instanceB, instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }

    @SuppressWarnings("unchecked")
    public B map(A instanceA, MappingContext context) {
        B result = (B) context.getMappedObject(instanceA, bType);
        if (result == null && instanceA != null) {
            result = (B) aToB.getStrategy(instanceA, context).map(instanceA, null, context);
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    public A mapReverse(B instanceB, MappingContext context) {
        A result = (A) context.getMappedObject(instanceB, aType);
        if (result == null && instanceB != null) {
            result = (A) bToA.getStrategy(instanceB, context).map(instanceB, null, context);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public B map(A instanceA, B instanceB, MappingContext context) {
        B result = (B) context.getMappedObject(instanceA, bType);
        if (result == null && instanceA != null) {
            result = (B) aToBInPlace.getStrategy(instanceA, context).map(instanceA, instanceB, context);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public A mapReverse(B instanceB, A instanceA, MappingContext context) {
        A result = (A) context.getMappedObject(instanceB, aType);
        if (result == null && instanceB != null) {
            result = (A) bToAInPlace.getStrategy(instanceB, context).map(instanceB, instanceA, context);
        }
        return result;
    }

    public String toString() {
        String srcName = TypeFactory.nameOf(aType, bType);
        String dstName = TypeFactory.nameOf(bType, aType);
        return getClass().getSimpleName() + "<" + srcName +", " + dstName + ">";
    }

    public B newObject(A source, MappingContext context) {
        if (objectFactoryB == null) {
            synchronized(this) {
                if (objectFactoryB == null) {
                    objectFactoryB = mapperFactory.lookupObjectFactory(bType, aType, context);
                }
            }
        }
        return objectFactoryB.create(source, context);
    }

    public A newObjectReverse(B source, MappingContext context) {
        if (objectFactoryA == null) {
            synchronized(this) {
                if (objectFactoryA == null) {
                    objectFactoryA = mapperFactory.lookupObjectFactory(aType, bType, context);
                }
            }
        }
        return objectFactoryA.create(source, context);
    }

    private static class BoundStrategyCache {
        private final Type<?> aType;
        private final Type<?> bType;
        private final boolean inPlace;
        private final MapperFacade mapperFacade;
        private final UnenhanceStrategy unenhanceStrategy;
        protected final ConcurrentHashMap<Class<?>, MappingStrategy> strategies = new ConcurrentHashMap<Class<?>, MappingStrategy>(2);

        private volatile Class<?> idClass;
        private volatile MappingStrategy defaultStrategy;

        private BoundStrategyCache(Type<?> aType, Type<?> bType, MapperFacade mapperFacade, UnenhanceStrategy unenhanceStrategy, boolean inPlace) {
            this.aType = aType;
            this.bType = bType;
            this.mapperFacade = mapperFacade;
            this.unenhanceStrategy = unenhanceStrategy;
            this.inPlace = inPlace;
        }

        public MappingStrategy getStrategy(Object sourceObject, MappingContext context) {
            MappingStrategy strategy = null;
            Class<?> sourceClass = getClass(sourceObject);
            if (defaultStrategy != null && sourceClass.equals(idClass)) {
                strategy = defaultStrategy;
            } else if (defaultStrategy == null) {
                synchronized(this) {
                    if (defaultStrategy == null) {
                        defaultStrategy = mapperFacade.resolveMappingStrategy(sourceObject, aType, bType, inPlace, context);
                        idClass = sourceClass;
                        strategies.put(idClass, defaultStrategy);
                    }
                }
                strategy = defaultStrategy;
            } else {
                strategy = strategies.get(sourceClass);
                if (strategy == null) {
                    strategy = mapperFacade.resolveMappingStrategy(sourceObject, aType, bType, inPlace, context);
                    strategies.put(sourceClass, strategy);
                }
            }

            context.setResolvedSourceType(strategy.getAType());
            context.setResolvedDestinationType(strategy.getBType());

            return strategy;
        }

        protected Class<?> getClass(Object object) {
            if (this.unenhanceStrategy == null) {
                return object.getClass();
            } else {
                return unenhanceStrategy.unenhanceObject(object, TypeFactory.TYPE_OF_OBJECT).getClass();
            }
        }
    }
}
