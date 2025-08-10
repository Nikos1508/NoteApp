package com.example.noteapp

import io.ktor.serialization.kotlinx.json.json
import com.example.noteapp.data.Note
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class SupabaseViewModel {
    private val baseUrl = "https://gcxpaqzaqojybawzzuro.supabase.co"
    private val apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjeHBhcXphcW9qeWJhd3p6dXJvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ3OTEwMzEsImV4cCI6MjA3MDM2NzAzMX0.pt_JeDbvJ8OzgYDe1Vhj8nwVInhNY96I67Qft5Vyr2E"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getNotes(): List<Note> {
        return client.get("$baseUrl/rest/v1/notes") {
            header("apikey", apiKey)
            header("Authorization", "Bearer $apiKey")
        }.body()
    }

    suspend fun getNoteById(id: String): Note {
        return client.get("$baseUrl/rest/v1/notes") {
            header("apikey", apiKey)
            header("Authorization", "Bearer $apiKey")
            parameter("id", "eq.$id")
        }.body<List<Note>>().first()
    }

    suspend fun createNote(title: String, htmlContent: String): Note {
        return client.post("$baseUrl/rest/v1/notes") {
            header("apikey", apiKey)
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(listOf(mapOf("title" to title, "content" to htmlContent)))
        }.body<List<Note>>().first()
    }

    suspend fun updateNote(id: String, title: String, htmlContent: String) {
        client.patch("$baseUrl/rest/v1/notes?id=eq.$id") {
            header("apikey", apiKey)
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(mapOf("title" to title, "content" to htmlContent))
        }
    }

    suspend fun deleteNote(id: String) {
        client.delete("$baseUrl/rest/v1/notes?id=eq.$id") {
            header("apikey", apiKey)
            header("Authorization", "Bearer $apiKey")
        }
    }
}
