package com.xql.appbootauto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(private val appList: List<AppInfo>) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    // ViewHolder 定义了列表项中的视图
    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.iv_app_icon)
        val appName: TextView = itemView.findViewById(R.id.tv_app_name)
        val appCheckBox: CheckBox = itemView.findViewById(R.id.cb_select_app)
    }

    // 创建新的 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_app, parent, false)
        return AppViewHolder(view)
    }

    // 获取列表项的数量
    override fun getItemCount(): Int = appList.size

    // 将数据绑定到 ViewHolder
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val currentApp = appList[position]

        // 设置视图内容
        holder.appIcon.setImageDrawable(currentApp.icon)
        holder.appName.text = currentApp.appName
        holder.appCheckBox.isChecked = currentApp.isChecked

        // 设置整行的点击事件，用于更新复选框状态
        holder.itemView.setOnClickListener {
            // 如果当前项已经被选中，再次点击则取消选中
            val isCurrentlyChecked = currentApp.isChecked

            // 1. 先将所有项的勾选状态全部清除
            appList.forEach { it.isChecked = false }

            // 2. 如果当前项之前是未选中的，则将其设为选中状态
            // 如果已经是选中的，上面已经把它清除了，实现了"再次点击取消"的效果
            currentApp.isChecked = !isCurrentlyChecked

            // 3. 通知适配器数据已改变，刷新整个列表的UI
            notifyDataSetChanged()
        }
    }
}
