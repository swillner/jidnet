package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JPanel;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 * Swing component, that shows a 2D blcok grid of idnetManager's nodes and their occupation.
 *
 * @author Sven Willner
 */
public class NetworkBlockPanel extends JPanel {

    private IdnetManager idnetManager;
    // Coordinates of mouse pointer in component
    private int mouseX = -1, mouseY = -1;
    private HashMap<Integer, ArrayList<Integer>> blocks;
    private int dimSubgroup;
    private int d;
    private int blockRowCount, blockColCount, innerBlockRowCount, innerBlockColCount;
    private int squareSize = 10; // Width/height of grid's squares
    private int xOffset = 20;
    private int yOffset = 20; // Offsets of grid
    private int last_i = -1;

    public NetworkBlockPanel(IdnetManager idnetManager) {
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

        change_d(idnetManager.getd());
    }

    public void setOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public void change_d(int d) {
        this.d = d;
        repaint();
    }

    public void setSquareSize(int size) {
        this.squareSize = size;
        repaint();
    }

    public void setSubgroupCreators(ArrayList<Integer> s) {
        dimSubgroup = s.size();
        blockColCount = (1 << ((d - dimSubgroup) / 2 + (d - dimSubgroup) % 2));
        blockRowCount = (1 << ((d - dimSubgroup) / 2));
        innerBlockRowCount = (1 << (dimSubgroup / 2 + dimSubgroup % 2));
        innerBlockColCount = (1 << (dimSubgroup / 2));
        System.out.println("blockRowCount = " + blockRowCount);
        System.out.println("blockColCount = " + blockColCount);
        System.out.println("innerBlockRowCount = " + innerBlockRowCount);
        System.out.println("innerBlockColCount = " + innerBlockColCount);
        System.out.println("RowCount = " + blockRowCount * innerBlockRowCount);
        System.out.println("ColCount = " + blockColCount * innerBlockColCount);
        blocks = new HashMap<Integer, ArrayList<Integer>>();
        blocks.put(0, new ArrayList<Integer>());
        blocks.get(0).add(0);
        for (int i = 0; i < dimSubgroup; i++) {
            int size = blocks.get(0).size();
            for (int j = 0; j < size; j++) {
                blocks.get(0).add(s.get(i) ^ blocks.get(0).get(j));
                //System.out.println(Helper.getBitString(s.get(i) ^ blocks.get(0).get(j), d));
            }
        }
        //System.out.println("Blocksize = " + blocks.get(0).size());
        for (int i = 0; i < (1 << d); i++) {
            boolean found = false;
            for (ArrayList<Integer> b : blocks.values()) {
                if (b.contains(i)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ArrayList<Integer> b = new ArrayList<Integer>();
                for (int j = 0; j < blocks.get(0).size(); j++) {
                    b.add(i ^ blocks.get(0).get(j));
                }
                blocks.put(i, b);
            }
        }
        //System.out.println("Blocks.Size() == " + blocks.size());
    }

    public int coordsToNode(int x, int y) {
        int block = blockColCount * (y / innerBlockRowCount) + (x / innerBlockColCount);
        //return block * innerBlockColCount * innerBlockRowCount + innerBlockColCount * (y % innerBlockRowCount) + x % innerBlockColCount;
        for (Entry<Integer, ArrayList<Integer>> s : blocks.entrySet()) {
            if (block == 0) {
                return s.getValue().get(innerBlockColCount * (y % innerBlockRowCount) + x % innerBlockColCount);
            } else {
                block--;
            }
        }
        return 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (blocks == null) {
            return;
        }

        // Draw grid outline
        g.setColor(Color.GRAY);
        g.drawRect(xOffset - 1, yOffset - 1, (1 << (d / 2)) * squareSize + 1, (1 << (d / 2 + (d % 2))) * squareSize + 1);
        g.drawRect((1 << (d / 2)) * squareSize + 3 * xOffset - 1, yOffset - 1, (1 << (d / 2)) * squareSize + 1, (1 << (d / 2) + (d % 2)) * squareSize + 1);

        for (int x = 0; x < blockColCount; x++) {
            for (int y = 0; y < blockRowCount; y++) {
                if ((x + y) % 2 == 0) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.GREEN);
                }
                g.fillRect(x * squareSize * innerBlockColCount + xOffset, y * squareSize * innerBlockRowCount + yOffset, squareSize * innerBlockColCount, squareSize * innerBlockRowCount);
            }
        }

        for (int x = 0; x < (1 << (d / 2)); x++) {
            for (int y = 0; y < (1 << (d / 2 + (d % 2))); y++) {
                int i = coordsToNode(x, y);
                g.setColor(Color.getHSBColor(0.0f, 0.0f, 1.0f - (float) idnetManager.getIdiotypes()[i].sum_n / (float) idnetManager.getN() / (float) idnetManager.gett()));
                g.fillRect(x * squareSize + 3 * xOffset + (1 << (d / 2)) * squareSize, y * squareSize + yOffset, squareSize, squareSize);
            }
        }

        g.setColor(Color.RED);
        for (int x = 0; x < blockColCount; x++) {
            for (int y = 0; y < blockRowCount; y++) {
                if (y > 0) {
                    g.drawLine((1 << (d / 2)) * squareSize + 3 * xOffset + x * innerBlockColCount * squareSize, yOffset + y * innerBlockRowCount * squareSize, (1 << (d / 2)) * squareSize + 3 * xOffset + (x + 1) * innerBlockColCount * squareSize, yOffset + y * innerBlockRowCount * squareSize);
                }
                if (x > 0) {
                    g.drawLine((1 << (d / 2)) * squareSize + 3 * xOffset + x * innerBlockColCount * squareSize, yOffset + y * innerBlockRowCount * squareSize, (1 << (d / 2)) * squareSize + 3 * xOffset + x * innerBlockColCount * squareSize, yOffset + (y + 1) * innerBlockRowCount * squareSize);
                }
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

            // Draw neighbours, brigthness determined by link weighting
            int c = ~i & ((1 << d) - 1);
            for (int x = 0; x < (1 << (d / 2)); x++) {
                for (int y = 0; y < (1 << (d / 2 + (d % 2))); y++) {
                    int v = coordsToNode(x, y);
                    int diff = Helper.hammingWeight(v ^ c);
                    if (idnetManager.getLinkWeighting(diff) > 0) {
                        if (((x / innerBlockColCount) + (y / innerBlockRowCount)) % 2 == 0) {
                            g.setColor(Color.getHSBColor(0f, 1f - (float) idnetManager.getLinkWeighting(diff), 1f));
                        } else {
                            g.setColor(Color.getHSBColor(0.3f, 1f - (float) idnetManager.getLinkWeighting(diff), 1f));
                        }
                        g.fillRect(x * squareSize + xOffset, y * squareSize + yOffset, squareSize, squareSize);
                    }
                }
            }

            // Mark node below mouse pointer
            g.setColor(Color.WHITE);
            g.fillRect(((mouseX - xOffset) / squareSize) * squareSize + xOffset, ((mouseY - yOffset - 2) / squareSize)
                    * squareSize + yOffset,
                    squareSize, squareSize);

        }

        boolean[] check = new boolean[1 << d];
        for (int i = 0; i < (1 << d); i++) {
            check[i] = false;
        }

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
