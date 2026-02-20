// TODO: Updated WeeklyTodoScreen with editing, deletion, week navigation, and Monday start

package com.odevs.photodiary.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.odevs.photodiary.notifications.ReminderScheduler
import com.odevs.photodiary.ui.LanguageProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.time.LocalDateTime
import java.time.LocalTime
import com.odevs.photodiary.datastore.NotificationPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val Context.todoDataStore by preferencesDataStore("todo_data")

val colors = listOf(
    Color(0xFFFFE0B2), Color(0xFFBBDEFB), Color(0xFFFFCDD2)
)

val fontCormorant = FontFamily(Font(com.odevs.photodiary.R.font.cormorantgaramond_variablefont_wght))

data class HourlyTodo(
    val id: String = UUID.randomUUID().toString(),
    val time: String,
    val title: String,
    val note: String = "",
    val colorIndex: Int = 0,
    val done: Boolean = false
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WeeklyTodoScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val todoMap = remember { mutableStateMapOf<LocalDate, MutableList<HourlyTodo>>() }
    val language by remember { derivedStateOf { LanguageProvider.language } }
    var editingOriginalDate by remember { mutableStateOf<LocalDate?>(null) }

    val previousWeek = if (language == "hu") "Előző hét" else "Previous Week"
    val nextWeek = if (language == "hu") "Következő hét" else "Next Week"
    val noTodo = if (language == "hu") "Nincs teendő" else "No tasks"
    val newTask = if (language == "hu") "Új teendő" else "New Task"
    val save = if (language == "hu") "Mentés" else "Save"
    val cancel = if (language == "hu") "Mégse" else "Cancel"
    val newTaskTitle = if (language == "hu") "Új teendő" else "New Task"
    val taskLabel = if (language == "hu") "Feladat" else "Task"
    val noteLabel = if (language == "hu") "Jegyzet" else "Note"
    val pickTime = if (language == "hu") "Válassz időt" else "Pick time"
    val pickDate = if (language == "hu") "Válassz dátumot" else "Pick date"

    var currentStartDate by remember { mutableStateOf(LocalDate.now().with(java.time.DayOfWeek.MONDAY)) }
    val allDates = remember(currentStartDate) {
        List(7) { currentStartDate.plusDays(it.toLong()) }
    }

    var dialogOpen by remember { mutableStateOf(false) }
    var newText by remember { mutableStateOf("") }
    var newNote by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("08:00") }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var editingId by remember { mutableStateOf<String?>(null) }

    val isTaskNotificationEnabled by NotificationPreferences.isTaskNotificationEnabled(context)
        .collectAsStateWithLifecycle(initialValue = true)

    fun saveTodosForDate(date: LocalDate) {
        scope.launch {
            context.todoDataStore.edit { prefs ->
                val items = todoMap[date] ?: mutableListOf()
                val key = stringPreferencesKey(date.toString())
                val json = JSONArray().apply {
                    items.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("time", it.time)
                            put("title", it.title)
                            put("note", it.note)
                            put("colorIndex", it.colorIndex)
                            put("done", it.done)
                        })
                    }
                }
                prefs[key] = json.toString()
            }
        }
    }

    fun loadTodos() {
        scope.launch {
            val prefs = context.todoDataStore.data.first()
            allDates.forEach { date ->
                val key = stringPreferencesKey(date.toString())
                val list = prefs[key]?.let {
                    val arr = JSONArray(it)
                    MutableList(arr.length()) { i ->
                        val obj = arr.getJSONObject(i)
                        HourlyTodo(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            time = obj.optString("time", "08:00"),
                            title = obj.optString("title", if (language == "hu") "Névtelen feladat" else "Untitled task"),
                            note = obj.optString("note", ""),
                            colorIndex = obj.optInt("colorIndex", 0),
                            done = obj.optBoolean("done", false)
                        )
                    }
                } ?: mutableListOf()
                todoMap[date] = list
            }
        }
    }

    LaunchedEffect(currentStartDate) { loadTodos() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp).background(Color(0xFFFDFBF6))) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { currentStartDate = currentStartDate.minusWeeks(1) }, modifier = Modifier.weight(1f)) {
                Text(previousWeek)
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { currentStartDate = currentStartDate.plusWeeks(1) }, modifier = Modifier.weight(1f)) {
                Text(nextWeek)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (language == "hu") "📌 Tipp: Hosszan nyomva szerkesztheted a feladatot!" else "📌 Tip: Long press to edit a task!",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allDates) { date ->
                val formatter = DateTimeFormatter.ofPattern("EEEE - yyyy.MM.dd", if (language == "hu") Locale("hu") else Locale.ENGLISH)
                Text(
                    text = date.format(formatter),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Color(0xFF4B3832),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val items = todoMap[date]?.sortedBy { it.time } ?: emptyList()
                if (items.isEmpty()) {
                    Text(noTodo, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                items.forEach { item ->
                    val backgroundColor = if (item.done) Color.LightGray else colors[item.colorIndex % colors.size]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        newText = item.title
                                        newNote = item.note
                                        selectedTime = item.time
                                        selectedDay = date
                                        editingId = item.id
                                        editingOriginalDate = date
                                        dialogOpen = true
                                    }
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = {
                                    val updatedItem = item.copy(done = it)
                                    todoMap[date] = todoMap[date]?.map { t -> if (t.id == item.id) updatedItem else t }?.toMutableList() ?: mutableListOf()
                                    saveTodosForDate(date)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${item.time} - ${item.title}", fontWeight = FontWeight.SemiBold)
                                if (item.note.isNotBlank()) {
                                    Text(item.note, fontSize = 13.sp)
                                }
                            }
                            IconButton(onClick = {
                                val currentList = todoMap[date]?.toMutableList()
                                currentList?.removeIf { it.id == item.id }
                                todoMap[date] = currentList ?: mutableListOf()
                                saveTodosForDate(date)
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = {
            newText = ""
            newNote = ""
            selectedTime = "08:00"
            selectedDay = LocalDate.now()
            editingId = null
            dialogOpen = true
        }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(newTask)
        }
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = {
                dialogOpen = false
                newText = ""
                newNote = ""
                selectedTime = "08:00"
                selectedDay = LocalDate.now()
                editingId = null
                editingOriginalDate = null
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newText.isNotBlank()) {
                        val newItem = HourlyTodo(
                            id = editingId ?: UUID.randomUUID().toString(),
                            time = selectedTime,
                            title = newText,
                            note = newNote,
                            colorIndex = (0..2).random()
                        )

                        val updatedList = todoMap[selectedDay]?.toMutableList() ?: mutableListOf()

                        if (editingId != null) {
                            val index = updatedList.indexOfFirst { it.id == editingId }
                            if (index != -1) {
                                updatedList[index] = newItem
                            } else {
                                updatedList.add(newItem)
                            }
                        } else {
                            updatedList.add(newItem)
                        }

                        todoMap[selectedDay] = updatedList
                        saveTodosForDate(selectedDay)

                        val localTime = LocalTime.parse(selectedTime)
                        val dateTime = LocalDateTime.of(selectedDay, localTime)
                        if (isTaskNotificationEnabled) {
                            ReminderScheduler.scheduleTaskReminder(context, newItem, dateTime)
                        }

                        newText = ""
                        newNote = ""
                        selectedTime = "08:00"
                        selectedDay = LocalDate.now()
                        editingId = null
                        editingOriginalDate = null
                        dialogOpen = false
                    }
                },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                ) {
                    Text(save)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (editingId != null && editingOriginalDate != null) {
                            val restoredItem = HourlyTodo(
                                id = editingId!!,
                                time = selectedTime,
                                title = newText,
                                note = newNote,
                                colorIndex = (0..2).random()
                            )
                            val originalList = todoMap[editingOriginalDate!!]?.toMutableList() ?: mutableListOf()
                            if (originalList.none { it.id == restoredItem.id }) {
                                originalList.add(restoredItem)
                                todoMap[editingOriginalDate!!] = originalList
                                saveTodosForDate(editingOriginalDate!!)
                            }
                        }
                        dialogOpen = false
                        newText = ""
                        newNote = ""
                        selectedTime = "08:00"
                        selectedDay = LocalDate.now()
                        editingId = null
                        editingOriginalDate = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                ) {
                    Text(cancel)
                }
            },
            title = { Text(newTaskTitle) },
            text = {
                Column {
                    TextField(value = newText, onValueChange = { newText = it }, label = { Text(taskLabel) })
                    TextField(value = newNote, onValueChange = { newNote = it }, label = { Text(noteLabel) })
                    Button(
                        onClick = {
                            val timeParts = selectedTime.split(":")
                            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                            TimePickerDialog(context, { _, h, m ->
                                selectedTime = String.format("%02d:%02d", h, m)
                            }, hour, minute, true).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7))
                    ) {
                        Text("$pickTime: $selectedTime", color = Color(0xFF3E4E3E))
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val today = LocalDate.now()
                            DatePickerDialog(context, { _, y, m, d ->
                                selectedDay = LocalDate.of(y, m + 1, d)
                            }, today.year, today.monthValue - 1, today.dayOfMonth).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7))
                    ) {
                        Text("$pickDate: ${selectedDay.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}", color = Color(0xFF3E4E3E))
                    }
                }
            }
        )
    }
}
