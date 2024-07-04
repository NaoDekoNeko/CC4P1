package com.example.kotlinclientfinal

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private var fileUri: Uri? = null
    private lateinit var fileNameTextView: TextView
    private lateinit var taskTypeSpinner: Spinner
    private lateinit var keywordEditText: EditText
    private lateinit var repetitionsEditText: EditText
    private lateinit var resultTextView: TextView
    private var leaderUrl: String? = null

    companion object {
        private const val PICK_FILE_REQUEST_CODE = 1
        private const val REQUEST_PERMISSIONS_CODE = 2
        private const val BASE_URL = "http://3.83.250.253"
        private val WORKER_URLS = listOf(
            "$BASE_URL:5001",
            "$BASE_URL:5003",
            "$BASE_URL:5004"
        )
        private val TASK_TYPES = mapOf(
            "Word Count" to "word_count",
            "Keyword Search" to "keyword_search",
            "Keyword Repetition" to "keyword_repetition"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileNameTextView = findViewById(R.id.fileNameTextView)
        taskTypeSpinner = findViewById(R.id.taskTypeSpinner)
        keywordEditText = findViewById(R.id.keywordEditText)
        repetitionsEditText = findViewById(R.id.repetitionsEditText)
        resultTextView = findViewById(R.id.resultTextView)

        val selectFileButton: Button = findViewById(R.id.selectFileButton)
        val submitTaskButton: Button = findViewById(R.id.submitTaskButton)

        selectFileButton.setOnClickListener {
            checkPermissionsAndOpenFilePicker()
        }

        submitTaskButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                submitTask()
            }
        }

        val taskTypeAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, TASK_TYPES.keys.toList()
        )
        taskTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskTypeSpinner.adapter = taskTypeAdapter

        taskTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTaskType = parent.getItemAtPosition(position).toString()
                if (selectedTaskType == "Word Count") {
                    keywordEditText.visibility = View.VISIBLE
                    repetitionsEditText.visibility = View.GONE
                } else if (selectedTaskType == "Keyword Search") {
                    keywordEditText.visibility = View.VISIBLE
                    repetitionsEditText.visibility = View.GONE
                } else if (selectedTaskType == "Keyword Repetition") {
                    keywordEditText.visibility = View.VISIBLE
                    repetitionsEditText.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun checkPermissionsAndOpenFilePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS_CODE)
        } else {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fileUri = data?.data
            fileUri?.let { uri ->
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    fileNameTextView.text = fileName
                }
            }
        }
    }

    private suspend fun getLeaderUrl(): String? {
        for (worker in WORKER_URLS) {
            try {
                val request = Request.Builder()
                    .url("$worker/is_leader")
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    if (jsonResponse.getBoolean("is_leader")) {
                        return worker
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private suspend fun submitTask() {
        leaderUrl = getLeaderUrl()
        if (leaderUrl == null) {
            resultTextView.text = "Error: No leader available"
            return
        }

        val selectedTaskType = taskTypeSpinner.selectedItem.toString()
        val taskType = TASK_TYPES[selectedTaskType] ?: return
        val keyword = keywordEditText.text.toString()
        val repetitions = repetitionsEditText.text.toString().toIntOrNull()

        if (fileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileContent = contentResolver.openInputStream(fileUri!!)?.bufferedReader().use { it?.readText() }

            val json = JSONObject().apply {
                put("client_id", "your_client_id")  // Ajusta esto según sea necesario
                put("file_content", fileContent)
                put("task_type", taskType)
                put("keyword", keyword)
                repetitions?.let { put("n", it) }
            }

            val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())

            val request = Request.Builder()
                .url("$leaderUrl/leader_submit_task")
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            displayResult("Error: ${e.message}", false)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.body?.string()?.let { responseBody ->
                            runOnUiThread {
                                displayResult(responseBody, response.isSuccessful)
                            }
                        }
                    }
                })
            }
        } catch (e: Exception) {
            resultTextView.text = "Error: ${e.message}"
        }
    }

    private fun displayResult(response: String, isSuccess: Boolean) {
        val jsonResponse = JSONObject(response)
        val result = jsonResponse.optString("result")
        val status = jsonResponse.optString("status")

        val resultHtml = if (isSuccess) {
            when (taskTypeSpinner.selectedItem.toString()) {
                "Word Count" -> "Número de coincidencias: $result"
                "Keyword Search" -> if (result.toBoolean()) "Palabra clave encontrada" else "Palabra clave no encontrada"
                "Keyword Repetition" -> if (result.toBoolean()) "Se encontraron al menos ${repetitionsEditText.text} repeticiones de la palabra clave." else "No se encontraron suficientes repeticiones de la palabra clave."
                else -> "Resultado desconocido"
            }
        } else {
            "Error: $status"
        }

        val color = if (isSuccess) "#03DAC5" else "#CF6679"
        resultTextView.text = resultHtml
        resultTextView.setBackgroundColor(Color.parseColor(color))
        resultTextView.setTextColor(Color.parseColor("#FFFFFF"))
        resultTextView.textSize = 18f
        resultTextView.setPadding(16, 16, 16, 16)
    }
}