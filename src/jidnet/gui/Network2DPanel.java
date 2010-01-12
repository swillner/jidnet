package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import jidnet.idnet.IdiotypicNetwork;

/**
 *
 * @author sven
 */
public class Network2DPanel extends JPanel {

    private IdiotypicNetwork idiotypicNetwork;
    private int mouseX = -1, mouseY = -1;

    public Network2DPanel(IdiotypicNetwork idnetManager) {
        super();
        this.idiotypicNetwork = idnetManager;
        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                //paintImmediately(50, 562, 200, 50);
                repaint();
            }

        });
    }

    private int coordsToIndex(int x, int y) {
        /*return ((y & (1 << 0)) << 0) | ((x & (1 << 0)) << 1) |
                ((x & (1 << 1)) << 1) | ((y & (1 << 1)) <<
                2) | ((y & (1 << 2)) << 2) | ((x & (1 << 2)) << 3) | ((x & (1 <<
                3)) << 3) |
                ((y & (1 << 3)) << 4) | ((y & (1 << 4)) << 4) |
                ((x & (1 << 4)) << 5) |
                ((x & (1 << 5)) << 5) | ((y & (1 << 5)) << 6);*/
        return ((y & (1 << 0)) << 0) | ((x & (1 << 0)) << 1) |
                ((y & (1 << 1)) << 1) | ((x & (1 << 1)) <<
                2) | ((y & (1 << 2)) << 2) | ((x & (1 << 2)) << 3) | ((y & (1 <<
                3)) << 3) |
                ((x & (1 << 3)) << 4) | ((y & (1 << 4)) << 4) |
                ((x & (1 << 4)) << 5) |
                ((y & (1 << 5)) << 5) | ((x & (1 << 5)) << 6);
    }

    private Point indexToCoords(int i) {
        /*int y = ((i & (1 << 0)) >> 0) | ((i & (1 << 3)) >> 2) |
                ((i & (1 << 4)) >> 2) | ((i & (1 << 7)) >> 4) |
                ((i & (1 << 8)) >> 4) | ((i & (1 << 11)) >> 6);
        int x = ((i & (1 << 1)) >> 1) | ((i & (1 << 2)) >> 1) |
                ((i & (1 << 5)) >> 3) | ((i & (1 << 6)) >> 3) |
                ((i & (1 << 9)) >> 5) | ((i & (1 << 10)) >> 5);*/
        int y = ((i & (1 << 0)) >> 0) | ((i & (1 << 2)) >> 1) |
                ((i & (1 << 4)) >> 2) | ((i & (1 << 6)) >> 3) |
                ((i & (1 << 8)) >> 4) | ((i & (1 << 10)) >> 5);
        int x = ((i & (1 << 1)) >> 1) | ((i & (1 << 3)) >> 2) |
                ((i & (1 << 5)) >> 3) | ((i & (1 << 7)) >> 4) |
                ((i & (1 << 9)) >> 5) | ((i & (1 << 11)) >> 6);
        return new Point(x, y);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //if (idiotypicNetwork.gett() == 0)
        //    return;

        int xOffset = 50;
        int yOffset = 50;
        int size = 8;
        // Draw mean occupation dots
        g.setColor(Color.GRAY);
        g.drawRect(xOffset - 1, yOffset - 1, 64 * size + 1, 64 * size + 1);
        g.setColor(Color.BLUE);
        for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++) {
                int i = coordsToIndex(x, y);
                //g.setColor(Color.getHSBColor(240f/360f, (float)idiotypicNetwork.getIdiotypes()[i].sum_n/idiotypicNetwork.gett(), 1.0f));
                //if (idiotypicNetwork.getIdiotypes()[i].n > 0) {
                if (idiotypicNetwork.getIdiotypes()[i].n == 1)
                    g.setColor(Color.BLUE);
                else if (idiotypicNetwork.getIdiotypes()[i].n == 2)
                    g.setColor(Color.RED);
                else
                    g.setColor(Color.WHITE);
                g.fillRect(x * size + xOffset, y * size + yOffset, size, size);
            }

        g.setColor(Color.BLACK);
        if (mouseX > xOffset && mouseY > yOffset && mouseX < xOffset + 64 * size &&
                mouseY < yOffset + 64 * size) {
            int i = coordsToIndex((mouseX - xOffset) /
                    size, (mouseY - yOffset) / size);
            String str = Integer.toString(i, 2);
            str = "000000000000".substring(str.length()) + str;
            g.drawString(str, xOffset, yOffset + 64 * size + 30);

            Point p = indexToCoords(i);
            g.setColor(Color.BLACK);
            g.fillRect(p.x * size + xOffset, p.y * size + yOffset, size, size);

            int c = ~i & 4095;
            p = indexToCoords(c);
            g.setColor(Color.RED);
            g.fillRect(p.x * size + xOffset, p.y * size + yOffset, size, size);

            int m1, m2;
            m1 = 4096;
            while (m1 != 0) {
                p = indexToCoords(c ^ m1);
                g.setColor(Color.RED);
                g.fillRect(p.x * size + xOffset, p.y * size + yOffset, size,
                        size);
                g.setColor(Color.ORANGE);

                m2 = m1 >> 1;
                while (m2 != 0) {
                    p = indexToCoords((c ^ m1) ^ m2);
                    g.fillRect(p.x * size + xOffset, p.y * size + yOffset, size,
                            size);

                    m2 = m2 >> 1;
                }
                m1 = m1 >> 1;
            }
        }

    }

}
