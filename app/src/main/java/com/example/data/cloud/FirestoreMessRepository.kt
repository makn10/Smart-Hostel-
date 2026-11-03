package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

@IgnoreExtraProperties
data class FirestoreUserProfile(
  val id: String = "",
  val userId: String = "",
  val name: String = "",
  val email: String = "",
  val activeMessId: String = "",
  val role: String = "manager",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreMess(
  val id: String = "",
  val name: String = "",
  val mealSystem: String = "shared", // "fixed" or "shared"
  val fixedMealRatePaisa: Long = 0L,
  val joinCode: String = "",
  val managerUid: String = "",
  val managerName: String = "",
  val membersUids: List<String> = emptyList(),
  val currencySymbol: String = "৳",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreBazarRecord(
  val id: String = "",
  val messId: String = "",
  val date: String = "",
  val shopperName: String = "",
  val itemDescription: String = "",
  val category: String = "Vegetables",
  val totalPaisa: Long = 0L,
  val addToDeposit: Boolean = true,
  val creatorUid: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreDeposit(
  val id: String = "",
  val messId: String = "",
  val memberName: String = "",
  val date: String = "",
  val amountPaisa: Long = 0L,
  val method: String = "cash",
  val note: String = "",
  val creatorUid: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

class FirestoreMessRepository(
  val firestore: FirebaseFirestore,
  val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id)),
    FirebaseAuth.getInstance()
  )

  fun requireUserId(): String {
    return auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google")
  }

  // 1. User Profile Management
  suspend fun saveUserProfile(name: String, email: String, activeMessId: String = "") {
    val uid = requireUserId()
    val docRef = firestore.collection("users").document(uid)
    val data = hashMapOf(
      "userId" to uid,
      "name" to name,
      "email" to email,
      "activeMessId" to activeMessId,
      "role" to "manager",
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(data).await()
    Log.d("FirestoreRepo", "User profile saved for $uid")
  }

  suspend fun getUserProfile(uid: String): FirestoreUserProfile? {
    val snapshot = firestore.collection("users").document(uid).get().await()
    return if (snapshot.exists()) snapshot.toObject(FirestoreUserProfile::class.java) else null
  }

  // 2. Mess Creation & Joining
  suspend fun createMess(
    name: String,
    mealSystem: String, // "fixed" or "shared"
    fixedRatePaisa: Long = 0L,
    managerName: String
  ): FirestoreMess {
    val uid = requireUserId()
    val messId = "mess_${UUID.randomUUID().toString().take(8)}"
    val joinCode = generateJoinCode()

    val messDoc = firestore.collection("messes").document(messId)
    val messData = hashMapOf(
      "id" to messId,
      "name" to name,
      "mealSystem" to mealSystem,
      "fixedMealRatePaisa" to fixedRatePaisa,
      "joinCode" to joinCode,
      "managerUid" to uid,
      "managerName" to managerName,
      "membersUids" to listOf(uid),
      "currencySymbol" to "৳",
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    messDoc.set(messData).await()

    // Update activeMessId in user profile
    val userDoc = firestore.collection("users").document(uid).get().await()
    if (userDoc.exists()) {
      firestore.collection("users").document(uid).update(
        mapOf(
          "activeMessId" to messId,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } else {
      saveUserProfile(name = managerName, email = auth.currentUser?.email ?: "manager@mess.com", activeMessId = messId)
    }

    return FirestoreMess(
      id = messId,
      name = name,
      mealSystem = mealSystem,
      fixedMealRatePaisa = fixedRatePaisa,
      joinCode = joinCode,
      managerUid = uid,
      managerName = managerName,
      membersUids = listOf(uid)
    )
  }

  suspend fun joinMessByCode(code: String): FirestoreMess? {
    val uid = requireUserId()
    val cleanCode = code.trim().uppercase()
    val query = firestore.collection("messes")
      .whereEqualTo("joinCode", cleanCode)
      .limit(1)
      .get()
      .await()

    val doc = query.documents.firstOrNull() ?: return null
    val mess = doc.toObject(FirestoreMess::class.java) ?: return null

    // Add UID to members list
    doc.reference.update(
      mapOf(
        "membersUids" to FieldValue.arrayUnion(uid),
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).await()

    // Update activeMessId in user profile
    val memberDoc = firestore.collection("users").document(uid).get().await()
    if (memberDoc.exists()) {
      firestore.collection("users").document(uid).update(
        mapOf(
          "activeMessId" to mess.id,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } else {
      saveUserProfile(name = auth.currentUser?.displayName ?: "Member", email = auth.currentUser?.email ?: "member@mess.com", activeMessId = mess.id)
    }

    return mess
  }

  suspend fun getMess(messId: String): FirestoreMess? {
    val doc = firestore.collection("messes").document(messId).get().await()
    return if (doc.exists()) doc.toObject(FirestoreMess::class.java) else null
  }

  // 3. Cloud Data Sync (Bazar, Deposits, Meals)
  suspend fun syncBazarRecord(
    messId: String,
    date: String,
    shopperName: String,
    itemDescription: String,
    category: String,
    totalPaisa: Long,
    addToDeposit: Boolean
  ): String {
    val uid = requireUserId()
    val recordId = "bazar_${UUID.randomUUID().toString().take(8)}"
    val docRef = firestore.collection("messes").document(messId)
      .collection("bazar_records").document(recordId)

    val payload = hashMapOf(
      "id" to recordId,
      "messId" to messId,
      "date" to date,
      "shopperName" to shopperName,
      "itemDescription" to itemDescription,
      "category" to category,
      "totalPaisa" to totalPaisa,
      "addToDeposit" to addToDeposit,
      "creatorUid" to uid,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    docRef.set(payload).await()
    return recordId
  }

  suspend fun syncDeposit(
    messId: String,
    memberName: String,
    date: String,
    amountPaisa: Long,
    method: String,
    note: String
  ): String {
    val uid = requireUserId()
    val depositId = "dep_${UUID.randomUUID().toString().take(8)}"
    val docRef = firestore.collection("messes").document(messId)
      .collection("deposits").document(depositId)

    val payload = hashMapOf(
      "id" to depositId,
      "messId" to messId,
      "memberName" to memberName,
      "date" to date,
      "amountPaisa" to amountPaisa,
      "method" to method,
      "note" to note,
      "creatorUid" to uid,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    docRef.set(payload).await()
    return depositId
  }

  fun observeBazarRecords(messId: String): Flow<List<FirestoreBazarRecord>> = flow {
    val subCol = firestore.collection("messes").document(messId).collection("bazar_records")
    subCol.snapshots()
      .map { snap -> snap.toObjects(FirestoreBazarRecord::class.java) }
      .collect { emit(it) }
  }

  fun observeDeposits(messId: String): Flow<List<FirestoreDeposit>> = flow {
    val subCol = firestore.collection("messes").document(messId).collection("deposits")
    subCol.snapshots()
      .map { snap -> snap.toObjects(FirestoreDeposit::class.java) }
      .collect { emit(it) }
  }

  private fun generateJoinCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
  }
}
