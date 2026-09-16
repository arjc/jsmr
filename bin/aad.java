import javax.sound.midi.*;

public class aad {
    public static void main(String[] args) {
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();

            
            MidiChannel ch = synth.getChannels()[0];
            ch.programChange(1);

            ch.noteOn(100, 100);
            Thread.sleep(1000);
            ch.noteOff(0, 100);

            ch.noteOn(101, 100);
            Thread.sleep(1000);
            ch.noteOff(1, 100);

            ch.noteOn(102, 100);
            Thread.sleep(1000);
            ch.noteOff(2, 100);

            synth.close();
        } catch (Exception e) { System.out.println(e);}
        
    }
    
}