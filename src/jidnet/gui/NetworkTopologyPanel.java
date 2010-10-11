package jidnet.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.Idiotype;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class NetworkTopologyPanel extends JPanel implements Observer {

    private IdnetManager idnetManager;
    private DeterminantBits detBits;
    private final static int MAX_NEIGHBOUR_COUNT = 80;
    private int[] neighbourCounts;
    private int[] neighbourCountsOccupied;
    private int[][] groupNeighbourCounts;
    private int[][] bitwiseNeighbourCounts;
    private int max;
    public final static int DRAW_CURRENT = 0;
    public final static int DRAW_TOTAL_MEANS = 1;
    private int drawType;

    public NetworkTopologyPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        drawType = DRAW_CURRENT;
        idnetManager.addObserver(this);
        recalc();
    }

    public int getDrawType() {
        return drawType;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
        switch (drawType) {
            case DRAW_CURRENT:
                update(null, null);
                break;
            case DRAW_TOTAL_MEANS:
                recalc();
                break;
        }
        repaint();
    }

    public void setDeterminantBits(DeterminantBits detBits) {
        this.detBits = detBits;
        //if (drawType != DRAW_CURRENT)
        //    setDrawType(DRAW_CURRENT);
        recalc();
        update(null, "iteration");
        repaint();
    }

    public void recalc() {
        int d_m = 0;
        if (detBits != null)
            d_m = Helper.hammingWeight(detBits.mask);
        neighbourCounts = new int[MAX_NEIGHBOUR_COUNT + 1];
        neighbourCountsOccupied = new int[MAX_NEIGHBOUR_COUNT + 1];
        groupNeighbourCounts = new int[d_m + 1][MAX_NEIGHBOUR_COUNT + 1];
        bitwiseNeighbourCounts = new int[idnetManager.getd()][MAX_NEIGHBOUR_COUNT + 1];
    }

    public void update(Observable o, Object arg) {
        if (!arg.equals("iteration"))
            return;
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
                for (int j = 0; j < idnetManager.getd(); j++)
                    if ((i.i & (1 << j)) != 0)
                        bitwiseNeighbourCounts[j][(int) i.n_d]++;
                max = Math.max(max, neighbourCounts[(int) i.n_d]);
            }
    }

    public void saveDataToFile(String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        fw.write("# Total network:\n");
        for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++)
            fw.write(i + " " + neighbourCounts[i] + "\n");
        fw.write("\n# Occupied nodes:\n");
        for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++)
            fw.write(i + " " + neighbourCountsOccupied[i] + "\n");
        if (detBits != null) {
            fw.write("\n# Determinant bit groups:\n");
            for (int j = 0; j <= Helper.hammingWeight(detBits.mask); j++) {
                fw.write("# S_" + j + ":\n");
                for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++)
                    fw.write(i + " " + groupNeighbourCounts[j][i] + "\n");
            }
        }
        fw.close();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //if (idnetManager.gett() == 0)
        //    return;

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
        else
            lastGroupY = new int[idnetManager.getd()];
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
                g.fillArc(xOffset + i * stepWidth - 4, yOffset + height - lastYOccupied - 4, 7, 7, 0, 360);
            }

            if (detBits != null) {
                int d_m = Helper.hammingWeight(detBits.mask);
                for (int j = 0; j <= d_m; j++) {
                    g.setColor(Color.getHSBColor((float) j / (float) (d_m + 1), 1.0f, 0.8f));
                    if (i > 0)
                        g.drawLine(xOffset + (i - 1) * stepWidth, yOffset + height - lastGroupY[j], xOffset + i
                                * stepWidth,
                                yOffset + height - (int) (height * (double) groupNeighbourCounts[j][i] / (double) max));
                    lastGroupY[j] = (int) (height * (double) groupNeighbourCounts[j][i] / (double) max);

                    if (lastGroupY[j] > 0) {
                        g.setColor(Color.getHSBColor((float) j / (float) (d_m + 1), 1.0f, 1.0f));
                        g.fillRect(xOffset + i * stepWidth - 2, yOffset + height - lastGroupY[j] - 2, 5, 5);
                    }
                }
            }/* else
            for (int j = 0; j < d; j++) {
            g.setColor(Color.getHSBColor((float) j / (float) d, 1.0f, 0.8f));
            if (i > 0)
            g.drawLine(xOffset + (i - 1) * stepWidth, yOffset + height - lastGroupY[j], xOffset + i *
            stepWidth,
            yOffset + height - (int) (height * (double) bitwiseNeighbourCounts[j][i] / (double) max));
            lastGroupY[j] = (int) (height * (double) bitwiseNeighbourCounts[j][i] / (double) max);

            if (lastGroupY[j] > 0) {
            g.setColor(Color.getHSBColor((float) j / (float) d, 1.0f, 1.0f));
            g.fillRect(xOffset + i * stepWidth - 2, yOffset + height - lastGroupY[j] - 2, 5, 5);
            }
            }*/

            g.setColor(Color.BLACK);
            g.drawString(i + "", xOffset + i * stepWidth - 3, yOffset + height + 10);
        }

        g.setColor(new Color(255, 255, 255, 128));
        g.fillRect((int) (xOffset + idnetManager.gett_l() * stepWidth), yOffset, (int) ((idnetManager.gett_u()
                - idnetManager.gett_l()) * stepWidth), height);

        g.setFont(new Font("Arial", 0, 12));
        if (detBits != null) {
            int d_m = Helper.hammingWeight(detBits.mask);
            for (int j = 0; j <= d_m; j++) {
                g.setColor(Color.getHSBColor((float) j / (float) (d_m + 1), 1.0f, 1.0f));
                g.fillRect(xOffset + MAX_NEIGHBOUR_COUNT * stepWidth + 50, yOffset + 150 + 30 * j, 18, 18);
                g.setColor(Color.BLACK);
                g.drawString("S_" + j, xOffset + MAX_NEIGHBOUR_COUNT * stepWidth + 72, yOffset + 163 + 30 * j);
            }
        }/* else
        for (int j = 0; j < d; j++) {
        g.setColor(Color.getHSBColor((float) j / (float) d, 1.0f, 1.0f));
        g.fillRect(xOffset + MAX_NEIGHBOUR_COUNT * stepWidth + 50, yOffset + 150 + 30 * j, 18, 18);
        g.setColor(Color.BLACK);
        g.drawString(j + "", xOffset + MAX_NEIGHBOUR_COUNT * stepWidth + 72, yOffset + 163 + 30 * j);
        }*/

    }
}
