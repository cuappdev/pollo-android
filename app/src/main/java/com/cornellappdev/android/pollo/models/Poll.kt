package com.cornellappdev.android.pollo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PollResult(val letter: String?, val text: String, val count: Int?): Parcelable

@Parcelize
data class PollChoice(val letter: String?, val text: String): Parcelable

@Parcelize
enum class PollType: Parcelable {
    multipleChoice, freeResponse
}

@Parcelize
enum class PollState: Parcelable {
    live, ended, shared
}

@Parcelize
data class Poll(val createdAt: String?, val updatedAt: String?, var id: Int?,
                val text: String, val answerChoices: ArrayList<PollResult>,
                val type: PollType, val correctAnswer: String?, val userAnswers: MutableMap<String, ArrayList<PollChoice>>?,
                val state: PollState): Parcelable