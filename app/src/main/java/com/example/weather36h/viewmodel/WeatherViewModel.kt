package com.example.weather36h.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather36h.model.ForecastItem
import com.example.weather36h.model.WeatherResponse
import com.example.weather36h.service.WeatherApiClient
import com.example.weather36h.ui.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val TAG = "Weather36H"
    private val apiKey = "CWA-F789313B-CFBB-4B07-A88F-C8EF35EE1711"

    // 內部讀寫的 StateFlow，預設為初始狀態
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    // 對外公開、唯讀的 StateFlow 供 Compose 監聽
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /**
     * 執行天氣查詢
     */
    fun searchWeather(cityName: String) {
        // 防呆：若未輸入直接返回
        if (cityName.isBlank()) {
            _uiState.value = WeatherUiState.Error("請輸入要查詢的城市名稱")
            return
        }

        // 啟動協程進行非同步網路請求
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                val response = WeatherApiClient.service.get36HourForecast(apiKey, cityName.trim())
                val locationList = response.records.location
                Log.i(TAG, "result: ${locationList}")

                if (locationList.isNotEmpty()) {
                    // 解析成功，轉換資料結構
                    val parsedForecast = parseWeatherResponse(response)
                    _uiState.value = WeatherUiState.Success(
                        cityName = locationList.first().locationName,
                        forecastList = parsedForecast
                    )
                } else {
                    // API 回傳成功但找不到該城市資料 (搜尋條件無效)
                    _uiState.value = WeatherUiState.Error("找不到「$cityName」的天氣資料，請檢查字詞是否正確（例如：臺北市、高雄市）")
                }
            } catch (e: Exception) {
                // 處理網路異常、連線逾時或 API 格式有誤
                Log.e(TAG, "api error: ${e.message}", e)
                _uiState.value = WeatherUiState.Error("連線或資料解析失敗")
            }
        }
    }

    /**
     * 清除搜尋結果，回到初始狀態
     */
    fun clearResult() {
        _uiState.value = WeatherUiState.Initial
    }

    /**
     * 將原始 API JSON 結構轉換成 UI 好讀的 List<ForecastItem>
     */
    private fun parseWeatherResponse(response: WeatherResponse): List<ForecastItem> {
        val location = response.records.location.first()
        val elements = location.weatherElement

        // 找出對應的天氣要素
        val wxElement = elements.find { it.elementName == "Wx" }
        val popElement = elements.find { it.elementName == "PoP" }
        val minTElement = elements.find { it.elementName == "MinT" }
        val maxTElement = elements.find { it.elementName == "MaxT" }

        val forecastItems = mutableListOf<ForecastItem>()

        // 36小時預報通常固定有 3 個時間段 (每 12 小時一段)
        val timeSlotsCount = wxElement?.time?.size ?: 0
        for (i in 0 until timeSlotsCount) {
            val startTime = wxElement?.time?.get(i)?.startTime ?: ""
            val endTime = wxElement?.time?.get(i)?.endTime ?: ""

            val weatherStatus = wxElement?.time?.get(i)?.parameter?.parameterName ?: "未知"
            val rainProbability = popElement?.time?.get(i)?.parameter?.parameterName ?: "0"
            val minTemp = minTElement?.time?.get(i)?.parameter?.parameterName ?: "--"
            val maxTemp = maxTElement?.time?.get(i)?.parameter?.parameterName ?: "--"

            forecastItems.add(
                ForecastItem(
                    startTime = formatTime(startTime),
                    endTime = formatTime(endTime),
                    weatherStatus = weatherStatus,
                    rainProbability = "$rainProbability%",
                    minTemperature = "$minTemp°C",
                    maxTemperature = "$maxTemp°C"
                )
            )
        }
        return forecastItems
    }

    /**
     * 簡單格式化時間字串 (ex: 2026-05-20 18:00:00 -> 05/20 18:00)
     */
    private fun formatTime(timeStr: String): String {
        return try {
            // 氣象局格式通常為 "yyyy-MM-dd HH:mm:ss"
            if (timeStr.length >= 16) {
                timeStr.substring(5, 16).replace("-", "/")
            } else {
                timeStr
            }
        } catch (e: Exception) {
            timeStr
        }
    }
}