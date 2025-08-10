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
    var editTextRef by remember { mutableStateOf<android.widget.EditText?>(null) }

    // Polling every 3s for updates
    LaunchedEffect(noteId) {
        if (noteId != "new") {
            while (true) {
                val note = service.getNoteById(noteId)
                title = note.title
                htmlContent = note.content
                editTextRef?.setText(Html.fromHtml(note.content, Html.FROM_HTML_MODE_COMPACT))
                kotlinx.coroutines.delay(3000)
            }
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Formatting buttons
            Row {
                Button(onClick = {
                    editTextRef?.apply {
                        val start = selectionStart
                        val end = selectionEnd
                        if (start < end) {
                            val selected = text.substring(start, end)
                            text.replace(start, end, Html.fromHtml("<b>$selected</b>", Html.FROM_HTML_MODE_COMPACT))
                            htmlContent = Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        }
                    }
                }) { Text("B") }

                Spacer(Modifier.width(4.dp))

                Button(onClick = {
                    editTextRef?.apply {
                        val start = selectionStart
                        val end = selectionEnd
                        if (start < end) {
                            val selected = text.substring(start, end)
                            text.replace(start, end, Html.fromHtml("<i>$selected</i>", Html.FROM_HTML_MODE_COMPACT))
                            htmlContent = Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        }
                    }
                }) { Text("I") }

                Spacer(Modifier.width(4.dp))

                Button(onClick = {
                    editTextRef?.apply {
                        val start = selectionStart
                        val end = selectionEnd
                        if (start < end) {
                            val selected = text.substring(start, end)
                            text.replace(start, end, Html.fromHtml("<u>$selected</u>", Html.FROM_HTML_MODE_COMPACT))
                            htmlContent = Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        }
                    }
                }) { Text("U") }
            }

            Spacer(Modifier.height(8.dp))

            // Full-screen editable content
            AndroidView(
                factory = { context ->
                    android.widget.EditText(context).apply {
                        setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT))
                        setPadding(16, 16, 16, 16)
                        isSingleLine = false
                        minLines = 10
                        maxLines = Int.MAX_VALUE
                        setHorizontallyScrolling(false)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        editTextRef = this

                        addTextChangedListener(object : android.text.TextWatcher {
                            override fun afterTextChanged(s: android.text.Editable?) {
                                htmlContent = Html.toHtml(s, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            }
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        })
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )

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