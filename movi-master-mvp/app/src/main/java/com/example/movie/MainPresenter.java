package com.example.movie;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eightbitlab.com.blurview.BlurView;

public class MainPresenter {
    private final MainView mainView;

    DAO dao_object;
    public MainPresenter(MainView mainView) {
        this.mainView = mainView;
    }

    public void loadData(String collection, int rviewid, MainActivity mainActivity) {

// Instantiate the RequestQueue :.
        RequestQueue queue = Volley.newRequestQueue(mainActivity);
        String API_KEY= BuildConfig.API_KEY;
        ArrayList<DataClass> arrayList=new ArrayList<>();
        Adapter adapter = new Adapter(mainActivity,arrayList,R.layout.itemposter);
        Adapter smalladapter = new Adapter(mainActivity,arrayList,R.layout.itemposter_small);
        RecyclerView recyclerView;

        GetURL getURL=new GetURL();

        String url = getURL.fetch(collection);
        if(collection.equalsIgnoreCase("Search"))
        {
            //TextInputEditText query=v.findViewById(R.id.searchboxtext);
            String searchval= mainActivity.query.toString().trim();
            String[] words=searchval.split("\\s+");
            String joined= TextUtils.join("+",words);
            String newurl=url.replace("+insertquery+",joined);
            Log.e("srch",newurl);
            url=newurl;
            Log.e("srch",url);




            recyclerView= mainActivity.findViewById(rviewid);

            GridLayoutManager gridLayoutManager=new GridLayoutManager(mainActivity,3);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(dy>0)
                    {
                        mainActivity.searchView.clearFocus();
                    }
                }
            });
            recyclerView.setAdapter(smalladapter);



        }



        else if (collection.equals("Movie")||collection.equals("TV")||collection.equals("Upcoming")) {
            recyclerView= mainActivity.collectionv.findViewById(rviewid);
            GridLayoutManager gridLayoutManager=new GridLayoutManager(mainActivity,3);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(smalladapter);


        }
        else {
            recyclerView= mainActivity.findViewById(rviewid);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mainActivity,LinearLayoutManager.HORIZONTAL,false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);
        }

        //recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new Adapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Context context= mainActivity;
                LayoutInflater layoutInflater=LayoutInflater.from(context);
                if(mainActivity.searchflag==1)
                {
                    if (mainActivity.searchView != null) {
                        mainActivity.searchView.clearFocus();
                    }

                }
                View view=layoutInflater.inflate(R.layout.moviedetails,null);

                ImageView hero =view.findViewById(R.id.heroimage);
                TextView title=view.findViewById(R.id.title);
                TextView year=view.findViewById(R.id.year);
                TextView description=view.findViewById(R.id.description);
                String herourl=arrayList.get(position).getHerourl();
                MaterialButton button=view.findViewById(R.id.watchlist);
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
                    year.setText(arrayList.get(position).getYear());
                    description.setText(arrayList.get(position).getDescription());
                    DataClass currentData = arrayList.get(position);
                    if(currentData.added==true)
                    {
                        button.setText("Убрать из списка");
                        int color = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                        int textcolor = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                        button.setBackgroundTintList(ColorStateList.valueOf(color));
                        button.setTextColor(textcolor);
                        button.setIconTint(ColorStateList.valueOf(textcolor));
                    }
                    else{
                        button.setText("Добавить в список");
                        int color = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                        int textcolor = mainActivity.getResources().getColor(R.color.md_theme_light_background);
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
                                        mainActivity.dao_object.deleteDataById(currentData.key);
                                        //notify=true;

                                        mainView.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                currentData.added=false;
                                                button.setText("Добавить в список");
                                                int color = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                                int textcolor = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                button.setTextColor(textcolor);
                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                mainActivity.notify=false;

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
                                        mainActivity.dao_object.insertOrUpdate(currentData);
                                        //notify=true;
                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                button.setText("Убрать из списка");
                                                int color = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                                int textcolor = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                button.setTextColor(textcolor);
                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                mainActivity.notify=false;

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

                mainActivity.popupWindow=new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                mainActivity.popupWindow.setEnterTransition(new Slide());
                mainActivity.popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
                mainActivity.popupWindow.setExitTransition(new Slide());

            }
        });

        smalladapter.setOnItemClickListener(new Adapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Context context= mainActivity;
                LayoutInflater layoutInflater=LayoutInflater.from(context);
                if(mainActivity.searchflag==1)
                {
                    if (mainActivity.searchView != null) {
                        mainActivity.searchView.clearFocus();
                    }

                }
                View view=layoutInflater.inflate(R.layout.moviedetails,null);

                ImageView hero =view.findViewById(R.id.heroimage);
                TextView title=view.findViewById(R.id.title);
                TextView year=view.findViewById(R.id.year);
                TextView description=view.findViewById(R.id.description);
                String herourl=arrayList.get(position).getHerourl();
                MaterialButton button=view.findViewById(R.id.watchlist);
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
                    year.setText(arrayList.get(position).getYear());
                    description.setText(arrayList.get(position).getDescription());
                    DataClass currentData = arrayList.get(position);
                    if(currentData.added==true)
                    {
                        button.setText("Убрать из списка");
                        int color = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                        int textcolor = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                        button.setBackgroundTintList(ColorStateList.valueOf(color));
                        button.setTextColor(textcolor);
                        button.setIconTint(ColorStateList.valueOf(textcolor));
                    }
                    else{
                        button.setText("Добавить в список");
                        int color = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                        int textcolor = mainActivity.getResources().getColor(R.color.md_theme_light_background);
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
                                        mainActivity.dao_object.deleteDataById(currentData.key);
                                        mainActivity.notify=true;

                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                currentData.added=false;
                                                button.setText("Добавить в список");
                                                int color = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                                int textcolor = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                button.setTextColor(textcolor);
                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                mainActivity.notify=false;

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
                                        mainActivity.dao_object.insertOrUpdate(currentData);
                                        mainActivity.notify=true;
                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                button.setText("Убрать из списка");
                                                int color = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                                int textcolor = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                button.setTextColor(textcolor);
                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                mainActivity.notify=false;

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

                mainActivity.popupWindow=new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                mainActivity.popupWindow.setEnterTransition(new Slide());
                mainActivity.popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
                mainActivity.popupWindow.setExitTransition(new Slide());

            }
        });










// Request a string response from the provided URL.

        Log.e("api1",url);
        String baseimageurl="https://image.tmdb.org/t/p/original";

        Log.e("stringurl",url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e("api1",response.toString());


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("results");
                            for (int i = 0; i <jsonObject.getJSONArray("results").length(); i++) {

                                JSONObject movies=jsonArray.getJSONObject(i);
                                int key= movies.getInt("id");

                                String imgurl=baseimageurl+movies.getString("poster_path");
                                String herourl=baseimageurl+movies.getString("backdrop_path");
                                //Log.e("api2",imgurl);
                                String title="";
                                if(movies.has("title"))
                                {
                                    title=movies.getString("title");
                                } else if (movies.has("name")) {
                                    title=movies.getString("name");
                                } else if (movies.has("original_title")) {
                                    title=movies.getString("original_title");
                                }
                                String description=movies.getString("overview");
                                String year=movies.getString("release_date");
                                DataClass data=new DataClass(key,imgurl,herourl,title,year,description,false);
                                ExecutorService service = Executors.newSingleThreadExecutor();
                                service.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataClass duplicate = mainActivity.dao_object.checkduplicateById(data.key);
                                        if(duplicate!=null)
                                        {
                                            data.setAdded(true);
                                            //Log.e("test",data.title+data.added);
                                        }

                                    }
                                });



                                arrayList.add(data);
                                adapter.notifyDataSetChanged();
                                smalladapter.notifyDataSetChanged();//took me 3hrs to find this error broo!!





                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }






                        //Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mainActivity, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);



    }

    public void loadLocalData() {
        ArrayList<DataClass> arrayList=new ArrayList<>();
        List<DataClass> localData = dao_object.getAll();
        arrayList.addAll(localData);

    }

    public void serviceExecute(MainActivity mainActivity) {
        RelativeLayout relativeLayout= mainActivity.findViewById(R.id.watchlistview);
        relativeLayout.setVisibility(View.VISIBLE);
        int id=R.id.watchlistRVIEW;
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                List<DataClass> localData = mainActivity.dao_object.getAll();
                ArrayList<DataClass> arrayList = new ArrayList<>(localData);


                mainView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Adapter smalladapter = new Adapter(mainActivity, arrayList, R.layout.itemposter_small);
                        RecyclerView recyclerView = relativeLayout.findViewById(id);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(mainActivity, 3);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(smalladapter);
                        smalladapter.setOnItemClickListener(new Adapter.onItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Context context= mainActivity;
                                Log.d("SmallAdapter", "Item clicked at position: " + position);
                                //Toast.makeText(MainActivity.this, "Item clicked at position: " + position, Toast.LENGTH_SHORT).show();
                                LayoutInflater layoutInflater=LayoutInflater.from(context);
                                if(mainActivity.searchflag==1)
                                {
                                    if (mainActivity.searchView != null) {
                                        mainActivity.searchView.clearFocus();
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
                                    year.setText(arrayList.get(position).getYear());
                                    description.setText(arrayList.get(position).getDescription());
                                    DataClass currentData = arrayList.get(position);

                                    if(currentData.added==true)
                                    {
                                        button.setText("Убрать из списка");
                                        int color = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                        int textcolor = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                        button.setBackgroundTintList(ColorStateList.valueOf(color));
                                        button.setTextColor(textcolor);
                                        button.setIconTint(ColorStateList.valueOf(textcolor));
                                    }
                                    else{
                                        button.setText("Добавить в список");
                                        int color = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                        int textcolor = mainActivity.getResources().getColor(R.color.md_theme_light_background);
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
                                                        mainActivity.dao_object.deleteDataById(currentData.key);
                                                        mainActivity.notify=true;

                                                        mainView.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                currentData.added=false;
                                                                button.setText("Добавить в список");
                                                                int color = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                                                int textcolor = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                                button.setTextColor(textcolor);
                                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                                mainActivity.notify=false;

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
                                                        mainActivity.dao_object.insertOrUpdate(currentData);
                                                        mainActivity.notify=true;
                                                        mainView.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                button.setText("Убрать из списка");
                                                                int color = mainActivity.getResources().getColor(R.color.md_theme_light_background);
                                                                int textcolor = mainActivity.getResources().getColor(R.color.md_theme_dark_secondary);
                                                                button.setBackgroundTintList(ColorStateList.valueOf(color));
                                                                button.setTextColor(textcolor);
                                                                button.setIconTint(ColorStateList.valueOf(textcolor));
                                                                mainActivity.notify=false;

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

                                mainActivity.popupWindow=new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                                mainActivity.popupWindow.setEnterTransition(new Slide());
                                mainActivity.popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
                                mainActivity.popupWindow.setExitTransition(new Slide());

                            }
                        });


                        smalladapter.notifyDataSetChanged();

                    }
                });
            }
        });
    }

}
