# JSMR
## AKA Project Java Sheet Music Reader

By Arjun M Liji

![Alpha screenshot](https://github.com/arjc/smr/blob/main/public/ss1.png)

> current indev build screenshot 4th Aug 26

## Overview

This java sheet music reader is a Image to music program which only uses vanila dependancies built into jdk.

It works by taking in a raster image of a sheet music (single staff only) and then processing it further to be mapped to a binary monochrome image. A cluster allocation algo is performed to find the note heads and we use bfs to look for symbols around the head, for instance, a stem could be found to the right or to the left of a note head.
wild symbols use use a template recognition algorithm to identify matching patterns.

# Features

### User Interface

SMR use Swing to build an simple and user firendly UI.
`swing.Box, swing.BoxLayout, swing.DefaultComboBoxModel, swing.JButton, swing.JComboBox, swing.JFileChooser, swing.JFrame, swing.JPanel, swing.SwingUtilities, swing.UIManager and swing.filechooser.FileNameExtensionFilter` are the dependancies imported from swing

![Outline of the final product](https://github.com/arjc/smr/blob/main/public/wire.png)

> Final interface

# Image to expression

### brief

We first recive the image from swing. Then convert it to mononchromatic and store the image as a binary bitmap.

Then use formulas and some math to determine what musical symbol it is.

### Import Image

Get the image from swing and we store as binary using `imageio`'s BufferedImage

`BufferedImage image = ImageIO.read(file);`

### Process the image

### Convert the image to monochrome (NOT GRAY SCALE!!!).

We need to do this since out binary bitmap cannot store range of grayness.

Assuming that the user shall only input a high resolution image for better accuracy. (Has experienced some issues while scaning lower quality images)

<!-- This may/ may not affect the staff symbol reading accuracy but we shall assume that the user has input a high resolution image -->

```

Graphics2D g = mono.createGraphics();

g.drawImage(image, 0, 0, null);

g.dispose();

```


# MIDI Expression player

## ExpPlayer class
### off: note offset for c major notes from C to B
```
C = 0
D = 2
E = 4
F = 5
G = 7
A = 9
B = 11
```
### oct: octave of the note
Octave range from 0 to 8
treble clef from: G3-D6 with ledger and E4-F5 without ledger

### dur: inverse of duration of the note
full note = 1
half note = 2 ie., 1/2
quater note = 4 ie., 1/4
### mod: note tone modification 
Sharp # = 1
flat b = -1
natural H = 0
### vel: velocity of the note
Mainly used to represent a timed rests when `vel = 0`
`vel = 100` used for all notes for all octaves by default.

## Play function

initialise and import `Midisystem`
`.getSynthesizer()` is used to initialise a synth object
`.programChange()[instrumentIndex]` instrument index corresponds to a specific instrument available with 
For each note in the note array we use `chanel.noteOn()`


