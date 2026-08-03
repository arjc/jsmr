import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class main {
    private final JFrame frame;
    private final JButton importButton;
    private final JButton playPianoButton;
    private final JButton playStringsButton;
    private final JButton stopButton;
    private final JComboBox<String> clefCombo;
    private final JComboBox<String> timeSignatureCombo;
    private final MusicPanel musicPanel;
    private BufferedImage currentImage;

    public main() {
        frame = new JFrame("Project Sheet Music Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 720);
        frame.setMinimumSize(new Dimension(900, 650));
        frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout(0, 12));
        JPanel topControls = new JPanel();
        topControls.setLayout(new BoxLayout(topControls, BoxLayout.X_AXIS));
        importButton = new JButton("+ Import");
        playPianoButton = new JButton("Play Piano");
        playStringsButton = new JButton("Play Strings");
        stopButton = new JButton("Stop");
        clefCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "𝄞 Treble Clef", "𝄢 Bass Clef"}));
        timeSignatureCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "TS 4/4", "TS 3/4", "TS 2/4", "TS 6/8", "TS 12/8" }));
        topControls.add(importButton);
        topControls.add(Box.createHorizontalStrut(12));
        topControls.add(clefCombo);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(timeSignatureCombo);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(playPianoButton);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(playStringsButton);
        topControls.add(Box.createHorizontalStrut(10));
        topControls.add(stopButton);
        musicPanel = new MusicPanel();
        musicPanel.setPreferredSize(new Dimension(860, 260));
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(0, 12));
        content.add(topControls, BorderLayout.NORTH);
        content.add(musicPanel, BorderLayout.CENTER);
        center.add(content, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        frame.setContentPane(root);
        importButton.addActionListener(event -> openImage());
        playPianoButton.addActionListener(event -> startPlayback("Grand Piano"));
        playStringsButton.addActionListener(event -> startPlayback("Strings"));
        stopButton.addActionListener(event -> stopPlayback());
        clefCombo.addActionListener(event -> { musicPanel.setClef((String) clefCombo.getSelectedItem()); });
        timeSignatureCombo.addActionListener(event -> { musicPanel.setTimeSignature((String) timeSignatureCombo.getSelectedItem()); });
        musicPanel.setClef((String) clefCombo.getSelectedItem());
        musicPanel.setTimeSignature((String) timeSignatureCombo.getSelectedItem());
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a sheet music image");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try { currentImage = ImageIO.read(file); musicPanel.setImage(currentImage); }
            catch (IOException exception) {}
        }
    }

    private void startPlayback(String instrument) { if (currentImage == null) return; }

    private void stopPlayback() {}
    
    private void show() { frame.setVisible(true); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());} 
            catch (Exception ignored) {}
            new main().show();
        });
    }

    private static final class MusicPanel extends JPanel {
        private BufferedImage image;
        private String clef = "Treble Clef";
        private String timeSignature = "TS 4/4";

        private MusicPanel() {}

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
                double scale = Math.min((double) availableWidth / image.getWidth(), (double) availableHeight / image.getHeight());
                int drawWidth = (int) Math.round(image.getWidth() * scale);
                int drawHeight = (int) Math.round(image.getHeight() * scale);
                int x = (width - drawWidth) / 2;
                int y = (height - drawHeight) / 2;
                graphics.drawImage(image, x, y, drawWidth, drawHeight, null);
            }
        }
    }
}
