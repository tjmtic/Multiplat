package com.abyxcz.composeforms.persistence

/** Current wall-clock time in epoch milliseconds. Platform-provided to avoid extra dependencies. */
internal expect fun nowMillis(): Long
