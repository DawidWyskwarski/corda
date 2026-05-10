package com.example.corda.ui.screen.inspirations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.corda.R

/**
 * Screen for the inspirations (currently a skeleton).
 *
 * @param openDrawer lambda reporting an event to `CordaApp` to open a drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationsScreen(
    modifier: Modifier = Modifier,
    openDrawer: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar( // TODO add search bar
                title = { Text(stringResource(R.string.inspirations) ) },
                navigationIcon = {
                    IconButton(
                        onClick = openDrawer
                    ){
                        Icon(
                            Icons.Rounded.Menu,
                            null
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //TODO add functionality
                }
            ) {
                Icon(
                    Icons.Rounded.Add,
                    null
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Inspirations Screen"
            )
        }
    }
}