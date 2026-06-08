package com.century.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.century.app.data.local.dao.*
import com.century.app.data.local.entity.*

@Database(
    entities = [
        UserProfile::class,
        WeightLog::class,
        WorkoutSession::class,
        ExerciseLog::class,
        ExerciseImage::class,
        PushUpTest::class
    ],
    version = 2,
    exportSchema = true
)
abstract class CenturyDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun exerciseImageDao(): ExerciseImageDao
    abstract fun pushUpTestDao(): PushUpTestDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS exercise_logs_to_delete")
                db.execSQL(
                    """
                    CREATE TEMP TABLE exercise_logs_to_delete AS
                    SELECT old.id
                    FROM exercise_log AS old
                    WHERE EXISTS (
                        SELECT 1
                        FROM exercise_log AS newer
                        WHERE newer.sessionId = old.sessionId
                            AND newer.exerciseName = old.exerciseName
                            AND (
                                COALESCE(newer.completedAt, 0) > COALESCE(old.completedAt, 0)
                                OR (
                                    COALESCE(newer.completedAt, 0) = COALESCE(old.completedAt, 0)
                                    AND newer.id > old.id
                                )
                            )
                    )
                    """
                )
                db.execSQL("DELETE FROM exercise_log WHERE id IN (SELECT id FROM exercise_logs_to_delete)")
                db.execSQL("DROP TABLE exercise_logs_to_delete")

                db.execSQL("ALTER TABLE exercise_log ADD COLUMN exerciseIndex INTEGER NOT NULL DEFAULT 0")

                db.execSQL(
                    """
                    UPDATE exercise_log
                    SET exerciseIndex = (
                        SELECT COUNT(*)
                        FROM exercise_log AS earlier
                        WHERE earlier.sessionId = exercise_log.sessionId
                            AND earlier.id < exercise_log.id
                    )
                    """
                )

                db.execSQL("DROP TABLE IF EXISTS sessions_to_delete")
                db.execSQL(
                    """
                    CREATE TEMP TABLE sessions_to_delete AS
                    SELECT old.id
                    FROM workout_session AS old
                    WHERE EXISTS (
                        SELECT 1
                        FROM workout_session AS newer
                        WHERE newer.userId = old.userId
                            AND newer.weekNumber = old.weekNumber
                            AND newer.dayNumber = old.dayNumber
                            AND (
                                newer.startedAt > old.startedAt
                                OR (newer.startedAt = old.startedAt AND newer.id > old.id)
                            )
                    )
                    """
                )
                db.execSQL("DELETE FROM exercise_log WHERE sessionId IN (SELECT id FROM sessions_to_delete)")
                db.execSQL("DELETE FROM workout_session WHERE id IN (SELECT id FROM sessions_to_delete)")
                db.execSQL("DROP TABLE sessions_to_delete")

                db.execSQL("DROP TABLE IF EXISTS exercise_images_to_delete")
                db.execSQL(
                    """
                    CREATE TEMP TABLE exercise_images_to_delete AS
                    SELECT old.id
                    FROM exercise_image AS old
                    WHERE EXISTS (
                        SELECT 1
                        FROM exercise_image AS newer
                        WHERE newer.illustrationId = old.illustrationId
                            AND (
                                newer.updatedAt > old.updatedAt
                                OR (newer.updatedAt = old.updatedAt AND newer.id > old.id)
                            )
                    )
                    """
                )
                db.execSQL("DELETE FROM exercise_image WHERE id IN (SELECT id FROM exercise_images_to_delete)")
                db.execSQL("DROP TABLE exercise_images_to_delete")

                db.execSQL("DROP TABLE IF EXISTS push_up_tests_to_delete")
                db.execSQL(
                    """
                    CREATE TEMP TABLE push_up_tests_to_delete AS
                    SELECT old.id
                    FROM push_up_test AS old
                    WHERE EXISTS (
                        SELECT 1
                        FROM push_up_test AS newer
                        WHERE newer.userId = old.userId
                            AND newer.weekNumber = old.weekNumber
                            AND (
                                newer.testedAt > old.testedAt
                                OR (newer.testedAt = old.testedAt AND newer.id > old.id)
                            )
                    )
                    """
                )
                db.execSQL("DELETE FROM push_up_test WHERE id IN (SELECT id FROM push_up_tests_to_delete)")
                db.execSQL("DROP TABLE push_up_tests_to_delete")

                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_log_sessionId_exerciseIndex ON exercise_log(sessionId, exerciseIndex)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_workout_session_userId_weekNumber_dayNumber ON workout_session(userId, weekNumber, dayNumber)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_image_illustrationId ON exercise_image(illustrationId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_push_up_test_userId_weekNumber ON push_up_test(userId, weekNumber)")
            }
        }
    }
}
