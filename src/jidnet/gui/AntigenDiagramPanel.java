package jidnet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import javax.swing.JPanel;
import jidnet.idnet.Antigen;

public class AntigenDiagramPanel extends JPanel implements Observer {

    private Stack<Antigen> antigens;
    private Stack<double[]> history;
    final static int historySize = 1000;

    public AntigenDiagramPanel() {
        super();
        Application.getIdnetManager().addObserver(this);
        history = new Stack<double[]>();
        antigens = new Stack<Antigen>();
    }

    public void update(Observable o, Object arg) {
        if (!arg.equals("iteration") || antigens == null) {
            return;

        }
        for (int i = 0; i < antigens.size(); i++) {
            history.get(i)[Application.getIdnetManager().gett() % historySize] =
                    antigens.get(i).getX();

        }
    }

    public void addAntigen(Antigen antigen) {
        antigens.push(antigen);
        history.push(new double[historySize]);
    }

    public void removeAntigen() {
        if (antigens.empty())
            return;
        antigens.pop().kill();
        history.pop();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int height = 400;
        int x_offset = 20;
        int y_offset = 20;
        final double max = 1.0;

        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset, y_offset, x_offset, y_offset + height);
        g.drawLine(x_offset, y_offset, x_offset + historySize, y_offset);
        g.drawLine(x_offset, y_offset + height, x_offset + historySize, y_offset + height);
        g.drawLine(x_offset + historySize, y_offset, x_offset + historySize, y_offset + height);

        g.setColor(Color.RED);
        int y = y_offset + height;
        int old_y = y;
        for (int j = 0; j < antigens.size(); j++) {
            for (int i = 0; i < historySize; i++) {
                y = y_offset + height - (int) Math.round(height * history.get(j)[i] / max);
                g.drawLine(x_offset + i, old_y, x_offset + i, y);
                old_y = y;
            }
        }
        g.setColor(Color.getHSBColor(0.3f, 0.3f, 0.3f));
        g.drawLine(x_offset + Application.getIdnetManager().gett() % historySize + 1, y_offset,
                x_offset + Application.getIdnetManager().gett() % historySize + 1, y_offset + height);

    }
}
