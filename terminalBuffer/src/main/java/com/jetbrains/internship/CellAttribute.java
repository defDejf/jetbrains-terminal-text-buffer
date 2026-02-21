package com.jetbrains.internship;

/**
 * Sequences of chars will likely share the same attributes,
 * keep them in a single obj with shared reference to reduce mem footprint
 * over just keeping them in cells.
 * When modifying style in editable part often it will be necessary to create new attribute anyway.
 * Unused attributes will be cleaned up.
 *
 * @param foregroundColor 0 - 15 for ANSI colors, -1 for whatever default is
 * @param backgroundColor 0 - 15 for ANSI colors, -1 for whatever default is
 * @param styleMask       style mask:
 *                        bit 0 → bold
 *                        bit 1 → italic
 *                        bit 2 → underline
 */
public record CellAttribute(byte foregroundColor, byte backgroundColor, byte styleMask) {

    // fully default style
    public CellAttribute {
        foregroundColor = -1;
        backgroundColor = -1;
        styleMask = 0;
    }
}
