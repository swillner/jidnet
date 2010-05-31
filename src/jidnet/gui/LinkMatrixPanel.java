/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

public class LinkMatrixPanel extends JPanel {

    private IdnetManager idnetManager;
    // Coordinates of mouse pointer in component
    private int mouseX = -1, mouseY = -1;
    private DeterminantBits detBits;
    private int d_m = 0;
    private int d;
    // Permutation of bits to arrange nodes by det. bit group
    private int[] order;
    private int[][] b;
    private int[] values;
    private final static int xOffset = 160;
    private final static int yOffset = 20; // Offsets of grid
    private final static int xGap = 20;

    public LinkMatrixPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1)
                    return;
                mouseX = e.getX();
                mouseY = e.getY();
                int squareSize = 1 << (12 - (d + d_m) / 2 - (d + d_m) % 2);
                if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + (1 << d_m) * squareSize && mouseY
                        < yOffset + (1 << d_m) * squareSize) {
                    //int i = (mouseY - yOffset - 1) / squareSize;
                    int i = (mouseX - xOffset) / squareSize;
                    for (int j = 0; j < (1 << (d - d_m)); j++) {
                        Application.getIdnetManager().getIdiotypes()[remapBits((j << d_m) | i)].n = e.isControlDown() ? 0 : 1;
                        Application.getIdnetManager().getIdiotypes()[remapBits((j << d_m) | i)].sum_n = e.isControlDown() ? 0 : Application.getIdnetManager().gett();
                    }
                }
                mouseX = -1;
                mouseY = -1;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                mouseY = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }
        });

        change_d(idnetManager.getd());
    }

    public void change_d(int d) {
        this.d = d;
        order = new int[d];

        // Default order: identity
        for (int i = 0; i < d; i++)
            order[i] = i;
        detBits = null;

        repaint();
    }

    /**
     * Arrange bits by order and set variations/accordances to det. bits
     *
     * @param v Bitstring to remap (1 for variation from det. bit, 0 for none)
     * @return Remapped bitstring
     */
    private int remapBits(int v) {
        int res = 0;
        // Reorder
        for (int i = 0; i < d; i++)
            res |= (((v >> i) & 1) << order[i]);
        // Set variations/accordances to det. bits
        return res ^ detBits.values;
    }

    /**
     * Arrange back to default: order = identity
     */
    public void arrangeDefault() {
        for (int i = 0; i < d; i++)
            order[i] = i;
        d_m = 0;
        detBits = null;
    }

    public void arrangeByDetBitGroups() {
        arrangeByDetBitGroups(idnetManager.calcDeterminantBits());
    }

    /**
     *  Generates order-array and blocks to rearrange nodes by determinant bit group (S_0, ...)
     */
    public void arrangeByDetBitGroups(DeterminantBits detBits) {
        if (Helper.hammingWeight(detBits.mask) == 0)
            return;
        this.detBits = detBits;
        int orderIndexLow = 0, orderIndexHigh = d - 1;
        for (int i = 0; i < d; i++)
            if ((detBits.mask & (1 << i)) == 0) {
                order[orderIndexLow] = i;
                orderIndexLow++;
            } else {
                order[orderIndexHigh] = i;
                orderIndexHigh--;
            }

        if (detBits.order != null)
            for (int i = 0; i < d; i++)
                order[i] = detBits.order[i];

        // Set number of determinant bits (d_m) and determine size of blocks, that differ only in not det. bits
        d_m = Helper.hammingWeight(detBits.mask);

        int mask = (1 << d_m) - 1;

        int c = 0;
        for (int i = 0; i <= d; i++)
            if (idnetManager.getLinkWeighting(i) > 0)
                c++;
            else
                break;
        values = new int[c + 1];

        c = 0;
        for (int i = 0; i < values.length; i++) {
            c += Helper.binomial(d - d_m, i);
            values[i] = c;
        }

        b = new int[1 << d_m][1 << d_m];

        for (int i = 0; i < (1 << d_m); i++) {
            long complement = (~(long) i) & ((long) (1 << d) - 1);
            /*for (int n = 0; n < (1 << d); n++)
            if (Helper.hammingWeight(n ^ complement) <= m)
            b[i][n & mask]++;*/
            b[i][(int) (complement & mask)]++;
            calcNeighbourCountRec(complement, 1 << d, 1, b[i], mask);
        }

        repaint();
    }

    private static Color getColor(int value, int[] values) {
        if (value == 0)
            return Color.BLACK;
        for (int i = 0; i < values.length; i++)
            if (value == values[i])
                return Color.getHSBColor((float) i / (float) (values.length), 1.0f, 1.0f);
        return Color.WHITE;
    }

    private void calcNeighbourCountRec(long j, long mismatchMask, int dist, int[] b, int mask) {
        if (dist >= d)
            return;
        while (mismatchMask != 0) {
            mismatchMask >>= 1;
            if (mismatchMask == 0)
                break;
            b[(int) ((j ^ mismatchMask) & mask)]++;
            if (idnetManager.getLinkWeighting(dist + 1) > 0)
                calcNeighbourCountRec(j ^ mismatchMask, mismatchMask, dist + 1, b, mask);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (detBits == null)
            return;

        // Draw grid outline
        g.setColor(Color.GRAY);
        int squareSize = 1 << (12 - (d + d_m) / 2 - (d + d_m) % 2);
        g.drawRect(xOffset - 1, yOffset - 1, (1 << d_m) * squareSize + 1, (1 << d_m) * squareSize + 1);
        g.drawRect((1 << d_m) * squareSize + xGap + xOffset - 1, yOffset - 1, (1 << d_m) * squareSize + 1, (1 << d_m) * squareSize + 1);

        for (int i = 0; i < (1 << d_m); i++)
            for (int j = 0; j < (1 << d_m); j++) {
                g.setColor(getColor(b[i][j], values));
                g.fillRect(xOffset + i * squareSize, yOffset + j * squareSize, squareSize, squareSize);
            }
        float[] sum = new float[1 << d_m];
        for (int i = 0; i < (1 << d_m); i++) {
            sum[i] = 0;
            for (int j = 0; j < (1 << (d - d_m)); j++)
                sum[i] += (float) idnetManager.getIdiotypes()[remapBits((j << d_m) | i)].sum_n;
            //System.out.println(Helper.getBitString(i, 12) + " -> " + Helper.getBitString(k, 12));
            sum[i] = sum[i] / (float) (1 << (d - d_m)) / (float) idnetManager.getN() / (float) idnetManager.gett();
            g.setColor(new Color(1.0f, 1.0f, 1.0f, sum[i]));
            g.fillRect(xOffset, yOffset + i * squareSize + squareSize / 3, squareSize * (1 << d_m), squareSize > 2 ? squareSize / 3 : 1);
            g.fillRect(xOffset + i * squareSize + squareSize / 3, yOffset, squareSize > 2 ? squareSize / 3 : 1, squareSize * (1 << d_m));
        }

        g.setColor(Color.BLACK);
        for (int i = 0; i < (1 << d_m); i++) {
            g.drawString((Math.round(sum[i] * 100) / 100.) + "", 10, yOffset + i * 20);
            float s = 0;
            for (int j = 0; j < (1 << d_m); j++)
                s += b[i][j] * sum[j];
            g.drawString((Math.round(s * 100) / 100.) + "", 60, yOffset + i * 20);

            s = 0;
            for (int j = 0; j < (1 << d_m); j++)
                s += b[i][j] * (sum[j] + (1 - sum[j]) * idnetManager.getp());
            g.drawString((Math.round(s * 100) / 100.) + "", 110, yOffset + i * 20);
        }

        int mask = (1 << d_m) - 1;
        int v_i = 0;
        for (int i = 0; i < (1 << d_m); i++) {
            int v_j = 0;
            for (int j = 0; j < (1 << d_m); j++) {

                //System.out.println(v_i + " " + Helper.getBitString(v_i, d_m));
                g.setColor(getColor(b[v_i][v_j], values));
                g.fillRect((1 << d_m) * squareSize + xGap + xOffset + i * squareSize, yOffset + j * squareSize, squareSize, squareSize);

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

        }

        v_i = 0;
        for (int i = 0; i < (1 << d_m); i++) {
            float s = 0;
            for (int j = 0; j < (1 << (d - d_m)); j++)
                s += (float) idnetManager.getIdiotypes()[remapBits((j << d_m) | v_i)].sum_n;
            //System.out.println(Helper.getBitString(i, 12) + " -> " + Helper.getBitString(k, 12));
            s = s / (float) (1 << (d - d_m)) / (float) idnetManager.getN() / (float) idnetManager.gett();
            g.setColor(new Color(1.0f, 1.0f, 1.0f, s));
            g.fillRect((1 << d_m) * squareSize + xGap + xOffset, yOffset + i * squareSize + squareSize / 3, squareSize * (1 << d_m), squareSize > 2 ? squareSize / 3 : 1);
            g.fillRect((1 << d_m) * squareSize + xGap + xOffset + i * squareSize + squareSize / 3, yOffset, squareSize > 2 ? squareSize / 3 : 1, squareSize * (1 << d_m));

            if (i > 0) {
                v_i = Helper.nextPermutation(v_i);
                if (v_i > mask)
                    v_i = ((v_i & mask) << 2) | 3;
            } else
                v_i = 1;

        }


        if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + (1 << d_m) * squareSize && mouseY
                < yOffset + (1 << d_m) * squareSize) {
            int i = (mouseY - yOffset - 1) / squareSize;
            int j = (mouseX - xOffset) / squareSize;
            g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.6f));
            g.fillRect(xOffset + j * squareSize, yOffset + i * squareSize, squareSize, squareSize);
            g.setColor(Color.BLACK);
            g.drawString("B(i,j)=" + b[i][j], xOffset, 2 * yOffset + (1 << d_m) * squareSize);
            g.drawString("(i,j)=( " + Helper.getBitString(i, d_m) + " , " + Helper.getBitString(j, d_m) + " )",
                    xOffset, 3 * yOffset + (1 << d_m) * squareSize);
        }
    }
}
