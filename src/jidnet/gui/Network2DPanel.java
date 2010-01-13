package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;
import jidnet.idnet.IdnetManager.DeterminantBits;

/**
 *
 * @author sven
 */
public class Network2DPanel extends JPanel {

    private IdnetManager idnetManager;
    private int mouseX = -1, mouseY = -1;
    private int d_m = 0;
    private int[] order, reverseOrder, values;
    private int[] inBlockX, inBlockY, outBlockX, outBlockY;
    private DeterminantBits detBits;
    private int xBlockSize, yBlockSize, yBlockCount, xBlockCount;

    public Network2DPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }

        });
        order = new int[12];
        reverseOrder = new int[12];
        values = new int[12];
        for (int i = 0; i < 12; i++) {
            order[i] = reverseOrder[i] = i;
            values[i] = 0;
        }
    }

    private int remapBits(int v) {
        //System.out.println(getBitString(v));
        int res = 0;
        for (int i = 0; i < 12; i++)
            res |= ((((v >> i) & 1) ^ values[i]) << order[i]);
        return res;
    }

    private int coordsToIndex(int x, int y) {
        /*return ((((y >> 0) & 1) ^ values[0]) << order[0]) | ((((x >> 0) & 1) ^
        values[1]) << order[1]) | ((((y >> 1) & 1) ^ values[2]) <<
        order[2]) | ((((x >> 1) & 1) ^ values[3]) << order[3]) |
        ((((y >> 2) & 1) ^ values[4]) << order[4]) | ((((x >> 2) & 1) ^
        values[5]) << order[5]) | ((((y >> 3) & 1) ^ values[6]) <<
        order[6]) | ((((x >> 3) & 1) ^ values[7]) << order[7]) |
        ((((y >> 4) & 1) ^ values[8]) << order[8]) | ((((x >> 4) & 1) ^
        values[9]) << order[9]) | ((((y >> 5) & 1) ^ values[10]) <<
        order[10]) | ((((x >> 5) & 1) ^ values[11]) << order[11]);*/
        if (detBits != null)
            return remapBits(inBlockX[x % xBlockSize] | inBlockY[y % yBlockSize] |
                    outBlockX[x / xBlockSize] | outBlockY[y /
                    yBlockSize]);
        else
            return ((y & (1 << 0)) << 0) | ((x & (1 << 0)) << 1) |
                    ((y & (1 << 1)) << 1) | ((x & (1 << 1)) <<
                    2) | ((y & (1 << 2)) << 2) | ((x & (1 << 2)) << 3) | ((y & (1 <<
                    3)) << 3) |
                    ((x & (1 << 3)) << 4) | ((y & (1 << 4)) << 4) |
                    ((x & (1 << 4)) << 5) |
                    ((y & (1 << 5)) << 5) | ((x & (1 << 5)) << 6);
    }

    private Point indexToCoords(int i) {
        /*int y = ((i & (1 << 0)) >> 0) | ((i & (1 << 2)) >> 1) |
        ((i & (1 << 4)) >> 2) | ((i & (1 << 6)) >> 3) |
        ((i & (1 << 8)) >> 4) | ((i & (1 << 10)) >> 5);
        int x = ((i & (1 << 1)) >> 1) | ((i & (1 << 3)) >> 2) |
        ((i & (1 << 5)) >> 3) | ((i & (1 << 7)) >> 4) |
        ((i & (1 << 9)) >> 5) | ((i & (1 << 11)) >> 6);*/
        // TODO : Achtung ab hier dirty-Lösung!
        for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++)
                if (coordsToIndex(x, y) == i)
                    return new Point(x, y);
        return null;
    }

    private void fillOrderedArray(int size, int[] array, int shift) {
        int mask = size - 1;
        /*for (int i = 11; i >= 0; i--)
        if ((size & (1 << i)) != 0)
        mask = (1 << i) - 1;Ǜ*/
        int v = 1;
        array[0] = 0;
        for (int i = 1; i < size; i++) {
            array[i] = v << shift;
            v = Helper.nextPermutation(v);
            if (v > mask)
                v = ((v & mask) << 2) + 3;
        }
    }

    public void sortByDetBits() {
        detBits = idnetManager.getDeterminantBits();
        int orderIndexLow = 0, orderIndexHigh = 11;
        for (int i = 0; i < 12; i++)
            if ((detBits.mask & (1 << i)) == 0) {
                order[orderIndexLow] = i;
                values[orderIndexLow] = 0;
                reverseOrder[i] = orderIndexLow;
                orderIndexLow++;
            } else {
                order[orderIndexHigh] = i;
                reverseOrder[i] = orderIndexHigh;
                if ((detBits.values & (1 << i)) != 0)
                    values[orderIndexHigh] = 1;
                else
                    values[orderIndexHigh] = 0;
                orderIndexHigh--;
            }
        d_m = Helper.hammingWeight(detBits.mask);
        xBlockSize = 1 << (6 - (d_m / 2) - (d_m % 2));
        yBlockSize = 1 << (6 - (d_m / 2));

        xBlockCount = 64 / xBlockSize;
        yBlockCount = 64 / yBlockSize;

        inBlockX = new int[xBlockSize];
        inBlockY = new int[yBlockSize];
        outBlockX = new int[xBlockCount];
        outBlockY = new int[yBlockCount];

        fillOrderedArray(xBlockSize, inBlockX, (6 - (d_m / 2)));
        fillOrderedArray(yBlockSize, inBlockY, 0);
        fillOrderedArray(xBlockCount, outBlockX, (d_m / 2) + (12 - d_m));
        fillOrderedArray(yBlockCount, outBlockY, (12 - d_m));

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int xOffset = 50;
        int yOffset = 50;
        int size = 8;

        g.setColor(Color.GRAY);
        g.drawRect(xOffset - 1, yOffset - 1, 64 * size + 1, 64 * size + 1);

        if (detBits != null) {
            g.setColor(Color.BLUE);
            for (int x = 0; x < 64; x++)
                for (int y = 0; y < 64; y++) {
                    int i = coordsToIndex(x, y);
                    g.setColor(Color.getHSBColor(Helper.hammingWeight(
                            (detBits.mask & i) ^ detBits.values) / (float) d_m, 1.0f, 1.0f));
                    g.fillRect(x * size + xOffset, y * size + yOffset, size, size);
                }
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(xOffset, yOffset, 64 * size, 64 * size);
        }

        g.setColor(Color.BLACK);

        if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + 64 * size && mouseY <
                yOffset + 64 * size) {
            int i = coordsToIndex((mouseX - xOffset) /
                    size, (mouseY - yOffset) / size);
            g.drawString(Helper.getBitString(i), xOffset, yOffset + 64 * size +
                    30);
            if (detBits != null)
                g.drawString("belongs to S_" + Helper.hammingWeight(
                        (detBits.mask & i) ^ detBits.values),
                        xOffset, yOffset + 64 * size + 50);

            Point p = indexToCoords(i);
            g.setColor(Color.GRAY);
            g.fillRect(p.x * size + xOffset, p.y * size + yOffset, size,
                    size);

            int c = ~i & 4095;
            p = indexToCoords(c);
            g.setColor(Color.WHITE);
            g.fillRect(p.x * size + xOffset, p.y * size + yOffset, size,
                    size);

            int m1, m2;
            m1 = 4096 >> 1;
            while (m1 != 0) {
                p = indexToCoords(c ^ m1);
                g.setColor(Color.WHITE);
                g.fillRect(p.x * size + xOffset, p.y * size + yOffset,
                        size,
                        size);
                g.setColor(Color.LIGHT_GRAY);

                m2 = m1 >> 1;
                while (m2 != 0) {
                    p = indexToCoords((c ^ m1) ^ m2);
                    g.fillRect(p.x * size + xOffset, p.y * size +
                            yOffset, size,
                            size);

                    m2 = m2 >> 1;
                }
                m1 = m1 >> 1;
            }
        }

        g.setColor(Color.BLACK);
        for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++) {
                int i = coordsToIndex(x, y);
                if (idnetManager.getIdiotypes()[i].n > 0)
                    g.fillRect(x * size + xOffset + 2, y * size + yOffset +
                            2, size - 4, size - 4);
            }



    }

}
