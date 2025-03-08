package top.limbang.remoteoc.utils

import top.limbang.remoteoc.utils.NBTUtil.readFluidName
import top.limbang.remoteoc.utils.NBTUtil.readTargetCircuitDamage
import kotlin.test.Test


internal class NBTUtilTest {

    @Test
    fun base64ToCompoundTag() {
        // 测试液滴的NBT数据
        val nbtBase64 = "H4sIAAAAAAAA/+NiYOBgYHXLKc1MYRDOzc8pSc3TK8tMKkrMK0nMycmvZAAAv9iX+CEAAAA="
        val compoundTag = NBTUtil.base64StringToCompoundTag(nbtBase64)
        println(compoundTag.readFluidName())
        // 测试可编程舱室的NBT数据
        val nbtBase642 =
            "H4sIAAAAAAAA/yXLMQqAMBAEwFVQNGBt5xN8gG38h4R4nFcY4dz838LpJwABE5OrMJrnamzQxacWNi36Pd1JBfOA8aVb0cNOLOqilHxtytUKRT1RziP/H/gA1vHg/VUAAAA="
        val compoundTag2 = NBTUtil.base64StringToCompoundTag(nbtBase642)
        println(compoundTag2.readTargetCircuitDamage())
    }

    @Test
    fun parseNBTFromString() {
        // 测试可编程舱室的NBT数据
        val nbtString = """{targetCircuit:{Count:1b,Damage:15s,string_id:"miscutils:item.T3RecipeSelector"}}"""
        val compoundTag = NBTUtil.parseNBTFromString(nbtString)
        println(compoundTag)
    }

    @Test
    fun readAspectsName() {
        // 测试可编程舱室的NBT数据
        val nbtString = """{Aspects:[0:{amount:2,key:"machina"}]}"""
        val name = NBTUtil.readAspectsName(nbtString)
        println(name)
    }
}