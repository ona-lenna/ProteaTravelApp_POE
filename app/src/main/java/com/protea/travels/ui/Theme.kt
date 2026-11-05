package com.protea.travels.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneOffset

val Navy = Color(0xFF0E1C3A)
val Gold = Color(0xFFC8A64B)
val Green = Color(0xFF1E7A4C)
val Cream = Color(0xFFFAF8F5)

@Composable
fun ProteaTheme(dark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (dark)
        darkColorScheme(primary = Gold, onPrimary = Navy, background = Color(0xFF0B1224), surface = Color(0xFF15203A),
            onBackground = Color.White, onSurface = Color.White, secondary = Green)
    else
        lightColorScheme(primary = Gold, onPrimary = Navy, background = Cream, surface = Color.White,
            onBackground = Navy, onSurface = Navy, secondary = Green)
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, modifier.fillMaxWidth().height(56.dp), enabled = enabled, shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier.fillMaxWidth(),
          password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true, modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        visualTransformation = if (password && !show) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) ({
            IconButton({ show = !show }) { Icon(if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility, "toggle") }
        }) else null
    )
}

@Composable
fun FieldButton(label: String, value: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick, modifier.height(62.dp), enabled = enabled, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
        Column(Modifier.fillMaxWidth()) {
            Text(label.uppercase(), fontSize = 10.sp, color = Color.Gray)
            Text(value.ifEmpty { "Select" }, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
fun Picker(label: String, value: String, options: List<Pair<String, String>>, modifier: Modifier = Modifier,
           onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        FieldButton(label, options.firstOrNull { it.first == value }?.second ?: value, Modifier.fillMaxWidth()) { open = true }
        DropdownMenu(open, { open = false }) {
            options.forEach { (k, v) -> DropdownMenuItem(text = { Text(v) }, onClick = { onSelect(k); open = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, value: String, modifier: Modifier = Modifier, enabled: Boolean = true, onPick: (String) -> Unit) {
    var show by remember { mutableStateOf(false) }
    FieldButton(label, value, modifier, enabled) { show = true }
    if (show) {
        val st = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { show = false }, confirmButton = {
            TextButton({
                st.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
                show = false
            }) { Text("OK") }
        }) { DatePicker(st) }
    }
}
