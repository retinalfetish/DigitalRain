/*
 * Copyright 2021 Christopher Zaborsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unary.digitalrain;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Creates a rain shower effect of falling random ASCII characters on the given canvas.
 */
public class Rain {

    private static final char ASCII_MIN = 32;
    private static final char ASCII_MAX = 255;
    private static final int MULTIPLIER = 10;

    private Canvas mCanvas;
    private Paint mPaint;
    private int[] mPositions;
    private float mDropRate;

    /**
     * Constructor to initialize the rain with a few visible drops placed within the field.
     *
     * @param canvas Drawing canvas to use.
     * @param paint  Paint to draw with.
     */
    public Rain(Canvas canvas, Paint paint) {
        mCanvas = canvas;
        mPaint = paint;

        float textSize = mPaint.getTextSize();

        mPositions = new int[(int) (canvas.getWidth() / textSize) + 1];
        mDropRate = 1 - textSize / canvas.getHeight();

        // Start with a few drops
        for (int i = 0; i < mPositions.length; i++) {
            mPositions[i] = (int) (Math.random() * canvas.getHeight() * MULTIPLIER / textSize);
        }
    }

    /**
     * Update the canvas with random ASCII characters drawn into position below the last.
     */
    public void draw() {
        float textSize = mPaint.getTextSize();

        for (int i = 0; i < mPositions.length; i++) {
            String character = "" + (char) (Math.random() * (ASCII_MAX - ASCII_MIN) + ASCII_MIN);

            mCanvas.drawText(character, i * textSize, mPositions[i] * textSize, mPaint);

            // Fall past the screen height
            if (mPositions[i] * textSize > mCanvas.getHeight() && Math.random() > mDropRate) {
                mPositions[i] = 0;
            }

            mPositions[i]++;
        }
    }
}
