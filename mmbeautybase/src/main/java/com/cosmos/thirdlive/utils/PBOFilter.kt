package com.cosmos.thirdlive.utils

import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter

open class PBOFilter(width: Int, height: Int) : FaceInfoCreatorPBOFilter(width, height) {
    fun newTextureReady(textureIn: Int, width: Int, height: Int, newData: Boolean) {
        if (newData) {
            markAsDirty()
        }

        texture_in = textureIn
        setWidth(width)
        setHeight(height)
        onDrawFrame()
    }
}