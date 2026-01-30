package com.daedan.festabook.presentation.explore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.explore.component.ExploreScreen
import com.daedan.festabook.presentation.main.MainActivity
import com.daedan.festabook.presentation.theme.FestabookTheme

class ExploreActivity : AppCompatActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.metroViewModelFactory

    private val viewModel: ExploreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FestabookTheme {
                ExploreScreen(
                    viewModel = viewModel,
                    onNavigateToMain = {
                        navigateToMainActivity()
                    },
                    onBackClick = { finish() },
                )
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent =
            MainActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        startActivity(intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ExploreActivity::class.java)
    }
}