package com.tokis.tokiapp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.tokis.tokiapp.utils.SweetAlert.SweetAlertDialog

class DialogHelperActivityLifeCycle(val dialog: DialogLoading) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onDestroy(){
        if (dialog.dialog != null){
            if(dialog.dialog!!.isShowing){
                dialog.dismiss()
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
            }
        }
    }
}