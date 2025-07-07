package com.swolo.lpy.pysx.main.recycler;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainTwoActivity extends AppCompatActivity {


    private RecyclerView rvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);

        rvMain = (RecyclerView) findViewById(R.id.recyclerView);
        rvMain.setLayoutManager(new LinearLayoutManager(MainTwoActivity.this));
        rvMain.addItemDecoration(new myDecoration() );
        rvMain.setAdapter(new LinerAdapter(this, new LinerAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {
                Toast.makeText(MainTwoActivity.this,"nindianjile:" + position, Toast.LENGTH_SHORT).show();
            }
        }));

        // Remove cached Bluetooth scale information display
        // if (isBluetoothScaleConnected()) {
        //     displayWeight();
        //     if (!isBluetoothPrinterConnected()) {
        //         showPrinterNotConnectedDialog();
        //     } else {
        //         printSuccessMessage();
        //     }
        // }
        // Print a message when Bluetooth scale is connected in the popup
        if (isBluetoothScaleConnected()) {
            printSuccessMessage();
        }
    }

    private boolean isBluetoothScaleConnected() {
        // TODO: Implement logic to check if Bluetooth scale is connected
        return false; // Placeholder
    }

    private void displayWeight() {
        // TODO: Implement logic to display the weight
        Toast.makeText(this, "Weight: 0.0 kg", Toast.LENGTH_SHORT).show();
    }

    private boolean isBluetoothPrinterConnected() {
        // TODO: Implement logic to check if Bluetooth printer is connected
        return false; // Placeholder
    }

    private void showPrinterNotConnectedDialog() {
        // TODO: Implement dialog to show that the printer is not connected
        Toast.makeText(this, "Bluetooth printer is not connected", Toast.LENGTH_SHORT).show();
    }

    private void printSuccessMessage() {
        // TODO: Implement logic to print a success message
        Log.d("MainTwoActivity", "Attempting to print success message");
        Toast.makeText(this, "Bluetooth scale connected successfully", Toast.LENGTH_SHORT).show();
        // Add additional logging or print logic here
    }

    class myDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0,0,0,getResources().getDimensionPixelOffset(R.dimen.dividerHeight));
        }
    }



}
