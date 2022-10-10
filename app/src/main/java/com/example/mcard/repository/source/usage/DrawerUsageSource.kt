package com.example.mcard.repository.source.usage

internal interface DrawerUsageSource {
    fun lockDrawer()
    fun unlockDrawer()
    fun show()
    fun hide()
}