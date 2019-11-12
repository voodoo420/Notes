package ru.geekbrains.gb_kotlin.ui.note

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_note.*
import ru.geekbrains.gb_kotlin.R
import ru.geekbrains.gb_kotlin.common.format
import ru.geekbrains.gb_kotlin.common.getColorInt
import ru.geekbrains.gb_kotlin.data.entity.Note
import ru.geekbrains.gb_kotlin.ui.base.BaseActivity
import java.util.*

class NoteActivity: BaseActivity<Note?, NoteViewState>() {

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

    override val viewModel: NoteViewModel by lazy {
        ViewModelProviders.of(this).get(NoteViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val noteId = intent.getStringExtra(EXTRA_NOTE)

        noteId?.let { viewModel.loadNote(it) } ?: let { supportActionBar?.title = getString(R.string.new_note) }

        initView()
    }

    override fun renderData(data: Note?) {
        this.note = data

        supportActionBar?.title = note?.run {
            note?.lastChanged?.format(DATE_FORMAT)
        }?: getString(R.string.new_note)

        initView()
    }

    private fun initView() {
        et_title.removeTextChangedListener(textChangeListener)
        et_body.removeTextChangedListener(textChangeListener)

        note?.let { note ->
             et_title.setText(note.title)
            et_body.setText(note.text)
            toolbar.setBackgroundColor(note.color.getColorInt(this))
        }

        et_body.addTextChangedListener(textChangeListener)
        et_title.addTextChangedListener(textChangeListener)

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
            lastChanged = Date()
        ) ?: Note(UUID.randomUUID().toString(), et_title.text.toString(), et_body.text.toString())

        note?.let { viewModel.save(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
