#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES inputTexture;
varying vec2 vTextureCoord;
void main(){
    gl_FragColor = texture2D(inputTexture, vTextureCoord );
}

