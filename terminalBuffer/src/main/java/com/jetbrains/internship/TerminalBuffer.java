package com.jetbrains.internship;

import java.util.ArrayDeque;
import java.util.Deque;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollbackLines;
    private int cursorRowPos;
    private int cursorColPos;
    private final LineBuffer lines;
    private CellAttribute currentlyUsedAttr; // what style is used for adding new chars

    public TerminalBuffer(int width, int height, int maxScrollbackLines) {
        this.width = width;
        this.height = height;
        this.maxScrollbackLines = maxScrollbackLines;

        this.cursorRowPos = 0;
        this.cursorColPos = 0;

        int capacity = maxScrollbackLines + height;
        this.lines = new LineBuffer(capacity);

        this.currentlyUsedAttr = new CellAttribute();

        // init screen
        for (int i = 0; i < height; i++) {
            lines.addLast(new Line(width, currentlyUsedAttr));
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

    public boolean setCursorPos(int rowPos, int colPos) {
        if (rowPos < 0 || rowPos >= height) return false;
        if (colPos < 0 || colPos >= width) return false;

        Line line = getScreenLine(rowPos);

        // If we land on a trailing cell, move left to the leading cell
        int col = colPos;
        while (col > 0 && line.getCellType(col) == CellType.trailing) {
            col--;
        }

        // check: should never end on trailing
        if (line.getCellType(col) == CellType.trailing) {
            return false; // corrupt line state, should not happen
        }

        cursorRowPos = rowPos;
        cursorColPos = col;
        return true;
    }

    public boolean writeTextOnLine(String text){
        return false;
    }

    public boolean insertTextOnLine(String text){
        return false;
    }

    public boolean fillLineWithChar(int unicodeVal){
        return false;
    }

    public void insertEmptyLineAtEnd(){

    }

    public void clearEditable(){

    }

    public void clearAll(){

    }

    public int getUnicodeAt(int rowPos, int colPos){
        return 0;
    }

    public CellAttribute getAttributesAt(int rowPos, int colPos){
        return null;
    }

    public String getLineAsString(int row){
        return null;
    }

    public String getScreenText(){
        return null;
    }

    public String getAllText(){
        return null;
    }

}
