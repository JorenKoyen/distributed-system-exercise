package be.kdg.distrib.util;

import java.util.*;

public class PrimitiveUtils {
    // wrapper class list
    private static final List<Class<?>> WRAPPERS
            = new ArrayList<>(Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class, String.class));

    // primitives to wrappers map
    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
            = new HashMap<Class<?>, Class<?>>() {{
                put(boolean.class, Boolean.class);
                put(char.class, Character.class);
                put(byte.class, Byte.class);
                put(short.class, Short.class);
                put(int.class, Integer.class);
                put(long.class, Long.class);
                put(float.class, Float.class);
                put(double.class, Double.class);
                put(void.class, Void.class);
    }};


    public static boolean isWrappper(Class<?> type) {
        return WRAPPERS.contains(type);
    }
    public static boolean isSimpleType(Class<?> type) {
        return isWrappper(type) || type.isPrimitive();
    }

    public static Class<?> getWrapperType(Class<?> type) {
        // type is already a wrapper class
        if (isWrappper(type)) return type;



        // return wrapper class
        return PRIMITIVES_TO_WRAPPERS.get(type);

    }
}
