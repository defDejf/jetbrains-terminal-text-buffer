package com.jetbrains.internship;

import java.util.Deque;

public class TerminalBuffer {
    private int width;
    private int height;
    private int cursorRowPos;
    private int cursorColPos;
    private Deque<Line> lines;
    private long editableStart;
}
