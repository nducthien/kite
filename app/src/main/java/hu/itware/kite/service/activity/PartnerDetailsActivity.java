package hu.itware.kite.service.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.FixedViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;

import hu.itware.kite.service.R;
import hu.itware.kite.service.fragments.PartnerDetailsFragment;
import hu.itware.kite.service.fragments.PartnerMachinesFragment;
import hu.itware.kite.service.fragments.PartnerSearchFragment;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MachineFragmentInterface;
import hu.itware.kite.service.interfaces.PartnerFragmentInterface;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Partner;

public class PartnerDetailsActivity extends BaseActivity implements PartnerFragmentInterface, MachineFragmentInterface, ActionBar.TabListener {

    public static final String MODE_PARTNER = "hu.itware.kite.service.mode.partner";

    public static final int MODE_PARTNER_INFO = 0;
    public static final int MODE_PARTNER_SELECT = 1;

    public static final int PARTNER_CREATE_REQUEST_CODE = 1;

    public static final int RESULT_PARTNER_SELECTED = 1;

    private Partner mPartner;
    private Gep mGep;

    private ActionBar actionBar;
    private FixedViewPager mViewPager;
    private PartnerDetailsPagerAdapter mPagerAdapter;

    private int mMode;
    private String filter;
    private HashMap<String, String> filters;

    public class PartnerDetailsPagerAdapter extends FragmentPagerAdapter {

        private PartnerDetailsFragment partnerDetailsFragment;
        private PartnerMachinesFragment partnerMachinesFragment;

        public PartnerDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PartnerSearchFragment();
                case 1:
                    if (partnerDetailsFragment == null || partnerDetailsFragment.isDetached()) {
                        partnerDetailsFragment = new PartnerDetailsFragment();
                    }
                    return partnerDetailsFragment;
                case 2:
                    if (partnerMachinesFragment == null || partnerMachinesFragment.isDetached()) {
                        partnerMachinesFragment = new PartnerMachinesFragment();
                    }
                    return partnerMachinesFragment;
                default:
                    return new PartnerSearchFragment();
            }
        }

        @Override
        public int getCount() {
            switch (mMode) {
                case MODE_PARTNER_INFO:
                    return 3;
                case MODE_PARTNER_SELECT:
                    return 1;
                default:
                    return 3;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Keresés";
                case 1:
                    return "Részletek";
                case 2:
                    return "Gépek";
                default:
                    return "";
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_details);
        mMode = getIntent().getIntExtra(MODE_PARTNER, 0);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mPagerAdapter = new PartnerDetailsPagerAdapter(getSupportFragmentManager());
        setupUIElements();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_partner_details, menu);
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
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setupUIElements() {
        mViewPager = (FixedViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPagingEnabled(false);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    public Partner getPartner() {
        return mPartner;
    }

    @Override
    public void setPartner(Partner partner) {
        mPartner = partner;
        refreshFragments();
        if (mMode == MODE_PARTNER_SELECT) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("partnerkod", mPartner.partnerkod);
            resultIntent.putExtra("tempkod", mPartner.tempkod);
            setResult(RESULT_PARTNER_SELECTED, resultIntent);
            finish();
        } else {
            nextPage();
        }
    }

    @Override
    public Gep getMachine() {
        return mGep;
    }

    @Override
    public void setMachine(Gep machine) {
        mGep = machine;
    }

    @Override
    public void nextPage() {
        if (mViewPager.getCurrentItem() == 0) {
            for (int i = 1; i < mPagerAdapter.getCount(); i++) {
                actionBar.addTab(actionBar.newTab().setText(mPagerAdapter.getPageTitle(i)).setTabListener(PartnerDetailsActivity.this));
            }
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        }
        refreshFragments();
    }

    @Override
    public void previousPage() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        refreshFragments();
    }

    @Override
    public int getMode() {
        return mMode;
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
        return null;
    }

    @Override
    public void setSelectedPartnerkod(String partnerkod) {

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
            actionBar.removeAllTabs();
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition() + 1);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PARTNER_CREATE_REQUEST_CODE && resultCode == PartnerHozzaadasActivity.RESULT_PARTNER_CREATED) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("partnerkod", data.getStringExtra("partnerkod"));
            resultIntent.putExtra("tempkod", data.getStringExtra("tempkod"));
            setResult(RESULT_PARTNER_SELECTED, resultIntent);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
