package com.jetbrains.internship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TerminalBufferTest {

    @Test
    void constructor_shouldInitializeScreenWithEmptyLines_whenCreated() {
        TerminalBuffer buffer = new TerminalBuffer(4, 3, 2);

        assertEquals("    \n    \n    ", buffer.getScreenText());
        assertEquals("    \n    \n    ", buffer.getAllText());
        assertArrayEquals(new int[]{0, 0}, buffer.getCursorPos());
    }

    @Test
    void setCursorPos_shouldIgnoreOutOfBoundsAndTrailingCell_whenPositionInvalidOrTrailing() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 1);
        buffer.writeTextOnLine("A界");

        buffer.setCursorPos(0, 2);
        assertArrayEquals(new int[]{0, 1}, buffer.getCursorPos());

        buffer.setCursorPos(5, 1);
        assertArrayEquals(new int[]{0, 1}, buffer.getCursorPos());

        buffer.setCursorPos(0, 5);
        assertArrayEquals(new int[]{0, 1}, buffer.getCursorPos());
    }

    @Test
    void writeTextOnLine_shouldWrapAndScroll_whenTextExceedsScreen() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);

        buffer.writeTextOnLine("abcdefgh");

        assertEquals("def\ngh ", buffer.getScreenText());
        assertEquals("abc\ndef\ngh ", buffer.getAllText());
        assertArrayEquals(new int[]{1, 2}, buffer.getCursorPos());
    }

    @Test
    void insertTextOnLine_shouldInsertAtCursorAndPushOverflowToNextLines_whenContentExists() {
        TerminalBuffer buffer = new TerminalBuffer(4, 3, 2);
        buffer.writeTextOnLine("ABCDEFGH");
        buffer.setCursorPos(0, 1);

        buffer.insertTextOnLine("ZZ");

        assertEquals("AZZB\nCDEF\nGH  ", buffer.getScreenText());
        assertArrayEquals(new int[]{0, 3}, buffer.getCursorPos());
    }

    @Test
    void insertTextOnLine_shouldResetInsertionColumnAfterNewline_whenTextContainsNewline() {
        TerminalBuffer buffer = new TerminalBuffer(4, 3, 2);
        buffer.writeTextOnLine("ABCD");
        buffer.setCursorPos(0, 2);

        buffer.insertTextOnLine("X\nY");

        assertEquals("ABXC\nYD  \n    ", buffer.getScreenText());
    }

    @Test
    void fillLineWithChar_shouldRespectWideCharAlignment_whenWidthIsOdd() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 1);
        buffer.setCurrentlyUsedAttr((byte) 3, (byte) 4, (byte) 1);

        buffer.fillLineWithChar('界');

        assertEquals("界界 \n     ", buffer.getScreenText());
        assertEquals('界', buffer.getUnicodeAt(0, 0));
        assertEquals('界', buffer.getUnicodeAt(0, 1));
        assertEquals('界', buffer.getUnicodeAt(0, 2));
        assertEquals('界', buffer.getUnicodeAt(0, 3));
        assertEquals(0, buffer.getUnicodeAt(0, 4));
    }

    @Test
    void clearEditable_shouldKeepScrollbackAndResetScreenAndCursor_whenCalled() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 3);
        buffer.writeTextOnLine("111222333");

        buffer.clearEditable();

        assertEquals("   \n   ", buffer.getScreenText());
        assertEquals("111\n222\n   \n   ", buffer.getAllText());
        assertArrayEquals(new int[]{0, 0}, buffer.getCursorPos());
    }

    @Test
    void clearAll_shouldResetBothScreenAndScrollback_whenCalled() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 3);
        buffer.writeTextOnLine("111222333");

        buffer.clearAll();

        assertEquals("   \n   ", buffer.getScreenText());
        assertEquals("   \n   ", buffer.getAllText());
        assertArrayEquals(new int[]{0, 0}, buffer.getCursorPos());
    }

    @Test
    void getAttributesAt_shouldReturnCurrentAttributeForWrittenCells_whenAttributesConfigured() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 1);
        buffer.setCurrentlyUsedAttr((byte) 5, (byte) 6, (byte) 0b111);

        buffer.writeTextOnLine("AB");

        assertEquals(new CellAttribute((byte) 5, (byte) 6, (byte) 0b111), buffer.getAttributesAt(0, 0));
        assertEquals(new CellAttribute((byte) 5, (byte) 6, (byte) 0b111), buffer.getAttributesAt(0, 1));
        assertNull(buffer.getAttributesAt(8, 0));
    }

    @Test
    void getters_shouldReturnSafeDefaults_whenCoordinatesOutOfBounds() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 1);

        assertEquals(-1, buffer.getUnicodeAt(-1, 0));
        assertEquals(-1, buffer.getUnicodeAt(0, 8));
        assertEquals("", buffer.getLineAsString(-1));
        assertEquals("", buffer.getLineAsString(99));
    }
}
