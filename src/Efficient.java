import java.awt.*;
import java.io.*;
import java.util.HashSet;

public class Efficient {
    private static HashSet<Point> points;
    private static Runtime rt = Runtime.getRuntime();
    public static void main(String[] args) {
            try {
                double beforeMem = (rt.totalMemory() - rt.freeMemory() );
                double start = System.currentTimeMillis();
                String filename = args[0];
                StringGenerator stringGenerator = new StringGenerator(filename);
                PenaltyData penaltyData = new PenaltyData();
                String str1 = stringGenerator.getStr1();
                String str2 = stringGenerator.getStr2();
                points = new HashSet<>();
                String[] alignedStrDC;
                if (str1.length() <= str2.length()) {
                    alignDC(str1, 0, str1.length(), str2, 0, str2.length(), penaltyData);
                    alignedStrDC = findAlignDC(str1, str2);
                }
                else {
                    alignDC(str2, 0, str2.length(), str1, 0, str1.length(), penaltyData);
                    alignedStrDC = findAlignDC(str2, str1);
                }
                double end = System.currentTimeMillis();
                double afterMem = (rt.totalMemory() - rt.freeMemory() );
                double usedTime = (end - start)/1000.0;
                double usedMem = (afterMem-beforeMem)/(1024.0);
                File file = new File("output_Efficient.txt");
                BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                if (str1.length() <= str2.length()) {
                    bw.write(alignedStrDC[0].substring(0, 50));
                    bw.append(" ");
                    bw.append(alignedStrDC[0].substring(alignedStrDC[0].length() - 50) + "\n");
                    bw.append(alignedStrDC[1].substring(0, 50));
                    bw.append(" ");
                    bw.append(alignedStrDC[1].substring(alignedStrDC[1].length() - 50) + "\n");
                    bw.append(String.format("%d", findPenaltyDC(str1, str2, penaltyData)) + "\n");
                }
                else {
                    bw.write(alignedStrDC[1].substring(0, 50));
                    bw.append(" ");
                    bw.append(alignedStrDC[1].substring(alignedStrDC[1].length() - 50) + "\n");
                    bw.append(alignedStrDC[0].substring(0, 50));
                    bw.append(" ");
                    bw.append(alignedStrDC[0].substring(alignedStrDC[0].length() - 50) + "\n");
                    bw.append(String.format("%d", findPenaltyDC(str2, str1, penaltyData)) + "\n");
                }
                bw.append(String.format("%.4f",usedTime)+" s\n");
                bw.append(String.format("%.4f",usedMem)+" KB\n");
                bw.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + e.getMessage());
            } catch (BadDataException e) {
                System.out.println("Bad Data: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private static int[][] align(String str1, String str2, PenaltyData penaltyData) throws BadDataException{
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for(int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i * penaltyData.getGapPenalty();
        }
        for(int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j * penaltyData.getGapPenalty();
        }
        for(int j = 1; j <= str2.length(); j++) {
            for(int i = 1; i <= str1.length(); i++) {
                dp[i][j] = Math.min(dp[i - 1][j - 1] + penaltyData.getMismatchPenalty(str1.charAt(i - 1),
                        str2.charAt(j - 1)), Math.min(dp[i][j - 1] + penaltyData.getGapPenalty(),
                        dp[i - 1][j] + penaltyData.getGapPenalty()));
            }
        }
        return dp;
    }


    private static int[] alignSpaceEfficient(String str1, String str2, PenaltyData penaltyData) throws BadDataException {
        int[][] dp = new int[str1.length() + 1][2];
        for(int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i * penaltyData.getGapPenalty();
        }
        for(int j = 1; j <= str2.length(); j++) {
            dp[0][1] = j * penaltyData.getGapPenalty();
            for(int i = 1; i <= str1.length(); i++) {
                dp[i][1] = Math.min(dp[i - 1][0] + penaltyData.getMismatchPenalty(str1.charAt(i - 1),
                        str2.charAt(j - 1)), Math.min(dp[i][0] + penaltyData.getGapPenalty(),
                        dp[i - 1][1] + penaltyData.getGapPenalty()));
            }
            for(int i = 0; i <= str1.length(); i++) {
                dp[i][0] = dp[i][1];
            }
        }
        int[] last = new int[str1.length() + 1];
        for(int i = 0; i < last.length; i++) {
            last[i] = dp[i][1];
        }
        return last;

    }



    private static void alignDC(String str1, int first1, int last1,
                                String str2, int first2, int last2, PenaltyData penaltyData)
            throws BadDataException {
        if(last1 - first1 <= 2 || last2 - first2 <= 2) {
            int[][] dp = align(str1.substring(first1, last1), str2.substring(first2, last2), penaltyData);
            findAlignE(dp, str1, last1, str2, last2, penaltyData);
            return;
        }
        int[] f = alignSpaceEfficient(str1.substring(first1, last1), str2.substring(first2, (first2 + last2) / 2 + 1), penaltyData);
        int[] g = alignSpaceEfficient(new StringBuilder(str1.substring(first1,last1)).reverse().toString(),
                new StringBuilder(str2.substring((first2 + last2) / 2+1,last2)).reverse().toString(), penaltyData);
        int q = 0;
        int min = f[0] + g[f.length - 1];
        for(int i = 0; i < f.length; i++) {
            if(f[i] + g[f.length - i - 1] < min) {
                min = f[i] + g[f.length - i - 1];
                q = i;
            }
        }
        q = q + first1;
        points.add(new Point(q, (first2 + last2) / 2 + 1));
        alignDC(str1, first1, q, str2, first2, (first2 + last2) / 2 + 1, penaltyData);
        alignDC(str1, q, last1, str2, (first2 + last2) / 2 + 1, last2, penaltyData);
    }

    private static void findAlignE(int[][] dp, String str1, int last1,
                                   String str2, int last2, PenaltyData penaltyData)
            throws BadDataException {
        int i = dp.length - 1;
        int j = dp[0].length - 1;
        points.add(new Point(last1, last2));
        while(i > 0 && j > 0) {
            if(dp[i][j] == dp[i - 1][j - 1] + penaltyData.getMismatchPenalty(str1.charAt(last1 - 1),
                    str2.charAt(last2 - 1))) {
                points.add(new Point(last1 - 1, last2 - 1));
                i--; last1--;
                j--; last2--;
            }
            else if(dp[i][j] == dp[i][j - 1] + penaltyData.getGapPenalty()) {
                points.add(new Point(last1, last2 - 1));
                j--; last2--;
            }
            else if(dp[i][j] == dp[i - 1][j] + penaltyData.getGapPenalty()) {
                points.add(new Point(last1 - 1, last2));
                i--; last1--;
            }
        }
        if(i == 0) {
            while(j > 0) {
                points.add(new Point(last1, last2 - 1));
                j--; last2--;
            }
        }
        if(j == 0) {
            while(i > 0) {
                points.add(new Point(last1 - 1, last2));
                i--; last1--;
            }
        }
    }

    private static String[] findAlignDC(String str1, String str2) {
        int i = 0;
        int j = 0;
        StringBuilder str1ABuilder = new StringBuilder();
        StringBuilder str2ABuilder = new StringBuilder();
        while(i < str1.length() || j < str2.length()) {
            if (points.contains(new Point(i + 1, j + 1))) {
                if (!points.contains(new Point(i, j + 1)) && !points.contains(new Point(i + 1, j))) {
                    str1ABuilder.append(str1.charAt(i));
                    str2ABuilder.append(str2.charAt(j));
                    i++;
                    j++;
                }
            }
            if (points.contains(new Point(i, j + 1))) {
                str1ABuilder.append('_');
                str2ABuilder.append(str2.charAt(j));
                j++;
            }
            if (points.contains(new Point(i + 1, j))) {
                str1ABuilder.append(str1.charAt(i));
                str2ABuilder.append('_');
                i++;
            }
        }
        return new String[]{str1ABuilder.toString(),str2ABuilder.toString()};
    }
    private static int findPenaltyDC(String str1, String str2, PenaltyData penaltyData) throws BadDataException {
        int penalty = 0;
        int i = 0;
        int j = 0;
        while (i < str1.length() || j < str2.length()) {
            if (points.contains(new Point(i + 1, j + 1))) {
                if (!points.contains(new Point(i, j + 1)) && !points.contains(new Point(i + 1, j))) {
                    penalty += penaltyData.getMismatchPenalty(str1.charAt(i), str2.charAt(j));
                    i++;
                    j++;
                }
            }
            if (points.contains(new Point(i, j + 1))) {
                penalty += penaltyData.getGapPenalty();
                j++;
            }
            if (points.contains(new Point(i + 1, j))) {
                penalty += penaltyData.getGapPenalty();
                i++;
            }
        }
        return penalty;
    }

}
