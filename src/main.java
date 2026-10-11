import java.awt.*; 
import java.awt.image.*; 
import java.io.*; 
import java.util.*;

import javax.imageio.*;
import javax.swing.*; 

public class Main {

    private static class ImgContainer extends JPanel {
        private BufferedImage i; 
        private int playheadX = -1;
        
        private void setImage(BufferedImage i) { 
            this.i = i; 
            repaint();
        } 
        private void setPlayhead(int x) { 
            playheadX = x; 
            repaint(); 
        }

        @Override 
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            if (i == null) return;
            double scale = Math.min(
                (double) (getWidth() - 36) / i.getWidth(), 
                (double) (getWidth() - 36) / i.getHeight()
            );
            int drawWidth = (int) Math.round(i.getWidth() * scale);
            int drawHeight = (int) Math.round(i.getHeight() * scale);
            int x = (getWidth() - drawWidth) / 2, y = (getHeight() - drawHeight) / 2; 
            g.drawImage(i, x, y, drawWidth, drawHeight, null);

            // if (playheadX >= 0) { 
            //     g.setColor(Color.RED); 
            //     int lineX = x + (int) (playheadX * scale); 
            //     g.drawLine(lineX, y, lineX, y + drawHeight); 
            // }
            if (playheadX >= 0) { 
                g.setColor(Color.RED); 
                int lineX = x + (int) (playheadX * scale); 
                g.drawLine(lineX, y, lineX, y + drawHeight); 
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
        fr.setSize(670, 400); 
        fr.setMinimumSize(new Dimension(670, 100)); 
        fr.setLocationRelativeTo(null); 

        Container mainWraper = fr.getContentPane();
        // mainWraper.setLayout(new BorderLayout(12, 12));
        JPanel ctrlsContainer = new JPanel(new GridLayout());
        JButton impBtn = new JButton("Import"), 
                play1Btn = new JButton("Play 1"), 
                play2Btn = new JButton("Play 2"), 
                haltBtn = new JButton("Stop");
        JComboBox<String> clefSelect = new JComboBox<>(new DefaultComboBoxModel<>(
            new String[] { "𝄞 Treble Clef", "𝄢 Bass Clef" }
        ));
        // | import |    | clef |    | play 1 |    | play 2 |    | stop |
        ctrlsContainer.add(impBtn); 
        ctrlsContainer.add(clefSelect); 
        ctrlsContainer.add(play1Btn); 
        ctrlsContainer.add(play2Btn); 
        ctrlsContainer.add(haltBtn);  
        // sheetImgCtr.setPreferredSize(new Dimension(860, 260));

        mainWraper.add(ctrlsContainer, BorderLayout.NORTH);
        mainWraper.add(sheetImgCtr, BorderLayout.CENTER);

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
        haltBtn.addActionListener(e -> { 
            if (playTrd != null) playTrd.interrupt();
            sheetImgCtr.setPlayhead(-1); 
        });
        play1Btn.addActionListener(e -> play(1));           // Pno
        play2Btn.addActionListener(e -> play(27));          // Gtr
        clefSelect.addActionListener(e -> { 
            System.out.println("Clef set to " + clefSelect.getSelectedItem()); 
            if (sheetImg == null) return;
            notesArr.clear();
            BufferedImage img = MusicSheet.generate(sheetImg, notesArr, clefSelect.getSelectedIndex());
            sheetImgCtr.setImage(img);
        }); 

        // TEST ===============
        try {
            notesArr.clear();
            BufferedImage img = 
                MusicSheet.generate(
                    ImageIO.read(new File(System.getProperty("user.home"), "Documents/staffTEST.jpg")), 
                    notesArr, 1
                );
            sheetImgCtr.setImage(img);
        } catch (IOException x) {}

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