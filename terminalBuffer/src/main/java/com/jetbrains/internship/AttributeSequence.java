package com.jetbrains.internship;

/**
 * To keep mem footprint down sequences of same-style characters will share one CellAttribute class
 */
public class AttributeSequence {
    public int startCol; // inclusive
    public int length; // includes start
    public CellAttribute attributes;

    public AttributeSequence(int startCol, int length, CellAttribute attributes) {
        this.startCol = startCol;
        this.length = length;
        this.attributes = attributes;
    }
}
