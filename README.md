# JetBrains Terminal Text Buffer

## Project description

This repository contains a **Java/Maven skeleton** for a terminal text buffer project.
Created for 2026 summer JetBrains intership applications.

- A **root Maven aggregator** manages the repository as a multi-module build.

## Design choices

The primary focus was to keep memory footprint low, as I believe a terminal programme should not take too many resources, you never know on what device it will run.<br>
As the primary object meant for interaction is the TerminalBuffer class. It provides the methods for retrieving and manipulating content specified in the task.
Some user experience enhancements are provided, like cursor snapping to leading cell of double wide characters.

A circular buffer is implemented for holding lines, when capacity is reached (screen height + scrollback limit), oldest content begins to be overridden, saving costly operations,<br>
like reassigning the underlying arrays. It also permits O(1) random access, an improvement over originally intended Dequeue.<br>
<br>
Cells are represented by an array of codepoint ints held in Line class, an array of bytes is used for cell type representation. Cells are either 0 - normal, 1 - leading, 2 - trailing.
This is used to allow for 2-cell wide unicode characters, such as emojis. Trailing cells are always kept empty as the leading contains the codepoint.
Line supports clearing, putting codepoints, and insertion of cells. Spill is handled by a helper class LineFragment, where the spilled codepoints are stored and later propagated downwards by TerminalBuffer.

Another optimization for memory is sharing CellAttribute objects by many cell. But instead of keeping an object reference for each cell, we keep a list of AttributeSequence objects, represented by starting column and length.
As most of the time consecutive chars will share the same style, we can expect only a few to be present per line. To avoid fragmentation on insertion a cleanup is implemented, merging identical sequences.

The CellAttributes are represented by a record, containing foreground and background color (-1 for default, 0-15 meant for default terminal colors). Style (bold, italic, underline, strikethrough) is represented by one byte style mask.
While this keeps memory usage low, it also limits possible extension of provided style flags and would require bitwise operations on the frontend to find out how text is supposed to be rendered.

## What could be improved

- Instead of silently ignoring invalid inputs, a proper exception system should be in place.
- Style mask should be exchanged for a solution more open for extension.
- Spill mechanism would be rather slow for large buffer sizes as it is done basically character by character.
- Support for resizing (with current spill system would be an extremely expensive operation)
- Add simple renderer class to see buffer content in action.

## Why the Maven structure looks like this

### Root `pom.xml` is an aggregator (packaging `pom`)
The root POM is intentionally lightweight and is used to:

- define repository-level coordinates,
- list Maven modules,
- centralize build entry points (`mvn test`, `mvn verify` from repo root).

## Repository layout

```text
jetbrains-terminal-text-buffer/
├── pom.xml                  # Root aggregator/parent entry point
├── README.md
└── terminalBuffer/
    └── pom.xml              # Module build + dependencies
```

## Prerequisites

- Java 21+
- Apache Maven 3.9+

## Build and test

Run from repository root:

```bash
mvn -N validate
mvn validate
mvn test
mvn clean verify
```
