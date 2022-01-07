package com.indeed.util.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/** @author jplaisance */
public final class CommonMethodsTester {
    private static final Logger log = LoggerFactory.getLogger(CommonMethodsTester.class);

    public static void testObjectMethods(Class c) {
        try {
            Object o = c.newInstance();
            testObjectMethods(o);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void testObjectMethods(Object o) {
        try {
            try {
                o.toString();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                o.equals(o);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                o.hashCode();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void testGettersAndSetters(Class c) {
        try {
            testGettersAndSetters(c, c.newInstance());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void testGettersAndSetters(Class c, Object o) {
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(c, Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                try {
                    final Method readMethod = propertyDescriptor.getReadMethod();
                    if (readMethod != null) {
                        readMethod.invoke(o);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                try {
                    final Method writeMethod = propertyDescriptor.getWriteMethod();
                    if (writeMethod != null) {
                        writeMethod.invoke(o, new Object[1]);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
