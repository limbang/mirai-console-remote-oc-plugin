package top.limbang.remoteoc.utils

import top.limbang.remoteoc.utils.NBTUtil.readFluidName
import kotlin.test.Test


internal class NBTUtilTest {

    @Test
    fun base64ToCompoundTag() {
        val nbtBase64 = "H4sIAAAAAAAA/+NiYOBgYHXLKc1MYRDOzc8pSc3TK8tMKkrMK0nMycmvZAAAv9iX+CEAAAA="
        val compoundTag = NBTUtil.base64StringToCompoundTag(nbtBase64)
        println(compoundTag.readFluidName())
    }
}