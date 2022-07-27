package com.android.keepyournotes.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.keepyournotes.R
import com.android.keepyournotes.activity.MainActivity
import com.android.keepyournotes.activity.WriteNoteActivity

class NoteRecyclerAdapter(private var listOfNotes: ArrayList<WriteNoteActivity.Note>) :
    RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = listOfNotes[position]
        holder.title.text = note.title
        holder.content.text = note.content
        holder.date.text = note.date
    }

    override fun getItemCount(): Int {
        return listOfNotes.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var content: TextView
        var date: TextView

        init {
            title = itemView.findViewById(R.id.tvTitleNote)
            content = itemView.findViewById(R.id.tvContentNote)
            date = itemView.findViewById(R.id.tvDateNote)

            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                val intent = Intent(itemView.context, WriteNoteActivity::class.java)
                intent.putExtra("key", listOfNotes[position].key)
                itemView.context.startActivity(intent)
            }
        }
    }
}