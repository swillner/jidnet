package jidnet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import jidnet.idnet.Helper;

public class LinkMatrix {

    private static int d = 12;
    private static int m = 2;

    private static Color getColor(int value, int[] values) {
        if (value == 0)
            return Color.BLACK;
        for (int i = 0; i < values.length; i++)
            if (value == values[i])
                return Color.getHSBColor((float) i / (float) (values.length), 1.0f, 1.0f);
        return Color.WHITE;
    }

    private static void calcLinkMatrixRec(long j, long mismatchMask, int dist, int[] b, int mask) {
        if (dist >= d)
            return;
        while (mismatchMask != 0) {
            mismatchMask >>= 1;
            if (mismatchMask == 0)
                break;
            //test++;
            //System.out.println(Helper.getBitString(j ^mismatchMask) + " " + dist);
            b[(int) ((j ^ mismatchMask) & mask)]++;
            if (dist + 1 <= m)
                calcLinkMatrixRec(j ^ mismatchMask, mismatchMask, dist + 1, b, mask);
        }
    }

    public static void main(String[] args) throws Exception {
        for (int d_m = 0; d_m <= 5; d_m++) //int d_m = 3;
        //int d_m = 10;
        {

            int mask = (1 << d_m) - 1;

            int[] values = new int[m + 1];
            int c = 0;
            for (int i = 0; i <= m; i++) {
                c += Helper.binomial(d - d_m, i);
                values[i] = c;
            }

            int[][] b = new int[1 << d_m][1 << d_m];

            for (int i = 0; i < (1 << d_m); i++) {
                long complement = (~(long) i) & ((long) (1 << d) - 1);
                /*for (int n = 0; n < (1 << d); n++)
                if (Helper.hammingWeight(n ^ complement) <= m)
                b[i][n & mask]++;*/
                b[i][(int) (complement & mask)]++;
                calcLinkMatrixRec(complement, 1 << d, 1, b[i], mask);
                //System.out.println(i + " / " + (1 << d_m) + " done");
            }

                System.out.println();
                System.out.println("\\begin{bmatrix}");
                System.out.println("%d_m=" + d_m);
                for (int i = 0; i < (1 << d_m); i++) {
                    for (int j = 0; j < (1 << d_m); j++)
                        System.out.print(b[i][j] + "&");
                    System.out.println("\\\\");
                }
                System.out.println("\\end{bmatrix}");

/*
                String fileName = "d12blockmatrix" + d_m + ".png";
            fileName = "";

            if (fileName.equals("")) {
                System.out.print("      ");
                for (int i = 0; i < (1 << d_m); i++)
                    System.out.print(Helper.printInteger100(i) + " ");
                System.out.println();
                System.out.print("      ");
                for (int i = 0; i < (1 << d_m); i++)
                    System.out.print("----");
                System.out.println();
                for (int i = 0; i < (1 << d_m); i++) {
                    System.out.print(Helper.printInteger100(i) + " | ");
                    for (int j = 0; j < (1 << d_m); j++)
                        System.out.print(Helper.printInteger100(b[i][j]) + " ");
                    System.out.println(" |");
                }
                System.out.println();
            } else {
                int squareSize = 1 << (d - d_m);
                BufferedImage img = new BufferedImage((1 << d_m) * squareSize, (1 << d_m) * squareSize, BufferedImage.TYPE_INT_ARGB);
                Graphics g = img.createGraphics();

                /*
                int v_i = 0;
                for (int i = 0; i < (1 << d_m); i++) {
                    int v_j = 0;
                    for (int j = 0; j < (1 << d_m); j++) {

                        //System.out.println(v_i + " " + Helper.getBitString(v_i, d_m));
                        g.setColor(getColor(b[v_i][v_j], values));
                        g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);

                        if (j > 0) {
                            v_j = Helper.nextPermutation(v_j);
                            if (v_j > mask)
                                v_j = ((v_j & mask) << 2) | 3;
                        } else
                            v_j = 1;
                    }
                    if (i > 0) {
                        v_i = Helper.nextPermutation(v_i);
                        if (v_i > mask)
                            v_i = ((v_i & mask) << 2) | 3;
                    } else
                        v_i = 1;

                }*/
/*
                for (int i = 0; i < (1 << d_m); i++)
                for (int j = 0; j < (1 << d_m); j++) {
                g.setColor(getColor(b[i][j], values));
                g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                }
                ImageIO.write(img, "png", new File(fileName));
            }
*/
        }
    }

}
