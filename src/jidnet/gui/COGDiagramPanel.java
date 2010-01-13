package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public class COGDiagramPanel extends JPanel {

    private IdnetManager idnetManager;
    final static int historySize = 1000;
    private double[][] cogHistory;

    public COGDiagramPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        cogHistory = new double[historySize][12];
    }

    public void afterIteration() {
        for (int i = 0; i < 12; i++)
            cogHistory[Application.getIdnetManager().gett() % historySize][i] = Application.getIdnetManager().getCOG()[i];
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (idnetManager.gett() == 0)
            return;

        final int halfHeight = 200;
        int x_offset = 20;
        int y_offset = 300;

        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset, y_offset, x_offset, y_offset + 2 * halfHeight);
        g.drawLine(x_offset, y_offset, x_offset + historySize, y_offset);
        g.drawLine(x_offset, y_offset + halfHeight, x_offset + historySize, y_offset + halfHeight);
        g.drawLine(x_offset, y_offset + 2 * halfHeight, x_offset + historySize, y_offset + 2 * halfHeight);
        g.drawLine(x_offset + historySize, y_offset, x_offset + historySize, y_offset + 2 * halfHeight);

        int y = y_offset + halfHeight;
        int old_y = y;
        for (int j = 0; j < 12; j++)
            for (int i = 0; i < historySize; i++) {
                y = y_offset + halfHeight - (int) Math.round(halfHeight * cogHistory[i][j]);
                g.setColor(Color.getHSBColor((float) j / 12f, 1.0f, 1.0f));
                g.drawLine(x_offset + i, old_y, x_offset + i + 1, y);
                old_y = y;
            }
        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset + Application.getIdnetManager().gett() % historySize, y_offset,
                x_offset + Application.getIdnetManager().gett() % historySize, y_offset + 2 * halfHeight);

        IdnetManager.DeterminantBits determinantBits = idnetManager.getDeterminantBits();
        for (int j = 0; j < 12; j++) {
            g.setColor(Color.getHSBColor((float) j / 12f, 1.0f, 1.0f));
            g.drawString("cog[" + j + "] = " + Math.round(idnetManager.getCOG()[j] * 1000) / 1000.0, 50, 50 + 20 * j);
            g.drawString("s(" + j + ") = " + Math.round(idnetManager.getCOGStandardDeviation(j) * 10000) / 10000.0,
                    200, 50 + 20 * j);
            if ((determinantBits.mask & (1 << j)) != 0)
                if ((determinantBits.values & (1 << j)) != 0)
                    g.drawString("1", 350, 50 + 20 * j);
                else
                    g.drawString("0", 350, 50 + 20 * j);
        }

        g.setColor(Color.BLACK);
        g.drawString("Determinant bits mask  = " + Helper.getBitString(determinantBits.mask), 500, 50);
        g.drawString("Determinant bits values = " + Helper.getBitString(determinantBits.values), 500, 70);
        g.drawString("d_m = " + Helper.hammingWeight(determinantBits.mask), 500, 90);
    }

}
