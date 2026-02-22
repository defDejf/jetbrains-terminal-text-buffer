package com.jetbrains.internship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void clear_shouldResetCellsAndWrapFlag_whenLineContainsContent() {
        CellAttribute defaultAttr = new CellAttribute();
        CellAttribute styledAttr = new CellAttribute((byte) 1, (byte) 2, (byte) 3);
        Line line = new Line(5, defaultAttr);

        line.putCodePoint(0, 'A', 1, styledAttr);
        line.wrapped = true;

        line.clear(defaultAttr);

        assertEquals(' ', line.toString().charAt(0));
        assertEquals(CellType.normal, line.getCellType(0));
        assertEquals(defaultAttr, line.getAttr(0));
        assertFalse(line.wrapped);
    }

    @Test
    void putCodePoint_shouldMarkLeadingAndTrailing_whenWideCharacterInserted() {
        CellAttribute attr = new CellAttribute((byte) 2, (byte) 3, (byte) 1);
        Line line = new Line(4, new CellAttribute());

        line.putCodePoint(1, '界', 2, attr);

        assertEquals('界', line.getCodePoint(1));
        assertEquals(CellType.leading, line.getCellType(1));
        assertEquals(CellType.trailing, line.getCellType(2));
        assertEquals(-1, line.getCodePoint(2)); // on line level API we access double wide chars by leading
        assertEquals(attr, line.getAttr(1));
        assertEquals(attr, line.getAttr(2));
    }

    @Test
    void clearCell_shouldClearWholeWideCharacter_whenClearingTrailingCell() {
        Line line = new Line(4, new CellAttribute());
        CellAttribute attr = new CellAttribute((byte) 4, (byte) 5, (byte) 6);

        line.putCodePoint(1, '界', 2, attr);
        line.clearCell(2, new CellAttribute());

        assertEquals(0, line.getCodePoint(1));
        assertEquals(CellType.normal, line.getCellType(1));
        assertEquals(CellType.normal, line.getCellType(2));
    }

    @Test
    void insertCells_shouldReturnSpillAndShiftContent_whenInsertingInsideLine() {
        CellAttribute defaultAttr = new CellAttribute();
        CellAttribute attr = new CellAttribute((byte) 1, (byte) 1, (byte) 1);
        Line line = new Line(5, defaultAttr);
        line.putCodePoint(0, 'A', 1, attr);
        line.putCodePoint(1, 'B', 1, attr);
        line.putCodePoint(2, 'C', 1, attr);
        line.putCodePoint(3, 'D', 1, attr);
        line.putCodePoint(4, 'E', 1, attr);

        LineFragment spill = line.insertCells(2, 2, defaultAttr);

        assertNotNull(spill);
        assertEquals(2, spill.size());
        assertEquals('D', spill.codepoints[0]);
        assertEquals('E', spill.codepoints[1]);
        assertEquals("AB  C", line.toString());
    }

    @Test
    void insertFragmentAtStart_shouldInsertAndSpillExcess_whenFragmentLargerThanWidth() {
        Line line = new Line(3, new CellAttribute());
        LineFragment fragment = new LineFragment(4);
        CellAttribute attr = new CellAttribute((byte) 6, (byte) 7, (byte) 1);

        fragment.codepoints[0] = 'X';
        fragment.codepoints[1] = 'Y';
        fragment.codepoints[2] = 'Z';
        fragment.codepoints[3] = 'Q';
        fragment.cellTypes[0] = CellType.normal;
        fragment.cellTypes[1] = CellType.normal;
        fragment.cellTypes[2] = CellType.normal;
        fragment.cellTypes[3] = CellType.normal;
        fragment.attributes[0] = attr;
        fragment.attributes[1] = attr;
        fragment.attributes[2] = attr;
        fragment.attributes[3] = attr;

        LineFragment spill = line.insertFragmentAtStart(fragment);

        assertEquals("XYZ", line.toString());
        assertNotNull(spill);
        assertEquals(1, spill.size());
        assertEquals('Q', spill.codepoints[0]);
    }
}
