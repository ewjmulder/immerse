package com.programyourhome.immerse.domain.location.dynamic;

import static com.programyourhome.immerse.domain.util.MathUtil.calculateValueInRange;

import java.util.SortedMap;

import com.programyourhome.immerse.domain.location.Vector3D;

/**
 * Dynamic location based on a path, defined by key frames.
 * For a time before the first key frame, use the location of the first key frame.
 * For a time after the last key frame, use the location of the last key frame.
 * For a time in between 2 key frames, use linear interpolation.
 */
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
            // Special case: before first key frame. Use value of first key frame.
            location = this.keyFrames.get(this.keyFrames.firstKey());
        } else if (millisSinceStart > this.keyFrames.lastKey()) {
            // Special case: after last key frame. Use value of last key frame.
            location = this.keyFrames.get(this.keyFrames.lastKey());
        } else {
            Long millisKeyFrameBefore = this.keyFrames.headMap(millisSinceStart).lastKey();
            Vector3D vectorKeyFrameBefore = this.keyFrames.get(millisKeyFrameBefore);
            Long millisKeyFrameAfter = this.keyFrames.tailMap(millisSinceStart).firstKey();
            Vector3D vectorKeyFrameAfter = this.keyFrames.get(millisKeyFrameAfter);

            double distance = millisKeyFrameAfter - millisKeyFrameBefore;
            double fraction = (millisSinceStart - millisKeyFrameBefore) / distance;

            double locationX = calculateValueInRange(vectorKeyFrameBefore.getX(), vectorKeyFrameAfter.getX(), fraction);
            double locationY = calculateValueInRange(vectorKeyFrameBefore.getY(), vectorKeyFrameAfter.getY(), fraction);
            double locationZ = calculateValueInRange(vectorKeyFrameBefore.getZ(), vectorKeyFrameAfter.getZ(), fraction);
            location = new Vector3D(locationX, locationY, locationZ);
        }
        return location;
    }

}
