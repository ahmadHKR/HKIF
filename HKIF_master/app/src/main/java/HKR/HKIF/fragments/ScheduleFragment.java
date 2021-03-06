package HKR.HKIF.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.Objects;

import HKR.HKIF.R;
import HKR.HKIF.adapters.ScheduleAdapter;
import HKR.HKIF.data.ScheduleItem;


@SuppressLint("ValidFragment")
public class ScheduleFragment extends Fragment {

    private String path;
    private ArrayList<ScheduleItem> items = new ArrayList<>();

    public ScheduleFragment(String eventHandler) {
        this.path = eventHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.schedule_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get our list view
        final ListView theListView = getActivity().findViewById(R.id.mainListView);
        final SwipeRefreshLayout refreshLayout = getActivity().findViewById(R.id.swipeRefreshLayout);
        getList(path, theListView);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ScheduleFragment(path)).commit();
            }

        });
    }

    //retrieve data from DB
    private void getList(String eventHandler, final ListView listView) {
        final Query query = FirebaseDatabase.getInstance().getReference("schedule").orderByChild("day").equalTo(eventHandler);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot i : dataSnapshot.getChildren()) {
                        items.add(i.getValue(ScheduleItem.class));
                    }
                }
                setAdapter(listView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    //To fill the adapter with items
    private void setAdapter(ListView theListView) {

        // create custom adapter that holds elements and their state (we need hold a id's of unfolded elements for reusable elements)
        final ScheduleAdapter adapter = new ScheduleAdapter(getContext(), items);
        // set elements to adapter
        theListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // set on click event listener to list view
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                // toggle clicked cell state
                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected cell is toggled
                adapter.registerToggle(pos);
            }
        });
    }
}
