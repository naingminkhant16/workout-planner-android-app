package com.nmk.fitlife.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nmk.fitlife.R
import com.nmk.fitlife.WorkoutDetailsActivity
import com.nmk.fitlife.data.workout.WeeklyWorkoutDto
import com.nmk.fitlife.service.getEndOfWeekDate
import com.nmk.fitlife.service.getStartOfWeekDate

class WeeklyWorkoutItemAdapter(
    private val context: Context,
    private val list: List<WeeklyWorkoutDto>
) :
    RecyclerView.Adapter<WeeklyWorkoutItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.weekly_workout_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val workout: WeeklyWorkoutDto = list[position]
        holder.tvWorkoutTitle.text = workout.title
        holder.tvWorkoutDescription.text = workout.description

        if (workout.isCompleted) {
            holder.tvWorkoutStatus.text = "Completed"
            holder.tvWorkoutStatus.setBackgroundResource(R.drawable.bg_status_completed)
        } else {
            holder.tvWorkoutStatus.text = "Not Completed"
            holder.tvWorkoutStatus.setBackgroundResource(R.drawable.bg_status_pending)
        }

        holder.tvWeekRange.text = getStartOfWeekDate() + " - " + getEndOfWeekDate()
        holder.tvDayOfWeek.text = workout.dayOfWeek

        holder.itemView.setOnClickListener {
            val intent = Intent(context, WorkoutDetailsActivity::class.java)
            intent.putExtra("workoutId", workout.workoutId)
            intent.putExtra("isTemplate", false)
            intent.putExtra("alreadyAdded", true)
            context.startActivity(intent)
        }
    }

    fun getItem(position: Int): WeeklyWorkoutDto = list[position]

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeekRange: TextView = view.findViewById(R.id.tvWeekRange)
        val tvWorkoutStatus: TextView = view.findViewById(R.id.tvWorkoutStatus)
        val tvWorkoutTitle: TextView = view.findViewById(R.id.tvWorkoutTitle)
        val tvWorkoutDescription: TextView = view.findViewById(R.id.tvWorkoutDescription)
        val tvDayOfWeek: TextView = view.findViewById(R.id.tvDayOfWeek)
    }
}