package com.msa.visitApp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.msa.core.common.config.AppConfig
import com.msa.visitApp.ui.theme.msaPakhshMobileTheme
import org.koin.android.ext.android.inject
class MainActivity : ComponentActivity() {
    private val appConfig: AppConfig by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.e("MainActivity", "onCreate: ${appConfig.flavorName}")
        setContent {
            msaPakhshMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    msaPakhshMobileTheme {
        Greeting("Android")
    }
}