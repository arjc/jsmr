import java.util.*;

public class Sheet {
    private Signature signature;
    private List<Measure> measures = new ArrayList<>();

    public Sheet(Signature signature) {
        this.signature = signature;
    }

    public Signature getSignature() {
        return signature;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void addMeasure(Measure measure) {
        measures.add(measure);
    }

    public static class Signature {
        private Clef clef;
        private TimeSignature timeSignature;
        private KeySignature keySignature;

        public Signature(Clef clef, TimeSignature timeSignature, KeySignature keySignature) {
            this.clef = clef;
            this.timeSignature = timeSignature;
            this.keySignature = keySignature;
        }

        public Clef getClef() { return clef; }
        public TimeSignature getTimeSignature() { return timeSignature; }
        public KeySignature getKeySignature() { return keySignature; }
    }

    public static class TimeSignature {
        private int beats;
        private int beatUnit;

        public TimeSignature(int beats, int beatUnit) {
            if (beats <= 0 || beatUnit <= 0) {
                throw new IllegalArgumentException("Time signature values must be positive");
            }
            this.beats = beats;
            this.beatUnit = beatUnit;
        }

        public int getBeats() { return beats; }
        public int getBeatUnit() { return beatUnit; }
    }

    public static class KeySignature {
        private int accidentals;
        private boolean minor;

        public KeySignature(int accidentals, boolean minor) {
            if (accidentals < -7 || accidentals > 7) {
                throw new IllegalArgumentException("A key signature must have 0 to 7 sharps or flats");
            }
            this.accidentals = accidentals;
            this.minor = minor;
        }

        public int getAccidentals() { return accidentals; }
        public boolean isMinor() { return minor; }
    }

    public static class Measure {
        private List<Note> notes = new ArrayList<>();
        private Barline barline;

        public List<Note> getNotes() { return notes; }
        public Barline getBarline() { return barline; }
        public void addNote(Note note) { notes.add(note); }
        public void setBarline(Barline barline) { this.barline = barline; }
    }

    public static class Note {
        private String pitch;
        private Duration duration;
        private Accidental accidental;
        private boolean dotted;

        public Note(String pitch, Duration duration) {
            this(pitch, duration, Accidental.NONE, false);
        }

        public Note(String pitch, Duration duration, Accidental accidental, boolean dotted) {
            this.pitch = pitch;
            this.duration = duration;
            this.accidental = accidental;
            this.dotted = dotted;
        }

        public String getPitch() { return pitch; }
        public Duration getDuration() { return duration; }
        public Accidental getAccidental() { return accidental; }
        public boolean isDotted() { return dotted; }
    }

    public static class Barline {
        private Type type;

        public Barline(Type type) { this.type = type; }
        public Type getType() { return type; }

        public enum Type { SINGLE, DOUBLE, FINAL, REPEAT_START, REPEAT_END }
    }

    public enum Clef { TREBLE, BASS }
    public enum Duration { WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH }
    public enum Accidental { NONE, SHARP, FLAT, NATURAL, DOUBLE_SHARP, DOUBLE_FLAT }
}
