package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import jidnet.idnet.IdiotypicNetwork;

/**
 *
 * @author sven
 */
public class COGDiagramPanel extends JPanel {

    private IdiotypicNetwork idiotypicNetwork;
    final static int historySize = 1000;
    private double[][] cogHistory;

    public COGDiagramPanel(IdiotypicNetwork idnetManager) {
        super();
        this.idiotypicNetwork = idnetManager;
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

        if (idiotypicNetwork.gett() == 0) {
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
                g.setColor(Color.getHSBColor((float) (j % 2) / 2, (float) (j % 5) / 5, (float) (j % 7) / 7));
                g.drawLine(x_offset + i, old_y, x_offset + i + 1, y);
                old_y = y;
            }
        }
        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset + Application.getIdiotypicNetwork().gett() % historySize, y_offset,
                x_offset + Application.getIdiotypicNetwork().gett() % historySize, y_offset + 2 * halfHeight);

    }
}
