package com.tttv.thiendinh.breakroid;

import android.provider.BaseColumns;

public class QuizContract {
    private QuizContract() {
    }

    public static class QuestionsTable implements BaseColumns {
        public static final String TABLE_NAME = "quiz_questions";
        public static final String COLUMN_FIRE = "id";
        public static final String COLUMN_QUESTION = "q";
        public static final String COLUMN_OPTION1 = "a";
        public static final String COLUMN_OPTION2 = "b";
        public static final String COLUMN_OPTION3 = "c";
        public static final String COLUMN_OPTION4 = "d";
        public static final String COLUMN_ANSWER_NR = "t";
    }
}
