package com.cpubooster

import android.app.Activity
import android.app.ActivityManager
import android.os.BatteryManager
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var cpuBar: ProgressBar

    private var lastIdle = 0L
    private var lastTotal = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        updateStats()

        content.postDelayed(object : Runnable {
            override fun run() {
                if (isFinishing) return
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

        scroll.addView(
            content,
            ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        addLabel(content, "⚡ CPU BOOSTER", 28, neon)
        addLabel(content, "NO ROOT • PERFORMANCE MONITOR", 12, muted)
        addSpace(content, 22)

        val cpuCard = createCard()
        addLabel(cpuCard, "CPU USAGE", 13, muted)

        cpuText = addLabel(cpuCard, "--%", 42, neon)

        cpuBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
            progressTintList = ColorStateList.valueOf(neon)
        }

        cpuCard.addView(
            cpuBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
            )
        )

        addCardToContent(cpuCard)

        val deviceCard = createCard()
        addLabel(deviceCard, "THIẾT BỊ", 16, neon)

        ramText = addLabel(
            deviceCard, "RAM: đang đọc...", 15, Color.WHITE
        )
        tempText = addLabel(
            deviceCard,