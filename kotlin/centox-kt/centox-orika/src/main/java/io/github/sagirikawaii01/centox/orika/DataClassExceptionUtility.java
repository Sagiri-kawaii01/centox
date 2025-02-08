package io.github.sagirikawaii01.centox.orika;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.StateReporter;
import ma.glasnost.orika.impl.ExceptionUtility;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.2.4
 */
public class DataClassExceptionUtility extends ExceptionUtility {
    public static List<String> corePackages = Arrays.asList(
            "java.","javax.","sun.", "sunw.", "com.sun.", "com.ibm.",
            "javassist.", "com.thoughtworks.paranamer.");

    private DataClassMapperFactory mapperFactory;
    private boolean reportStateOnException;

    public DataClassExceptionUtility(DataClassMapperFactory mapperFactory, boolean reportStateOnException) {
        super(null, false);
        this.mapperFactory = mapperFactory;
        this.reportStateOnException = reportStateOnException;
    }

    @Override
    public MappingException newMappingException(String message, Throwable cause) {
        return decorate(new MappingException(message, cause));
    }

    @Override
    public MappingException newMappingException(String message) {
        return newMappingException(message, null);
    }

    @Override
    public MappingException newMappingException(Throwable cause) {
        return decorate(new MappingException(cause));
    }

    @Override
    public MappingException decorate(MappingException me) {
        if (reportStateOnException && !me.containsStateReport()) {
            StringBuilder report = new StringBuilder();
            StateReporter.reportCurrentState(report, mapperFactory);
            report.replace(0, StateReporter.DIVIDER.length(),
                    "\n-----begin dump of current state-----------------------------");
            report.append("\n-----end dump of current state-------------------------------");
            me.setStateReport(report.toString());
        }
        return me;
    }


    public static boolean originatedByOrika(Throwable t) {
        for (StackTraceElement ste: t.getStackTrace()) {
            if (isJreClass(ste.getClassName())) {
                continue;
            } else if (isOrikaClass(ste.getClassName())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private static boolean isOrikaClass(String className) {
        return className.startsWith("ma.glasnost.orika.") && !className.startsWith("ma.glasnost.orika.test.");
    }

    private static boolean isJreClass(String className) {
        for (String pkg: corePackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
