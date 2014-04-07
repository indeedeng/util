package com.indeed.util.core.reference;

import junit.framework.TestCase;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author pwp
 */

public class TestSharedReference extends TestCase {
    // A simple test class that starts life as open, and lets us ask whether its close() has been called.
    public static class TestObj implements Closeable {
        private boolean isOpen = false;

        public TestObj() {
            isOpen = true;
        }

        public void close() {
            isOpen = false;
        }

        public boolean isOpen() {
            return isOpen;
        }
    }

    public void testCreateClose() throws IOException {
        final TestObj objOne = new TestObj();
        final SharedReference<TestObj> refOne = SharedReference.create(objOne);
        assertSame(objOne, refOne.get());
        assertEquals(1, refOne.getRefCount());
        assertTrue(objOne.isOpen());

        final TestObj objTwo = new TestObj();
        final SharedReference<TestObj> refTwo = SharedReference.create(objTwo);
        assertSame(objTwo, refTwo.get());
        assertEquals(1, refTwo.getRefCount());
        assertTrue(objTwo.isOpen());
        assertNotSame(objOne, objTwo);

        refOne.close();
        assertFalse(objOne.isOpen());
        assertEquals(0, refOne.getRefCount());

        // Messing with refOne should not affect refTwo
        assertSame(objTwo, refTwo.get());
        assertEquals(1, refTwo.getRefCount());
        assertTrue(objTwo.isOpen());
    }

    public void testCopy() throws IOException {
        final TestObj objOne = new TestObj();
        final SharedReference<TestObj> refOne = SharedReference.create(objOne);
        assertSame(objOne, refOne.get());
        assertEquals(1, refOne.getRefCount());
        assertTrue(objOne.isOpen());

        final SharedReference<TestObj> refTwo = refOne.copy();
        assertSame(objOne, refTwo.get());
        assertEquals(2, refOne.getRefCount());
        assertEquals(2, refTwo.getRefCount());
        assertTrue(objOne.isOpen());

        refOne.close();

        assertEquals(0, refOne.getRefCount());
        assertEquals(1, refTwo.getRefCount());
        assertTrue(objOne.isOpen());

        final SharedReference<TestObj> refThree = refTwo.copy();
        final SharedReference<TestObj> refFour = refThree.tryCopy();
        assertSame(objOne, refThree.get());
        assertSame(objOne, refFour.get());
        assertEquals(3, refTwo.getRefCount());
        assertEquals(3, refThree.getRefCount());
        assertTrue(objOne.isOpen());

        refTwo.close();
        refFour.close();

        assertSame(objOne, refThree.get());
        assertEquals(1, refThree.getRefCount());
        assertTrue(objOne.isOpen());

        refThree.close();

        assertEquals(0, refTwo.getRefCount());
        assertFalse(objOne.isOpen());
    }

    public void testCopyClosed() throws IOException {
        final TestObj objOne = new TestObj();
        final SharedReference<TestObj> refOne = SharedReference.create(objOne);
        assertSame(objOne, refOne.get());
        assertEquals(1, refOne.getRefCount());
        assertTrue(objOne.isOpen());
        refOne.close();

        assertFalse(objOne.isOpen());
        try {
            final SharedReference<TestObj> badRef = refOne.copy();
            fail("copy of closed ref shouldn't have worked");
        } catch (IllegalStateException e){
            // expected
        }

        final @Nullable SharedReference<TestObj> badRefTwo = refOne.tryCopy();
        assertNull(badRefTwo);
    }
}
