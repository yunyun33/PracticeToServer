package com.example.practicetoserver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    val scope = CoroutineScope(Dispatchers.Default)

    var count:Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener(){

            if (count == 1) {
                scope.launch {
                    apiTask()
                }
            } else {
                //何度も通信することを回避する
                return@setOnClickListener
            }
        }
    }

    private suspend fun apiTask() {
        try {
            val httpRequest = ApiTask()

            /*
            URL中のIPアドレスは、ループバックアドレス(自分自身を指すIPアドレス)であり、
            エミュレーター で使用するとエミュレーター自身にアクセスしてしまうため、
            そのままAndroidStudioで使用しても、WebAPIにアクセスすることはできない。
            これを解決するには、URLのIPアドレスを[10.0.2.2]に変更する必要がある。
             */
            var success: Boolean =
                httpRequest.request("http://10.0.2.2:3000")

            val jsonStr = httpRequest.getModel()
            val jsonObject = JSONObject(jsonStr)

            val name = jsonObject.getString("name")
            val age = jsonObject.getString("age")
            val food = jsonObject.getString("favorite food")

            var siroData = MinisiroData(name, age, food)

            //通信終了時の処理
            withContext(Dispatchers.Main) {
                Log.i("データ", "${siroData}")
                findViewById<TextView>(R.id.nameText).append("${siroData.name}")
                findViewById<TextView>(R.id.ageText).append("${siroData.age}")
                findViewById<TextView>(R.id.favoriteFoodText).append("${siroData.food}")

                count += 1
            }

        } catch (e: Exception) {
            Log.e(localClassName, "onCancelled", e)
        }
    }
}

class ApiTask  {
    private var status = 0
    private var model = "";
    fun request(requestUrl: String): Boolean {
        if (requestUrl.isEmpty()) {
            Log.w(TAG, "URLが空です。")
            return false
        }

        val url = URL(requestUrl)
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        urlConnection.connect()
        status = urlConnection.responseCode
        val br = BufferedReader(InputStreamReader(urlConnection.inputStream))
        val sb = StringBuilder()
        for (line: String? in br.readLines()) {
            line?.let { sb.append(line) }
        }
        br.close()
        model = sb.toString()
        return true

    }

    fun getModel(): String {
        return model
    }

    companion object {
        private const val TAG = "HttpRequest"
    }
}

data class MinisiroData(
    val name: String,
    val age: String,
    val food: String
)