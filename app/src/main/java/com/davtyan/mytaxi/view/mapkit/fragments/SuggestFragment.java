package com.davtyan.mytaxi.view.mapkit.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.davtyan.mytaxi.R;
import com.davtyan.mytaxi.view.mapkit.activitys.YMapsActivity;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SuggestFragment extends DialogFragment
        implements SearchManager.SuggestListener, View.OnClickListener {

    public static final String TAG = SuggestFragment.class.getSimpleName();

    private final Point CENTER = new Point(55.75, 37.62);

    private final BoundingBox BOUNDING_BOX = new BoundingBox(
            new Point(CENTER.getLatitude() - 0.2, CENTER.getLongitude() - 0.2),
            new Point(CENTER.getLatitude() + 0.2, CENTER.getLongitude() + 0.2));

    private final SearchOptions SEARCH_OPTIONS = new SearchOptions().setSearchTypes(
            SearchType.GEO.value |
                    SearchType.BIZ.value |
                    SearchType.TRANSIT.value);


    private SearchManager searchManager;
    private ListView suggestResultView;
    private ArrayAdapter resultAdapter;
    private List<String> suggestResult;
    private SearchView queryEndEdit;
    private SearchView queryStartEdit;
    private InputMethodManager inputMethodManager;
    private boolean isStart;
    private String startText;
    private String endText;
    private TextView queryEndText;
    private TextView queryStartText;


    public void show(YMapsActivity activity, String title) {
        SuggestFragment fragment = new SuggestFragment();
        fragment.show(activity.getSupportFragmentManager(), TAG);
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullscreen_dialog);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getView(inflater, container);
        assert getArguments() != null;
        startText = getArguments().getString("title");
        queryStartEdit.setQuery(startText,false);
        queryStartText.setText(startText);
        prepareSearchView(queryStartEdit);
        prepareSearchView(queryEndEdit);
        suggestOnItemClickListener();
        initAdapter();
        queryEndEdit.setIconified(false);
        queryStartEdit.setIconified(false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private View getView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.suggest, container, false);
        queryEndEdit = view.findViewById(R.id.end_query);
        queryStartEdit = view.findViewById(R.id.start_query);

        queryEndText = view.findViewById(R.id.end_text);
        queryStartText = view.findViewById(R.id.start_text);
        suggestResultView = view.findViewById(R.id.suggest_result);

        queryEndText.setOnClickListener(this);
        queryStartText.setOnClickListener(this);

        showSoftKeyboard(view);
        return view;
    }

    private void initAdapter() {
        suggestResult = new ArrayList<>();
        resultAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                suggestResult);

        suggestResultView.setAdapter(resultAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            inputMethodManager = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private boolean isSelectRoute() {
        return queryEndEdit.getQuery().length() > 0 && queryStartEdit.getQuery().length() > 0;
    }

    private void prepareSearchView(final SearchView view) {

        view.setQueryHint(getString(R.string.search_view_hint));


        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isSelectRoute()) {
                    ((YMapsActivity) getActivity()).requestGeoPoint(
                            queryEndEdit.getQuery().toString(),
                            queryStartEdit.getQuery().toString()
                    );
                   // inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    dismiss();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) return false;
                requestSuggest(view.getQuery().toString());
                return false;
            }
        });

    }

    private void suggestOnItemClickListener() {
        suggestResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                assert getActivity() != null;
                if (!isStart){
                    endText = suggestResult.get(i);
                    queryEndEdit.setQuery(endText,false);
                    queryEndText.setText(endText);
                }
                else {
                    startText = suggestResult.get(i);
                    queryStartEdit.setQuery(startText,false);
                    queryStartText.setText(startText);
                }
                if (isSelectRoute()) {
                    ((YMapsActivity) getActivity()).requestGeoPoint(
                            endText, startText
                    );
                    //inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    Log.i(TAG, "suggestResultView: " + suggestResult.get(i));
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "Dialog 1: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "Dialog 1: onCancel");
    }


    @Override
    public void onSuggestResponse(@NonNull List<SuggestItem> list) {
        suggestResult.clear();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            suggestResult.add(list.get(i).getDisplayText());
        }
        resultAdapter.notifyDataSetChanged();
        suggestResultView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuggestError(@NonNull Error error) {
        String errorMessage = "unknown_error_message";
        if (error instanceof RemoteError) {
            errorMessage = "remote_error_message";
        } else if (error instanceof NetworkError) {
            errorMessage = "network_error_message";
        }

        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void requestSuggest(String query) {
        suggestResultView.setVisibility(View.INVISIBLE);
        searchManager.suggest(query, BOUNDING_BOX, SEARCH_OPTIONS, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_text:
                isStart = true;
                queryStartText.setVisibility(View.GONE);
                queryEndText.setVisibility(View.VISIBLE);
                queryEndEdit.setVisibility(View.GONE);
                queryStartEdit.setVisibility(View.VISIBLE);
                if (startText != null) {
                    queryStartEdit.setQuery(startText,false);
                    queryStartText.setText(startText);
                }
                //queryEndEdit.setIconified(true);

                break;
            case R.id.end_text:
                isStart = false;
                queryStartText.setVisibility(View.VISIBLE);
                queryEndText.setVisibility(View.GONE);
                queryEndEdit.setVisibility(View.VISIBLE);
                queryStartEdit.setVisibility(View.GONE);

                if (endText != null){
                    queryEndEdit.setQuery(endText,false);
                    queryEndText.setText(endText);
                }
               // queryStartEdit.setIconified(true);
                break;
        }
    }
}
