package dllv.carina.carina.demo;

import com.zebrunner.carina.api.APIMethodPoller;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import dllv.carina.carina.demo.api.PostAirlineMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import java.util.Random;
import java.lang.invoke.MethodHandles;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class APIAirlineTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test()
    @MethodOwner(owner = "qpsdemo")
    public void testCreationAirline() throws Exception{
        LOGGER.info("test");
        PostAirlineMethod api = new PostAirlineMethod();
        api.setProperties("api/airline/airline.properties");
        int randomId = new Random().nextInt(89999) + 10000;
        api.addProperty("random_id", randomId);
        api.addProperty("random_name", "AutoTest-" + randomId);

        AtomicInteger counter = new AtomicInteger(0);

        api.callAPIWithRetry()
                .withLogStrategy(APIMethodPoller.LogStrategy.ALL)
                .peek(rs -> counter.getAndIncrement())
                .until(rs -> counter.get() == 1)
                .pollEvery(1, ChronoUnit.SECONDS)
                .stopAfter(10, ChronoUnit.SECONDS)
                .execute();
        api.validateResponse();
    }

}
