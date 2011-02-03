package jidnet;


import java.io.FileReader;
import java.io.FileWriter;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sven
 */
public class Test5EntrFix {

    private static String getLine(FileReader fr) throws Exception {
        String res = "";
        int c = fr.read();
        if (c == -1) {
            return null;
        } else if ((char) c == '\n') {
            return res;
        } else {
            res += (char) c;
        }
        while ((char) (c = fr.read()) != '\n') {
            res += (char) c;
        }
        return res;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("h_m3b.txt");
        FileWriter fw = new FileWriter("h_3ok.txt");
        String line;
        double lastHalf = 0;
        int c = 0;
        while ((line = getLine(fr)) != null) {
            if (line == null || line.equals("")) {
                lastHalf = 0;
                c = 0;
                fw.write("\n");
                continue;
            }
            String s1[] = line.split(" ");
            //if ((line = getLine(fr))==null)
            //    return;
            String s2[] = getLine(fr).split(" ");
            int res;
            res = Integer.parseInt(s1[2]);
            res += Integer.parseInt(s2[2]);
            if (c % 2 == 0) {
                String s3[] = getLine(fr).split(" ");
                res += Integer.parseInt(s3[2]) / 2 + Integer.parseInt(s3[2]) % 2;
                lastHalf = Integer.parseInt(s3[2]) / 2;
            } else {
                res += lastHalf;
            }
            fw.write(s1[0] + " " + ((double) c / 200.0) + " " + res +"\n");
            c++;
        }
        fr.close();
        fw.close();
    }
}
