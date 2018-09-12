package hu.itware.kite.service.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.FixedViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;

import hu.itware.kite.service.R;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.fragments.MachineDetailsFragment;
import hu.itware.kite.service.fragments.MachineSearchFragment;
import hu.itware.kite.service.fragments.MachineServiceHistoryFragment;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MachineFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.MetaData;

public class MachineDetailsActivity extends BaseActivity implements MachineFragmentInterface {

    public static final String DETAILS_MODE = "details_mode";
    public static final int MODE_WITH_HISTORY = 0;
    public static final int MODE_DETAILS_ONLY = 1;

    private int mode;

    public static final String GEP_ALVAZSZAM = "gep_alvazszam";

    Gep mGep;

    private ActionBar actionBar;
    private FixedViewPager mViewPager;
    private MachineDetailsPagerAdapter mPagerAdapter;
    private String filter;
    private HashMap<String, String> filters;
    private String selectedPartnerkod;
    private MetaData[] contractTypes;

    public class MachineDetailsPagerAdapter extends FragmentStatePagerAdapter {

        private MachineDetailsFragment machineDetailsFragment;
        private MachineServiceHistoryFragment machineServiceHistoryFragment;
        private MachineSearchFragment machineSearchFragment;

        public MachineDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    switch (mode) {
                        case MODE_WITH_HISTORY:
                            if (machineSearchFragment == null || machineSearchFragment.isDetached()) {
                                machineSearchFragment = new MachineSearchFragment();
                            }
                            return machineSearchFragment;
                        case MODE_DETAILS_ONLY:
                            if (machineDetailsFragment == null || machineDetailsFragment.isDetached()) {
                                machineDetailsFragment = new MachineDetailsFragment();
                            }
                            machineDetailsFragment.setGep(mGep);
                            return machineDetailsFragment;
                        default:
                            if (machineSearchFragment == null || machineSearchFragment.isDetached()) {
                                machineSearchFragment = new MachineSearchFragment();
                            }
                            return machineSearchFragment;
                    }
                case 1:
                    if (machineDetailsFragment == null || machineDetailsFragment.isDetached()) {
                        machineDetailsFragment = new MachineDetailsFragment();
                    }
                    return machineDetailsFragment;
                case 2:
                    if (machineServiceHistoryFragment == null || machineServiceHistoryFragment.isDetached()) {
                        machineServiceHistoryFragment = new MachineServiceHistoryFragment();
                    }
                    return machineServiceHistoryFragment;
                default:
                    return new MachineSearchFragment();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            switch (mode) {
                case MODE_WITH_HISTORY:
                    return 3;
                case MODE_DETAILS_ONLY:
                    return 1;
                default:
                    return 3;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_details);
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
        mPagerAdapter = new MachineDetailsPagerAdapter(getSupportFragmentManager());
        mode = getIntent().getIntExtra(DETAILS_MODE, 0);
        String alvazszam = getIntent().getStringExtra(GEP_ALVAZSZAM);
        if (alvazszam != null && !alvazszam.isEmpty()) {
            Log.e("GEP", "loading gep: " + alvazszam);
            KiteORM kiteORM = new KiteORM(this);
            mGep = kiteORM.loadSingle(Gep.class, GepekTable.COL_ALVAZSZAM + " = ?", new String[]{alvazszam});
        }
        contractTypes = KiteDAO.loadMetaDataByTypeId(this, "SSTIP", "K2");
        setupUIElements();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gep, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void setupUIElements() {
        mViewPager = (FixedViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPagingEnabled(false);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    public Gep getMachine() {
        return mGep;
    }

    @Override
    public void setMachine(Gep machine) {
        this.mGep = machine;
        refreshFragments();
    }

    @Override
    public void nextPage() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        refreshFragments();
    }

    @Override
    public void previousPage() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        refreshFragments();
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public HashMap<String, String> getFilters() {
        return filters;
    }

    @Override
    public String getSelectedPartnerkod() {
        return selectedPartnerkod;
    }

    @Override
    public void setSelectedPartnerkod(String partnerkod) {
        selectedPartnerkod = partnerkod;
    }

    private void refreshFragments() {
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            Fragment fragment = mPagerAdapter.getItem(i);
            if (fragment != null && fragment instanceof IRefreshable) {
                ((IRefreshable)fragment).refresh();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            previousPage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFragments();
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {
        super.onOkClicked(dialog);
    }

    public int getMode() {
        return mode;
    }

    public String getContractType(String contractTypeCode) {
        for (MetaData contractType : contractTypes) {
            if (contractType.text.startsWith(contractTypeCode)) {
                return contractType.text;
            }
        }
        return "";
    }
}
