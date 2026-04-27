package com.jetcemetery.calculusPhoneNumber.helper

import android.widget.EditText
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues

class StartOperationHelper(private val dataObj: Singleton_OperationValues) {
    var misconstruedIntegralRange_text: String = ""
        private set

    fun misconstruedIntegralRange(): Boolean {
        var returningVal = false
        misconstruedIntegralRange_text = ""
        if (dataObj.integral_1_End() < dataObj.integral_1_Start()) {
            misconstruedIntegralRange_text = "Integral 1 end value is less then initial!"
            returningVal = true
        } else if (dataObj.integral_2_End() < dataObj.integral_2_Start()) {
            misconstruedIntegralRange_text = "Integral 2 end value is less then initial!"
            returningVal = true
        } else if (dataObj.integral_3_End() < dataObj.integral_3_Start()) {
            misconstruedIntegralRange_text = "Integral 3 end value is less then initial!"
            returningVal = true
        }
        return returningVal
    }

    fun numberInputValid(phoneText: EditText): Boolean {
        val goodVersionDash = "-"
        val badVersionDash = "–"
        val un_parsed_phone = phoneText.text.toString().replace(badVersionDash, goodVersionDash)
        val regEx = "^\\s*(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)\\s*$"
        return un_parsed_phone.matches(Regex(regEx))
    }
}
