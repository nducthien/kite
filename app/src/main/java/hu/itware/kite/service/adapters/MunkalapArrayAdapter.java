package hu.itware.kite.service.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.fragments.IDialogResult;
import hu.itware.kite.service.interfaces.MachineFragmentInterface;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.widget.DateTimePickerView;

/**
 * Created by szeibert on 2015.09.28..
 */
public class MunkalapArrayAdapter extends ArrayAdapter<Munkalap> {
    public MunkalapArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public MunkalapArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public MunkalapArrayAdapter(Context context, int resource, Munkalap[] objects) {
        super(context, resource, objects);
    }

    public MunkalapArrayAdapter(Context context, int resource, int textViewResourceId, Munkalap[] objects) {
        super(context, resource, textViewResourceId, objects);
    }
    public MunkalapArrayAdapter(Context context, int resource, List<Munkalap> objects) {
        super(context, resource, objects);
    }

    public MunkalapArrayAdapter(Context context, int resource, List<Munkalap> objects, ProgressBar pb) {
        super(context, resource, objects);
        this.pb = pb;
    }

    public MunkalapArrayAdapter(Context context, int resource, int textViewResourceId, List<Munkalap> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public AsyncTask<Munkalap, Void, Boolean> checkMunkalapTask;
    private ProgressBar pb;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null ) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_munkalap, parent, false);

            holder = new ViewHolder();

            holder.container = (LinearLayout) convertView.findViewById(R.id.munkalap_ll_list_item);
            holder.header = (TextView) convertView.findViewById(R.id.munkalap_tv_header);
            holder.workDate = (DateTimePickerView) convertView.findViewById(R.id.munkalap_dp_work_date);
            holder.workHour = (EditText) convertView.findViewById(R.id.munkalap_et_work_hour);
            holder.partnerName = (EditText) convertView.findViewById(R.id.munkalap_et_partner_name);
            holder.partnerAddress = (EditText) convertView.findViewById(R.id.munkalap_et_partner_address);
            holder.machineName = (EditText) convertView.findViewById(R.id.munkalap_et_machine_name);
            holder.machineSerial = (EditText) convertView.findViewById(R.id.munkalap_et_machine_serial);
            holder.taskDescription = (EditText) convertView.findViewById(R.id.munkalap_et_task_description);
            holder.worker = (EditText) convertView.findViewById(R.id.munkalap_et_machine_worker);
            holder.status = (EditText) convertView.findViewById(R.id.munkalap_et_status);
            holder.continueButton = (Button) convertView.findViewById(R.id.munkalap_btn_continue);
            holder.deleteButton = (Button) convertView.findViewById(R.id.munkalap_btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Munkalap munkalap = getItem(position);
        if (("0".equals(munkalap.allapotkod) || "1".equals(munkalap.allapotkod)) && getContext() instanceof MunkalapFragmentInterface) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
        if ("1".equals(munkalap.allapotkod)) {
            holder.container.setBackgroundColor(getContext().getResources().getColor(R.color.border_yellow));
        } else if ("2".equals(munkalap.allapotkod) || "3".equals(munkalap.allapotkod)) {
            holder.container.setBackgroundColor(getContext().getResources().getColor(R.color.border_green));
        } else {
            holder.container.setBackgroundColor(Color.WHITE);
        }
        holder.header.setText(getContext().getString(R.string.label_munkalap_header, munkalap.munkalapsorszam != null ? munkalap.munkalapsorszam : munkalap.tempkod));
        holder.workDate.setDate(munkalap.munkavegzesdatum);
        holder.workHour.setText(munkalap.munkaora == null ? "" : "" + munkalap.munkaora);
        holder.partnerName.setText(munkalap.getPartner().getNev());
        holder.partnerAddress.setText(munkalap.getPartner().getAddress());
        holder.machineName.setText(munkalap.getGep().tipushosszunev);
        holder.machineSerial.setText(munkalap.getGep().alvazszam);
        holder.taskDescription.setText(munkalap.tevekenyseg1);
        holder.worker.setText(munkalap.szervizes);
        if (getContext() instanceof MachineFragmentInterface || (getContext() instanceof  MunkalapFragmentInterface && ((MunkalapFragmentInterface)getContext()).getMode() == MunkalapActivity.MODE_OWN)) {
            holder.continueButton.setText(getContext().getString(R.string.munkalap_btn_details));
        }
        if ("1".equals(munkalap.allapotkod)) {
            holder.status.setText(getContext().getString(R.string.munkalap_status_1));
        }
        if ("2".equals(munkalap.allapotkod)) {
            holder.status.setText(getContext().getString(R.string.munkalap_status_2));
        }
        if ("3".equals(munkalap.allapotkod)) {
            holder.status.setText(getContext().getString(R.string.munkalap_status_3));
        }
        if ("4".equals(munkalap.allapotkod)) {
            holder.status.setText(getContext().getString(R.string.munkalap_status_4));
        }
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MunkalapFragmentInterface munkalapFragmentInterface = (MunkalapFragmentInterface) getContext();
                final BaseActivity baseActivity = (BaseActivity) getContext();
                final Munkalap selectedMunkalap = getItem(position);
                baseActivity.showQuestionDialog(baseActivity.getString(R.string.munkalap_delete_title), baseActivity.getString(R.string.munkalap_delete_message), new IDialogResult() {
                    @Override
                    public void onOkClicked(DialogFragment dialog) {
                        munkalapFragmentInterface.deleteMunkalap(selectedMunkalap);
                        munkalapFragmentInterface.reload();
                    }

                    @Override
                    public void onCancelClicked(DialogFragment dialog) {
                        // do nothing
                    }
                });
            }
        });
        holder.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() instanceof MunkalapFragmentInterface) {
                    final MunkalapFragmentInterface munkalapFragmentInterface = (MunkalapFragmentInterface) getContext();
                    Munkalap selectedMunkalap = getItem(position);
                    switch (munkalapFragmentInterface.getMode()) {
                        case MunkalapActivity.MODE_OPEN:
                            if ("3".equals(selectedMunkalap.allapotkod)) {
                                if (selectedMunkalap.canContinue()) {
                                    holder.continueButton.setOnClickListener(null);
                                    munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                                    munkalapFragmentInterface.checkAllapotkod();
                                    munkalapFragmentInterface.nextPage();
                                } else {
                                    ((BaseActivity) getContext()).showErrorDialog(getContext().getString(R.string.error_dialog_cannot_continue_title), getContext().getString(R.string.error_dialog_cannot_continue_message, Integer.toString(Munkalap.getDaysToContinue())));
                                }
                            } else {
                                holder.continueButton.setOnClickListener(null);
                                munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                                munkalapFragmentInterface.checkAllapotkod();
                                munkalapFragmentInterface.nextPage();
                            }
                            break;
                        case MunkalapActivity.MODE_CONTINUE:
                            if (selectedMunkalap.canContinue()) {
                                holder.continueButton.setOnClickListener(null);
                                selectedMunkalap.allapotkod = "3";
                                munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                                munkalapFragmentInterface.nextPage();
                            } else {
                                ((BaseActivity) getContext()).showErrorDialog(getContext().getString(R.string.error_dialog_cannot_continue_title), getContext().getString(R.string.error_dialog_cannot_continue_message, Integer.toString(Munkalap.getDaysToContinue())));
                            }
                            break;
                        case MunkalapActivity.MODE_COPY:
                            Log.e("ADAPTER","" + pb);
                            if(checkMunkalapTask != null){
                                checkMunkalapTask.cancel(true);
                            }
                            checkMunkalapTask = new AsyncTask<Munkalap, Void, Boolean>() {

                                Munkalap selectedMunkalap;

                                @Override
                                protected void onPreExecute() {
                                    if(pb != null){
                                        pb.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                protected Boolean doInBackground(Munkalap... params) {
                                    selectedMunkalap = params[0];
                                    Log.e("ADAPTER", "selectedMunkalap= " + selectedMunkalap);
                                    return selectedMunkalap.findOpenMunkalapInRelatedMunkalapArray();
                                }

                                @Override
                                protected void onPostExecute(Boolean result) {
                                    hidePb();
                                    Log.e("ADAPTER", "findOpenMunkalapInMunkalapArray= " + result);
                                    if (result) {
                                        ((BaseActivity) getContext()).showErrorDialog(getContext().getString(R.string.warning_dialog_already_open_munkalap_title), getContext().getString(R.string.info_dialog_already_open_munkalap_message));
                                    } else {
                                        holder.continueButton.setOnClickListener(null);
                                        munkalapFragmentInterface.setMunkalap(selectedMunkalap.copy());
                                        munkalapFragmentInterface.nextPage();
                                    }
                                }

                                private void hidePb(){
                                    if(pb != null){
                                        pb.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                protected void onCancelled() {
                                    hidePb();
                                }
                            }.execute(selectedMunkalap);
                            break;
                        case MunkalapActivity.MODE_OWN:
                            holder.continueButton.setOnClickListener(null);
                            munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                            munkalapFragmentInterface.nextPage();
                            break;
                        default:
                            break;
                    }
                } else {
                    Intent intent = new Intent(getContext(), MunkalapActivity.class);
                    intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_VIEW);
                    intent.putExtra(MunkalapActivity.MUNKALAP_ID, Long.toString(getItem(position)._id));
                    getContext().startActivity(intent);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        LinearLayout container;
        TextView header;
        EditText workHour, partnerName, partnerAddress, machineName, machineSerial, taskDescription, worker, status;
        DateTimePickerView workDate;
        Button continueButton;
        Button deleteButton;
    }
}
