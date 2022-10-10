package com.example.mcard.repository.source.usage

internal interface CameraUsageSource
{
    fun stopStream()
    fun startStream()
}