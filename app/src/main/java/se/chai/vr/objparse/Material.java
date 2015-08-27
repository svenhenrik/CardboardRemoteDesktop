package se.chai.vr.objparse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Material {
    String name;
    float[] ambientColor; //ambient color
    float[] diffuseColor;
    float[] specularColor;
    float alpha;
    float shine;
    int illum;
    String textureFile;

    public Material(String name){
        this.name=name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float[] getAmbientColor() {
        return ambientColor;
    }
    public FloatBuffer getAmbientColorBuffer(){
        FloatBuffer f;
        ByteBuffer b = ByteBuffer.allocateDirect(12);
        b.order(ByteOrder.nativeOrder());
        f = b.asFloatBuffer();
        f.put(ambientColor);
        f.position(0);
        return f;
    }
    public void setAmbientColor(float r, float g, float b) {
        ambientColor = new float[3];
        ambientColor[0]=r;
        ambientColor[1]=g;
        ambientColor[2]=b;
    }

    public float[] getDiffuseColor() {
        return diffuseColor;
    }
    public FloatBuffer getDiffuseColorBuffer(){
        FloatBuffer f;
        ByteBuffer b = ByteBuffer.allocateDirect(12);
        b.order(ByteOrder.nativeOrder());
        f = b.asFloatBuffer();
        f.put(diffuseColor);
        f.position(0);
        return f;
    }

    public void setDiffuseColor(float r, float g, float b) {
        diffuseColor = new float[3];
        diffuseColor[0]=r;
        diffuseColor[1]=g;
        diffuseColor[2]=b;
    }

    public float[] getSpecularColor() {
        return specularColor;
    }
    public FloatBuffer getSpecularColorBuffer(){
        FloatBuffer f;
        ByteBuffer b = ByteBuffer.allocateDirect(12);
        b.order(ByteOrder.nativeOrder());
        f = b.asFloatBuffer();
        f.put(specularColor);
        f.position(0);
        return f;
    }

    public void setSpecularColor(float r, float g, float b) {
        specularColor = new float[3];
        specularColor[0]=r;
        specularColor[1]=g;
        specularColor[2]=b;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getShine() {
        return shine;
    }

    public void setShine(float shine) {
        this.shine = shine;
    }

    public int getIllum() {
        return illum;
    }

    public void setIllum(int illum) {
        this.illum = illum;
    }

    public String getTextureFile() {
        return textureFile;
    }

    public void setTextureFile(String textureFile) {
        this.textureFile = textureFile;
    }
    public String toString(){
        String str=new String();
        str+="Material name: "+name;
        str+="\nAmbient color: "+ambientColor.toString();
        str+="\nDiffuse color: "+diffuseColor.toString();
        str+="\nSpecular color: "+specularColor.toString();
        str+="\nAlpha: "+alpha;
        str+="\nShine: "+shine;
        return str;
    }
}
