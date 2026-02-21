package com.jetbrains.internship;

import java.util.ArrayDeque;
import java.util.Deque;

public class TerminalBuffer {
    private int width;
    private int height;

    private int cursorRowPos;
    private int cursorColPos;

    private Deque<Line> lines;
    private long editableStart;
    private int maxScrollbackLines;

    private CellAttribute currentlyUsedAttr; // what style is used for adding new chars

    public TerminalBuffer(int width, int height, int maxScrollbackLines) {
        this.width = width;
        this.height = height;
        this.maxScrollbackLines = maxScrollbackLines;
        cursorColPos = 0;
        cursorRowPos = 0;
        editableStart = 0;
        lines = new ArrayDeque<>(height);
        currentlyUsedAttr = new CellAttribute(); // use default style on init
    }

    public boolean setCurrentlyUsedAttr(byte foregroundColor, byte backgroundColor, byte styleMask){
        return false;
    }

    public int[] getCursorPos(){
        return new int[]{0, 0};
    }

    public boolean setCursorPos(int rowPos, int colPos){
        return false;
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
