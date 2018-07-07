package com.sollyu.android.adb.input.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import com.sollyu.android.adb.input.R

/**
 * 作者：sollyu
 * 时间：2018/7/7
 * 说明：
 */
class ServiceIME : InputMethodService() {
    var receiverIME: ReceiverIME? = null

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

    inner class ReceiverIME : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "ADB_INPUT_TEXT" -> {
                    currentInputConnection.commitText(intent.getStringExtra("msg"), 1)
                }
                "ADB_INPUT_CLEAR" -> {
                    currentInputConnection.setSelection(0, currentInputConnection.getExtractedText(ExtractedTextRequest(), 0).text.length)
                    currentInputConnection.commitText("",0)
                }
            }
        }
    }
}