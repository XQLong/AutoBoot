package com.xql.appbootauto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xql.appbootauto.MainActivity // 确保替换为您的包名

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 确保接收到的广播是我们想要的
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val i = Intent(context, MainActivity::class.java)
            // 因为是在广播接收器中启动Activity，所以需要添加这个Flag
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}
