package com.baruckis.ainews.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategyScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [TwoPaneSceneStrategy]: only the back-stack shapes the strategy owns —
 * the bare list, or the list with one detail on top — map to a [TwoPaneScene]; everything
 * else falls back to the single-pane strategy by returning null.
 */
class TwoPaneSceneStrategyTest {
    /** Stand-in for a future destination that belongs to neither pane. */
    private object UnknownKey : NavKey

    private val scope = SceneStrategyScope<NavKey>()

    private val listEntry =
        NavEntry<NavKey>(NewsList, metadata = TwoPaneScene.listPane()) {}

    private val detailEntry =
        NavEntry<NavKey>(ArticleDetail(id = "1"), metadata = TwoPaneScene.detailPane()) {}

    private val unknownEntry = NavEntry<NavKey>(UnknownKey) {}

    private fun calculateScene(
        isTwoPane: Boolean,
        entries: List<NavEntry<NavKey>>,
    ) = with(TwoPaneSceneStrategy<NavKey>(isTwoPane)) { scope.calculateScene(entries) }

    @Test
    fun `a compact width never produces the two-pane scene`() {
        assertNull(calculateScene(isTwoPane = false, entries = listOf(listEntry, detailEntry)))
    }

    @Test
    fun `the bare list maps to the two-pane scene with the placeholder pane`() {
        val scene = calculateScene(isTwoPane = true, entries = listOf(listEntry))

        assertNotNull(scene)
        assertEquals(listOf(listEntry), scene?.entries)
        assertEquals(emptyList<NavEntry<NavKey>>(), scene?.previousEntries)
    }

    @Test
    fun `the list with one detail on top maps to the two-pane scene`() {
        val scene = calculateScene(isTwoPane = true, entries = listOf(listEntry, detailEntry))

        assertNotNull(scene)
        assertEquals(listOf(listEntry, detailEntry), scene?.entries)
        // Popping the detail keeps the list pane on screen.
        assertEquals(listOf(listEntry), scene?.previousEntries)
    }

    @Test
    fun `a third entry above the detail falls back to the single-pane strategy`() {
        assertNull(calculateScene(isTwoPane = true, entries = listOf(listEntry, detailEntry, unknownEntry)))
    }

    @Test
    fun `a top entry that is not a detail pane falls back to the single-pane strategy`() {
        assertNull(calculateScene(isTwoPane = true, entries = listOf(listEntry, unknownEntry)))
    }

    @Test
    fun `a stack not starting with the list falls back to the single-pane strategy`() {
        assertNull(calculateScene(isTwoPane = true, entries = listOf(detailEntry)))
    }
}
