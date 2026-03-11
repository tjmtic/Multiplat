package com.abyxcz.multiplat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform