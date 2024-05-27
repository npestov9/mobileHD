package com.example.cookai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import org.json.JSONException;
import java.math.BigDecimal;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "PayPalActivity";
    private static final int PAYPAL_REQUEST_CODE = 123;

    private TextView profileName;
    private TextView profileEmail;
    private TextView promptsUsed;
    private TextView promptsLeft;
    private Button buy10PromptsButton;
    private Button buy100PromptsButton;
    private Button buy1000PromptsButton;
    private SharedPreferences sharedPreferences;

    private static final PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // Use sandbox for testing
            .clientId("Abezek8rUEH6BTeC9J5TMv0yJWynWF6KAmD8d9IGXsuzI9IUDDn-sSYwtYO89stdf3U7nw2u9iUbMVGU");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        promptsUsed = findViewById(R.id.promptsUsed);
        promptsLeft = findViewById(R.id.promptsLeft);
        buy10PromptsButton = findViewById(R.id.buy10PromptsButton);
        buy100PromptsButton = findViewById(R.id.buy100PromptsButton);
        buy1000PromptsButton = findViewById(R.id.buy1000PromptsButton);

        sharedPreferences = getSharedPreferences("com.example.cookai", MODE_PRIVATE);

        // Start PayPal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);

        // Get the currently signed-in user
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            profileName.setText(account.getDisplayName());
            profileEmail.setText(account.getEmail());
        }

        // Load prompts count from SharedPreferences
        int promptsLeftCount = sharedPreferences.getInt("prompts_left", 100);
        int promptsUsedCount = 100 - promptsLeftCount;

        promptsUsed.setText("Prompts Used: " + promptsUsedCount);
        promptsLeft.setText("Prompts Left: " + promptsLeftCount);

        // Set up onClickListeners for the buttons
        buy10PromptsButton.setOnClickListener(v -> makePayment(5, 10));
        buy100PromptsButton.setOnClickListener(v -> makePayment(20, 100));
        buy1000PromptsButton.setOnClickListener(v -> makePayment(50, 1000));
    }

    private void makePayment(int cost, int prompts) {
        PayPalPayment payment = new PayPalPayment(
                new BigDecimal(cost), "USD", "Purchase Prompts",
                PayPalPayment.PAYMENT_INTENT_SALE
        );

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        Log.d(TAG, "onActivityResult: " + paymentDetails);
                        // Handle payment success
                        Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
                        addPrompts(100); // Update prompts based on payment
                    } catch (JSONException e) {
                        Log.e(TAG, "onActivityResult: " + e.getMessage());
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Payment was canceled by user
                Log.d(TAG, "onActivityResult: Payment canceled");
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.e(TAG, "onActivityResult: Invalid payment");
                Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addPrompts(int amount) {
        int promptsLeftCount = sharedPreferences.getInt("prompts_left", 100);
        promptsLeftCount += amount;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("prompts_left", promptsLeftCount);
        editor.apply();

        // Update the prompts display
        int promptsUsedCount = 100 - promptsLeftCount;
        promptsUsed.setText("Prompts Used: " + promptsUsedCount);
        promptsLeft.setText("Prompts Left: " + promptsLeftCount);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}
