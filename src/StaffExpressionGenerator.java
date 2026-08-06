import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class StaffExpressionGenerator {

    private static void getStaffLinePosArr(BufferedImage i, ArrayList<Integer> arr) {
        int w = i.getWidth(), h = i.getHeight();
        for (int y = 0; y < h; y++) {
            int nBlack = 0;
            for (int x = 0; x < w; x++) {
                int rgb = i.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0x000000) nBlack++;
            }
            if (nBlack >  w / 2) arr.add(y);
        }


        
    }
    
    public static BufferedImage getBwImg(BufferedImage i) {
        BufferedImage binImg = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binImg.createGraphics();
        g.drawImage(i, 0, 0, null);
        g.dispose();
        return binImg;
    }
    
    public static void generate(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        System.out.println("\nImage recived: " + w + "x" + h);
        
        ArrayList<Integer> staff = new ArrayList<>();

        BufferedImage bin = getBwImg(img);

        getStaffLinePosArr(bin, staff);

        System.out.println(staff);

        // Graphics2D gr = bin.createGraphics();
        // gr.setColor(Color.BLACK);
        // gr.drawLine(0, 0, 100, 100);
    }

}