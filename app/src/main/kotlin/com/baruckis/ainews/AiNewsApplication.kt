package com.baruckis.ainews

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point that bootstraps Hilt's dependency-injection graph. */
@HiltAndroidApp
class AiNewsApplication : Application()
