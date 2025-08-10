package com.example.noteapp.ui.items

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.noteapp.SupabaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(noteId: String, navBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val service = remember { SupabaseViewModel() }

    var title by remember { mutableStateOf("") }
    var htmlContent by remember { mutableStateOf("") }
    var editTextRef by remember { mutableStateOf<android.widget.EditText?>(null) }

    var lastServerUpdate by remember { mutableStateOf<String?>(null) }
    var hasLocalChanges by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        if (noteId != "new") {
            val initialNote = service.getNoteById(noteId)
            title = initialNote.title
            htmlContent = initialNote.content ?: ""
            lastServerUpdate = initialNote.updated_at

            while (true) {
                if (hasLocalChanges) {
                    service.updateNote(noteId, title, htmlContent)
                    hasLocalChanges = false
                }

                val remoteNote = service.getNoteById(noteId)

                if (remoteNote.updated_at != null && (lastServerUpdate == null || remoteNote.updated_at > lastServerUpdate!!)) {
                    title = remoteNote.title
                    htmlContent = remoteNote.content ?: ""
                    lastServerUpdate = remoteNote.updated_at
                    hasLocalChanges = false
                }

                delay(3000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new") "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = navBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = {
                    title = it
                    hasLocalChanges = true
                },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    android.widget.EditText(context).apply {
                        setPadding(16, 16, 16, 16)
                        isSingleLine = false
                        minLines = 10
                        maxLines = Int.MAX_VALUE
                        setHorizontallyScrolling(false)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        gravity = android.view.Gravity.TOP
                        editTextRef = this

                        addTextChangedListener(object : android.text.TextWatcher {
                            override fun afterTextChanged(s: android.text.Editable?) {
                                if (s != null) {
                                    val newHtml = Html.toHtml(
                                        s,
                                        Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                                    )
                                    if (htmlContent != newHtml) {
                                        htmlContent = newHtml
                                        hasLocalChanges = true
                                    }
                                }
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        })
                    }
                },
                update = { editText ->
                    val htmlText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
                    if (editText.text.toString() != htmlText.toString()) {
                        editText.setText(htmlText)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        editTextRef?.apply {
                            val start = selectionStart
                            val end = selectionEnd
                            if (start < end) {
                                val selected = text.substring(start, end)
                                text.replace(
                                    start, end,
                                    Html.fromHtml("<b>$selected</b>", Html.FROM_HTML_MODE_COMPACT)
                                )
                                htmlContent = Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("B", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                TextButton(
                    onClick = {
                        editTextRef?.apply {
                            val start = selectionStart
                            val end = selectionEnd
                            if (start < end) {
                                val selected = text.substring(start, end)
                                text.replace(
                                    start, end,
                                    Html.fromHtml("<i>$selected</i>", Html.FROM_HTML_MODE_COMPACT)
                                )
                                htmlContent = Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("I", fontStyle = FontStyle.Italic, fontSize = 20.sp)
                }

                TextButton(
                    onClick = {
                        editTextRef?.apply {
                            val start = selectionStart
                            val end = selectionEnd
                            if (start < end) {
                                val selected = text.substring(start, end)
                                text.replace(
                                    start, end,
                                    Html.fromHtml("<u>$selected</u>", Html.FROM_HTML_MODE_COMPACT)
                                )
                                htmlContent = Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("U", textDecoration = TextDecoration.Underline, fontSize = 20.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            ElevatedButton(
                onClick = {
                    scope.launch {
                        if (noteId == "new") {
                            service.createNote(title, htmlContent)
                        } else {
                            service.updateNote(noteId, title, htmlContent)
                        }
                        navBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Save",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}
