package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
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
    // Permutation of bits to arrange nodes by det. bit group
    private int[] order;
    // Determinated/undet. bits of rows and cols in grid (arranged in blocks, whose nodes only differ in undet. bits)
    private int[] undetRow, undetCol, detRow, detCol;
    private int undetBlockWidth, undetBlockHeight, undetBlockColCount, undetBlockRowCount;
    public final static int DRAW_CURRENT = 0;
    public final static int DRAW_MEAN_OCCUPATIONS = 1;
    private int drawType = DRAW_CURRENT;

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

        // Default order: identity
        for (int i = 0; i < 12; i++)
            order[i] = i;
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
        for (int i = 0; i < 12; i++)
            // res |= ((((v >> i) & 1) ^ values[i]) << order[i]);
            res |= (((v >> i) & 1) << order[i]);
        // Set variations/accordances to det. bits
        return res ^ detBits.values;
    }

    /**
     * Maps coordinates in grid (by position in grid, not screen pixels) to node
     *
     * @param x x position in grid (between 0 and 63)
     * @param y y position in grid (between 0 and 63)
     * @return Node at given position
     */
    private int coordsToNode(int x, int y) {
        if (detBits != null)
            return remapBits(undetRow[x % undetBlockWidth] | undetCol[y % undetBlockHeight] |
                    detRow[x / undetBlockWidth] | detCol[y /
                    undetBlockHeight]);
        else
            return ((y & (1 << 0)) << 0) | ((x & (1 << 0)) << 1) |
                    ((y & (1 << 1)) << 1) | ((x & (1 << 1)) <<
                    2) | ((y & (1 << 2)) << 2) | ((x & (1 << 2)) << 3) | ((y & (1 <<
                    3)) << 3) |
                    ((x & (1 << 3)) << 4) | ((y & (1 << 4)) << 4) |
                    ((x & (1 << 4)) << 5) |
                    ((y & (1 << 5)) << 5) | ((x & (1 << 5)) << 6);
// for "mytest"
        //values = 100101011101
        // most sign to less : 8 -1 -10 0 3 -9 -7 11 2 4 -5 6
        // most sign to less : 8 -1 -10 3 0 -9 -7 11 2 4 -5 6
/*            return Integer.parseInt("100101011101",2) ^(((y & (1 << 0)) << 6) | ((x & (1 << 0)) << 5) |
                    ((y & (1 << 1)) << (4-1)) | ((x & (1 << 1)) <<
                    (2-1)) | ((y & (1 << 2)) << (11-2)) | ((x & (1 << 2)) << (9-2)) | ((y & (1 <<
                    3)) << (7-3)) |
                            ((x & (1 << 3)) << (0-3)) | ((y & (1 << 4)) << (3-4)) |
                    ((x & (1 << 4)) << (10-4)) |
                    ((y & (1 << 5)) << (1-5)) | ((x & (1 << 5)) << (8-5)));*/

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
        /*for (int i = 11; i >= 0; i--)
        if ((size & (1 << i)) != 0)
        mask = (1 << i) - 1;Ǜ*/
        int v = 1;
        array[0] = 0;
        for (int i = 1; i < size; i++) {
            array[i] = v << shift;
            // Get next Permutation of bits in v
            v = Helper.nextPermutation(v);
            if (v > mask)
                // All permutations in mask done, shift by 2
                // (highest order bit vanishes by masking, next is 0),
                // set least significant bits => number of set bits in v inreased by 1
                v = ((v & mask) << 2) | 3;
        }
    }

    /**
     * Arrange back to default: order = identity
     */
    public void arrangeDefault() {
        for (int i = 0; i < 12; i++)
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
        this.detBits = detBits;
        int orderIndexLow = 0, orderIndexHigh = 11;
        for (int i = 0; i < 12; i++)
            if ((detBits.mask & (1 << i)) == 0) {
                order[orderIndexLow] = i;
                orderIndexLow++;
            } else {
                order[orderIndexHigh] = i;
                orderIndexHigh--;
            }

        // Set number of determinant bits (d_m) and determine size of blocks, that differ only in not det. bits
        d_m = Helper.hammingWeight(detBits.mask);
        undetBlockWidth = 1 << (6 - (d_m / 2) - (d_m % 2));
        undetBlockHeight = 1 << (6 - (d_m / 2));

        // Determine number of undet. bit blocks, grid size is 64x64
        undetBlockRowCount = 64 / undetBlockWidth;
        undetBlockColCount = 64 / undetBlockHeight;

        undetRow = new int[undetBlockWidth];
        undetCol = new int[undetBlockHeight];
        detRow = new int[undetBlockRowCount];
        detCol = new int[undetBlockColCount];

        // Fill column and row arrays
        fillOrderedArray(undetBlockHeight, undetCol, 0); // least significant bits
        fillOrderedArray(undetBlockWidth, undetRow, (6 - (d_m / 2))); // shifted by log2(undetBlockHeight)
        fillOrderedArray(undetBlockColCount, detCol, (12 - d_m)); // shifted some more by log2(undetBlockWidth)
        fillOrderedArray(undetBlockRowCount, detRow, (d_m / 2) + (12 - d_m)); // shifted some more by log2(undetBlockColCount) => most significant bits

        repaint();
    }

    public int getDrawType() {
        return drawType;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int xOffset = 50;
        int yOffset = 20; // Offsets of grid
        int squareSize = 11; // Width/height of grid's squares

        // Draw grid outline
        g.setColor(Color.GRAY);
        g.drawRect(xOffset - 1, yOffset - 1, 64 * squareSize + 1, 64 * squareSize + 1);

        if (detBits != null) {
            // Arranged by determinant bits => Draw groups
            g.setColor(Color.BLUE);
            for (int x = 0; x < 64; x++)
                for (int y = 0; y < 64; y++) {
                    int i = coordsToNode(x, y);
                    g.setColor(Color.getHSBColor(Helper.hammingWeight(
                            (detBits.mask & i) ^ detBits.values) / (float) d_m, 1.0f, 1.0f));
                    g.fillRect(x * squareSize + xOffset, y * squareSize + yOffset, squareSize, squareSize);
                }
        } else {
            // Not arranged => Draw grid's background monochrome
            g.setColor(Color.CYAN);
            g.fillRect(xOffset, yOffset, 64 * squareSize, 64 * squareSize);
        }

        if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + 64 * squareSize && mouseY <
                yOffset + 64 * squareSize) {
            // If mouse pointer in grid
            g.setColor(Color.BLACK);

            // Index of node below mouse pointer
            int i = coordsToNode((mouseX - xOffset) /
                    squareSize, (mouseY - yOffset - 2) / squareSize);

            g.drawString(Helper.getBitString(i), xOffset, yOffset + 64 * squareSize + 30);
            if (detBits != null)
                g.drawString("belongs to S_" + Helper.hammingWeight(
                        (detBits.mask & i) ^ detBits.values),
                        xOffset, yOffset + 64 * squareSize + 50);

            // Draw neighbours, brigthness determined by link weighting
            int c = ~i & ((1 << 12) - 1);
            for (int x = 0; x < 64; x++)
                for (int y = 0; y < 64; y++) {
                    int v = coordsToNode(x, y);
                    int diff = Helper.hammingWeight(v ^ c);
                    if (idnetManager.getLinkWeighting(diff) > 0) {
                        if (detBits != null)
                            g.setColor(Color.getHSBColor(Helper.hammingWeight((detBits.mask & v) ^ detBits.values) /
                                    (float) d_m, 1f - (float) idnetManager.getLinkWeighting(diff), 1f));
                        else
                            g.setColor(Color.getHSBColor(0f, 1f - (float) idnetManager.getLinkWeighting(diff), 1f));
                        g.fillRect(x * squareSize + xOffset, y * squareSize + yOffset, squareSize, squareSize);
                    }
                }

            // Mark node below mouse pointer
            g.setColor(Color.WHITE);
            g.fillRect(((mouseX - xOffset) / squareSize) * squareSize + xOffset, ((mouseY - yOffset - 2) / squareSize) *
                    squareSize + yOffset,
                    squareSize, squareSize);

        }

        // Draw dots for nodes' occupations
        switch (drawType) {
            case DRAW_CURRENT:
                g.setColor(Color.BLACK);
                for (int x = 0; x < 64; x++)
                    for (int y = 0; y < 64; y++) {
                        int i = coordsToNode(x, y);
                        if (idnetManager.getIdiotypes()[i].n > 0) {
                            int size = (idnetManager.getIdiotypes()[i].n * (squareSize - 4)) / idnetManager.getN();
                            g.fillRect(x * squareSize + xOffset + (squareSize - size) / 2, y * squareSize + yOffset +
                                    (squareSize - size) / 2, size, size);
                        }
                    }
                break;

            case DRAW_MEAN_OCCUPATIONS:
                g.setColor(Color.BLACK);
                for (int x = 0; x < 64; x++)
                    for (int y = 0; y < 64; y++) {
                        int i = coordsToNode(x, y);
                        if (idnetManager.gett() > 0) {
                            int size = (idnetManager.getIdiotypes()[i].sum_n * (squareSize - 4)) / idnetManager.getN() /
                                    idnetManager.gett();
                            g.fillRect(x * squareSize + xOffset + (squareSize - size) / 2, y * squareSize + yOffset +
                                    (squareSize - size) / 2, size, size);
                        }
                    }
                break;
        }

    }

}
