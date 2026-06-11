package com.baruckis.ainews

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath

/** Fraction of the app's memory class dedicated to decoded article images. */
private const val IMAGE_MEMORY_CACHE_PERCENT = 0.25

/** Disk budget for downloaded article images (50 MB). */
private const val IMAGE_DISK_CACHE_BYTES = 50L * 1024 * 1024

/** Application entry point that bootstraps Hilt's dependency-injection graph. */
@HiltAndroidApp
class AiNewsApplication :
    Application(),
    SingletonImageLoader.Factory {
    /**
     * App-wide Coil [ImageLoader] with explicit memory and disk caches, so article images
     * survive scrolling (memory) and process restarts (disk) instead of being re-fetched,
     * and with crossfade as the default transition for freshly loaded images.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .memoryCache {
                MemoryCache
                    .Builder()
                    .maxSizePercent(context, IMAGE_MEMORY_CACHE_PERCENT)
                    .build()
            }.diskCache {
                DiskCache
                    .Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                    .build()
            }.crossfade(true)
            .build()
}
