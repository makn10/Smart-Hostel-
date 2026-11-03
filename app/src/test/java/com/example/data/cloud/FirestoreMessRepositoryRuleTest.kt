package com.example.data.cloud

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreMessRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun userProfile_createAndRetrieve_succeeds() = runBlocking {
    val email = "manager_${System.currentTimeMillis()}@test.com"
    val uid = signInTestUser(email)
    val repo = FirestoreMessRepository(firestore, auth)

    withTimeout(5000L) {
      repo.saveUserProfile(name = "Md Abdul Kuddus", email = email)
      val profile = repo.getUserProfile(uid)
      assertNotNull(profile)
      assertEquals("Md Abdul Kuddus", profile?.name)
      assertEquals(email, profile?.email)
    }
  }

  @Test
  fun mess_create_succeeds() = runBlocking {
    val managerEmail = "owner_${System.currentTimeMillis()}@test.com"
    val managerUid = signInTestUser(managerEmail)
    val managerRepo = FirestoreMessRepository(firestore, auth)

    val mess = withTimeout(5000L) {
      managerRepo.createMess(
        name = "Rose Garden Hostel",
        mealSystem = "shared",
        fixedRatePaisa = 0L,
        managerName = "Manager Bhai"
      )
    }

    assertNotNull(mess)
    assertEquals("Rose Garden Hostel", mess.name)
    assertTrue(mess.joinCode.length == 6)

    val retrieved = withTimeout(5000L) {
      managerRepo.getMess(mess.id)
    }
    assertNotNull(retrieved)
    assertEquals(mess.name, retrieved?.name)
  }

  @Test
  fun mess_joinByCode_succeeds() = runBlocking {
    val managerEmail = "manager_${System.currentTimeMillis()}@test.com"
    signInTestUser(managerEmail)
    val managerRepo = FirestoreMessRepository(firestore, auth)
    val mess = withTimeout(5000L) {
      managerRepo.createMess(
        name = "Dhaka City Hostel",
        mealSystem = "fixed",
        fixedRatePaisa = 6000L,
        managerName = "Hostel Admin"
      )
    }

    val found = withTimeout(5000L) {
      managerRepo.joinMessByCode(mess.joinCode)
    }
    assertNotNull(found)
    assertEquals(mess.id, found?.id)
  }

  @Test
  fun bazarRecord_syncToCloud_succeeds() = runBlocking {
    val shopperEmail = "shopper_${System.currentTimeMillis()}@test.com"
    signInTestUser(shopperEmail)
    val repo = FirestoreMessRepository(firestore, auth)

    val recordId = withTimeout(5000L) {
      repo.syncBazarRecord(
        messId = "test_mess_123",
        date = "2026-11-02",
        shopperName = "Abdul Kuddus",
        itemDescription = "Beef and Mustard Oil",
        category = "Meat",
        totalPaisa = 75000L,
        addToDeposit = true
      )
    }

    assertNotNull(recordId)
    assertTrue(recordId.startsWith("bazar_"))
  }
}
