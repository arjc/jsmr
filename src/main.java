import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
    private JFrame fr;
    private JButton importBtn;
    private JButton playPriBtn;
    private JButton playSecBtn;
    private JButton stopBtn;
    private JComboBox<String> clefCombo;
    private JComboBox<String> timeSignatureCombo;
    private CtrlPanel controlPanel;
    private BufferedImage importedSheetImg;

    public Main() {
        fr = new JFrame("Project Sheet Music Reader");
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setSize(670, 400);
        fr.setMinimumSize(new Dimension(670, 400));
        fr.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout(0, 12));
        JPanel topControls = new JPanel();
        topControls.setLayout(new BoxLayout(topControls, BoxLayout.X_AXIS));
        importBtn = new JButton("+ Import");
        playPriBtn = new JButton("Play Piano");
        playSecBtn = new JButton("Play Guitar");
        stopBtn = new JButton("Stop");
        clefCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "𝄞 Treble Clef", "𝄢 Bass Clef" }));
        timeSignatureCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "TS 4/4", "TS 3/4", "TS 2/4", "TS 6/8", "TS 12/8" }));
        topControls.add(importBtn);
        topControls.add(Box.createHorizontalStrut(12));
        topControls.add(clefCombo);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(timeSignatureCombo);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(playPriBtn);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(playSecBtn);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(stopBtn);
        controlPanel = new CtrlPanel();
        controlPanel.setPreferredSize(new Dimension(860, 260));
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(0, 12));
        content.add(topControls, BorderLayout.NORTH);
        content.add(controlPanel, BorderLayout.CENTER);
        center.add(content, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        fr.setContentPane(root);
        importBtn.addActionListener(e -> openImage());
        playPriBtn.addActionListener(e -> startPlayback(1));
        playSecBtn.addActionListener(e -> startPlayback(27));
        stopBtn.addActionListener(e -> stopPlayback());
        clefCombo.addActionListener(e -> {
            controlPanel.setClef((String) clefCombo.getSelectedItem());
        });
        timeSignatureCombo.addActionListener(e -> {
            controlPanel.setTimeSignature((String) timeSignatureCombo.getSelectedItem());
        });
        controlPanel.setClef((String) clefCombo.getSelectedItem());
        controlPanel.setTimeSignature((String) timeSignatureCombo.getSelectedItem());
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a sheet music staff image to read...");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
        int result = chooser.showOpenDialog(fr);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                importedSheetImg = ImageIO.read(file);
                BufferedImage processed = StaffExpressionGenerator.generate(importedSheetImg);
                controlPanel.setImage(processed);
            } catch (IOException exception) {
                System.out.println("\nPlease select a valid Staff image...");
            }
        }
    }

    private void startPlayback(int instrument) {
        if (importedSheetImg == null) {
            System.out.println("\nPlease import an image of the sheet music using the import button before you play...");
            return;
        }
        new Thread(() -> {
            try {
                System.out.println("\nPlaying Sheet Music...");
                ArrayList<ExpPlayer.Note> meashureArray = new ArrayList<>();
                meashureArray.add(new ExpPlayer.Note(1, 0, 4, 4, 0));
                
                meashureArray.add(new ExpPlayer.Note(0, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(2, 0, 4,16, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(5, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(7, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(9, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(11, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(0, 0, 5, 2, 100));

                meashureArray.add(new ExpPlayer.Note(1, 0, 4, 4, 0));

                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 8, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 8, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 4, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 8, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 8, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 4, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 8, 100));
                meashureArray.add(new ExpPlayer.Note(7, 0, 4, 8, 100));
                meashureArray.add(new ExpPlayer.Note(0, 0, 4, 6, 100));
                meashureArray.add(new ExpPlayer.Note(2, 0, 4, 16, 100));
                meashureArray.add(new ExpPlayer.Note(4, 0, 4, 4, 100));
                ExpPlayer.playMeashure(meashureArray, instrument);

            } catch (Exception ex) { ex.printStackTrace(); }
        }, "Playback-Thread").start();
    }

    private void stopPlayback() {

    }

    private void show() {
        fr.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { 
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new Main().show();
        });
    }

    private static final class CtrlPanel extends JPanel {
        private BufferedImage image;
        private String clef = "Treble Clef";
        private String timeSignature = "TS 4/4";

        private CtrlPanel() {
        }

        private void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        private void setClef(String clef) {
            this.clef = clef;
            repaint();
        }

        private void setTimeSignature(String timeSignature) {
            this.timeSignature = timeSignature;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                int width = getWidth();
                int height = getHeight();
                int inset = 18;
                int availableWidth = width - inset * 2;
                int availableHeight = height - inset * 2;
                double scale = Math.min((double) availableWidth / image.getWidth(),
                        (double) availableHeight / image.getHeight());
                int drawWidth = (int) Math.round(image.getWidth() * scale);
                int drawHeight = (int) Math.round(image.getHeight() * scale);
                int x = (width - drawWidth) / 2;
                int y = (height - drawHeight) / 2;
                graphics.drawImage(image, x, y, drawWidth, drawHeight, null);
            }
        }
    }
}
