package exchange.tests.util;

import exchange.core.common.api.ApiCommand;
import exchange.core.common.api.ApiPersistState;
import exchange.core.common.cmd.CommandResultCode;
import exchange.core.common.config.InitialStateConfiguration;
import exchange.core.common.config.PerformanceConfiguration;
import exchange.core.common.config.SerializationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.Is;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
public class PersistenceTestsModule {

    // TODO current persistence test does not cover positions serialization

    public static void persistenceTestImpl(final PerformanceConfiguration performanceConfiguration,
                                           final TestDataParameters testDataParameters,
                                           final int iterations) throws InterruptedException, ExecutionException {

        for (int iteration = 0; iteration < iterations; iteration++) {

            final long stateId;

//            long t = System.currentTimeMillis();

            final ExchangeTestContainer.TestDataFutures testDataFutures = ExchangeTestContainer.prepareTestDataAsync(testDataParameters, iteration);

            final String exchangeId = String.format("%012X", System.currentTimeMillis());
            final InitialStateConfiguration firstStartConfig = InitialStateConfiguration.cleanStart(exchangeId);

            final long originalPrefillStateHash;
            final float originalPerfMt;

            try (final ExchangeTestContainer container = ExchangeTestContainer.create(performanceConfiguration, firstStartConfig, SerializationConfiguration.DISK_SNAPSHOT_ONLY)) {

                container.loadSymbolsUsersAndPrefillOrders(testDataFutures);

                log.info("Creating snapshot...");
                stateId = System.currentTimeMillis() * 1000 + iteration;
                final ApiPersistState apiPersistState = ApiPersistState.builder().dumpId(stateId).build();
                try (ExecutionTime ignore = new ExecutionTime(t -> log.debug("Snapshot {} created in {}", stateId, t))) {
                    final CommandResultCode resultCode = container.getApi().submitCommandAsync(apiPersistState).get();
                    assertThat(resultCode, Is.is(CommandResultCode.SUCCESS));
                }

                log.info("Requesting state hash...");
                originalPrefillStateHash = container.requestStateHash();

                log.info("Benchmarking original state...");

                originalPerfMt = container.executeTestingThreadPerfMtps(() -> {
                    final List<ApiCommand> apiCommandsBenchmark = testDataFutures.genResult.get().getApiCommandsBenchmark().join();
                    container.getApi().submitCommandsSync(apiCommandsBenchmark);
                    return apiCommandsBenchmark.size();
                });

                assertTrue(container.totalBalanceReport().isGlobalBalancesAllZero());

                log.info("{}. original throughput: {} MT/s", iteration, String.format("%.3f", originalPerfMt));
            }


            System.gc();
            Thread.sleep(200);

            final InitialStateConfiguration fromSnapshotConfig = InitialStateConfiguration.fromSnapshotOnly(exchangeId, stateId, 0);

            log.debug("Creating new exchange from persisted state...");
            final long tLoad = System.currentTimeMillis();
            try (final ExchangeTestContainer recreatedContainer = ExchangeTestContainer.create(performanceConfiguration, fromSnapshotConfig, SerializationConfiguration.DISK_SNAPSHOT_ONLY)) {

                // simple sync query in order to wait until core is started to respond
                recreatedContainer.totalBalanceReport();

                float loadTimeSec = (float) (System.currentTimeMillis() - tLoad) / 1000.0f;
                log.debug("Load+start time: {}s", String.format("%.3f", loadTimeSec));

                log.info("Requesting state hash...");
                final long restoredPrefillStateHash = recreatedContainer.requestStateHash();
                assertThat(restoredPrefillStateHash, is(originalPrefillStateHash));

                assertTrue(recreatedContainer.totalBalanceReport().isGlobalBalancesAllZero());
                log.info("Restored snapshot is valid, benchmarking original state...");

                final float perfMt = recreatedContainer.executeTestingThreadPerfMtps(() -> {
                    final List<ApiCommand> apiCommandsBenchmark = testDataFutures.genResult.get().getApiCommandsBenchmark().join();
                    recreatedContainer.getApi().submitCommandsSync(apiCommandsBenchmark);
                    return apiCommandsBenchmark.size();
                });

                final float perfRatioPerc = perfMt / originalPerfMt * 100f;
                log.info("{}. restored throughput: {} MT/s ({}%)", iteration, String.format("%.3f", perfMt), String.format("%.1f", perfRatioPerc));
            }

            System.gc();
            Thread.sleep(200);
        }

    }


    private static final Consumer<? super Object> IGNORING_CONSUMER = x -> {
    };

}
