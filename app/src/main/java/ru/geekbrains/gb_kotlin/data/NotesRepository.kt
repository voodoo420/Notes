package ru.geekbrains.gb_kotlin.data

import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.data.provider.RemoteDataProvider

class NotesRepository(private val remoteProvider: RemoteDataProvider) {

   fun getNotes() = remoteProvider.subscribeToAllNotes()
   suspend fun saveNote(note: Note) = remoteProvider.saveNote(note)
   suspend fun getNoteById(id: String) = remoteProvider.getNoteById(id)
   suspend fun getCurrentUser() = remoteProvider.getCurrentUser()
   suspend fun deleteNote(noteId: String) = remoteProvider.deleteNote(noteId)
}