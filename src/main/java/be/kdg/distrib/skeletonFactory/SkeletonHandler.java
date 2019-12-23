package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;
import be.kdg.distrib.exception.ParseException;
import be.kdg.distrib.logger.Logger;
import be.kdg.distrib.util.InvocationFormatter;
import be.kdg.distrib.util.ObjectParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SkeletonHandler implements Skeleton {
    private final static Logger LOGGER = Logger.getLogger("SkeletonHandler");
    private final MessageManager messageManager;
    private final NetworkAddress networkAddress;
    private final Object implementation;
    private final Map<String, Method> methodMap;

    // -- CONSTRUCTOR ----------------------
    public SkeletonHandler(Object implementation) {
        this.implementation = implementation;
        this.messageManager = new MessageManager();
        this.networkAddress = this.messageManager.getMyAddress();
        this.methodMap = this.createMethodMap();
    }

    @Override
    public void run() {
        // create new thread that runs listen method
        Thread thread = new Thread(this::listen);

        // start new thread
        thread.start();
    }


    @Override
    public NetworkAddress getAddress() {
        return this.networkAddress;
    }

    @Override
    public void handleRequest(MethodCallMessage message) {
        LOGGER.info("Handling method call '%s' for implementation '%s'",
                message.getMethodName(), this.implementation.getClass().getSimpleName());

        try {

            // get invoked method
            Method method = this.methodMap.get(message.getMethodName());
            if (method == null) {
                LOGGER.error("Method with name '%s' has not been found", message.getMethodName());
                throw new NullPointerException("No method with name " + message.getMethodName() + " exists for the current implementation");
            }

            // parse map to arguments and invoke with arguments
            Object[] args = InvocationFormatter.formatCallParameters(method.getParameters(), message.getParameters());

            // invoke method and get return value
            Object returnVal = method.invoke(this.implementation, args);

            // create response as a method call message
            MethodCallMessage response = constructResponse(method, returnVal);

            // send response to originator (sync communication)
            this.messageManager.send(response, message.getOriginator());



        } catch (IllegalAccessException | InvocationTargetException | ParseException e) {
            LOGGER.error("Unable to invoke method '%s' for implementation '%s'",
                    message.getMethodName(), this.implementation.getClass().getSimpleName());
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }


    // -- HELPER METHODS -------------------
    private void listen() {
        // TODO: (optional) allow for graceful shutdown of thread

        if (this.implementation == null) {
            LOGGER.error("Implementation can not be null");
            return; // stops thread
        }

        LOGGER.info("Started listening on %s for implementation '%s'",
                this.networkAddress.toString(),
                this.implementation.getClass().getSimpleName());

        // start listening loop
        while (true) {

            // wait for request - synchronous
            MethodCallMessage request = this.messageManager.wReceive();

            // handle request
            this.handleRequest(request);
        }

    }

    private MethodCallMessage constructResponse(Method method, Object returnValue) throws IllegalAccessException {
        MethodCallMessage response = new MethodCallMessage(this.networkAddress, "result");

        // differentiate response if void
        if (method.getReturnType().equals(Void.TYPE)) {
            response.setParameter("result", "Ok");
            return response;
        }

        // construct response based on returnValue
        Map<String, String> returnParams = InvocationFormatter.encodeObjectAsPairs("result", returnValue);
        returnParams.forEach(response::setParameter);

        // return response
        return response;
    }

    private Map<String, Method> createMethodMap() {
        Class<?> type = implementation.getClass();
        Method[] methods = type.getDeclaredMethods();
        Map<String, Method> map = new HashMap<>();

        // put all methods into a map
        for (Method m : methods) {
            map.put(m.getName(), m);
        }

        return map;
    }
}
