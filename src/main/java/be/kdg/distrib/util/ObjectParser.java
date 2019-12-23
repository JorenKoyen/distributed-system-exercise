package be.kdg.distrib.util;

import be.kdg.distrib.exception.ParseException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static be.kdg.distrib.util.PrimitiveUtils.getWrapperType;
import static be.kdg.distrib.util.PrimitiveUtils.isSimpleType;

public class ObjectParser {

    /**
     * Parses an object to the type passed as parameter.
     * @param type The type which needs to be returned
     * @param args Arguments which map to the corresponding field names
     * @return Object of the type passed as parameter
     */
    public static Object parse(Class<?> type, Map<String, String> args) {
        // return null if type is void
        if (type.equals(Void.TYPE)) return null;

        // check if args received
        if (args == null || args.size() == 0) {
            throw new IllegalArgumentException("Expected arguments to be passed when not expecting a void");
        }

        // parse simple type
        if (isSimpleType(type)) {

            // when a simple type the argument should be in an empty key
            String val = args.get("");

            // get wrapper class of type
            Class<?> wrapper = getWrapperType(type);

            // return parsed object
            return parseSimpleType(wrapper, val);
        }

        // +++ complex types +++
        // create instance of complex type
        Object instance = null;
        try {
            instance = type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ParseException("Unable to instantiate new object of type " + type.getSimpleName(), e);
        }

        // get all declared fields of type
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {

            // make field accessible
            f.setAccessible(true);

            // get keys associated with field
            Map<String, String> fieldKeys = getKeysStartingWith(f.getName(), args);

            // parse object to field type
            Object val = parse(f.getType(), fieldKeys);

            // set value to field
            try {
                f.set(instance, val);
            } catch (IllegalAccessException e) {
                throw new ParseException("Unable to set new value to field " + f.getName(), e);
            }
        }

        return instance;
    }

    /**
     * Removes the prefix from all the matching keys in the map
     * @param prefix Prefix which needs to be removed from the map
     * @param map Map which needs to be modified
     * @return A new map with the matched and modified keys
     */
    public static Map<String, String> getKeysStartingWith(String prefix, Map<String, String> map) {
        Map<String, String> matchedKeys = new HashMap<>();
        map.forEach((key, value) -> {
            if (key.startsWith(prefix)) {

                // create new key name
                String refactoredName = key.length() == prefix.length() ? "" : key.substring(prefix.length() + 1);

                // insert new entry in matchedKeys
                matchedKeys.put(refactoredName, value);

            }
        });
        return matchedKeys;
    }

    /**
     * Parses a string value to a simple type.
     * THe type passed must be a wrapper class, so it can use the constructor notation to parse.
     * @param type Type which needs to returned (must be wrapper class)
     * @param val Value which needs to be parsed
     * @return An object as the passed type
     */
    private static Object parseSimpleType(Class<?> type, String val) {
        // return first character of val if parsed to char
        if (type.equals(Character.class)) return val.toCharArray()[0];

        try {
            // get constructor from type with a single string param
            Constructor<?> ctor = null;
            ctor = type.getConstructor(String.class);


            // make constructor accessible
            ctor.setAccessible(true);

            // return instance of object
            return ctor.newInstance(val);

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            String em = String.format("Unable to parse '%s' to type %s", val, type.getSimpleName());
            throw new ParseException(em, e);
        }

    }
}
