package com.iptvapp.player.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.iptvapp.player.data.preferences.EncryptedPreferencesManager
import com.iptvapp.player.ui.navigation.IPTVNavGraph
import com.iptvapp.player.ui.navigation.Screen
import com.iptvapp.player.ui.theme.IPTVPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: EncryptedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IPTVPlayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IPTVNavGraph(
                        startDestination = if (prefs.isLoggedIn()) Screen.Home.route else Screen.Login.route
                    )
                }
            }
        }
    }
}
