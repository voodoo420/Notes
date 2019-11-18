package ru.geekbrains.gb_kotlin.ui.note

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.System.DATE_FORMAT
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_note.*
import org.jetbrains.anko.alert
import org.koin.android.viewmodel.ext.android.viewModel
import ru.geekbrains.gb_kotlin.R
import ru.geekbrains.gb_kotlin.common.format
import ru.geekbrains.gb_kotlin.common.getColorInt
import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.ui.base.BaseActivity
import java.util.*

class NoteActivity: BaseActivity<NoteViewState.Data, NoteViewState>() {

    companion object{
        private val EXTRA_NOTE = NoteActivity::class.java.name + "extra.note"
        private const val DATE_FORMAT = "dd.MM.yy HH:mm"
        fun start(context: Context, noteId: String? = null) = Intent(context, NoteActivity::class.java)
            .run {putExtra(EXTRA_NOTE, noteId)
                context.startActivity(this)
            }
    }

    private var note: Note? = null

    override val layoutRes = R.layout.activity_note

    override val model: NoteViewModel by viewModel()

    private var color = Note.Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val noteId = intent.getStringExtra(EXTRA_NOTE)

        noteId?.let {
            model.loadNote(it)
        } ?: let {
            supportActionBar?.title = getString(R.string.new_note)
        }

        colorPicker.onColorClickListener = {
            color = it
            toolbar.setBackgroundColor(color.getColorInt(this))
            saveNote()
        }

        initView()
    }

    override fun renderData(data: NoteViewState.Data) {
        if (data.isDeleted) {
            finish()
            return
        }

        this.note = data.note
        initView()
    }

    private fun initView() {
        et_title.removeTextChangedListener(textChangeListener)
        et_body.removeTextChangedListener(textChangeListener)
        
        note?.let { note ->
            et_title.setText(note.title)
            et_title.setSelection(et_title.length())
            et_body.setText(note.text)
            et_body.setSelection(et_body.length())
            toolbar.setBackgroundColor(note.color.getColorInt(this))
            supportActionBar?.title = note.run {
                lastChanged.format(DATE_FORMAT)
            }
        } ?: let {
            supportActionBar?.title = getString(R.string.new_note)
        }

        et_title.addTextChangedListener(textChangeListener)
        et_body.addTextChangedListener(textChangeListener)
    }

    private val textChangeListener = object : TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            saveNote()
        }
    }

    fun saveNote(){
        if (et_title.text == null || et_title.text!!.length < 3) return

        note = note?.copy(
            title = et_title.text.toString(),
            text = et_body.text.toString(),
            lastChanged = Date(),
            color = color
        ) ?: Note(UUID.randomUUID().toString(), et_title.text.toString(), et_body.text.toString(), color)

        note?.let { model.save(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu) = MenuInflater(this).inflate(R.menu.note, menu).let { true }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> onBackPressed().let { true }
        R.id.palette -> togglePalette().let { true }
        R.id.delete -> deleteNote().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun togglePalette() {
        if (colorPicker.isOpen) {
            colorPicker.close()
        } else {
            colorPicker.open()
        }
    }

    private fun deleteNote() {
        alert {
            messageResource = R.string.note_delete_message
            negativeButton(R.string.note_delete_cancel) { dialog -> dialog.dismiss() }
            positiveButton(R.string.note_delete_ok) { model.deleteNote() }
        }.show()
    }

}
