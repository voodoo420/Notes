package ru.geekbrains.gb_kotlin.ui.main

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ScrollingTabContainerView
import androidx.core.content.ContextCompat
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_note.view.*
import ru.geekbrains.gb_kotlin.R
import ru.geekbrains.gb_kotlin.common.format
import ru.geekbrains.gb_kotlin.data.entity.Note

class NotesRVAdapter(val onItemClick: ((Note) -> Unit)? = null) : RecyclerView.Adapter<NotesRVAdapter.ViewHolder>() {

    var notes: List<Note> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false))
    override fun getItemCount() = notes.size
    override fun onBindViewHolder(vh: ViewHolder, pos: Int) = vh.bind(notes[pos])

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(note: Note) = with(itemView) {
            tv_title.text = note.title
            tv_text.text = note.text
            val color = when(note.color){
                Note.Color.WHITE -> R.color.white
                Note.Color.YELLOW -> R.color.yellow
                Note.Color.GREEN -> R.color.green
                Note.Color.BLUE -> R.color.blue
                Note.Color.RED -> R.color.red
                Note.Color.VIOLET -> R.color.violet

            }
          //  tv_date.text = note?.lastChanged?.format("dd.MM.yy HH:mm")
            setBackgroundColor(ContextCompat.getColor(itemView.context, color))
            itemView.setOnClickListener{
                onItemClick?.invoke(note)
            }
        }

    }
}