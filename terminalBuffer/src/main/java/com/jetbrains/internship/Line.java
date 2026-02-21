package com.jetbrains.internship;

import java.util.List;

public final class Line {
    /**
     * Unicode code point per cell.
     * Valid only if width != TRAILING.
     * TRAILING/EMPTY cells store 0.
     */
    public int[] codepoints;

    public byte[] widths;

    public List<AttributeSequence> attrRuns;

    public boolean wrapped;
}