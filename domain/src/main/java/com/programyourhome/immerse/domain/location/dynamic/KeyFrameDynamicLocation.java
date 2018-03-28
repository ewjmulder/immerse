package com.programyourhome.immerse.domain.location.dynamic;

import static com.programyourhome.immerse.domain.util.MathUtil.calculateFractionInRange;

import java.util.SortedMap;
import java.util.TreeMap;

import com.programyourhome.immerse.domain.location.Vector3D;

public class KeyFrameDynamicLocation implements DynamicLocation {

    private final SortedMap<Long, Vector3D> keyFrames;

    public KeyFrameDynamicLocation(SortedMap<Long, Vector3D> keyFrames) {
        if (keyFrames.isEmpty()) {
            throw new IllegalArgumentException("At least 1 key frame is required.");
        }
        this.keyFrames = keyFrames;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        Vector3D location;
        if (this.keyFrames.containsKey(millisSinceStart)) {
            // Special case: direct hit.
            location = this.keyFrames.get(millisSinceStart);
        } else if (millisSinceStart < this.keyFrames.firstKey()) {
            // Special case: before first keyframe. Use value of first keyframe.
            location = this.keyFrames.get(this.keyFrames.firstKey());
        } else if (millisSinceStart > this.keyFrames.lastKey()) {
            // Special case: after last keyframe. Use value of last keyframe.
            location = this.keyFrames.get(this.keyFrames.lastKey());
        } else {
            Long millisKeyFrameBefore = this.keyFrames.headMap(millisSinceStart).lastKey();
            Vector3D vectorKeyFrameBefore = this.keyFrames.get(millisKeyFrameBefore);
            Long millisKeyFromAfter = this.keyFrames.tailMap(millisSinceStart).firstKey();
            Vector3D vectorKeyFrameAfter = this.keyFrames.get(millisKeyFromAfter);

            double distance = millisKeyFromAfter - millisKeyFrameBefore;
            double fraction = (millisSinceStart - millisKeyFrameBefore) / distance;

            double locationX = calculateFractionInRange(fraction, vectorKeyFrameBefore.getX(), vectorKeyFrameAfter.getX());
            double locationY = calculateFractionInRange(fraction, vectorKeyFrameBefore.getY(), vectorKeyFrameAfter.getY());
            double locationZ = calculateFractionInRange(fraction, vectorKeyFrameBefore.getZ(), vectorKeyFrameAfter.getZ());
            location = new Vector3D(locationX, locationY, locationZ);
        }
        return location;
    }

    // TODO: make into good unit test
    public static void main(String[] args) {
        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 0, 0));
        keyFrames.put(10L, new Vector3D(10, 10, 10));
        keyFrames.put(20L, new Vector3D(210, 210, 210));
        keyFrames.put(30L, new Vector3D(3210, 3210, 3210));

        KeyFrameDynamicLocation keyFramedDynamicLocation = new KeyFrameDynamicLocation(keyFrames);
        System.out.println(keyFramedDynamicLocation.getLocation(9));
        System.out.println(keyFramedDynamicLocation.getLocation(15));
        System.out.println(keyFramedDynamicLocation.getLocation(28));
    }

}
