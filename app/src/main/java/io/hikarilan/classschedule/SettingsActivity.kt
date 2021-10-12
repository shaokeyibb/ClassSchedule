package io.hikarilan.classschedule

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.util.JsonReader
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import com.chargemap.compose.numberpicker.NumberPicker
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.hikarilan.classschedule.data.ClassEntity
import io.hikarilan.classschedule.data.Database
import io.hikarilan.classschedule.data.getPreferenceByKey
import io.hikarilan.classschedule.data.updatePreference
import io.hikarilan.classschedule.ui.theme.ClassScheduleTheme
import java.lang.IllegalStateException
import com.google.gson.JsonElement


val maxClassNumber = mutableStateOf(getPreferenceByKey("generic.maxClassNumber").value.toInt())
val maxWeek = mutableStateOf(getPreferenceByKey("generic.maxWeek").value.toInt())

val currentWeekInThisSemester =
    mutableStateOf(getPreferenceByKey("generic.currentWeekInThisSemester").value.toInt())
val maxWeekInThisSemester =
    mutableStateOf(getPreferenceByKey("generic.maxWeekInThisSemester").value.toInt())

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClassScheduleTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    SettingsMain(activity = this)
                }
            }
        }
    }
}

@Composable
fun SettingsMain(activity: ComponentActivity) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.settings))
        }, navigationIcon = {
            IconButton(onClick = {
                activity.finish()
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "BackToMain")
            }
        })
    }) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_setMaxClassNumber))
                NumberPicker(
                    value = maxClassNumber.value,
                    onValueChange = {
                        maxClassNumber.value = it
                        updatePreference("generic.maxClassNumber", it.toString())
                    },
                    range = 1..30
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_setEnableWeekend))
                Switch(checked = maxWeek.value == 7, onCheckedChange = {
                    if (it) maxWeek.value = 7 else maxWeek.value = 5;
                    updatePreference("generic.maxWeek", maxWeek.value.toString())
                })
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_setCurrentWeekInThisSemester))
                NumberPicker(
                    value = currentWeekInThisSemester.value,
                    onValueChange = {
                        currentWeekInThisSemester.value = it
                        currentWeekInThisSemesterShowed.value = it
                        updatePreference("generic.currentWeekInThisSemester", it.toString())
                    },
                    range = 1..maxWeekInThisSemester.value
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_setMaxWeekInThisSemester))
                NumberPicker(
                    value = maxWeekInThisSemester.value,
                    onValueChange = {
                        maxWeekInThisSemester.value = it
                        if (currentWeekInThisSemesterShowed.value > it)
                            currentWeekInThisSemesterShowed.value = it
                        updatePreference("generic.maxWeekInThisSemester", it.toString())
                    },
                    range = 1..30
                )
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        val clipboardManager =
                            activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "ClassScheduler_ExportJSON", Gson()
                                    .toJson(
                                        classList.toList(),
                                        object : TypeToken<List<ClassEntity>>() {}.type
                                    )
                                    .toString()
                            )
                        )
                        Toast
                            .makeText(
                                activity,
                                R.string.toast_successfulllyExport,
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_exportDataToClipboard))
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(id = R.string.settings_exportDataToClipboard)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        val clipboardManager =
                            activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        try {
                            val gson = Gson()
                            val array: JsonArray =
                                JsonParser.parseString(clipboardManager.primaryClip?.getItemAt(0)?.text.toString()).asJsonArray
                            classList.clear()
                            for (jsonElement in array) {
                                classList.add(gson.fromJson(jsonElement, ClassEntity::class.java))
                            }
                        } catch (e: JsonSyntaxException) {
                            Toast
                                .makeText(
                                    activity,
                                    R.string.toast_failedToImport,
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            return@clickable
                        }
                        Database
                            .getAppDatabase()
                            .classDao()
                            .let {
                                it
                                    .getAll()
                                    .forEach { dao -> it.delete(dao) }
                                classList.forEach { entity ->
                                    it.insertAll(entity)
                                }
                            }
                        reInit()
                        activity.finish()
                        Toast
                            .makeText(
                                activity,
                                R.string.toast_successfulllyImport,
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_importDataFromClipboard))
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(id = R.string.settings_importDataFromClipboard)
                )
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clickable {
                        Database
                            .getAppDatabase()
                            .preferenceDao()
                            .let {
                                it
                                    .getAll()
                                    .forEach { dao -> it.delete(dao) }
                            }
                        Database
                            .getAppDatabase()
                            .classDao()
                            .let {
                                it
                                    .getAll()
                                    .forEach { dao -> it.delete(dao) }
                            }
                        reInit()
                        activity.finish()
                        Toast
                            .makeText(
                                activity,
                                R.string.toast_successfullyClear,
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = stringResource(id = R.string.settings_resetAllData))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(id = R.string.settings_resetAllData)
                )
            }
        }
    }
}