package com.example.pickacard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.*;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class Add_Card_Activity extends AppCompatActivity {
    ImageButton backarroww;
    EditText validity, cardHolderName;
    private EditText etCardNumber;
    private String detectedNetwork = "Unknown";

    private ImageView ivCardType;

    RadioGroup cardTypeGroup;
    Button clearButton, submitButton;
    TextInputLayout layout;
    TextView cardError,bankSpinner,amountspinnerr;
    List<BankItem> bankList;
    ImageView selectedLogoImage = null;
    String selectedLogoTag = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        backarroww = findViewById(R.id.backarrow);
        cardError = findViewById(R.id.cardErrorText);
        etCardNumber = findViewById(R.id.etCardNumber);
        ivCardType = findViewById(R.id.ivCardType);
        validity = findViewById(R.id.validity);
        cardHolderName = findViewById(R.id.cardHolderName);
        bankSpinner = findViewById(R.id.bankSpinner);
        amountspinnerr = findViewById(R.id.amountspinnerr);
        cardTypeGroup = findViewById(R.id.cardTypeGroup);
        clearButton = findViewById(R.id.clearButton);
        submitButton = findViewById(R.id.submitButton);

        backarroww.setOnClickListener(v -> onBackPressed());
        validity.setOnClickListener(v -> showMonthYearPicker());

        // Card number formatting + detection
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final String space = " ";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String cardNum = s.toString().replaceAll("\\s", "");

                if (!s.toString().equals(current)) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < cardNum.length(); i++) {
                        if (i > 0 && i % 4 == 0) formatted.append(space);
                        formatted.append(cardNum.charAt(i));
                    }

                    current = formatted.toString();
                    etCardNumber.removeTextChangedListener(this);
                    etCardNumber.setText(current);
                    etCardNumber.setSelection(current.length());
                    etCardNumber.addTextChangedListener(this);
                }

                if (!cardNum.isEmpty() && cardNum.length() < 16) {
                    Animation shake = AnimationUtils.loadAnimation(etCardNumber.getContext(), R.anim.shake);
                    etCardNumber.startAnimation(shake);
                    cardError.setVisibility(View.VISIBLE);
                    cardError.setError("Card number must be 16 digits");
                } else {
                    etCardNumber.setError(null);
                    cardError.setVisibility(View.GONE);
                }

                detectCardType(cardNum);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Bank spinner list
        bankList = new ArrayList<>();
        bankList.add(new BankItem("SBI", R.drawable.sbi));
        bankList.add(new BankItem("Kotak", R.drawable.kotak));
        bankList.add(new BankItem("ICICI", R.drawable.icici));
        bankList.add(new BankItem("Axis", R.drawable.axis));
        bankList.add(new BankItem("IndusInd", R.drawable.indusind));
        bankList.add(new BankItem("RBL", R.drawable.rbl));
        bankList.add(new BankItem("HDFC", R.drawable.hdfc));
        bankList.add(new BankItem("Canara", R.drawable.canara));
        bankSpinner.setOnClickListener(v -> showBankSelectionDialog());


        //amount Spinner

        List<String> amounts = new ArrayList<>();

// Step 10,000 from 10,000 to 100,000
        for (int i = 10000; i <= 100000; i += 10000) {
            amounts.add(String.valueOf(i));
        }

// Step 50,000 from 150,000 to 300,000
        for (int i = 150000; i <= 300000; i += 50000) {
            amounts.add(String.valueOf(i));
        }

        amountspinnerr.setOnClickListener(v -> showAmountSelectionDialog(amounts));


        ImageView[] logos = {
                findViewById(R.id.sbiicon), findViewById(R.id.hdfcicon),
                findViewById(R.id.canaraicon), findViewById(R.id.rblicon),
                findViewById(R.id.indusindicon), findViewById(R.id.iciciicon),
                findViewById(R.id.kotakicon), findViewById(R.id.axisicon)
        };

        for (ImageView logo : logos) {
            logo.setOnClickListener(v -> {
                if (selectedLogoImage != null) {
                    selectedLogoImage.setBackgroundResource(0);
                }
                logo.setBackgroundResource(R.drawable.logo_border_selected);
                selectedLogoImage = logo;
                selectedLogoTag = logo.getTag().toString();
            });
        }

        applyPressAnimation(clearButton);

        clearButton.setOnClickListener(v -> {
            etCardNumber.setText("");
            validity.setText("");
            cardHolderName.setText("");
            cardTypeGroup.clearCheck();
            amountspinnerr.setText("");
            amountspinnerr.setTag(null);

            bankSpinner.setText("");
            bankSpinner.setTag(null);

            if (selectedLogoImage != null) {
                selectedLogoImage.setBackgroundResource(0);
                selectedLogoImage = null;
                selectedLogoTag = null;
            }
        });
applyPressAnimation(submitButton);
        submitButton.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showAmountSelectionDialog(List<String> amounts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Card Limit");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, amounts);

        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedAmount = amounts.get(which);
            amountspinnerr.setText(selectedAmount);
            amountspinnerr.setTag(selectedAmount);  // Optional: store as tag
        });

        builder.show();
    }

    private void showBankSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Bank");

        ArrayAdapter<BankItem> adapter = new BankSpinnerAdapter(this, bankList);

        builder.setAdapter(adapter, (dialog, which) -> {
            BankItem selectedBank = bankList.get(which);
            bankSpinner.setText(selectedBank.getBankName());
            bankSpinner.setTag(selectedBank); // store BankItem as tag for later use
        });

        builder.show();
    }

    private void detectCardType(String number) {
        if (number.matches("^4[0-9]{0,}$")) {
            ivCardType.setImageResource(R.drawable.visa);
            detectedNetwork = "Visa";
        } else if (number.matches("^5[1-5][0-9]{0,}$")) {
            ivCardType.setImageResource(R.drawable.mastercard);
            detectedNetwork = "MasterCard";
        } else if (number.matches("^6(?:011|5[0-9]{2})[0-9]{0,}$")) {
            ivCardType.setImageResource(R.drawable.discover);
            detectedNetwork = "Discover";
        } else if (number.matches("^(508[5-9]|6069|607|356|608|6521|6522)[0-9]{0,}$")) {
            ivCardType.setImageResource(R.drawable.rupay);
            detectedNetwork = "RuPay";
        } else {
            ivCardType.setImageResource(R.drawable.default123);
            detectedNetwork = "Unknown";
        }
    }


    private void showMonthYearPicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> validity.setText(String.format(Locale.getDefault(), "%02d/%d", m + 1, y)),
                year, month, calendar.get(Calendar.DAY_OF_MONTH));

        try {
            Field[] fields = dialog.getDatePicker().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("mDaySpinner") || field.getName().equals("mDayPicker")) {
                    field.setAccessible(true);
                    Object dayPicker = field.get(dialog.getDatePicker());
                    ((View) dayPicker).setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.show();
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Add Card")
                .setMessage("Are you sure you want to add this card?")
                .setPositiveButton("Yes", (dialog, which) -> addCardToDatabase())
                .setNegativeButton("No", null)
                .show();
    }

    private void addCardToDatabase() {
        int selectedId = cardTypeGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Select card type", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = ((RadioButton) findViewById(selectedId)).getText().toString();
        BankItem selectedBankItem = (BankItem) bankSpinner.getTag();

        String bank = selectedBankItem != null ? selectedBankItem.getBankName() : "";
        String number = etCardNumber.getText().toString();
        String valid = validity.getText().toString();
        String holder = cardHolderName.getText().toString();
        String limit = amountspinnerr.getText().toString();



        if (number.isEmpty() || valid.isEmpty() || holder.isEmpty() || limit.isEmpty() ) {
            Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLogoTag == null) {
            Toast.makeText(this, "Select a bank logo", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String customId = prefs.getString("customId", null);

        if (customId == null || customId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(customId).child("cards");

        cardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String cardId = "card" + (count + 1);

                Map<String, Object> cardData = new HashMap<>();
                cardData.put("type", type);
                cardData.put("bank", bank.toLowerCase().trim());
                cardData.put("number", number);
                cardData.put("validity", valid);
                cardData.put("holder", holder);
                cardData.put("limit", limit);

                cardData.put("logo", selectedLogoTag);
                cardData.put("network", detectedNetwork);


                cardRef.child(cardId).setValue(cardData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(Add_Card_Activity.this, "Card added successfully", Toast.LENGTH_SHORT).show();
                            logCardAddToHistory(bank, type);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(Add_Card_Activity.this, "Failed to add card", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Add_Card_Activity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logCardAddToHistory(String bank, String cardType) {
        String actionText = "Card added - " + cardType + " (" + bank + ")";
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String iconTag = "add_card";

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String customId = prefs.getString("customId", null);
        if (customId == null) return;

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(customId)
                .child("history");

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String historyId = "history" + (count + 1);

                Map<String, Object> historyData = new HashMap<>();
                historyData.put("action", actionText);
                historyData.put("time", time);
                historyData.put("icon", iconTag);

                historyRef.child(historyId).setValue(historyData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Add_Card_Activity.this, "Failed to log history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void applyPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    break;
            }
            return false;
        });
    }

}
