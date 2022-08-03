import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        levenshtein levenshtein = new levenshtein();
        String similarity = levenshtein.advancedFindSimilarity("토1끼"); // 비교군, 대조군
    }
}

