package com.example.delfoodiepassenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.delfoodiepassenger.model.Cart;
import com.nimbusds.jose.util.ArrayUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    Cart[] cartData;
    Context context;
    Cart currentCartItem;
    String restaurantLat,restaurantLng;
    Realm realm;

    public CartAdapter(Cart[] cartData, CartActivity activity) {
        this.cartData = cartData;
        this.context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.cart_menu_item_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            currentCartItem = cartData[position];
            holder.itemName.setText(""+cartData[position].getItemName());
            holder.itemPrice.setText(""+cartData[position].getItemPrice());
            holder.itemQuantity.setText(""+cartData[position].getItemQuantity());
            final String itemId = cartData[position].getItemId();
            String imageUrl = cartData[position].getImageurl();
            Glide.with(context).load(imageUrl).centerCrop().into(holder.itemImage);
            restaurantLat = cartData[position].getLat();
            restaurantLng = cartData[position].getLon();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<Cart> rows = realm.where(Cart.class).equalTo("itemId",itemId).findAll();
                            rows.deleteAllFromRealm();
                            cartData[position] = null;
                            /*Cart[] temp = new Cart[cartData.length-1];
                            for (int i = 0; i < cartData.length-1; i++) {
                                if (i<position){
                                    temp[i]=cartData[i];
                                }else if (i==position){
                                    temp[i]=cartData[i+1];
                                }else {
                                    temp[i]=cartData[i+1];
                                }
                            }
                            cartData = temp;*/
                        }
                    });
                }
            });
    }

    @Override
    public int getItemCount() {
        return cartData.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView itemImage;
        TextView itemName, itemPrice, itemQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImageForCart);
            itemName = itemView.findViewById(R.id.itemNameForCart);
            itemPrice = itemView.findViewById(R.id.itemPriceForCart);
            itemQuantity = itemView.findViewById(R.id.itemQuantityForCart);
        }
    }
}
