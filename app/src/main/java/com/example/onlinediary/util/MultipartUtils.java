package com.example.onlinediary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class MultipartUtils {
    private static final MediaType TEXT = MediaType.parse("text/plain");
    private static final MediaType IMAGE_JPEG = MediaType.parse("image/jpeg");
    private static final long DEFAULT_MAX_IMAGE_BYTES = 950 * 1024; // keep under 1 MB
    private static final int DEFAULT_MAX_DIMENSION = 1600;

    private MultipartUtils() {}

    public static RequestBody toTextBody(String value) {
        if (value == null) {
            value = "";
        }
        return RequestBody.create(value, TEXT);
    }

    public static MultipartBody.Part createFilePart(Context context, String partName, Uri uri, String prefix) throws IOException {
        File file = FileUtils.copyToCache(context, uri, prefix);
        String mime = context.getContentResolver().getType(uri);
        MediaType mediaType = mime != null ? MediaType.parse(mime) : MediaType.parse("application/octet-stream");
        RequestBody body = RequestBody.create(file, mediaType);
        String fileName = FileUtils.getFileName(context, uri);
        return MultipartBody.Part.createFormData(partName, fileName, body);
    }

    public static MultipartBody.Part createImagePart(Context context, String partName, Uri uri, String prefix) throws IOException {
        return createImagePart(context, partName, uri, prefix, DEFAULT_MAX_IMAGE_BYTES);
    }

    public static MultipartBody.Part createImagePart(Context context, String partName, Uri uri, String prefix, long maxBytes) throws IOException {
        Bitmap bitmap = decodeScaledBitmap(context, uri, DEFAULT_MAX_DIMENSION);
        byte[] jpeg = compressBitmap(bitmap, maxBytes);

        File file = File.createTempFile(prefix, ".jpg", context.getCacheDir());
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(jpeg);
        }

        RequestBody body = RequestBody.create(file, IMAGE_JPEG);
        String fileName = FileUtils.getFileName(context, uri);
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = prefix + ".jpg";
        } else {
            String lower = fileName.toLowerCase();
            if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
                fileName = fileName + ".jpg";
            }
        }
        return MultipartBody.Part.createFormData(partName, fileName, body);
    }

    private static Bitmap decodeScaledBitmap(Context context, Uri uri, int maxDimension) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Unable to open image");
            }
            BitmapFactory.decodeStream(in, null, bounds);
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = calculateInSampleSize(bounds, maxDimension);
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Unable to open image");
            }
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, opts);
            if (bitmap == null) {
                throw new IOException("Unable to decode image");
            }
            return bitmap;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int maxDimension) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        while ((height / inSampleSize) > maxDimension || (width / inSampleSize) > maxDimension) {
            inSampleSize *= 2;
        }
        return Math.max(inSampleSize, 1);
    }

    private static byte[] compressBitmap(Bitmap bitmap, long maxBytes) throws IOException {
        int quality = 90;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        while (stream.size() > maxBytes && quality > 40) {
            stream.reset();
            quality -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        }
        return stream.toByteArray();
    }
}
