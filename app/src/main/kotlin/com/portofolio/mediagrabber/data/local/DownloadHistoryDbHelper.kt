package com.portofolio.mediagrabber.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.portofolio.mediagrabber.domain.model.DownloadedMedia
import com.portofolio.mediagrabber.domain.model.PlatformType

class DownloadHistoryDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT,
                $COLUMN_AUTHOR TEXT,
                $COLUMN_COVER_URL TEXT,
                $COLUMN_PLATFORM TEXT,
                $COLUMN_CONTENT_URI TEXT,
                $COLUMN_FILENAME TEXT,
                $COLUMN_DOWNLOADED_AT INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insert(item: DownloadedMedia): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, item.id)
            put(COLUMN_TITLE, item.title)
            put(COLUMN_AUTHOR, item.author)
            put(COLUMN_COVER_URL, item.coverUrl)
            put(COLUMN_PLATFORM, item.platform.name)
            put(COLUMN_CONTENT_URI, item.contentUri)
            put(COLUMN_FILENAME, item.filename)
            put(COLUMN_DOWNLOADED_AT, item.downloadedAt)
        }
        return db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun delete(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id))
    }

    fun getAll(): List<DownloadedMedia> {
        val list = mutableListOf<DownloadedMedia>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DOWNLOADED_AT DESC"
        )
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
            val titleIndex = it.getColumnIndexOrThrow(COLUMN_TITLE)
            val authorIndex = it.getColumnIndexOrThrow(COLUMN_AUTHOR)
            val coverIndex = it.getColumnIndexOrThrow(COLUMN_COVER_URL)
            val platformIndex = it.getColumnIndexOrThrow(COLUMN_PLATFORM)
            val uriIndex = it.getColumnIndexOrThrow(COLUMN_CONTENT_URI)
            val filenameIndex = it.getColumnIndexOrThrow(COLUMN_FILENAME)
            val timeIndex = it.getColumnIndexOrThrow(COLUMN_DOWNLOADED_AT)

            while (it.moveToNext()) {
                val platformName = it.getString(platformIndex)
                val platform = runCatching { PlatformType.valueOf(platformName) }.getOrDefault(PlatformType.UNKNOWN)
                list.add(
                    DownloadedMedia(
                        id = it.getString(idIndex),
                        title = it.getString(titleIndex),
                        author = it.getString(authorIndex),
                        coverUrl = it.getString(coverIndex),
                        platform = platform,
                        contentUri = it.getString(uriIndex),
                        filename = it.getString(filenameIndex),
                        downloadedAt = it.getLong(timeIndex)
                    )
                )
            }
        }
        return list
    }

    companion object {
        private const val DATABASE_NAME = "mediagrabber.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "download_history"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AUTHOR = "author"
        private const val COLUMN_COVER_URL = "cover_url"
        private const val COLUMN_PLATFORM = "platform"
        private const val COLUMN_CONTENT_URI = "content_uri"
        private const val COLUMN_FILENAME = "filename"
        private const val COLUMN_DOWNLOADED_AT = "downloaded_at"
    }
}
