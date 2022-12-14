import java.io.*;
import java.util.*;

public class levenshtein {
    int getLevenshteinDistance(String X, String Y) {
        int m = X.length();
        int n = Y.length();

        int[][] T = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            T[i][0] = i;
        }
        for (int j = 1; j <= n; j++) {
            T[0][j] = j;
        }

        int cost;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                cost = X.charAt(i - 1) == Y.charAt(j - 1) ? 0 : 1;
                T[i][j] = Integer.min(Integer.min(T[i - 1][j] + 1, T[i][j - 1] + 1),
                        T[i - 1][j - 1] + cost);
            }
        }

        return T[m][n];
    }

    String advancedFindSimilarity(String message) throws IOException {
        WordDisassembler wordDisassembler = new WordDisassembler();
        List<String> curseWordList = new ArrayList<>(); // fword_list가 담길 list

        File file = new File("fword_list.txt"); //파일 객체 생성
        FileReader filereader = new FileReader(file); //입력 스트림 생성
        BufferedReader bufReader = new BufferedReader(filereader); //입력 버퍼 생성

        String line = "";
        while ((line = bufReader.readLine()) != null) { // readLine()은 끝에 개행문자를 읽지 않는다.
            curseWordList.add(line);
        }
        bufReader.close();

        if (message == null) {
            throw new IllegalArgumentException("채팅창에 아무것도 안 치고 엔터쳤음.");
        }

        String[] messageArray = message.split(" "); // 띄어쓰기를 기준으로 분리된 메세지가 담기는 list
        Map<Double, String> allCurseWordsValues = new HashMap<>(); // {비속어 매칭률}, {fword_list의 비속어} map
        List<String> messageToList = new ArrayList<>(); // message가 필터링되어 담길 list

        double potentialRate = 0; // 비속어일 확률 초기값
        for (String eachWord : messageArray) {
            String[] curseWord = new String[eachWord.length()]; // 띄어쓰기를 기준으로 분할된 단어의 길이
            if (eachWord.length() > 0) {
                for (int i = 0; i < curseWordList.size(); i++) {
                    potentialRate = (double) (eachWord.length() - getLevenshteinDistance(eachWord, curseWordList.get(i))) / eachWord.length();
                    allCurseWordsValues.put(potentialRate, curseWordList.get(i));
                }

                Double maxNum = Collections.max(allCurseWordsValues.keySet()); //potentialRate가 가장 높은 값
                System.out.println(eachWord +" + 가 비속어일 1차 확률" + maxNum);

                if (maxNum >= 0.5) { // correlation coefficient r >= 0.5. 비속어일 가능성이 절반 이상이면
                    String maxValue = allCurseWordsValues.get(maxNum); // maxNum의 값의 value

                    String decomposedMaxValue = wordDisassembler.decompose(maxValue); // 초성 중성 종성으로 분해
                    String decomposedInputWord = wordDisassembler.decompose(eachWord); // 초성 중성 종성으로 분해

                    int maxNumBetween = Math.max(decomposedMaxValue.length(), decomposedInputWord.length()); // 분해된 단어의 가장 긴 값
                    int minNumBetween = Math.min(decomposedMaxValue.length(), decomposedInputWord.length()); // 분해된 단어의 가장 짧은 값

                    potentialRate = getLevenshteinDistance(decomposedInputWord, decomposedMaxValue); // 분해된 단어들로 욕설인지 구체적으로 비교
                    double gapBetween = (double) 1 - (potentialRate / maxNumBetween); // 1-b. 검정력(power of test)
                    System.out.println(eachWord +" + 가 비속어일 2차 확률" + gapBetween);

                    if (gapBetween >= 0.66) { // 비속어로 판명나면
                        for (int i = 0; i < eachWord.length(); i++) {
                            curseWord[i] = "*"; // 단어를 별표로 바꿈
                        }
                        StringBuilder filteredWord = new StringBuilder();
                        for (String string : curseWord) {
                            filteredWord.append(string);
                        }
                        messageToList.add(String.valueOf(filteredWord)); // 비속어를 새롭게 만들어진 문장 리스트에 넣어주고

                    } else {
                        return eachWord; // 비속어가 아닌거라면 메시지에서 원래 단어 가져오고
                    }
                } else {
                    messageToList.add(eachWord); // 비속어가 아닌 단어를 문장 리스트에 넣어줌
                }
                allCurseWordsValues.clear(); // 한 단어의 비속어 map 초기화
            }
        }

        StringBuilder filtered = new StringBuilder();
        for (String string : messageToList) {
            filtered.append(string).append(" "); // 단어를 넣어주면서 원래 문장 크기로 복원
        }
        return filtered.toString();
    }
}
