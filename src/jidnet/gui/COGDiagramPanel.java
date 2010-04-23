package jidnet.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public class COGDiagramPanel extends JPanel implements Observer {

    private IdnetManager idnetManager;
    final static int historySize = 1000;
    private double[][] cogHistory;

    public COGDiagramPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        idnetManager.addObserver(this);
        cogHistory = new double[historySize][12];
    }

    public void update(Observable o, Object arg) {
        for (int i = 0; i < 12; i++)
            cogHistory[Application.getIdnetManager().gett() % historySize][i] =
                    Application.getIdnetManager().getCOG()[i];
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);


        //if (idnetManager.gett() == 0)
        //    return;

        final int halfHeight = 200;
        int x_offset = 20;
        int y_offset = 20;

        g.setFont(new Font("Arial", 0, 12));

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
                g.drawLine(x_offset + i, old_y, x_offset + i, y);
                old_y = y;
            }


        /*g.setColor(Color.BLACK);
        for (int i = 0; i < historySize; i++) {
            double y_ = 0;
            for (int j = 0; j < 12; j++)
                for (int k = 0; k < 100; k++)
                y_ += Math.abs(cogHistory[(i + historySize - k + 1) % historySize][j] - cogHistory[(i + historySize - k) % historySize][j]);
            y = y_offset + halfHeight - (int) Math.round(halfHeight * y_ / 100);
            g.drawLine(x_offset + i, old_y, x_offset + i, y);
            old_y = y;
        }*/

        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset + Application.getIdnetManager().gett() % historySize + 1, y_offset,
                x_offset + Application.getIdnetManager().gett() % historySize + 1, y_offset + 2 * halfHeight);

        DeterminantBits determinantBits = idnetManager.calcDeterminantBits();
        for (int j = 0; j < 12; j++) {
            g.setColor(Color.getHSBColor((float) j / 12f, 1.0f, 1.0f));
            g.fillRect(25, 20 * j + 2 * halfHeight + 3 * y_offset - 13, 18, 18);
            g.setColor(Color.BLACK);
            g.drawString("cog[" + j + "] = " + Math.round(idnetManager.getCOG()[j] * 1000) / 1000.0, 70, 20 * j + 2
                    * halfHeight + 3 * y_offset);
            g.drawString("s(" + j + ") = " + Math.round(idnetManager.getCOGStandardDeviation(j) * 10000) / 10000.0,
                    200, 20 * j + 2 * halfHeight + 3 * y_offset);
            if ((determinantBits.mask & (1 << j)) != 0)
                if ((determinantBits.values & (1 << j)) != 0)
                    g.drawString("1", 50, 20 * j + 2 * halfHeight + 3 * y_offset);
                else
                    g.drawString("0", 50, 20 * j + 2 * halfHeight + 3 * y_offset);
        }

        g.setColor(Color.BLACK);
        g.drawString("Determinant bits mask  = " + Helper.getBitString(determinantBits.mask), 500, 2 * halfHeight + 3
                * y_offset);
        g.drawString("Determinant bits values = " + Helper.getBitString(determinantBits.values), 500, 2 * halfHeight
                + 3 * y_offset + 20);
        g.drawString("d_m = " + Helper.hammingWeight(determinantBits.mask), 500, 2 * halfHeight + 3 * y_offset + 40);
    }
}
