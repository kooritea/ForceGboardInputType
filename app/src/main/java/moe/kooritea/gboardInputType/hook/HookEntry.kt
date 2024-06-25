package moe.kooritea.gboardInputType.hook

import android.inputmethodservice.InputMethodService
import android.view.inputmethod.EditorInfo
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XposedBridge

const val TYPE_MASK_CLASS = 15
const val TYPE_MASK_VARIATION = 240

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog {
            isEnable = false
        }
    }

    override fun onHook() = encase {
        loadApp("com.google.android.inputmethod.latin") {
            searchClass {
                extends<InputMethodService>()
                method {
                    name = "onStartInputView"
                    param(VagueType, BooleanType)
                }.count(num = 1)
            }.get()?.hook {
                injectMember {
                    method {
                        name = "onStartInputView"
                        param(VagueType, BooleanType)
                    }
                    beforeHook {
                        hooker((args[0]!! as EditorInfo), false)
                    }
                }
            }
            searchClass {
                extends<InputMethodService>()
                method {
                    name = "onStartInput"
                    param(VagueType, BooleanType)
                }.count(num = 1)
            }.get()?.hook {
                injectMember {
                    method {
                        name = "onStartInput"
                        param(VagueType, BooleanType)
                    }
                    beforeHook {
                        hooker((args[0]!! as EditorInfo), prefs.getBoolean("debug_mode", false))
                    }
                }
            }
        }
    }

    private fun hooker(editorInfo: EditorInfo, log: Boolean) {
        var result = editorInfo.inputType
        result = replaceFlag(result, EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD, EditorInfo.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT, TYPE_MASK_VARIATION)
        result = replaceFlag(result, EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, 0, TYPE_MASK_VARIATION)
        result = replaceFlag(result, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, 0, TYPE_MASK_VARIATION)
        result = replaceFlag(result, 0, EditorInfo.TYPE_CLASS_TEXT, TYPE_MASK_CLASS)
        if(result != editorInfo.inputType){
            if(log){
                XposedBridge.log("[ForceGboardInputType][" + editorInfo.packageName + "] inputType: " + editorInfo.inputType + "->" + result)
            }
            editorInfo.inputType = result
        }else{
            if(log){
                XposedBridge.log("[ForceGboardInputType][" + editorInfo.packageName + "] inputType: " + editorInfo.inputType)
            }
        }
    }

    private fun replaceFlag(target: Int, flag:Int, replaceTo: Int, flagType: Int): Int {
        if((target and flagType) == flag){
            val otherFlag = target xor (target and flagType)
            return otherFlag or replaceTo
        }
        return target
    }
}