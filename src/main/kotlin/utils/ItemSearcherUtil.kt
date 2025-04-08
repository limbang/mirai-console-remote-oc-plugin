/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import me.towdium.pinin.PinIn
import me.towdium.pinin.searchers.Searcher
import me.towdium.pinin.searchers.TreeSearcher
import top.limbang.remoteoc.entity.Item

/**
* 物品搜索工具类（支持中文拼音模糊搜索）
*
* @param items 待搜索的物品列表，包含本地化名称
* @param config PinIn 配置闭包，用于自定义拼音匹配规则
*
* @see Item 物品数据类结构
* @see PinIn 拼音处理核心库
*/
class ItemSearcherUtil(
    private val items: List<Item>,
    config: PinIn.Config.() -> Unit = {
        // 声母模糊
        fZh2Z(true)   // zh → z  （如 "zhi" → "zi"）
        fSh2S(true)   // sh → s  （如 "shi" → "si"）
        fCh2C(true)   // ch → c  （如 "chi" → "ci"）
        // 韵母模糊
        fAng2An(true) // ang → an （如 "fang" → "fan"）
        fIng2In(true) // ing → in （如 "qing" → "qin"）
        fEng2En(true) // eng → en （如 "feng" → "fen"）
        // 特殊韵母
        fU2V(true)    // ü → v    （如 "nü" → "nv"）
    }
) {

    private val pinIn = PinIn().config().apply(config).commit()
    private val searcher = TreeSearcher<Item>(Searcher.Logic.CONTAIN, pinIn)

    init {
        buildIndex()
    }

    /**
     * 构建索引
     */
    private fun buildIndex() {
        items.forEach {
            // 添加物品名称作为搜索键
            searcher.put(it.label, it)
        }
    }

    /**
    * 执行搜索操作
     *
    * @param query 搜索关键词（支持中文/拼音/混合）
    * @param limit 最大返回结果数（默认100）
    * @return 按匹配度排序的搜索结果
    */
    fun search(query: String, limit: Int = 100): List<Item> {
        if (query.isBlank()) return emptyList()

        return searcher.search(query)
            .sortedWith(compareByDescending { getMatchScore(it, query) })
            .take(limit)
    }

    /**
    * 计算物品匹配得分
    *
    * @param item 待评分的物品
    * @param query 搜索关键词
    * @return 匹配得分（越高优先级越高）
    */
    private fun getMatchScore(item: Item, query: String): Int {
        val label = item.label
        var score = 0

        // 原字符串匹配得分
        when {
            label == query -> score += 1000 // 完全匹配
            label.startsWith(query) -> score += 800 // 前缀匹配
            label.contains(query) -> score += 400 // 包含匹配
        }

        // 拼音匹配得分（考虑模糊音配置）
        when {
            pinIn.matches(label, query) -> score += 900 // 拼音完全匹配
            pinIn.begins(label, query) -> score += 700 // 拼音前缀匹配
            pinIn.contains(label, query) -> score += 500 // 拼音包含匹配
        }

        // 匹配长度加成（匹配部分越长得分越高）
        score += query.length * 10

        // 中文优先：如果原字符串匹配存在，提升权重
        if (score > 0) score += 200

        return score
    }

}
