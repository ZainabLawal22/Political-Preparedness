package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE )
    fun insertElectionList(elections: List<Election>)

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    fun insertElection(election: Election) :Long

    //TODO: Add select all election query
    @Query("Select * From election_table")
    fun getAllElections() : LiveData<List<Election>>

    //TODO: Add select single election query
    @Query("Select * from election_table where isSaved = 1")
    fun getAllSavedElections() : LiveData<List<Election>>

    //TODO: Add delete query
    @Query("UPDATE election_table SET deleteFlag = 1 WHERE id = :id")
    fun deleteElectionById(id: Int)

    //TODO: Add clear query
    @Query("SELECT * FROM election_table WHERE id=:id")
    fun getElectionById(id: Int): LiveData<Election>

}