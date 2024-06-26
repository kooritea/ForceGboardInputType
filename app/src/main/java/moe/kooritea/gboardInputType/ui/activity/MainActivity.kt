@file:Suppress("SetTextI18n")

package moe.kooritea.gboardInputType.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.YukiHookAPI.Status.Executor
import com.highcapable.yukihookapi.hook.factory.prefs
import moe.kooritea.gboardInputType.BuildConfig
import moe.kooritea.gboardInputType.R
import moe.kooritea.gboardInputType.databinding.ActivityMainBinding
import moe.kooritea.gboardInputType.ui.activity.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun onCreate() {
        refreshModuleStatus()
        binding.mainTextVersion.text = getString(R.string.module_version, BuildConfig.VERSION_NAME)
        binding.hideIconInLauncherSwitch.isChecked = isLauncherIconShowing.not()
        binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) hideOrShowLauncherIcon(isChecked)
        }
        binding.debugSwitch.isChecked = prefs().getBoolean("debug_mode", false)
        binding.debugSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                prefs().edit { putBoolean("debug_mode", isChecked) }
            }
        }
        binding.titleGithubIcon.setOnClickListener {
            startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://github.com/kooritea/ForceGboardInputType")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    /**
     * Hide or show launcher icons
     *
     * - You may need the latest version of LSPosed to enable the function of hiding launcher
     *   icons in higher version systems
     *
     * 隐藏或显示启动器图标
     *
     * - 你可能需要 LSPosed 的最新版本以开启高版本系统中隐藏 APP 桌面图标功能
     * @param isShow Whether to display / 是否显示
     */
    private fun hideOrShowLauncherIcon(isShow: Boolean) {
        packageManager?.setComponentEnabledSetting(
            ComponentName(packageName, "${BuildConfig.APPLICATION_ID}.Home"),
            if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Get launcher icon state
     *
     * 获取启动器图标状态
     * @return [Boolean] Whether to display / 是否显示
     */
    private val isLauncherIconShowing
        get() = packageManager?.getComponentEnabledSetting(
            ComponentName(packageName, "${BuildConfig.APPLICATION_ID}.Home")
        ) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    /**
     * Refresh module status
     *
     * 刷新模块状态
     */
    private fun refreshModuleStatus() {
        binding.mainLinStatus.setBackgroundResource(
            when {
                YukiHookAPI.Status.isModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                YukiHookAPI.Status.isModuleActive -> R.mipmap.ic_success
                else -> R.mipmap.ic_warn
            }
        )
        binding.mainTextStatus.text = getString(
            when {
                YukiHookAPI.Status.isModuleActive -> R.string.module_is_activated
                else -> R.string.module_not_activated
            }
        )
        binding.mainTextApiWay.isVisible = YukiHookAPI.Status.isModuleActive
        when {
            Executor.apiLevel > 0 ->
                binding.mainTextApiWay.text =
                    "Activated by ${Executor.name} API ${Executor.apiLevel}"
            YukiHookAPI.Status.isTaiChiModuleActive -> binding.mainTextApiWay.text = "Activated by TaiChi"
            else -> binding.mainTextApiWay.text = "Activated by anonymous"
        }
    }
}