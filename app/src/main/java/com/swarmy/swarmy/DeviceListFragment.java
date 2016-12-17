package com.swarmy.swarmy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.tumblr.bookends.Bookends;

import com.google.common.collect.Lists;

import org.apache.commons.collections4.comparators.BooleanComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.collections4.comparators.NullComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import io.particle.android.sdk.utils.EZ;
import io.particle.android.sdk.utils.TLog;
import io.particle.android.sdk.utils.ui.Toaster;
import io.particle.android.sdk.utils.ui.Ui;

import static io.particle.android.sdk.utils.Py.list;
import static io.particle.android.sdk.utils.Py.truthy;

@ParametersAreNonnullByDefault
public class DeviceListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<DevicesLoader.DevicesLoadResult> {


    public interface Callbacks {
        void onDeviceSelected(ParticleDevice device);

        void onDeviceSelected(ArrayList<ParticleDevice> devices);
    }


    private static final TLog log = TLog.get(DeviceListFragment.class);

    // A no-op impl of {@link Callbacks}. Used when this fragment is not attached to an activity.
    private static final Callbacks dummyCallbacks = new Callbacks() {
        @Override
        public void onDeviceSelected(ParticleDevice device) {
            // no-op
        }

        @Override
        public void onDeviceSelected(ArrayList<ParticleDevice> devices) {
            // no-op
        }
    };

    private SwipeRefreshLayout refreshLayout;
    private FloatingActionsMenu fabMenu;
    private DeviceListAdapter adapter;
    private Bookends<DeviceListAdapter> bookends;
    // FIXME: naming, document better
    private ProgressBar partialContentBar;
    private boolean isLoadingSnackbarVisible;

    private final ReloadStateDelegate reloadStateDelegate = new ReloadStateDelegate();
    private final Comparator<ParticleDevice> comparator = new HelpfulOrderDeviceComparator();

    private Callbacks callbacks = dummyCallbacks;
    private ParticleDeviceSetupLibrary.DeviceSetupCompleteReceiver deviceSetupCompleteReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = EZ.getCallbacksOrThrow(this, Callbacks.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View top = inflater.inflate(R.layout.fragment_device_list, container, false);

        RecyclerView rv = Ui.findView(top, R.id.device_list);
        rv.setHasFixedSize(true);  // perf. optimization
        LinearLayoutManager layoutManager = new LinearLayoutManager(inflater.getContext());
        rv.setLayoutManager(layoutManager);

        @SuppressLint("InflateParams")
        View myHeader = inflater.inflate(R.layout.device_list_header, null);
        myHeader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        partialContentBar = (ProgressBar) inflater.inflate(R.layout.device_list_footer, null);
        partialContentBar.setVisibility(View.INVISIBLE);
        partialContentBar.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        adapter = new DeviceListAdapter(getActivity());
        // Add them as headers / footers
        bookends = new Bookends<>(adapter);
        bookends.addHeader(myHeader);
        bookends.addFooter(partialContentBar);

        rv.setAdapter(bookends);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                // subtracting 1 from position because of header.  This is gross, but it's simple
                // and in this case adequate, so #SHIPIT.
                onDeviceRowClicked(recyclerView, position - 1, v);
            }
        });

        return top;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabMenu = Ui.findView(view, R.id.add_device_fab);
        AddFloatingActionButton addPhoton = Ui.findView(view, R.id.action_set_up_a_photon);

        addPhoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhotonDevice();
                fabMenu.collapse();
            }
        });

        refreshLayout = Ui.findView(view, R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDevices();
            }
        });

        deviceSetupCompleteReceiver = new ParticleDeviceSetupLibrary.DeviceSetupCompleteReceiver() {
            @Override
            public void onSetupSuccess(String id) {
                log.d("Successfully set up " + id);
            }

            @Override
            public void onSetupFailure() {
                log.w("Device not set up.");
            }
        };
        deviceSetupCompleteReceiver.register(getActivity());

        getLoaderManager().initLoader(R.id.device_list_devices_loader_id, null, this);
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDevices();
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshLayout.setRefreshing(false);
        fabMenu.collapse();
        reloadStateDelegate.reset();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = dummyCallbacks;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceSetupCompleteReceiver.unregister(getActivity());
    }

    @Override
    public Loader<DevicesLoader.DevicesLoadResult> onCreateLoader(int i, Bundle bundle) {
        return new DevicesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<DevicesLoader.DevicesLoadResult> loader, DevicesLoader.DevicesLoadResult result) {
        refreshLayout.setRefreshing(false);

        ArrayList<ParticleDevice> devices = Lists.newArrayList(result.devices);
        Collections.sort(devices, comparator);

        reloadStateDelegate.onDeviceLoadFinished(loader, result);

        adapter.clear();
        adapter.addAll(devices);
        bookends.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<DevicesLoader.DevicesLoadResult> loader) {
        // no-op
    }

    private void onDeviceRowClicked(RecyclerView recyclerView, int position, View view) {
        log.i("Clicked on item at position: #" + position);
        if (position >= bookends.getItemCount() || position == -1) {
            // we're at the header or footer view, do nothing.
            return;
        }

        if (adapter.isAllDevicesItem(position)) {
            callbacks.onDeviceSelected(adapter.getConnectedDevices());
            return;
        }

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        final ParticleDevice device = adapter.getItem(position);

        if (device.isFlashing()) {
            Toaster.s(getActivity(),
                    "Device is being flashed, please wait for the flashing process to end first");

        } else if (!device.isConnected()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Device offline")
                    .setMessage("Device is offline")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        } else {
            callbacks.onDeviceSelected(device);
        }
    }

    public boolean onBackPressed() {
        if (fabMenu.isExpanded()) {
            fabMenu.collapse();
            return true;
        } else {
            return false;
        }
    }

    private void addPhotonDevice() {
        ParticleDeviceSetupLibrary.startDeviceSetup(getActivity());
    }


    private void refreshDevices() {
        Loader<Object> loader = getLoaderManager().getLoader(R.id.device_list_devices_loader_id);
        loader.forceLoad();
    }


    static class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            final View topLevel;
            final TextView modelName;
            final ImageView productImage;
            final TextView deviceName;
            final TextView statusTextWithIcon;
            final TextView productId;
            final ImageView overflowMenuIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                topLevel = itemView;
                modelName = Ui.findView(itemView, R.id.product_model_name);
                productImage = Ui.findView(itemView, R.id.product_image);
                deviceName = Ui.findView(itemView, R.id.product_name);
                statusTextWithIcon = Ui.findView(itemView, R.id.online_status);
                productId = Ui.findView(itemView, R.id.product_id);
                overflowMenuIcon = Ui.findView(itemView, R.id.context_menu);
            }
        }


        private final ArrayList<ParticleDevice> devices = new ArrayList<>();
        private final ArrayList<ParticleDevice> connectedDevices = new ArrayList<>();
        private final FragmentActivity activity;
        private Drawable defaultBackground;

        DeviceListAdapter(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.row_device_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (defaultBackground == null) {
                defaultBackground = holder.topLevel.getBackground();
            }

            if (position % 2 == 0) {
                holder.topLevel.setBackgroundResource(R.color.shaded_background);
            } else {
                holder.topLevel.setBackground(defaultBackground);
            }
            if (isAllDevicesItem(position)) {
                holder.modelName.setText("");
                holder.productImage.setImageResource(R.drawable.swarmy_vertical_logo);
                holder.statusTextWithIcon.setText("Online");
                holder.statusTextWithIcon.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.online_dot, 0);
                holder.productId.setText("");
                holder.deviceName.setText("All Online Devices");
                holder.overflowMenuIcon.setVisibility(View.INVISIBLE);
                return;
            }
            final ParticleDevice device = devices.get(position);
            holder.modelName.setText("Swarmy Robot");
            holder.productImage.setImageResource(R.drawable.photon_vector_small);

            Pair<String, Integer> statusTextAndColoredDot = getStatusTextAndColoredDot(device);
            holder.statusTextWithIcon.setText(statusTextAndColoredDot.first);
            holder.statusTextWithIcon.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, statusTextAndColoredDot.second, 0);

            holder.productId.setText(device.getID().toUpperCase());

            String name = truthy(device.getName())
                    ? device.getName()
                    : "Unnamed device";
            holder.deviceName.setText(name);

            holder.overflowMenuIcon.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showMenu(view, device);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (connectedDevices.size() > 1) {
                return devices.size() + 1;
            } else {
                return devices.size();
            }
        }

        void clear() {
            devices.clear();
            connectedDevices.clear();
            notifyDataSetChanged();
        }

        void addAll(List<ParticleDevice> toAdd) {
            devices.addAll(toAdd);
            for (ParticleDevice device : devices) {
                if (device.isConnected()) {
                    connectedDevices.add(device);
                }
            }
            notifyDataSetChanged();
        }

        ParticleDevice getItem(int position) {
            return devices.get(position);
        }

        boolean isAllDevicesItem(int position) {
            if (position >= devices.size() && connectedDevices.size() > 1) {
                return true;
            }
            return false;
        }

        ArrayList<ParticleDevice> getConnectedDevices() {
            return connectedDevices;
        }

        private void showMenu(View v, final ParticleDevice device) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.context_device_row);
            popup.setOnMenuItemClickListener(DeviceActionsHelper.buildPopupMenuHelper(activity, device));
            popup.show();
        }

        private Pair<String, Integer> getStatusTextAndColoredDot(ParticleDevice device) {
            int dot;
            String msg;
            if (device.isFlashing()) {
                dot = R.drawable.device_flashing_dot;
                msg = "Flashing";

            } else if (device.isConnected()) {
                dot = R.drawable.online_dot;
                msg = "Online";

            } else {
                dot = R.drawable.offline_dot;
                msg = "Offline";
            }
            return Pair.create(msg, dot);
        }
    }


    static class DeviceOnlineStatusComparator implements Comparator<ParticleDevice> {

        @Override
        public int compare(ParticleDevice lhs, ParticleDevice rhs) {
            return BooleanComparator.getTrueFirstComparator().compare(
                    lhs.isConnected(), rhs.isConnected());
        }
    }


    static class UnnamedDevicesFirstComparator implements Comparator<ParticleDevice> {

        private final NullComparator<String> nullComparator = new NullComparator<>(false);

        @Override
        public int compare(ParticleDevice lhs, ParticleDevice rhs) {
            String lhname = lhs.getName();
            String rhname = rhs.getName();
            return nullComparator.compare(lhname, rhname);
        }
    }


    static class HelpfulOrderDeviceComparator extends ComparatorChain<ParticleDevice> {

        HelpfulOrderDeviceComparator() {
            super(new DeviceOnlineStatusComparator(), false);
            this.addComparator(new UnnamedDevicesFirstComparator(), false);
        }
    }


    class ReloadStateDelegate {

        static final int MAX_RETRIES = 10;

        int retryCount = 0;

        void onDeviceLoadFinished(final Loader<DevicesLoader.DevicesLoadResult> loader, DevicesLoader.DevicesLoadResult result) {
            if (!result.isPartialResult) {
                reset();
                return;
            }

            retryCount++;
            if (retryCount > MAX_RETRIES) {
                // tried too many times, giving up. :(
                partialContentBar.setVisibility(View.INVISIBLE);
                return;
            }

            if (!isLoadingSnackbarVisible) {
                isLoadingSnackbarVisible = true;
            }

            partialContentBar.setVisibility(View.VISIBLE);
            ((DevicesLoader) loader).setUseLongTimeoutsOnNextLoad(true);
            EZ.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (isResumed()) {
                        loader.forceLoad();
                    }
                }
            });
        }

        void reset() {
            retryCount = 0;
            partialContentBar.setVisibility(View.INVISIBLE);
        }

    }

}
