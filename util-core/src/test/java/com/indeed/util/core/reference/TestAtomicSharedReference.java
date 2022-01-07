package com.indeed.util.core.reference;

import junit.framework.TestCase;

import java.io.IOException;

/** @author pwp */
public class TestAtomicSharedReference extends TestCase {
    public void testCreateClose() throws IOException {
        TestSharedReference.TestObj testObj = new TestSharedReference.TestObj();
        assertTrue(testObj.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(testObj);
        assertEquals(1, refOne.getRefCount());

        SharedReference<TestSharedReference.TestObj> copyRef = refOne.getCopy();
        assertSame(testObj, copyRef.get());
        assertEquals(2, refOne.getRefCount());
        assertEquals(2, copyRef.getRefCount());
        assertTrue(testObj.isOpen());

        copyRef.close();

        assertEquals(1, refOne.getRefCount());
        assertTrue(testObj.isOpen());

        refOne.unset();
        assertFalse(testObj.isOpen());
    }

    public void testSetDifferent() throws IOException {
        TestSharedReference.TestObj objOne = new TestSharedReference.TestObj();
        assertTrue(objOne.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(objOne);
        assertEquals(1, refOne.getRefCount());

        TestSharedReference.TestObj objTwo = new TestSharedReference.TestObj();
        assertTrue(objTwo.isOpen());

        refOne.set(objTwo); // Note that we are setting refOne to the same object that it already
        // holds.
        assertFalse(objOne.isOpen());
        assertTrue(objTwo.isOpen());

        refOne.unset();
        assertFalse(objTwo.isOpen());
    }

    public void testSetQuietlyDifferent() throws IOException {
        TestSharedReference.TestObj objOne = new TestSharedReference.TestObj();
        assertTrue(objOne.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(objOne);
        assertEquals(1, refOne.getRefCount());

        TestSharedReference.TestObj objTwo = new TestSharedReference.TestObj();
        assertTrue(objTwo.isOpen());

        refOne.setQuietly(
                objTwo); // Note that we are setting refOne to the same object that it already
        // holds.
        assertFalse(objOne.isOpen());
        assertTrue(objTwo.isOpen());

        refOne.unset();
        assertFalse(objTwo.isOpen());
    }

    public void testSetWithSame() throws IOException {
        TestSharedReference.TestObj testObj = new TestSharedReference.TestObj();
        assertTrue(testObj.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(testObj);
        assertEquals(1, refOne.getRefCount());

        refOne.set(testObj); // Note that we are setting refOne to the same object that it already
        // holds.
        assertTrue(testObj.isOpen());

        refOne.unset();
        assertFalse(testObj.isOpen());
    }

    public void testSetQuietlyWithSame() throws IOException {
        TestSharedReference.TestObj testObj = new TestSharedReference.TestObj();
        assertTrue(testObj.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(testObj);
        assertEquals(1, refOne.getRefCount());

        refOne.setQuietly(
                testObj); // Note that we are setting refOne to the same object that it already
        // holds.
        assertTrue(testObj.isOpen());

        refOne.unset();
        assertFalse(testObj.isOpen());
    }

    public void testGetAndSetDifferent() throws IOException {
        TestSharedReference.TestObj objOne = new TestSharedReference.TestObj();
        assertTrue(objOne.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(objOne);
        assertEquals(1, refOne.getRefCount());

        TestSharedReference.TestObj objTwo = new TestSharedReference.TestObj();
        assertTrue(objTwo.isOpen());

        SharedReference<TestSharedReference.TestObj> refFromGnS = refOne.getAndSet(objTwo);
        assertNotNull(refFromGnS);
        assertSame(objOne, refFromGnS.get());

        assertEquals(1, refFromGnS.getRefCount());

        assertEquals(1, refOne.getRefCount());
        SharedReference<TestSharedReference.TestObj> copyRef = refOne.getCopy();
        assertNotNull(copyRef);
        assertSame(objTwo, copyRef.get());
        assertEquals(2, refOne.getRefCount());
        assertEquals(2, copyRef.getRefCount());
        copyRef.close();

        assertTrue(objOne.isOpen());
        assertTrue(objTwo.isOpen());

        refFromGnS.close();
        assertFalse(objOne.isOpen());
        assertEquals(1, refOne.getRefCount());
        assertTrue(objTwo.isOpen());

        refOne.unset();
        assertFalse(objTwo.isOpen());
    }

    public void testGetAndSetWithSame() throws IOException {
        TestSharedReference.TestObj testObj = new TestSharedReference.TestObj();
        assertTrue(testObj.isOpen());

        AtomicSharedReference<TestSharedReference.TestObj> refOne =
                AtomicSharedReference.create(testObj);
        assertEquals(1, refOne.getRefCount());

        // Note that we are setting refOne to the same object that it already holds.
        // We expect getAndSet to return a copy of the SharedRef to the thing that is already in
        // there.
        SharedReference<TestSharedReference.TestObj> refFromGnS = refOne.getAndSet(testObj);
        assertNotNull(refFromGnS);
        assertSame(testObj, refFromGnS.get());

        assertEquals(2, refFromGnS.getRefCount());

        assertEquals(2, refOne.getRefCount());
        SharedReference<TestSharedReference.TestObj> copyRef = refOne.getCopy();
        assertNotNull(copyRef);
        assertSame(testObj, copyRef.get());
        assertEquals(3, refOne.getRefCount());
        assertEquals(3, copyRef.getRefCount());
        copyRef.close();

        assertTrue(testObj.isOpen());

        refFromGnS.close();
        assertTrue(testObj.isOpen());
        assertEquals(1, refOne.getRefCount());

        refOne.unset();
        assertFalse(testObj.isOpen());
    }
}
