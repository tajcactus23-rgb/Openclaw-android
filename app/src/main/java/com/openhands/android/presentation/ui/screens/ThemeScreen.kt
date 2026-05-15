package com.openhands.android.presentation.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class ThemeConfig(
    val name: String,
    val primary: Long,
    val secondary: Long,
    val background: Long,
    val surface: Long,
    val onPrimary: Long,
    val onSurface: Long
) {
    fun toJson(): String = JSONObject().apply {
        put("name", name)
        put("primary", "#" + primary.toString(16).padStart(6, '0'))
        put("secondary", "#" + secondary.toString(16).padStart(6, '0'))
        put("background", "#" + background.toString(16).padStart(6, '0'))
        put("surface", "#" + surface.toString(16).padStart(6, '0'))
        put("onPrimary", "#" + onPrimary.toString(16).padStart(6, '0'))
        put("onSurface", "#" + onSurface.toString(16).padStart(6, '0'))
    }.toString(2)

    fun copy(name: String): ThemeConfig = ThemeConfig(name, primary, secondary, background, surface, onPrimary, onSurface)

    companion object {
        fun fromJson(json: String): ThemeConfig {
            val j = JSONObject(json)
            fun parse(key: String): Long = try { 
                java.lang.Long.parseLong(j.optString(key, "2196F3").trimStart('#'), 16) 
            } catch (e: Exception) { 0xFF2196F3 }
            return ThemeConfig(j.optString("name", "Custom"), parse("primary"), parse("secondary"),
                parse("background"), parse("surface"), parse("onPrimary"), parse("onSurface"))
        }
    }
}

val DEFAULT = ThemeConfig("Default", 0xFF2196F3, 0xFF4CAF50, 0xFFFFFFFF, 0xFFF5F5F5, 0xFFFFFFFF, 0xFF000000)

// SECTION 13: THEME - Full JSON with save/load/import/export
@Composable
fun ThemeScreen() {
    val ctx = LocalContext.current
    var saved by remember { mutableStateOf(listOf(DEFAULT)) }
    var json by remember { mutableStateOf("") }
    var current by remember { mutableStateOf(DEFAULT) }
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    fun save(t: ThemeConfig) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val f = File(ctx.filesDir, "themes/${t.name}.json")
                f.parentFile?.mkdirs()
                withContext(Dispatchers.IO) { f.writeText(t.toJson()) }
                status = "Saved: ${t.name}"
                saved = saved + t
            } catch (e: Exception) { status = "Error" }
        }
    }

    fun apply(t: ThemeConfig) { current = t; status = "Applied" }
    fun delete(t: ThemeConfig) { if (t.name != "Default") { saved = saved - t; status = "Deleted" } }

    val loadPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                val j = ctx.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                if (j != null) { val t = ThemeConfig.fromJson(j); name = t.name; json = j; status = "Loaded" }
            } catch (e: Exception) { status = "Error" }
        }
    }

    val savePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            try { ctx.contentResolver.openOutputStream(it)?.write(json.toByteArray()); status = "Exported" } 
            catch (e: Exception) { status = "Error" }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Theme Engine", style = MaterialTheme.typography.headlineMedium)
        Text("Saved Themes", style = MaterialTheme.typography.titleMedium)
        saved.forEach { t ->
            Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(Modifier.fillMaxWidth().padding(8.dp), Arrangement.SpaceBetween) {
                    Row {
                        Box(Modifier.size(20.dp).clip(CircleShape).background(Color(t.primary)))
                        Box(Modifier.size(20.dp).clip(CircleShape).background(Color(t.secondary)).padding(start=2.dp))
                        Text(t.name, Modifier.padding(start=8.dp))
                    }
                    Row {
                        Button(onClick = { apply(t) }, modifier = Modifier.size(50.dp)) { Icon(Icons.Default.Palette, null, Modifier.size(16.dp)) }
                        if (t.name != "Default") { IconButton(onClick = { delete(t) }) { Icon(Icons.Default.Delete, "Del", Modifier.size(16.dp)) } }
                    }
                }
            }
        }

        Text("Preview", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.padding(8.dp)) {
                Box(Modifier.fillMaxWidth().background(Color(current.primary)).padding(8.dp)) { Text("Primary", color=Color(current.onPrimary)) }
                Box(Modifier.fillMaxWidth().background(Color(current.secondary)).padding(8.dp)) { Text("Secondary", color=Color(current.onPrimary)) }
                Box(Modifier.fillMaxWidth().background(Color(current.background)).padding(8.dp)) { Text("Background", color=Color(current.onSurface)) }
            }
        }

        Text("JSON Editor", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = json, onValueChange = { json = it }, label = { Text("JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 4)

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(4.dp)) {
            Button(onClick = { if(name.isNotBlank()&&json.isNotBlank()) save(ThemeConfig.fromJson(json).copy(name = name)) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Save,null); Text("Save") }
            Button(onClick = { loadPicker.launch(arrayOf("application/json")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Upload,null); Text("Load") }
        }
        Button(onClick = { json = current.toJson(); name = current.name }, modifier = Modifier.fillMaxWidth()) { Text("Export") }
        Button(onClick = { if(json.isNotBlank()) savePicker.launch("$name.json") }, modifier = Modifier.fillMaxWidth(), enabled = json.isNotBlank()) { Text("Export File") }

        Text("Live Apply", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) { Text("ADAPTER_REQUIRED - ThemeProvider", Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0)) }

        if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        Spacer(Modifier.padding(16.dp))
    }
}