package com.jetbrains.internship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineBufferTest {

    @Test
    void addLast_shouldIncreaseSizeUntilCapacity_whenBufferNotFull() {
        LineBuffer buffer = new LineBuffer(3);
        Line line1 = new Line(2, new CellAttribute());
        Line line2 = new Line(2, new CellAttribute());

        buffer.addLast(line1);
        buffer.addLast(line2);

        assertEquals(2, buffer.size());
        assertEquals(3, buffer.capacity());
        assertSame(line1, buffer.get(0));
        assertSame(line2, buffer.get(1));
    }

    @Test
    void addLast_shouldOverwriteOldest_whenBufferIsFull() {
        LineBuffer buffer = new LineBuffer(2);
        Line line1 = new Line(2, new CellAttribute());
        Line line2 = new Line(2, new CellAttribute());
        Line line3 = new Line(2, new CellAttribute());

        buffer.addLast(line1);
        buffer.addLast(line2);
        buffer.addLast(line3);

        assertEquals(2, buffer.size());
        assertSame(line2, buffer.get(0));
        assertSame(line3, buffer.get(1));
    }

    @Test
    void get_shouldThrowIndexOutOfBounds_whenIndexInvalid() {
        LineBuffer buffer = new LineBuffer(1);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(-1));
    }

    @Test
    void clear_shouldResetHeadAndSize_whenCalled() {
        LineBuffer buffer = new LineBuffer(2);
        Line line = new Line(2, new CellAttribute());

        buffer.addLast(line);
        buffer.clear();

        assertEquals(0, buffer.size());
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(0));
    }
}
