import java.util.ArrayList;
import javax.sound.midi.*;

public class ExpPlayer {
    public static class Note {
        int off, octave, dur, mod, vel;
        public Note(int off, int mod, int octave, int dur, int vel) {
            this.off = off;
            this.mod = mod;
            this.octave = octave;
            this.dur = dur;
            this.vel = vel;
        }
    }

    public static void playMeashure(ArrayList<Note> noteArr, int instrument) throws Exception {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel channel = synth.getChannels()[0];
        channel.programChange(instrument);
        for (Note n : noteArr) {
            channel.noteOn(n.off + 12 * (n.octave + 1) + n.mod, n.vel);
            Thread.sleep(2000 / n.dur);
            channel.noteOff(n.off + 12 * (n.octave + 1) + n.mod);
        }
        Thread.sleep(5000);
        channel.allNotesOff();
        synth.close();
    }
}
    
    // public static void main(String[] args) throws Exception {
        
        
    //     // let: integer number from c to b
    //     // mod: sharp is 1, flat is -1, normal is 0
    //     // dur: 1 is full, 2 is 1/2, 4 is 1/4, 8 is 1/8, 16 is 1/16 ...
    //     // ArrayList<Note> meashureArray = new ArrayList<>();
    //     // meashureArray.add(new Note(1, 0, 4, 2, 0 ));

    //     // meashureArray.add(new Note(4, 0, 4, 8, 100));
    //     // meashureArray.add(new Note(4, 0, 4, 8, 100));
    //     // meashureArray.add(new Note(4, 0, 4, 4, 100));
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
        
    //     // playMeashure(meashureArray, 25);
        
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