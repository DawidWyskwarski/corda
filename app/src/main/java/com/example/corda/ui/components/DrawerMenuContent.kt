package com.example.corda.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.ui.navigation.Screen

private data class NavigationItem(
    val icon: ImageVector,
    // Currently annotations applies to the value parameter only, 'param' ensures it remains that way in the future
    @param:StringRes val labelRes: Int,
    val screen: Screen
)

/**
 * Contents of the drawer menu
 *
 * @param currentScreen The currently selected screen. Used to highlight the corresponding item.
 * @param onScreenSelected Callback to invoke when a screen is selected.
 */
@Composable
fun DrawerMenuContent(
    modifier: Modifier = Modifier,
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    val tools = remember {
        listOf(
            NavigationItem(
                icon = Icons.Rounded.MusicNote,
                labelRes = R.string.tuner,
                screen = Screen.Tuner
            ),
            NavigationItem(
                icon = Icons.Rounded.Speed,
                labelRes = R.string.metronome,
                screen = Screen.Metronome
            ),
            NavigationItem(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                labelRes = R.string.inspirations,
                screen = Screen.Inspirations
            )
        )
    }

    val utilities = remember {
        listOf(
            NavigationItem(
                icon = Icons.Rounded.Settings,
                labelRes = R.string.settings,
                screen = Screen.Settings
            )
        )
    }

    ModalDrawerSheet(
        modifier = modifier,
        windowInsets = DrawerDefaults.windowInsets
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // LOGO SECTION

            // Change later to something more interesting
            // Something with a logo for example
            Spacer(Modifier.height(12.dp))
            Text(
                modifier = Modifier
                    .padding(16.dp),
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )

            // TOOLS SECTION
            Text(
                text = stringResource(R.string.tools),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            tools.forEach { tool ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(tool.labelRes)) },
                    selected = currentScreen == tool.screen,
                    onClick = {
                        onScreenSelected(tool.screen)
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // UTILITIES SECTION
            utilities.forEach { utility ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = utility.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(utility.labelRes)) },
                    selected = currentScreen == utility.screen,
                    onClick = {
                        onScreenSelected(utility.screen)
                    }
                )
            }
        }
    }
}
