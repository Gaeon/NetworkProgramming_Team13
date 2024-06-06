package LiarGame;

import java.util.Random;

public class GameTopic {
    static final String TOPIC1 = "동물";
    static final String TOPIC2 = "직업";
    static final String TOPIC3 = "음식";
    static final String TOPIC4 = "랜덤";

    static final String[] TOPICS = {TOPIC1, TOPIC2, TOPIC3, TOPIC4};

    static final String[] WORD1 = {"강아지", "고양이", "토끼", "판다", "기린", "하마", "말", "호랑이", "뱀", "소", "곰", "사자", "사슴", "카피바라", "치타", "라쿤", "나무늘보", "거북이", "미어캣", "오리너구리"};
    static final String[] WORD2 = {"의사", "선생님", "소방관", "간호사", "군인", "기관사", "가수", "만화가", "미술가", "미용사", "엔지니어", "건축가", "배우", "운동선수", "번역가", "변리사", "검사", "판사", "변호사"};
    static final String[] WORD3 = {"파스타", "햄버거", "치킨", "피자", "김치", "된장찌개", "삼겹살", "순대", "떡볶이", "파전", "팥빙수", "삼계탕", "비빔밥", "불고기"};
    static final String[] WORD4 = {"강아지", "고양이", "토끼", "판다", "기린", "의사", "선생님", "소방관", "간호사", "군인", "파스타", "햄버거", "치킨", "피자", "김치"};


    public static String getRandomWord(String gameTopic) {
        Random random = new Random();
        String randomWord = "";

        switch (gameTopic.replaceAll("\"", "")) {
            case TOPIC1:
                randomWord = WORD1[random.nextInt(WORD1.length)];
                break;
            case TOPIC2:
                randomWord = WORD2[random.nextInt(WORD2.length)];
                break;
            case TOPIC3:
                randomWord = WORD3[random.nextInt(WORD3.length)];
                break;
            case TOPIC4:
                randomWord = WORD4[random.nextInt(WORD4.length)];
                break;
        }
        return randomWord;
    }
}
