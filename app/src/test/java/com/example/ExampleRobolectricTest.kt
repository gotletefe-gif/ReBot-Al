package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultStations
import com.example.data.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Radyo Türk", appName)
  }

  @Test
  fun `default stations are available and valid`() {
    assertTrue(DefaultStations.list.isNotEmpty())
    assertTrue(DefaultStations.categories.contains("Tümü"))
    assertTrue(DefaultStations.categories.contains("Favoriler"))
    assertTrue(DefaultStations.categories.contains("Pop"))

    val trtFm = DefaultStations.list.find { it.id == "trt_fm" }
    assertTrue(trtFm != null)
    assertEquals("91.4 FM", trtFm?.frequency)
  }

  @Test
  fun `preferences manager toggles favorites correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    val stationId = "test_radio_1"
    val initiallyFav = prefs.getFavoriteIds().contains(stationId)
    assertFalse(initiallyFav)

    val isFavAfterAdd = prefs.toggleFavorite(stationId)
    assertTrue(isFavAfterAdd)
    assertTrue(prefs.getFavoriteIds().contains(stationId))

    val isFavAfterRemove = prefs.toggleFavorite(stationId)
    assertFalse(isFavAfterRemove)
    assertFalse(prefs.getFavoriteIds().contains(stationId))
  }

  @Test
  fun `search by station name filters matching stations`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.RadioViewModel(app)

    // Initially all stations are visible
    assertTrue(vm.filteredStations.value.isNotEmpty())

    // Search for "Kral"
    vm.onSearchQueryChanged("Kral")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val kralStations = vm.filteredStations.value
    assertTrue(kralStations.isNotEmpty())
    assertTrue(kralStations.all { it.name.contains("Kral", ignoreCase = true) || it.description.contains("Kral", ignoreCase = true) })

    // Search for "TRT"
    vm.onSearchQueryChanged("trt")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val trtStations = vm.filteredStations.value
    assertTrue(trtStations.isNotEmpty())
    assertTrue(trtStations.all { it.name.contains("TRT", ignoreCase = true) })

    // Search non-existing
    vm.onSearchQueryChanged("NonExistentStationXYZ123")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val emptyStations = vm.filteredStations.value
    assertTrue(emptyStations.isEmpty())

    // Clear search returns full list
    vm.onSearchQueryChanged("")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals(DefaultStations.list.size, vm.filteredStations.value.size)
  }

  @Test
  fun `room database stores and deletes favorite radio stations`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.RadioDatabase::class.java
    ).allowMainThreadQueries().build()

    val dao = db.favoriteStationDao()
    val testStation = com.example.data.local.FavoriteStationEntity(
      id = "test_radio_room",
      name = "Test FM",
      category = "Pop",
      streamUrl = "https://test.stream/live",
      frequency = "100.0 FM"
    )

    // Insert
    dao.insertFavorite(testStation)
    val fetched = dao.getFavoriteById("test_radio_room")
    org.junit.Assert.assertNotNull(fetched)
    assertEquals("Test FM", fetched?.name)

    // Delete
    dao.deleteFavoriteById("test_radio_room")
    val afterDelete = dao.getFavoriteById("test_radio_room")
    org.junit.Assert.assertNull(afterDelete)

    db.close()
  }

  @Test
  fun `switching tabs in view model updates active tab`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.RadioViewModel(app)

    assertEquals(com.example.ui.RadioTab.STATIONS, vm.currentTab.value)

    vm.selectTab(com.example.ui.RadioTab.FAVORITES)
    assertEquals(com.example.ui.RadioTab.FAVORITES, vm.currentTab.value)

    vm.selectTab(com.example.ui.RadioTab.STATIONS)
    assertEquals(com.example.ui.RadioTab.STATIONS, vm.currentTab.value)
  }

  @Test
  fun `room database tracks recent radio stations and limits to 5 ordered by playedAt desc`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.RadioDatabase::class.java
    ).allowMainThreadQueries().build()

    val dao = db.recentStationDao()

    // Insert 6 stations with increasing timestamps
    for (i in 1..6) {
      val entity = com.example.data.local.RecentStationEntity(
        id = "radio_$i",
        name = "Radio $i",
        category = "Pop",
        streamUrl = "https://stream.radio$i.com",
        frequency = "90.$i FM",
        playedAt = 1000L * i
      )
      dao.insertRecent(entity)
    }

    val recents = dao.getRecentStationsList()
    // Must return at most 5 stations
    assertEquals(5, recents.size)
    // Most recent must be radio_6 (timestamp 6000L)
    assertEquals("radio_6", recents[0].id)
    assertEquals("radio_5", recents[1].id)
    assertEquals("radio_4", recents[2].id)
    assertEquals("radio_3", recents[3].id)
    assertEquals("radio_2", recents[4].id)

    db.close()
  }

  @Test
  fun `sleep timer presets include 15, 30, 45, 60 minutes`() {
    val presetMinutes = com.example.ui.components.SLEEP_TIMER_PRESETS.map { it.minutes }
    assertEquals(listOf(15, 30, 45, 60), presetMinutes)
  }

  @Test
  fun `startSleepTimer activates sleep timer with countdown and cancelSleepTimer deactivates it`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.RadioViewModel(app)

    // Test 15 minutes preset
    vm.startSleepTimer(15)
    assertTrue(vm.playerState.value.sleepTimerActive)
    assertEquals(15, vm.playerState.value.sleepTimerMinutesRemaining)
    assertEquals(15, vm.playerState.value.sleepTimerSelectedMinutes)
    assertEquals(15 * 60L, vm.playerState.value.sleepTimerTotalSeconds)

    // Test switching to 45 minutes preset
    vm.startSleepTimer(45)
    assertTrue(vm.playerState.value.sleepTimerActive)
    assertEquals(45, vm.playerState.value.sleepTimerMinutesRemaining)
    assertEquals(45, vm.playerState.value.sleepTimerSelectedMinutes)

    // Test cancellation
    vm.cancelSleepTimer()
    assertFalse(vm.playerState.value.sleepTimerActive)
    assertNull(vm.playerState.value.sleepTimerMinutesRemaining)
    assertNull(vm.playerState.value.sleepTimerSecondsRemaining)
  }
}
