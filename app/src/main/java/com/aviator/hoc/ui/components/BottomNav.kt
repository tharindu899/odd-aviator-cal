package com.aviator.hoc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aviator.hoc.ui.theme.Mint

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", AppIcons.Home),
    Generate("Generate", AppIcons.Bolt),
    Results("Results", AppIcons.Clock),
}

/** Floating pill: the active tab springs open and shows its label. */
@Composable
fun BottomNav(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().height(70.dp),
        shape = RoundedCornerShape(35.dp),
        color = Mint.Card2,
        border = BorderStroke(1.dp, Mint.Border2),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { tab -> NavItem(tab, tab == selected) { onSelect(tab) } }
        }
    }
}

@Composable
private fun NavItem(tab: Tab, active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (active) Mint.Lime else Mint.Card2, label = "navBg")
    val fg by animateColorAsState(if (active) Mint.Black else Mint.Muted, label = "navFg")
    val pad by animateDpAsState(
        targetValue = if (active) 20.dp else 14.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "navPad",
    )

    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = bg,
        pressedScale = 0.9f,
        modifier = Modifier.height(52.dp),
    ) {
        Row(
            Modifier.padding(horizontal = pad),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(tab.icon, contentDescription = tab.label, tint = fg, modifier = Modifier.size(22.dp))
            AnimatedVisibility(
                visible = active,
                enter = expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Row {
                    Spacer(Modifier.size(8.dp))
                    Text(tab.label, style = MaterialTheme.typography.labelLarge, color = fg)
                }
            }
        }
    }
}
