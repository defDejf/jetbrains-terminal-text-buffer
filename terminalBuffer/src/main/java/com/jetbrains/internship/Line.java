package com.jetbrains.internship;

import java.util.ArrayList;
import java.util.List;

public final class Line {

    public int width;

    /**
     * Unicode code point per cell.
     * Valid only if width != trailing.
     * trailing/EMPTY cells store 0.
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

    public byte getCellType(int col) {
        return widths[col];
    }

    public CellAttribute getAttr(int col) {
        for (AttributeSequence run : attributeSequences) {
            if (col >= run.startCol && col < run.startCol + run.length) {
                return run.attributes;
            }
        }
        throw new IllegalStateException("No attributeSequence for column " + col);
    }

    public void clearCell(int col, CellAttribute defaultAttr) {
        byte w = widths[col];

        if (w == CellType.trailing) {
            clearCell(col - 1, defaultAttr);
            return;
        }

        if (w == CellType.leading && col + 1 < width) {
            widths[col + 1] = CellType.normal;
            codepoints[col + 1] = 0;
            setAttrRange(col + 1, 1, defaultAttr);
        }

        widths[col] = CellType.normal;
        codepoints[col] = 0;
        setAttrRange(col, 1, defaultAttr);
    }

    private void setAttrRange(int start, int length, CellAttribute attr) {
        int end = start + length;
        List<AttributeSequence> newRuns = new ArrayList<>();

        for (AttributeSequence seq : attributeSequences) {
            int seqStart = seq.startCol;
            int seqEnd = seq.startCol + seq.length;

            if (seqEnd <= start || seqStart >= end) {
                newRuns.add(seq);
                continue;
            }

            // Left split
            if (seqStart < start) {
                newRuns.add(new AttributeSequence(seqStart, start - seqStart, seq.attributes));
            }

            // Middle
            int midStart = Math.max(seqStart, start);
            int midEnd = Math.min(seqEnd, end);
            newRuns.add(new AttributeSequence(midStart, midEnd - midStart, attr));

            // Right split
            if (seqEnd > end) {
                newRuns.add(new AttributeSequence(end, seqEnd - end, seq.attributes));
            }
        }

        attributeSequences.clear();
        mergeRuns(newRuns);
    }

    private void mergeRuns(List<AttributeSequence> newSequences) {
        for (AttributeSequence r : newSequences) {
            if (attributeSequences.isEmpty()) {
                attributeSequences.add(r);
            } else {
                AttributeSequence last = attributeSequences.getLast();
                if (last.attributes.equals(r.attributes)
                    && last.startCol + last.length == r.startCol) {
                    last.length += r.length;
                } else {
                    attributeSequences.add(r);
                }
            }
        }
    }

    public LineFragment insertCells(int col, int count, CellAttribute fillAttr) {
        if (count <= 0) return null;

        // Fragment for spilled cells (right side)
        int spillSize;
        LineFragment fragment = null;

        if (col + count > width) {
            spillSize = col + count - width;
            fragment = new LineFragment(spillSize);

            // capture spilled cells
            for (int i = 0; i < spillSize; i++) {
                int src = width - spillSize + i;
                fragment.codepoints[i] = codepoints[src];
                fragment.cellTypes[i] = widths[src];
                fragment.attributes[i] = getAttr(src);
            }
        }

        // Shift cells to the right
        for (int i = width - 1; i >= col + count; i--) {
            codepoints[i] = codepoints[i - count];
            widths[i] = widths[i - count];
        }

        // Clear inserted range
        for (int i = col; i < Math.min(col + count, width); i++) {
            codepoints[i] = 0;
            widths[i] = CellType.normal;
        }

        // Reset attributes in inserted range
        setAttrRange(col, Math.min(count, width - col), fillAttr);

        return fragment;
    }

    public void putCodePoint(int col, int cp, int cellWidth, CellAttribute attr) {
        // Clear any overlapped wide chars
        clearCell(col, attr);

        codepoints[col] = cp;
        widths[col] = (cellWidth == 2)
            ? CellType.leading
            : CellType.normal;

        setAttrRange(col, 1, attr);

        if (cellWidth == 2 && col + 1 < width) {
            codepoints[col + 1] = 0;
            widths[col + 1] = CellType.trailing;
            setAttrRange(col + 1, 1, attr);
        }
    }

    public LineFragment insertFragmentAtStart(LineFragment fragment) {
        if (fragment == null || fragment.size() == 0) return null;

        int count = fragment.size();

        LineFragment spill = null;
        if (count > width) {
            spill = new LineFragment(count - width);
            for (int i = width; i < count; i++) {
                spill.codepoints[i - width] = fragment.codepoints[i];
                spill.cellTypes[i - width] = fragment.cellTypes[i];
                spill.attributes[i - width] = fragment.attributes[i];
            }
            count = width;
        }

        for (int i = width - 1; i >= count; i--) {
            codepoints[i] = codepoints[i - count];
            widths[i] = widths[i - count];
        }

        for (int i = 0; i < count; i++) {
            codepoints[i] = fragment.codepoints[i];
            widths[i] = fragment.cellTypes[i];
            setAttrRange(i, 1, fragment.attributes[i]);
        }

        return spill;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < width; i++) {
            if (widths[i] == CellType.trailing) {
                continue;
            }
            int cp = codepoints[i];
            if (cp == 0) {
                sb.append(' ');
            } else {
                sb.appendCodePoint(cp);
            }
        }
        return sb.toString();
    }

}