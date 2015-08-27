package se.chai.vr.objparse;

import java.util.Vector;

import android.util.Log;

public class Triangulator{
    public static Vector<Short> triangulate(Vector<Short> polygon){
        Vector<Short> triangles=new Vector<Short>();
        for(int i=1; i<polygon.size()-1; i++){
            triangles.add(polygon.get(0));
            triangles.add(polygon.get(i));
            triangles.add(polygon.get(i+1));
        }
        return triangles;
    }
}