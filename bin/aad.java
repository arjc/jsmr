import javax.sound.midi.*;

public class aad {
    public static void main(String[] args) {
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();

            
            MidiChannel ch = synth.getChannels()[0];
            ch.programChange(1);

            int note = 76;
            ch.noteOn(note, 100);
            Thread.sleep(2000);
            ch.noteOff(note);

            ch.noteOn(note, 100);
            Thread.sleep(2000);
            ch.noteOff(note);

            ch.noteOn(note, 100);
            Thread.sleep(5000);
            ch.noteOff(note);

            synth.close();
        } catch (Exception e) { System.out.println(e);}
        
    }
    
}