package com.aviator.hoc.domain

import com.aviator.hoc.core.WindowStatus

enum class AlertEvent { WINDOW_OPEN, MARK_OPEN }

/**
 * Decides when to beep / vibrate. Pure and stateful only through [lastOpenId] / [lastMarkId],
 * so every signal fires each event once (same rule as the web app's useSignalSounds).
 *
 * The first state it ever sees is only recorded, never fired – opening the app in the middle
 * of a window should not beep.
 */
class AlertTracker {
    private var primed = false
    private var lastOpenId: Long? = null
    private var lastMarkId: Long? = null

    fun onState(state: HomeUiState): List<AlertEvent> {
        val openId = state.current?.takeIf { it.status == WindowStatus.ACTIVE }?.id
        val markId = state.mark?.signal?.id

        val events = ArrayList<AlertEvent>(2)
        if (primed) {
            if (openId != null && openId != lastOpenId) events += AlertEvent.WINDOW_OPEN
            if (markId != null && markId != lastMarkId) events += AlertEvent.MARK_OPEN
        }
        primed = true
        if (openId != null) lastOpenId = openId
        if (markId != null) lastMarkId = markId
        return events
    }
}
