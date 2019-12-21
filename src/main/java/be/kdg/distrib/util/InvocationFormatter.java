package be.kdg.distrib.util;

import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static be.kdg.distrib.util.PrimitiveUtils.isSimpleType;

public class InvocationFormatter {

    // == PUBLIC METHODS =============================
    /**
     * Parses an invoked method call to a MethodCallMessage.
     * @param method The method that has been invoked
     * @param args The arguments associated with the method
     * @return A new MethodCallMessage that represents the invoked method call
     */
    public static MethodCallMessage parseInvokeCall(Method method, Object[] args, NetworkAddress origin) throws IllegalAccessException {
        // create method call message object
        MethodCallMessage methodCallMessage = new MethodCallMessage(origin, method.getName());

        // get all mapped arguments
        Map<String, String> arguments = mapArgs(method.getParameters(), args);

        // add arguments to method call message
        arguments.forEach(methodCallMessage::setParameter);

        // return method call message
        return methodCallMessage;
    }

    // == PRIVATE METHODS ============================
    /**
     * Maps arguments and parameters as name value pairs, by using the parameter name as key
     * and the object as value. Complex object get nested keys prefixed by the associated parameter.
     * @param parameters The parameters associated with the arguments
     * @param args The arguments that match the parameters
     * @return A list of NameValuePairs
     * @throws IllegalAccessException Thrown when the length of the parameters do not match the length of the arguments
     */
    private static Map<String, String> mapArgs(Parameter[] parameters, Object[] args) throws IllegalAccessException {
        // return empty list if no params or args
        if (parameters == null || args == null) return Collections.emptyMap();

        // check if amount of params matches amount of args
        if (parameters.length != args.length)
            throw new IllegalArgumentException("The amount of parameters expected and arguments passed does not match");

        // map arguments to name value pairs
        Map<String, String> pairs = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            Parameter p = parameters[i];
            Object o = args[i];

            // encode object and add to pairs
            pairs.putAll(encodeObjectAsPairs(p.getName(), o));
        }

        // return name value pairs
        return pairs;
    }

    /**
     * Encodes an a single object to a list of NameValuePairs with the basename as their
     * key value. Complex objects are mapped under the same basename but also use the name of the
     * field as their identifier. (Keeps encoding until a wrapper class or primitive type is found)
     * @param baseName Name used as key value
     * @param object Object that must be encoded
     * @return Returns a list of NameValuePairs
     * @throws IllegalAccessException Thrown when unable to access the getter of a specific field
     */
    private static Map<String, String> encodeObjectAsPairs(String baseName, Object object) throws IllegalAccessException {
        // return single name value pair if object is a primitive or wrapper type
        if (isSimpleType(object.getClass())) {
            return Map.of(baseName, String.valueOf(object));
        }

        // get fields of non primitive object
        Field[] fields = object.getClass().getDeclaredFields();

        // loop over fields in non primitive object
        Map<String, String> pairs = new HashMap<>();
        for (Field f : fields) {

            // make field accessible
            f.setAccessible(true);

            // use recursion to get nested objects
            String extendedBaseName = baseName + "." + f.getName();
            pairs.putAll(encodeObjectAsPairs(extendedBaseName, f.get(object)));

        }

        return pairs;
    }

}
