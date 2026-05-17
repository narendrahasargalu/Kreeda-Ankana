package com.kreeda.ankana.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingDto(
    val id: String,
    @SerialName("team_name") val teamName: String,
    val sport: String,
    val date: String,
    val hour: Int,
    @SerialName("device_id") val deviceId: String,
    @SerialName("created_at") val createdAt: Long
)

@Serializable
data class ChallengeDto(
    val id: String,
    @SerialName("team_name") val teamName: String,
    val sport: String,
    @SerialName("preferred_date") val preferredDate: String? = null,
    @SerialName("preferred_hour") val preferredHour: Int? = null,
    val note: String? = null,
    val status: String = "OPEN",
    @SerialName("accepted_by") val acceptedBy: String? = null,
    @SerialName("accepted_at") val acceptedAt: Long? = null,
    @SerialName("device_id") val deviceId: String,
    @SerialName("created_at") val createdAt: Long
)

@Serializable
data class ScoreDto(
    val id: String,
    @SerialName("team_a") val teamA: String,
    @SerialName("team_b") val teamB: String,
    @SerialName("score_a") val scoreA: Int,
    @SerialName("score_b") val scoreB: Int,
    val sport: String,
    val date: String,
    val note: String? = null,
    @SerialName("device_id") val deviceId: String,
    @SerialName("created_at") val createdAt: Long
)
