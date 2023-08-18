package diminishingcoloursbot;

import java.awt.Color;

public class Test {
    public static void main(String[] args) {
        var c1 = Color.decode("#ffffff");
        var c2 = Color.decode("#000000");
        var lab1 = ColorConverters.RGBToOkLab(new float[] {1f, 1f, 1f});
        var lab2 = ColorConverters.RGBToOkLab(new float[] {0f, 0f, 0f});
        var avg = ColorConverters.OkLabToRGB(new float[] {(lab1[0] + lab2[0]) / 2, (lab1[1] + lab2[1]) / 2, (lab1[2] + lab2[2]) / 2});
        //System.out.println(Math.sqrt((Math.pow(c1.getRed(), 2) + Math.pow(c2.getRed(), 2)) / 2) + "," + Math.sqrt((Math.pow(c1.getGreen(), 2) + Math.pow(c2.getGreen(), 2)) / 2) + "," + Math.sqrt((Math.pow(c1.getBlue(), 2) + Math.pow(c2.getBlue(), 2.2)) / 2));
        System.out.println(lab1[0] + "," + lab1[1] + "," + lab1[2]);
        System.out.println(lab2[0] + "," + lab2[1] + "," + lab2[2]);
        System.out.println(avg[0] + "," + avg[1] + "," + avg[2]);
        System.out.println(c1.getRed());
    }
}
