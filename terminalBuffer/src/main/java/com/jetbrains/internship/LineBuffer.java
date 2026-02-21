package com.jetbrains.internship;

/**
 * Custom circular buffer to avoid poor random access performance of Dequeue while keeping fast insert
 */

final class LineBuffer {

    private final Line[] buffer;
    private int head = 0;
    private int size = 0;

    LineBuffer(int capacity) {
        this.buffer = new Line[capacity];
    }

    int size() {
        return size;
    }

    int capacity() {
        return buffer.length;
    }

    Line get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return buffer[(head + index) % buffer.length];
    }

    void addLast(Line line) {
        if (size < buffer.length) {
            buffer[(head + size) % buffer.length] = line;
            size++;
        } else {
            // overwrite oldest
            buffer[head] = line;
            head = (head + 1) % buffer.length;
        }
    }

    void clear() {
        head = 0;
        size = 0;
    }
}
