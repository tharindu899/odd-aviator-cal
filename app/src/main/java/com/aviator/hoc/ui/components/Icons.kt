package com.aviator.hoc.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Tiny stroke icon set (no material-icons-extended => smaller APK). Tint comes from Icon(). */
private fun stroke(name: String, path: String, width: Float = 2.2f): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes(path),
            stroke = SolidColor(Color.White),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()

object AppIcons {
    val Home = stroke("home", "M3 11l9-8 9 8v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z")
    val Bolt = stroke("bolt", "M13 2L4 14h7l-1 8 9-12h-7z")
    val Clock = stroke("clock", "M12 7v5l3 2M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z")
    val Menu = stroke("menu", "M4 6h16M4 12h16M4 18h10", 2f)
    val Check = stroke("check", "M5 13l4 4L19 7", 2.6f)
    val Close = stroke("close", "M6 6l12 12M18 6L6 18", 2.6f)
    val Layers = stroke("layers", "M12 3l9 5-9 5-9-5 9-5zM3 13l9 5 9-5", 2f)
    val Clipboard = stroke("clipboard", "M9 4h6a1 1 0 0 1 1 1v1H8V5a1 1 0 0 1 1-1zM8 6H6a1 1 0 0 0-1 1v13a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V7a1 1 0 0 0-1-1h-2", 2f)
    val Alert = stroke("alert", "M12 8v5M12 16v.01M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z", 2f)
    val Play = stroke("play", "M7 4l13 8-13 8z", 2f)
    val ChevronDown = stroke("chevronDown", "M6 9l6 6 6-6", 2.2f)
    val Trash = stroke("trash", "M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3", 2f)
    val Search = stroke("search", "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM21 21l-5-5", 2f)
    val Download = stroke("download", "M12 3v12M7 11l5 5 5-5M4 21h16", 2f)
    val Undo = stroke("undo", "M3 12a9 9 0 1 0 3-6.7M3 4v5h5", 2f)
    val Chart = stroke("chart", "M4 20V10M10 20V4M16 20v-8M22 20H2", 2f)
    val Activity = stroke("activity", "M3 12h4l3-8 4 16 3-8h4", 2f)
    val Timer = stroke("timer", "M12 8v5l3 2M9 2h6M12 4a8 8 0 1 0 0 16 8 8 0 0 0 0-16z", 2f)
}
