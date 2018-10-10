package com.chteuchteu.munin.adptr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.chteuchteu.munin.R;
import com.chteuchteu.munin.obj.MuninPlugin;

import java.util.ArrayList;
import java.util.List;

public class Adapter_CheckablePluginsList extends ArrayAdapter<MuninPlugin> {
    private static int rowLayout = R.layout.adapter_twolines_checkbox;

    private Context context;
    private List<MuninPlugin> plugins;
    private List<MuninPlugin> selectedItems;

    public Adapter_CheckablePluginsList(Context context, List<MuninPlugin> plugins) {
        super(context, rowLayout, plugins);
        this.context = context;
        this.plugins = plugins;
        this.selectedItems = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null)
            view = convertView;
        else
            view = LayoutInflater.from(context).inflate(rowLayout, parent, false);

        final MuninPlugin plugin = this.plugins.get(position);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.line_0);
        TextView row1 = (TextView) view.findViewById(R.id.line_a);
        TextView row2 = (TextView) view.findViewById(R.id.line_b);

        // Toggle checkbox on row click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });

        row1.setText(plugin.getFancyNameOrDefault());
        row2.setText(plugin.getCategory());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked && !selectedItems.contains(plugin))
                    selectedItems.add(plugin);
                else if (!checked)
                    selectedItems.remove(plugin);
            }
        });

        // Android will recycle views. We have to update the checkbox state
        checkBox.setChecked(selectedItems.contains(plugin));

        return view;
    }

    public List<MuninPlugin> getSelectedItems() {
        return this.selectedItems;
    }
}
