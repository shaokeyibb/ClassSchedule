package io.hikarilan.classschedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.hikarilan.classschedule.data.ClassEntity
import io.hikarilan.classschedule.data.Database
import io.hikarilan.classschedule.data.PreferenceEntity
import io.hikarilan.classschedule.data.getPreferenceByKey
import io.hikarilan.classschedule.ui.theme.ClassScheduleTheme

val classList = mutableStateListOf<ClassEntity>()

val currentWeekInThisSemesterShowed = mutableStateOf(currentWeekInThisSemester.value)

val dialogEditingClass: MutableState<ClassEntity?> = mutableStateOf(null)

class MainActivity : ComponentActivity() {

    @ExperimentalUnitApi
    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContent {
            ClassScheduleTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Main(this)
                }
            }
        }
    }

    private fun init() {
        Database.deploy(this)
        Database.getAppDatabase().preferenceDao().findByKey("generic.maxClassNumber").let {
            if (it == null) Database.getAppDatabase().preferenceDao()
                .insertAll(PreferenceEntity("generic.maxClassNumber", "8"))
        }
        Database.getAppDatabase().preferenceDao().findByKey("generic.maxWeek").let {
            if (it == null) Database.getAppDatabase().preferenceDao()
                .insertAll(PreferenceEntity("generic.maxWeek", "5"))
        }
        Database.getAppDatabase().preferenceDao().findByKey("generic.currentWeekInThisSemester")
            .let {
                if (it == null) Database.getAppDatabase().preferenceDao()
                    .insertAll(PreferenceEntity("generic.currentWeekInThisSemester", "1"))
            }
        Database.getAppDatabase().preferenceDao().findByKey("generic.maxWeekInThisSemester").let {
            if (it == null) Database.getAppDatabase().preferenceDao()
                .insertAll(PreferenceEntity("generic.maxWeekInThisSemester", "16"))
        }
    }
}

fun reInit() {
    Database.getAppDatabase().preferenceDao().findByKey("generic.maxClassNumber").let {
        if (it == null) Database.getAppDatabase().preferenceDao()
            .insertAll(PreferenceEntity("generic.maxClassNumber", "8"))
    }
    Database.getAppDatabase().preferenceDao().findByKey("generic.maxWeek").let {
        if (it == null) Database.getAppDatabase().preferenceDao()
            .insertAll(PreferenceEntity("generic.maxWeek", "5"))
    }
    Database.getAppDatabase().preferenceDao().findByKey("generic.currentWeekInThisSemester")
        .let {
            if (it == null) Database.getAppDatabase().preferenceDao()
                .insertAll(PreferenceEntity("generic.currentWeekInThisSemester", "1"))
        }
    Database.getAppDatabase().preferenceDao().findByKey("generic.maxWeekInThisSemester").let {
        if (it == null) Database.getAppDatabase().preferenceDao()
            .insertAll(PreferenceEntity("generic.maxWeekInThisSemester", "16"))
    }
    maxClassNumber.value = getPreferenceByKey("generic.maxClassNumber").value.toInt()
    maxWeek.value = getPreferenceByKey("generic.maxWeek").value.toInt()
    currentWeekInThisSemester.value =
        getPreferenceByKey("generic.currentWeekInThisSemester").value.toInt()
    currentWeekInThisSemesterShowed.value = currentWeekInThisSemester.value
    maxWeekInThisSemester.value =
        getPreferenceByKey("generic.maxWeekInThisSemester").value.toInt()
    classList.clear()
    classList.addAll(Database.getAppDatabase().classDao().getAll())
    dialogEditingClass.value = null
}

@ExperimentalUnitApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun Main(activity: ComponentActivity) {
    ShowClassEditDialog()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = stringResource(id = R.string.app_name) + " - " + stringResource(id = R.string.currentWeek).replace(
                    "{0}",
                    currentWeekInThisSemesterShowed.value.toString()
                )
            )
        }, navigationIcon = {
            IconButton(onClick = {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
            }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        }, actions = {
            IconButton(
                onClick = {
                    currentWeekInThisSemesterShowed.value--
                },
                enabled = currentWeekInThisSemesterShowed.value > 1
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
            }
            IconButton(
                onClick = {
                    currentWeekInThisSemesterShowed.value++
                },
                enabled = currentWeekInThisSemesterShowed.value < maxWeekInThisSemester.value
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next")
            }
        })
    }) {
        ShowSchedule(currentWeekInThisSemesterShowed.value)
    }
}

@ExperimentalUnitApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun ShowSchedule(currentWeek: Int) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(maxWeek.value),
        contentPadding = PaddingValues(1.dp),
        modifier = Modifier.draggable(
            rememberDraggableState(onDelta = {
                if (it < 5.0f && currentWeekInThisSemesterShowed.value > 1)
                    currentWeekInThisSemesterShowed.value--
                else if (it > 5.0f && currentWeekInThisSemesterShowed.value < maxWeekInThisSemester.value)
                    currentWeekInThisSemesterShowed.value++
            }),
            orientation = Orientation.Horizontal
        )
    ) {
        items(maxWeek.value) { index ->
            ShowWeeks(week = index + 1)
        }
        items(
            items = ClassEntity.fillEntities(
                classList,
                maxWeek.value,
                maxClassNumber.value
            ).map {
                if (currentWeek.toString() !in it.availableWeeks.split(',')) {
                    return@map it.copy(className = "", teacher = "", location = "")
                }
                return@map it
            }
        ) { item ->
            ShowClassCard(item)
        }
    }
}

@Composable
fun ShowWeeks(week: Int) {
    Box(contentAlignment = Alignment.Center) {
        when (week) {
            1 -> Text(text = stringResource(id = R.string.week_1))
            2 -> Text(text = stringResource(id = R.string.week_2))
            3 -> Text(text = stringResource(id = R.string.week_3))
            4 -> Text(text = stringResource(id = R.string.week_4))
            5 -> Text(text = stringResource(id = R.string.week_5))
            6 -> Text(text = stringResource(id = R.string.week_6))
            7 -> Text(text = stringResource(id = R.string.week_7))
        }
    }
}


@ExperimentalUnitApi
@ExperimentalMaterialApi
@Composable
fun ShowClassCard(item: ClassEntity) {
    Card(
        onClick = {
            dialogEditingClass.value =
                Database.getAppDatabase().classDao().getAll().find { it == item } ?: item
        }, modifier = Modifier
            .size(100.dp)
    ) {
        Column(
            Modifier
                .padding(5.dp)
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.className,
                fontSize = TextUnit(5F, TextUnitType.Em)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.location,
                fontSize = TextUnit(3F, TextUnitType.Em)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (item.teacher.isNotBlank()) "@${item.teacher}" else item.teacher,
                fontSize = TextUnit(3F, TextUnitType.Em)
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun ShowClassEditDialog() {
    if (dialogEditingClass.value == null) return

    val className = remember { mutableStateOf(dialogEditingClass.value!!.className) }
    val teacher = remember { mutableStateOf(dialogEditingClass.value!!.teacher) }
    val location = remember { mutableStateOf(dialogEditingClass.value!!.location) }
    val availableClass: SnapshotStateList<Int> = remember {
        val list: SnapshotStateList<Int> = mutableStateListOf()
        if (dialogEditingClass.value!!.availableWeeks.isNotBlank())
            list.addAll(
                dialogEditingClass.value!!.availableWeeks.split(',').map { it.toInt() })
        list
    }

    Dialog(onDismissRequest = { dialogEditingClass.value = null }) {
        Surface(
            elevation = 8.dp,
            modifier = Modifier
                .requiredWidth(LocalConfiguration.current.screenWidthDp.dp * 0.85f)
                .requiredHeight(LocalConfiguration.current.screenHeightDp.dp * 0.60f)
                .padding(4.dp)
        ) {
            Scaffold(topBar = {
                TopAppBar(title = {
                    Text(text = stringResource(id = R.string.dialogEditingClass_title))
                }, actions = {
                    IconButton(
                        onClick = {
                            Database.getAppDatabase().classDao().delete(dialogEditingClass.value!!)
                            classList.remove(dialogEditingClass.value!!)
                            dialogEditingClass.value = null
                        },
                        enabled = dialogEditingClass.value
                                in Database.getAppDatabase().classDao().getAll()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove"
                        )
                    }
                    IconButton(
                        onClick = {
                            dialogEditingClass.value = null
                        }, enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                    IconButton(
                        onClick = {
                            Database.getAppDatabase().classDao()
                                .insertAll(dialogEditingClass.value!!)

                            classList.clear()
                            classList.addAll(Database.getAppDatabase().classDao().getAll())

                            dialogEditingClass.value = null
                        },
                        enabled = className.value.isNotBlank()
                                && availableClass.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Save"
                        )
                    }
                })
            }) {
                if (dialogEditingClass.value == null) return@Scaffold
                val availableClassType = remember { mutableStateOf(3) }

                fun updateAvailableClassType() {
                    availableClassType.value =
                        if (dialogEditingClass.value!!.availableWeeks.isNotBlank())
                            dialogEditingClass.value!!.availableWeeks.split(",")
                                .map { it.toInt() }
                                .let {
                                    val list = mutableListOf<Int>()
                                    for (i in 1..maxWeekInThisSemester.value) {
                                        list.add(i)
                                    }
                                    when {
                                        it.containsAll(list) -> 0
                                        it.all { i -> i % 2 == 1 } && it.containsAll(
                                            list.filter { i -> i % 2 == 1 }) -> 1
                                        it.all { i -> i % 2 == 0 } && it.containsAll(
                                            list.filter { i -> i % 2 == 0 }) -> 2
                                        else -> 3
                                    }
                                }
                        else 3
                }

                updateAvailableClassType()

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.size(10.dp))
                    TextField(
                        value = className.value,
                        onValueChange = { str ->
                            className.value = str
                            dialogEditingClass.value!!.className = className.value
                        },
                        label = { Text(text = stringResource(id = R.string.dialogEditingClass_setClassName)) },
                        isError = className.value.isBlank()
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Divider()

                    Spacer(modifier = Modifier.size(10.dp))
                    TextField(
                        value = teacher.value,
                        onValueChange = { str ->
                            teacher.value = str
                            dialogEditingClass.value!!.teacher = teacher.value
                        },
                        label = { Text(text = stringResource(id = R.string.dialogEditingClass_setTeacher)) }
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Divider()

                    Spacer(modifier = Modifier.size(10.dp))
                    TextField(
                        value = location.value,
                        onValueChange = { str ->
                            location.value = str
                            dialogEditingClass.value!!.location = location.value
                        },
                        label = { Text(text = stringResource(id = R.string.dialogEditingClass_setLocation)) }
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Divider()

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialogEditingClass_setAvailableWeeks),
                            modifier = Modifier.weight(0.25f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(0.25f)
                        ) {
                            RadioButton(selected = availableClassType.value == 1, onClick = {
                                val list = mutableListOf<Int>()
                                for (i in 1..maxWeekInThisSemester.value) {
                                    list.add(i)
                                }
                                availableClass.clear()
                                availableClass.addAll(list.filter { i -> i % 2 == 1 })
                                dialogEditingClass.value!!.availableWeeks =
                                    availableClass.joinToString(separator = ",")
                                updateAvailableClassType()
                            })
                            Text(text = stringResource(id = R.string.dialogEditingClass_setAvailableWeeks_oddNumberOnly))
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(0.25f)
                        ) {
                            RadioButton(selected = availableClassType.value == 2, onClick = {
                                val list = mutableListOf<Int>()
                                for (i in 1..maxWeekInThisSemester.value) {
                                    list.add(i)
                                }
                                availableClass.clear()
                                availableClass.addAll(list.filter { i -> i % 2 == 0 })
                                dialogEditingClass.value!!.availableWeeks =
                                    availableClass.joinToString(separator = ",")
                                updateAvailableClassType()
                            })
                            Text(text = stringResource(id = R.string.dialogEditingClass_setAvailableWeeks_evenNumberOnly))
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(0.25f)
                        ) {
                            RadioButton(selected = availableClassType.value == 0, onClick = {
                                val list = mutableListOf<Int>()
                                for (i in 1..maxWeekInThisSemester.value) {
                                    list.add(i)
                                }
                                availableClass.clear()
                                availableClass.addAll(list)
                                dialogEditingClass.value!!.availableWeeks =
                                    availableClass.joinToString(separator = ",")
                                updateAvailableClassType()
                            })
                            Text(text = stringResource(id = R.string.dialogEditingClass_setAvailableWeeks_all))
                        }
                    }
                    LazyVerticalGrid(
                        cells = GridCells.Fixed(6)
                    ) {
                        items(maxWeekInThisSemester.value) { index ->
                            Row(Modifier.padding(5.dp)) {
                                val index = index + 1
                                Checkbox(
                                    checked = availableClass.contains(index),
                                    onCheckedChange = { checked ->
                                        if (checked) availableClass.add(index)
                                        else availableClass.remove(index)
                                        dialogEditingClass.value!!.availableWeeks =
                                            availableClass.joinToString(separator = ",")
                                        updateAvailableClassType()
                                    })
                                Text(text = index.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}