package com.jetbrains.internship;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private int cursorRowPos;
    private int cursorColPos;
    private final LineBuffer lines;
    private CellAttribute currentlyUsedAttr; // what style is used for adding new chars

    public TerminalBuffer(int width, int height, int maxScrollbackLines) {
        this.width = width;
        this.height = height;

        this.cursorRowPos = 0;
        this.cursorColPos = 0;

        int capacity = maxScrollbackLines + height;
        this.lines = new LineBuffer(capacity);

        this.currentlyUsedAttr = new CellAttribute();

        // init screen
        for (int i = 0; i < height; i++) {
            insertEmptyLineAtEnd();
        }
    }

    public void setCurrentlyUsedAttr(byte foregroundColor, byte backgroundColor, byte styleMask) {
        currentlyUsedAttr = new CellAttribute(foregroundColor, backgroundColor, styleMask);
    }

    private int editableStart() {
        return Math.max(0, lines.size() - height);
    }

    private Line getScreenLine(int screenRow) {
        return lines.get(editableStart() + screenRow);
    }

    private void scrollUp() {
        lines.addLast(new Line(width, new CellAttribute()));
    }

    private void newline() {
        cursorColPos = 0;
        cursorRowPos++;

        if (cursorRowPos >= height) {
            scrollUp();
            cursorRowPos = height - 1;
        }
    }

    /**
     * @return array containing current cursor position in order row, column
     */
    public int[] getCursorPos() {
        return new int[]{cursorRowPos, cursorColPos};
    }

    public void setCursorPos(int rowPos, int colPos) {
        if (rowPos < 0 || rowPos >= height) return;
        if (colPos < 0 || colPos >= width) return;

        Line line = getScreenLine(rowPos);

        // If we land on a trailing cell, move left to the leading cell
        int col = colPos;
        while (col > 0 && line.getCellType(col) == CellType.trailing) {
            col--;
        }

        // check: should never end on trailing
        if (line.getCellType(col) == CellType.trailing) {
            return; // corrupt line state, should not happen
        }

        cursorRowPos = rowPos;
        cursorColPos = col;
    }

    public void writeTextOnLine(String text) {
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            if (codePoint == '\n') {
                newline();
                continue;
            }

            int w = getCharacterWidth(codePoint);

            if (w == 2 && cursorColPos == width - 1) {
                newline();
            }

            Line line = getScreenLine(cursorRowPos);
            line.putCodePoint(cursorColPos, codePoint, w, currentlyUsedAttr);
            cursorColPos += w;

            if (cursorColPos >= width) {
                newline();
            }
        }
    }

    public void insertTextOnLine(String text) {
        int row = cursorRowPos;
        int col = cursorColPos;

        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            if (cp == '\n') {
                cursorColPos = col;
                newline();
                row = cursorRowPos;
                continue;
            }

            int w = getCharacterWidth(cp);

            // Wrap if wide char doesn't fit
            if (w == 2 && col == width - 1) {
                cursorColPos = col;
                newline();
                row = cursorRowPos;
                col = cursorColPos;
            }

            Line line = getScreenLine(row);

            // Insert empty cells
            LineFragment spill = line.insertCells(col, w, new CellAttribute());

            // Write the glyph
            line.putCodePoint(col, cp, w, currentlyUsedAttr);

            col += w;
            cursorColPos = col;

            // Propagate spill downward
            while (spill != null) {
                row++;

                if (row >= height) {
                    scrollUp();
                    row = height - 1;
                }

                Line next = getScreenLine(row);
                spill = next.insertFragmentAtStart(spill);
            }

            if (col >= width) {
                cursorColPos = col;
                newline();
                row = cursorRowPos;
                col = cursorColPos;
            }
        }

        cursorRowPos = row;
        cursorColPos = col;
    }

    public boolean fillLineWithChar(int unicodeVal) {
        int w = getCharacterWidth(unicodeVal);
        int effectiveWidth = width;
        if (w == 2 && (width % 2 != 0)) {
            effectiveWidth = width - 1;
        }

        Line line = getScreenLine(cursorRowPos);
        int col = 0;
        while (col < effectiveWidth) {
            line.putCodePoint(col, unicodeVal, w, currentlyUsedAttr);
            col += w;
        }
        return true;
    }

    public void insertEmptyLineAtEnd() {
        lines.addLast(new Line(width, new CellAttribute()));
    }

    public void clearEditable() {
        for (int i = 0; i < height; i++) {
            getScreenLine(i).clear(new CellAttribute());
        }
        cursorRowPos = 0;
        cursorColPos = 0;
    }

    public void clearAll() {
        lines.clear();
        for (int i = 0; i < height; i++) {
            lines.addLast(new Line(width, new CellAttribute()));
        }
        cursorRowPos = 0;
        cursorColPos = 0;
    }


    public int getUnicodeAt(int rowPos, int colPos) {
        if (rowPos < 0 || colPos < 0 || colPos >= width) {
            return -1;
        }
        if (rowPos >= lines.size()) {
            return -1;
        }

        Line line = lines.get(rowPos);

        int col = colPos;

        // If trailing cell, move left to the leading cell
        while (col > 0 && line.getCellType(col) == CellType.trailing) {
            col--;
        }

        // should never happen unless line is corrupted
        if (line.getCellType(col) == CellType.trailing) {
            return -1;
        }

        return line.getCodePoint(col);
    }

    public CellAttribute getAttributesAt(int rowPos, int colPos) {
        if (rowPos < 0 || colPos < 0 || colPos >= width) return null;
        if (rowPos >= lines.size()) return null;
        return lines.get(rowPos).getAttr(colPos);
    }

    public String getLineAsString(int row) {
        if (row < 0 || row >= lines.size()) return "";
        return lines.get(row).toString();
    }

    public String getScreenText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            sb.append(getScreenLine(i).toString());
            if (i < height - 1) sb.append('\n');
        }
        return sb.toString();
    }

    public String getAllText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i).toString());
            if (i < lines.size() - 1) sb.append('\n');
        }
        return sb.toString();
    }

    // does not handle 0 width/combining - é wont work
    private int getCharacterWidth(int codePoint) {
        if (codePoint >= 0x1100 &&
            (codePoint <= 0x115F ||
                codePoint == 0x2329 || codePoint == 0x232A ||
                (codePoint >= 0x2E80 && codePoint <= 0xA4CF) ||
                (codePoint >= 0xAC00 && codePoint <= 0xD7A3) ||
                (codePoint >= 0xF900 && codePoint <= 0xFAFF) ||
                (codePoint >= 0xFE10 && codePoint <= 0xFE19) ||
                (codePoint >= 0xFE30 && codePoint <= 0xFE6F) ||
                (codePoint >= 0xFF00 && codePoint <= 0xFF60) ||
                (codePoint >= 0xFFE0 && codePoint <= 0xFFE6) ||
                (codePoint >= 0x1F300 && codePoint <= 0x1FAFF))) {
            return 2;
        }
        return 1;
    }
}
