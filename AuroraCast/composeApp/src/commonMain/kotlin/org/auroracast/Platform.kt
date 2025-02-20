package org.auroracast

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform