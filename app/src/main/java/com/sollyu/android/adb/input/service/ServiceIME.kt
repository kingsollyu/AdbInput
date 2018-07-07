package com.sollyu.android.adb.input.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.inputmethodservice.InputMethodService
import android.text.method.MetaKeyKeyListener
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import com.sollyu.android.adb.input.R

/**
 * 作者：sollyu
 * 时间：2018/7/7
 * 说明：
 */
class ServiceIME : InputMethodService() {
    var receiverIME: ReceiverIME? = null

    private var isShifted = false
    private var metaState = 0L
    private var composingStringBuilder: StringBuilder? = null

    override fun onCreateInputView(): View {
        val inputView = layoutInflater.inflate(R.layout.view, null)
        if (receiverIME == null) {
            receiverIME = ReceiverIME()
            val intentFilter = IntentFilter("ADB_INPUT_TEXT")
            intentFilter.addAction("ADB_INPUT_CLEAR")
            registerReceiver(receiverIME, intentFilter)
        }
        return inputView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        if (restarting) {
            isShifted = false
            metaState = 0L
        }
        composingStringBuilder = null
    }

    override fun onFinishInput() {
        super.onFinishInput()
        composingStringBuilder = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val c = getUnicodeChar(keyCode, event)
        if (c == 0)
            return super.onKeyDown(keyCode, event)

        if (!isShifted) {
            if (c == '~'.toInt()) {
                isShifted = true
                composingStringBuilder = StringBuilder()
                return true
            }
            return super.onKeyDown(keyCode, event)
        }

        if (c == '$'.toInt()) {
            isShifted = false
            val normalString = String(Base64.decode(composingStringBuilder?.toString()?.toByteArray(), Base64.NO_WRAP))
            //val normalString = String(composingStringBuilder?.toString()?.toByteArray(Charset.forName("US-ASCII"))!!, modifiedUtf7Charset!!)
            currentInputConnection.commitText(normalString, 1)
            composingStringBuilder = null
        } else {
            appendComposing(c)
        }

        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        metaState = MetaKeyKeyListener.handleKeyUp(metaState, keyCode, event)
        return super.onKeyUp(keyCode, event)
    }

    private fun getUnicodeChar(keyCode: Int, keyEvent: KeyEvent?): Int {
        metaState = MetaKeyKeyListener.handleKeyDown(metaState, keyCode, keyEvent)
        val c = keyEvent?.getUnicodeChar(MetaKeyKeyListener.getMetaState(metaState))
        metaState = MetaKeyKeyListener.adjustMetaAfterKeypress(metaState)
        return c ?: 0
    }

    private fun commitCharacter(c: Int) {
        currentInputConnection.commitText(c.toString(), 1)
    }

    private fun appendComposing(c: Int) {
        composingStringBuilder?.append(c.toChar())
    }

    inner class ReceiverIME : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "ADB_INPUT_TEXT" -> {
                    currentInputConnection.commitText(intent.getStringExtra("msg"), 1)
                }
                "ADB_INPUT_CLEAR" -> {
                    currentInputConnection.setSelection(0, currentInputConnection.getExtractedText(ExtractedTextRequest(), 0).text.length)
                    currentInputConnection.commitText("", 0)
                }
            }
        }
    }
}