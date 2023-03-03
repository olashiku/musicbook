package com.olashiku.musicbook.utils

import android.os.Handler
import android.os.Looper

object utilz {

    fun delayForALittleBit(numberOfSeconds:Long = 500L,action:()->Unit){
       Handler(Looper.getMainLooper()).postDelayed({
          action.invoke()
       },numberOfSeconds)
    }

}