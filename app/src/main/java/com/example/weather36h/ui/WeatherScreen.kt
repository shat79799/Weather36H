package com.example.weather36h.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weather36h.model.ForecastItem
import com.example.weather36h.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    // 監聽 ViewModel 中的 UI 狀態變化（生命週期安全）
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 記住使用者當前輸入的城市文字
    var cityInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                // 橫向排列：清除按鈕 -> 輸入框 -> 搜尋按鈕
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左上角：清除搜尋結果按鈕
                    IconButton(
                        onClick = {
                            cityInput = "" // 清空輸入欄
                            viewModel.clearResult() // 觸發 ViewModel 回到初始狀態
                        },
                        // 當在初始或查詢中狀態時，禁用清除按鈕
                        enabled = uiState !is WeatherUiState.Initial && uiState !is WeatherUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除搜尋"
                        )
                    }

                    // 中間：文字輸入框
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        placeholder = { Text("輸入城市 (例: 臺北市)", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f) // 自動擠滿中間剩餘空間
                            .padding(horizontal = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        enabled = uiState !is WeatherUiState.Loading // 查詢中禁用輸入
                    )

                    // 右上角：開始進行搜尋按鈕
                    IconButton(
                        onClick = { viewModel.searchWeather(cityInput) },
                        enabled = uiState !is WeatherUiState.Loading && cityInput.isNotBlank() // 查詢中或空白時禁用
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "開始搜尋"
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        // 下方區域：根據四種狀態切換顯示內容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is WeatherUiState.Initial -> {
                    InitialView()
                }
                is WeatherUiState.Loading -> {
                    LoadingView()
                }
                is WeatherUiState.Success -> {
                    SuccessView(cityName = state.cityName, forecastList = state.forecastList)
                }
                is WeatherUiState.Error -> {
                    ErrorView(message = state.message)
                }
            }
        }
    }
}

/**
 * 初始狀態 UI
 */
@Composable
fun InitialView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "歡迎使用天氣預報 App",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "請在上方輸入台灣縣市名稱並按下搜尋按鈕",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 查詢中狀態 UI
 */
@Composable
fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(strokeWidth = 4.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "正在獲取最新天氣預報...", fontSize = 16.sp)
    }
}

/**
* 查詢失敗狀態 UI
*/
@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "發生錯誤",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 查詢成功狀態 UI
 */
@Composable
fun SuccessView(cityName: String, forecastList: List<ForecastItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "$cityName 未來 36 小時天氣預報",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.secondary
        )

        // 類似 RecyclerView 的高效延遲載入清單
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(forecastList) { item ->
                ForecastRowItem(item = item)
            }
        }
    }
}

/**
 * 單一預報時段的卡片元件 (表單細項)
 */
@Composable
fun ForecastRowItem(item: ForecastItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 時間標題區
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "時段: ${item.startTime} ~ ${item.endTime}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            // 天氣資訊網格表單
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "天氣現象", fontSize = 12.sp, color = Color.Gray)
                    Text(text = item.weatherStatus, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "降雨機率", fontSize = 12.sp, color = Color.Gray)
                    Text(text = item.rainProbability, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "溫度區間", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${item.minTemperature} - ${item.maxTemperature}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}