package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import jidnet.idnet.IdiotypicNetwork;

/**
 *
 * @author sven
 */
public class NetworkDiagramPanel extends JPanel {

    private IdiotypicNetwork idiotypicNetwork;

    public NetworkDiagramPanel(IdiotypicNetwork idnetManager) {
        super();
        this.idiotypicNetwork = idnetManager;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (idiotypicNetwork.gett() == 0)
            return;
        
        // Draw diagram outlines
        g.setColor(Color.getHSBColor(0.7f, 0.7f, 1.0f));

        g.drawLine(0, 0, 1024, 0);
        g.drawLine(0, 150, 1024, 150);
        g.drawLine(0, 200, 1024, 200);
        g.drawLine(0, 350, 1024, 350);
        g.drawLine(0, 400, 1024, 400);
        g.drawLine(0, 550, 1024, 550);
        g.drawLine(0, 600, 1024, 600);
        g.drawLine(0, 750, 1024, 750);

        // Draw mean occupation dots
        g.setColor(Color.BLACK);
        for (int i = 0; i < 4096; i++) {
            long y = i / 1024 * 200 + 149
                    - Math.round(idiotypicNetwork.getIdiotypes()[i].sum_n
                      / (double)idiotypicNetwork.gett()
                      / (double)idiotypicNetwork.getN() * 150);
            //g.drawLine(i % 1024, (int)y, i % 1024, (int)y);
            g.drawRect(i % 1024, (int)y, 1, 1);
        }

    }
}
