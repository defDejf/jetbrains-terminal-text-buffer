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


    public boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
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