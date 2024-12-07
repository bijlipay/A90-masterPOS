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
class ConfirmDialog : DialogFragment() {
    private var infoText: String? = null
    private var confirmText: String? = null
    private var cancelText: String? = null
    private var cancelAble = false
    private var tvInfo: TextView? = null
    private var tvConfirm: TextView? = null
    private var tvCancel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewTop = inflater.inflate(R.layout.dialog_confirm, container)
        tvInfo = viewTop.findViewById(R.id.tvInfo)
        tvConfirm = viewTop.findViewById(R.id.tvConfirm)
        tvCancel = viewTop.findViewById(R.id.tvCancel)
        return viewTop
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvInfo!!.text = infoText
        tvConfirm!!.text = confirmText
        tvCancel!!.text = cancelText
        isCancelable = cancelAble
        tvConfirm!!.setOnClickListener { view1: View? ->
            dismiss()
            onBtnClickListener!!.onConfirmClick()
        }
        tvCancel!!.setOnClickListener { view12: View? ->
            dismiss()
            onBtnClickListener!!.onCancelClick()
        }
    }

    fun setTvInfo(infoText: String?): ConfirmDialog {
        this.infoText = infoText
        return this
    }

    fun setCancelText(cancelText: String?): ConfirmDialog {
        this.cancelText = cancelText
        return this
    }

    fun setCancelAble(cancelable: Boolean): ConfirmDialog {
        this.cancelAble = cancelable
        return this
    }

    fun setConfirmText(confirmText: String?): ConfirmDialog {
        this.confirmText = confirmText
        return this
    }

    interface OnBtnClickListener {
        fun onConfirmClick()
        fun onCancelClick()
    }

    private var onBtnClickListener: OnBtnClickListener? = null
    fun setOnBtnClickListener(onBtnClickListener: OnBtnClickListener): ConfirmDialog {
        this.onBtnClickListener = onBtnClickListener
        return this
    }
}