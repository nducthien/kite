package hu.itware.kite.service.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.FolderListAdapter;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.KonfigTable;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.ImageThumbnailLoader;
import hu.itware.kite.service.utils.MetaDataTypes;
import hu.itware.kite.service.widget.ThumbnailView;

public class InformaciosAnyagokActivity extends Activity {

    private Stack<String> parentDirectories;        // contains the current directory tree
    private String currentPath;                     // stores the path of the current directory

    private ViewFlipper vfFolderListViews;          // viewFlipper, provides the animation at folder change
    private TextView tvPrevFolder;                  // button to move up pne directory
    private ListView folderListView1;               // listView for listing folder names of the current folder
    private ListView folderListView2;               // listView for listing folder names of the current folder (needed for viewFlipper animation)
    private GridLayout glFileThumbsView;            // gridLayout for the thumbnails

    private String fileFilterCriteria;              // its a criteria to filter the files in the current folder
    String studyMaterialsDir;
    ImageThumbnailLoader imageThumbnailLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_informacios_anyagok);
        parentDirectories = new Stack<String>();
        KiteORM orm = new KiteORM(this);
        Konfig konfig = orm.loadSingle(Konfig.class, KonfigTable.COL_NAME + " =?", new String[]{MetaDataTypes.OKTATASI_ANYAGOK_HELYE});
        studyMaterialsDir = null;
        if(konfig != null){
            studyMaterialsDir = konfig.value;
        }
        if(studyMaterialsDir == null){
            studyMaterialsDir = Settings.STUDY_MATERIALS_ROOT_DIRECTORY;
        }
        //File rootDir = Environment.getExternalStoragePublicDirectory((studyMaterialsDir.startsWith("/")?studyMaterialsDir:"/"+studyMaterialsDir));


        //studyMaterialsDir = "/storage/sdcard1" + (studyMaterialsDir.startsWith("/")?studyMaterialsDir:"/"+studyMaterialsDir);
        //studyMaterialsDir = rootDir.getAbsolutePath();
        studyMaterialsDir = getSDCardDirectory(studyMaterialsDir);

        parentDirectories.push(studyMaterialsDir);

        setupUIElements();
        imageThumbnailLoader = new ImageThumbnailLoader(this, BitmapFactory.decodeResource(getResources(), R.drawable.kite_icon));

        glFileThumbsView = (GridLayout)findViewById(R.id.study_material__thumbs);
        fileFilterCriteria = "";

        tvPrevFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!parentDirectories.lastElement().equals(studyMaterialsDir)) {
                    parentDirectories.pop();
                    tvPrevFolder.setText(parentDirectories.peek());
                    vfFolderListViews.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left));
                    vfFolderListViews.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right));
                    setCurrentPath();
                }
            }
        });

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String folderName = (String) view.getTag();
                parentDirectories.push(folderName);
                tvPrevFolder.setText(folderName);
                vfFolderListViews.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right));
                vfFolderListViews.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
                setCurrentPath();
            }
        };

        folderListView1.setOnItemClickListener(onItemClickListener);
        folderListView2.setOnItemClickListener(onItemClickListener);

        setCurrentPath();
    }


    public String getSDCardDirectory(String path) {
        String sdCardPath= getRemovableStoragePath() + path;
        if (isExists(sdCardPath)) {
            return sdCardPath;
        }

        sdCardPath = "/storage/sdcard1" + path;
        if (isExists(sdCardPath)) {
            return sdCardPath;
        }

        sdCardPath = "/storage/sdcard0" + path;
        if (isExists(sdCardPath)) {
            return sdCardPath;
        }

        sdCardPath = "/storage/emulated/0" + path;
        if (isExists(sdCardPath)) {
            return sdCardPath;
        }
        return null;
    }


    private boolean isExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }


    public String getRemovableStoragePath() {
        String removableStoragePath = null;
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
                removableStoragePath = file.getAbsolutePath();
        }
        return removableStoragePath;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_informacios_anyagok, menu);
        final MenuItem filterFieldLabel = menu.findItem(R.id.study_materials_filter_label);
        filterFieldLabel.setEnabled(false);

        MenuItem fileFilterItem = menu.findItem(R.id.study_materials_filter);
        SearchView searchView = (SearchView)fileFilterItem.getActionView();
        int imageId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView v = (ImageView) searchView.findViewById(imageId);
        v.setImageResource(R.mipmap.ic_action_search);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        final TextView fileFilterField = (TextView)searchView.findViewById(id);
        fileFilterField.setHint(getResources().getString(R.string.study_materials_filter_label));
        fileFilterField.setTextColor(getResources().getColor(R.color.primary_green));
        fileFilterField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                fileFilterCriteria = s.toString();
                loadFilesAndDirectories(true);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                filterFieldLabel.setVisible(true);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null) {
                    filterFieldLabel.setVisible(false);
                    fileFilterField.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the UI elements
     */
    private void setupUIElements() {
        vfFolderListViews = (ViewFlipper)findViewById(R.id.study_materials_folder_list_view_flipper);
        tvPrevFolder = (TextView)findViewById(R.id.study_materials_prev_folder);
        folderListView1 = (ListView)findViewById(R.id.study_materials_folder_list1);
        folderListView2 = (ListView)findViewById(R.id.study_materials_folder_list2);
    }

    /**
     * Sets the path of the current directory from the directory tree stack
     */
    private void setCurrentPath() {
        StringBuilder sb = new StringBuilder();
        for (String key: parentDirectories) {
            sb.append(parentDirectories.size() > 1 ? key + "/" : key + "");
        }
        currentPath = sb.toString();

        loadFilesAndDirectories(false);
    }

    /**
     * Loads the files and folders of the current directory
     */
    private void loadFilesAndDirectories(boolean onlyFiltering) {
        Log.e("current_path", currentPath);
        List<String> directoriesInDirectory = new ArrayList<String>();
        List<String> filesInDirectory = new ArrayList<String>();
        File dir = new File(currentPath);
        if (!dir.exists()) {
            Log.d("dir_exists", "no");
            return;
        }

        for (File f: dir.listFiles()) {
            if (f.isDirectory() && !onlyFiltering) {
                directoriesInDirectory.add(f.getName());
            } else if (!f.isDirectory() && f.getName().toLowerCase().contains(fileFilterCriteria.toLowerCase())) {
                filesInDirectory.add(f.getName());
            }
        }

        Collections.sort(directoriesInDirectory);
        Collections.sort(filesInDirectory);

        if (vfFolderListViews.getDisplayedChild() == 0 && !onlyFiltering) {
            folderListView2.setAdapter(new FolderListAdapter(this, directoriesInDirectory));
            vfFolderListViews.setDisplayedChild(1);
        } else if (!onlyFiltering) {
            folderListView1.setAdapter(new FolderListAdapter(this, directoriesInDirectory));
            vfFolderListViews.setDisplayedChild(0);
        }

        setThumbs(filesInDirectory);

        tvPrevFolder.setVisibility(parentDirectories.size() > 1 ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Generates the file icons related to the list of files
     * @param fileNames the list of the file names
     */
    private void setThumbs(List<String> fileNames) {
        glFileThumbsView.removeAllViews();

        for (String fileName : fileNames) {
            final File f = new File(currentPath + File.separator + fileName);

            if (f.canRead()) {
                final String mimeType;
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String[] fileNameSplits = f.getName().split("\\.");
                mimeType = mimeTypeMap.getMimeTypeFromExtension(fileNameSplits[fileNameSplits.length-1]);

                Log.d("file_ext",fileNameSplits[fileNameSplits.length-1]);

                int fileIconResId = 0;
                if (mimeType != null && "application/pdf".equals(mimeType)) {
                    fileIconResId = R.mipmap.ic_pdf_icon;
                } else if (mimeType != null && mimeType.matches("image(.+)")) {
                    fileIconResId = R.mipmap.ic_image_icon;
                } else if (mimeType != null && mimeType.matches("video(.+)")) {
                    fileIconResId = R.mipmap.ic_movie_icon;
                }

                View thumbNail = new ThumbnailView(getApplicationContext(), fileIconResId,f.getName());

                glFileThumbsView.addView(thumbNail);

                if (mimeType != null && mimeType.matches("image(.+)")){
                    imageThumbnailLoader.loadBitmapBackground(f, ((ThumbnailView) thumbNail).background);
                } else if (mimeType != null && mimeType.matches("video(.+)")) {
                    imageThumbnailLoader.loadBitmapBackground(f, ((ThumbnailView) thumbNail).background);
                }

                ((ThumbnailView) thumbNail).background.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Uri uri = Uri.fromFile(f);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, mimeType != null ? mimeType : "*/*");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.d("application", "not found! " + e.getMessage());
                        }
                    }
                });
            }
            glFileThumbsView.getChildCount();
        }
    }
}
