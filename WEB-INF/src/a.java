import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String str = in.nextLine();

        int count =0;
        for (int i = 0; i < str.length();i++) {
            if (str.charAt(i) == (' ')){
                count= count+1;
            }else if (str.charAt(i) == '!'){
                count= count+2;
            }else if (str.charAt(i) == '.'){
                count= count+2;
            }
        }

        System.out.println(count);
    }
}

