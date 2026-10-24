package com.quicktext.panel.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.quicktext.panel.data.repository.ThemeMode
import com.quicktext.panel.di.ServiceEntryPoint
import com.quicktext.panel.ui.theme.QuickTextTheme
import com.quicktext.panel.util.ServiceLifecycleOwner
import com.quicktext.panel.util.isPasswordInputType
import dagger.hilt.android.EntryPointAccessors

/**
 * Input method. It never stores or transmits keystrokes, is disabled in password fields,
 * and only inserts a template when the user taps it. Enter is sent only when the user taps it.
 */
class QuickTextIme : InputMethodService() {

    private val owner = ServiceLifecycleOwner()
    private var passwordField by mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()
        owner.create()
        window?.window?.decorView?.let { owner.attachTo(it) }
        owner.resume()
    }

    override fun onCreateInputView(): View {
        val entry = EntryPointAccessors.fromApplication(applicationContext, ServiceEntryPoint::class.java)
        val repository = entry.templateRepository()
        val settings = entry.settingsRepository()

        val actions = object : KeyboardActions {
            override fun commit(text: String) {
                if (!passwordField) currentInputConnection?.commitText(text, 1)
            }

            override fun backspace() {
                if (!passwordField) sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            }

            override fun enter() {
                if (passwordField) return
                if (!sendDefaultEditorAction(true)) sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            }

            override fun switchKeyboard() {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        }

        return ComposeView(this).apply {
            setContent {
                val mode by remember { settings.themeMode }.collectAsState(initial = ThemeMode.DARK)
                val boxes by remember { repository.observeBoxes() }.collectAsState(initial = emptyList())
                val templates by remember { repository.observeTemplates() }.collectAsState(initial = emptyList())
                QuickTextTheme(mode) {
                    KeyboardContent(
                        passwordField = passwordField,
                        boxes = boxes,
                        templates = templates,
                        actions = actions,
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        passwordField = info != null && isPasswordInputType(info.inputType)
    }

    override fun onDestroy() {
        owner.destroy()
        super.onDestroy()
    }
}
