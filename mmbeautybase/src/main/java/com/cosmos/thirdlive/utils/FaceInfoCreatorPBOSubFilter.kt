package com.cosmos.thirdlive.utils

import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter

class FaceInfoCreatorPBOSubFilter(width: Int, height: Int) :
    FaceInfoCreatorPBOFilter(width, height) {
    override fun getTextOutID(): Int {
        return if (texture_out == null || texture_out.isEmpty()) 0 else texture_out[0]
    }
}