package com.example.weather36h.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// 使用 @JsonClass 讓 Moshi 自動生成解析程式碼
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "records") val records: WeatherRecords
)

@JsonClass(generateAdapter = true)
data class WeatherRecords(
    @Json(name = "location") val location: List<LocationData>
)

@JsonClass(generateAdapter = true)
data class LocationData(
    @Json(name = "locationName") val locationName: String, // 縣市名稱
    @Json(name = "weatherElement") val weatherElement: List<WeatherElement>
)

@JsonClass(generateAdapter = true)
data class WeatherElement(
    @Json(name = "elementName") val elementName: String, // Wx(天氣現象), PoP(降雨機率), MinT(最低溫), MaxT(最高溫)
    @Json(name = "time") val time: List<TimePeriod>
)

@JsonClass(generateAdapter = true)
data class TimePeriod(
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "parameter") val parameter: ParameterData
)

@JsonClass(generateAdapter = true)
data class ParameterData(
    @Json(name = "parameterName") val parameterName: String, // 數值或數值名稱
    @Json(name = "parameterValue") val parameterValue: String? = null // 單位或權重
)

/**
 * 為了讓 UI 層更好讀取，建立一個「最終呈現用」資料結構
 */
data class ForecastItem(
    val startTime: String,
    val endTime: String,
    val weatherStatus: String,  // 天氣現象 (Wx)
    val rainProbability: String, // 降雨機率 (PoP)
    val minTemperature: String,  // 最低溫 (MinT)
    val maxTemperature: String   // 最高溫 (MaxT)
)