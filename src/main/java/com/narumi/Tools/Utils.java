package com.narumi.Tools;

public class Utils {
    public static String getVersionString(int xyz) {
        int z = xyz % 10;
        int y = (xyz / 10) % 10;
        int x = (xyz / 100) % 10;

        return "v" + x + "." + y + "." + z;
    }
}
