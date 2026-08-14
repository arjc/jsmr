import java.awt.image.BufferedImage;

public class SymbolAnalyser {

    public class Symbol {
        int w, h, fst, snd = 0, trd = 0;
        private Symbol(int w, int h, int fst, int snd, int trd) {
            this.w = w;
            this.h = h;
            this.fst = fst;
            this.snd = snd;
            this.trd = trd;
        }

        private void getSymbol() {

        }

    }

    public static void analyse(BufferedImage i, StaffExpressionGenerator.Cluster cl) {

    }
}
