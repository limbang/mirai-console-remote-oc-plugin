package top.limbang.remoteoc.utils

import junit.framework.TestCase.assertEquals
import top.limbang.remoteoc.entity.Item
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ItemUtilTest {

    private val itemsJson =
        """[{"name":"minecraft:diamond_block","size":0,"label":"Block of Diamond","damage":0},{"name":"appliedenergistics2:tile.BlockInterface","size":0,"label":"ME Interface","damage":0},{"name":"appliedenergistics2:tile.BlockCraftingUnit","size":2,"label":"Crafting Unit","damage":0},{"name":"gregtech:gt.blockcasings","size":2,"label":"EV Machine Casing","damage":4},{"name":"IC2:itemDust2","size":0,"label":"Energium Dust","damage":2},{"name":"IC2:itemBatCrystal","size":0,"label":"Energy Crystal","damage":0},{"name":"IC2:itemBatLamaCrystal","size":1,"label":"Lapotron Crystal","damage":26},{"name":"appliedenergistics2:item.ItemMultiMaterial","size":1,"label":"Printed Calculation Circuit","damage":16},{"name":"appliedenergistics2:item.ItemMultiMaterial","size":0,"label":"Printed Engineering Circuit","damage":17},{"name":"appliedenergistics2:item.ItemMultiMaterial","size":0,"label":"Printed Logic Circuit","damage":18},{"name":"appliedenergistics2:item.ItemMultiMaterial","size":0,"label":"Printed Silicon","damage":20},{"name":"ggfab:gt.ggfab.d1","size":116,"label":"Single Use Wrench","damage":1},{"name":"dreamcraft:item.RawLapotronCrystal","size":0,"label":"Raw Lapotron Crystal","damage":0},{"name":"dreamcraft:item.LapotronDust","size":0,"label":"Lapotron Dust","damage":0},{"name":"gregtech:gt.metaitem.01","size":120,"label":"Aluminium Plate","damage":17019},{"name":"gregtech:gt.metaitem.01","size":67,"label":"Titanium Plate","damage":17028},{"name":"gregtech:gt.metaitem.01","size":2,"label":"Gold Plate","damage":17086},{"name":"gregtech:gt.metaitem.01","size":203,"label":"Diamond Plate","damage":17500},{"name":"gregtech:gt.metaitem.01","size":234,"label":"Certus Quartz Plate","damage":17516},{"name":"gregtech:gt.metaitem.01","size":16,"label":"Silicon Solar Grade (Poly SI) Plate","damage":17856},{"name":"gregtech:gt.metaitem.01","size":4,"label":"Data Stick","damage":32708},{"name":"gregtech:gt.metaitem.01","size":36,"label":"Engraved Lapotron Chip","damage":32714},{"name":"ae2fc:fluid_drop","size":0,"label":"drop of Molten Steel","damage":0},{"name":"ae2fc:fluid_drop","size":0,"label":"drop of Molten Vibrant Alloy","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Reinforced Glass Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Casing))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Ring))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Emerald Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Green Sapphire Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Enderpearl Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Endereye Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Bolt))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Topaz Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Ruby Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Yellow Garnet Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Rotor))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Diamond Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Gear))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Rod))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Small Gear))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Sapphire Lens)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Extruder Shape (Cell))","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Inscriber Engineering Press)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Inscriber Logic Press)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Inscriber Silicon Press)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Inscriber Calculation Press)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Programmed Circuit)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Reset)","damage":0},{"name":"programmablehatches:prog_circuit","size":0,"label":"Programming Circuit (Tool Casting Mold (Wrench))","damage":0}]"""

    private val items: List<Item> by lazy { json.decodeFromString(itemsJson) }

    private val itemUtil = ItemUtil("debug-sandbox/data/top.limbang.RemoteOC")

    @Test
    fun getItemNameTest() {
        val result = itemUtil.getLocalizedDataList(items).joinToString("\n") {
            "物品名称：${it.name} 物品图片路径：${it.imgPath}"
        }
        // 增加断言
        assertTrue(result.isNotEmpty())

        println(result)
    }


}


class PinyinTest {
    private val itemUtil = ItemUtil("debug-sandbox/data/top.limbang.RemoteOC")

    /**
     * 测试纯中文字符（无多音字）
     */
    @Test
    fun testAllChineseNoPolyphone() {
        val input = "你好"
        val expected = "nihao"  // 直接返回拼音，不需要集合
        val result = itemUtil.generatePinyinVariations(input)
        println("testAllChineseNoPolyphone: input = '$input', expected = $expected, result = $result")
        assertEquals(expected, result)
    }

    /**
     * 测试包含多音字的中文字符（假设库返回第一个拼音）
     */
    @Test
    fun testChineseWithPolyphone() {
        val input = "重庆"
        // 假设 "重" 返回 "zhong"，"庆" 返回 "qing"
        val expected = "zhongqing"  // 直接返回拼音，不需要集合
        val result = itemUtil.generatePinyinVariations(input)
        println("testChineseWithPolyphone: input = '$input', expected = $expected, result = $result")
        assertEquals(expected, result)
    }

    /**
     * 测试混合中英文、数字、符号
     */
    @Test
    fun testMixedCharacters() {
        val input = "a你b好123@"
        val expected = "anibhao123@"  // 直接返回拼音，不需要集合
        val result = itemUtil.generatePinyinVariations(input)
        println("testMixedCharacters: input = '$input', expected = $expected, result = $result")
        assertEquals(expected, result)
    }

    /**
     * 测试空字符串
     */
    @Test
    fun testEmptyString() {
        val input = ""
        val expected = ""  // 空字符串应该返回空字符串
        val result = itemUtil.generatePinyinVariations(input)
        println("testEmptyString: input = '$input', expected = '$expected', result = '$result'")
        assertEquals(expected, result)
    }

    /**
     * 测试非中文字符（字母、数字）
     */
    @Test
    fun testNonChineseCharacters() {
        val input = "abc123"
        val expected = "abc123"  // 直接返回拼音，不需要集合
        val result = itemUtil.generatePinyinVariations(input)
        println("testNonChineseCharacters: input = '$input', expected = '$expected', result = '$result'")
        assertEquals(expected, result)
    }
}

class ItemUtilTestByName {
    private val itemUtil = ItemUtil("debug-sandbox/data/top.limbang.RemoteOC")

    @Test
    fun `test search item by exact name - silver wire`() {
        val results = itemUtil.searchItemByName("1x银导线")
        println("Search results for '1x银导线': $results")
        val expectedItem = Item(name = "gregtech:wire_single", damage = 100, label = "1x银导线", size = 0)
        assertTrue(results.any { item -> item.name == expectedItem.name && item.damage == expectedItem.damage })

    }

    @Test
    fun `test search item by exact name - tin wire`() {
        val results = itemUtil.searchItemByName("1x锡导线")
        println("Search results for '1x锡导线': $results")
        val expectedItem = Item(name = "gregtech:wire_single", damage = 112, label = "1x锡导线", size = 0)
        assertTrue(results.any { item -> item.name == expectedItem.name && item.damage == expectedItem.damage })
        // 验证列表是否包含至少一个匹配的 Item
    }

    @Test
    fun `test search item by exact name - tungsten wire`() {
        val results = itemUtil.searchItemByName("1x钨导线")
        println("Search results for '1x钨导线': $results")
        val expectedItem = Item(name = "gregtech:wire_single", damage = 115, label = "1x钨导线", size = 0)
        assertTrue(results.any { item -> item.name == expectedItem.name && item.damage == expectedItem.damage })
        // 验证列表是否包含至少一个匹配的 Item
    }

    @Test
    fun `test search item by non-existent name`() {
        val results = itemUtil.searchItemByName("1x金导线")  // 假设"金导线"不存在
        println("Search results for '1x金导线': $results")
        val expectedItem = Item(name = "gregtech:wire_single", damage = 41, label = "1x金导线", size = 0)
        assertTrue(results.any { item -> item.name == expectedItem.name && item.damage == expectedItem.damage })
        // 验证列表是否包含至少一个匹配的 Item
    }

    @Test
    fun `test search item by pinyin with different case`() {
        val results = itemUtil.searchItemByName("jindaoxian")  // 拼音的不同大小写
        println("Search results for 'jindaoxian': $results")
        val expectedItem = Item(name = "gregtech:wire_single", damage = 41, label = "1x金导线", size = 0)
        assertTrue(results.any { item -> item.name == expectedItem.name && item.damage == expectedItem.damage })
        // 验证列表是否包含至少一个匹配的 Item
    }
}