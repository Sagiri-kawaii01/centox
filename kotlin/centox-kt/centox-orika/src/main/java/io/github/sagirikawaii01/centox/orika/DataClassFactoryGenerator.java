package io.github.sagirikawaii01.centox.orika;


import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.impl.GeneratedObjectFactory;
import ma.glasnost.orika.impl.generator.*;
import ma.glasnost.orika.metadata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.4
 */
public class DataClassFactoryGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectFactoryGenerator.class);

    private final ConstructorResolverStrategy constructorResolverStrategy;
    private final MapperFactory mapperFactory;
    private final String nameSuffix;

    public DataClassFactoryGenerator(MapperFactory mapperFactory, ConstructorResolverStrategy constructorResolverStrategy,
                                  CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.nameSuffix = String.valueOf(System.nanoTime());
        this.constructorResolverStrategy = constructorResolverStrategy;
    }


    public GeneratedObjectFactory build(Type<?> type, Type<?> sourceType, MappingContext context) {

        String className = type.getSimpleName() + "_" + sourceType.getSimpleName() + "_ObjectFactory" + nameSuffix;
        className = prependPackageName(getPackageName(type), className);
        try {
            StringBuilder logDetails;
            if (LOGGER.isDebugEnabled()) {
                logDetails = new StringBuilder();
                logDetails.append("Generating new object factory for (" + type + ")");
            } else {
                logDetails = null;
            }

            final SourceCodeContext factoryCode = new SourceCodeContext(className, GeneratedObjectFactory.class, context, logDetails);

            UsedTypesContext usedTypes = new UsedTypesContext();
            UsedConvertersContext usedConverters = new UsedConvertersContext();
            UsedMapperFacadesContext usedMapperFacades = new UsedMapperFacadesContext();

            addCreateMethod(factoryCode, usedTypes, usedConverters, usedMapperFacades, type, sourceType, context, logDetails);

            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());

            if (logDetails != null) {
                LOGGER.debug(logDetails.toString());
            }

            return objectFactory;

        } catch (final Exception e) {
            if (e instanceof MappingException) {
                throw (MappingException) e;
            } else {
                throw new MappingException("exception while creating object factory for " + type.getName(), e);
            }
        }
    }

    private static String getPackageName(Type<?> type) {
        Package typePackage = type.getRawType().getPackage();
        return typePackage == null ? "" : typePackage.getName();
    }
    private static String prependPackageName(String packageName, String className) {
        return packageName.isEmpty() || packageName.startsWith("java.") ? className : packageName + "." + className;
    }

    private void addCreateMethod(SourceCodeContext code, UsedTypesContext usedTypes, UsedConvertersContext usedConverters,
                                 UsedMapperFacadesContext usedMappers, Type<?> type, Type<?> sourceType, MappingContext mappingContext, StringBuilder logDetails) {

        final StringBuilder out = new StringBuilder();
        out.append("public Object create(Object s, " + MappingContext.class.getCanonicalName() + " mappingContext) {");
        out.append(format("if(s == null) throw new %s(\"source object must be not null\");",
                IllegalArgumentException.class.getCanonicalName()));

        out.append(addSourceClassConstructor(code, type, sourceType, mappingContext, logDetails));
        out.append(addUnmatchedSourceHandler(code, type, sourceType, mappingContext, logDetails));

        out.append("\n}");

        code.addMethod(out.toString());
    }


    private String addSourceClassConstructor(SourceCodeContext code, Type<?> destinationType, Type<?> sourceType,
                                             MappingContext mappingContext, StringBuilder logDetails) {

        MapperKey mapperKey = new MapperKey(sourceType, destinationType);
        ClassMap<Object, Object> classMap = mapperFactory.getClassMap(mapperKey);

        if (classMap == null) {
            classMap = mapperFactory.getClassMap(new MapperKey(destinationType, sourceType));
        }

        StringBuilder out = new StringBuilder();
        if (classMap != null) {
            if (destinationType.isArray()) {
                out.append(addArrayClassConstructor(code, destinationType, sourceType, classMap.getFieldsMapping().size()));
            } else {

                out.append(format("if (s instanceof %s) {", sourceType.getCanonicalName()));
                out.append(format("%s source = (%s) s;", sourceType.getCanonicalName(), sourceType.getCanonicalName()));
                out.append("\ntry {\n");

                ConstructorResolverStrategy.ConstructorMapping<?> constructorMapping = (ConstructorResolverStrategy.ConstructorMapping<?>) constructorResolverStrategy.resolve(classMap,
                        destinationType);
                Constructor<?> constructor = constructorMapping.getConstructor();
                if (constructor == null) {
                    throw new IllegalArgumentException("no suitable constructors found for " + destinationType);
                } else if (logDetails != null) {
                    logDetails.append("\n\tUsing constructor: " + constructor);
                }

                List<FieldMap> properties = constructorMapping.getMappedFields();
                Type<?>[] constructorArguments = constructorMapping.getParameterTypes();

                int argIndex = 0;

                for (FieldMap fieldMap : properties) {
                    VariableRef v = new VariableRef(constructorArguments[argIndex], "arg" + argIndex++);
                    if (fieldMap.getSource().getName().isEmpty()) {
                        String line = v.declare();
                        if (fieldMap.getDestination().getType().getRawType().isAssignableFrom(List.class)) {
                            line = line.substring(0, line.indexOf('=') + 1) + "java.util.Collections.emptyList()";
                        } else {
                            line = line.substring(0, line.indexOf('=') + 2) + "null";
                        }
                        out.append(statement(line));
                    } else {
                        VariableRef s = new VariableRef(fieldMap.getSource(), "source");
                        VariableRef destOwner = new VariableRef(fieldMap.getDestination(), "");
                        v.setOwner(destOwner);
                        out.append(statement(v.declare()));
                        out.append(code.mapFields(fieldMap, s, v));
                    }
                }

                out.append(format("return new %s(", destinationType.getCanonicalName()));
                for (int i = 0; i < properties.size(); i++) {
                    out.append(format("arg%d", i));
                    if (i < properties.size() - 1) {
                        out.append(",");
                    }
                }
                out.append(");");

                /*
                 * Any exceptions thrown calling constructors should be
                 * propagated
                 */
                append(out, "\n} catch (java.lang.Exception e) {\n", "if (e instanceof RuntimeException) {\n",
                        "throw (RuntimeException)e;\n", "} else {", "throw new java.lang.RuntimeException("
                                + "\"Error while constructing new " + destinationType.getSimpleName() + " instance\", e);", "\n}\n}\n}");
            }
        }
        return out.toString();
    }

    /**
     * Adds a default constructor call (where possible) as fail-over case when
     * no specific source type has been matched.
     *
     * @param code
     * @param type
     * @param mappingContext
     * @param logDetails
     * @return
     */
    private String addUnmatchedSourceHandler(SourceCodeContext code, Type<?> type, Type<?> sourceType, MappingContext mappingContext,
                                             StringBuilder logDetails) {
        StringBuilder out = new StringBuilder();
        for (Constructor<?> constructor : type.getRawType().getConstructors()) {
            if (constructor.getParameterTypes().length == 0 && Modifier.isPublic(constructor.getModifiers())) {
                out.append(format("return new %s();", type.getCanonicalName()));
                break;
            }
        }

        /*
         * If no default constructor field exists, attempt to locate and call a
         * constructor which takes a single argument of source type
         */
        if (out.length() == 0) {
            for (Constructor<?> constructor : type.getRawType().getConstructors()) {
                if (constructor.getParameterTypes().length == 1 && Modifier.isPublic(constructor.getModifiers())) {
                    Type<?> argType = TypeFactory.valueOf(constructor.getGenericParameterTypes()[0]);
                    if (argType.isAssignableFrom(sourceType)) {
                        out.append(format("return new %s((%s)s);", type.getCanonicalName(), sourceType.getCanonicalName()));
                        break;
                    }
                }
            }
        }

        if (out.length() == 0) {

            out.append(format(
                    "throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class for constructing instances of "
                            + type.getCanonicalName() + "\");", IllegalArgumentException.class.getCanonicalName()));
        }

        return out.toString();
    }

    /**
     * @param type
     * @param size
     */
    private String addArrayClassConstructor(SourceCodeContext code, Type<?> type, Type<?> sourceType, int size) {
        return format("if (s instanceof %s) {", sourceType.getCanonicalName()) + "return new "
                + type.getRawType().getComponentType().getCanonicalName() + "[" + size + "];" + "\n}";
    }
}
