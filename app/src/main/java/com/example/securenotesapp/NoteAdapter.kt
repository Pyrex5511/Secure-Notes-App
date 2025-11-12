package com.example.securenotesapp

import android.content.Intent
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onItemClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var notes = listOf<Note>()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.noteTitle)
        val content = itemView.findViewById<TextView>(R.id.noteContent)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        val maxLength = 256
        holder.content.text = if (note.content.length > maxLength) {
            note.content.substring(0, maxLength) + "…"
        } else {
            note.content
        }

        holder.deleteButton.setOnClickListener {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes") { dialogInterface, _ ->
                    onDeleteClick(note)
                    dialogInterface.dismiss()
                }
                .setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Nastavenie vlastného pozadia
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

            dialog.show()

            // Zmena farby tlačidiel
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(holder.itemView.context.getColor(R.color.dark_red))
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(holder.itemView.context.getColor(android.R.color.white))
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, NoteDetailActivity::class.java)
            intent.putExtra("noteId", note.id)
            holder.itemView.context.startActivity(intent)
        }
    }


    fun setNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }


}
