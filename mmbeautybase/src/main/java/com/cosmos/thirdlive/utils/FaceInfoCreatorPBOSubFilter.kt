package com.cosmos.thirdlive.utils

import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter

class FaceInfoCreatorPBOSubFilter(width: Int, height: Int) :
    FaceInfoCreatorPBOFilter(width, height) {
    override fun getTextOutID(): Int {
        return if (texture_out == null || texture_out.isEmpty()) 0 else texture_out[0]
    }

    override fun getFragmentShader(): String {
        return """precision mediump float;
                  uniform sampler2D inputImageTexture0;
                  varying vec2 textureCoordinate;
                  void main(){
                     gl_FragColor = texture2D(inputImageTexture0,textureCoordinate);
                  }
              """
    }
}