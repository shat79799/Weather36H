package com.example.weather36h.ui

import com.example.weather36h.model.ForecastItem

sealed class WeatherUiState {
    // 初始狀態：顯示輸入框與引導文字
    object Initial : WeatherUiState()

    // 查詢中狀態：顯示進度條
    object Loading : WeatherUiState()

    // 查詢成功狀態：城市名稱與 36 小時預報資料
    data class Success(
        val cityName: String,
        val forecastList: List<ForecastItem>
    ) : WeatherUiState()

    // 查詢失敗狀態：錯誤訊息
    data class Error(val message: String) : WeatherUiState()
}