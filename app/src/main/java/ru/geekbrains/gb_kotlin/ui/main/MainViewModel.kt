package ru.geekbrains.gb_kotlin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.geekbrains.gb_kotlin.data.NotesRepository

class MainViewModel : ViewModel() {

    private val viewStateLiveData: MutableLiveData<MainViewState> = MutableLiveData()

    init {
        NotesRepository.getNotes().observeForever{notes ->
            notes?.let {viewStateLiveData.value = viewStateLiveData.value?.copy(notes = it!!)?: MainViewState(it)}
        }
    }

    fun viewState(): LiveData<MainViewState> = viewStateLiveData
}