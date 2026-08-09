import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class StaffExpressionGenerator {

    public static BufferedImage getBwImg(BufferedImage i) {
        BufferedImage binImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binImg.createGraphics();
        g.drawImage(i, 0, 0, null);
        g.dispose();
        return binImg;
    }

    private static void getStLines(BufferedImage i, ArrayList<Integer> arr) {
        int w = i.getWidth(), h = i.getHeight(), temp = 0;
        for (int y = 0; y < h; y++) {
            int nBlack = 0;
            for (int x = 0; x < w; x++) if ((i.getRGB(x, y) & 0xFFFFFF) == 0x000000) nBlack++;
            if (nBlack >= w / 2) {
                if (arr.isEmpty() || temp + 1 != y) arr.add(y);
                temp = y;
            }
        }
    }

    private static void getBarLines(BufferedImage i, ArrayList<Integer> arr, int bh) {
        int w = i.getWidth(), h = i.getHeight(), temp = 0;
        for (int x = 0; x < w; x++) {
            int nBlack = 0;
            for (int y = 0; y < h; y++) if ((i.getRGB(x, y) & 0xffffff) == 0x000000) nBlack++;
            // if (nBlack >= bh && ) {
            //     if (arr.isEmpty() || temp + 1 != x) arr.add(x);
            //     temp = x;
            // }
        }

    }

    public static BufferedImage generate(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        System.out.println("\nImage recived: " + w + "x" + h);

        ArrayList<Integer> stfLineIndices = new ArrayList<>();
        ArrayList<Integer> barLineIndices = new ArrayList<>();
        BufferedImage iBin = getBwImg(img);
        getStLines(iBin, stfLineIndices);

        if (stfLineIndices.size() < 5) return iBin;

        // Head height is the mean of differences of the line position which is the
        // height of all note heads.
        int meanHeadHeight = (stfLineIndices.get(4) - stfLineIndices.get(0)) / 5;
        int barHeight = stfLineIndices.get(4) - stfLineIndices.get(0);

        getBarLines(iBin, barLineIndices, barHeight);

        System.out.println(stfLineIndices);
        System.out.println(meanHeadHeight);
        System.out.println(barLineIndices);

        Graphics2D gr = iBin.createGraphics();
        gr.setColor(Color.RED);
        gr.setStroke(new BasicStroke(2));

        int topY = Math.max(0, stfLineIndices.get(0) - meanHeadHeight);
        int boxHeight = barHeight + meanHeadHeight * 2;

        for (int x : barLineIndices) {
            int boxWidth = Math.max(2, meanHeadHeight / 2);
            int x0 = Math.max(0, x - boxWidth / 2);
            gr.drawRect(x0, topY, boxWidth, boxHeight);
        }

        gr.dispose();

        return iBin;
    }

}