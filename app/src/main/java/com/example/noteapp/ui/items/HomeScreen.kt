package com.example.noteapp.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noteapp.SupabaseViewModel
import com.example.noteapp.data.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navToNote: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val service = remember { SupabaseViewModel() }
    var notes by remember { mutableStateOf(listOf<Note>()) }

    LaunchedEffect(Unit) {
        scope.launch {
            notes = service.getNotes()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navToNote("new") }) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        },
        topBar = {
            TopAppBar(title = { Text("My Notes") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(notes.size) { index ->
                val note = notes[index]
                Text(
                    text = note.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navToNote(note.id) }
                        .padding(16.dp)
                )
            }
        }
    }
}
