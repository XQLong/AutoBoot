# 背景

手上有一台闲置的华为手机，想将其改造为家用NAS服务器，平时进行一些文件转存使用。因华为手机无法root，采用安装Termux模拟linux环境搭建开源的NextCloud服务的方式。又因手机屏幕失灵且无法正常显示内容，因而构建此应用用作手机重启后服务自动拉起，其主要功能：

- 手机开机或重启后自动拉起AutoBoot应用；
- 获取手机已安装的应用列表，选择一个需要关联启动的应用；（对于此场景即选择Termux，应用会默认记住上次所选应用）
- 应用自启后可自动唤起所选择的已安装应用；

# 安卓Termux和部署NextCloud

参考了如下链接中的方法进行了部署，按此逐步执行即可，
https://github.com/TechTutoPPT/Handheld-NAS

# 设置应用开机自启动相关配置

- 开启应用自启动和悬浮窗权限：
<img width="385" height="840" alt="image" src="https://github.com/user-attachments/assets/e2236be2-e1b7-4350-9942-b670d1d8e9f2" />

- 应用启动该权限管理中设置为手动管理：
<img width="389" height="843" alt="image" src="https://github.com/user-attachments/assets/88a6aa68-b07d-4696-94df-8d5563945469" />

