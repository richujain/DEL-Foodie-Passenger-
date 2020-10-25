package com.example.delfoodiepassenger;

import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
    ItemListData[] itemsListData;
    Context context;
    int quantity = 1;
    public ItemListAdapter(ItemListData[] itemsListData,RestaurantMenuListActivity context) {
        this.itemsListData = itemsListData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.menu_item_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.itemName.setText(itemsListData[position].getItemName());
        holder.itemPrice.setText(itemsListData[position].getItemPrice());
        final String imageurl = itemsListData[position].getImageUrl();
        final String itemId = itemsListData[position].getItemId();
        Glide.with(context).load(imageurl).centerCrop().into(holder.itemImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToCartDialog(position);
            }
        });
    }
    private void showAddToCartDialog(int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        dialog.setTitle("Add To Cart");

        LayoutInflater inflater = LayoutInflater.from(context);
        View login_layout = inflater.inflate(R.layout.layout_add_to_cart,null);

        final MaterialTextView itemNameInPopUp = login_layout.findViewById(R.id.itemNameInPopUp);
        itemNameInPopUp.setText(itemsListData[position].getItemName());
        final CircleImageView itemImageInPopUp = login_layout.findViewById(R.id.itemImageInPopUp);
        String imageUrl = itemsListData[position].getImageUrl();
        Glide.with(context).load(imageUrl).centerCrop().into(itemImageInPopUp);
        final Button minus = login_layout.findViewById(R.id.minus);
        final Button plus = login_layout.findViewById(R.id.plus);
        final MaterialEditText itemQuantity = login_layout.findViewById(R.id.itemImage);
        itemQuantity.setText(quantity);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity > 0){
                    quantity--;
                }
                else{
                    Toast.makeText(context, "Minimum Quantity Reached", Toast.LENGTH_SHORT).show();
                }
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity<11){
                    quantity++;
                }
                else{
                    Toast.makeText(context, "Maximum Quantity Reached", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("Add To Cart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //validation
                if (TextUtils.isEmpty(itemQuantity.getText().toString().trim())) {
                    Toast.makeText(context, "Enter Valid Quantity", Toast.LENGTH_SHORT).show();
                }
                else{

                }


            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }
    private void itemDetails(String itemId){
        String json;
        try{
            InputStream inputStream = context.getAssets().open("restaurants.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer,"UTF-8");
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0; i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.getString("id").equals(itemId)){
                    itemId.add(jsonObject.getString("id"));
                    itemName.add(jsonObject.getString("item_name"));
                    itemPrice.add(jsonObject.getString("item_price"));
                    imageUrl.add(jsonObject.getString("image_URL"));
                }
            }

        }
        catch (IOException exception){
            exception.printStackTrace();
            Log.v("aaa","4");
        }
        catch (JSONException jsonException){
            jsonException.printStackTrace();
            Log.v("aaa","5");
        }
    }

    @Override
    public int getItemCount() {
        return itemsListData.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemImage;
        TextView itemName, itemPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
        }
    }
}
