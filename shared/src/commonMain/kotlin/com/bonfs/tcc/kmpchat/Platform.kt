package com.bonfs.tcc.kmpchat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform