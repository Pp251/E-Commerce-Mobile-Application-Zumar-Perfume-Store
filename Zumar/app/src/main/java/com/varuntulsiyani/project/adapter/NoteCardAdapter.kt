package com.varuntulsiyani.project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varuntulsiyani.project.R

class NoteCardAdapter(
    private val notes: MutableList<String>,
    private val selectedNotes: MutableSet<String>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<NoteCardAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.noteTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note_card, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        val isSelected = selectedNotes.any { it.equals(note, ignoreCase = true) }

        holder.title.text = if (isSelected) "$note ✓" else note
        holder.itemView.setBackgroundResource(
            if (isSelected) R.drawable.bg_note_card_selected else R.drawable.bg_note_card
        )

        holder.itemView.setOnClickListener {
            if (isSelected) {
                selectedNotes.removeAll { it.equals(note, ignoreCase = true) }
            } else {
                selectedNotes.add(note)
            }
            notifyItemChanged(position)
            onSelectionChanged()
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<String>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}
