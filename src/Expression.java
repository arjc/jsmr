import java.util.ArrayList;
import javax.sound.midi.*;

public class Expression {
    static int trnsps = 5;

    public interface nposInterface {
        void onNotePlayed(Note note);
    }

    public static class Note {
        int off, oct, dur, mod, vel, x;
        public Note(int off, int mod, int oct, int dur, int vel) {
            this(off, mod, oct, dur, vel, -1);
        }
        public Note(int off, int mod, int oct, int dur, int vel, int x) {
            this.off = off; this.mod = mod; this.oct = oct; 
            // (0,  0, 3, dur, vel) [CN3 or C3] 
            // (0,  1, 3, dur, vel) [C#3 or Db3]
            // (0, -1, 3, dur, vel) [Cb3 or B2 (BN2)]
            this.dur = dur; this.vel = vel; 
            // (0,  0, 0, dur, 000) [Rest representation]
            // vel = 100 is used with all notes and vel = 0 is taken as rests
            this.x = x; // For playerHead
        }
    }
    // public static void play(ArrayList<Note> noteArr, int instrument) {
    //     System.out.println("\nPlaying Sheet Music...");
    //     play(noteArr, instrument, note -> {});
    // }

    public static void play(ArrayList<Note> noteArr, int instrument, nposInterface ln) {
        System.out.println("\nPlaying Sheet Music...");
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel channel = synth.getChannels()[0];
            channel.programChange(instrument);
            
            String[] nnn = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
            for (Note n : noteArr) {
                if (n == null || n.dur <= 0) continue;
                ln.onNotePlayed(n);
                // MIDI note numbers use C-1 = 0, so C3 (off = 0, oct = 3) is 48.
                int nt = ((n.oct + 1) * 12 + n.off + n.mod) + trnsps;
                if (n.vel <= 0) {
                    Thread.sleep(2000 / n.dur);
                    continue;
                }
                if (nt < 0 || nt > 127) continue;
                System.out.println("playing note: " + nnn[Math.floorMod(n.off + 5, nnn.length)]);
                channel.noteOn(nt, n.vel); 
                Thread.sleep(2000 / n.dur);
                channel.noteOff(nt);
            }
            System.out.println("\nEnd of sheet music.");
            Thread.sleep(5000);channel.allNotesOff();synth.close();

        } catch (Exception e) {System.out.println("\nSheet music player is halted.");}
    }


}


// public static void main(String[] args) throws Exception {
    

    //     // let: integer number from c to b
    //     // mod: sharp is 1, flat is -1, normal is 0
    //     // dur: 1 is full, 2 is 1/2, 4 is 1/4, 8 is 1/8, 16 is 1/16 ...
    //     // ArrayList<Note> meashureArray = new ArrayList<>();
    //     // meashureArray.add(new Note(1, 0, 4, 2, 0 ));
    
    //     // Thread.sleep(250);
    
    //     // meashureArray.add(new Note(4, 0, 4, 8, 100));
    //     // meashureArray.add(new Note(4, 0, 4, 8, 100));
    //     // meashureArray.add(new Note(4, 0, 4, 4, 100));
    //     // Thread.sleep(250);
    
    //     // meashureArray.add(new Note(4, 0, 4, 8, 100));
    //     // meashureArray.add(new Note(7, 0, 4, 8, 100));
    //     // meashureArray.add(new Note(0, 0, 4, 6,  100));
    //     // meashureArray.add(new Note(2, 0, 4, 16,  100));
    //     // Thread.sleep(250);
    
    //     // meashureArray.add(new Note(4, 0, 4, 8, 100));
    
    //     // play(meashureArray, 25);
    
    //     // meashureArray.add(new Note(5, 0, 5, 2));
    //     // meashureArray.add(new Note(7, 0, 5, 4));
    //     // meashureArray.add(new Note(9, 0, 5, 8));
    //     // int[][] Sheet = { { 67, 125 }, { 78, 125 }, { 92, 125 }, { 68, 125 }, { 32, 125 }, { 62, 125 } };
    
    //     // Scanner scanner = new Scanner(System.in);
    //     // Synthesizer synth = MidiSystem.getSynthesizer();
    
    //     // synth.open();
    //     // MidiChannel channel = synth.getChannels()[0];
    
    //     // // System.out.print("Note (0-127): ");
    //     // // int note = scanner.nextInt();
    //     // // System.out.print("Duration (ms): ");
    //     // // int dur = scanner.nextInt();
    
    //     // for (int[] n : Sheet) {
        //     // channel.noteOn(n[0], 100);
        //     // Thread.sleep(1000);
        //     // channel.noteOff(n[0]);
        //     // Thread.sleep(n[1]);
        //     // }
        
        //     // synth.close();
        //     // scanner.close();
        // }