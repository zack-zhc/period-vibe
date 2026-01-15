package com.example.periodvibe.domain.model

enum class Symptom(
    val displayName: String,
    val icon: String,
    val category: SymptomCategory
) {
    ABDOMINAL_PAIN("腹痛", "abdominal_pain", SymptomCategory.PHYSICAL),
    LOWER_BACK_PAIN("腰痛", "lower_back_pain", SymptomCategory.PHYSICAL),
    BREAST_TENDERNESS("乳房胀痛", "breast_tenderness", SymptomCategory.PHYSICAL),
    HEADACHE("头痛", "headache", SymptomCategory.PHYSICAL),
    FATIGUE("疲劳", "fatigue", SymptomCategory.PHYSICAL),
    MOOD_SWINGS("情绪波动", "mood_swings", SymptomCategory.EMOTIONAL),
    IRRITABILITY("易怒", "irritability", SymptomCategory.EMOTIONAL),
    ANXIETY("焦虑", "anxiety", SymptomCategory.EMOTIONAL),
    BLOATING("腹胀", "bloating", SymptomCategory.PHYSICAL),
    NAUSEA("恶心", "nausea", SymptomCategory.PHYSICAL),
    ACNE("痘痘", "acne", SymptomCategory.PHYSICAL),
    INSOMNIA("失眠", "insomnia", SymptomCategory.EMOTIONAL),
    APPETITE_CHANGES("食欲变化", "appetite_changes", SymptomCategory.PHYSICAL),
    OTHER("其他", "other", SymptomCategory.OTHER);

    enum class SymptomCategory {
        PHYSICAL, EMOTIONAL, OTHER
    }
}
