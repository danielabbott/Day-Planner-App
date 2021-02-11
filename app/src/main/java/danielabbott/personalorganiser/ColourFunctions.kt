package danielabbott.personalorganiser

object ColourFunctions {

    fun lightenRGB(rgb: Int, evenLighter: Boolean = false): Int {
        var r = rgb and 0xff
        var g = (rgb shr 8) and 0xff
        var b = (rgb shr 16) and 0xff

        if(evenLighter) {
            r += ((255 - r) / 5) * 3
            g += ((255 - g) / 5) * 3
            b += ((255 - b) / 5) * 3
        }
        else {
            r += (255 - r) / 2
            g += (255 - g) / 2
            b += (255 - b) / 2
        }

        return (r or (g shl 8)) or (b shl 16)
    }

}