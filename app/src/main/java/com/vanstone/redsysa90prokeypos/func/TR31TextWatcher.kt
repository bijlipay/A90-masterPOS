package com.vanstone.redsysa90prokeypos.func

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText

class TR31TextWatcher (editText: EditText) : TextWatcher {
    private var mFormat = false
    private var mInvalid = false
    private var mSelection = 0
    private var mLastText = ""
    private var mEditText: EditText? = null

    /**
     * Creates an instance of `CustomTextWatcher`.
     *
     * @param editText the editText to edit text.
     */
    init {
        mEditText = editText
        mEditText!!.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    }

    override fun beforeTextChanged(
        charSequence: CharSequence,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        charSequence: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        try {
            var temp = charSequence.toString()

            // Set selection.
            if (mLastText == temp) {
                if (mInvalid) {
                    mSelection -= 1
                } else {
                    if (mSelection >= 1 && temp.length > mSelection - 1 && temp[mSelection - 1] == ' ') {
                        mSelection += 1
                    }
                }
                val length = mLastText.length
                if (mSelection > length) {
                    mEditText!!.setSelection(length)
                } else {
                    mEditText!!.setSelection(mSelection)
                }
                mFormat = false
                mInvalid = false
                return
            }
            mFormat = true
            mSelection = start

            // Delete operation.
            if (count == 0) {
                if (mSelection >= 1 && temp.length > mSelection - 1 && temp[mSelection - 1] == ' ') {
                    mSelection -= 1
                }
                return
            }

            // Input operation.
            mSelection += count
            val lastChar = temp.substring(start, start + count)
                .toCharArray()
            val mid = lastChar[0].code
            if (mid in 48..57) {
                /* 1-9. */
            } else if (mid in 65..70) {
                /* A-F. */
            } else if (mid in 97..102) {
                /* a-f. */
            } else if(mid == 78 || mid == 84 || mid == 88 || mid == 110 || mid == 116 || mid == 120){
                /* N, T, X, n, t, x */
            }
            else {
                /* Invalid input. */
                mInvalid = true
                temp = (temp.substring(0, start)
                        + temp.substring(start + count, temp.length))
                mEditText!!.setText(temp)
                return
            }
        } catch (e: Exception) {
        }
    }

    override fun afterTextChanged(editable: Editable) {
        try {
            if (mFormat) {
                val text = StringBuilder()
                text.append(editable.toString().replace(" ", ""))
                mLastText = text.toString()
                mEditText!!.setText(text)
            }
        } catch (e: Exception) {
        }
    }
}