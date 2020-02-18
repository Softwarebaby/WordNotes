package software.baby.wordnotes.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import software.baby.wordnotes.dao.WordDao;
import software.baby.wordnotes.entity.Word;

/**
 * Created by Du Senmiao on 2020/02/18
 */
@Database(entities = {Word.class}, version = 2, exportSchema = false)
public abstract class WordDatabase extends RoomDatabase {
    //单例模式：减少频繁创建所造成的资源消耗
    private static WordDatabase INSTANCE;

    public static synchronized WordDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    WordDatabase.class, "word_database")
//                    .allowMainThreadQueries()  //允许在主线程中执行
//                    .fallbackToDestructiveMigration()  //不添加任何迁移策略，破坏式迁移：将现有的数据清空
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }

    public abstract WordDao getWordDao();

    //迁移策略
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE WORD ADD COLUMN chinese_invisible INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE WORD_TEMP (ID INTEGER PRIMARY KEY NOT NULL, " +
                    "english_word TEXT, chinese_meaning TEXT)");
            database.execSQL("INSERT INTO WORD_TEMP (ID, english_word, chinese_meaning)" +
                    "SELECT ID, english_word, chinese_meaning FROM WORD");
            database.execSQL("DROP TABLE WORD");
            database.execSQL("ALTER TABLE WORD_TEMP RENAME TO WORD");
        }
    };
}
