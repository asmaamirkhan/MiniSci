package com.asmaamir.minisci.tflite;

import android.graphics.Bitmap;
import android.graphics.RectF;

public interface SimilarityClassifier {

    void register(String name, Recognition recognition);

    //List<Recognition> recognizeImage(Bitmap bitmap, boolean getExtra);
    float[] recognizeImage(Bitmap bitmap, boolean getExtra);

    float findCosDistance(float[] knownEmb, float[] currentEmb);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();

    void setNumThreads(int num_threads);

    void setUseNNAPI(boolean isChecked);

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    public class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /**
         * Display name for the recognition.
         */
        private final String title;

        /**
         * A sortable score for how good the recognition is relative to others. Lower should be better.
         */
        private final Float distance;
        private Object extra;

        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;
        private Integer color;
        private Bitmap crop;

        public Recognition(
                final String id, final String title, final Float distance, final RectF location) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.location = location;
            this.color = null;
            this.extra = null;
            this.crop = null;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        public Object getExtra() {
            return this.extra;
        }

        public void setColor(Integer color) {
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getDistance() {
            return distance;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }

        public Integer getColor() {
            return this.color;
        }

        public void setCrop(Bitmap crop) {
            this.crop = crop;
        }

        public Bitmap getCrop() {
            return this.crop;
        }
    }
}
