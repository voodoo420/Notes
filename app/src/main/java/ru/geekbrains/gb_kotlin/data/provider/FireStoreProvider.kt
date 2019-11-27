package ru.geekbrains.gb_kotlin.data.provider

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.data.entity.User
import ru.geekbrains.gb_kotlin.data.errors.NoAuthException
import ru.geekbrains.gb_kotlin.data.model.NoteResult
import timber.log.Timber
import java.lang.NullPointerException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStoreProvider(private val firebaseAuth: FirebaseAuth, private val store: FirebaseFirestore): RemoteDataProvider {
    companion object{
        private const val NOTE_COLLECTION = "notes"
        private const val USERS_COLLECTION = "users"
    }

    private val currentUser
        get() = firebaseAuth.currentUser

    override suspend fun getCurrentUser(): User? = suspendCoroutine { continuation ->
        continuation.resume(currentUser?.let {
            User(it.displayName ?: "", it.email ?: "")})
    }

    private fun getUserNotesCollection() = currentUser?.let {
        store.collection(USERS_COLLECTION).document(it.uid).collection(NOTE_COLLECTION)
    } ?: throw NoAuthException()

    override fun subscribeToAllNotes(): ReceiveChannel<NoteResult> = Channel<NoteResult>(Channel.CONFLATED).apply {
        var registration: ListenerRegistration? = null
        try {
            registration = getUserNotesCollection().addSnapshotListener{ snapshot, e ->
                val value = e?.let { NoteResult.Error(it) } ?: let {
                    snapshot?.let {
                        val notes = it.documents.map { it.toObject(Note::class.java) }
                        NoteResult.Success(notes)
                    }
                }
                value?.let { offer(it) }
            }
        } catch (e: Throwable) {
            offer(NoteResult.Error(e))
        }

        invokeOnClose { registration?.remove() }
    }

    override suspend fun getNoteById(id: String): Note = suspendCoroutine { continuation ->
         try {
             getUserNotesCollection().document(id).get()
                .addOnSuccessListener { snapshot -> continuation.resume( snapshot.toObject(Note::class.java)!!) }
                .addOnFailureListener { continuation.resumeWithException(it) }
         } catch (e: Throwable) {
             continuation.resumeWithException(e)
         }
    }

    override suspend fun saveNote(note: Note): Note = suspendCoroutine { continuation ->
        try {
            getUserNotesCollection().document(note.id).set(note)
                .addOnSuccessListener {
                    Timber.d("Note $note is saved")
                    continuation.resume(note) }
                .addOnFailureListener {
                    Timber.d("Error saving not $note, message: ${it.message}")
                    continuation.resumeWithException(it)
                }
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Unit = suspendCoroutine { continuation ->
        try {
            getUserNotesCollection().document(noteId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

}