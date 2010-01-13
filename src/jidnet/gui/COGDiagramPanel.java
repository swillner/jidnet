package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
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
        for (int i = 0; i < 12; i++) {
            cogHistory[Application.getIdiotypicNetwork().gett() % historySize][i] = Application.getIdiotypicNetwork().getCOG()[i];
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (idnetManager.gett() == 0) {
            return;
        }

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
        for (int j = 0; j < 12; j++) {
            for (int i = 0; i < historySize; i++) {
                y = y_offset + halfHeight - (int) Math.round(halfHeight * cogHistory[i][j]);
                g.setColor(Color.getHSBColor((float) j/ 12f, 1.0f, 1.0f));
                //g.setColor(Color.getHSBColor((float) (j % 2) / 2, (float) (j % 5) / 5, (float) (j % 7) / 7));
                g.drawLine(x_offset + i, old_y, x_offset + i + 1, y);
                old_y = y;
            }
        }
        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset + Application.getIdiotypicNetwork().gett() % historySize, y_offset,
                x_offset + Application.getIdiotypicNetwork().gett() % historySize, y_offset + 2 * halfHeight);

        for (int j = 0; j < 12; j++) {
                g.setColor(Color.getHSBColor((float) j/ 12f, 1.0f, 1.0f));
                g.drawString("cog[" + j + "] = " + Math.round(idnetManager.getCOG()[j]*1000)/1000.0, 50, 50 + 20 *j);
                g.drawString("s(" + j + ") = " + Math.round(idnetManager.getCOGStandardDeviation(j)*10000)/10000.0, 200, 50 + 20 *j);
                if (idnetManager.getCOGStandardDeviation(j) < 0.05)
                    if (idnetManager.getCOG()[j] > 5 * idnetManager.getCOGStandardDeviation(j))
                        g.drawString("1", 350, 50 + 20 *j);
                    else if (idnetManager.getCOG()[j] < -5 * idnetManager.getCOGStandardDeviation(j))
                        g.drawString("0", 350, 50 + 20 *j); // TODO
        }

        g.setColor(Color.BLACK);
        IdnetManager.DeterminantBits determinantBits = idnetManager.getDeterminantBits();
        String str = Integer.toString(determinantBits.mask, 2);
            str = "000000000000".substring(str.length()) + str;
        g.drawString("Determinant bits mask  = " + str, 500, 50);
        str = Integer.toString(determinantBits.values, 2);
            str = "000000000000".substring(str.length()) + str;
        g.drawString("Determinant bits values = " + str, 500, 70);
    }
}
