package com.kreeda.ankana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kreeda.ankana.ui.nav.KreedaNavHost
import com.kreeda.ankana.ui.theme.KreedaAnkanaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KreedaAnkanaTheme {
                KreedaNavHost()
            }
        }
    }
}
