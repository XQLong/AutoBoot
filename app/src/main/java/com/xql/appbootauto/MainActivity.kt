package com.xql.appbootauto

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.net.Uri

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable,
    var isChecked: Boolean = false
)

class MainActivity : AppCompatActivity() {

    // 将 appList 声明为成员变量，以便在点击事件中访问
    private lateinit var appList: List<AppInfo>
    private lateinit var appListAdapter: AppListAdapter

    // 用于跳转到应用详情页的启动器
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // 当用户从设置页面返回时，可以再次检查权限
        // 如果需要，可以在这里添加刷新逻辑
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() 已被新模板弃用，ViewCompat 写法是正确的
        setContentView(R.layout.activity_main)

        // 在加载UI前，先检查悬浮窗权限
        checkOverlayPermission()
        // 在UI加载前，先执行自动启动逻辑
        if (autoLaunchSavedApps()) {
            // 如果成功触发了自动启动，可以直接关闭当前界面（暂不关闭）
            //finish()
            //return // 提前返回，避免继续加载UI
        }
        // 如果没有自动启动，处理窗口边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 获取应用列表
        appList = getInstalledApps()

        // 2. 设置 RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.rv_app_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        appListAdapter = AppListAdapter(appList)
        recyclerView.adapter = appListAdapter

        // 3. 设置按钮点击事件
        val launchButton: Button = findViewById(R.id.btn_launch_selected_apps)
        launchButton.setOnClickListener {
            launchSelectedApps()
        }
    }

    // 检查并请求悬浮窗权限的函数
    private fun checkOverlayPermission() {
        // Android 6.0 (API 23) 以上才需要动态检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 使用 Settings.canDrawOverlays 检查权限
            if (!Settings.canDrawOverlays(this)) {
                // 如果没有权限，创建一个对话框引导用户
                AlertDialog.Builder(this)
                    .setTitle("需要悬浮窗权限")
                    .setMessage("为了确保应用能够在开机后成功自启（特别是在鸿蒙、小米等系统上），请授予应用“显示在其他应用上层”的权限。")
                    .setPositiveButton("去设置") { dialog, _ ->
                        // 创建一个意图，跳转到应用的悬浮窗权限设置页面
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        // 启动设置页面
                        settingsLauncher.launch(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                        // 可以在这里提示用户，缺少该权限可能导致功能异常
                        Toast.makeText(this, "缺少权限可能导致自启动失败", Toast.LENGTH_LONG).show()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        //先获取已保存的包名
        val sharedPrefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedPackageNames = sharedPrefs.getStringSet("selected_apps", emptySet()) ?: emptySet()

        val pm: PackageManager = packageManager
        val apps = mutableListOf<AppInfo>()
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            // 过滤掉系统应用
            if ((packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                // 确保应用有启动入口
                if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                    val appName = packageInfo.loadLabel(pm).toString()
                    val packageName = packageInfo.packageName
                    val icon = packageInfo.loadIcon(pm)
                    // 检查当前应用的包名是否在已保存的列表中
                    val isChecked = savedPackageNames.contains(packageName)
                    apps.add(AppInfo(appName, packageName, icon, isChecked))
                }
            }
        }
        return apps
    }

    private fun launchSelectedApps() {
        // 筛选出被勾选的应用
        val selectedApps = appList.filter { it.isChecked }

        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "请先选择要启动的应用", Toast.LENGTH_SHORT).show()
            // 保存一个空列表，清除之前的设置
            saveSelectedApps(emptySet())
            return
        }
        // 将选中的应用包名转换为一个Set<String>
        val selectedPackageNames = selectedApps.map { it.packageName }.toSet()
        // 保存选中的应用包名
        saveSelectedApps(selectedPackageNames)
        // 遍历并启动每一个选中的应用
        for (app in selectedApps) {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                try {
                    startActivity(launchIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "无法启动 ${app.appName}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "找不到 ${app.appName} 的启动项", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 保存应用包名到 SharedPreferences 的函数
    private fun saveSelectedApps(packageNames: Set<String>) {
        // 获取 SharedPreferences 实例
        val sharedPrefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        // 开启编辑模式
        with(sharedPrefs.edit()) {
            // 存储一个字符串集合
            putStringSet("selected_apps", packageNames)
            // 提交保存
            apply()
        }
    }

    // 检查并自动启动已保存应用的函数
    private fun autoLaunchSavedApps(): Boolean {
        val sharedPrefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedPackageNames = sharedPrefs.getStringSet("selected_apps", null)

        // 如果列表不为空，则启动这些应用
        if (!savedPackageNames.isNullOrEmpty()) {
            // 获取应用名称并显示在 Toast 中
            val appNames = savedPackageNames.mapNotNull { packageName ->
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    null // 如果应用已被卸载，则返回null
                }
            }.joinToString(", ") // 将应用名用逗号和空格连接

            //在这里弹出Toast通知
            Toast.makeText(this, "启动应用：$appNames", Toast.LENGTH_LONG).show()

            var launchedAtLeastOne = false
            for (packageName in savedPackageNames) {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    try {
                        startActivity(launchIntent)
                        launchedAtLeastOne = true
                    } catch (e: Exception) {
                        // 可以在这里加一个Toast，但由于Activity很快关闭，用户可能看不到
                    }
                }
            }
            return launchedAtLeastOne // 如果至少启动了一个，返回true
        }
        return false // 如果没有保存的应用，返回false
    }
}