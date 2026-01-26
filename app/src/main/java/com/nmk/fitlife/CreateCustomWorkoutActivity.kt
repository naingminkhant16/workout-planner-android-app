package com.nmk.fitlife

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlin.properties.Delegates

class CreateCustomWorkoutActivity : AppCompatActivity() {
    private lateinit var etWorkoutName: EditText
    private lateinit var etWorkoutDescription: EditText
    private lateinit var exerciseContainer: LinearLayout
    private lateinit var equipmentContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnAddEquipment: Button
    private lateinit var btnSaveWorkout: Button
    private lateinit var btnCancel: Button

    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@CreateCustomWorkoutActivity)
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

    private var AUTH_ID by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_custom_workout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        AUTH_ID = authPrefs.getInt("id", 0)

        initializeUIComponents()

        addExerciseField()
        addEquipmentField()

        btnAddExercise.setOnClickListener {
            addExerciseField()
        }

        btnAddEquipment.setOnClickListener {
            addEquipmentField()
        }

        btnSaveWorkout.setOnClickListener {
            saveWorkout()
        }

        btnCancel.setOnClickListener {
            startActivity(Intent(this@CreateCustomWorkoutActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun initializeUIComponents() {
        etWorkoutName = findViewById(R.id.etWorkoutName)
        etWorkoutDescription = findViewById(R.id.etWorkoutDescription)
        exerciseContainer = findViewById(R.id.exerciseContainer)
        equipmentContainer = findViewById(R.id.equipmentContainer)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnAddEquipment = findViewById(R.id.btnAddEquipment)
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun addExerciseField() {
        // Main Layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }

        // Horizontal Row for Name, Sets, and Reps
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Exercise Name
        val editTextExeName = EditText(this).apply {
            hint = "Name"
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
        }

        // Sets
        val editTextSets = EditText(this).apply {
            hint = "Sets"
            inputType = InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Reps
        val editTextReps = EditText(this).apply {
            hint = "Reps"
            inputType = InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        rowLayout.addView(editTextExeName)
        rowLayout.addView(editTextSets)
        rowLayout.addView(editTextReps)

        // Instruction
        val editTextInstruction = EditText(this).apply {
            hint = "Instruction"
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minLines = 2
        }

        mainLayout.addView(rowLayout)
        mainLayout.addView(editTextInstruction)

        exerciseContainer.addView(mainLayout)
    }

    private fun addEquipmentField() {
        val parentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setPadding(0, 8, 0, 8)
        }

        val editTextName = EditText(this).apply {
            hint = "Equipment Name"
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val editTextRemark = EditText(this).apply {
            hint = "Remark"
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minLines = 2
        }

        parentLayout.addView(editTextName)
        parentLayout.addView(editTextRemark)

        equipmentContainer.addView(parentLayout)
    }

    private fun saveWorkout() {
        val workoutName = etWorkoutName.text.toString().trim()
        val workoutDescription = etWorkoutDescription.text.toString().trim()

        if (workoutName.isEmpty()) {
            etWorkoutName.error = "Workout name required"
            return
        }

        // Workout
        val newWorkout = Workout(
            title = workoutName,
            description = workoutDescription,
            userId = AUTH_ID,
            isTemplate = false,
            isCompleted = false,
            locationId = null,
            createdAt = System.currentTimeMillis().toString()
        )

        // Exercise List
        val exerciseList = mutableListOf<Exercise>()
        for (i in 0 until exerciseContainer.childCount) {
            val mainLayout = exerciseContainer.getChildAt(i) as? LinearLayout ?: continue

            val rowLayout = mainLayout.getChildAt(0) as? LinearLayout ?: continue

            val name = (rowLayout.getChildAt(0) as? EditText)?.text.toString()
            val sets = (rowLayout.getChildAt(1) as? EditText)?.text.toString()
            val reps = (rowLayout.getChildAt(2) as? EditText)?.text.toString()

            val instruction = (mainLayout.getChildAt(1) as? EditText)?.text.toString()

            if (name.isNotBlank() && sets.isNotBlank() && reps.isNotBlank()) {
                exerciseList.add(
                    Exercise(
                        name = name,
                        sets = sets.toInt(),
                        reps = reps.toInt(),
                        instructions = instruction,
                        isCompleted = false,
                        workoutId = 1, // dummy id
                        imageUrl = null
                    )
                )
            }
        }
        if (exerciseList.isEmpty()) {
            Toast.makeText(this, "Workout should have at least one exercise!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val equipmentList = mutableListOf<Equipment>()
        for (i in 0 until equipmentContainer.childCount) {
            val parent = equipmentContainer.getChildAt(i) as? LinearLayout ?: continue

            val name = (parent.getChildAt(0) as? EditText)?.text.toString()
            val remark = (parent.getChildAt(1) as? EditText)?.text.toString()

            if (name.isNotBlank()) {
                equipmentList.add(
                    Equipment(
                        name = name,
                        remark = remark,
                        workoutId = 1 // dummy id
                    )
                )
            }
        }

        workoutViewModel.createWorkout(newWorkout, exerciseList, equipmentList)

        Toast.makeText(this, "Workout Created", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}