import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

    
class MusicSheet {

    int[] gClef = {4, 5, 7, 9, 11, 0, 2, 4}, gClefLeg = {7, 9, 11, 0, 2};
    int[] fClef = {7, 9, 11, 0, 2, 4, 5, 7}, fClefLeg = {11, 0, 2, 4, 5};
    int x, y, h, w;
    int clef, ts;
    int nBlack;
    int getConc() { return this.nBlack / (this.w * this.h); }
    
    class Cluster extends MusicSheet{
        // All clusters is enclosed in a rectangle have a starting coordinate x and y, 
        // The cluster enclosing rectangle is then drawn 
        // by extending the height and width h, w respectively
        // nBlack is the total black pixels inside the cluster 
        // used for claculating density of the cluster
        Cluster(int x, int y, int w, int h, int nBlack) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.nBlack = nBlack;
        }
    }


    public static BufferedImage getBwImg(BufferedImage i) {
        BufferedImage binImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binImg.createGraphics();
        g.drawImage(i, 0, 0, null); g.dispose();
        return binImg;
    }
    
    private static void getStaffLinesCoords(BufferedImage i, ArrayList<Integer> arr) {
        // The positional average indeces are appended to an integer arrayList
        // Keep in mind thses are not objects, just plain integers.
        // Specifically looks for staffLines by making sure the following are implemented:
        // 1) width > the total width of the img
        int w = i.getWidth(), h = i.getHeight(), tem = 0;
        int[] pixels = i.getRGB(0, 0, w, h, null, 0, w);
        for (int y = 0; y < h; y++) {
            long nBlack = Arrays.stream(pixels, y * w, (y + 1) * w).filter(rgb -> (rgb & 0xFFFFFF) == 0).count();
            if (nBlack >= w / 2) { if (arr.isEmpty() || tem + 1 != y) arr.add(y); tem = y; }
        }
    }
    
    
    public static BufferedImage generate(BufferedImage img) {

        int imgW = img.getWidth(), imgH = img.getHeight();
        System.out.println("\nImage recived: " + imgW + "x" + imgH);

        ArrayList<Integer> staffLineIndexes = new ArrayList<>();
        
        BufferedImage iBin = MusicSheet.getBwImg(img);
        MusicSheet.getStaffLinesCoords(iBin, staffLineIndexes);
        
        if (staffLineIndexes.size() <= 5) return iBin;
        
        // meanHeadHeight is the mean of differences of the line position which is the height of 1 gap.
        // Height of all note heads = 1 gap height.
        // This is an Optimised version of the mean formula. 
        // Derived by Alwin Rajesh
        int meanHeadHeight = (staffLineIndexes.get(4) - staffLineIndexes.get(0)) / 5;
        int barLineHeight = staffLineIndexes.get(0) * 5;

        // Graphics2D gr = iBin.createGraphics();
        // gr.setColor(Color.RED);
        // gr.setStroke(new BasicStroke(2));
        
        // int topY = Math.max(0, staffLineIndexes.get(0) - meanHeadHeight);
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
    

    
    // === Global pre public methods ===


    // private static ArrayList<Cluster> performBFS(BufferedImage i, int xi, int yi){
        
    //     ArrayList<int[]> q = new ArrayList<>();
    //     ArrayList<int[]> inCluster = new ArrayList<>();
    //     q.add(new int[]{xi, yi});
    //     int x = 0, y = 0;
    //     do {
    //         if ((i.getRGB(x + 1, y) & 0xFFFFFF) == 0) q.add(new int[]{x + 1, y});
    //         if ((i.getRGB(x, y + 1) & 0xFFFFFF) == 0) q.add(new int[]{x, y + 1});
    //         if ((i.getRGB(x - 1, y) & 0xFFFFFF) == 0) q.add(new int[]{x - 1, y});
    //         if ((i.getRGB(x, y - 1) & 0xFFFFFF) == 0) q.add(new int[]{x, y - 1});
    //     } while (!q.isEmpty());
        
    // }

// private static void scanForAllXBetween(BufferedImage i, int minY, int maxY, ArrayList<Cluster> gapClustures) {
//     int w = i.getWidth();
//     for (int y = minY; y <= maxY; y++) {
//         System.out.println("Scan for at y =" + y);
//         int nBlack = 0, curClusterWidth, startX, endX;
//         for (int x = 0; x < w; x++) {
//             if ((i.getRGB(x, y) & 0xFFFFFF) == 0x000000) {
//                 System.out.println("Cluster found at x = " + x + "y = " + y);
//                 startX = x;
//                 if ((i.getRGB(x + 1, y) & 0xFFFFFF) != 0x000000) {
//                     System.out.println("Cluster finished at x = " + x + "y = " + y);
//                     endX = x;
//                     gapClustures.add(new Cluster(startX, y, endX - startX, 1, nBlack));
//                 } 
//                 nBlack++;
//             }
//         } 
//         if (nBlack >= w / 2) System.out.println("Staff Line");
//     }
// }