package mns.io.mdlm.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface DownloadFileDAO {

    @Insert
    public void insert(DownloadFile... downloadFiles);

    @Update
    public void update(DownloadFile... downloadFiles);

    @Delete
    public void delete(DownloadFile... downloadFiles);

    @Query("SELECT * FROM downloads")
    public List<DownloadFile> getDownloads();

    @Query("DELETE FROM downloads")
    public void rmAll();

    @Query("SELECT last_insert_rowid()")
    public int getLast();

}
