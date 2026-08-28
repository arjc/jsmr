import java.awt.*; import java.awt.image.BufferedImage; import java.io.*; import java.util.ArrayList; import javax.imageio.ImageIO;
import javax.swing.*; import javax.swing.filechooser.FileNameExtensionFilter;
public class Main {
    private JFrame fr; private CtrlPanel cPnl; private BufferedImage sheetImg; private Thread playTrd;
    ArrayList<Expression.Note> notesArr = new ArrayList<>();
    public Main() {
        fr = new JFrame("Project Sheet Music Reader"); fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); fr.setSize(670, 400); 
        fr.setMinimumSize(new Dimension(670, 400)); fr.setLocationRelativeTo(null); JPanel root = new JPanel(new BorderLayout(0, 12)), 
        center = new JPanel(), ctrls = new JPanel(); center.setLayout(new BorderLayout(0, 12)); ctrls.setLayout(new BoxLayout(ctrls, BoxLayout.X_AXIS));
        JButton impBtn = new JButton("Import"), play1Btn = new JButton("Play 1"), play2Btn = new JButton("Play 2"), haltBtn = new JButton("Stop");
        JComboBox<String> clefSelect = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "𝄞 Treble Clef", "𝄢 Bass Clef" })), 
         tsSelect = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "TS 4/4", "TS 3/4", "TS 6/4", "TS 6/8" }));
        // | import |    | clef |    | time sig |    | Play 1 |    | Play 2 |    | stop |
        ctrls.add(impBtn); ctrls.add(Box.createHorizontalStrut(12)); ctrls.add(clefSelect); ctrls.add(Box.createHorizontalStrut(10)); ctrls.add(tsSelect); 
        ctrls.add(Box.createHorizontalStrut(10)); ctrls.add(play1Btn); ctrls.add(Box.createHorizontalStrut(10)); ctrls.add(play2Btn); 
        ctrls.add(Box.createHorizontalStrut(10)); ctrls.add(haltBtn); cPnl = new CtrlPanel(); cPnl.setPreferredSize(new Dimension(860, 260));
        JPanel ctx = new JPanel(); ctx.setLayout(new BorderLayout(0, 12)); ctx.add(ctrls, BorderLayout.NORTH);                                 // CTX
        ctx.add(cPnl, BorderLayout.CENTER); center.add(ctx, BorderLayout.CENTER); root.add(center, BorderLayout.CENTER); fr.setContentPane(root);         // LAYOUT
        impBtn.addActionListener(e -> getImgChooser()); haltBtn.addActionListener(e -> haltSheet());                                                       // IMP / STOP
        play1Btn.addActionListener(e -> playSheet(1)); play2Btn.addActionListener(e -> playSheet(27));                     // PLAY 
        clefSelect.addActionListener(e -> { cPnl.setClef((String) clefSelect.getSelectedItem()); }); cPnl.setClef((String) clefSelect.getSelectedItem()); // CLEF
        tsSelect.addActionListener(e -> { cPnl.setTs((String) tsSelect.getSelectedItem()); }); cPnl.setTs((String) tsSelect.getSelectedItem());           // TS
    } private void getImgChooser() {
        JFileChooser f = new JFileChooser(); f.setDialogTitle("Select a sheet music staff image to read..."); 
        f.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes())); int result = f.showOpenDialog(fr);
        if (result == JFileChooser.APPROVE_OPTION) { File file = f.getSelectedFile();
            try { sheetImg = ImageIO.read(file); BufferedImage img = MusicSheet.generate(sheetImg, notesArr); cPnl.setImage(img); }
            catch (IOException exception) { System.out.println("\nPlease select a valid Sheet music image...");} }
    } private void playSheet(int instrument) {
        if (sheetImg == null) { System.out.println("\nPlease import a sheet music using the import button before you play..."); return; } playTrd = new Thread( () -> {
            try { System.out.println("\nPlaying Sheet Music...");
                // notesArr.add(new Expression.Note(1, 0, 4, 4, 0));
                // notesArr.add(new Expression.Note(4, 0, 4, 8, 100));
                Expression.play(notesArr, instrument);
            } catch (Exception e) { System.out.println("\nMusic halted..."); }
        }, "Playback-Thread"); playTrd.start();
    } private void haltSheet() { if (playTrd != null) playTrd.interrupt(); } private void show() { fr.setVisible(true); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {} new Main().show();
    }); } private static class CtrlPanel extends JPanel {
        private BufferedImage i; private String clef = "Treble Clef", ts = "TS 4/4"; private CtrlPanel() {} 
        private void setImage(BufferedImage i) { this.i = i; repaint();} 
        private void setClef(String clef) { this.clef = clef; repaint();} private void setTs(String ts) { this.ts = ts; repaint(); }
        @Override protected void paintComponent(Graphics graphics) { super.paintComponent(graphics); if (i != null) { int w = getWidth(), h = getHeight(), inset = 18, 
            wAvl = w - inset * 2, hAvl = h - inset * 2; double scale = Math.min((double) wAvl / i.getWidth(), (double) hAvl / i.getHeight());
            int drawWidth = (int) Math.round(i.getWidth() * scale), drawHeight = (int) Math.round(i.getHeight() * scale);
            int x = (w - drawWidth) / 2, y = (h - drawHeight) / 2; graphics.drawImage(i, x, y, drawWidth, drawHeight, null);
        }}
    }
}
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