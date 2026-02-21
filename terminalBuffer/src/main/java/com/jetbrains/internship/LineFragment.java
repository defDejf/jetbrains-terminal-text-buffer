package com.jetbrains.internship;

public final class LineFragment {

    public final int[] codepoints;
    public final byte[] cellTypes;
    public final CellAttribute[] attributes;

    public LineFragment(int size) {
        this.codepoints = new int[size];
        this.cellTypes = new byte[size];
        this.attributes = new CellAttribute[size];
    }

    public int size() {
        return codepoints.length;
    }
}