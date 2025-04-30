package com.swolo.lpy.pysx.main.recycler;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;

public class MainTwoActivity extends AppCompatActivity {


    private RecyclerView rvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);

        rvMain = (RecyclerView) findViewById(R.id.rv_main);
        rvMain.setLayoutManager(new LinearLayoutManager(MainTwoActivity.this));
        rvMain.addItemDecoration(new myDecoration() );
        rvMain.setAdapter(new LinerAdapter(this, new LinerAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {
                Toast.makeText(MainTwoActivity.this,"nindianjile:" + position, Toast.LENGTH_SHORT).show();
            }
        }));

    }

    class myDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0,0,0,getResources().getDimensionPixelOffset(R.dimen.dividerHeight));
        }
    }



}
