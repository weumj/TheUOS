package com.uoscs09.theuos2.async;


import android.graphics.Bitmap;

import com.uoscs09.theuos2.util.ImageUtil;

public class ImageWriteProcessor extends FileWriteProcessor<Bitmap>{

    public ImageWriteProcessor(String fileName) {
        super(fileName);
    }

    @Override
    public String process(Bitmap bitmap) throws Exception {
        try {
            ImageUtil.saveImageToFile(fileName, bitmap);
            return fileName;
        } finally {
            if (bitmap != null)
                bitmap.recycle();
        }
    }
}
