/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet.graphplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Stack;
import jidnet.idnet.Helper;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author sven
 */
public class CreateGraphs {

    private static final int d = 12;
    private static final int m = 2;
    private static final int R = 22400;
    private static final int r = 7;
    private static int drawingIndex = 0;
    private static int[] order;
    private static int pos;

    private static Point getCoords(int index) {
        Point res = new Point();
        res.x = R + (int) Math.round(R * Math.cos(2 * Math.PI * index / (1 << d)));
        res.y = R + (int) Math.round(R * Math.sin(2 * Math.PI * index / (1 << d)));
        return res;
    }

    public static void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 2 * r + 2 * R, 2 * r + 2 * R);

        g.setColor(Color.RED);

        for (int i = 0; i < (1 << d); i++)
            for (int j = 0; j < (1 << d); j++)
                if (Helper.hammingWeight(i ^ j) >= d - m)
                    g.drawLine(getCoords(order[i]).x + r, getCoords(order[i]).y + r, getCoords(order[j]).x + r, getCoords(order[j]).y + r);

        g.setColor(Color.BLUE);
        for (int i = 0; i < (1 << d); i++)
            g.fillArc(getCoords(order[i]).x, getCoords(order[i]).y, 2 * r, 2 * r, 0, 360);
    }

    private static void paint() {
        /*try {
        BufferedImage img = new BufferedImage(2 * r + 2 * R, 2 * r + 2 * R, BufferedImage.TYPE_INT_ARGB);
        paint(img.createGraphics(), order);
        ImageIO.write(img, "png", new File("graphs/g" + drawingIndex + ".png"));
        } catch (IOException io) {
        io.printStackTrace();
        }*/
        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        paint(svgGenerator);
        try {
            Writer out = new FileWriter("graphs/gd" + d + "_" + drawingIndex + ".svg");
            svgGenerator.stream(out, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(drawingIndex);
        drawingIndex++;
    }

    private static void drawRec(int i) {
        order[i] = pos;
        pos++;
        if (pos == (1 << d)) {
            if (Helper.hammingWeight(i) >= d - m)
                paint();
        } else
            for (int j = 0; j < (1 << d); j++)
                if ((order[j] == -1) && (Helper.hammingWeight(i ^ j) >= d - m))
                    drawRec(j);
        order[i] = -1;
        pos--;
        //System.out.println(" -> " + pos);
    }

    public static void main(String[] args) {
        order = new int[1 << d];
        for (int i = 0; i < (1 << d); i++)
            order[i] = -1;
        pos = 0;
        //drawRec(0);

        Stack<Integer> s = new Stack<Integer>();

        int i = 0;
        int tmp = 0;
        boolean found;
        do {
            found = false;
            if (order[i] == -1) {
                order[i] = pos;
                pos++;
            }
            if (pos == (1 << d)) {
                if (Helper.hammingWeight(i) >= d - m)
                    paint();
            } else
                for (int j = tmp; j < (1 << d); j++)
                    if ((order[j] == -1) && (Helper.hammingWeight(i ^ j) >= d - m)) {
                        s.push(i);
                        i = j;
                        found = true;
                        tmp = 0;
                        break;
                    }
            if (!found) {
                tmp = i + 1;
                order[i] = -1;
                pos--;
                i = s.pop();
            }
        } while (!s.empty());

    }
}
