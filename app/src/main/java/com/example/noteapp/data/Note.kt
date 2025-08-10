package com.example.noteapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)