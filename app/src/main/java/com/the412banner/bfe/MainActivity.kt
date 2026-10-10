package com.the412banner.bfe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.the412banner.bfe.ui.ApkJobPill
import com.the412banner.bfe.ui.screens.CloneHubScreen
import com.the412banner.bfe.ui.theme.BfeTheme

/** Clone Hub front page. Original BFE file manager stays in source for compatibility. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BfeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CloneHubScreen(modifier = Modifier.weight(1f).fillMaxWidth())
                        ApkJobPill()
                    }
                }
            }
        }
    }
}
