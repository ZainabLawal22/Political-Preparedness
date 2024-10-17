package com.example.android.politicalpreparedness.network.models

import android.os.Parcelable
import androidx.room.*
import com.squareup.moshi.*
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "election_table")
data class Election(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name")val name: String,
        @ColumnInfo(name = "electionDay")val electionDay: Date,
        @ColumnInfo(name = "isSaved") var isSaved: Boolean = false,
        @ColumnInfo(name = "deleteFlag") var deleteFlag: Boolean = false,
        @Embedded(prefix = "division_") @Json(name="ocdDivisionId") val division: Division
) : Parcelable