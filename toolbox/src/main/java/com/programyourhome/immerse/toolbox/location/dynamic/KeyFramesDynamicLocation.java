package com.programyourhome.immerse.toolbox.location.dynamic;

import static com.programyourhome.immerse.toolbox.util.MathUtil.calculateValueInRange;

import java.util.SortedMap;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;

/**
 * Dynamic location based on a path, defined by key frames.
 *
 * Always true:
 * - The first key frame time must be 0. (if you want to start moving later, add a second key frame at a later time at the same location)
 * - For a time in between 2 key frames, linear interpolation in used.
 *
 * With loop mode:
 * - The last key frame location must be equal to the first key frame location.
 * - The input time is put into the configured times by taking the modulo. (so effectively looping over the key frames)
 *
 * Without loop mode:
 * - For a time before the first key frame, use the location of the first key frame.
 * - For a time after the last key frame, use the location of the last key frame. (so effectively stopping after one loop)
 */
public class KeyFramesDynamicLocation implements DynamicLocation {

    private static final long serialVersionUID = Serialization.VERSION;

    private final SortedMap<Long, Vector3D> keyFrames;
    private final boolean loop;

    public KeyFramesDynamicLocation(SortedMap<Long, Vector3D> keyFrames) {
        this(keyFrames, false);
    }

    public KeyFramesDynamicLocation(SortedMap<Long, Vector3D> keyFrames, boolean loop) {
        if (keyFrames.isEmpty()) {
            throw new IllegalArgumentException("At least 1 key frame is required.");
        }
        if (keyFrames.firstKey() != 0) {
            throw new IllegalArgumentException("The first key frame must be at time 0.");
        }
        if (loop && !keyFrames.get(keyFrames.firstKey()).equals(keyFrames.get(keyFrames.lastKey()))) {
            throw new IllegalArgumentException("In loop mode the location of the last key frame must be equal to the location of the first key frame");
        }
        this.keyFrames = keyFrames;
        this.loop = loop;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        Vector3D location;
        if (this.loop) {
            // Calculate the modulo to get a value within the frame times. (use floorMod instead of % to always get a positive value)
            millisSinceStart = Math.floorMod(millisSinceStart, this.keyFrames.lastKey());
        }
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

    public static Factory<DynamicLocation> keyFrames(SortedMap<Long, Vector3D> keyFrames) {
        return new Factory<DynamicLocation>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public DynamicLocation create() {
                return new KeyFramesDynamicLocation(keyFrames);
            }
        };
    }

    public static Factory<DynamicLocation> keyFramesLoop(SortedMap<Long, Vector3D> keyFrames) {
        return () -> new KeyFramesDynamicLocation(keyFrames, true);
    }

}
