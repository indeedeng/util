package com.indeed.util.mmap;

import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class MemoryInputTest extends TestCase {
    private static final Logger log = LogManager.getLogger(MemoryInputTest.class);

    int length = 120000;

    Memory[] memoryArray;
    MemoryDataInput[] memoryDataInputArray;
    MemoryInputStream[] memoryInputStreamArray;
    MemoryScatteringByteChannel[] memoryScatteringByteChannelArray;

    @Override
    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "" , new File("."));
        File file2 = File.createTempFile("tmp", "" , new File("."));
        file.deleteOnExit();
        file2.deleteOnExit();

        memoryArray = new Memory[6];
        memoryArray[0] = new HeapMemory(length, ByteOrder.LITTLE_ENDIAN);
        BufferResource bufferResource = new MMapBuffer(file, 0L, length, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN);
        memoryArray[1] = bufferResource.memory();
        memoryArray[2] = new NativeBuffer(length, ByteOrder.LITTLE_ENDIAN).memory();
        memoryArray[3] = new HeapMemory(length, ByteOrder.BIG_ENDIAN);
        BufferResource bufferResource2 = new MMapBuffer(file2, 0L, length, FileChannel.MapMode.READ_WRITE, ByteOrder.BIG_ENDIAN);
        memoryArray[4] = bufferResource2.memory();
        memoryArray[5] = new NativeBuffer(length, ByteOrder.BIG_ENDIAN).memory();

        memoryDataInputArray = new MemoryDataInput[memoryArray.length];
        memoryInputStreamArray = new MemoryInputStream[memoryArray.length];
        memoryScatteringByteChannelArray = new MemoryScatteringByteChannel[memoryArray.length];
        for (int i = 0; i < memoryDataInputArray.length; i++) {
            memoryDataInputArray[i] = new MemoryDataInput(memoryArray[i]);
            memoryInputStreamArray[i] = new MemoryInputStream(memoryArray[i]);
            memoryScatteringByteChannelArray[i] = new MemoryScatteringByteChannel(memoryArray[i]);
        }

    }

    public void testMemoryDataInput() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            ByteArray byteArray = aMemoryArray.byteArray(0L, length);
            for (int j = 0; j < length; j++) {
                byteArray.set(j, (byte) j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            assertEquals(length, memoryDataInput.length());
            for (int i = 0; i < memoryDataInput.length(); i+=2) {
                int value = i % 256;
                if (value >= 128) {
                    value -= 256;
                }
                assertEquals(value, memoryDataInput.readByte());
                memoryDataInput.skipBytes(1);
            }
        }

        for (MemoryInputStream memoryInputStream : memoryInputStreamArray) {
            assertEquals(length, memoryInputStream.length());
            for (int i = 0; i < memoryInputStream.length(); i++) {
                int value = i % 256;
                assertEquals(value, memoryInputStream.read());
            }
            memoryInputStream.seek(0);
            for (int i = 0; i < memoryInputStream.length()-8; i += 16) {
                byte[] byteArray = new byte[8];

                //noinspection ResultOfMethodCallIgnored
                memoryInputStream.read(byteArray);

                //noinspection ResultOfMethodCallIgnored
                memoryInputStream.skip(8);
                for (int j = 0; j < byteArray.length; j++) {
                    int value = (i+j) % 256;
                    if (value >= 128) {
                        value -= 256;
                    }
                    assertEquals(value, byteArray[j]);
                }
            }
        }

        for (MemoryScatteringByteChannel memoryScatteringByteChannel : memoryScatteringByteChannelArray) {
            assertTrue(memoryScatteringByteChannel.isOpen());
            for (int i = 0; memoryScatteringByteChannel.isOpen(); i+=3) {
                int value = i % 256;
                if (value >= 128) {
                    value -= 256;
                }
                int value2 = (i+1) % 256;
                if (value2 >= 128) {
                    value2 -= 256;
                }
                int value3 = (i+2) % 256;
                if (value3 >= 128) {
                    value3 -= 256;
                }
                ByteBuffer[] byteBufferArray = new ByteBuffer[3];
                for (int j = 0; j < 3; j++) {
                    byteBufferArray[j] = ByteBuffer.allocate(1);
                }
                long ret = memoryScatteringByteChannel.read(byteBufferArray);
                if (ret < 1) {
                    break;
                }
                assertEquals(value, byteBufferArray[0].get(0));
                assertEquals(value2, byteBufferArray[1].get(0));
                assertEquals(value3, byteBufferArray[2].get(0));
            }
        }
    }

    public void testReadByte() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            ByteArray byteArray = aMemoryArray.byteArray(0L, length);
            for (int j = 0; j < length; j++) {
                byteArray.set(j, (byte) j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                byte b = memoryDataInput.readByte();
                assertEquals((byte) i, b);
            }
        }
    }

    public void testReadChar() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            CharArray charArray = aMemoryArray.charArray(0L, length / 2);
            for (int j = 0; j < charArray.length(); j++) {
                charArray.set(j, (char) j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                assertEquals((char) i, memoryDataInput.readChar());
            }
        }
    }

    public void testReadShort() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            ShortArray shortArray = aMemoryArray.shortArray(0L, length / 2);
            for (int j = 0; j < shortArray.length(); j++) {
                shortArray.set(j, (short) j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                assertEquals((short) i, memoryDataInput.readShort());
            }
        }
    }

    public void testReadInt() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            IntArray intArray = aMemoryArray.intArray(0L, length / 4);
            for (int j = 0; j < intArray.length(); j++) {
                intArray.set(j, j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                assertEquals(i, memoryDataInput.readInt());
            }
        }
    }

    public void testReadLong() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            LongArray longArray = aMemoryArray.longArray(0L, length / 8);
            for (int j = 0; j < longArray.length(); j++) {
                longArray.set(j, j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                assertEquals(i, memoryDataInput.readLong());
            }
        }
    }

    public void testReadFloat() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            FloatArray floatArray = aMemoryArray.floatArray(0L, length / 4);
            for (int j = 0; j < floatArray.length(); j++) {
                floatArray.set(j, j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                assertEquals((float) i, memoryDataInput.readFloat());
            }
        }
    }

    public void testReadDouble() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            DoubleArray doubleArray = aMemoryArray.doubleArray(0L, length / 8);
            for (int j = 0; j < doubleArray.length(); j++) {
                doubleArray.set(j, j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            for (int i = 0; memoryDataInput.position() < memoryDataInput.length(); i++) {
                assertEquals((double) i, memoryDataInput.readDouble());
            }
        }
    }

    public void testReadFully() throws Exception {
        for (Memory aMemoryArray : memoryArray) {
            ByteArray byteArray = aMemoryArray.byteArray(0L, length);
            for (int j = 0; j < length; j++) {
                byteArray.set(j, (byte) j);
            }
        }

        for (MemoryDataInput memoryDataInput : memoryDataInputArray) {
            byte[] bytes = new byte[(int) memoryDataInput.length()];
            memoryDataInput.readFully(bytes);
            for (int i = 0; i < bytes.length; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }
}
