package com.jetbrains.internship;

import java.util.List;

public final class Line {

    public int width;

    /**
     * Unicode code point per cell.
     * Valid only if width != TRAILING.
     * TRAILING/EMPTY cells store 0.
     */
    public int[] codepoints;

    public byte[] widths;

    public List<AttributeSequence> attributeSequences;

    public boolean wrapped;

    public Line(int width, CellAttribute defaultAttr) {
        this.width = width;
        this.codepoints = new int[width];
        this.widths = new byte[width];
        clear(defaultAttr);
    }

    public void clear(CellAttribute defaultAttr) {
        for (int i = 0; i < width; i++) {
            this.codepoints[i] = 0;
            widths[i] = CellType.normal;
        }
        attributeSequences.clear();
        attributeSequences.add(new AttributeSequence(0, width, defaultAttr));
        wrapped = false;
    }

    public int getCodePoint(int col) {
        if (widths[col] == CellType.trailing) {
            // trailing cells do not contain anything as the character is stored in leading.
            // On higher level (TerminalBuffer) API both cells will return the character unicode
            return -1;
        }
        return codepoints[col];
    }

    public byte getCellWidth(int col) {
        return widths[col];
    }

    // could be improved with binary search over linear, it is assumed the attributeSequences will
    // not be long enough to matter
    public CellAttribute getAttr(int col) {
        for (AttributeSequence run : attributeSequences) {
            if (col >= run.startCol && col < run.startCol + run.length) {
                return run.attributes;
            }
        }
        throw new IllegalStateException("No attributeSequence for column " + col);
    }
}