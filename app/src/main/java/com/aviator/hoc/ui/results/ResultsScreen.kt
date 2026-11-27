package com.aviator.hoc.ui.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aviator.hoc.core.Hms
import com.aviator.hoc.core.formatCountdown
import com.aviator.hoc.data.db.SignalStatus
import com.aviator.hoc.domain.ResultRow
import com.aviator.hoc.domain.SortMode
import com.aviator.hoc.domain.StatusFilter
import com.aviator.hoc.domain.exportJson
import com.aviator.hoc.ui.components.AppIcons
import com.aviator.hoc.ui.components.PressableSurface
import com.aviator.hoc.ui.theme.Mint
import com.aviator.hoc.util.shareResultsJson
import java.util.Locale
import kotlin.math.abs

@Composable
fun ResultsScreen(vm: ResultsViewModel, state: ResultsUiState, bottomPadding: Dp) {
    val c = state.controls
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Header(
            total = state.total,
            settled = state.stats.completed,
            searchOpen = c.searchOpen,
            onSearch = vm::toggleSearch,
            onExport = { shareResultsJson(context, exportJson(state.rows.map { it.signal })) },
            onDeleteAll = { vm.askDeleteAll(true) },
            hasData = state.total > 0,
        )

        if (state.total == 0) {
            EmptyAll()
        } else {
            StatStrip(state)
            FilterChips(state, vm::setFilter)
            SearchPanel(c.searchOpen, c.query, c.sort, vm::setQuery, vm::setSort)

            if (state.rows.isEmpty()) {
                NothingMatches(vm::clearFilters)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = bottomPadding),
                ) {
                    itemsIndexed(state.rows, key = { _, r -> r.signal.id }) { i, row ->
                        ResultCard(
                            row = row,
                            index = i + 1,
                            expanded = c.expandedId == row.signal.id,
                            onToggle = { vm.toggleExpand(row.signal.id) },
                            onWin = { vm.markWin(row.signal.id) },
                            onLoss = { vm.markLoss(row.signal.id) },
                            onReset = { vm.reset(row.signal.id) },
                            onDelete = { vm.askDelete(row.signal.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (c.deleteId != null) {
        ConfirmDialog(
            title = "Delete this result?",
            text = "It is removed permanently and cannot be restored.",
            confirm = "Delete",
            onConfirm = vm::confirmDelete,
            onDismiss = { vm.askDelete(null) },
        )
    }
    if (c.deleteAllOpen) {
        ConfirmDialog(
            title = "Delete all results?",
            text = "All ${state.total} results (won, lost, missed and pending) are removed permanently.",
            confirm = "Delete all",
            onConfirm = vm::confirmDeleteAll,
            onDismiss = { vm.askDeleteAll(false) },
        )
    }
}

// ---------------------------------------------------------------- header

@Composable
private fun Header(
    total: Int,
    settled: Int,
    searchOpen: Boolean,
    hasData: Boolean,
    onSearch: () -> Unit,
    onExport: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Results", style = MaterialTheme.typography.headlineMedium, color = Mint.Text)
            Text("$total signals · $settled settled", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
        }
        if (hasData) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(AppIcons.Search, "Search and sort", onSearch, highlighted = searchOpen)
                IconButton(AppIcons.Download, "Export JSON", onExport)
                IconButton(AppIcons.Trash, "Delete all results", onDeleteAll, danger = true)
            }
        }
    }
}

@Composable
private fun IconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
    danger: Boolean = false,
    size: Dp = 44.dp,
) {
    val tint = when {
        danger -> Mint.PinkSoft
        highlighted -> Mint.Black
        else -> Mint.Text
    }
    val bg by animateColorAsState(if (highlighted) Mint.Lime else Mint.Card, label = "iconBg")
    PressableSurface(
        onClick = onClick,
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, if (danger) Mint.Pink.copy(alpha = 0.4f) else Mint.Border2),
        modifier = Modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

// ---------------------------------------------------------------- stats + filters

@Composable
private fun StatStrip(state: ResultsUiState) {
    val s = state.stats
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile("Won", s.wins.toString(), Mint.Lime, Modifier.weight(1f))
        Tile("Lost", s.losses.toString(), Mint.PinkSoft, Modifier.weight(1f))
        Tile("Pending", s.pending.toString(), Mint.Amber, Modifier.weight(1f))
        Tile("Win %", "${s.winRate}%", if (s.winRate >= 50) Mint.Lime else Mint.Amber, Modifier.weight(1f))
    }
}

@Composable
private fun Tile(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = Mint.Card, border = BorderStroke(1.dp, Mint.Border)) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        }
    }
}

private fun StatusFilter.icon(): ImageVector = when (this) {
    StatusFilter.ALL -> AppIcons.Chart
    StatusFilter.ACTIVE -> AppIcons.Activity
    StatusFilter.WON -> AppIcons.Check
    StatusFilter.LOST -> AppIcons.Close
    StatusFilter.MISSED -> AppIcons.Alert
}

@Composable
private fun FilterChips(state: ResultsUiState, onSelect: (StatusFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(StatusFilter.entries, key = { it.name }) { f ->
            val on = state.controls.filter == f
            val bg by animateColorAsState(if (on) Mint.Lime else Mint.Card, label = "chipBg")
            val fg by animateColorAsState(if (on) Mint.Black else Mint.Muted, label = "chipFg")
            PressableSurface(
                onClick = { onSelect(f) },
                shape = RoundedCornerShape(16.dp),
                color = bg,
                border = if (on) null else BorderStroke(1.dp, Mint.Border2),
                pressedScale = 0.94f,
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(f.icon(), contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                    Text(f.label, style = MaterialTheme.typography.labelLarge, color = fg)
                    Text(
                        "${state.counts[f] ?: 0}",
                        style = MaterialTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchPanel(
    open: Boolean,
    query: String,
    sort: SortMode,
    onQuery: (String) -> Unit,
    onSort: (SortMode) -> Unit,
) {
    AnimatedVisibility(
        visible = open,
        enter = expandVertically(tween(250)) + fadeIn(tween(250)),
        exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Odd, status, time or date", color = Mint.Muted) },
                leadingIcon = { Icon(AppIcons.Search, null, tint = Mint.Lime, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            AppIcons.Close, contentDescription = "Clear search", tint = Mint.Muted,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(11.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { onQuery("") },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Mint.Text, unfocusedTextColor = Mint.Text,
                    focusedContainerColor = Mint.Card, unfocusedContainerColor = Mint.Card,
                    focusedBorderColor = Mint.Lime, unfocusedBorderColor = Mint.Border2,
                    cursorColor = Mint.Lime,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Sort", style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
                SortMode.entries.forEach { m ->
                    val on = m == sort
                    val bg by animateColorAsState(if (on) Mint.LimeChip else Mint.Card, label = "sortBg")
                    PressableSurface(
                        onClick = { onSort(m) },
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = BorderStroke(1.dp, if (on) Mint.Lime else Mint.Border2),
                        pressedScale = 0.94f,
                    ) {
                        Text(
                            m.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (on) Mint.Lime else Mint.Muted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- result card

private data class Tone(val color: Color, val label: String, val icon: ImageVector)

private fun toneOf(row: ResultRow): Tone = when (row.signal.status) {
    SignalStatus.WIN -> Tone(Mint.Lime, "Won", AppIcons.Check)
    SignalStatus.LOSS -> Tone(Mint.PinkSoft, "Lost", AppIcons.Close)
    SignalStatus.MISSED -> Tone(Mint.Muted, "Missed", AppIcons.Alert)
    else -> if (row.active) Tone(Mint.Lime, "Active", AppIcons.Activity) else Tone(Mint.Amber, "Pending", AppIcons.Clock)
}

@Composable
private fun ResultCard(
    row: ResultRow,
    index: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onWin: () -> Unit,
    onLoss: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tone = toneOf(row)
    val toneColor by animateColorAsState(tone.color, tween(300), label = "tone")
    val rotation by animateFloatAsState(
        if (expanded) 180f else 0f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "chevron",
    )
    val pending = row.signal.status == SignalStatus.PENDING

    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        shape = MaterialTheme.shapes.medium,
        color = if (row.active) Mint.LimeChip else Mint.Card,
        border = BorderStroke(1.dp, if (row.active) Mint.LimeBorder else Mint.Border),
    ) {
        Column {
            Row(
                Modifier.padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // tappable summary
                Row(
                    Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggle,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier.size(34.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = toneColor.copy(alpha = 0.15f), modifier = Modifier.size(34.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("$index", style = MaterialTheme.typography.labelSmall, color = toneColor)
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(row.resultTime.toString(), style = MaterialTheme.typography.titleLarge, color = Mint.Text)
                            Text(
                                String.format(Locale.ROOT, "%.2fx", row.signal.odd),
                                style = MaterialTheme.typography.bodySmall, color = Mint.Muted,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(tone.icon, null, tint = toneColor, modifier = Modifier.size(12.dp))
                            Text(tone.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = toneColor)
                            row.countdown?.let { cd ->
                                val txt = if (cd >= 0) formatCountdown(cd) else "${abs(cd)}s"
                                val suffix = if (row.active) (if (cd > 0) " left" else " ago") else ""
                                Text("· $txt$suffix", style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
                            }
                        }
                    }
                    Icon(
                        AppIcons.ChevronDown, contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Mint.Muted, modifier = Modifier.size(18.dp).rotate(rotation),
                    )
                }

                if (pending) {
                    RoundAction(AppIcons.Check, "Mark win", Mint.Lime, Mint.LimeChip, Mint.LimeBorder, onWin)
                    RoundAction(AppIcons.Close, "Mark loss", Mint.PinkSoft, Mint.PinkChip, Mint.Pink.copy(alpha = 0.5f), onLoss)
                }
            }

            AnimatedVisibility(visible = expanded, enter = fadeIn(tween(200)), exit = fadeOut(tween(120))) {
                Column(
                    Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeTile("Open", row.times.open, Modifier.weight(1f))
                        TimeTile("Close", row.times.close, Modifier.weight(1f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Added ${row.created}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Mint.Muted,
                            modifier = Modifier.weight(1f),
                        )
                        if (!pending) {
                            SmallButton("Reset", AppIcons.Undo, Mint.Lime, onReset)
                            Spacer(Modifier.size(8.dp))
                        }
                        PressableSurface(
                            onClick = onDelete,
                            shape = RoundedCornerShape(14.dp),
                            color = Mint.PinkChip,
                            border = BorderStroke(1.dp, Mint.Pink.copy(alpha = 0.5f)),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(AppIcons.Trash, "Delete result", tint = Mint.PinkSoft, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundAction(icon: ImageVector, description: String, tint: Color, bg: Color, border: Color, onClick: () -> Unit) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        pressedScale = 0.88f,
        modifier = Modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SmallButton(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Mint.LimeChip,
        border = BorderStroke(1.dp, Mint.LimeBorder),
        modifier = Modifier.height(40.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = tint)
        }
    }
}

@Composable
private fun TimeTile(label: String, time: Hms, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Mint.Card2, border = BorderStroke(1.dp, Mint.Border)) {
        Column(Modifier.padding(vertical = 8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Mint.Muted)
            Text(time.toString(), style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), color = Mint.Text)
        }
    }
}

// ---------------------------------------------------------------- empty states + dialog

@Composable
private fun EmptyAll() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = Mint.Card, border = BorderStroke(1.dp, Mint.Border)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(AppIcons.Chart, null, tint = Mint.Lime, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text("No results yet", style = MaterialTheme.typography.titleLarge, color = Mint.Text)
            Text("Added signals and their outcomes show up here.", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
        }
    }
}

@Composable
private fun NothingMatches(onClear: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = Mint.Card, border = BorderStroke(1.dp, Mint.Border)) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Nothing matches", style = MaterialTheme.typography.titleMedium, color = Mint.Text)
            Text("Try another filter or clear the search.", style = MaterialTheme.typography.bodyMedium, color = Mint.Muted)
            Spacer(Modifier.height(10.dp))
            PressableSurface(onClick = onClear, shape = RoundedCornerShape(24.dp), color = Mint.Lime) {
                Text(
                    "Clear filters", style = MaterialTheme.typography.labelLarge, color = Mint.Black,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Mint.Card,
        shape = RoundedCornerShape(28.dp),
        icon = { Icon(AppIcons.Trash, null, tint = Mint.PinkSoft, modifier = Modifier.size(28.dp)) },
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Mint.Text) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, color = Mint.Muted) },
        confirmButton = {
            PressableSurface(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
                color = Mint.Pink,
            ) {
                Text(confirm, style = MaterialTheme.typography.labelLarge, color = Mint.Black, modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp))
            }
        },
        dismissButton = {
            PressableSurface(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                color = Mint.Card2,
                border = BorderStroke(1.dp, Mint.Border2),
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = Mint.Text, modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp))
            }
        },
    )
}
