import java.awt.*; 
import java.awt.image.*; 
import java.io.*; 
import java.util.*;

import javax.imageio.*;
import javax.swing.*; 

public class Main {
    private static final String[] NOTE_NAMES =
        {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private static class ImgContainer extends JPanel {
        private BufferedImage i; 
        private int playheadX = -1;
        private void setImage(BufferedImage i) { this.i = i; repaint();} 
        private void setPlayhead(int x) { playheadX = x; repaint(); }
        @Override 
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            if (i == null) return;
            int x = 10, y = 10, width = getWidth() - 20, height = getHeight() - 20;
            g.drawImage(i, x, y, width, height, null);
            
            if (playheadX >= 0) { 
                g.setColor(Color.RED); int lineX = x + playheadX * width / i.getWidth();
                g.drawLine(lineX, y, lineX, y + height); 
            }
        }
    }

    private ImgContainer sheetImgCtr = new ImgContainer(); 
    private BufferedImage sheetImg; 
    private Thread playTrd;

    ArrayList<Expression.Note> notesArr = new ArrayList<>();

    public Main() {
        JFrame fr = new JFrame("JSMR - Java Sheet Music Reader"); 
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        fr.setMinimumSize(new Dimension(512, 360)); 
        fr.setSize(new Dimension(1920, 360)); 
        fr.setLocationRelativeTo(null); 

        Container mainWrp = fr.getContentPane();
        JPanel ctrlsCtr = new JPanel(new GridBagLayout());
        JButton impBtn = new JButton("Import"), 
                play1Btn = new JButton("Play 1"), 
                play2Btn = new JButton("Play 2"), 
                haltBtn = new JButton("Stop"),
                exportBtn = new JButton("Export Notes");
        JComboBox<String> clefSelect = new JComboBox<>(
            new DefaultComboBoxModel<>(new String[] { "𝄞 Treble Clef", "𝄢 Bass Clef" })
        );// | import |    | clef |    | play 1 |    | play 2 |    | stop |
        
        ctrlsCtr.add(impBtn); 
        ctrlsCtr.add(clefSelect); 
        ctrlsCtr.add(play1Btn); 
        ctrlsCtr.add(play2Btn); 
        ctrlsCtr.add(haltBtn);  
        ctrlsCtr.add(exportBtn);
        mainWrp.add(ctrlsCtr, BorderLayout.NORTH);
        mainWrp.add(sheetImgCtr, BorderLayout.CENTER);

        // IMPORT AND KEEP
        impBtn.addActionListener(e -> {
            JFileChooser f = new JFileChooser(); 
            if (f.showOpenDialog(fr) != JFileChooser.APPROVE_OPTION) return; 
            try { 
                sheetImg = ImageIO.read(f.getSelectedFile()); 
                notesArr.clear();
                sheetImgCtr.setImage(MusicSheet.generate(sheetImg, notesArr, clefSelect.getSelectedIndex()));
            } catch (Exception x) { System.out.println("\nSelect a valid image..."); } 
        }); 

        // STOP
        haltBtn.addActionListener(e -> { 
            if (playTrd != null) playTrd.interrupt();
            sheetImgCtr.setPlayhead(-1); 
        });
        play1Btn.addActionListener(e -> play(1));           // Pno
        play2Btn.addActionListener(e -> play(27));          // Gtr
        exportBtn.addActionListener(e -> exportNotes(fr, clefSelect.getSelectedIndex()));

        // COMBOBOX
        clefSelect.addActionListener(e -> { 
            System.out.println("Clef set to " + clefSelect.getSelectedItem()); 
            if (sheetImg == null) return;
            notesArr.clear();
            BufferedImage img = MusicSheet.generate(sheetImg, notesArr, clefSelect.getSelectedIndex());
            sheetImgCtr.setImage(img);
        }); 

        fr.setVisible(true);
    } 
    private void play(int instrument){
        playTrd = new Thread( () -> {
            try { 

                Expression.play(notesArr, instrument, note -> sheetImgCtr.setPlayhead(note.x));


            } catch (Exception e) {}

        }); 
        playTrd.start();
    } 

    private void exportNotes(JFrame parent, int clefIndex) {
        if (notesArr.isEmpty()) {
            System.out.println("Import a sheet image before exporting.");
            return;
        } JFileChooser jc = new JFileChooser();
        jc.setDialogTitle("Save note notation");
        jc.setSelectedFile(new File("practice.txt"));
        if (jc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = jc.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".txt"))
            file = new File(file.getParentFile(), file.getName() + ".txt");
        if (file.exists())
            System.out.println("File exists. Overwriting: " + file.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Clef: " + (clefIndex == 0 ? "G clef" : "F clef"));
            writer.newLine(); 
            writer.write("Time signature: 4/4");
            writer.newLine(); writer.newLine();
            for (Expression.Note note : notesArr) { writer.write(formatNote(note)); writer.newLine(); }
            System.out.println("Note notation saved to:" + file.getAbsolutePath());

        } catch (IOException ex) { System.out.println("Could not save file"); }
    }

    private String formatNote(Expression.Note note) {
        StringBuilder notation = new StringBuilder();
        notation.append(noteName(note)).append(note.oct).append(" ");
        notation.append(durationName(note.dur)).append(" (");
        notation.append(durationFraction(note.dur)).append(")");
        return notation.toString();
    }

    private String noteName(Expression.Note note) {
        String name = NOTE_NAMES[Math.floorMod(note.off, NOTE_NAMES.length)];
        if (note.mod > 0) {
            name += accidentalMarks("#", note.mod);
        } else if (note.mod < 0) {
            name += accidentalMarks("b", -note.mod);
        }
        return name;
    }

    private String accidentalMarks(String mark, int count) {
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < count; i++) marks.append(mark);
        return marks.toString();
    }

    private String durationName(int duration) {
        switch (duration) {
            case 1: return "Semibreve";
            case 2: return "Minim";
            case 4: return "Crotchet";
            case 8: return "Quaver";
            case 16: return "Semiquaver";
            default: return "Unknown duration";
        }
    }

    private String durationFraction(int duration) {
        if (duration <= 0) return "unknown";
        return "1/" + duration;
    }

    public static void main(String[] args) { new Main(); } 
    
}
    
// private void show() { fr.setVisible(true); }
    
// SwingUtilities.invokeLater(() -> {
//     // try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
//     // try { UIManager.setLookAndFeel("javax.swing.playf."); }
//     // catch (Exception ignored) {} 
// });
// notesArr.add(new Expression.Note(4, 0, 4, 8, 100));
// notesArr.add(new Expression.Note(4, 0, 4, 4, 100));
// notesArr.add(new Expression.Note(4, 0, 4, 8, 100));
// notesArr.add(new Expression.Note(4, 0, 4, 8, 100));
// notesArr.add(new Expression.Note(4, 0, 4, 4, 100));
// notesArr.add(new Expression.Note(4, 0, 4, 8, 100));
// notesArr.add(new Expression.Note(7, 0, 4, 8, 100));
// notesArr.add(new Expression.Note(0, 0, 4, 6, 100));
// notesArr.add(new Expression.Note(2, 0, 4, 16, 100));
// notesArr.add(new Expression.Note(4, 0, 4, 4, 100));

// private void haltSheet() { 
    //     if (playTrd != null) playTrd.interrupt(); 
    //     sheetImgCtr.setPlayhead(-1); 
    // } 
// private void regenerateSheet(int selectedClefIdx) {
    //     if (sheetImg == null) return;
    //     BufferedImage img = MusicSheet.generate(sheetImg, notesArr, selectedClefIdx);
//     sheetImgCtr.setImage(img);
// }

// private void importAndKeep(int selectedClefIdx) {
//     JFileChooser f = new JFileChooser(); 
//     int result = f.showOpenDialog(fr);
//     if (result == JFileChooser.APPROVE_OPTION) { 
//         File file = f.getSelectedFile();
//         try { 
//             sheetImg = ImageIO.read(file); 
//             BufferedImage img = 

//                 MusicSheet.generate(sheetImg, notesArr, selectedClefIdx); 

//             sheetImgCtr.setImage(img);
//         } catch (Exception e) { 
//             System.out.println("\nPlease select a valid Sheet music image...");
//         } 
//     }
// } 