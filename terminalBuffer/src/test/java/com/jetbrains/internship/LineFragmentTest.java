package com.jetbrains.internship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LineFragmentTest {

    @Test
    void constructor_shouldCreateArraysWithGivenSize_whenSizeProvided() {
        LineFragment fragment = new LineFragment(4);

        assertEquals(4, fragment.size());
        assertEquals(4, fragment.codepoints.length);
        assertEquals(4, fragment.cellTypes.length);
        assertEquals(4, fragment.attributes.length);
    }

    @Test
    void newFragment_shouldHaveDefaultValues_whenCreated() {
        LineFragment fragment = new LineFragment(2);

        assertEquals(0, fragment.codepoints[0]);
        assertEquals(0, fragment.cellTypes[0]);
        assertNull(fragment.attributes[0]);
    }
}
