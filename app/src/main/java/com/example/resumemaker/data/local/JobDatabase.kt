package com.example.resumemaker.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

enum class JobStatus { APPLIED, INTERVIEWING, OFFER, REJECTED }

@Entity(tableName = "job_applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyName: String,
    val jobTitle: String,
    val dateApplied: Long = System.currentTimeMillis(),
    val status: JobStatus = JobStatus.APPLIED,
    val notes: String = ""
)

@Dao
interface JobDao {
    @Query("SELECT * FROM job_applications ORDER BY dateApplied DESC")
    fun getAllJobs(): Flow<List<JobApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobApplication)

    @Delete
    suspend fun deleteJob(job: JobApplication)

    @Update
    suspend fun updateJob(job: JobApplication)

    @Query("DELETE FROM job_applications")
    suspend fun deleteAll()
}

@Database(entities = [JobApplication::class], version = 1, exportSchema = false)
abstract class JobDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao

    companion object {
        @Volatile private var INSTANCE: JobDatabase? = null

        fun getDatabase(context: Context): JobDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JobDatabase::class.java,
                    "job_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}