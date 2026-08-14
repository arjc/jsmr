import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class StaffExpressionGenerator {

    int[] gClef = {4, 5, 7, 9, 11, 0, 2, 4}, gClefLeg = {7, 9, 11, 0, 2};
    int[] fClef = {7, 9, 11, 0, 2, 4, 5, 7}, fClefLeg = {11, 0, 2, 4, 5};
    static class Cluster {
        int x, y, w, h, nBlack;
        private Cluster(int x, int y, int w, int h, int nBlack) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.nBlack = nBlack;
        }
    }

    class rowCluster {
        
    }

    public static BufferedImage getBwImg(BufferedImage i) {
        BufferedImage binImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binImg.createGraphics();
        g.drawImage(i, 0, 0, null);
        g.dispose();
        return binImg;
    }

    private static void getFiveLinesYCoords(BufferedImage i, ArrayList<Integer> arr) {
        int w = i.getWidth(), h = i.getHeight(), temp = 0;
        for (int y = 0; y < h; y++) {
            int nBlack = 0;
            for (int x = 0; x < w; x++) if ((i.getRGB(x, y) & 0xFFFFFF) == 0x000000) nBlack++;
            if (nBlack >= w / 2) { if (arr.isEmpty() || temp + 1 != y) arr.add(y); temp = y; }
        }
    }
    
    private static void scanForAllXBetween(BufferedImage i, int minY, int maxY, ArrayList<Cluster> gapClustures) {
        int w = i.getWidth();
        for (int y = minY; y <= maxY; y++) {
            System.out.println("Scan for at y =" + y);
            int nBlack = 0, curClusterWidth, startX, endX;
            for (int x = 0; x < w; x++) {
                if ((i.getRGB(x, y) & 0xFFFFFF) == 0x000000) {
                    System.out.println("Cluster found at x = " + x + "y = " + y);
                    startX = x;
                    if ((i.getRGB(x + 1, y) & 0xFFFFFF) != 0x000000) {
                        System.out.println("Cluster finished at x = " + x + "y = " + y);
                        endX = x;
                        gapClustures.add(new Cluster(startX, y, endX - startX, 1, nBlack));
                    } 
                    nBlack++;
                }
            } 
            if (nBlack >= w / 2) System.out.println("Staff Line");
        }
    }

    public static BufferedImage generate(BufferedImage img) {

        ArrayList<ExpPlayer.Note> noteArr = new ArrayList<>();


        int w = img.getWidth(), h = img.getHeight();
        System.out.println("\nImage recived: " + w + "x" + h);

        ArrayList<Integer> stLineYIndices = new ArrayList<>();
        
        BufferedImage iBin = getBwImg(img);
        getFiveLinesYCoords(iBin, stLineYIndices);
        
        if (stLineYIndices.size() < 5) return iBin;
        
        // meanHeadHeight is the mean of differences of the line position which is the height of 1 gap.
        // Height of all note heads = 1 gap height.
        int meanHeadHeight = (stLineYIndices.get(4) - stLineYIndices.get(0)) / 5;
        int barLineHeight = stLineYIndices.get(0) * 5;
        
        System.out.println(stLineYIndices);
        System.out.println(meanHeadHeight);
        System.out.println(barLineHeight);

        ArrayList<Cluster> gapAClusters = new ArrayList<>();
        scanForAllXBetween(iBin, stLineYIndices.get(0), stLineYIndices.get(1), gapAClusters);

        System.out.println("all on gap cluster " + gapAClusters.size());

        // Graphics2D gr = iBin.createGraphics();
        // gr.setColor(Color.RED);
        // gr.setStroke(new BasicStroke(2));

        // int topY = Math.max(0, stLineYIndices.get(0) - meanHeadHeight);
        // int boxHeight = barHeight + meanHeadHeight * 2;

        // for (int x : barLineXIndices) {
        //     int boxWidth = Math.max(2, meanHeadHeight / 2);
        //     int x0 = Math.max(0, x - boxWidth / 2);
        //     gr.drawRect(x0, topY, boxWidth, boxHeight);
        // }

        // gr.dispose();

        return iBin;
    }

}