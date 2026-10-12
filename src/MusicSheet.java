import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

    
public class MusicSheet {
    // C C# D D# E F F# G G# A A# Cb
    // 0 01 2 03 4 5 06 7 08 9 10 11
    // int[] gClef = {11 - 12, 0, 2, 4, 5, 7, 9, 11, 0 + 12, 2 + 12, 4 + 12, 5 + 12, 7 + 12};
    // private static final int[] F_CLEF = {5, 7, 9, 11, 0, 2, 4, 5, 7, 9, 11, 0 + 12, 2 + 12, 4 + 12, 5 + 12, 7 + 12};
    private static final int[] F_CLEF =
    {2 - 12, 4 - 12, 5 - 12, 7 - 12, 9 - 12, 11 - 12, 0, 2, 4, 5, 7, 9, 
        11, 0 + 12, 2 + 12, 4 + 12, 5 + 12, 7 + 12, 9 + 12, 11 + 12};
    private static final int[] G_CLEF = 
    {11 - 12, 0, 2, 4, 5, 7, 9, 11, 0 + 12, 2 + 12, 4 + 12, 5 + 12, 7 + 12, 9 + 12, 11 + 12};
    private final int[] cNotes;
    private int x, y, h, w, nBars;

    private final List<Integer> barXIdx = new ArrayList<>(), 
                                staffYIdx = new ArrayList<>(), 
                                igX = new ArrayList<>(), 
                                igY = new ArrayList<>();

    private List<Cluster> allClusters = new ArrayList<>(), 
                          headClusters = new ArrayList<>(), 
                          beamClusters = new ArrayList<>(), 
                          stemClusters = new ArrayList<>();

    private MusicSheet(int currClef) {
        this.cNotes = currClef == 0 ? 
            G_CLEF.clone() 
            : F_CLEF.clone();
    }
    
    public static class Cluster {
        private final int x, y, h, w;
        private int nBlack;
        private boolean onLine;
        /*
        All clusters is enclosed in a rectangle have a starting coordinate x and y, 
        The cluster enclosing rectangle is then drawn 
        by extending the height and width h, w respectively
        nBlack is the total black pixels inside the cluster 
        used for claculating density of the cluster
        */
        public Cluster(int x, int y, int w, int h, int nBlack) {
           this.x = x; 
           this.y = y; 
           this.w = w; 
           this.h = h; 
           this.nBlack = nBlack;
        }


        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return w; }
        public int getHeight() { return h; }
        public int getBlackPixelCount() { return nBlack; }
        public boolean isOnLine() { return onLine; }
        private void markOnLine() { onLine = true; }
        // private int getConc() { return this.nBlack / (this.w * this.h); }
    }
    
    // public class Meashure {
    //     private int x, y, w, h; private final List<Cluster> allNoteHeads = new ArrayList<>();
    //     public List<Cluster> getAllNoteHeads() { return new ArrayList<>(allNoteHeads); }
    //     // private int getNumberOfNotes() { return this.allNoteHeads.size(); }
    //     // private ArrayList<int[]> getNoteDiff() {
    //     //     for (Cluster c : this.allNoteHeads) {
    //     //         return new ArrayList<>();
    //     //     }
    //     // }

    // }

    // public class NoteHead {
    //     int noteIdx; // 0 from bottom till 8 for F
    //     private Expression.Note returnNote() {
    //         return new Expression.Note();
    //     }

    // }

    public static BufferedImage getBwImg(BufferedImage i) {
        BufferedImage binImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binImg.createGraphics(); 
        g.drawImage(i, 0, 0, null); 
        g.dispose(); 
        return binImg;
    }
    
    private void getSheetLinesCoords(BufferedImage i) {
        barXIdx.clear();
        staffYIdx.clear();
        igX.clear();
        igY.clear();
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
            int nYPx = 0; 
            for (int x = 0; x < w; x++) 
                if (i.getRGB(x, y) != -1) 
                    nYPx++;
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

    private void getClusters(BufferedImage i) {
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

    

    private void filterOnlyHeads(List<Expression.Note> notesArr){
        int headH = this.staffYIdx.get(3) - this.staffYIdx.get(2);
        double headW = headH * 2;
        this.allClusters.sort(Comparator.comparingInt(n -> n.x));
        this.headClusters.clear();

        for (Cluster c : this.allClusters) 
            if (c.h < headH * 1.3 && 
                c.h > headH * 0.2 && 
                c.w < headW && 
                c.w > headW / 2
            ) 
                this.headClusters.add(c);
                
            for (int i = 1; i < this.headClusters.size();) {
                Cluster c = this.headClusters.get(i - 1), n = this.headClusters.get(i);
                boolean onLineHead = c.x <= n.x + n.w && n.x <= c.x + c.w;
                if (onLineHead && c.y != n.y) {
                    Cluster abvLn = (c.y < n.y) ? c : n;
                    Cluster blwLn = (abvLn == c) ? n : c;
                    abvLn.markOnLine();
                    // abvLn.nBlack = c.w * c.h;
                    this.headClusters.remove(blwLn);
                    if (blwLn == c && i > 1) i--;
                } else i++;
            }
            // for (Cluster c : this.allClusters) 
            //     if (c.nBlack < 0.6 * (c.w * c.h)) this.headClusters.remove(c);

        for (Cluster c : headClusters) {
            System.out.println(c.y);
            int dur = 1;
            if (hasStemAttached(c)) dur = 2;
            if (c.nBlack > 0.5 * (c.w * c.h)) dur = 4;
            if (hasNearbyBeam(c, 10)) dur = 8;
            
            int cNotePosIdx = (!c.isOnLine()) ?
                c.y <= staffYIdx.get(0) ? 9 : // on line 4
                c.y <= staffYIdx.get(1) ? 7 : // on line 3
                c.y <= staffYIdx.get(2) ? 5 : // on line 2
                c.y <= staffYIdx.get(3) ? 3 : // on line 1
                c.y <= staffYIdx.get(4) ? 1 : 0
                :
                c.y <= staffYIdx.get(0) ? 8 :   // Space bw third and 4th 
                c.y <= staffYIdx.get(1) ? 6 :   // Space berween second and 3rd
                c.y <= staffYIdx.get(2) ? 4 :   // Space between first two line
                c.y <= staffYIdx.get(3) ? 2 : 0 // Space bw second and third
            ;

            int note = cNotes[cNotePosIdx];
            int oct = (cNotes[0] != 4) ? 4 : 2;
            notesArr.add(new Expression.Note(note, 0, oct, dur, 100, c.x));
            // System.out.println(c.y + note + oct + (c.onLine ? " onLine" :""));

        }
    }

    private boolean hasStemAttached(Cluster head) {
        for (Cluster stem : this.stemClusters) {
            boolean closeOnLeft = stem.x + stem.w >= head.x - 2 && stem.x <= head.x;
            boolean closeOnRight = stem.x <= head.x + head.w + 2 && stem.x + stem.w >= head.x;
            boolean overlapsVertically = stem.y <= head.y + head.h && head.y <= stem.y + stem.h;
            if ((closeOnLeft || closeOnRight) && overlapsVertically) return true;
        }
        return false;
    }

    private boolean hasNearbyBeam(Cluster head, int maxDistance) {
        for (Cluster beam : this.beamClusters) {
            int distance;
            if (beam.x + beam.w < head.x) {
                distance = head.x - (beam.x + beam.w);
            } else if (head.x + head.w < beam.x) {
                distance = beam.x - (head.x + head.w);
            } else {
                distance = 0;
            }
            if (distance <= maxDistance) return true;
        }
        return false;
    }

    private void filterOnlyBeams(BufferedImage i) {
        int gap = Math.max(1, this.staffYIdx.get(3) - this.staffYIdx.get(2));
        this.beamClusters.clear();
        int minX = 0, maxX = i.getWidth();
        int minY = Math.max(0, this.y), maxY = Math.min(i.getHeight(), this.y + this.h);
        for (int y = minY; y < maxY; y++) {
            if (this.igY.contains(y)) continue;
            int start = -1;
            for (int x = minX; x <= maxX; x++) {
                boolean black = x < maxX && !this.igX.contains(x) && i.getRGB(x, y) != -1;
                if (black && start < 0) start = x;
                if ((!black || x == maxX) && start >= 0) {
                    int end = black && x == maxX ? x : x - 1;
                    if (end - start + 1 >= gap * 2)
                        addBeamCluster(start, y, end - start + 1, 1);
                    start = -1;
                }
            }
        }
    }

    private void addBeamCluster(int x, int y, int w, int h) {
        if (!this.beamClusters.isEmpty()) {
            Cluster previous = this.beamClusters.get(this.beamClusters.size() - 1);
            boolean overlaps = x <= previous.x + previous.w && previous.x <= x + w;
            if (overlaps && y <= previous.y + previous.h + 1) {
                int right = Math.max(previous.x + previous.w, x + w);
                int bottom = Math.max(previous.y + previous.h, y + h);
                this.beamClusters.set(this.beamClusters.size() - 1,
                    new Cluster(Math.min(previous.x, x), Math.min(previous.y, y),
                        right - Math.min(previous.x, x), bottom - Math.min(previous.y, y), 0));
                return;
            }
        }
        this.beamClusters.add(new Cluster(x, y, w, h, w * h));
    }

    private void filterOnlyStems(BufferedImage i) {
        int gap = Math.max(1, this.staffYIdx.get(3) - this.staffYIdx.get(2));
        int minimumHeight = (int) Math.floor(gap * 2.5);
        // Stems can occur before the first detected barline, so do not use the
        // measure region used by note-head clustering as the horizontal bound.
        int minX = 0, maxX = i.getWidth();
        int minY = Math.max(0, this.y), maxY = Math.min(i.getHeight(), this.y + this.h);
        this.stemClusters.clear();

        for (int x = minX; x < maxX; x++) {
            int firstBlackY = -1;
            int lastBlackY = -1;
            int blackPixels = 0;
            for (int y = minY; y < maxY; y++) {
                if (this.igY.contains(y) || this.igX.contains(x) || i.getRGB(x, y) == -1) continue;
                if (firstBlackY < 0) firstBlackY = y;
                lastBlackY = y;
                blackPixels++;
            }

            int height = lastBlackY - firstBlackY + 1;
            if (firstBlackY >= 0 && height > minimumHeight && blackPixels * 2 >= height) {
                addStemCluster(x, firstBlackY, 1, height);
            }
        }
    }

    private void addStemCluster(int x, int y, int w, int h) {
        if (!this.stemClusters.isEmpty()) {
            Cluster previous = this.stemClusters.get(this.stemClusters.size() - 1);
            boolean touches = x <= previous.x + previous.w + 1;
            boolean overlapsVertically = y <= previous.y + previous.h && previous.y <= y + h;
            if (touches && overlapsVertically) {
                int left = Math.min(previous.x, x);
                int top = Math.min(previous.y, y);
                int right = Math.max(previous.x + previous.w, x + w);
                int bottom = Math.max(previous.y + previous.h, y + h);
                this.stemClusters.set(this.stemClusters.size() - 1,
                    new Cluster(left, top, right - left, bottom - top, 0));
                return;
            }
        }
        this.stemClusters.add(new Cluster(x, y, w, h, w * h));
    }

    private boolean isInCluster(BufferedImage i, int x, int y) { 
        return i.getRGB(x, y) != -1 && !this.igY.contains(y) && !this.igX.contains(x); 
    }

    private BufferedImage enboxCluster(BufferedImage i) {
        BufferedImage markedImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = markedImg.createGraphics(); g.drawImage(i, 0, 0, null);
        g.setColor(Color.RED); g.setStroke(new BasicStroke(1));
        // for (Cluster cluster : this.allClusters) g.drawRect(cluster.x, cluster.y, cluster.w - 1, cluster.h - 1);
        for (Cluster cluster : this.stemClusters) g.drawRect(cluster.x, cluster.y, cluster.w - 1, cluster.h - 1);
        g.setColor(Color.GREEN);
        for (Cluster cluster : this.headClusters) g.drawRect(cluster.x, cluster.y, cluster.w - 1, cluster.h - 1);
        g.setColor(Color.BLUE);
        for (Cluster cluster : this.beamClusters) g.drawRect(cluster.x, cluster.y, cluster.w - 1, cluster.h - 1);
        g.dispose(); 
        return markedImg;
    }

    public static BufferedImage generate(BufferedImage img, ArrayList<Expression.Note> notesArr, int currClef) {

        if (img == null || notesArr == null) {
            throw new IllegalArgumentException("Image and note list are required.");
        }
        notesArr.clear();

        int imgW = img.getWidth(), imgH = img.getHeight();
        System.out.println("\nImage recived: " + imgW + "x" + imgH);
        
        BufferedImage iBin = MusicSheet.getBwImg(img); // B/W
        
        MusicSheet sheet = new MusicSheet(currClef);
        // sheet.cNoteOct = (currClef == 0) ? sheet.gOct : sheet.fOct;

        sheet.getSheetLinesCoords(iBin);

        if (sheet.staffYIdx.size() < 5 || sheet.barXIdx.size() < 2) return iBin;

        sheet.nBars = sheet.barXIdx.size();
        sheet.x = sheet.barXIdx.get(0);
        int staffTop = sheet.staffYIdx.get(0), staffBottom = sheet.staffYIdx.get(4);
        int staffSpacing = Math.max(1, (staffBottom - staffTop) / 4), my = staffSpacing * 3;
        sheet.y = Math.max(0, staffTop - my);
        sheet.w = sheet.barXIdx.get(sheet.nBars -1) - sheet.x;
        sheet.h = Math.min(imgH - sheet.y, staffBottom - staffTop + my * 2 + 1);
        sheet.getClusters(iBin);
        sheet.filterOnlyBeams(iBin);
        sheet.filterOnlyStems(iBin);
        sheet.filterOnlyHeads(notesArr);

        
        return sheet.enboxCluster(iBin);
        // return iBin;
    }
    
}
    

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