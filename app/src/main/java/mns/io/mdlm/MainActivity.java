package mns.io.mdlm;

import android.app.Activity;
import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.RequestInfo;
import com.tonyodev.fetch2.database.migration.Migration;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Func;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mns.io.mdlm.Adapters.RecyclerAdapter;
import mns.io.mdlm.models.AppDatabase;
import mns.io.mdlm.models.DownloadFile;
import mns.io.mdlm.models.DownloadFileDAO;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<DownloadFile> downloads;
    String url;
    FloatingActionButton fab;
    ConstraintLayout root;
    DownloadFileDAO mContactDAO;
    private Fetch fetch;
    ProgressBar pb;
    FetchConfiguration fetchConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        pb = findViewById(R.id.progressBar);


        mContactDAO = Room.databaseBuilder(this, AppDatabase.class, "db-downloads")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
                .getContactDAO();


        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();


        fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(3)
                .setHttpDownloader(new OkHttpDownloader(okHttpClient))
                .build();

        fetch = Fetch.Impl.getInstance(fetchConfiguration);


        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        downloads = getDownloadsInfo();

        adapter = new RecyclerAdapter(downloads);
        recyclerView.setAdapter(adapter);


        root = findViewById(R.id.root);
        fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder.setTitle("New Download");

                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View dialogView = layoutInflater.inflate(R.layout.new_download_attempt, null);
                builder.setView(dialogView);

                final EditText input = dialogView.findViewById(R.id.url_text);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        url = input.getText().toString();


                        DownloadFile download = new DownloadFile();

                        download.setUrl(url);
                        String names[] = download.getUrl().split("/");
                        String name = names[names.length - 1];
                        download.setFName(name);
                        downloads.add(download);


                        try {
                            mContactDAO.insert(download);
                            setResult(RESULT_OK);
                            adapter = new RecyclerAdapter(downloads);
                            recyclerView.setAdapter(adapter);
                            startDownload(download);
                            // finish();

                        } catch (SQLiteConstraintException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage() + "///" + mContactDAO.getLast(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mContactDAO.rmAll();
                downloads = getDownloadsInfo();
                adapter = new RecyclerAdapter(downloads);
                recyclerView.setAdapter(adapter);
                return true;
            }
        });


    }

    private List<DownloadFile> getDownloadsInfo() {
        return mContactDAO.getDownloads();
    }

    public void startDownload(DownloadFile dl) {
        fetch.deleteAll();
        String url = dl.getUrl();

        File dled = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/mdls/" + dl.getFName());
        if (dled.exists()) {
            dled.delete();
            try {
                dled.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String file = dled.getPath();
        /*Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" +name*/
        ;

        final Request request = new Request(url, file);

        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);

        fetch.enqueue(request, result -> {

                    FetchListener fetchListener = new FetchListener() {
                        @Override
                        public void onAdded(@NotNull Download download) {

                        }

                        @Override
                        public void onQueued(@NotNull Download download, boolean b) {

                        }

                        @Override
                        public void onWaitingNetwork(@NotNull Download download) {
                            Log.i("FEETH", "waiting");
                        }

                        @Override
                        public void onCompleted(@NotNull Download download) {

                            Log.i("FEETH", "completed");

                        }

                        @Override
                        public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                            Log.i("FEETH", "errrrr");
                        }

                        @Override
                        public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {

                        }

                        @Override
                        public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {

                        }

                        @Override
                        public void onProgress(@NotNull Download download, long l, long l1) {





                        }

                        @Override
                        public void onPaused(@NotNull Download download) {

                        }

                        @Override
                        public void onResumed(@NotNull Download download) {

                        }

                        @Override
                        public void onCancelled(@NotNull Download download) {

                        }

                        @Override
                        public void onRemoved(@NotNull Download download) {

                        }

                        @Override
                        public void onDeleted(@NotNull Download download) {

                        }
                    };

                    fetch.addListener(fetchListener);


                    Log.i("FEETH", "downloading");
                    Log.i("FEETH", request.getFile());
                },
                result -> {
                    Log.i("FEETH", result.getThrowable().getMessage());
                });


    }

    @Override
    protected void onDestroy() {
        fetch.close();
        finish();
        super.onDestroy();
    }
}

