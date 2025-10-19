package com.zar.visitApp

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
import com.zar.zarpakhsh.utils.config.AppConfigZar
import com.zar.visitApp.ui.theme.ZarPakhshMobileTheme
import org.koin.core.component.inject
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZarPakhshMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val appConfig = AppConfigZar
                    Log.e("MainActivity", "onCreate: ${appConfig.appFlavor}")


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
    ZarPakhshMobileTheme {
        Greeting("Android")
    }
}