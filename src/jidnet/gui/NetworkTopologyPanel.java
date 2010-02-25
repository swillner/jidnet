package jidnet.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.Idiotype;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class NetworkTopologyPanel extends JPanel {

    private IdnetManager idnetManager;
    private DeterminantBits detBits;
    private final static int MAX_NEIGHBOUR_COUNT = 80;
    private int[] neighbourCounts;
    private int[] neighbourCountsOccupied;
    private int[][] groupNeighbourCounts;
    private int[] neighbourCountsOld;
    private int[] neighbourCountsOccupiedOld;
    private int[][] groupNeighbourCountsOld;
    private int max;
    public final static int DRAW_CURRENT = 0;
    public final static int DRAW_TOTAL_MEANS = 1;
    private int drawType = DRAW_CURRENT;

    public NetworkTopologyPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        recalc();
    }

    public int getDrawType() {
        return drawType;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
        switch (drawType) {
            case DRAW_CURRENT:
                afterIteration();
                break;
            case DRAW_TOTAL_MEANS:
                recalc();
                break;
        }
        repaint();
    }

    public void setDeterminantBits(DeterminantBits detBits) {
        this.detBits = detBits;
        if (drawType != DRAW_CURRENT)
            setDrawType(DRAW_CURRENT);
        recalc();
        afterIteration();
        repaint();
    }

    public void recalc() {
        int d_m = 0;
        if (detBits != null)
            d_m = Helper.hammingWeight(detBits.mask);
        neighbourCounts = new int[MAX_NEIGHBOUR_COUNT + 1];
        neighbourCountsOccupied = new int[MAX_NEIGHBOUR_COUNT + 1];
        groupNeighbourCounts = new int[d_m + 1][MAX_NEIGHBOUR_COUNT + 1];
    }

    public void afterIteration() {
        if (drawType == DRAW_CURRENT)
            if (isShowing())
                recalc();
            else
                return;
        max = 1;
        for (Idiotype i : idnetManager.getIdiotypes())
            if ((int) i.n_d <= MAX_NEIGHBOUR_COUNT) {
                neighbourCounts[(int) i.n_d]++;
                if (i.n > 0)
                    neighbourCountsOccupied[(int) i.n_d]++;
                if (detBits != null)
                    groupNeighbourCounts[Helper.hammingWeight((detBits.mask & i.i) ^ detBits.values)][(int) i.n_d]++;
                max = Math.max(max, neighbourCounts[(int) i.n_d]);
            }
        /*BufferedImage img = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawString("t = " + idnetManager.gett(), 800, 20);
        g.dispose();
        try {
        ImageIO.write(img, "png", new File(1000000 + idnetManager.gett() + ".png"));
        } catch(IOException e) {
        e.printStackTrace();
        }*/
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (idnetManager.gett() == 0)
            return;

        // Draw diagram outlines
        g.setColor(Color.getHSBColor(0.7f, 0.7f, 1.0f));

        final int xOffset = 20;
        final int yOffset = 20;
        final int stepWidth = 12;
        final int height = 700;
        g.drawLine(xOffset, yOffset, xOffset, yOffset + height);
        g.drawLine(xOffset, yOffset + height, xOffset + stepWidth * 80, yOffset + height);

        int lastY = (int) (height * (double) neighbourCounts[0] / (double) max);
        int lastYOccupied = 0;
        int[] lastGroupY = null;
        if (detBits != null)
            lastGroupY = new int[Helper.hammingWeight(detBits.mask) + 1];
        g.setFont(new Font("Arial", 0, 8));
        for (int i = 1; i <= MAX_NEIGHBOUR_COUNT; i++) {
            g.setColor(Color.LIGHT_GRAY);
            //g.drawLine(xOffset + (i - 1) * stepWidth, yOffset + height - lastY, xOffset + i * stepWidth,
            //        yOffset + height - (int) (height * (double) neighbourCounts[i] / (double) max));
            Polygon p = new Polygon();
            p.addPoint(xOffset + (i - 1) * stepWidth, yOffset + height); // bottom left
            p.addPoint(xOffset + (i - 1) * stepWidth, yOffset + height - lastY); // top left
            lastY = (int) (height * (double) neighbourCounts[i] / (double) max);
            p.addPoint(xOffset + i * stepWidth, yOffset + height - lastY); // top right
            p.addPoint(xOffset + i * stepWidth, yOffset + height); // bottom right
            g.fillPolygon(p);
        }

        for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++) {
            if (i > 0 && neighbourCountsOccupied[i] > 0) {
                g.setColor(Color.GRAY);
                g.drawLine(xOffset + (i - 1) * stepWidth, yOffset + height - lastYOccupied, xOffset + i * stepWidth,
                        yOffset + height - (int) (height * (double) neighbourCountsOccupied[i] / (double) max));
            }
            lastYOccupied = (int) (height * (double) neighbourCountsOccupied[i] / (double) max);

            //g.setColor(Color.BLACK);
            //g.fillRect(xOffset + i * stepWidth - 3, yOffset + height - lastY - 3, 6, 6);

            if (lastYOccupied > 0) {
                g.setColor(Color.BLACK);
                g.fillArc(xOffset + i * stepWidth - 3, yOffset + height - lastYOccupied - 3, 5, 5, 0, 360);
            }

            if (detBits != null) {
                int d_m = Helper.hammingWeight(detBits.mask);
                for (int j = 0; j <= d_m; j++) {
                    g.setColor(Color.getHSBColor((float) j / (float) d_m, 1.0f, 0.5f));
                    if (i > 0)
                        g.drawLine(xOffset + (i - 1) * stepWidth, yOffset + height - lastGroupY[j], xOffset + i *
                                stepWidth,
                                yOffset + height - (int) (height * (double) groupNeighbourCounts[j][i] / (double) max));
                    lastGroupY[j] = (int) (height * (double) groupNeighbourCounts[j][i] / (double) max);

                    if (lastGroupY[j] > 0) {
                        g.setColor(Color.getHSBColor((float) j / (float) d_m, 1.0f, 1.0f));
                        g.fillRect(xOffset + i * stepWidth - 1, yOffset + height - lastGroupY[j] - 1, 3, 3);
                    }
                }
            }

            g.setColor(Color.BLACK);
            g.drawString(i + "", xOffset + i * stepWidth, yOffset + height + 10);
        }

        g.setColor(new Color(255, 255, 255, 128));
        g.fillRect((int) (xOffset + idnetManager.gett_l() * stepWidth), yOffset, (int) ((idnetManager.gett_u() -
                idnetManager.gett_l()) * stepWidth), height);

    }

}
