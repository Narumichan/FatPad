package com.narumi.Tools;

import java.awt.*;

public class Utils {
    private Utils(){}
    public static String getVersionString(int xyz) {
        int z = xyz % 10;
        int y = (xyz / 10) % 10;
        int x = (xyz / 100) % 10;

        return "v" + x + "." + y + "." + z;
    }

    public static String toHexString(Color color) {
        return String.format("#%06x", color.getRGB() & 0xFFFFFF);
    }
}
