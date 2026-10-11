package com.cpubooster

import android.app.Activity
import android.os.BatteryManager
import android.os.Bundle
import android.os.SystemClock
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class MainActivity : Activity() {

    private val bg = Color.rgb(9, 13, 22)
    private val panel = Color.rgb(20, 28, 42)
    private val neon = Color.rgb(57, 255, 120)
    private val muted = Color.rgb(155, 170, 190)

    private lateinit var content: LinearLayout
    private lateinit var cpuText: TextView
    private lateinit var ramText: TextView
    private lateinit var tempText: TextView
    private lateinit var batteryText: TextView
    private lateinit var statusText: TextView
    private var lastIdle = 0L
    private var lastTotal = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        updateStats()
        content.postDelayed(object : Runnable {
            override fun run() {
                updateStats()
                content.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 26, 20, 24)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        label("⚡ CPU BOOSTER", 28, neon)
        label("NO ROOT • PERFORMANCE MONITOR", 12, muted)
        space(22)

        card {
            label("CPU USAGE", 13, muted)
            cpuText = label("--%", 42, neon)
            val bar = ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            )
            bar.max = 100
            bar.progressTintList =
                android.content.res.ColorStateList.valueOf(neon)
            addView(bar, LinearLayout.LayoutParams(-1, 12))
            tagBar = bar
        }

        card {
            label("THIẾT BỊ", 16, neon)
            ramText = label("RAM: đang đọc...", 15, Color.WHITE)
            tempText = label("Nhiệt độ pin: -- °C", 15, Color.WHITE)
            batteryText = label("Pin: --%", 15, Color.WHITE)
            statusText = label("Chế độ: NO ROOT", 14, muted)
        }

        card {
            label("CHẾ ĐỘ HIỆU NĂNG", 18, neon)
            actionButton("🎮 Gaming") { setMode("Gaming") }
            actionButton("⚖ Cân bằng") { setMode("Cân bằng") }
            actionButton("🔋 Tiết kiệm") { setMode("Tiết kiệm") }
        }

        card {
            label("CÔNG CỤ", 18, neon)
            actionButton("📊 Cập nhật thông số") { updateStats() }
            actionButton("ℹ Giới thiệu giới hạn thiết bị") {
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("CPU Booster No Root")
                    .setMessage(
                        "Bản No Root theo dõi các thông số Android cho phép đọc. " +
                        "Ứng dụng thông thường không thể tự ép xung CPU hoặc " +
                        "đảm bảo tăng FPS nếu hệ điều hành không cấp quyền."
                    )
                    .setPositiveButton("Đóng", null)
                    .show()
            }
        }

        label(
            "Theo dõi hiệu năng • Không đảm bảo ép xung thật",
            12,
            muted
        )
    }

    private lateinit var tagBar: ProgressBar

    private fun label(
        text: String,
        size: Int,
        color: Int
    ): TextView {
        val v = TextView(this)
        v.text = text
        v.textSize = size.toFloat()
        v.setTextColor(color)
        v.setPadding(0, 5, 0, 5)
        content.addView(v)
        return v
    }

    private fun card(block: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            setBackgroundColor(panel)
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = 16
        content.addView(box, params)

        val oldContent = content
        content = box
        block()
        content = oldContent
    }

    private fun space(height: Int) {
        val v = View(this)
        content.addView(v, LinearLayout.LayoutParams(1, height))
    }

    private fun actionButton(
        text: String,
        click: () -> Unit
    ) {
        val b = Button(this)
        b.text = text
        b.setTextColor(neon)
        b.setBackgroundColor(Color.rgb(31, 48, 45))
        b.isAllCaps = false
        b.setOnClickListener { click() }

        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = 5
        content.addView(b, p)
    }

    private fun setMode(mode: String) {
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putString("mode", mode)
            .apply()

        statusText.text = "Chế độ: $mode (chỉ lưu lựa chọn)"
        android.widget.Toast.makeText(
            this,
            "Đã chọn $mode",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateStats() {
        try {
            val lines = File("/proc/stat").readLines().first()
                .trim().split(Regex("\\s+"))

            val values = lines.drop(1).mapNotNull { it.toLongOrNull() }
            if (values.size >= 4) {
                val idle = values[3] + (values.getOrNull(4) ?: 0L)
                val total = values.sum()
                if (lastTotal > 0L && total > lastTotal) {
                    val deltaTotal = total - lastTotal
                    val deltaIdle = idle - lastIdle
                    val usage = (
                        100.0 * (deltaTotal - deltaIdle) / deltaTotal
                    ).toInt().coerceIn(0, 100)

                    cpuText.text = "$usage%"
                    tagBar.progress = usage
                }
                lastIdle = idle
                lastTotal = total
            }
        } catch (_: Exception) {
            cpuText.text = "Không khả dụng"
        }

        try {
            val manager =
                getSystemService(Context.ACTIVITY_SERVICE)
                    as android.app.ActivityManager
            val info = android.app.ActivityManager.MemoryInfo()
            manager.getMemoryInfo(info)

            val used = (info.totalMem - info.availMem) / (1024 * 1024)
            val total = info.totalMem / (1024 * 1024)
            ramText.text = "RAM: $used / $total MB"
        } catch (_: Exception) {
            ramText.text = "RAM: không khả dụng"
        }

        try {
            val intent = registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            if (intent != null) {
                val level = intent.getIntExtra(
                    BatteryManager.EXTRA_LEVEL, -1
                )
                val scale = intent.getIntExtra(
                    BatteryManager.EXTRA_SCALE, 100
                )
                val pct = if (level >= 0 && scale > 0) {
                    level * 100 / scale
                } else -1

                val temp = intent.getIntExtra(
                    BatteryManager.EXTRA_TEMPERATURE, -1
                ) / 10.0

                batteryText.text =
                    if (pct >= 0) "Pin: $pct%" else "Pin: không khả dụng"

                tempText.text =
                    if (temp >= 0) "Nhiệt độ pin: $temp °C"
                    else "Nhiệt độ pin: không khả dụng"
            }
        } catch (_: Exception) {
            tempText.text = "Nhiệt độ pin: không khả dụng"
        }

        statusText.text = "Chế độ: " +
            getSharedPreferences("settings", MODE_PRIVATE)
                .getString("mode", "Cân bằng")
    }
}