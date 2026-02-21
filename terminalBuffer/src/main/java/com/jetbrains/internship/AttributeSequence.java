package com.jetbrains.internship;

/**
 * To keep mem footprint down sequences of same-style characters will share one CellAttribute class
 */
public class AttributeSequence {
    private int startCol;
    private int length;
    private CellAttribute attributes;
}
