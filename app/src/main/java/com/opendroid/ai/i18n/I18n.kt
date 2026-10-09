package com.opendroid.ai.i18n

import android.content.Context
import org.json.JSONObject

/**
 * OpenDroid 中文版 (opendroid-cn) 的运行时翻译层。
 *
 * 设计要点：
 *  - 界面文案以**英文原文为 key**，翻译表放在 `assets/i18n/zh.json`（英文 -> 中文）。
 *  - [tr] 命中返回中文，**未命中回退英文**，任何异常都回退英文，绝不崩溃。
 *  - 翻译数据是纯 JSON，便于 diff、翻译、重放，也便于上游同步时增量补译。
 *
 * 该文件由 opendroid-cn 分支新增；上游无此文件。
 */
object I18n {

    @Volatile
    private var translations: Map<String, String> = emptyMap()

    @Volatile
    private var initialized: Boolean = false

    private const val ASSET_PATH = "i18n/zh.json"

    /** 幂等初始化；应在 Application.onCreate 中调用一次。 */
    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            translations = load(context.applicationContext)
            initialized = true
        }
    }

    private fun load(context: Context): Map<String, String> = try {
        val raw = context.assets.open(ASSET_PATH)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val obj = JSONObject(raw)
        val map = HashMap<String, String>(obj.length() * 2)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.optString(key, "")
            if (key.isNotEmpty() && value.isNotEmpty()) map[key] = value
        }
        map
    } catch (t: Throwable) {
        // 翻译表缺失/损坏时必须不影响启动，直接回退英文。
        emptyMap()
    }

    /** 英文 -> 中文；无译文返回原文。 */
    fun translate(en: String): String {
        if (en.isEmpty()) return en
        val table = translations
        if (table.isEmpty()) return en
        return table[en] ?: en
    }

    /** 是否已加载到有效翻译表（供自检/设置页显示）。 */
    fun isReady(): Boolean = translations.isNotEmpty()

    /** 已加载的译文条数。 */
    fun size(): Int = translations.size
}

/**
 * codemod (tools/apply_i18n.py) 生成的调用点使用的顶层入口。
 * 保持为顶层函数，便于自动注入 `import com.opendroid.ai.i18n.tr`。
 */
fun tr(en: String): String = I18n.translate(en)
