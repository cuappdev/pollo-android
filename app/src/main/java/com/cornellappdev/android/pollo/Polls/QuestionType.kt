package com.cornellappdev.android.pollo.polls

enum class QuestionType {
    MULTIPLE_CHOICE, FREE_RESPONSE;

    companion object {
        fun create(type: String): QuestionType? {
            return try {
                valueOf(type)
            } catch (error: IllegalArgumentException) {
                null
            }
        }
    }
}