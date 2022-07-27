package com.android.keepyournotes.adapter

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.android.keepyournotes.R
import com.android.keepyournotes.activity.MainActivity
import com.android.keepyournotes.activity.WriteNoteActivity
import com.google.android.material.card.MaterialCardView
import kotlin.random.Random

class NoteRecyclerAdapter(private var listOfNotes: ArrayList<WriteNoteActivity.Note>) :
    RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout_note, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = listOfNotes[position]
        holder.title.text = note.title
        holder.content.text = note.content
        holder.date.text = note.date
        holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(getRandomColor()))
    }

    private fun getRandomColor(): Int {
        val colors: ArrayList<Int> = arrayListOf(
            R.color.light_brown,
            R.color.light_yellow_tight,
            R.color.light_orange,
            R.color.light_red,
            R.color.light_green_tight,
            R.color.light_green_dark,
            R.color.light_green_yel,
            R.color.light_blue_tight,
            R.color.light_blue_dark
        )
        val randomColor = Random
        return colors[randomColor.nextInt(colors.size)]
    }

    override fun getItemCount(): Int {
        return listOfNotes.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var content: TextView
        var date: TextView
        var cardView: MaterialCardView

        init {
            title = itemView.findViewById(R.id.tvTitleNote)
            content = itemView.findViewById(R.id.tvContentNote)
            date = itemView.findViewById(R.id.tvDateNote)
            cardView = itemView.findViewById(R.id.cardLayoutNote)

            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                val intent = Intent(itemView.context, WriteNoteActivity::class.java)
                intent.putExtra("key", listOfNotes[position].key)
                itemView.context.startActivity(intent)
            }
        }
    }
}