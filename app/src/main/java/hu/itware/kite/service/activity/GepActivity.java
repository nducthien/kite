package hu.itware.kite.service.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.GepExport;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.utils.Export;
import hu.itware.kite.service.utils.IdGenerator;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.widget.DateTimePickerView;
import hu.itware.kite.service.widget.DoubleCheckBoxView;

public class GepActivity extends BaseActivity implements Validator.ValidationListener {

    public static final int PARTNER_SELECT_REQUEST_CODE = 1;
    public static final int RESULT_MACHINE_CREATED = 1;

    private TextView header;

	@Required(order = 1, messageResId = R.string.error_required_field)
    private EditText name;

	@Required(order = 2, messageResId = R.string.error_required_field)
    private EditText serialNumber;

    private Spinner manufactureYear;
    private EditText manufactureDate;
    private DateTimePickerView operationStartDate;
    private DateTimePickerView warrantyEndDate;
    private DateTimePickerView extendedWarranty;
    private EditText workhourLimit;
    @Required(order = 3, messageResId = R.string.error_required_field)
    private EditText owner;

    private Button newButton;

    private Partner partner;
    private Gep gep;

    private List<String> years;

    KiteORM kiteORM;

    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gep);
        kiteORM = new KiteORM(this);
        String alvazszam = getIntent().getStringExtra("alvazszam");
        if (alvazszam != null && !"".equals(alvazszam)) {
            gep = kiteORM.loadSingle(Gep.class, GepekTable.COL_ALVAZSZAM + " = ?", new String[]{alvazszam});
        }
        String partnerkod = null;
        if (alvazszam != null && !"".equals(alvazszam) && gep != null) {
            partnerkod = gep.partnerkod;
        } else {
            partnerkod = getIntent().getStringExtra("partnerkod");
        }
        if (partnerkod != null && !"".equals(partnerkod) ) {
            partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ? OR " + PartnerekTable.COL_TEMPKOD + " = ?", new String[] {partnerkod, partnerkod});
        }
        setupUIElements();
        setListeners();
        if (gep != null) {
            setTitle(getString(R.string.gep_details_header));
            newButton.setVisibility(View.GONE);
            name.setEnabled(false);
            serialNumber.setEnabled(false);
			manufactureDate.setEnabled(false);
            operationStartDate.setEnabled(false);
            warrantyEndDate.setEnabled(false);
            extendedWarranty.setEnabled(false);
            workhourLimit.setEnabled(false);
			hide(R.id.gep_tv_layout_manufacture_date);
        } else {
			hide(R.id.gep_tv_layout_manufacture_date2);
			hide(R.id.gep_tv_layout_start_of_operation_date);
			hide(R.id.gep_tv_layout_warranty_end_date);
            hide(R.id.gep_tv_layout_extended_warranty);
            hide(R.id.gep_tv_layout_workhour_limit);
		}
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupUIElements() {
        header = (TextView) findViewById(R.id.gep_header);
        name = (EditText) findViewById(R.id.gep_et_name);
        serialNumber = (EditText) findViewById(R.id.gep_et_serial_number);
		manufactureDate = (EditText) findViewById(R.id.gep_et_manufacture_date2);
        operationStartDate = (DateTimePickerView) findViewById(R.id.gep_dp_start_of_operation_date);
        warrantyEndDate = (DateTimePickerView) findViewById(R.id.gep_dp_warranty_end_date);
        manufactureYear = (Spinner) findViewById(R.id.gep_sp_manufacture_date);
        extendedWarranty = (DateTimePickerView) findViewById(R.id.gep_dp_extended_warranty);
        workhourLimit = (EditText) findViewById(R.id.gep_et_workhour_limit);
        years = new ArrayList<String>();
        years.add("");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        for (int i = calendar.get(Calendar.YEAR); i >= 1980; i--) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, years);
        manufactureYear.setAdapter(yearsAdapter);
        newButton = (Button) findViewById(R.id.gep_btn_new);
        owner = (EditText) findViewById(R.id.gep_et_partner);
        if (gep == null) {
            owner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GepActivity.this, PartnerDetailsActivity.class);
                    intent.putExtra(PartnerDetailsActivity.MODE_PARTNER, PartnerDetailsActivity.MODE_PARTNER_SELECT);
                    startActivityForResult(intent, PARTNER_SELECT_REQUEST_CODE);
                }
            });
        }
        refresh();
    }

    private void refresh() {
        if (gep != null) {
            header.setText(gep.tipushosszunev);
            name.setText(gep.tipushosszunev);
            serialNumber.setText(gep.alvazszam);
            operationStartDate.setDate(gep.uzembehelyezesdatum);
            warrantyEndDate.setDate(gep.garanciaervenyesseg);
            manufactureYear.setSelection(years.indexOf(gep.gyartaseve));
			manufactureDate.setText(StringUtils.isEmpty(gep.gyartaseve) ? "-" : gep.gyartaseve);
            extendedWarranty.setDate(gep.kjotallas);
            workhourLimit.setText(gep.uzemorakorlat !=null ? Double.toString(gep.uzemorakorlat) : "");
        }
        if (partner != null) {
            owner.setText(partner.getNev());
        }
    }

    @Override
    protected void setListeners() {
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });
    }

    private void saveData() {
        Gep old = kiteORM.loadSingle(Gep.class, "UPPER(alvazszam) = ?", new String[] { serialNumber.getText().toString().toUpperCase()});
        if (old != null) {
            showErrorDialog(getString(R.string.gep_dialog_title_machine_exists_title), getString(R.string.gep_dialog_title_machine_exists_message, old.alvazszam, old.getPartner().getNev()));
        } else {
            if (gep == null) {
                gep = new Gep();
            }
            gep.setPartner(partner);
            gep.tipushosszunev = name.getText().toString();
            gep.alvazszam = serialNumber.getText().toString().toUpperCase();
            gep.garanciaervenyesseg = warrantyEndDate.getDate();
            gep.uzembehelyezesdatum = operationStartDate.getDate();
            gep.gyartaseve = manufactureYear.getSelectedItem().toString();
			gep.tempgepkod = IdGenerator.generate(this);//"K" + IdGenerator.generate(this, 5);
			gep.partnerkod = partner.partnerkod;
			gep.temppartnerkod = partner.tempkod;
			gep.modified = new Date();
			gep.status = "A";

            kiteORM.insert(gep);
            GepExport export = Export.createGepExport(GepActivity.this, gep);
            kiteORM.insert(export);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("alvazszam", gep.alvazszam);
            setResult(RESULT_MACHINE_CREATED, resultIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PARTNER_SELECT_REQUEST_CODE && resultCode == PartnerDetailsActivity.RESULT_PARTNER_SELECTED) {
            String partnerkod = data.getStringExtra("partnerkod");
            String tempkod = data.getStringExtra("tempkod");
            kiteORM = new KiteORM(this);
            if (partnerkod != null) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod});
            } else if (tempkod != null) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_TEMPKOD + " = ?", new String[]{tempkod});
            }
            refresh();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onValidationSucceeded() {
        saveData();
    }

    @Override
    public void onValidationFailed(View view, Rule<?> rule) {
        if (view instanceof EditText) {
            ((EditText) view).setError(rule.getFailureMessage());
        }
        if (view instanceof DateTimePickerView) {
            ((DateTimePickerView) view).setError(rule.getFailureMessage());
        }
        if (view instanceof DoubleCheckBoxView) {
            ((DoubleCheckBoxView) view).setError(rule.getFailureMessage());
        }
    }
}
