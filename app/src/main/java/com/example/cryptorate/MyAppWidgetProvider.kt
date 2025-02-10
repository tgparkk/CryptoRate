package com.example.coinexchange

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import kotlinx.coroutines.*

/**
 * 코인환율 앱 위젯 프로바이더
 */
class MyAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.coinexchange.ACTION_REFRESH"
    }

    /**
     * 위젯 업데이트 시 호출 (예: 위젯 추가, 주기적 업데이트 등)
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 모든 위젯 인스턴스에 대해 업데이트 처리
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * 위젯 관련 브로드캐스트 수신 (새로고침 버튼 클릭 등)
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            // 새로고침 버튼 클릭 이벤트 처리
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                fetchLatestData(context, appWidgetId)
            }
        }
    }

    /**
     * 네트워크 호출(모의) 후 위젯 UI 업데이트
     */
    fun fetchLatestData(context: Context, appWidgetId: Int) {
        // CoroutineScope를 사용하여 백그라운드에서 데이터를 가져옵니다.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 네트워크 호출 모의 (실제 구현 시 Retrofit 등 사용)
                val exchangeRate = fetchExchangeRate()
                val bitcoinPrice = fetchBitcoinPrice()

                // UI 업데이트는 메인 스레드에서 수행
                withContext(Dispatchers.Main) {
                    updateWidgetUI(context, appWidgetId, exchangeRate, bitcoinPrice)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 모의: 환율 데이터 가져오기 (실제 API 호출 구현 필요)
     */
    private suspend fun fetchExchangeRate(): String {
        delay(1000)  // 네트워크 지연 모의
        // 예시: "1달러 → 1,200원" (실제 값으로 교체)
        return "1,200.00"
    }

    /**
     * 모의: 비트코인 가격 데이터 가져오기 (실제 API 호출 구현 필요)
     */
    private suspend fun fetchBitcoinPrice(): String {
        delay(1000)  // 네트워크 지연 모의
        // 예시: "BTC 30,000,000원" (실제 값으로 교체)
        return "30,000,000"
    }

    /**
     * 위젯 UI 업데이트
     */
    private fun updateWidgetUI(
        context: Context,
        appWidgetId: Int,
        exchangeRate: String,
        bitcoinPrice: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.tvExchangeRate, "USD → KRW: $exchangeRate")
        views.setTextViewText(R.id.tvBitcoinPrice, "BTC: $bitcoinPrice")

        // 새로고침 버튼의 PendingIntent 재설정
        val intent = Intent(context, MyAppWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
        views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent)

        // 위젯 업데이트
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
    }
}

/**
 * 위젯 인스턴스 업데이트 (앱 실행 시 혹은 위젯 추가 시 호출)
 */
fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_layout)
    // 초기 텍스트 설정
    views.setTextViewText(R.id.tvExchangeRate, "USD → KRW: 로딩중...")
    views.setTextViewText(R.id.tvBitcoinPrice, "BTC: 로딩중...")

    // 새로고침 버튼 클릭 이벤트 등록
    val intent = Intent(context, MyAppWidgetProvider::class.java).apply {
        action = MyAppWidgetProvider.ACTION_REFRESH
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
    val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
    views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent)

    // 위젯 초기 업데이트
    appWidgetManager.updateAppWidget(appWidgetId, views)

    // 위젯 생성 시 최신 데이터 호출
    MyAppWidgetProvider().fetchLatestData(context, appWidgetId)
}
