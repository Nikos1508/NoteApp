package com.example.noteapp.ui.items

import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.noteapp.SupabaseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(noteId: String, navBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val service = remember { SupabaseViewModel() }
    var title by remember { mutableStateOf("") }
    var htmlContent by remember { mutableStateOf("") }

    LaunchedEffect(noteId) {
        if (noteId != "new") {
            val note = service.getNoteById(noteId)
            title = note.title
            htmlContent = note.content
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new") "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = navBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Formatting buttons
            Row {
                Button(onClick = { htmlContent += "<b>Bold</b>" }) { Text("B") }
                Spacer(Modifier.width(4.dp))
                Button(onClick = { htmlContent += "<i>Italic</i>" }) { Text("I") }
                Spacer(Modifier.width(4.dp))
                Button(onClick = { htmlContent += "<u>Underline</u>" }) { Text("U") }
            }

            Spacer(Modifier.height(8.dp))

            // Preview HTML
            AndroidView(factory = { context ->
                TextView(context).apply {
                    text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
                }
            })

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                scope.launch {
                    if (noteId == "new") {
                        service.createNote(title, htmlContent)
                    } else {
                        service.updateNote(noteId, title, htmlContent)
                    }
                    navBack()
                }
            }) {
                Text("Save")
            }
        }
    }
}
