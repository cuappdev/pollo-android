package com.cornellappdev.android.pollo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PollResult(val index: Int, val text: String, val count: Int?): Parcelable

@Parcelize
enum class PollState: Parcelable {
    live, ended, shared
}

@Parcelize
data class Poll(val createdAt: String?, val updatedAt: String?, var id: String?,
                val text: String, val answerChoices: ArrayList<PollResult>,
                val correctAnswer: Int, val userAnswers: MutableMap<String, ArrayList<Int>>,
                val state: PollState): Parcelable