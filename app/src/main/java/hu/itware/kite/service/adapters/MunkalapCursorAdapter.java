package hu.itware.kite.service.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.fragments.IDialogResult;
import hu.itware.kite.service.interfaces.MachineFragmentInterface;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.widget.DateTimePickerView;

/**
 * Created by szeibert on 2017.06.13..
 */

public class MunkalapCursorAdapter extends KiteCursorAdapter<Munkalap> {

    public AsyncTask<Munkalap, Void, Boolean> checkMunkalapTask;
    private ProgressBar pb;


    public MunkalapCursorAdapter(Context context, Cursor c) {
        super(Munkalap.class, context, c, R.layout.list_item_munkalap);
    }

    @Override
    public void createView(View view, final Context context, final Munkalap munkalap) {
        ViewHolder holder;

        holder = new ViewHolder();

        holder.container = (LinearLayout) view.findViewById(R.id.munkalap_ll_list_item);
        holder.header = (TextView) view.findViewById(R.id.munkalap_tv_header);
        holder.workDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_work_date);
        holder.workHour = (EditText) view.findViewById(R.id.munkalap_et_work_hour);
        holder.partnerName = (EditText) view.findViewById(R.id.munkalap_et_partner_name);
        holder.partnerAddress = (EditText) view.findViewById(R.id.munkalap_et_partner_address);
        holder.machineName = (EditText) view.findViewById(R.id.munkalap_et_machine_name);
        holder.machineSerial = (EditText) view.findViewById(R.id.munkalap_et_machine_serial);
        holder.taskDescription = (EditText) view.findViewById(R.id.munkalap_et_task_description);
        holder.worker = (EditText) view.findViewById(R.id.munkalap_et_machine_worker);
        holder.status = (EditText) view.findViewById(R.id.munkalap_et_status);
        holder.continueButton = (Button) view.findViewById(R.id.munkalap_btn_continue);
        holder.deleteButton = (Button) view.findViewById(R.id.munkalap_btn_delete);

        view.setTag(holder);

        holder.deleteButton.setVisibility(View.GONE);

        if ("1".equals(munkalap.allapotkod)) {
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.border_yellow));
        } else if ("2".equals(munkalap.allapotkod) || "3".equals(munkalap.allapotkod)) {
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.border_green));
        } else {
            holder.container.setBackgroundColor(Color.WHITE);
        }
        holder.header.setText(context.getString(R.string.label_munkalap_header, munkalap.munkalapsorszam != null ? munkalap.munkalapsorszam : munkalap.tempkod));
        holder.workDate.setDate(munkalap.munkavegzesdatum);
        holder.workHour.setText(munkalap.munkaora == null ? "" : "" + munkalap.munkaora);
        holder.partnerName.setText(munkalap.getPartner().getNev());
        holder.partnerAddress.setText(munkalap.getPartner().getAddress());
        holder.machineName.setText(munkalap.getGep().tipushosszunev);
        holder.machineSerial.setText(munkalap.getGep().alvazszam);
        holder.taskDescription.setText(munkalap.tevekenyseg1);
        holder.worker.setText(munkalap.szervizes);
        if (context instanceof MachineFragmentInterface || (context instanceof MunkalapFragmentInterface && ((MunkalapFragmentInterface)context).getMode() == MunkalapActivity.MODE_OWN)) {
            holder.continueButton.setText(context.getString(R.string.munkalap_btn_details));
        }
        if ("1".equals(munkalap.allapotkod)) {
            holder.status.setText(context.getString(R.string.munkalap_status_1));
        }
        if ("2".equals(munkalap.allapotkod)) {
            holder.status.setText(context.getString(R.string.munkalap_status_2));
        }
        if ("3".equals(munkalap.allapotkod)) {
            holder.status.setText(context.getString(R.string.munkalap_status_3));
        }
        if ("4".equals(munkalap.allapotkod)) {
            holder.status.setText(context.getString(R.string.munkalap_status_4));
        }
        holder.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof MunkalapFragmentInterface) {
                    final MunkalapFragmentInterface munkalapFragmentInterface = (MunkalapFragmentInterface) context;
                    Munkalap selectedMunkalap = munkalap;
                    switch (munkalapFragmentInterface.getMode()) {
                        case MunkalapActivity.MODE_OPEN:
                            if ("3".equals(selectedMunkalap.allapotkod)) {
                                if (selectedMunkalap.canContinue()) {
                                    munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                                    munkalapFragmentInterface.checkAllapotkod();
                                    munkalapFragmentInterface.nextPage();
                                } else {
                                    ((BaseActivity) context).showErrorDialog(context.getString(R.string.error_dialog_cannot_continue_title), context.getString(R.string.error_dialog_cannot_continue_message, Integer.toString(Munkalap.getDaysToContinue())));
                                }
                            } else {
                                munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                                munkalapFragmentInterface.checkAllapotkod();
                                munkalapFragmentInterface.nextPage();
                            }
                            break;
                        case MunkalapActivity.MODE_CONTINUE:
                            if (selectedMunkalap.canContinue()) {
                                selectedMunkalap.allapotkod = "3";
                                munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                                munkalapFragmentInterface.nextPage();
                            } else {
                                ((BaseActivity) context).showErrorDialog(context.getString(R.string.error_dialog_cannot_continue_title), context.getString(R.string.error_dialog_cannot_continue_message, Integer.toString(Munkalap.getDaysToContinue())));
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
                                        ((BaseActivity) context).showErrorDialog(context.getString(R.string.warning_dialog_already_open_munkalap_title), context.getString(R.string.info_dialog_already_open_munkalap_message));
                                    } else {
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
                            munkalapFragmentInterface.setMunkalap(selectedMunkalap);
                            munkalapFragmentInterface.nextPage();
                            break;
                        default:
                            break;
                    }
                } else {
                    Intent intent = new Intent(context, MunkalapActivity.class);
                    intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_VIEW);
                    intent.putExtra(MunkalapActivity.MUNKALAP_ID, Long.toString(munkalap._id));
                    context.startActivity(intent);
                }
            }
        });
    }

    static class ViewHolder {
        LinearLayout container;
        TextView header;
        EditText workHour, partnerName, partnerAddress, machineName, machineSerial, taskDescription, worker, status;
        DateTimePickerView workDate;
        Button continueButton, deleteButton;
    }
}
