package ru.geekbrains.gb_kotlin.data.provider

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.data.entity.User
import ru.geekbrains.gb_kotlin.data.errors.NoAuthException
import ru.geekbrains.gb_kotlin.data.model.NoteResult
import timber.log.Timber

class FireStoreProvider(private val firebaseAuth: FirebaseAuth, private val store: FirebaseFirestore): RemoteDataProvider {
    companion object{
        private const val NOTE_COLLECTION = "notes"
        private const val USERS_COLLECTION = "users"
    }

    private val currentUser
        get() = firebaseAuth.currentUser

    override fun getCurrentUser() = MutableLiveData<User?>().apply {
        value = currentUser?.let {
            User(it.displayName ?: "", it.email ?: "")
        }
    }

    private fun getUserNotesCollection() = currentUser?.let {
        store.collection(USERS_COLLECTION).document(it.uid).collection(NOTE_COLLECTION)
    } ?: throw NoAuthException()

    override fun subscribeToAllNotes() = MutableLiveData<NoteResult>().apply {
        try {
            getUserNotesCollection().addSnapshotListener{ snapshot, e ->
                e?.let { value = NoteResult.Error(it) } ?: let {
                    snapshot?.let {
                       // val notes = mutableListOf<Note>()
                        val notes = it.documents.map { it.toObject(Note::class.java) }

/*                        for (doc: QueryDocumentSnapshot in snapshot) {
                            notes.add(doc.toObject(Note::class.java))
                        }*/
                        value = NoteResult.Success(notes)
                    }
                }
            }
        } catch (e: Throwable) {
            value = NoteResult.Error(e)
        }
    }

    override fun getNoteById(id: String) = MutableLiveData<NoteResult>().apply {
         try {
             getUserNotesCollection().document(id).get()
                .addOnSuccessListener { snapshot -> value = NoteResult.Success(snapshot.toObject(Note::class.java)) }
                .addOnFailureListener { value = NoteResult.Error(it) }
         } catch (e: Throwable) {
             value = NoteResult.Error(e)
         }
    }

    override fun saveNote(note: Note) =  MutableLiveData<NoteResult>().apply {
        try {
            getUserNotesCollection().document(note.id).set(note)
                .addOnSuccessListener {
                    Timber.d("Note $note is saved")
                    value = NoteResult.Success(note) }
                .addOnFailureListener {
                    Timber.d("Error saving not $note, message: ${it.message}")
                    value = NoteResult.Error(it) }
        } catch (e: Throwable) {
            value = NoteResult.Error(e)
        }
    }

    override fun deleteNote(noteId: String) = MutableLiveData<NoteResult>().apply {
        try {
            getUserNotesCollection().document(noteId)
                .delete()
                .addOnSuccessListener { snapshot ->
                    value = NoteResult.Success(null)
                }.addOnFailureListener {
                    value = NoteResult.Error(it)
                }
        } catch (e: Throwable) {
            value = NoteResult.Error(e)
        }
    }

}