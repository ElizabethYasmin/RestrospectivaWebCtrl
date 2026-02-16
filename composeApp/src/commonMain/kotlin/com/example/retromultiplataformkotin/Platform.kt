package com.example.retromultiplataformkotin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform