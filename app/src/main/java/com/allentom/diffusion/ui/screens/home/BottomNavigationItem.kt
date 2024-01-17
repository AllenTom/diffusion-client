package com.allentom.diffusion.ui.screens.home
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R

data class BottomNavigationItem(
    val label : String = "",
    val icon : ImageVector = Icons.Filled.Home,
) {
    @Composable
    fun bottomNavigationItems() : List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = stringResource(id = R.string.tab_draw),
                icon = Icons.Filled.Create,
            ),
            BottomNavigationItem(
                label = stringResource(id = R.string.tab_gallery),
                icon = Icons.Filled.Star,
            ),
            BottomNavigationItem(
                label = stringResource(id = R.string.tab_tools),
                icon = Icons.Filled.ThumbUp,
            ),
        )
    }
}