package be.kdg.distrib.skeletonFactory;

public class SkeletonFactory {
    public static Skeleton createSkeleton(Object implementation) {
        return new SkeletonHandler(implementation);
    }
}
