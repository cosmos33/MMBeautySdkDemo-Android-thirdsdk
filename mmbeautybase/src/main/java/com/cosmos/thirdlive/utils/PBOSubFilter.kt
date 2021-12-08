package com.cosmos.thirdlive.utils

class PBOSubFilter(width: Int, height: Int) :
    PBOFilter(width, height) {
    override fun getTextOutID(): Int {
        return if (texture_out == null || texture_out.isEmpty()) 0 else texture_out[0]
    }
}