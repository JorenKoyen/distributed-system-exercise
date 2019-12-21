package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;
import be.kdg.distrib.logger.Logger;
import be.kdg.distrib.util.InvocationFormatter;
import be.kdg.distrib.util.ObjectParser;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static be.kdg.distrib.util.PrimitiveUtils.*;

public class StubInvocationHandler implements InvocationHandler {
    private final static Logger LOGGER = Logger.getLogger("InvocationHandler");
    private final NetworkAddress receiver;
    private final MessageManager messageManager;

    // -- CONSTRUCTOR ----------------------
    public StubInvocationHandler(NetworkAddress receiver) {
        this.receiver = receiver;
        this.messageManager = new MessageManager();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // parse invoked method as method call message
        MethodCallMessage invokedMessage = InvocationFormatter.parseInvokeCall(method, args, this.messageManager.getMyAddress());

        // log incoming method invocation
        LOGGER.info("Method '%s' has been invoked with expected return type '%s'", invokedMessage.getMethodName(), method.getReturnType().getSimpleName());
        invokedMessage.getParameters().forEach((key, value) -> LOGGER.info("PARAM: %s = %s", key, value));

        // send method call message to receiver (stub)
        this.messageManager.send(invokedMessage, this.receiver);

        // receive remote method call message from remote receiver (stub)
        MethodCallMessage response = this.messageManager.wReceive();

        // parse method call message to return object
        Map<String, String> params = ObjectParser.getKeysStartingWith("result", response.getParameters());
        return ObjectParser.parse(method.getReturnType(), params);
    }


}
