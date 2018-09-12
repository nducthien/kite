package hu.itware.kite.service.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.Validator.ValidationListener;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Regex;
import com.mobsandgeeks.saripaar.annotation.Required;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.ExisingPartnerAdapter;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.utils.AdoszamInputFilter;
import hu.itware.kite.service.utils.AutoCompleteData;
import hu.itware.kite.service.utils.Global;
import hu.itware.kite.service.utils.IdGenerator;
import hu.itware.kite.service.utils.StringUtils;

public class PartnerHozzaadasActivity extends BaseActivity implements ValidationListener {

	private static final String TAG = "PartnerHozzaadas";
	public static final int RESULT_PARTNER_CREATED = 1;

	Button btnSave;
	Partner partner;

	Validator validator;

	ExisingPartnerAdapter partnerAdapter;

	@Required(order = 1, messageResId = R.string.error_required_field)
	AutoCompleteTextView etPartnerName;

	@Required(order = 2, messageResId = R.string.error_required_field)
	EditText etPartnerIrsz;

	@Required(order = 3, messageResId = R.string.error_required_field)
	AutoCompleteTextView etPartnerTelepules;

	@Required(order = 4, messageResId = R.string.error_required_field)
	EditText etPartnerCim;

	@Regex(order = 6, pattern = "[0-9]{8}-[0-9]{1}-[0-9]{2}|^HU[0-9]{8}|[.]{0}|-", messageResId = R.string.error_regexp_tax1)
	EditText etPartnerAdoszam;

	@Regex(order = 6, pattern = "[0-9]{10}|[.]{0}|-", messageResId = R.string.error_regexp_tax2)
	EditText etPartnerAdoazonosito;

	@Regex(order = 5, pattern = "^\\+?(?=(?:\\D*\\d){0,15}\\D*$)[0-9 \\-()\\\\\\/]{0,15}$", messageResId = R.string.error_regexp_phone)
	EditText etPartnerPhone;

	@Regex(order = 6, pattern = "^\\+?(?=(?:\\D*\\d){0,15}\\D*$)[0-9 \\-()\\\\\\/]{0,15}$", messageResId = R.string.error_regexp_fax)
	EditText etPartnerFax;

	@Email(order = 7, messageResId = R.string.error_regexp_email)
	EditText etPartnerEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_partner_hozzaadas);

		validator = new Validator(this);
		validator.setValidationListener(this);

		partner = new Partner();

		setupUIElements();
		setListeners();
		setupClickOutFromEditText(findViewById(R.id.parent));

		validator.put(etPartnerAdoszam, new Rule<EditText>(getString(R.string.error_check_partner_entry_2)) {
			@Override
			public boolean isValid(EditText et) {
				return !TextUtils.isEmpty(etPartnerAdoszam.getText()) || !TextUtils.isEmpty(etPartnerAdoazonosito.getText());
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	protected void setupUIElements() {
		btnSave = (Button) findViewById(R.id.cd_btn_new);
		etPartnerName = (AutoCompleteTextView) findViewById(R.id.cd_et_customer_name);
		partnerAdapter = new ExisingPartnerAdapter(this, null);

		etPartnerName.setAdapter(partnerAdapter);
		partnerAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence str) {
				return getCursor(str);
			}
		});

		etPartnerIrsz = (EditText) findViewById(R.id.cd_et_customer_postal);
		etPartnerTelepules = (AutoCompleteTextView) findViewById(R.id.cd_et_customer_telep);
		ArrayAdapter<String> citiesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, AutoCompleteData.CITIES);
		etPartnerTelepules.setAdapter(citiesAdapter);
		etPartnerCim = (EditText) findViewById(R.id.cd_et_customer_address);
		etPartnerAdoszam = (EditText) findViewById(R.id.cd_et_customer_taxnum);
		etPartnerAdoszam.setFilters(new InputFilter[]{new AdoszamInputFilter()});
		etPartnerAdoazonosito = (EditText) findViewById(R.id.cd_et_customer_taxsign);
		etPartnerPhone = (EditText) findViewById(R.id.cd_et_customer_phone);
		etPartnerFax = (EditText) findViewById(R.id.cd_et_customer_fax);
		etPartnerEmail = (EditText) findViewById(R.id.cd_et_customer_email);
	}

	public Cursor getCursor(CharSequence str) {
		KiteORM helper = new KiteORM(PartnerHozzaadasActivity.this);
		String param = str != null ? StringUtils.clearText(str.toString().toUpperCase()) + "%" : "";
		return helper.query(Partner.class, getSearchQuery(true, PartnerekTable.COL_SEARCHNEV), new String[]{param}, PartnerekTable.COL_NEV1 + " asc");
	}

	@Override
	protected void setListeners() {
		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				validator.validate();
			}
		});
	}

	public static String getSearchQuery(boolean conjuctive, String... columns) {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < columns.length; i++) {
			sb.append(columns[i]).append(" LIKE ?");
			if (i < columns.length - 1) {
				sb.append(conjuctive ? " AND " : " OR ");
			}
		}

		return sb.toString();
	}

	@Override
	public void onValidationFailed(View failedView, Rule<?> failedRule) {
		if (failedView instanceof EditText) {
			((EditText) failedView).setError(failedRule.getFailureMessage());
		}
	}

	private class CheckPartnerTask extends AsyncTask<Partner, Void, Integer>{

		@Override
		protected void onPreExecute() {
			show(R.id.sync_progress_loading);
			btnSave.setEnabled(false);
		}

		@Override
		protected Integer doInBackground(Partner... params) {
			Partner partnerToCheck = params[0];
			KiteORM helper = new KiteORM(PartnerHozzaadasActivity.this);
			Cursor c;

			if (!TextUtils.isEmpty(partnerToCheck.adoszam)) {
				c = helper.query(Partner.class, PartnerekTable.COL_ADOSZAM + "=?", new String[]{partnerToCheck.adoszam}, null);
				if (c != null && c.moveToFirst()) {
					return 1;
				}
			}

			c =  helper.query(Partner.class, PartnerekTable.COL_NEV1 + "=? AND " + PartnerekTable.COL_IRSZ + "=? AND " + PartnerekTable.COL_TELEPULES + "=? AND " + PartnerekTable.COL_CIM + "=?", new String[]{partnerToCheck.nev1, partnerToCheck.irsz,partnerToCheck.telepules,partnerToCheck.cim}, null);
			if (c != null && c.moveToFirst()){
				return 2;
			}

			if (!TextUtils.isEmpty(partnerToCheck.adoazonosito)) {
				c = helper.query(Partner.class, PartnerekTable.COL_ADOAZONOSITO + "=?", new String[]{partnerToCheck.adoazonosito}, null);
				if (c != null && c.moveToFirst()) {
					return 3;
				}
			}

			if (TextUtils.isEmpty(partnerToCheck.adoazonosito) && TextUtils.isEmpty(partnerToCheck.adoszam)) {
				return 4;
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer errorCode) {
			hide(R.id.sync_progress_loading);
			if(btnSave != null){
				btnSave.setEnabled(true);
			}
			switch (errorCode){
				case 0:
					saveToDB();
					break;
				case 1:
					showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_existingpartner_errorcode_1));
					break;
				case 2:
					showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_existingpartner_errorcode_2));
					break;
				case 3:
					showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_existingpartner_errorcode_3));
					break;
				case 4:
					showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_existingpartner_errorcode_4));
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onValidationSucceeded() {
		CheckPartnerTask checkPartnerTask = new CheckPartnerTask();
		checkPartnerTask.execute(populatePartner());
	}

	private boolean checkEntryValues(){
		if (etPartnerIrsz.getText().toString().length() != 4){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_partner_entry_1));
			return false;
		}
		if(etPartnerAdoazonosito.getText().toString().length() == 0 && etPartnerAdoszam.getText().toString().length() == 0){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_partner_entry_2));
			return false;
		}
		if (etPartnerAdoszam.getText().toString().length() > 0 && etPartnerAdoszam.getText().toString().length() != 13){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_partner_entry_3));
			return false;
		}
		if (etPartnerAdoszam.getText().toString().length() > 0 && etPartnerAdoszam.getText().toString().charAt(8) != '-' || etPartnerAdoszam.getText().toString().charAt(10) != '-'){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_partner_entry_4));
			return false;
		}
		if (etPartnerAdoazonosito.getText().toString().length() > 0 && etPartnerAdoazonosito.getText().toString().length() != 10){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_partner_entry_5));
			return false;
		}
		if(etPartnerEmail.getText().toString().length() > 0 && !android.util.Patterns.EMAIL_ADDRESS.matcher(etPartnerEmail.getText().toString()).matches()){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check_relation_email));
			return false;
		}
		if(etPartnerPhone.getText().toString().length() > 0 &&!Patterns.PHONE.matcher(etPartnerPhone.getText().toString()).matches()){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check__phone));
			return false;
		}
		if(etPartnerFax.getText().toString().length() > 0 &&!Patterns.PHONE.matcher(etPartnerFax.getText().toString()).matches()){
			showErrorDialog(getString(R.string.title_activity_partner_hozzaadas), getString(R.string.error_check__phone));
			return false;
		}
		return true;
	}

	private void saveToDB(){
		KiteORM helper = new KiteORM(this);
		partner.beforeSave();
		helper.insert(partner);
		Global.selectedPartner = partner;
		Intent resultIntent = new Intent();
		resultIntent.putExtra("partnerkod", partner.partnerkod);
		resultIntent.putExtra("tempkod", partner.tempkod);
		setResult(RESULT_PARTNER_CREATED, resultIntent);
		finish();
	}

	private Partner populatePartner() {
		String[] nev = StringUtils.splitName(etPartnerName.getText().toString().trim());
		partner.nev1 = nev[0];
		partner.nev2 = nev[1];

		partner.irsz = etPartnerIrsz.getText().toString().trim();
		partner.telepules = etPartnerTelepules.getText().toString().trim();
		partner.cim = etPartnerCim.getText().toString().trim();
		partner.adoszam = etPartnerAdoszam.getText().toString().trim();
		partner.adoazonosito = etPartnerAdoazonosito.getText().toString().trim();
		partner.telefonszam = etPartnerPhone.getText().toString().trim();
		partner.email = etPartnerEmail.getText().toString().trim();

		// ilyenkor null kell!!!!
		partner.partnerkod = null;
		partner.tempkod = "T" + IdGenerator.generate(this, 5); //KITE-870
		partner.fax = etPartnerFax.getText().toString();
		partner.searchNev = StringUtils.collateName(partner.nev1, partner.nev2);
		partner.searchCim = StringUtils.collateName(partner.telepules, partner.cim, partner.irsz);
		List<String> uzletkotok = new ArrayList<String>();
		partner.uzletkotokData = uzletkotok;
		partner.status = "A";
		partner.modified = new Date();
		return partner;
	}



	@Override
	public void onPause() {
		KiteORM.closeCursor(partnerAdapter);
		super.onPause();
	}
}
