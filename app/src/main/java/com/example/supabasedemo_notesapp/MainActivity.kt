package com.example.supabasedemo_notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.supabasedemo_notesapp.ui.theme.SupabaseDemo_NotesAppTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


val supabase = createSupabaseClient(
    supabaseUrl = "",
    supabaseKey = ""
) {
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupabaseDemo_NotesAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesList()
                }
            }
        }
    }
}

@Serializable
data class Note (
    val id: Int,
    val content: String,
    val author: String
)

@Composable
fun NotesList() {
    val notes = remember { mutableStateListOf<Note>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val results = supabase.from("notes").select().decodeList<Note>()
            notes.addAll(results)
        }
    }

    Column {
        LazyColumn {
            items(notes) { note ->
                ListItem(headlineContent = {
                    Column {
                        Text(text = note.id.toString())
                        Text(text = note.content)
                        Text(text = note.author)
                    }
                })
            }
        }
        var newNote by remember { mutableStateOf("") }
        val composableScope = rememberCoroutineScope()

        Row {
            OutlinedTextField(value = newNote, onValueChange = {newNote = it})
            Button(onClick = {
                composableScope.launch(Dispatchers.IO) {
                    val note = supabase.from("notes").insert(mapOf("content" to newNote)) {
                        select()
                        single()
                    }.decodeAs<Note>()
                    notes.add(note)
                    newNote = ""
                }
            }) {
                Text("Save")
            }
        }
    }
}
