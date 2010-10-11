/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet.graphplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.JPanel;
import jidnet.idnet.Helper;

/**
 *
 * @author sven
 */
public class GraphPlotPanel extends JPanel {

    private ArrayList<Integer> order = new ArrayList<Integer>();
    private static final int d = 6;
    private static final int m = 2;
    private static final int R = 320;
    private static final int r = 7;

    public GraphPlotPanel() {
        super();

        try {
            FileReader f = new FileReader("jIdNet/order.txt");
            while (f.ready()) {
                String s = "";
                char c;
                while ((c = (char) f.read()) != '\n')
                    s += c;
                if (s.length() > 0) {
                    int i = Integer.parseInt(s.substring(0, d), 2);
                    order.add(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Point getCoords(int index) {
        Point res = new Point();
        res.x = r + R + (int) Math.round(R * Math.cos(2 * Math.PI * index / (1 << d)));
        res.y = r + R + (int) Math.round(R * Math.sin(2 * Math.PI * index / (1 << d)));
        return res;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);

        for (int i = 0; i < (1 << d); i++) {
            for (int j = 0; j < (1 << d); j++) {
                if (Helper.hammingWeight(i ^ j) >= d-m)
                    g.drawLine(getCoords(order.indexOf(i)).x+r, getCoords(order.indexOf(i)).y+r, getCoords(order.indexOf(j)).x+r, getCoords(order.indexOf(j)).y+r);
            }
        }

        g.setColor(Color.BLUE);
        for (int i = 0; i < (1 << d); i++)
            g.fillArc(getCoords(order.indexOf(i)).x, getCoords(order.indexOf(i)).y, 2 * r, 2 * r, 0, 360);
    }
}
