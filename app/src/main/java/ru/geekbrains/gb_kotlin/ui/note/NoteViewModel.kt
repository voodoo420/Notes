package ru.geekbrains.gb_kotlin.ui.note

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.launch
import ru.geekbrains.gb_kotlin.data.NotesRepository
import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.ui.base.BaseViewModel

class NoteViewModel(private val notesRepository: NotesRepository) : BaseViewModel<NoteData>() {

    private val currentNote: Note?
    get() = getViewState().poll()?.note

    fun save(note: Note){
        setData( NoteData(note = note))
    }

    @VisibleForTesting
    public override fun onCleared() {
        launch {
            currentNote?.let {
                notesRepository.saveNote(it)
            }
            super.onCleared()
        }
    }

    fun loadNote(noteId: String) {
        launch {
            try {
                notesRepository.getNoteById(noteId).let {
                    setData(NoteData(note = it))
                }
            } catch (e: Throwable) {
                setError(e)
            }
        }
    }

    fun deleteNote() {
        launch {
            try {
                currentNote?.let { notesRepository.deleteNote(it.id) }
                setData(NoteData(isDeleted = true))
            } catch (e: Throwable) {
                setError(e)
            }
        }
    }
}