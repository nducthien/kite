package hu.itware.kite.service.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.model.MetaData;

/**
 * Created by szeibert on 2017.05.31..
 *
 * Validator to determine whether PIP code is required or not based on the current values of various fields.
 * Relations between fields can be defined in the MetaData table using PIPR as type.
 * A relation is a set of fields and values for example if the value of GETI is A001 and the value of GETI2 is B001 then PIP code is required.
 *
 */
public class PipValidator {

    private static final String TAG = "PIPVALIDATOR";

    List<ElementRelation> elementRelations;

    public PipValidator(Context context) {
        MetaData[] metaDataList = KiteDAO.loadMetaData(context, "PIPR");
        elementRelations = new ArrayList<ElementRelation>();
        for (MetaData metaData : metaDataList) {
            try {
                JSONArray fields = new JSONArray(metaData.text);
                List<ElementValue> values = new ArrayList<ElementValue>();
                for (int i = 0; i < fields.length(); i++) {
                    values.add(new ElementValue(fields.getJSONObject(i).getString("field"), fields.getJSONObject(i).getString("value")));
                }
                elementRelations.add(new ElementRelation(values, Boolean.valueOf(metaData.value)));
            } catch (JSONException e) {
                Log.e(TAG, "Could not parse PIP relation field JSON: " + metaData.text);
            }
        }
        // Test data
        /*elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI", "A001"), new ElementValue("GETI2", "B001")}), true));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI", "A001"), new ElementValue("GETI2", "B002")}), true));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI", "A001"), new ElementValue("GETI2", "B003")}), false));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI2", "B001"), new ElementValue("TEV1", "C001")}), true));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI2", "B001"), new ElementValue("TEV1", "C002")}), false));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI2", "B001"), new ElementValue("TEV1", "C005")}), false));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("GETI2", "B002"), new ElementValue("TEV1", "C003")}), true));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("TEV1", "C001"), new ElementValue("MHB", "D001")}), false));
        elementRelations.add(new ElementRelation(Arrays.asList(new ElementValue[] {new ElementValue("TEV1", "C001"), new ElementValue("MHB", "D002")}), true));*/
    }

    public boolean isRequired(List<ElementValue> values) {
        HashMap<String, String> valuesMap = new HashMap<String, String>();
        for (ElementValue value : values) {
            valuesMap.put(value.field, value.value);
        }
        boolean required = false;

        for (ElementRelation relation : elementRelations) {
            boolean match = true;
            // check all fields in the relation
            for (ElementValue field : relation.fields) {
                // if we don't have a value for the field then it can't be a match
                if (!valuesMap.containsKey(field.field)) {
                    match = false;
                } else {
                    // if any of the fields' value does not equal to the value for that field in valuesMap, then it's not a relevant relation
                    // breaking out of the loop means 'match' will be false, therefore 'required' will be false as well and we will continue to check other relations
                    match = field.value.equals(valuesMap.get(field.field));
                }
                if (!match) {
                    break;
                }
            }
            required = match && relation.isRequired;
            if (required) {
                break;
            }
        }
        return required;
    }

    private static class ElementRelation {
        public List<ElementValue> fields;
        public boolean isRequired;

        public ElementRelation(List<ElementValue> fields, boolean isRequired) {
            this.fields = fields;
            this.isRequired = isRequired;
        }

        @Override
        public String toString() {
            return "ElementRelation{" +
                    "fields=" + fields +
                    ", isRequired=" + isRequired +
                    '}';
        }
    }

    public static class ElementValue {
        public String field;
        public String value;

        public ElementValue(String field, String value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public String toString() {
            return "ElementValue{" +
                    "field='" + field + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}