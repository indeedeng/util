package com.indeed.util.core;

import org.junit.Before;
import org.junit.Test;

import static com.indeed.util.core.DataLoadingRunnable.ReloadState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** @author casey (casey@indeed.com) */
public class TestDataLoadingRunnable {
    private FakeDataLoadingRunnable dataLoadingRunnable;

    @Before
    public void setup() {
        dataLoadingRunnable = new FakeDataLoadingRunnable("TestDataLoadingRunnable");
    }

    @Test
    public void testLoadComplete() {
        dataLoadingRunnable.loadComplete();
        assertEquals(ReloadState.RELOADED, dataLoadingRunnable.getReloadState());

        // asserting super's behavior
        assertTrue(dataLoadingRunnable.lastLoadWasSuccessful);
        assertNotNull(dataLoadingRunnable.getSecondsSinceLastLoad());
    }

    @Test
    public void testLoadFailed() {
        dataLoadingRunnable.loadFailed();
        assertEquals(ReloadState.FAILED, dataLoadingRunnable.getReloadState());

        // asserting super's behavior
        assertFalse(dataLoadingRunnable.lastLoadWasSuccessful);
        assertNotNull(dataLoadingRunnable.getSecondsSinceLastFailedLoad());
    }

    @Test
    public void testLoadNotChanged() {
        dataLoadingRunnable.loadNotChanged();
        assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
    }

    @Test
    public void testLoadNotChanged_LastLoadErroring() {
        dataLoadingRunnable.loadFailed();
        dataLoadingRunnable.loadNotChanged();
        assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
        assertNotNull(dataLoadingRunnable.getSecondsSinceLastLoad());

        // asserting super's behavior
        assertTrue(dataLoadingRunnable.lastLoadWasSuccessful);
        assertNotNull(dataLoadingRunnable.getSecondsSinceLastLoad());
    }

    @Test
    public void testLoadNotChanged_LastLoadSuccessful() {
        dataLoadingRunnable.loadComplete();
        dataLoadingRunnable.loadNotChanged();
        assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
        assertNotNull(dataLoadingRunnable.getSecondsSinceLastLoad());

        // asserting super's behavior
        assertTrue(dataLoadingRunnable.lastLoadWasSuccessful);
        assertNotNull(dataLoadingRunnable.getSecondsSinceLastLoad());
    }

    @Test
    public void testFinishLoadWithReloadState_Reloaded_UpdatesReloadState() {
        final String expectedDataVersion = "data version";
        dataLoadingRunnable.finishLoadWithReloadState(ReloadState.RELOADED, expectedDataVersion);
        assertEquals(ReloadState.RELOADED, dataLoadingRunnable.getReloadState());
        assertEquals(
                "Data version was not set",
                expectedDataVersion,
                dataLoadingRunnable.getDataVersion());
    }

    @Test
    public void testFinishLoadWithReloadState_Failed_UpdatesReloadState() {
        dataLoadingRunnable.finishLoadWithReloadState(
                ReloadState.FAILED, "Data version that should not propagate");
        assertEquals(ReloadState.FAILED, dataLoadingRunnable.getReloadState());
        assertNull(dataLoadingRunnable.getDataVersion());
    }

    @Test
    public void testFinishLoadWithReloadState_NoChange_UpdatesReloadState() {
        dataLoadingRunnable.finishLoadWithReloadState(
                ReloadState.NO_CHANGE, "Data version that should not propagate");
        assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
        assertNull(dataLoadingRunnable.getDataVersion());
    }

    @Test
    public void testFinishLoadWithReloadState_TestDataVersion() {
        final String expectedDataVersion = "data version";
        dataLoadingRunnable.finishLoadWithReloadState(ReloadState.RELOADED, expectedDataVersion);
        assertEquals(ReloadState.RELOADED, dataLoadingRunnable.getReloadState());
        assertEquals(
                "Data version was not set",
                expectedDataVersion,
                dataLoadingRunnable.getDataVersion());

        dataLoadingRunnable.finishLoadWithReloadState(
                ReloadState.NO_CHANGE, "Data version that should not propagate");
        assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
        assertEquals(
                "Data version was not set",
                expectedDataVersion,
                dataLoadingRunnable.getDataVersion());

        dataLoadingRunnable.finishLoadWithReloadState(
                ReloadState.FAILED, "Data version that should not propagate");
        assertEquals(ReloadState.FAILED, dataLoadingRunnable.getReloadState());
        assertEquals(
                "Data version was not set",
                expectedDataVersion,
                dataLoadingRunnable.getDataVersion());
    }

    @Test
    public void testRun_Reloaded() {
        dataLoadingRunnable.run();

        assertNotNull(
                dataLoadingRunnable
                        .getSecondsSinceLastLoadCheck()); // Assert that updateLastLoadCheck was
        // called
        assertTrue(dataLoadingRunnable.lastLoadWasSuccessful);
        assertNotNull(
                dataLoadingRunnable
                        .getSecondsSinceLastLoad()); // Assert that loadComplete was called
        assertEquals(ReloadState.RELOADED, dataLoadingRunnable.getReloadState());
    }

    @Test
    public void testRun_NoChange_afterSuccess() {
        // SETUP - we've loaded something at some point in the past and it worked
        final Integer secondsSinceLastLoadCheck;
        final Integer secondsSinceLastLoad;
        {
            dataLoadingRunnable.loadBehavior = ReloadState.RELOADED;
            dataLoadingRunnable.run();
            secondsSinceLastLoadCheck = dataLoadingRunnable.getSecondsSinceLastLoadCheck();
            secondsSinceLastLoad = dataLoadingRunnable.getSecondsSinceLastLoad();
            assertNotNull(secondsSinceLastLoadCheck); // Assert that updateLastLoadCheck was called
            assertTrue(
                    "last load was successful, because it was",
                    dataLoadingRunnable.lastLoadWasSuccessful);
            assertNotNull(secondsSinceLastLoad); // Assert that loadComplete was called
            assertEquals(ReloadState.RELOADED, dataLoadingRunnable.getReloadState());
        }

        // and then it tried again, and found the same thing
        {
            dataLoadingRunnable.loadBehavior = ReloadState.NO_CHANGE;
            dataLoadingRunnable.run();

            assertEquals(
                    secondsSinceLastLoad,
                    dataLoadingRunnable
                            .getSecondsSinceLastLoad()); // Assert that loadComplete was not called
            assertTrue(
                    "last load was successful, and unchanged from what is currently in memory",
                    dataLoadingRunnable.lastLoadWasSuccessful);
            assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
        }
    }

    @Test
    public void testRun_NoChange_afterFailure() {
        // SETUP - we've loaded something at some point in the past and it worked
        final Integer secondsSinceLastLoadCheck;
        final Integer secondsSinceLastLoad;
        {
            dataLoadingRunnable.loadBehavior = ReloadState.RELOADED;
            dataLoadingRunnable.run();
            secondsSinceLastLoadCheck = dataLoadingRunnable.getSecondsSinceLastLoadCheck();
            secondsSinceLastLoad = dataLoadingRunnable.getSecondsSinceLastLoad();
            assertNotNull(secondsSinceLastLoadCheck); // Assert that updateLastLoadCheck was called
            assertTrue(
                    "last load was successful, because it was",
                    dataLoadingRunnable.lastLoadWasSuccessful);
            assertNotNull(secondsSinceLastLoad); // Assert that loadComplete was called
            assertEquals(ReloadState.RELOADED, dataLoadingRunnable.getReloadState());
        }

        // but then someone broke the hard drive and it failed
        {
            dataLoadingRunnable.loadBehavior = ReloadState.FAILED;
            dataLoadingRunnable.run();

            assertEquals(
                    secondsSinceLastLoad,
                    dataLoadingRunnable
                            .getSecondsSinceLastLoad()); // Assert that loadComplete was not called
            assertFalse(dataLoadingRunnable.lastLoadWasSuccessful);
            assertFalse(
                    "last load failed because the hard drive is gone",
                    dataLoadingRunnable.lastLoadWasSuccessful);
            assertEquals(ReloadState.FAILED, dataLoadingRunnable.getReloadState());
        }

        // and the hard drive got fixed, but the artifact is still unchanged
        {
            dataLoadingRunnable.loadBehavior = ReloadState.NO_CHANGE;
            dataLoadingRunnable.run();

            assertEquals(
                    secondsSinceLastLoad,
                    dataLoadingRunnable
                            .getSecondsSinceLastLoad()); // Assert that loadComplete was not called
            assertTrue(
                    "last load was successful, in that it loaded something but it was unchanged from what is currently in memory",
                    dataLoadingRunnable.lastLoadWasSuccessful);
            assertEquals(ReloadState.NO_CHANGE, dataLoadingRunnable.getReloadState());
        }
    }

    @Test
    public void testRun_Failed() {
        dataLoadingRunnable.loadBehavior = ReloadState.FAILED;
        dataLoadingRunnable.run();

        assertNull(
                dataLoadingRunnable
                        .getSecondsSinceLastLoad()); // Assert that loadComplete was not called
        assertFalse(dataLoadingRunnable.lastLoadWasSuccessful);
        assertEquals(ReloadState.FAILED, dataLoadingRunnable.getReloadState());
    }

    /**
     * Class used to test {@link DataLoadingRunnable}.
     *
     * <p>Set {@link #loadBehavior} to determine how {@link FakeDataLoadingRunnable} should respond
     */
    private class FakeDataLoadingRunnable extends DataLoadingRunnable {
        public ReloadState loadBehavior = ReloadState.RELOADED;

        public FakeDataLoadingRunnable(final String namespace) {
            super(namespace);
        }

        @Override
        public boolean load() {
            return finishLoadWithReloadState(loadBehavior, "");
        }
    }
}
