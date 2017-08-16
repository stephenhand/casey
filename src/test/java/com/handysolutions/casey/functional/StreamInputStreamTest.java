package com.handysolutions.casey.functional;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by stephenh on 16/08/2017.
 */
public class StreamInputStreamTest {
    Stream<byte[]> mockStream;
    Spliterator<byte[]> mockSpliterator;

    @Before
    public void Setup(){
        mockSpliterator = Mockito.mock(Spliterator.class);

        mockStream = Mockito.mock(Stream.class);
        Mockito.when(mockStream.spliterator()).thenReturn(mockSpliterator);
    }

    @Test
    public void read_singleStreamItemContainingSingleByte_inputStreamReadsOneByte() throws IOException {
        byte[] target = new byte[1];
        new StreamInputStream(Stream.of(new byte[]{0x0a})).read(target);
        Assert.assertEquals((byte)0x0a, target[0]);
    }

    @Test
    public void read_emptyStream_inputStreamReadsNothing() throws IOException {
        Assert.assertEquals(-1, new StreamInputStream(Stream.empty()).read(new byte[16]));
    }

    @Test
    public void read_singleStreamItemContainingEmptyArray_inputStreamReadsNothing() throws IOException {
        Assert.assertEquals(-1, new StreamInputStream(Stream.of(new byte[]{})).read(new byte[16]));
    }

    @Test
    public void read_streamEmptyArrays_inputStreamReadsNothing() throws IOException {
        Assert.assertEquals(-1, new StreamInputStream(Stream.of(new byte[0],new byte[0],new byte[0],new byte[0])).read(new byte[16]));
    }


    @Test
    public void read_singleItemStreamContainingMultipleBytes_inputStreamReadsAllBytes() throws IOException {
        byte[] target = new byte[16];
        new StreamInputStream(Stream.of(new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f})).read(target);
        Assert.assertArrayEquals(new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f}, Arrays.copyOfRange(target, 0, 6));
    }

    @Test
    public void read_streamContainingSeveralArraysOfMultipleBytes_inputStreamReadsAllBytesFromEachItemInOrder() throws IOException {
        byte[] target = new byte[16];
        new StreamInputStream(Stream.of(
                new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f},
                new byte[]{0x01, 0x02, 0x03},
                new byte[]{0x06, 0x07, 0x08, 0x09}
        )).read(target);
        Assert.assertArrayEquals(new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x01, 0x02, 0x03, 0x06, 0x07, 0x08, 0x09}, Arrays.copyOfRange(target, 0, 13));
    }

    @Test
    public void read_streamContainingSeveralArraysWhereSomeAreEmpty_writesNothingForEmptyArray() throws IOException {
        byte[] target = new byte[16];
        new StreamInputStream(Stream.of(
                new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f},
                new byte[]{},
                new byte[]{0x06, 0x07, 0x08, 0x09}
        )).read(target);
        Assert.assertArrayEquals(new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x06, 0x07, 0x08, 0x09}, Arrays.copyOfRange(target, 0, 10));
    }

    @Test
    public void read_populatedStream_closesStreamAfterWriting() throws IOException {
        Mockito.when(mockSpliterator.tryAdvance(Mockito.any(Consumer.class))).thenAnswer(inv->{
            ((Consumer<byte[]>)(inv.getArguments()[0])).accept(new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f});
            return true;
        }).thenAnswer(inv->{
            ((Consumer<byte[]>)(inv.getArguments()[0])).accept(new byte[]{});
            return true;
        }).thenAnswer(inv->{
            ((Consumer<byte[]>)(inv.getArguments()[0])).accept(new byte[]{0x06, 0x07, 0x08, 0x09});
            return true;
        }).thenReturn(false);
        new StreamInputStream(mockStream).read(new byte[16]);
        Mockito.verify(mockStream).close();
    }

    @Test
    public void read_emptyStream_closesStreamAfterWriting() throws IOException {
        Mockito.when(mockSpliterator.tryAdvance(Mockito.any(Consumer.class))).thenReturn(false);
        new StreamInputStream(mockStream).read(new byte[16]);
        Mockito.verify(mockStream).close();
    }

    @Test
    public void read_erroringStream_closesStreamAfterWriting() throws IOException {
        Mockito.when(mockSpliterator.tryAdvance(Mockito.any(Consumer.class))).thenAnswer(inv->{
            throw new Exception();
        });
        try {
            new StreamInputStream(mockStream).read(new byte[16]);
        } catch (Exception e){}
        Mockito.verify(mockStream).close();
    }

    @Test(expected = Exception.class)
    public void read_twoInstancesReadFromSameStream_throws() throws IOException {
        byte[] target = new byte[16];
        Stream<byte[]> underlyingData=Stream.of(
                new byte[]{0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f},
                new byte[]{},
                new byte[]{0x06, 0x07, 0x08, 0x09}
        );
        new StreamInputStream(underlyingData).read(target);
        new StreamInputStream(underlyingData).read(target);
    }

}
