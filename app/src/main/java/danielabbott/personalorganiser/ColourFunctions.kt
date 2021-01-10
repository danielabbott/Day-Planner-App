package danielabbott.personalorganiser

object ColourFunctions {

    fun lightenRGB(rgb: Int): Int{
        var r = rgb and 0xff
        var g = (rgb shr 8) and 0xff
        var b = (rgb shr 16) and 0xff

        r += (255 - r) / 4
        g += (255 - g) / 4
        b += (255 - b) / 4

        return (r or (g shl 8)) or (b shl 16)
    }

}