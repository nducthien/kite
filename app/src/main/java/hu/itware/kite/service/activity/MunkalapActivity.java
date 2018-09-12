package hu.itware.kite.service.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.FixedViewPager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import hu.itware.kite.service.R;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.fragments.MunkalapListFragment;
import hu.itware.kite.service.fragments.MunkalapPage1Fragment;
import hu.itware.kite.service.fragments.MunkalapPage2Fragment;
import hu.itware.kite.service.fragments.MunkalapPage3Fragment;
import hu.itware.kite.service.fragments.MunkalapPage4Fragment;
import hu.itware.kite.service.fragments.MunkalapSummaryFragment;
import hu.itware.kite.service.fragments.OwnMunkalapListFragment;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.ISaveable;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.Photo;
import hu.itware.kite.service.orm.utils.GSON;
import hu.itware.kite.service.utils.Export;

public class MunkalapActivity extends BaseActivity implements MunkalapFragmentInterface {

    public static final String MUNKALAP_ID = "hu.itware.kite.service.extra.munkalapid";
    public static final String MUNKALAP_MODE = "hu.itware.kite.service.extra.munkalapmode";
    public static final String MUNKALAP_READONLY = "hu.itware.kite.service.extra.munkalapreadonly";

    public static final int MODE_CREATE_NEW = 0;    // Uj munkalap
    public static final int MODE_CONTINUE = 1;      // Nem befejezett munka folytatasa
    public static final int MODE_OPEN = 2;          // Nyitott munkalap folytatasa
    public static final int MODE_COPY = 3;          // Nem befejezett munka uj munkalapon
    public static final int MODE_VIEW = 4;          // Munkalap reszletei
    public static final int MODE_OWN = 5;           // Sajat munkalapok

    public static final int SIGNANTURE_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int PARTNER_SELECT_REQUEST_CODE = 3;
    public static final int VIDEO_REQUEST_CODE = 4;
    public static final int PARTNER_CREATE_REQUEST_CODE = 5;
    public static final int MACHINE_CREATE_REQUEST_CODE = 6;

    private ActionBar actionBar;
    private MunkalapPagerAdapter mSectionsPagerAdapter;
    private FixedViewPager mViewPager;
    private Partner mPartner;
    private Gep mGep;
    private Munkalap mMunkalap = new Munkalap(true);

    private String allapotkod;

    private Photo currentPhoto;

    private int mode;
    private boolean readonly = false;
    private MunkalapSummaryFragment munkalapSummaryFragment;

    private String mMunkalapOldJson = null;

    @Override
    public void nextPage() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    @Override
    public Class getCurrentPage() {
        return mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()).getClass();
    }

    @Override
    public void previousPage() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
    }

    @Override
    public void setPartner(Partner partner) {
        mPartner = partner;
        if (mMunkalap != null) {
            mMunkalap.setPartner(mPartner);
        }
    }

    @Override
    public void setGep(Gep gep, boolean forceNextPage) {
        mGep = gep;
        if (mMunkalap != null) {
            mMunkalap.setGep(mGep);
        }
        if (forceNextPage) {
            mViewPager.setCurrentItem(1, true);
            refreshFragments();
        }
    }

    @Override
    public void deleteMunkalap(Munkalap munkalap) {
        KiteORM kiteORM = new KiteORM(this);
        kiteORM.delete(munkalap);
    }

    @Override
    public void setMunkalap(Munkalap munkalap) {
        mMunkalap = munkalap;
        mPartner = munkalap.getPartner();
        mGep = munkalap.getGep();
        mMunkalapOldJson = GSON.toJson(mMunkalap);
        refreshFragments();
    }

    @Override
    public Partner getPartner() {
        return mPartner;
    }

    @Override
    public Gep getGep() {
        return mGep;
    }

    @Override
    public Munkalap getMunkalap() {
        return mMunkalap;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setCurrentPhoto(Photo photo) {
        currentPhoto = photo;
    }

    @Override
    public void checkAllapotkod() {
        allapotkod = mMunkalap.allapotkod;
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.invalidate();
        refreshFragments();
    }

    public String getMunkalapOldJson() {
        return mMunkalapOldJson;
    }

    public Photo getCurrentPhoto() {
        return currentPhoto;
    }

    public class MunkalapPagerAdapter extends FragmentStatePagerAdapter {

        private MunkalapPage1Fragment munkalapPage1Fragment;
        private MunkalapPage2Fragment munkalapPage2Fragment;
        private MunkalapPage3Fragment munkalapPage3Fragment;
        private MunkalapPage4Fragment munkalapPage4Fragment;

        private MunkalapListFragment munkalapListFragment;

        private OwnMunkalapListFragment ownMunkalapListFragment;

        public MunkalapPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    switch (mode) {
                        case MODE_CREATE_NEW:
                            if (munkalapPage1Fragment == null || munkalapPage1Fragment.isDetached()) {
                                munkalapPage1Fragment = new MunkalapPage1Fragment();
                            }
                            return munkalapPage1Fragment;
                        case MODE_COPY:
                        case MODE_OPEN:
                        case MODE_CONTINUE:
                            if (munkalapListFragment == null || munkalapListFragment.isDetached()) {
                                munkalapListFragment = new MunkalapListFragment();
                            }
                            return munkalapListFragment;
                        case MODE_VIEW:
                            if (munkalapPage3Fragment == null || munkalapPage3Fragment.isDetached()) {
                                munkalapPage3Fragment = new MunkalapPage3Fragment();
                            }
                            return munkalapPage3Fragment;
                        case MODE_OWN:
                            if (ownMunkalapListFragment == null || ownMunkalapListFragment.isDetached()) {
                                ownMunkalapListFragment = new OwnMunkalapListFragment();
                            }
                            return ownMunkalapListFragment;
                        default:
                            if (munkalapPage1Fragment == null || munkalapPage1Fragment.isDetached()) {
                                munkalapPage1Fragment = new MunkalapPage1Fragment();
                            }
                            return munkalapPage1Fragment;
                    }
                case 1:
                    switch (mode) {
                        case MODE_CREATE_NEW:
                            if (munkalapPage2Fragment == null || munkalapPage2Fragment.isDetached()) {
                                munkalapPage2Fragment = new MunkalapPage2Fragment();
                            }
                            return munkalapPage2Fragment;
                        case MODE_OPEN:
                            if ("3".equals(allapotkod)) {
                                if (munkalapPage4Fragment == null || munkalapPage4Fragment.isDetached()) {
                                    munkalapPage4Fragment = new MunkalapPage4Fragment();
                                }
                                return munkalapPage4Fragment;
                            } else {
                                if (munkalapPage3Fragment == null || munkalapPage3Fragment.isDetached()) {
                                    munkalapPage3Fragment = new MunkalapPage3Fragment();
                                }
                                return munkalapPage3Fragment;
                            }
                        case MODE_COPY:
                            if (munkalapPage3Fragment == null || munkalapPage3Fragment.isDetached()) {
                                munkalapPage3Fragment = new MunkalapPage3Fragment();
                            }
                            return munkalapPage3Fragment;
                        case MODE_CONTINUE:
                        case MODE_VIEW:
                            if (munkalapPage4Fragment == null || munkalapPage4Fragment.isDetached()) {
                                munkalapPage4Fragment = new MunkalapPage4Fragment();
                            }
                            return munkalapPage4Fragment;
                        case MODE_OWN:
                            if (munkalapPage3Fragment == null || munkalapPage3Fragment.isDetached()) {
                                munkalapPage3Fragment = new MunkalapPage3Fragment();
                            }
                            return munkalapPage3Fragment;
                        default:
                            if (munkalapPage2Fragment == null || munkalapPage2Fragment.isDetached()) {
                                munkalapPage2Fragment = new MunkalapPage2Fragment();
                            }
                            return munkalapPage2Fragment;
                    }
                case 2:
                    switch (mode) {
                        case MODE_CREATE_NEW:
                            if (munkalapPage3Fragment == null || munkalapPage3Fragment.isDetached()) {
                                munkalapPage3Fragment = new MunkalapPage3Fragment();
                            }
                            return munkalapPage3Fragment;
                        case MODE_COPY:
                        case MODE_OPEN:
                            if (munkalapSummaryFragment == null || munkalapSummaryFragment.isDetached()) {
                                munkalapSummaryFragment = new MunkalapSummaryFragment();
                            }
                            return munkalapSummaryFragment;
                        case MODE_OWN:
                            if (munkalapPage4Fragment == null || munkalapPage4Fragment.isDetached()) {
                                munkalapPage4Fragment = new MunkalapPage4Fragment();
                            }
                            return munkalapPage4Fragment;
                        default:
                            if (munkalapPage3Fragment == null || munkalapPage3Fragment.isDetached()) {
                                munkalapPage3Fragment = new MunkalapPage3Fragment();
                            }
                            return munkalapPage3Fragment;
                    }

                case 3:
                    switch (mode) {
                        case MODE_CREATE_NEW:
                            if (munkalapSummaryFragment == null || munkalapSummaryFragment.isDetached()) {
                                munkalapSummaryFragment = new MunkalapSummaryFragment();
                            }
                            return munkalapSummaryFragment;
                        case MODE_COPY:
                        case MODE_OPEN:
                            if (munkalapPage4Fragment == null || munkalapPage4Fragment.isDetached()) {
                                munkalapPage4Fragment = new MunkalapPage4Fragment();
                            }
                            return munkalapPage4Fragment;
                        default:
                            return munkalapSummaryFragment;
                    }

                case 4:
                    if (munkalapPage4Fragment == null || munkalapPage4Fragment.isDetached()) {
                        munkalapPage4Fragment = new MunkalapPage4Fragment();
                    }
                    return munkalapPage4Fragment;
                default:
                    if (munkalapPage1Fragment == null || munkalapPage1Fragment.isDetached()) {
                        munkalapPage1Fragment = new MunkalapPage1Fragment();
                    }
                    return munkalapPage1Fragment;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if (mode == MODE_OPEN) {
                return POSITION_NONE;
            } else {
                return super.getItemPosition(object);
            }
        }

        @Override
        public int getCount() {
            switch (mode) {
                case MODE_CREATE_NEW:
                    return 5;
                case MODE_COPY:
                    return 4;
                case MODE_OPEN:
                    if ("3".equals(allapotkod)) {
                        return 2;
                    } else {
                        return 4;
                    }
                case MODE_CONTINUE:
                case MODE_VIEW:
                    return 2;
                case MODE_OWN:
                    return 3;
                default:
                    return 5;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.label_munkalap_page1);
                case 1:
                    return getString(R.string.label_munkalap_page2);
                case 2:
                    return getString(R.string.label_munkalap_page3);
                case 3:
                    return getString(R.string.label_munkalap_page3);
                case 4:
                    return getString(R.string.label_munkalap_page4);
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getIntExtra(MUNKALAP_MODE, 0);
        readonly = getIntent().getBooleanExtra(MUNKALAP_READONLY, false);
        if (mode == MODE_VIEW) {
            KiteORM kiteORM = new KiteORM(this);
            mMunkalap = kiteORM.loadSingle(Munkalap.class, "_id = ?", new String[]{getIntent().getStringExtra(MUNKALAP_ID)});
            if (mMunkalap != null) {
                mPartner = mMunkalap.getPartner();
                mGep = mMunkalap.getGep();
            }
            mMunkalapOldJson = GSON.toJson(mMunkalap);
        }
        switch (mode) {
            case MODE_CREATE_NEW:
                setTitle(getString(R.string.main_new_munkalap));
                break;
            case MODE_CONTINUE:
                setTitle(getString(R.string.main_continue_munkalap));
                break;
            case MODE_COPY:
                setTitle(getString(R.string.main_copy_munkalap));
                break;
            case MODE_OPEN:
                refreshTitle();
                break;
            case MODE_VIEW:
                setTitle(getString(R.string.label_details));
                break;
            case MODE_OWN:
                setTitle(getString(R.string.main_own_munkalap));
        }
        setContentView(R.layout.activity_munkalap);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new MunkalapPagerAdapter(getSupportFragmentManager());

        setupUIElements();
    }

    private void refreshTitle() {
        setTitle(getString(R.string.main_open_items, Integer.toString(KiteDAO.getOpenMunkalapCount(this))));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_munkalap, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (mode != MODE_VIEW && mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof ISaveable) {
                    ((ISaveable) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).saveData();
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupUIElements() {
        mViewPager = (FixedViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mSectionsPagerAdapter.getItem(position) instanceof MunkalapListFragment) {
                    ((MunkalapListFragment) mSectionsPagerAdapter.getItem(position)).reload();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void setListeners() {

    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else if (mode == MODE_CREATE_NEW && ("2".equals(mMunkalap.allapotkod) || "3".equals(mMunkalap.allapotkod)) && mViewPager.getCurrentItem() == 4) {
            if (mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof ISaveable) {
                ((ISaveable)mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).saveData();
            }
            finish();
        } else if (mode == MODE_OPEN && ("2".equals(mMunkalap.allapotkod) || "3".equals(mMunkalap.allapotkod)) && mViewPager.getCurrentItem() == 3) {
            if (mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof ISaveable) {
                ((ISaveable)mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).saveData();
            }
            finish();
        } else if (mode == MODE_COPY && ("2".equals(mMunkalap.allapotkod) || "3".equals(mMunkalap.allapotkod)) && mViewPager.getCurrentItem() == 3) {
            if (mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof ISaveable) {
                ((ISaveable)mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).saveData();
            }
            finish();
        } else {
            if ((mode == MODE_CONTINUE && mViewPager.getCurrentItem() == 1) ||
                    (mode == MODE_COPY && mViewPager.getCurrentItem() == 1) ||
                    (mode == MODE_CREATE_NEW && mViewPager.getCurrentItem() == 2) ||
                    (mode == MODE_OPEN && mViewPager.getCurrentItem() == 1)) {
                if (mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof ISaveable) {
                    ((ISaveable)mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).saveData();
                }
            }
            if (mode == MODE_OWN) {
                Export.copyImagesFromTempToUpload(mMunkalap, this);
            }
            previousPage();
            refreshFragments();
        }
    }

    Handler handler = new Handler();

    private void refreshFragments() {
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            final Fragment fragment = mSectionsPagerAdapter.getItem(i);
            if (fragment != null && fragment instanceof IRefreshable) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((IRefreshable)fragment).refresh();
                    }
                }, i * 50);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGNANTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if(munkalapSummaryFragment != null){
                munkalapSummaryFragment.setSignature(data);
            }
        } else if ((requestCode == PARTNER_SELECT_REQUEST_CODE && resultCode == PartnerDetailsActivity.RESULT_PARTNER_SELECTED) ||
                (requestCode == PARTNER_CREATE_REQUEST_CODE && resultCode == PartnerHozzaadasActivity.RESULT_PARTNER_CREATED)) {
            String partnerkod = data.getStringExtra("partnerkod");
            String tempkod = data.getStringExtra("tempkod");
            KiteORM kiteORM = new KiteORM(this);
            Partner partner = null;
            if (partnerkod != null) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod});
            } else if (tempkod != null) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_TEMPKOD + " = ?", new String[]{tempkod});
            }
            setPartner(partner);
            refreshFragments();
        } else if (requestCode == MACHINE_CREATE_REQUEST_CODE && resultCode == GepActivity.RESULT_MACHINE_CREATED) {
            KiteORM kiteORM = new KiteORM(this);
            Gep gep = kiteORM.loadSingle(Gep.class, GepekTable.COL_ALVAZSZAM + " = ?", new String[]{data.getStringExtra("alvazszam")});
            if (gep != null) {
                mGep = gep;
                mPartner = gep.getPartner();
                if (mMunkalap != null) {
                    mMunkalap.setGep(mGep);
                    mMunkalap.setPartner(mPartner);
                }
                if (mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof MunkalapPage1Fragment) {
                    ((MunkalapPage1Fragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).machineCreated();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {
        Log.i(TAG, "onOkClicked=" + dialog);
        errorDialog = null;
        super.onOkClicked(dialog);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem()) instanceof ISaveable) {
            ((ISaveable) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).saveData();
        }
        if(mMunkalap != null) {
            outState.putLong(MUNKALAP_ID, mMunkalap._id);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        KiteORM kiteORM = new KiteORM(this);
        mMunkalap = kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_BASE_ID + " = ?", new String[]{Long.toString(savedInstanceState.getLong(MUNKALAP_ID))});
        if (mMunkalap != null) {
            mGep = mMunkalap.getGep();
            mPartner = mMunkalap.getPartner();
            mMunkalapOldJson = GSON.toJson(mMunkalap);
        }
        refreshFragments();
    }

    @Override
    public void reload() {
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            Fragment fragment = mSectionsPagerAdapter.getItem(i);
            if (fragment != null && fragment instanceof MunkalapListFragment) {
                ((MunkalapListFragment)fragment).reload();
                break;
            }
        }
        refreshTitle();
    }

    public boolean isReadonly() {
        return readonly;
    }
}
