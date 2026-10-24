package com.quicktext.panel.service

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import com.quicktext.panel.di.ServiceEntryPoint
import com.quicktext.panel.util.ServiceLifecycleOwner
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * Optional movable overlay. It only draws a window; it never reads other apps and never types
 * anything. Tapping a template copies it to the clipboard for the user to paste.
 */
class FloatingPanelService : Service() {

    private val owner = ServiceLifecycleOwner()
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        val entry = EntryPointAccessors.fromApplication(applicationContext, ServiceEntryPoint::class.java)
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 240
        }

        owner.create()
        val view = ComposeView(this)
        owner.attachTo(view)
        view.setContent {
            FloatingPanelContent(
                repository = entry.templateRepository(),
                settings = entry.settingsRepository(),
                onDrag = { dx, dy -> moveBy(dx, dy) },
                onExpandedChange = { setFocusable(it) },
                onCopy = { copyToClipboard(it) },
                onClose = { stopSelf() },
            )
        }
        owner.resume()
        try {
            wm.addView(view, params)
            composeView = view
            _running.value = true
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't show the floating panel", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }

    private fun moveBy(dx: Float, dy: Float) {
        val view = composeView ?: return
        params.x += dx.roundToInt()
        params.y += dy.roundToInt()
        windowManager?.updateViewLayout(view, params)
    }

    /** The search field needs focus (for the keyboard) only while the panel is expanded. */
    private fun setFocusable(focusable: Boolean) {
        val view = composeView ?: return
        params.flags = if (focusable) {
            params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        windowManager?.updateViewLayout(view, params)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("QuickText", text))
        Toast.makeText(this, "Copied. Paste it where you need it.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        composeView?.let { v -> runCatching { windowManager?.removeView(v) } }
        composeView = null
        owner.destroy()
        _running.value = false
        super.onDestroy()
    }

    companion object {
        private val _running = MutableStateFlow(false)
        val running: StateFlow<Boolean> = _running.asStateFlow()

        fun start(context: Context) {
            context.startService(Intent(context, FloatingPanelService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingPanelService::class.java))
        }
    }
}
