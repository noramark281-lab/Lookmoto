package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.CryptoDashboardScreen
import com.example.ui.CryptoViewModel
import com.example.ui.theme.BTCProTheme
import com.example.ui.theme.CryptoBgDark

class MainActivity : ComponentActivity() {

    private val viewModel: CryptoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BTCProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CryptoBgDark
                ) {
                    CryptoDashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
