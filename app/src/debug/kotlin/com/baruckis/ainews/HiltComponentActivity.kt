package com.baruckis.ainews

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Empty Hilt-enabled activity for Compose tests: `hiltViewModel()` requires the hosting
 * activity to be an `@AndroidEntryPoint`, which the default test activity is not. Lives in
 * the debug source set (the variant unit tests run against) and never ships in release.
 */
@AndroidEntryPoint
class HiltComponentActivity : ComponentActivity()
