package com.slate.downloader;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.ffmpeg.FFmpeg;

public class MainActivity extends Activity {
    final int bg = Color.rgb(13,17,26), card = Color.rgb(25,32,46), white = Color.WHITE, muted = Color.rgb(165,176,195), accent = Color.rgb(76,136,255);
    EditText url; Spinner quality; ProgressBar progress; TextView status; Button download, cancel;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    volatile boolean active = false;
    final String processId = "slate-current";
    String[] options = {"Best available", "1440p (2K)", "1080p", "720p", "480p", "MP3 audio"};
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroller = new ScrollView(this); scroller.setFillViewport(true); scroller.setBackgroundColor(bg);
        LinearLayout root = new LinearLayout(this); root.setPadding(dp(23),dp(32),dp(23),dp(28)); root.setOrientation(LinearLayout.VERTICAL); scroller.addView(root); setContentView(scroller);
        TextView brand = text("SLATE  /  MOBILE", 13, accent, true); root.addView(brand);
        TextView heading = text("Save your videos.", 30, white, true); heading.setPadding(0,dp(22),0,dp(7)); root.addView(heading);
        TextView sub = text("A simple, private downloader for Android.", 14, muted, false); root.addView(sub);
        LinearLayout panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(18),dp(20),dp(18),dp(21)); panel.setBackground(shape(card,18)); LinearLayout.LayoutParams panelLP = new LinearLayout.LayoutParams(-1,-2); panelLP.topMargin=dp(32); root.addView(panel,panelLP);
        panel.addView(text("VIDEO URL",12,muted,true));
        url = new EditText(this); url.setSingleLine(true); url.setTextColor(white); url.setHintTextColor(muted); url.setTextSize(14); url.setHint("https://www.youtube.com/watch?v=..."); url.setPadding(dp(14),0,dp(14),0); url.setBackground(shape(bg,10)); LinearLayout.LayoutParams urlLP=new LinearLayout.LayoutParams(-1,dp(54)); urlLP.topMargin=dp(9); panel.addView(url,urlLP);
        TextView qlabel = text("FORMAT & QUALITY",12,muted,true); qlabel.setPadding(0,dp(24),0,dp(9)); panel.addView(qlabel);
        quality=new Spinner(this); ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options); adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); quality.setAdapter(adapter); quality.setBackground(shape(bg,10)); panel.addView(quality,new LinearLayout.LayoutParams(-1,dp(54)));
        download=new Button(this); download.setAllCaps(false); download.setText("Download video"); download.setTextColor(white); download.setBackground(shape(accent,12)); LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(56)); dlp.topMargin=dp(24); panel.addView(download,dlp);
        cancel=new Button(this); cancel.setAllCaps(false); cancel.setText("Cancel"); cancel.setEnabled(false); cancel.setTextColor(white); cancel.setBackground(shape(Color.rgb(64,73,89),12)); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,dp(48)); clp.topMargin=dp(9); panel.addView(cancel,clp);
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); LinearLayout.LayoutParams prp = new LinearLayout.LayoutParams(-1,dp(8)); prp.topMargin=dp(24); panel.addView(progress,prp);
        status=text("Ready. Downloads are saved to Slate's app storage.",13,muted,false); status.setPadding(0,dp(13),0,0); panel.addView(status);
        TextView note=text("Download only content you own or have permission to save. Availability depends on the video's offered formats.",12,muted,false); note.setPadding(0,dp(28),0,0); root.addView(note);
        download.setOnClickListener(v -> start()); cancel.setOnClickListener(v -> { YoutubeDL.getInstance().destroyProcessById(processId); status.setText("Cancel requested…"); });
    }
    void start() {
        if(active) return;
        final String link=url.getText().toString().trim();
        if(!(link.startsWith("https://www.youtube.com/") || link.startsWith("https://youtube.com/") || link.startsWith("https://youtu.be/") || link.startsWith("https://m.youtube.com/"))) { status.setText("Enter a valid YouTube HTTPS URL."); return; }
        final int selected=quality.getSelectedItemPosition();
        active=true; download.setEnabled(false); cancel.setEnabled(true); progress.setProgress(0); status.setText("Preparing download…");
        executor.execute(() -> {
            try {
                YoutubeDL.getInstance().init(getApplication()); FFmpeg.getInstance().init(getApplication());
                File folder = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),"Slate");
                if(!folder.exists() && !folder.mkdirs()) throw new Exception("Could not create storage folder");
                YoutubeDLRequest req=new YoutubeDLRequest(link);
                req.addOption("--no-playlist");
                req.addOption("-o",new File(folder,"%(title).180B [%(id)s].%(ext)s").getAbsolutePath());
                if(selected==5){ req.addOption("-x"); req.addOption("--audio-format","mp3"); }
                else {
                    String limit = selected==1?"1440":selected==2?"1080":selected==3?"720":selected==4?"480":null;
                    String rule = limit==null?"bv*+ba/b":"bv*[height<="+limit+"]+ba/b[height<="+limit+"]";
                    req.addOption("-f",rule); req.addOption("--merge-output-format","mp4");
                }
                YoutubeDL.getInstance().execute(req,(pct,eta)->runOnUiThread(()->{ progress.setProgress(Math.max(0,Math.min(100,(int)pct))); status.setText(String.format(java.util.Locale.US,"Downloading %.1f%% · ETA %ds",pct,eta)); }),processId);
                runOnUiThread(()->{progress.setProgress(100);status.setText("Download complete. Folder: " + folder.getAbsolutePath());});
            } catch(Exception ex) {runOnUiThread(()-> status.setText("Download failed: " + ex.getMessage()));}
            finally { active=false;runOnUiThread(()->{download.setEnabled(true);cancel.setEnabled(false);});}
        });
    }
    TextView text(String s,int size,int color,boolean bold) { TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t; }
    GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override protected void onDestroy(){super.onDestroy();executor.shutdownNow();}
}
