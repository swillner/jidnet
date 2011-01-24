package jidnet.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileWriter;
import java.io.IOException;
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
    private int d;
    final static int historySize = 1000;
    private double[][] cogHistory;
    private double[] means;

    public COGDiagramPanel(IdnetManager idnetManager) {
        super();
        this.idnetManager = idnetManager;
        idnetManager.addObserver(this);
        change_d(idnetManager.getd());
    }

    public final void change_d(int d) {
        this.d = d;
        cogHistory = new double[historySize][d];
        means = new double[d];
        repaint();
    }

    public void update(Observable o, Object arg) {
        if (!arg.equals("iteration"))
            return;
        for (int i = 0; i < d; i++) {
            cogHistory[Application.getIdnetManager().gett() % historySize][i] =
                    Application.getIdnetManager().getCOG()[i];
            means[i] += Application.getIdnetManager().getCOG()[i];
        }
    }

    public void recalc() {
        means = new double[d];
    }

    public void saveDataToFile(String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        for (int i = 0; i < historySize; i++) {
            fw.write(i + " ");
            for (int j = 0; j < idnetManager.getd(); j++)
                fw.write (cogHistory[i][j] + " ");
            fw.write("\n");
        }
        fw.close();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

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
        for (int j = 0; j < d; j++)
            for (int i = 0; i < historySize; i++) {
                y = y_offset + halfHeight - (int) Math.round(halfHeight * cogHistory[i][j]);
                g.setColor(Color.getHSBColor((float) j / (float) d, 1.0f, 1.0f));
                g.drawLine(x_offset + i, old_y, x_offset + i, y);
                old_y = y;
            }


        /*g.setColor(Color.BLACK);
        for (int i = 0; i < historySize; i++) {
        double y_ = 0;
        for (int j = 0; j < d; j++)
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
        for (int j = 0; j < d; j++) {
            g.setColor(Color.getHSBColor((float) j / (float) d, 1.0f, 1.0f));
            g.fillRect(25, 20 * j + 2 * halfHeight + 3 * y_offset - 13, 18, 18);
            g.setColor(Color.BLACK);
            g.drawString("cog[" + j + "] = " + Math.round(idnetManager.getCOG()[j] * 1000000000) / 1000000000.0, 70, 20 * j + 2
                    * halfHeight + 3 * y_offset);
            g.drawString("s(" + j + ") = " + Math.round(idnetManager.getCOGStandardDeviation(j) * 10000) / 10000.0,
                    235, 20 * j + 2 * halfHeight + 3 * y_offset);
            g.drawString("mean[" + j + "] = " + Math.round(means[j] / idnetManager.gett() * 1000000000) / 1000000000.0, 360, 20 * j + 2
                    * halfHeight + 3 * y_offset);
            if ((determinantBits.mask & (1 << j)) != 0)
                if ((determinantBits.values & (1 << j)) != 0)
                    g.drawString("1", 50, 20 * j + 2 * halfHeight + 3 * y_offset);
                else
                    g.drawString("0", 50, 20 * j + 2 * halfHeight + 3 * y_offset);
        }

        g.setColor(Color.BLACK);
        g.drawString("Determinant bits mask  = " + Helper.getBitString(determinantBits.mask, d), 550, 2 * halfHeight + 3
                * y_offset);
        g.drawString("Determinant bits values = " + Helper.getBitString(determinantBits.values, d), 550, 2 * halfHeight
                + 3 * y_offset + 20);
        g.drawString("d_m = " + Helper.hammingWeight(determinantBits.mask), 550, 2 * halfHeight + 3 * y_offset + 40);
    }
}
