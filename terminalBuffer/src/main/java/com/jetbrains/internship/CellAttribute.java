package com.jetbrains.internship;

import java.util.Objects;

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
 *                        bit 0 - bold
 *                        bit 1 - italic
 *                        bit 2 - underline
 *                        bit 3 - strikethrough
 */
public record CellAttribute(byte foregroundColor, byte backgroundColor, byte styleMask) {

    // fully default style
    public CellAttribute() {
        this((byte) -1, (byte) -1, (byte) 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellAttribute that = (CellAttribute) o;
        return foregroundColor == that.foregroundColor && backgroundColor == that.backgroundColor && styleMask == that.styleMask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(foregroundColor, backgroundColor, styleMask);
    }
}
