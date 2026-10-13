# JSMR

## Java Sheet Music Reader

JSMR is an experimental Java application that reads a raster image of a
single-staff music score, identifies basic musical symbols, converts the
detected notes into an internal expression, and plays the result through the
Java MIDI synthesizer.

The project is intentionally built with standard JDK APIs. It does not use an
external image-processing, computer-vision, GUI, or MIDI library.

> **Project status:** This is an in-development prototype. It currently
> targets clean, high-contrast images containing one staff and a limited set of
> notation symbols.

## Contents

- [Features](#features)
- [Project structure](#project-structure)
- [Requirements](#requirements)
- [Building and running](#building-and-running)
- [Using the application](#using-the-application)
- [Application state and event flow](#application-state-and-event-flow)
- [Export file specification](#export-file-specification)
- [Image-processing pipeline](#image-processing-pipeline)
- [Music detection](#music-detection)
- [Duration detection](#duration-detection)
- [Note representation](#note-representation)
- [MIDI playback](#midi-playback)
- [Public API](#public-api)
- [Debug overlay](#debug-overlay)
- [Input requirements](#input-requirements)
- [Current limitations](#current-limitations)
- [Development notes](#development-notes)

## Features

- Swing-based desktop user interface.
- Import of common image formats supported by `ImageIO`.
- Conversion of imported images to a black-and-white bitmap.
- Detection of five horizontal staff lines.
- Detection of vertical barlines.
- Connected-component analysis using a four-direction breadth-first search.
- Approximate note-head detection.
- Treble- and bass-clef note-position mapping.
- Detection of long vertical stems.
- Detection of horizontal beams.
- Approximate note-duration detection.
- Red, green, and blue diagnostic boxes drawn over the processed image.
- Playback through `javax.sound.midi`.
- Piano and guitar playback buttons.
- A red playhead that follows the currently played note.

## Project structure

```text
jsmr/
├── README.md
├── public/
│   ├── elise.png
│   ├── mary.jpg
│   ├── ss1.png
│   ├── twinkle.png
│   └── wire.png
├── src/
│   ├── Expression.java
│   ├── main.java
│   ├── MusicSheet.java
│   └── Sheet.java
├── bin/
└── out/
```

### Source files

#### `main.java`

Creates the Swing window, import button, clef selector, playback controls,
image panel, and playhead. It calls `MusicSheet.generate(...)` whenever an
image is imported or the selected clef changes.

The file is currently stored as `main.java`, while its public class is named
`Main`. This works on the case-insensitive Windows filesystem used by the
project, but the file should be renamed to `Main.java` before building on a
case-sensitive filesystem.

#### `MusicSheet.java`

Contains the image-processing pipeline:

1. Binarizes the source image.
2. Locates staff lines and barlines.
3. Defines the staff processing region.
4. Finds connected black-pixel clusters.
5. Detects beams and stems.
6. Filters clusters that resemble note heads.
7. Maps note-head positions to pitches.
8. Assigns durations.
9. Draws the diagnostic overlay.

#### `Expression.java`

Defines `Expression.Note` and sends the generated notes to a Java MIDI
synthesizer for playback.

#### `Sheet.java`

Contains sheet-music model types such as notes, measures, clefs, durations,
signatures, and barlines. The current image-reader path primarily uses
`Expression.Note`.

## Requirements

- Java Development Kit (JDK) 8 or newer.
- A graphical desktop environment capable of running Swing.
- A working Java MIDI synthesizer for audio playback.
- A high-contrast image of one staff of sheet music.

The source uses lambdas, the diamond operator, `BufferedImage`, Swing, and
Java MIDI APIs. A recent JDK is recommended.

## Building and running

There is currently no Maven or Gradle build file. Compile the Java sources
directly with the JDK.

### Windows PowerShell

From the repository root:

```powershell
New-Item -ItemType Directory -Force out
javac -d out src\*.java
java -cp out Main
```

### macOS or Linux

From the repository root:

```bash
mkdir -p out
javac -d out src/*.java
java -cp out Main
```

The compiled classes are written to `out/`. If the directory contains stale
classes from an earlier build, remove only the generated class files and
compile again.

## Using the application

### Importing an image

1. Start the application.
2. Click **Import**.
3. Select an image file.
4. JSMR processes the image and displays the annotated result.

The selected source image is retained in memory so it can be processed again
when the clef changes.

### Selecting a clef

The clef selector has two choices:

- **Treble Clef**: uses the G-clef note mapping.
- **Bass Clef**: uses the F-clef note mapping.

Changing the selection reprocesses the retained source image and replaces the
displayed result.

### Playing notes

- **Play 1** starts playback using MIDI instrument program `1`.
- **Play 2** starts playback using MIDI instrument program `27`.
- **Stop** interrupts the playback thread and removes the playhead.
- **Export Notes** saves the detected notation to a text file.

The two program numbers are passed to
`MidiChannel.programChange(...)`. The exact instrument name can depend on the
MIDI sound bank installed on the operating system.

### Exporting note notation

Click **Export Notes** after an image has been processed. JSMR opens a save
dialogue and suggests `note-notation.txt`. The selected filename receives a
`.txt` extension when one is not already present. Existing files require
confirmation before replacement. The export operation writes the current
contents of `notesArr`; it does not rerun image recognition.

The generated file contains the selected clef, the current fixed time-signature
label, and one note per line:

```text
Clef: G clef
Time signature: 4/4

C3 Crotchet (1/4)
D3 Minim (1/2)
E3 Quaver (1/8)
```

The time signature is currently written as `4/4` because the image reader does
not yet detect time-signature symbols. The duration names and fractions use
the detected duration value:

| Value | Exported name | Fraction |
|---:|---|---|
| `1` | Semibreve | `1/1` |
| `2` | Minim | `1/2` |
| `4` | Crotchet | `1/4` |
| `8` | Quaver | `1/8` |
| `16` | Semiquaver | `1/16` |

Export is disabled logically rather than visually when no notes are available:
clicking it shows a message asking the user to import an image and detect at
least one note first.

## Application state and event flow

The Swing application has three important pieces of state:

| State | Field | Meaning |
|---|---|---|
| Source image | `sheetImg` | The last successfully loaded source image |
| Detected notes | `notesArr` | The mutable list populated by `MusicSheet.generate(...)` |
| Display image | `sheetImgCtr.i` | The binary image with diagnostic boxes |

The source image and display image are deliberately different objects. The
source image remains unmodified so that changing the clef can process the
original pixels again rather than attempting to analyze an image that already
contains colored overlay rectangles.

### Startup flow

When `Main` is constructed:

1. A `JFrame` is created with the title `JSMR - Java Sheet Music Reader`.
2. The frame is configured to close the application when its window closes.
3. The minimum window size is set to `1920 × 360`.
4. The control panel is created with `GridBagLayout`.
5. The image panel is placed in the center of the frame.
6. Button and combo-box listeners are registered.
7. JSMR attempts to load `Documents\staffTEST.jpg` below the current user's
   home directory.
8. If that test image exists and can be read, it is processed automatically
   with the bass-clef index (`1`).
9. The frame is made visible.

Failure to load the optional startup image is ignored. The user can still use
**Import** afterward.

### Import flow

When **Import** is clicked:

1. A `JFileChooser` opens.
2. Canceling the chooser stops the operation without changing the current
   source image or note list.
3. The selected file is passed to `ImageIO.read(...)`.
4. The resulting `BufferedImage` becomes `sheetImg`.
5. `notesArr` is cleared.
6. `MusicSheet.generate(...)` repopulates `notesArr` and returns an annotated
   image.
7. The annotated image is displayed.

If image loading or processing throws an exception, the current implementation
prints `Select a valid image...` to standard output. It does not display a
Swing error dialog for import failures.

### Clef-change flow

Changing the combo box:

1. Prints the selected clef to standard output.
2. Does nothing if no source image has been imported or loaded.
3. Clears `notesArr`.
4. Reprocesses the retained original `sheetImg`.
5. Replaces the displayed annotated image.

Changing the clef therefore changes pitch mapping and exported clef metadata,
but does not change the source image or the image-detection thresholds.

### Playback flow

Clicking either playback button starts a new `Thread`. That thread calls
`Expression.play(...)` and updates the image-panel playhead through the callback
passed to that method.

Clicking **Stop** interrupts the current playback thread if one exists and
resets the playhead to `-1`. The application does not disable the playback
buttons while another playback thread is active, so clicking a playback button
multiple times can create multiple playback threads.

### Export flow

Clicking **Export Notes** invokes `exportNotes(...)` with the frame and the
currently selected clef index. The method does not infer the clef from the
image; it uses the combo-box selection at the time of export.

The export flow is:

1. Check whether `notesArr` is empty.
2. If it is empty, show an informational `No notes to export` dialog and stop.
3. Create a `JFileChooser`.
4. Set its title to `Save note notation`.
5. Suggest `note-notation.txt` as the filename.
6. Show the save dialog.
7. Stop without writing if the user cancels.
8. Append `.txt` if the chosen filename does not already end in `.txt`,
   ignoring case.
9. Check whether the final path already exists.
10. Ask for confirmation before replacement.
11. Open a `BufferedWriter` through `FileWriter`.
12. Write the header and one line for each note.
13. Show the absolute output path in an `Export complete` dialog.

An `IOException` produces an `Export failed` error dialog containing the
underlying exception message. The implementation does not silently report a
failed write as successful.

## Export file specification

The export is a Java text stream using the platform-default encoding selected
by `FileWriter`. It is intended for human reading, not as a stable machine
interchange format. The exact encoding and line-ending behavior can therefore
vary between operating systems.

### Exact record order

The file always contains:

1. One clef header line.
2. One time-signature header line.
3. One blank line.
4. One note line for every note currently in `notesArr`.

The current writer does not add measure separators, bar numbers, source-image
coordinates, MIDI numbers, velocities, or confidence values.

### Header grammar

The header has this exact shape:

```text
Clef: <clef-name>
Time signature: 4/4
```

`<clef-name>` is:

- `G clef` when the combo-box index is `0` (Treble Clef);
- `F clef` for every other index currently available in the UI (Bass Clef).

The time-signature line is always `Time signature: 4/4`. This is a declaration
of the current export format, not a value recognized from the imported image.
Time-signature recognition has not yet been implemented.

### Note-line grammar

Each note is formatted as:

```text
<pitch-name><octave> <duration-name> (<duration-fraction>)
```

Examples:

```text
C3 Crotchet (1/4)
F#4 Quaver (1/8)
Bb2 Minim (1/2)
```

The pitch name is generated from the twelve-element name table:

```text
C, C#, D, D#, E, F, F#, G, G#, A, A#, B
```

The implementation selects the base name with `Math.floorMod(off, 12)`.
Positive `mod` values append one `#` per modifier unit. Negative `mod` values
append one `b` per modifier unit. The current image detector creates notes
with `mod = 0`, so accidental text is available in the formatter but is not
currently produced by image recognition.

The octave is appended exactly as stored in `Expression.Note.oct`; it is not
recalculated during export.

### Duration export mapping

The duration name is selected by an exact integer switch:

| `dur` | Name | Fraction text |
|---:|---|---|
| `1` | `Semibreve` | `1/1` |
| `2` | `Minim` | `1/2` |
| `4` | `Crotchet` | `1/4` |
| `8` | `Quaver` | `1/8` |
| `16` | `Semiquaver` | `1/16` |

For positive values not listed above, the name is `Unknown duration` and the
fraction is `1/<value>`. For zero or negative values, the fraction is
`unknown`. Normal image detection currently generates values `1`, `2`, `4`,
or `8`.

### Export examples

Treble-clef export:

```text
Clef: G clef
Time signature: 4/4

C4 Semibreve (1/1)
D4 Minim (1/2)
E4 Crotchet (1/4)
F4 Quaver (1/8)
```

Bass-clef export:

```text
Clef: F clef
Time signature: 4/4

G2 Crotchet (1/4)
A2 Crotchet (1/4)
B2 Quaver (1/8)
```

### File-name behavior

The chooser initially proposes:

```text
note-notation.txt
```

If the user enters `score`, the final file is `score.txt`. If the user enters
`score.TXT`, no additional extension is appended. If the selected path already
exists, the file is written only after the user chooses **Yes** in the
replacement confirmation dialog.

The extension check applies only to the filename string. It does not validate
that the selected location is writable before opening the writer.

## Image-processing pipeline

### 1. Binarization

`MusicSheet.getBwImg(...)` creates a `TYPE_BYTE_BINARY` image and draws the
source image into it. The result is a black-and-white bitmap rather than a
grayscale image.

The detector treats any pixel whose RGB value is not `-1` as black or occupied.
Consequently, the best input is a dark notation symbol on a white background.

### 2. Staff-line detection

The detector scans every image row and counts non-white pixels. A row is
treated as part of a staff line when at least 30% of the image width is
occupied.

Contiguous staff-line rows are stored in `igY`. The first row of each run is
stored in `staffYIdx`. The normal input is expected to contain at least five
staff lines.

If fewer than five staff lines are found, processing stops and the binary image
is returned.

### 3. Barline detection

For every image column, the detector checks whether all rows from the first to
the fifth staff line are occupied. Such columns are treated as barlines.

The barline columns are stored in `igX`, and the first column of each
contiguous barline run is stored in `barXIdx`.

At least two barline groups are required for the normal processing path. If
fewer than two are found, the binary image is returned without note detection.

### 4. Processing region

The first and last detected barline define the main note-head clustering region.
The vertical region extends above and below the staff by three staff spaces.

Note-head connected components are searched inside this region. Stem and beam
scans use the full image width so symbols before the first detected barline are
not excluded.

### 5. Connected components

`getClusters(...)` scans the processing region and uses a four-direction
breadth-first search:

- left
- right
- up
- down

Each connected component becomes a `Cluster` containing:

| Property | Meaning |
|---|---|
| `x` | Left coordinate |
| `y` | Top coordinate |
| `w` | Bounding-box width |
| `h` | Bounding-box height |
| `nBlack` | Number of occupied pixels in the component |

Staff-line rows and barline columns are excluded while components are built.
This prevents a staff line from joining many symbols into one enormous
component.

## Music detection

### Note-head detection

The distance between the third and fourth detected staff lines is used as the
staff-space reference size.

A cluster is considered a possible note head when:

- its height is less than `1.3 × staff-space`;
- its height is greater than `0.2 × staff-space`;
- its width is less than `2 × staff-space`;
- its width is greater than `1 × staff-space`.

These are heuristic geometric filters, not a complete music-notation
classifier.

When two candidate heads overlap horizontally but occupy different vertical
positions, the upper cluster is retained and marked as being on a staff line.
This is intended to reduce duplicate detections caused by note shapes touching
nearby staff positions.

### Pitch mapping

The note-head vertical position is converted to a diatonic position relative to
the five staff lines. The selected clef then maps that position to a MIDI-style
pitch offset and octave.

The current implementation supports the internal `G_CLEF` and `F_CLEF`
arrays. Accidentals are not inferred from image symbols in the current
pipeline; generated notes use `mod = 0`.

### Stem detection

A stem is detected by scanning each image column and finding the vertical
extent of occupied pixels after excluding staff rows and barline columns.

A column qualifies as a stem when:

- its occupied vertical extent is greater than `2.5 × staff-space`;
- at least half of the pixels across that extent are occupied.

Adjacent qualifying columns with overlapping vertical ranges are merged into a
single stem cluster.

### Beam detection

The beam scanner examines occupied horizontal runs. A run qualifies as a beam
when its width is at least `2 × staff-space`.

Adjacent beam runs that overlap horizontally and occur on neighboring rows are
merged. Beam detection is heuristic and may also identify other long horizontal
marks that resemble beams.

## Duration detection

Duration detection is applied to each accepted note head after stem and beam
clusters have been detected.

The current rules are:

| Condition | Duration value |
|---|---:|
| No attached stem | `1` |
| Stem attached to the head | `2` |
| Head density greater than 50% | `4` |
| Beam within 10 pixels horizontally | `8` |

The implementation applies the rules in this order:

```java
int dur = 1;
if (hasStemAttached(c)) dur = 2;
if (c.nBlack > 0.5 * (c.w * c.h)) dur = 4;
if (hasNearbyBeam(c, 10)) dur = 8;
```

Therefore, later conditions take precedence. A head with a nearby beam receives
`dur = 8` even if it also satisfies the filled-density rule.

The duration values are inverse rhythmic values used by the MIDI player:

- `1`: whole-note-like duration
- `2`: half-note-like duration
- `4`: quarter-note-like duration
- `8`: eighth-note-like duration

The duration classifier currently does not distinguish every possible musical
notation case, such as ties, dotted notes, rests, tuplets, or multiple beam
levels.

## Note representation

`Expression.Note` stores:

| Field | Meaning |
|---|---|
| `off` | Semitone offset within the octave |
| `mod` | Accidental modifier: sharp `1`, natural `0`, flat `-1` |
| `oct` | Octave |
| `dur` | Inverse rhythmic duration |
| `vel` | MIDI velocity; `0` represents a rest |
| `x` | Source-image x-coordinate used by the playhead |

Generated notes currently use:

- `mod = 0`
- `vel = 100`
- the detected note-head x-coordinate for `x`

## MIDI playback

Playback is implemented with the standard Java Sound MIDI API:

1. Open the default `Synthesizer`.
2. Select channel `0`.
3. Change the instrument program.
4. Convert each note to a MIDI note number.
5. Call `noteOn(...)`.
6. Sleep for `2000 / dur` milliseconds.
7. Call `noteOff(...)`.
8. Close the synthesizer after playback.

The MIDI note calculation includes the configured transposition:

```text
((octave + 2) × 12 + off + mod) + 5
```

Notes outside the MIDI range `0..127` are skipped. A note with velocity `0`
waits for its duration without calling `noteOn(...)`.

## Public API

### `MusicSheet.getBwImg`

```java
public static BufferedImage getBwImg(BufferedImage image)
```

Returns a binary copy of the supplied image.

### `MusicSheet.generate`

```java
public static BufferedImage generate(
    BufferedImage image,
    ArrayList<Expression.Note> notes,
    int currClef
)
```

Processes the image, clears and fills the supplied note list, and returns an
annotated image.

Parameters:

- `image`: source sheet-music image; must not be `null`.
- `notes`: mutable output list; must not be `null`.
- `currClef`: `0` selects treble clef; `1` selects bass clef in the current UI.

The method throws `IllegalArgumentException` when the image or note list is
`null`.

If staff-line or barline detection fails, it returns the binary image and may
leave the note list empty.

### `MusicSheet.Cluster`

`Cluster` is a public nested data object exposing its position, dimensions,
black-pixel count, and whether the candidate was marked as on a staff line.

## Debug overlay

The returned image preserves the binary source and draws diagnostic rectangles:

| Color | Detected object |
|---|---|
| Red | Long vertical stems |
| Green | Note-head candidates |
| Blue | Horizontal beam candidates |

The overlay is intended for debugging the detector, not for producing a clean
engraved score.

## Input requirements

For reliable results, use an image with:

- one clearly visible five-line staff;
- a white or near-white background;
- dark, high-contrast notation;
- minimal skew or rotation;
- consistent scale;
- visible barlines;
- enough resolution for staff lines and note heads to occupy multiple pixels.

The current detector is sensitive to scaling because its thresholds are
derived from staff spacing. It is also sensitive to noise, compression
artifacts, shadows, antialiasing, and broken staff lines.

## Current limitations

- The application is designed for a single staff, not a full multi-staff score.
- Staff-line detection uses a fixed 30% row-occupancy threshold.
- At least five staff lines and two barline groups are expected.
- Clef symbols are selected by the user; they are not recognized from the image.
- Key signatures and time signatures are not interpreted.
- Accidentals are not detected; generated notes use `mod = 0`.
- Rests, ties, slurs, dots, tuplets, and articulation marks are not fully
  supported.
- Beam detection is geometric and can produce false positives.
- Stem detection can classify other long vertical marks as stems.
- Duration detection is an approximation based on geometry and pixel density.
- The connected-component algorithm uses a full-image `boolean` visited array,
  which can consume substantial memory for very large images.
- Playback runs on a worker thread but the application does not maintain a
  dedicated playback queue.
- The current UI reports invalid imports with a generic console message.
- The startup test image is read from
  `Documents\staffTEST.jpg` under the current user's home directory when that
  file exists.
- There is no automated test suite or dependency-management configuration yet.

## Development notes

### Reprocessing

`Main` retains the imported source image in `sheetImg`. Changing the clef
reuses that original image rather than processing the already annotated output.

### Coordinate systems

Detection coordinates are source-image pixels. The Swing image panel scales the
annotated image to fit the window. The playhead converts the detected source
coordinate into the scaled display coordinate.

### Adding a new detector

New symbol detectors should generally:

1. derive thresholds from staff spacing where possible;
2. avoid staff rows in `igY`;
3. avoid barline columns in `igX`;
4. store results as `Cluster` objects;
5. run before note generation if the result affects pitch or duration;
6. add a distinct overlay color when visual debugging is useful.

### Rebuilding after source changes

Always compile all source files together so changes to shared model classes are
included:

```powershell
javac -d out src\*.java
```

## License and project metadata
bbg 
The repository currently does not include a license file or a formal build
descriptor. Add project licensing and release metadata before distributing
JSMR outside the development environment.
