import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

    
public class MusicSheet {

    int[] gClef = {4, 5, 7, 9, 11, 0, 2, 4}, gClefLeg = {7, 9, 11, 0, 2};
    int[] fClef = {7, 9, 11, 0, 2, 4, 5, 7}, fClefLeg = {11, 0, 2, 4, 5};
    int x, y, h, w, clef, ts, nBars;
    ArrayList<Integer> barXIdx = new ArrayList<>(), staffYIdx = new ArrayList<>(), igX = new ArrayList<>(), igY = new ArrayList<>();
    ArrayList<Cluster> allClusters = new ArrayList<>(), headClusters = new ArrayList<>();
    
    public static class Cluster {
        int x, y, h, w, clef, ts, nBlack, nextClusterX;
        /*
        All clusters is enclosed in a rectangle have a starting coordinate x and y, 
        The cluster enclosing rectangle is then drawn 
        by extending the height and width h, w respectively
        nBlack is the total black pixels inside the cluster 
        used for claculating density of the cluster
        */
       Cluster(int x, int y, int w, int h, int nBlack) {
           this.x = x; this.y = y; this.w = w; this.h = h; this.nBlack = nBlack;
        }
        // private int getConc() { return this.nBlack / (this.w * this.h); }
    }
    
    public class Meashure {
        int x, y, w, h; ArrayList<Cluster> allNoteHeads = new ArrayList<>();
        private int getNumberOfNotes() { return this.allNoteHeads.size(); }
        // private ArrayList<int[]> getNoteDiff() {
        //     for (Cluster c : this.allNoteHeads) {
        //         return new ArrayList<>();
        //     }
        // }

    }

    public static BufferedImage getBwImg(BufferedImage i) {
        BufferedImage binImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binImg.createGraphics(); g.drawImage(i, 0, 0, null); g.dispose(); return binImg;
    }
    
    private void getSheetLinesCoords(BufferedImage i) {
        int w = i.getWidth(), h = i.getHeight(), temp = 0;
        /*
            The positional first incance of pixel column or row of 
            the staff lines (and bar lines) index are appended to an integer arrayList
            Keep in mind thses are not objects, just plain integers
            representing the row/ column index of the image buffer.
            Specifically looks for staffLines by making sure the following are implemented:
            (i) width > the total width of the img
            (ii) the line is continous
            Same for barlines exept the barLine is exactly total y height of the staff.
            *-*-*-*-*
            Array can have a max of 5 staff line indexes.
            Makes sure that only the first instance of of a staff line is added to this array. 
            I made it this way so that the positional indexes can be used to get the postion of 
            the staff lines or barlines to making it possible for range calulation like this note is between
            staffArr[1] and staffArr[0], with treble clef that note should be a E4 note...
            *-*-*-*-*
            igX and igY are all the pixels occupied by the line thickenss of a barline and staffline rspectively.
            These rows and columns are to be avoided by the cluster identifier algo because BFS can go ahead and 
            give out humungusly huge cluster widths- as it recognises the staffLine as a whole cluster.
            This can also be a problem when there are notes on the staff lines and leger lines, 
            read the comment attached to the cluster scaning method for more info...
        */
        for (int y = 0; y < h; y++) {
            int nYPx = 0; for (int x = 0; x < w; x++) if (i.getRGB(x, y) != -1) nYPx++;
            if (nYPx >= w * 0.3) { this.igY.add(y); if (y != temp + 1) this.staffYIdx.add(y); temp = y; }
        } 
        temp = 0;
        // this.staffYIdx.add(38); this.staffYIdx.add(49); this.staffYIdx.add(61); this.staffYIdx.add(73); this.staffYIdx.add(85);
        if (this.staffYIdx.size() < 5) System.out.println("5 StaffLines not found");
        else {
            System.out.println("\nStaff Lines: " + this.staffYIdx + " for " + this.igY);
            for (int x = 0; x < w; x++) {
                Boolean isContBarLine = true;
                for (int y = this.staffYIdx.get(0); y <= this.staffYIdx.get(4); y++) 
                    if (i.getRGB(x, y) == -1) { isContBarLine = false; break; }
                if (isContBarLine) { this.igX.add(x); if (x != temp + 1) this.barXIdx.add(x); temp = x; }
            }
            System.out.println("Bar Lines: " + this.barXIdx + " from " + this.igX);
        } 
    }

    public void getClusters(BufferedImage i) {
        int imgW = i.getWidth(), imgH = i.getHeight();
        int minX = Math.max(0, this.x), minY = Math.max(0, this.y);
        int maxX = Math.min(imgW, this.x + this.w), maxY = Math.min(imgH, this.y + this.h);
        boolean[][] visited = new boolean[imgH][imgW]; this.allClusters.clear();

        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (visited[y][x] || !isInCluster(i, x, y)) continue; 
                ArrayList<int[]> q = new ArrayList<>(); q.add(new int[]{x, y}); visited[y][x] = true;
                int head = 0, minClrX = x, maxClrX = x, minClrY = y, maxClrY = y;
                while (head < q.size()) {
                    int[] point = q.get(head++);
                    int px = point[0], py = point[1];
                    minClrX = Math.min(minClrX, px); maxClrX = Math.max(maxClrX, px); 
                    minClrY = Math.min(minClrY, py); maxClrY = Math.max(maxClrY, py);
                    int[][] nearbyPx = { {px - 1, py}, {px + 1, py}, {px, py - 1}, {px, py + 1} };
                    for (int[] pt : nearbyPx) {
                        int nx = pt[0], ny = pt[1];
                        if (nx >= minX && nx < maxX && ny >= minY && ny < maxY && !visited[ny][nx] && isInCluster(i, nx, ny)) 
                            { visited[ny][nx] = true; q.add(new int[]{nx, ny}); }
                    }
                }
                this.allClusters.add(new Cluster(minClrX, minClrY, maxClrX - minClrX + 1, maxClrY - minClrY + 1, q.size()));
            }
        }
    }

    public void filterHeads(ArrayList<Expression.Note> notesArr){
        int headH = this.staffYIdx.get(3) - this.staffYIdx.get(2);
        double headW = headH * 1.5;
        this.allClusters.sort(Comparator.comparingInt(n -> n.x));
        for (Cluster c : this.allClusters) if (c.h < headH * 1.3 && c.h > headH * 0.3 && c.w < headW && c.w > headW / 2) this.headClusters.add(c);
        for (int i = 1; i < this.headClusters.size() - 1; i++) {
            if (this.headClusters.get(i - 1).x == this.headClusters.get(i).x) {
                this.headClusters.remove(i);
                System.out.println(i);
            } else {
                this.headClusters.get(i - 1).nextClusterX = this.headClusters.get(i).x;
            }
        }
        for (Cluster c : this.headClusters) {
            int dur = (c.nextClusterX > 40) ? 4 : 2; 
            int note = 5;
            notesArr.add(new Expression.Note(note, 0, 3, dur, 100));

        }
    }

    private boolean isInCluster(BufferedImage i, int x, int y) { return i.getRGB(x, y) != -1 && !this.igY.contains(y) && !this.igX.contains(x); }

    private BufferedImage enboxCluster(BufferedImage i) {
        BufferedImage markedImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = markedImg.createGraphics(); g.drawImage(i, 0, 0, null);
        g.setColor(Color.RED); g.setStroke(new BasicStroke(1));
        // for (Cluster cluster : this.allClusters) g.drawRect(cluster.x, cluster.y, cluster.w - 1, cluster.h - 1);
        g.setColor(Color.GREEN);
        for (Cluster cluster : this.headClusters) g.drawRect(cluster.x, cluster.y, cluster.w - 1, cluster.h - 1);
        g.dispose(); return markedImg;
    }

    public static BufferedImage generate(BufferedImage img, ArrayList<Expression.Note> notesArr) {

        int imgW = img.getWidth(), imgH = img.getHeight();
        System.out.println("\nImage recived: " + imgW + "x" + imgH);
        
        BufferedImage iBin = MusicSheet.getBwImg(img); // BW of img
        
        MusicSheet sheet = new MusicSheet();

        sheet.getSheetLinesCoords(iBin);

        if (sheet.staffYIdx.size() < 5 || sheet.barXIdx.size() < 2) return iBin;

        sheet.nBars = sheet.barXIdx.size();
        sheet.x = sheet.barXIdx.get(0);
        int staffTop = sheet.staffYIdx.get(0), staffBottom = sheet.staffYIdx.get(4);
        int staffSpacing = Math.max(1, (staffBottom - staffTop) / 4), YMargin = staffSpacing * 3;
        sheet.y = Math.max(0, staffTop - YMargin);
        sheet.w = sheet.barXIdx.get(sheet.nBars -1) - sheet.x;
        sheet.h = Math.min(imgH - sheet.y, staffBottom - staffTop + YMargin * 2 + 1);
        sheet.getClusters(iBin);
        sheet.filterHeads(notesArr);

        /*
            meanHeadHeight is the mean of differences of the line position which is the height of 1 gap.
            Height of all note heads = 1 gap height.
            This is an Optimised version of the mean formula. 
            Derived by Alwin Rajesh (https://github.com/aalwinrajesh001-a11y)
        */        
        // int meanHeadHeight = (staffXIdx.get(4) - staffXIdx.get(0)) / 5;
        // int barLineHeight = staffXIdx.get(0) - staffXIdx.get(4);

        // Graphics2D gr = iBin.createGraphics();
        // gr.setColor(Color.RED);
        // gr.setStroke(new BasicStroke(2));
        
        // int topY = Math.max(0, staffXIdx.get(0) - meanHeadHeight);
        // int boxHeight = barHeight + meanHeadHeight * 2;

        // for (int x : barLineXIndices) {
        //     int boxWidth = Math.max(2, meanHeadHeight / 2);
        //     int x0 = Math.max(0, x - boxWidth / 2);
        //     gr.drawRect(x0, topY, boxWidth, boxHeight);
        // }

        // gr.dispose();
        
        return sheet.enboxCluster(iBin);
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