/*
 * Copyright (c) 2016-2018. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.orpheusdroid.screenrecorder;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import javax.annotation.Nonnull;

public class DonateActivity extends AppCompatActivity implements View.OnClickListener{

    private final ActivityCheckout mCheckout = Checkout.forActivity(this, ScreenCamApp.get().getBilling());
    private Inventory mInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.preference_theme_key), Const.PREFS_LIGHT_THEME);
        switch (theme){
            case Const.PREFS_DARK_THEME:
                setTheme(R.style.AppTheme_Dark);
                break;
            case Const.PREFS_BLACK_THEME:
                setTheme(R.style.AppTheme_Black);
                break;
        }

        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_donate);

        CardView donate1 = findViewById(R.id.donate_1);
        CardView donate2 = findViewById(R.id.donate_2);
        CardView donate5 = findViewById(R.id.donate_5);
        CardView donatePayPal = findViewById(R.id.donate_paypal);

        donate1.setOnClickListener(this);
        donate2.setOnClickListener(this);
        donate5.setOnClickListener(this);
        donatePayPal.setOnClickListener(this);

        setupPurchase();
    }

    private void setupPurchase(){
        mCheckout.start();

        mCheckout.createPurchaseFlow(new PurchaseListener());

        mInventory = mCheckout.makeInventory();
        mInventory.load(Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(ProductTypes.IN_APP, "sku_1", "sku_2", "sku_5"), new InventoryCallback());
    }

    private void makePurchase(final String sku){
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(BillingRequests requests) {
                requests.purchase(ProductTypes.IN_APP, sku, null, mCheckout.getPurchaseFlow());
            }
        });
    }

    private void consumePurchase(final Purchase purchase){
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                requests.consume(purchase.token, new ConsumeListener());
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //finish this activity and return to parent activity
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.donate_1:
                makePurchase("sku_1");
                break;
            case R.id.donate_2:
                makePurchase("sku_2");
                break;
            case R.id.donate_5:
                makePurchase("sku_5");
                break;
            case R.id.donate_paypal:
                String url = "http://paypal.me/vijaichander/5";
                Intent donateURL = new Intent(Intent.ACTION_VIEW);
                donateURL.setData(Uri.parse(url));
                startActivity(donateURL);
                break;
        }
    }

    private class ConsumeListener extends EmptyRequestListener<Object> {
        @Override
        public void onSuccess(@Nonnull Object result) {
            Toast.makeText(DonateActivity.this, "Thank you for donating!", Toast.LENGTH_SHORT).show();
        }
    }

    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(Purchase purchase) {
            // here you can process the loaded purchase
            consumePurchase(purchase);
        }

        @Override
        public void onError(int response, Exception e) {
            // handle errors here
            Toast.makeText(DonateActivity.this, "Something went wrong... please try again. Error code: " + response, Toast.LENGTH_SHORT).show();
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            // your code here
        }
    }
}
