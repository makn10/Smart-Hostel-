package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.model.BazarRecord
import com.example.core.model.DepositRecord
import com.example.core.model.MealEntry
import com.example.core.model.Notice
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreService(private val context: Context) {

  private val TAG = "FirestoreService"

  // CRITICAL: Always initialize with R.string.firestore_database_id
  val db: FirebaseFirestore = FirebaseFirestore.getInstance(
    context.applicationContext.getString(R.string.firestore_database_id)
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  suspend fun saveUserProfile(user: FirebaseUser, currentMessId: String? = null) {
    try {
      val uid = user.uid
      val userDoc = db.collection("users").document(uid)
      val snapshot = userDoc.get().await()

      val data = mutableMapOf<String, Any>(
        "uid" to uid,
        "displayName" to (user.displayName ?: "Mess Member"),
        "email" to (user.email ?: ""),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      user.photoUrl?.toString()?.let { data["photoUrl"] = it }
      if (!currentMessId.isNullOrBlank()) {
        data["currentMessId"] = currentMessId
      }

      if (!snapshot.exists()) {
        data["createdAt"] = FieldValue.serverTimestamp()
        userDoc.set(data).await()
      } else {
        userDoc.set(data, SetOptions.merge()).await()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error saving user profile: ${e.message}", e)
    }
  }

  suspend fun getUserCurrentMessId(uid: String): String? {
    return try {
      val doc = db.collection("users").document(uid).get().await()
      doc.getString("currentMessId")
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching user current mess: ${e.message}", e)
      null
    }
  }

  suspend fun createMess(
    name: String,
    mealSystem: String, // "fixed" or "shared"
    mealRatePaisa: Long,
    managerUid: String,
    managerName: String
  ): Result<String> {
    return try {
      val messId = "mess_" + UUID.randomUUID().toString().take(8)
      val joinCode = (100000..999999).random().toString()

      val messData = hashMapOf(
        "id" to messId,
        "name" to name.trim(),
        "code" to joinCode,
        "managerId" to managerUid,
        "mealSystem" to mealSystem,
        "mealRatePaisa" to mealRatePaisa,
        "currencySymbol" to "৳",
        "memberIds" to listOf(managerUid),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )

      val messRef = db.collection("messes").document(messId)
      messRef.set(messData).await()

      // Add Manager as first member in members subcollection
      val memberId = "mem_mgr_" + managerUid.take(6)
      val memberData = hashMapOf(
        "id" to memberId,
        "userId" to managerUid,
        "name" to managerName,
        "role" to "manager",
        "phone" to "",
        "email" to (auth.currentUser?.email ?: ""),
        "active" to true,
        "openingBalancePaisa" to 0L,
        "joinedAt" to FieldValue.serverTimestamp()
      )
      messRef.collection("members").document(memberId).set(memberData).await()

      // Link to User Profile
      saveUserProfile(auth.currentUser!!, currentMessId = messId)

      Result.success(messId)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to create mess: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun joinMessByCode(code: String, userUid: String, userName: String): Result<String> {
    return try {
      val query = db.collection("messes")
        .whereEqualTo("code", code.trim())
        .limit(1)
        .get()
        .await()

      if (query.isEmpty) {
        return Result.failure(IllegalArgumentException("Invalid Mess Code. No mess found matching '$code'."))
      }

      val messDoc = query.documents.first()
      val messId = messDoc.id
      val messRef = db.collection("messes").document(messId)

      // Add user to memberIds array
      messRef.update("memberIds", FieldValue.arrayUnion(userUid), "updatedAt", FieldValue.serverTimestamp()).await()

      // Add to members subcollection
      val memberId = "mem_" + userUid.take(6)
      val memberData = hashMapOf(
        "id" to memberId,
        "userId" to userUid,
        "name" to userName,
        "role" to "member",
        "phone" to "",
        "email" to (auth.currentUser?.email ?: ""),
        "active" to true,
        "openingBalancePaisa" to 0L,
        "joinedAt" to FieldValue.serverTimestamp()
      )
      messRef.collection("members").document(memberId).set(memberData).await()

      // Save user profile with current mess ID
      saveUserProfile(auth.currentUser!!, currentMessId = messId)

      Result.success(messId)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to join mess: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun saveDailyMeal(
    messId: String,
    date: String,
    memberId: String,
    memberName: String,
    breakfast: Double,
    lunch: Double,
    dinner: Double,
    guests: Double = 0.0
  ) {
    try {
      val docId = "${date}_${memberId}"
      val data = hashMapOf(
        "id" to docId,
        "date" to date,
        "memberId" to memberId,
        "memberName" to memberName,
        "breakfast" to breakfast,
        "lunch" to lunch,
        "dinner" to dinner,
        "guests" to guests,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      db.collection("messes").document(messId)
        .collection("meals").document(docId)
        .set(data, SetOptions.merge())
        .await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving meal to Firestore: ${e.message}", e)
    }
  }

  suspend fun saveBazarRecord(
    messId: String,
    record: BazarRecord
  ) {
    try {
      val itemsSummary = record.items.joinToString(", ") { "${it.name} (${it.qty} ${it.unit})" }
      val data = hashMapOf(
        "id" to record.id,
        "date" to record.date,
        "shopperId" to record.shopperId,
        "shopperName" to record.shopperName,
        "totalPaisa" to record.totalPaisa,
        "itemsSummary" to itemsSummary,
        "addToDeposit" to record.addToDeposit,
        "createdAt" to FieldValue.serverTimestamp()
      )
      db.collection("messes").document(messId)
        .collection("bazar").document(record.id)
        .set(data)
        .await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving bazar to Firestore: ${e.message}", e)
    }
  }

  suspend fun saveDepositRecord(
    messId: String,
    deposit: DepositRecord
  ) {
    try {
      val data = hashMapOf(
        "id" to deposit.id,
        "date" to deposit.date,
        "memberId" to deposit.memberId,
        "memberName" to (deposit.addedBy),
        "amountPaisa" to deposit.amountPaisa,
        "paymentMethod" to deposit.method.name,
        "note" to deposit.note,
        "createdAt" to FieldValue.serverTimestamp()
      )
      db.collection("messes").document(messId)
        .collection("deposits").document(deposit.id)
        .set(data)
        .await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving deposit to Firestore: ${e.message}", e)
    }
  }

  suspend fun saveNotice(
    messId: String,
    title: String,
    description: String,
    pinned: Boolean,
    authorName: String
  ) {
    try {
      val noticeId = UUID.randomUUID().toString()
      val data = hashMapOf(
        "id" to noticeId,
        "title" to title,
        "description" to description,
        "pinned" to pinned,
        "authorName" to authorName,
        "createdAt" to FieldValue.serverTimestamp()
      )
      db.collection("messes").document(messId)
        .collection("notices").document(noticeId)
        .set(data)
        .await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving notice to Firestore: ${e.message}", e)
    }
  }

  fun observeMessDetails(messId: String): Flow<Map<String, Any?>?> {
    return db.collection("messes").document(messId).snapshots().map { snapshot ->
      if (snapshot.exists()) snapshot.data else null
    }
  }
}
