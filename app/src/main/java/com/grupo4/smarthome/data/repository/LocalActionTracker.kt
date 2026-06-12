package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.data.model.NotificationKind

class LocalActionTracker(
    private val windowMillis: Long = 10_000L,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private data class Key(val deviceId: String, val kind: NotificationKind)

    private val activeUntil = mutableMapOf<Key, Long>()

    @Synchronized
    fun record(deviceId: String, kind: NotificationKind) {
        if (deviceId.isBlank()) return
        val now = clock()
        purge(now)
        activeUntil[Key(deviceId, kind)] = now + windowMillis
    }

    @Synchronized
    fun shouldSuppress(deviceId: String, kind: NotificationKind): Boolean {
        if (deviceId.isBlank()) return false
        val now = clock()
        purge(now)
        return (activeUntil[Key(deviceId, kind)] ?: 0L) > now
    }

    private fun purge(now: Long) {
        activeUntil.entries.removeAll { it.value <= now }
    }
}
