package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;
import jidnet.idnet.DeterminantBits;

/**
 * Swing component, that shows a 2D grid of idnetManager's nodes and their occupation.
 * Grid can be arranged by det. bit groups
 *
 * @author Sven Willner
 */
public class Network2DPanel extends JPanel {

    private IdnetManager idnetManager;
    // Coordinates of mouse pointer in component
    private int mouseX = -1, mouseY = -1;
    private DeterminantBits detBits;
    private int d_m = 0;
    private int d;
    // Permutation of bits to arrange nodes by det. bit group
    private int[] order;
    // Determinated/undet. bits of rows and cols in grid (arranged in blocks, whose nodes only differ in undet. bits)
    private int[] undetRow, undetCol, detRow, detCol;
    private int undetBlockWidth, undetBlockHeight, undetBlockColCount, undetBlockRowCount;
    private int squareSize = 10; // Width/height of grid's squares
    private int xOffset = 20;
    private int yOffset = 20; // Offsets of grid
    private int last_i = -1;
    private boolean showNeighbourMeans = false;

    public Network2DPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + (1 << (d / 2)) * squareSize && mouseY
                        < yOffset + (1 << (d / 2 + (d % 2))) * squareSize) {
                    int i = coordsToNode((mouseX - xOffset)
                            / squareSize, (mouseY - yOffset - 2) / squareSize);
                    if (e.isControlDown()) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                new StringSelection(Helper.getBitString(i, d)), null);
                    } else {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                new StringSelection(Application.getClipboardContents()
                                + "\n" + Helper.getBitString(i, d)), null);
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON2 && e.getButton() != MouseEvent.BUTTON3) {
                    return;

                }
                mouseX = e.getX();
                mouseY = e.getY();
                if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + (1 << (d / 2)) * squareSize && mouseY
                        < yOffset + (1 << (d / 2 + (d % 2))) * squareSize) {
                    int i = coordsToNode((mouseX - xOffset)
                            / squareSize, (mouseY - yOffset - 2) / squareSize);
                    if (detBits != null) {
                        int blockMask = detBits.mask & i;
                        int k = Helper.hammingWeight(blockMask ^ detBits.values);
                        for (int j = 0; j < (1 << Application.getIdnetManager().getd()); j++) {
                            if ((((j & detBits.mask) == blockMask) && e.getButton() == MouseEvent.BUTTON3)
                                    || ((Helper.hammingWeight((detBits.mask & j) ^ detBits.values) == k) && e.getButton() == MouseEvent.BUTTON2)) {
                                if (e.isControlDown()) {
                                    Application.getIdnetManager().getIdiotypes()[j].n = 0;
                                } else {
                                    Application.getIdnetManager().getIdiotypes()[j].n = 1;
                                }

                            }

                        }
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
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
                if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + (1 << (d / 2)) * squareSize && mouseY
                        < yOffset + (1 << (d / 2 + (d % 2))) * squareSize) {
                    int i = coordsToNode((mouseX - xOffset)
                            / squareSize, (mouseY - yOffset - 2) / squareSize);
                    if (i != last_i) {
                        if (e.isControlDown()) {
                            Application.getIdnetManager().getIdiotypes()[i].n = 0;

                        } else {
                            Application.getIdnetManager().getIdiotypes()[i].n = 1;

                        }
                        last_i = i;
                    }
                }
            }

            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }
        });

        change_d(idnetManager.getd());
    }

    public void setOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public void setShowNeighbourMeans(boolean showNeighbourMeans) {
        this.showNeighbourMeans = showNeighbourMeans;
        repaint();
    }

    public final void change_d(int d) {
        this.d = d;
        order = new int[d];

        // Default order: identity
        for (int i = 0; i < d; i++) {
            order[i] = i;

        }
        detBits = null;

        repaint();
    }

    public void setSquareSize(int size) {
        this.squareSize = size;
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
        for (int i = 0; i < d; i++) {
            res |= (((v >> i) & 1) << order[i]);
            // Set variations/accordances to det. bits

        }
        return res ^ detBits.values;
    }

    /**
     * Maps coordinates in grid (by position in grid, not screen pixels) to node
     *
     * @param x x position in grid (between 0 and 2^(d/2)-1)
     * @param y y position in grid (between 0 and 2^(d/2)-1)
     * @return Node at given position
     */
    private int coordsToNode(int x, int y) {
        if (detBits != null) {
            return remapBits(undetRow[x % undetBlockWidth] | undetCol[y % undetBlockHeight]
                    | detRow[x / undetBlockWidth] | detCol[y
                    / undetBlockHeight]);

        } else {
            return (1 << (d / 2)) * y + x;
            /*            return ((y & (1 << 0)) << 0) | ((x & (1 << 0)) << 1)
            | ((y & (1 << 1)) << 1) | ((x & (1 << 1))
            << 2) | ((y & (1 << 2)) << 2) | ((x & (1 << 2)) << 3) | ((y & (1
            << 3)) << 3)
            | ((x & (1 << 3)) << 4) | ((y & (1 << 4)) << 4)
            | ((x & (1 << 4)) << 5)
            | ((y & (1 << 5)) << 5) | ((x & (1 << 5)) << 6);*/


        }
    }

    /**
     * Fills array masked by size-1 with shifted bitstrings
     * (first array element 0, followed by all bitstrings with 1 bit, those with 2, ...)
     * 
     * @param size Size of array (must be power of 2 => sum of bin. coeff. = 2 ^ n)
     * @param array Array to be filled
     * @param shift Shift to be applied to every array element
     */
    private void fillOrderedArray(int size, int[] array, int shift) {
        int mask = size - 1;
        int v = 1;
        array[0] = 0;
        for (int i = 1; i < size; i++) {
            array[i] = v << shift;
            // Get next Permutation of bits in v
            v = Helper.nextPermutation(v);
            if (v > mask) // All permutations in mask done, shift by 2
            // (highest order bit vanishes by masking, next is 0),
            // set least significant bits => number of set bits in v inreased by 1
            {
                v = ((v & mask) << 2) | 3;

            }
        }
    }

    /**
     * Arrange back to default: order = identity
     */
    public void arrangeDefault() {
        for (int i = 0; i < d; i++) {
            order[i] = i;

        }
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
        if (Helper.hammingWeight(detBits.mask) == 0) {
            arrangeDefault();
            return;
        }

        this.detBits = detBits;
        int orderIndexLow = 0, orderIndexHigh = d - 1;
        for (int i = 0; i < d; i++) {
            if ((detBits.mask & (1 << i)) == 0) {
                order[orderIndexLow] = i;
                orderIndexLow++;
            } else {
                order[orderIndexHigh] = i;
                orderIndexHigh--;
            }


        }
        if (detBits.order != null) {
            for (int i = 0; i < d; i++) {
                order[d - 1 - i] = detBits.order[i];

                // Set number of determinant bits (d_m) and determine size of blocks, that differ only in not det. bits

            }

        }
        d_m = Helper.hammingWeight(detBits.mask);
        undetBlockWidth = 1 << (d / 2 + (d % 2) - (d_m / 2) - (d_m % 2));
        undetBlockHeight = 1 << (d / 2 - (d_m / 2));

        // Determine number of undet. bit blocks, grid size is (1 << (d/2))x(1 << (d/2+(d_m % 2)))
        undetBlockRowCount = (1 << (d / 2)) / undetBlockWidth;
        undetBlockColCount = (1 << (d / 2 + (d % 2))) / undetBlockHeight;

        undetRow = new int[undetBlockWidth];
        undetCol = new int[undetBlockHeight];
        detRow = new int[undetBlockRowCount];
        detCol = new int[undetBlockColCount];

        // Fill column and row arrays
        fillOrderedArray(undetBlockHeight, undetCol, 0); // least significant bits
        fillOrderedArray(undetBlockWidth, undetRow, (d / 2 - (d_m / 2))); // shifted by log2(undetBlockHeight)
        fillOrderedArray(undetBlockColCount, detCol, (d - d_m)); // shifted some more by log2(undetBlockWidth)
        fillOrderedArray(undetBlockRowCount, detRow, (d_m / 2) + (d + (d % 2) - d_m)); // shifted some more by log2(undetBlockColCount) => most significant bits

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw grid outline
        g.setColor(Color.GRAY);
        g.drawRect(xOffset - 1, yOffset - 1, (1 << (d / 2)) * squareSize + 1, (1 << (d / 2 + (d % 2))) * squareSize + 1);
        g.drawRect((1 << (d / 2)) * squareSize + 3 * xOffset - 1, yOffset - 1, (1 << (d / 2)) * squareSize + 1, (1 << (d / 2) + (d % 2)) * squareSize + 1);

        if (detBits != null) {
            // Arranged by determinant bits => Draw groups
            g.setColor(Color.BLUE);
            for (int x = 0; x < (1 << (d / 2)); x++) {
                for (int y = 0; y < (1 << (d / 2 + (d % 2))); y++) {
                    int i = coordsToNode(x, y);
                    g.setColor(Color.getHSBColor(Helper.hammingWeight(
                            (detBits.mask & i) ^ detBits.values) / (float) (d_m + 1), 1.0f, 1.0f));

                    g.fillRect(x * squareSize + xOffset, y * squareSize + yOffset, squareSize, squareSize);
                }


            }
        } else {
            // Not arranged => Draw grid's background monochrome
            g.setColor(Color.CYAN);
            g.fillRect(xOffset, yOffset, (1 << (d / 2)) * squareSize, (1 << (d / 2 + (d % 2))) * squareSize);
        }

        for (int x = 0; x < (1 << (d / 2)); x++) {
            for (int y = 0; y < (1 << (d / 2 + (d % 2))); y++) {
                int i = coordsToNode(x, y);
                if (showNeighbourMeans) {
                    float mean = (float) idnetManager.getIdiotypes()[i].sum_n_d / (float) idnetManager.gett();
                    if (mean < idnetManager.gett_l()) {
                        g.setColor(Color.getHSBColor(0.33f, (1.0f - mean / (float) idnetManager.gett_l()) * 0.8f, 1.0f));
                    } else if (mean > idnetManager.gett_u()) {
                        g.setColor(Color.getHSBColor(0.0f, 1.0f, (float) ((mean - idnetManager.gett_u()) / 79) * 0.8f + 0.2f));
                    } else {
                        g.setColor(Color.getHSBColor(0.0f, 0.0f, 1.0f - (float) ((mean - idnetManager.gett_l()) / (float) (idnetManager.gett_u() - idnetManager.gett_l()))));
                    }
                } else {
                    g.setColor(Color.getHSBColor(0.0f, 0.0f, 1.0f - (float) idnetManager.getIdiotypes()[i].sum_n / (float) idnetManager.getN() / (float) idnetManager.gett()));
                }
                g.fillRect(x * squareSize + 3 * xOffset + (1 << (d / 2)) * squareSize, y * squareSize + yOffset, squareSize, squareSize);
            }


        }
        if (detBits != null) {
            g.setColor(Color.RED);
            int x = (1 << (d / 2)) * squareSize + 3 * xOffset;
            int n_x = (d_m / 2) + (d_m % 2) - (d % 2);
            g.drawLine(x, yOffset, x, yOffset + (1 << (d / 2 + (d % 2))) * squareSize);
            g.drawLine(x - 1, yOffset, x - 1, yOffset + (1 << (d / 2 + (d % 2))) * squareSize);
            for (int k = 0; k <= n_x; k++) {
                x += squareSize * undetBlockWidth * Helper.binomial(n_x, k);
                g.drawLine(x, yOffset, x, yOffset + (1 << (d / 2 + (d % 2))) * squareSize);
                g.drawLine(x - 1, yOffset, x - 1, yOffset + (1 << (d / 2 + (d % 2))) * squareSize);
            }
            int y = yOffset;
            int n_y = (d_m / 2) + (d % 2);
            g.drawLine((1 << (d / 2)) * squareSize + 3 * xOffset, y, 2 * (1 << (d / 2)) * squareSize + 3 * xOffset, y);
            g.drawLine((1 << (d / 2)) * squareSize + 3 * xOffset, y - 1, 2 * (1 << (d / 2)) * squareSize + 3 * xOffset, y - 1);
            for (int k = 0; k <= n_y; k++) {
                y += squareSize * undetBlockHeight * Helper.binomial(n_y, k);
                g.drawLine((1 << (d / 2)) * squareSize + 3 * xOffset, y, 2 * (1 << (d / 2)) * squareSize + 3 * xOffset, y);
                g.drawLine((1 << (d / 2)) * squareSize + 3 * xOffset, y - 1, 2 * (1 << (d / 2)) * squareSize + 3 * xOffset, y - 1);
            }
        }

        if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + (1 << (d / 2)) * squareSize && mouseY
                < yOffset + (1 << (d / 2 + (d % 2))) * squareSize) {
            // If mouse pointer in grid
            g.setColor(Color.BLACK);

            // Index of node below mouse pointer
            int i = coordsToNode((mouseX - xOffset)
                    / squareSize, (mouseY - yOffset - 2) / squareSize);

            g.drawString(Helper.getBitString(i, d), xOffset, yOffset + (1 << (d / 2 + (d % 2))) * squareSize + 30);
            if (detBits != null) {
                g.drawString("belongs to S_" + Helper.hammingWeight(
                        (detBits.mask & i) ^ detBits.values),
                        xOffset, yOffset + (1 << (d / 2 + (d % 2))) * squareSize + 50);

                // Draw neighbours, brigthness determined by link weighting

            }
            int c = ~i & ((1 << d) - 1);
            for (int x = 0; x < (1 << (d / 2)); x++) {
                for (int y = 0; y < (1 << (d / 2 + (d % 2))); y++) {
                    int v = coordsToNode(x, y);
                    int diff = Helper.hammingWeight(v ^ c);
                    if (idnetManager.getLinkWeighting(diff) > 0) {
                        if (detBits != null) {
                            g.setColor(Color.getHSBColor(Helper.hammingWeight((detBits.mask & v) ^ detBits.values)
                                    / (float) (d_m + 1), 1f - (float) idnetManager.getLinkWeighting(diff), 1f));

                        } else {
                            g.setColor(Color.getHSBColor(0f, 1f - (float) idnetManager.getLinkWeighting(diff), 1f));

                        }
                        g.fillRect(x * squareSize + xOffset, y * squareSize + yOffset, squareSize, squareSize);
                    }
                }

                // Mark node below mouse pointer

            }
            g.setColor(Color.WHITE);
            g.fillRect(((mouseX - xOffset) / squareSize) * squareSize + xOffset, ((mouseY - yOffset - 2) / squareSize)
                    * squareSize + yOffset,
                    squareSize, squareSize);

        }

        boolean[] check = new boolean[1 << d];

        // Draw dots for nodes' occupations
        g.setColor(Color.BLACK);
        for (int x = 0; x < (1 << (d / 2)); x++) {
            for (int y = 0; y < (1 << (d / 2 + (d % 2))); y++) {
                int i = coordsToNode(x, y);
                if (check[i]) {
                    System.err.println("ERROR: Node " + i + " painted twice");

                } else {
                    check[i] = true;

                }
                if (idnetManager.getIdiotypes()[i].n > 0) {
                    int size = (idnetManager.getIdiotypes()[i].n * (squareSize - 2)) / idnetManager.getN();
                    g.fillRect(x * squareSize + xOffset + (squareSize - size) / 2, y * squareSize + yOffset
                            + (squareSize - size) / 2, size, size);
                }
            }



        }
    }
}
