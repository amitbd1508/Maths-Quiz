package me.amitghosh.mathquiz.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import me.amitghosh.mathquiz.R;
import me.amitghosh.mathquiz.adapter.SetAdapter;
import me.amitghosh.mathquiz.database.DatabaseAccess;
import me.amitghosh.mathquiz.model.SetModel;
import me.amitghosh.mathquiz.utils.ConnectionDetector;
import me.amitghosh.mathquiz.utils.Constant;

import java.util.ArrayList;
import java.util.List;


public class SetActivity extends BaseActivity implements SetAdapter.ItemClick {

    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    boolean checkReminder = false;
    TextView tv_total_set, tv_total_question;
    DatabaseAccess databaseAccess;
    RelativeLayout layout_cell;
    View view;
    int id, main_id;
    String tableName;
    int themePosition, main_theme;
    List<SetModel> setModelList = new ArrayList<>();
    boolean interstitialCanceled;
    InterstitialAd mInterstitialAd;
    ConnectionDetector cd;
    int Mainposition = 0,themepos=0;
    String Title = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constant.setDefaultLanguage(this);
        setContentView(R.layout.activity_set);
        init();
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> backIntent());
        getSupportActionBar().setTitle(null);
        progressDialog = new ProgressDialog(this);
        id = getIntent().getIntExtra(Constant.ID, 0);
        main_id = getIntent().getIntExtra(Constant.MAIN_ID, 0);
        tableName = getIntent().getStringExtra(Constant.TABLE_NAME);
        themePosition = getIntent().getIntExtra(Constant.THEMEPOSITION, 0);
        main_theme = getIntent().getIntExtra(Constant.MAIN_THEME, 0);
        layout_cell = findViewById(R.id.layout_cell);
        view = findViewById(R.id.view);
        recyclerView = findViewById(R.id.recyclerView);
        tv_total_set = findViewById(R.id.tv_total_set);
        tv_total_question = findViewById(R.id.tv_total_question);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        toolbar.setBackgroundResource(Constant.getDrawbles().get(main_theme).cell);
        layout_cell.setBackgroundResource(Constant.getDrawbles().get(main_theme).cell);
        view.setBackgroundResource(Constant.getDrawbles().get(main_theme).cell);


        if (!TextUtils.isEmpty(tableName)) {
            TextView mTitle = findViewById(R.id.toolbar_title);
            mTitle.setText(Constant.getToolbarTitle(this, tableName));
        }


        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        setModelList = databaseAccess.getSetdata(tableName, main_id, checkReminder);
        databaseAccess.close();

        tv_total_set.setText(String.valueOf(setModelList.size()));
        tv_total_question.setText(String.valueOf((setModelList.size() * Constant.QUIZ_SIZE)));

        SetAdapter setAdapter = new SetAdapter(this, setModelList, tableName);
        recyclerView.setAdapter(setAdapter);
        setAdapter.setClickListener(this);

        recyclerView.scrollToPosition((id - 1));


    }


    public void backIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void itemClick(int position, int themePosition1, String title) {



        Mainposition = position;

        themepos = themePosition1;
        Title = title;
        if (!interstitialCanceled) {
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                ContinueIntent();
//                    onBackPressed();
            }
        }

    }

    @Override
    public void onBackPressed() {
        backIntent();
    }

    @Override
    protected void onPause() {
        mInterstitialAd = null;
        interstitialCanceled = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        interstitialCanceled = false;
        if (getResources().getString(R.string.ADS_VISIBILITY).equals("YES")) {
            CallNewInsertial();
        }
        super.onResume();
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }
    private void CallNewInsertial() {
        cd = new ConnectionDetector(SetActivity.this);
        if (cd.isConnectingToInternet()) {
            mInterstitialAd = new InterstitialAd(SetActivity.this);
            mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
            requestNewInterstitial();
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdClosed() {
                    ContinueIntent();
                }
            });
        }
    }

    private void ContinueIntent() {
        SetModel setModel = setModelList.get(Mainposition);

        Intent intent = new Intent(this, LevelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constant.TITLE, Title);
        intent.putExtra(Constant.PRACTICE_SET, setModel.practice_set);
        intent.putExtra(Constant.ID, setModel.id);
        intent.putExtra(Constant.THEMEPOSITION, themepos);
        intent.putExtra(Constant.MAIN_THEME, main_theme);
        intent.putExtra(Constant.MAIN_ID, main_id);
        intent.putExtra(Constant.TABLE_NAME, tableName);
        if (checkReminder) {
            if (setModel.isRemider.equals(getString(R.string.str_true))) {
                intent.putExtra(Constant.IsReminder, true);
            } else {
                intent.putExtra(Constant.IsReminder, false);
            }
        }
        startActivity(intent);

    }

}
