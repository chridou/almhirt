package almhirt.akkax;

import akka.util.Unsafe;

public class AbstractAlmCircuitBreaker {
    protected final static long stateOffset;
    static {
        try {
            stateOffset = Unsafe.instance.objectFieldOffset(AlmCircuitBreakerImpl.class.getDeclaredField("_currentStateDoNotCallMeDirectly"));
        } catch(Throwable t){
            throw new ExceptionInInitializerError(t);
        }
    }
}
