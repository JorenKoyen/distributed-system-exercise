package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationTargetException;

public interface Skeleton {
    void run();
    NetworkAddress getAddress();
    void handleRequest(MethodCallMessage message);
}
