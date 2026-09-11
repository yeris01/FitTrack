package com.fittrack.app.ai

import com.fittrack.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AIExercise(val name: String, val muscleGroup: String, val sets: Int, val reps: String)
data class AIDayRoutine(val dayLabel: String, val exercises: List<AIExercise>)
data class AIRoutinePlan(val planName: String, val days: List<AIDayRoutine>)

sealed class AIResult {
    data class Success(val plan: AIRoutinePlan) : AIResult()
    data class Error(val message: String) : AIResult()
}

/**
 * Genera una rutina de entrenamiento llamando a la API de Claude (Anthropic).
 *
 * IMPORTANTE: en esta implementación la API key vive en el APK (vía BuildConfig), lo cual
 * es aceptable solo para prototipar. Para producción, esta llamada debería hacerse desde
 * un backend propio que guarde la key de forma segura y no la exponga al cliente.
 */
class AIRoutineService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateRoutine(
        daysPerWeek: Int,
        goal: String,
        experienceLevel: String,
        equipment: String
    ): AIResult = withContext(Dispatchers.IO) {
        if (BuildConfig.ANTHROPIC_API_KEY.isBlank()) {
            return@withContext AIResult.Error(
                "Falta configurar la API key de Anthropic en local.properties (ANTHROPIC_API_KEY=...)."
            )
        }

        val systemPrompt = """
            Sos un entrenador personal experto. Generá un plan de entrenamiento de gimnasio.
            Respondé ÚNICAMENTE con un JSON válido, sin texto adicional, sin backticks de markdown,
            con exactamente esta forma:
            {
              "planName": "string corto describiendo el objetivo",
              "days": [
                {
                  "dayLabel": "string, ej: Día 1 - Pecho y Tríceps",
                  "exercises": [
                    { "name": "string", "muscleGroup": "PECHO|ESPALDA|PIERNAS|HOMBROS|BICEPS|TRICEPS|ABDOMINALES|GLUTEOS|CARDIO", "sets": number, "reps": "string ej 8-10" }
                  ]
                }
              ]
            }
            El array "days" debe tener exactamente la cantidad de días por semana solicitada.
            Cada día debe tener entre 4 y 6 ejercicios. Usá "muscleGroup" solo con los valores permitidos.
        """.trimIndent()

        val userPrompt = """
            Quiero entrenar $daysPerWeek días por semana.
            Objetivo: $goal.
            Nivel de experiencia: $experienceLevel.
            Equipamiento disponible: $equipment.
        """.trimIndent()

        val bodyJson = JSONObject().apply {
            put("model", "claude-sonnet-5")
            put("max_tokens", 2000)
            put("system", systemPrompt)
            put("messages", JSONArray().put(
                JSONObject().put("role", "user").put("content", userPrompt)
            ))
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext AIResult.Error("Error de la API (${response.code}): $responseBody")
                }

                val root = JSONObject(responseBody)
                val contentArray = root.optJSONArray("content") ?: JSONArray()
                val text = StringBuilder()
                for (i in 0 until contentArray.length()) {
                    val block = contentArray.getJSONObject(i)
                    if (block.optString("type") == "text") {
                        text.append(block.optString("text"))
                    }
                }

                val plan = parsePlan(text.toString())
                    ?: return@withContext AIResult.Error("No se pudo interpretar la respuesta de la IA.")
                AIResult.Success(plan)
            }
        } catch (e: Exception) {
            AIResult.Error("Fallo de red: ${e.message}")
        }
    }

    private fun parsePlan(rawText: String): AIRoutinePlan? {
        return try {
            // Por si el modelo agrega texto extra, buscamos el primer '{' y el último '}'.
            val start = rawText.indexOf('{')
            val end = rawText.lastIndexOf('}')
            val jsonText = rawText.substring(start, end + 1)
            val json = JSONObject(jsonText)

            val planName = json.optString("planName", "Rutina generada por IA")
            val daysArray = json.getJSONArray("days")
            val days = mutableListOf<AIDayRoutine>()

            for (i in 0 until daysArray.length()) {
                val dayObj = daysArray.getJSONObject(i)
                val dayLabel = dayObj.optString("dayLabel", "Día ${i + 1}")
                val exercisesArray = dayObj.getJSONArray("exercises")
                val exercises = mutableListOf<AIExercise>()
                for (j in 0 until exercisesArray.length()) {
                    val exObj = exercisesArray.getJSONObject(j)
                    exercises.add(
                        AIExercise(
                            name = exObj.optString("name"),
                            muscleGroup = exObj.optString("muscleGroup", "PECHO"),
                            sets = exObj.optInt("sets", 3),
                            reps = exObj.optString("reps", "8-10")
                        )
                    )
                }
                days.add(AIDayRoutine(dayLabel, exercises))
            }

            AIRoutinePlan(planName, days)
        } catch (e: Exception) {
            null
        }
    }
}
