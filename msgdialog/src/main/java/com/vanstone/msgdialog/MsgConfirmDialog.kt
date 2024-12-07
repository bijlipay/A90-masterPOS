package com.vanstone.msgdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

/**
 * Created by Kron.Xu on 2020/2/10 10:10
 * Description:
 */
class MsgConfirmDialog : DialogFragment() {
    private var infoText: String? = null
    private var confirmText: String? = null
    private var isButtonVisible = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_msg_confirm, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvInfo: TextView = view.findViewById<TextView>(R.id.tvInfo)
        val tvConfirm: TextView = view.findViewById<TextView>(R.id.tvConfirm)
        var view = view.findViewById<View>(R.id.view)
        if (!isButtonVisible) {
            tvConfirm.visibility = View.GONE
            view.visibility = View.GONE
        }
        tvInfo.text = infoText
        if (isButtonVisible) {
            tvConfirm.text = confirmText
            tvConfirm.setOnClickListener { onConfirmClickListener!!.onClick() }
        }
    }

    fun setTvInfo(infoText: String?): MsgConfirmDialog {
        this.infoText = infoText
        return this
    }

    fun setConfirmText(confirmText: String?): MsgConfirmDialog {
        this.confirmText = confirmText
        return this
    }

    fun setButtonVisible(isVisible: Boolean): MsgConfirmDialog {
        this.isButtonVisible = isVisible
        return this
    }

    interface OnConfirmClickListener {
        fun onClick()
    }

    private var onConfirmClickListener: OnConfirmClickListener? = null
    fun setOnConfirmClickListener(onConfirmClickListener: OnConfirmClickListener?): MsgConfirmDialog {
        this.onConfirmClickListener = onConfirmClickListener
        return this
    }
}