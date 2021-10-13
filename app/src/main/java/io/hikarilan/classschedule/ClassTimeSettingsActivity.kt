package io.hikarilan.classschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import io.hikarilan.classschedule.ui.theme.ClassScheduleTheme

class ClassTimeSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClassScheduleTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ClassTimeSettingsMain(activity = this)
                }
            }
        }
    }
}

@Composable
fun ClassTimeSettingsMain(activity: ComponentActivity) {

}