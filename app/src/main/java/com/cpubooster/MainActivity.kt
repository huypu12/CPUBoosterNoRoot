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
            actionButton("🎮 Gaming") { setMode("Gaming