package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class StubFactory {
    public static Object createStub(Class stub, String address, int port) {

        // setup network address for receiving party
        NetworkAddress receiver = new NetworkAddress(address, port);

        // create stub invocation handler
        InvocationHandler handler = new StubInvocationHandler(receiver);

        // create and return stub
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {stub}, handler);
    }
}
