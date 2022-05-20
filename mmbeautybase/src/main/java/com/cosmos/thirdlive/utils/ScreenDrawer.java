package com.cosmos.thirdlive.utils;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 渲染器真实操作类，可以对视频画面进行滤镜渲染、旋转、镜像调整等
 */
public class ScreenDrawer {
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{" +
                    "gl_Position = vPosition;" +
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;

    private FloatBuffer vertexBuffer, textureVerticesBuffer1;
    private ShortBuffer drawListBuffer;
    static float squareCoords[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
    };
    private int mPositionHandle;
    private int mTextureCoordHandle;

    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform sampler2D s_texture;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";
    private final int mProgram;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float textureVertices1[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };
    private int targetWidth;
    private int targetHeight;

    public ScreenDrawer(int textureWidth, int textureHeight, int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        float ratioW;
        float ratioH;
        if (targetWidth <= textureWidth && targetHeight <= textureHeight) {//如果texture(相机吐的尺寸)比target的size（即view的size）的大，缩小后裁剪
            ratioW = targetWidth * 1.0f / textureWidth;
            ratioH = targetHeight * 1.0f / textureHeight;
            float temp = Math.min(ratioH, ratioW);
            ratioH = temp;
            ratioW = temp;
        } else {//如果texture(相机吐的尺寸)比target的size（即view的size）的小，会将texture拉伸后再裁剪
            ratioW = targetWidth * 1.0f / textureWidth;
            ratioH = targetHeight * 1.0f / textureHeight;
            float ratio = Math.max(ratioW, ratioH);
            float newTextureWidth = ratio * textureWidth;
            float newTextureHeight = ratio * textureHeight;
            ratioW = targetWidth * 1.f / newTextureWidth;
            ratioH = targetHeight * 1.f / newTextureHeight;
        }

        for (int i = 0; i < textureVertices1.length; i += 2) {
            //x值
            if (textureVertices1[i] == 0) {
                textureVertices1[i] = (1 - ratioW) / 2;
            } else {
                textureVertices1[i] = 1 - (1 - ratioW) / 2;
            }
            //y值
            if (textureVertices1[i + 1] == 0) {
                textureVertices1[i + 1] = (1 - ratioH) / 2;
            } else if (textureVertices1[i + 1] == 1) {
                textureVertices1[i + 1] = 1 - (1 - ratioH) / 2;
            }
        }

        ByteBuffer bb3 = ByteBuffer.allocateDirect(textureVertices1.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        textureVerticesBuffer1 = bb3.asFloatBuffer();
        textureVerticesBuffer1.put(textureVertices1);
        textureVerticesBuffer1.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
    }

    public void draw(int texture) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, targetWidth, targetHeight);
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
//

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the <insert shape here> coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

        textureVerticesBuffer1.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer1);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
