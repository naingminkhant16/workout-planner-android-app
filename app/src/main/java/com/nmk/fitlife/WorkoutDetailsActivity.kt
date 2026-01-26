package com.nmk.fitlife

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nmk.fitlife.data.database.AppDatabase
import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.Exercise
import com.nmk.fitlife.data.exercise.ExerciseRepository
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanRepository
import com.nmk.fitlife.data.workout.Workout
import com.nmk.fitlife.data.workout.WorkoutRepository
import com.nmk.fitlife.data.workout.WorkoutViewModel
import com.nmk.fitlife.data.workout.WorkoutViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class WorkoutDetailsActivity : AppCompatActivity() {
    private lateinit var exerciseContainer: LinearLayout
    private lateinit var equipmentContainer: LinearLayout
    private lateinit var etWorkoutName: EditText
    private lateinit var etWorkoutDescription: EditText
    private lateinit var btnAddExercise: Button
    private lateinit var btnAddEquipment: Button
    private lateinit var btnUpdate: Button
    private lateinit var ivBack: ImageView
    private lateinit var fabEdit: FloatingActionButton
    private var workoutId by Delegates.notNull<Int>()
    private var workout: Workout? = null
    private lateinit var exercises: List<Exercise>
    private lateinit var equipments: List<Equipment>
    private var isEditMode = false
    private lateinit var btnAddToWeeklyPlan: Button

    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@WorkoutDetailsActivity)
        WorkoutViewModelFactory(
            WorkoutRepository(
                db.workoutDao(),
                db.exerciseDao(),
                db.equipmentDao()
            ),
            ExerciseRepository(db.exerciseDao()),
            EquipmentRepository(db.equipmentDao()),
            WeeklyPlanRepository(db.weeklyPlanDao(), db.weeklyPlanWorkoutDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeUIComponents()

        workoutId = intent.getIntExtra("workoutId", 0)
        val isTemplate = intent.getBooleanExtra("isTemplate", true)
        val alreadyAddedToPlan = intent.getBooleanExtra("alreadyAdded", false)
        updateUIState()

        if (isTemplate) {
            fabEdit.visibility = View.GONE
        }

        if (alreadyAddedToPlan) {
            btnAddToWeeklyPlan.visibility = View.GONE
        }

        loadWorkoutData()

        fabEdit.setOnClickListener {
            isEditMode = !isEditMode
            updateUIState()
        }

        btnAddExercise.setOnClickListener { addExerciseField() }
        btnAddEquipment.setOnClickListener { addEquipmentField() }

        btnUpdate.setOnClickListener {
            saveData()
        }

        ivBack.setOnClickListener {
            val activity = if (isTemplate or alreadyAddedToPlan) MainActivity::class.java
            else MyWorkoutsActivity::class.java

            startActivity(
                Intent(
                    this@WorkoutDetailsActivity,
                    activity
                )
            )
            finish()
        }

        btnAddToWeeklyPlan.setOnClickListener {
            addWorkoutToWeeklyPlan()
        }
    }

    private fun initializeUIComponents() {
        etWorkoutName = findViewById(R.id.etWorkoutName)
        etWorkoutDescription = findViewById(R.id.etWorkoutDescription)
        exerciseContainer = findViewById(R.id.detailExerciseContainer)
        equipmentContainer = findViewById(R.id.detailEquipmentContainer)
        btnAddExercise = findViewById(R.id.btnDetailAddExercise)
        btnAddEquipment = findViewById(R.id.btnDetailAddEquipment)
        btnUpdate = findViewById(R.id.btnUpdateWorkout)
        fabEdit = findViewById(R.id.fabEdit)
        ivBack = findViewById(R.id.ivBack)
        btnAddToWeeklyPlan = findViewById(R.id.btnAddToWeeklyPlan)
    }

    private fun updateUIState() {
        etWorkoutName.isEnabled = isEditMode
        etWorkoutDescription.isEnabled = isEditMode

        setViewsEnabled(exerciseContainer, isEditMode)
        setViewsEnabled(equipmentContainer, isEditMode)

        btnAddExercise.visibility = if (isEditMode) View.VISIBLE else View.GONE
        btnAddEquipment.visibility = if (isEditMode) View.VISIBLE else View.GONE
        btnUpdate.visibility = if (isEditMode) View.VISIBLE else View.GONE

        fabEdit.setImageResource(if (isEditMode) android.R.drawable.ic_menu_save else android.R.drawable.ic_menu_edit)
    }

    private fun setViewsEnabled(view: View, enabled: Boolean) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setViewsEnabled(view.getChildAt(i), enabled)
            }
        } else if (view is EditText) {
            view.isEnabled = enabled
        }
    }

    private fun loadWorkoutData() {
        lifecycleScope.launch {
            // Get Workout
            workout = workoutViewModel.getWorkoutById(workoutId)
            if (workout == null) {
                startActivity(Intent(this@WorkoutDetailsActivity, MainActivity::class.java))
                finish()
                return@launch
            }

            etWorkoutName.setText(workout?.title)
            etWorkoutDescription.setText(workout?.description)

            // Get Exercises
            launch {
                workoutViewModel.getExercisesByWorkoutId(workoutId).collect { exes ->
                    exercises = exes
                    exerciseContainer.removeAllViews()
                    exes.forEach { exercise ->
                        addExerciseField(
                            exercise.name,
                            exercise.sets.toString(),
                            exercise.reps.toString(),
                            exercise.instructions.orEmpty()
                        )
                    }
                }
            }

            // Get Equipments
            launch {
                workoutViewModel.getEquipmentsByWorkoutId(workoutId).collect { es ->
                    equipments = es
                    equipmentContainer.removeAllViews()
                    es.forEach { equipment ->
                        addEquipmentField(
                            equipment.name,
                            equipment.remark.orEmpty()
                        )
                    }
                }
            }
        }
    }

    private fun addExerciseField(
        name: String = "",
        sets: String = "",
        reps: String = "",
        instr: String = ""
    ) {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val etName = EditText(this).apply {
            hint = "Name"; setText(name); isEnabled = isEditMode
            layoutParams = LinearLayout.LayoutParams(0, -2, 2f)
        }
        val etSets = EditText(this).apply {
            hint = "Sets"; setText(sets); isEnabled = isEditMode
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f); inputType =
            InputType.TYPE_CLASS_NUMBER
        }
        val etReps = EditText(this).apply {
            hint = "Reps"; setText(reps); isEnabled = isEditMode
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f); inputType =
            InputType.TYPE_CLASS_NUMBER
        }

        row.addView(etName); row.addView(etSets); row.addView(etReps)

        val etInstr = EditText(this).apply {
            hint = "Instructions"; setText(instr); isEnabled = isEditMode
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        mainLayout.addView(row)
        mainLayout.addView(etInstr)
        exerciseContainer.addView(mainLayout)
    }

    private fun addEquipmentField(name: String = "", remark: String = "") {
        val parent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val etName =
            EditText(this).apply { hint = "Equipment"; setText(name); isEnabled = isEditMode }
        val etRem =
            EditText(this).apply { hint = "Remark"; setText(remark); isEnabled = isEditMode }

        parent.addView(etName)
        parent.addView(etRem)
        equipmentContainer.addView(parent)
    }

    private fun saveData() {
        val workoutName = etWorkoutName.text.toString()
        val workoutDesc = etWorkoutDescription.text.toString()

        if (workoutName.isBlank()) {
            etWorkoutName.error = "Name required"
            return
        }

        val workoutId = workout?.id ?: 0

        // Retrieve Exercises - Both edited and newly added
        val exerciseList = mutableListOf<Exercise>()
        for (i in 0 until exerciseContainer.childCount) {
            val mainLayout = exerciseContainer.getChildAt(i) as? LinearLayout ?: continue
            val row = mainLayout.getChildAt(0) as? LinearLayout ?: continue

            val name = (row.getChildAt(0) as? EditText)?.text.toString()
            val sets = (row.getChildAt(1) as? EditText)?.text.toString()
            val reps = (row.getChildAt(2) as? EditText)?.text.toString()
            val instr = (mainLayout.getChildAt(1) as? EditText)?.text.toString()

            if (name.isNotBlank()) {
                exerciseList.add(
                    Exercise(
                        name = name,
                        sets = sets.toInt(),
                        reps = reps.toInt(),
                        instructions = instr,
                        workoutId = workoutId,
                        imageUrl = null,
                    )
                )
            }
        }

        // Retrieve Equipments - Both edited and newly added
        val equipmentList = mutableListOf<Equipment>()
        for (i in 0 until equipmentContainer.childCount) {
            val parent = equipmentContainer.getChildAt(i) as? LinearLayout ?: continue
            val name = (parent.getChildAt(0) as? EditText)?.text.toString()
            val remark = (parent.getChildAt(1) as? EditText)?.text.toString()

            if (name.isNotBlank()) {
                equipmentList.add(Equipment(name = name, remark = remark, workoutId = workoutId))
            }
        }

        val updatedWorkout = workout?.copy(title = workoutName, description = workoutDesc) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                workoutViewModel.updateWorkoutWithDetails(
                    updatedWorkout,
                    exerciseList,
                    equipmentList
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@WorkoutDetailsActivity,
                        "Workout Updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                    isEditMode = false
                    updateUIState()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@WorkoutDetailsActivity,
                        "Update Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun addWorkoutToWeeklyPlan() {
        val authId = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .getInt("id", 0)

        val days = resources.getStringArray(R.array.days_items)
        var selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Select the workout day")
            .setSingleChoiceItems(days, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Submit") { _, _ ->
                val dayOfWeek = days[selectedIndex]

                try {
                    val w = workout ?: return@setPositiveButton
                    if (w.isTemplate) {
                        workoutViewModel.createWorkoutFromTemplate(
                            w,
                            authId = authId,
                            dayOfWeek = dayOfWeek
                        )
                    } else {
                        workoutViewModel.addToWeeklyPlan(w, authId, dayOfWeek)
                    }

                    Toast.makeText(
                        this@WorkoutDetailsActivity,
                        "Workout Added!",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@WorkoutDetailsActivity,
                        "Add Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}