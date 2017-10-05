package com.programyourhome.immerse.domain.location.dynamic;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import com.programyourhome.immerse.domain.location.Vector3D;

public class KeyFrameDynamicLocation implements DynamicLocation {

    private SortedMap<Long, Vector3D> keyFrames;

    public KeyFrameDynamicLocation(SortedMap<Long, Vector3D> keyFrames) {
        this.keyFrames = keyFrames;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        if (this.keyFrames.containsKey(millisSinceStart)) {
            // Special case: direct hit.
            return this.keyFrames.get(millisSinceStart);
        }
        try {
            Long millisKeyFrameBefore = this.keyFrames.headMap(millisSinceStart).lastKey();
            Vector3D vectorKeyFrameBefore = this.keyFrames.get(millisKeyFrameBefore);
            Long millisKeyFromAfter = this.keyFrames.tailMap(millisSinceStart).firstKey();
            Vector3D vectorKeyFrameAfter = this.keyFrames.get(millisKeyFromAfter);

            double distance = millisKeyFromAfter - millisKeyFrameBefore;
            double fraction = (millisSinceStart - millisKeyFrameBefore) / distance;

            double locationX = (1 - fraction) * vectorKeyFrameBefore.getX() + fraction * vectorKeyFrameAfter.getX();
            double locationY = (1 - fraction) * vectorKeyFrameBefore.getY() + fraction * vectorKeyFrameAfter.getY();
            double locationZ = (1 - fraction) * vectorKeyFrameBefore.getZ() + fraction * vectorKeyFrameAfter.getZ();
            return new Vector3D(locationX, locationY, locationZ);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Requested millis: '" + millisSinceStart + "' not in range of key frames.", e);
        }
    }

    // TODO: make into good unit test
    public static void main(String[] args) {
        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 0, 0));
        keyFrames.put(10L, new Vector3D(10, 10, 10));
        keyFrames.put(20L, new Vector3D(210, 210, 210));
        keyFrames.put(30L, new Vector3D(3210, 3210, 3210));

        KeyFrameDynamicLocation keyFramedDynamicLocation = new KeyFrameDynamicLocation(keyFrames);
        System.out.println(keyFramedDynamicLocation.getLocation(15));

    }

}
