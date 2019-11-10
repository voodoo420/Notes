package ru.geekbrains.gb_kotlin.data.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.data.model.NoteResult
import timber.log.Timber

class FireStoreProvider: RemoteDataProvider {
    companion object{
        private const val NOTE_COLLECTION = "notes"
    }

    private val store = FirebaseFirestore.getInstance()
    private val notesReference = store.collection(NOTE_COLLECTION)

    override fun subscribeToAllNotes(): LiveData<NoteResult> {
        val result = MutableLiveData<NoteResult>()
        notesReference.addSnapshotListener{snapshot, e ->
            e?.let { result.value = NoteResult.Error(it) } ?: let {
                snapshot?.let { val notes = mutableListOf<Note>()
                for (doc: QueryDocumentSnapshot in snapshot) {
                    notes.add(doc.toObject(Note::class.java))
                }
                    result.value = NoteResult.Success(notes)
                }
            }
        }
        return result
    }

    override fun getNoteById(id: String): LiveData<NoteResult> {
        val result = MutableLiveData<NoteResult>()
        notesReference.document(id).get()
            .addOnSuccessListener { snapshot -> result.value = NoteResult.Success(snapshot.toObject(Note::class.java)) }
            .addOnFailureListener { result.value = NoteResult.Error(it) }
        return result
    }

    override fun saveNote(note: Note): LiveData<NoteResult> {
        val result = MutableLiveData<NoteResult>()
        notesReference.document(note.id).set(note)
            .addOnSuccessListener {
                Timber.d("Note $note is saved")
                result.value = NoteResult.Success(note) }
            .addOnFailureListener {
                Timber.d("Error saving not $note, message: ${it.message}")
                result.value = NoteResult.Error(it) }
        return result
    }
}