package com.example.movie;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends AppCompatActivity implements MainView {
    PopupWindow popupWindow,popupWindow1;
    View v,collectionv;
    SearchView searchView;
    DAO dao_object;
    int searchflag=0,watchlistflag=0;
    String query;
    private TabLayout tabLayout;
    private BlurView bottomBlurView;
    private ViewGroup blurroot;
    boolean notify=false;


    MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainPresenter = new MainPresenter(this);
        initView();
        setupBlurView();
        //setupViewPager();

        //AppDatabase.clearInstance(this);

        AppDatabase db = Room.databaseBuilder(MainActivity.this,
                AppDatabase.class, "AppDatabase").fallbackToDestructiveMigration().build();



        dao_object = db.dao();


        RelativeLayout relativeLayout=findViewById(R.id.watchlistview);
        watchlistflag=1;
        int id=R.id.watchlistRVIEW;
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                List<DataClass> localData = dao_object.getAll();
                ArrayList<DataClass> arrayList = new ArrayList<>(localData);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Adapter smalladapter = new Adapter(MainActivity.this, arrayList, R.layout.itemposter_small);
                        RecyclerView recyclerView = relativeLayout.findViewById(id);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(smalladapter);
                        smalladapter.setOnItemClickListener(new Adapter.onItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Context context=MainActivity.this;
                                Log.d("SmallAdapter", "Item clicked at position: " + position);
                                //Toast.makeText(MainActivity.this, "Item clicked at position: " + position, Toast.LENGTH_SHORT).show();
                                LayoutInflater layoutInflater=LayoutInflater.from(context);
                                if(searchflag==1)
                                {
                                    if (searchView != null) {
                                        searchView.clearFocus();
                                    }
                                }
                                View view=layoutInflater.inflate(R.layout.moviedetails,null);

                                ImageView hero =view.findViewById(R.id.heroimage);
                                TextView title=view.findViewById(R.id.title);
                                TextView year=view.findViewById(R.id.year);
                                TextView description=view.findViewById(R.id.description);
                                String herourl=arrayList.get(position).getHerourl();

                                MaterialButton button=view.findViewById(R.id.watchlist);
                                //button.setEnabled(false);
                                //button.setVisibility(View.INVISIBLE);
                                //MaterialButton button=view.findViewById(R.id.watchlist);
                                //DataClass data = arrayList.get(position);
                                //Log.e("st1",arrayList.get(position).getUrl());
                                if(herourl!=null&&hero!=null) {

                                    Glide.with(view)
                                            .load(herourl)
                                            .centerCrop()
                                            .into(hero)
                                    ;
                                    title.setText(arrayList.get(position).getTitle());
                                    year.setText("Year: " + arrayList.get(position).getYear());
                                    description.setText(arrayList.get(position).getDescription());
                                    DataClass currentData = arrayList.get(position);

                                    if(currentData.added==true)
                                    {
                                        button.setText("Убрать из списка");
                                        int color = getResources().getColor(R.color.md_theme_light_background);
                                        int textcolor = getResources().getColor(R.color.md_theme_dark_secondary);
                                        button.setBackgroundTintList(ColorStateList.valueOf(color));
                                        button.setTextColor(textcolor);
                                        button.setIconTint(ColorStateList.valueOf(textcolor));
                                    }
                                    else{
                                        button.setText("Добавить в список");
                                        int color = getResources().getColor(R.color.md_theme_dark_secondary);
                                        int textcolor = getResources().getColor(R.color.md_theme_light_background);
                                        button.setBackgroundTintList(ColorStateList.valueOf(color));
                                        button.setTextColor(textcolor);
                                        button.setIconTint(ColorStateList.valueOf(textcolor));
                                    }
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if(currentData.added==true)
                                            {
                                                ExecutorService service=Executors.newSingleThreadExecutor();
                                                service.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dao_object.deleteDataById(currentData.key);
                                                        notify=true;

                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                currentData.added=false;
                                                                button.setText("Добавить в список");
                                                                int color = getResources().getColor(R.color.md_theme_dark_secondary);
                                                                int textcolor = getResources().getColor(R.color.md_theme_light_background);
                                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                                button.setTextColor(textcolor);
                                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                                notify=false;

                                                            }
                                                        });
                                                    }
                                                });

                                            }
                                            else {
                                                ExecutorService service=Executors.newSingleThreadExecutor();
                                                service.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        currentData.added = true;
                                                        dao_object.insertOrUpdate(currentData);
                                                        notify=true;
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                button.setText("Убрать из списка");
                                                                int color = getResources().getColor(R.color.md_theme_light_background);
                                                                int textcolor = getResources().getColor(R.color.md_theme_dark_secondary);
                                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                                button.setTextColor(textcolor);
                                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                                notify=false;

                                                            }
                                                        });
                                                    }
                                                });

                                            }



                                        }
                                    });


                                }
                                else
                                {
                                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                                }

                                //title.setText(arrayList.get(position));

                                popupWindow=new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                                popupWindow.setEnterTransition(new Slide());
                                popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
                                popupWindow.setExitTransition(new Slide());

                            }
                        });


                        smalladapter.notifyDataSetChanged();

                    }
                });
            }
        });




        ImageButton search=findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater=LayoutInflater.from(MainActivity.this);
                v=inflater.inflate(R.layout.search,null);
                RelativeLayout root=findViewById(R.id.main);
                root.addView(v,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                v.setVisibility(View.VISIBLE);
                searchflag=1;

                searchView=v.findViewById(R.id.searchbar);
                searchView.clearFocus(); //new_change
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String localquery) {
                        query=localquery;
                        mainPresenter.loadData("Search", R.id.searchRVIEW, MainActivity.this);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String localquery) {
                        query=localquery;
                        mainPresenter.loadData("Search",R.id.searchRVIEW, MainActivity.this);
                        return false;
                    }
                });

                /*

                ImageButton searchclick=v.findViewById(R.id.search);
                //TextInputEditText query=v.findViewById(R.id.searchboxtext);
                searchclick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadData("Search",R.id.searchRVIEW);





                    }
                });

                 */








            }
        });

        TabLayout mainTab=findViewById(R.id.tabLayout);
        mainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(MainActivity.this, R.color.md_theme_light_primary);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(MainActivity.this, R.color.md_theme_light_onPrimary);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabLayout.Tab watchlistTab = tabLayout.getTabAt(0);

        /*int initcolor = ContextCompat.getColor(MainActivity.this, R.color.md_theme_light_primary);
        if (homeTab.getIcon() != null) {
            homeTab.getIcon().setColorFilter(initcolor, PorterDuff.Mode.SRC_IN);
        }*/
        int initcolor = ContextCompat.getColor(MainActivity.this, R.color.md_theme_light_onPrimary);
        if (watchlistTab.getIcon() != null) {
            watchlistTab.getIcon().setColorFilter(initcolor, PorterDuff.Mode.SRC_IN);
        }



        // Setting onclick for tab items
        /*homeTab.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Clicking on the first tab takes you back to the main activity
                if(watchlistflag==1)
                {
                    watchlistflag=0;
                    RelativeLayout relativeLayout=findViewById(R.id.watchlistview);
                    relativeLayout.setVisibility(View.GONE);
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vb.hasVibrator()) {

                        VibrationEffect vibeEffect = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibeEffect = VibrationEffect.createOneShot(30, 50);
                            vb.vibrate(vibeEffect);
                        }

                    }


                }

            }
        });*/

        watchlistTab.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchlistflag=1;
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vb.hasVibrator()) {

                    VibrationEffect vibeEffect = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibeEffect = VibrationEffect.createOneShot(30, 50);
                        vb.vibrate(vibeEffect);
                    }

                }
                mainPresenter.serviceExecute(MainActivity.this);

            }
        });
    }


    @Override
    public void onBackPressed() {



        // if the popup window is showing
        if (popupWindow != null && popupWindow.isShowing()) {

            popupWindow.dismiss();
        } else  if (searchflag == 1 && searchView.hasFocus()) {

            searchView.clearFocus();
            }

         else if (searchflag == 1) {
                ((ViewGroup)v.getParent()).removeView(v);
                searchflag = 0;

        } else if (watchlistflag == 1) {
            watchlistflag = 0;
            RelativeLayout relativeLayout = findViewById(R.id.watchlistview);
            relativeLayout.setVisibility(View.INVISIBLE);
            TabLayout.Tab home = tabLayout.getTabAt(0);
            if (home != null) {
                home.select();
                int tabIconColor = ContextCompat.getColor(MainActivity.this, R.color.md_theme_light_primary);
                if (home.getIcon() != null) {
                    home.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }

            }
        } else if (popupWindow1 != null && popupWindow1.isShowing()) {
            popupWindow1.dismiss();
        } else {
            //default case of back
            super.onBackPressed();
        }
    }

    @NonNull
    private BlurAlgorithm getBlurAlgorithm() {
        BlurAlgorithm algorithm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            algorithm = new RenderEffectBlur();
        } else {
            algorithm = new RenderScriptBlur(this);
        }
        return algorithm;
    }


    private void initView() {
        //viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        bottomBlurView = findViewById(R.id.bottomBlurView);




        blurroot = findViewById(R.id.main);
    }


//for blur view
    private void setupBlurView() {
        final float radius = 25f;
        final float minBlurRadius = 4f;
        final float step = 4f;

        //set background, if your root layout doesn't have one
        final Drawable windowBackground = getWindow().getDecorView().getBackground();
        BlurAlgorithm algorithm = getBlurAlgorithm();


        bottomBlurView.setupWith(blurroot, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);


        int initialProgress = (int) (radius * step);

    }

    /*public void hideKeyboardAndClearFocus() {
        searchView.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }*/




}


