package com.nmk.fitlife.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nmk.fitlife.R
import com.nmk.fitlife.WorkoutDetailsActivity
import com.nmk.fitlife.data.workout.Workout

class TemplateWorkoutItemAdapter(private val context: Context, private val list: List<Workout>) :
    RecyclerView.Adapter<TemplateWorkoutItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.template_workout_rv_item,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val workout = list[position]

        holder.title.text = workout.title
        holder.description.text = workout.description

        holder.btnView.setOnClickListener {
            val intent = Intent(context, WorkoutDetailsActivity::class.java)
            intent.putExtra("workoutId", workout.id)
            intent.putExtra("isTemplate", workout.isTemplate)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvWorkoutTitle)
        val description: TextView = view.findViewById(R.id.tvWorkoutDescription)
        val btnView: Button = view.findViewById(R.id.btnView)
    }
}