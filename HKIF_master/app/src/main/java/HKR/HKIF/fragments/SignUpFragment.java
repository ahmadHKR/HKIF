package HKR.HKIF.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

import HKR.HKIF.R;
import HKR.HKIF.Users.Person;
import HKR.HKIF.dB.AttendancesUpdater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.app.Activity.RESULT_OK;

public class SignUpFragment extends Fragment {

    static  String firstNameText;
    static  String lastNameText;
    static  String emailText;
    static  String passwordText;
    static  String phoneNumberText;
    static  String format;

    static String position;


    private static final int PAYPAL_REQUEST_CODE = 999;

    private EditText firstName, lastName, email, password, phoneNumber;
    private Button signUpBtn;
    private DatabaseReference databasePerson;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    static String payPalClientId = "AcTzkvwF8gQ8X4SZlA5ytBJ5Mmv5OnfheeEVNNBVO1QpO9aOh9sMjQMJwoIo43kvBDlOY6oViBUBuLHL";

    TextView sportFee;

    // PayPal payment
    static PayPalConfiguration payConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) //Use SANDBOX for test, but PRODUCTION to implement real
            .clientId(payPalClientId);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        firstName = view.findViewById(R.id.signUp_firstName);
        lastName = view.findViewById(R.id.signUp_lastName);
        email = view.findViewById(R.id.signUp_email);
        password = view.findViewById(R.id.signUp_password);
        phoneNumber = view.findViewById(R.id.signUp_phone_number);
        signUpBtn = view.findViewById(R.id.btn_signUp);
        progressBar = view.findViewById(R.id.sign_up_progressBar);

        sportFee = view.findViewById(R.id.fee);

        progressBar.setVisibility(view.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        databasePerson = FirebaseDatabase.getInstance().getReference("person");

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pay();
                signUp();

            }
        });

        // Init PayPal
        Intent intent = new Intent(getActivity(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payConfig);
        getActivity().startService(intent);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    private void signUp() {

        firstNameText = firstName.getText().toString();
        lastNameText = lastName.getText().toString();
         emailText = email.getText().toString();
         passwordText = password.getText().toString();
        phoneNumberText = phoneNumber.getText().toString();

        if (TextUtils.isEmpty(firstNameText) && TextUtils.isEmpty(lastNameText) &&
                TextUtils.isEmpty(emailText) && TextUtils.isEmpty(passwordText) &&
                TextUtils.isEmpty(phoneNumberText)) {

            Toast.makeText(getContext(), "Please fill all the fields!", Toast.LENGTH_LONG).show();

        } else {

            final DateFormat dateFormat = DateFormat.getDateInstance();
            format = dateFormat.format(new Date());

            position = String.valueOf(Person.POSITION.MEMBER);
            final String ID = databasePerson.push().getKey();

            progressBar.setVisibility(getView().VISIBLE);

            String priceFormat = "200";//sportFee.getText().toString()
            //.replace("kr","")
            //.replace(",","");

            PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(priceFormat),
                    "SEK","Membership Fee", PayPalPayment.PAYMENT_INTENT_SALE);

            Intent intent = new Intent(getActivity(), PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,payConfig);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);

            firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NotNull Task<AuthResult> task) {

                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException existEmail) {
                                    Toast.makeText(getContext(), "This email is already exist", Toast.LENGTH_LONG).show();

                                    FragmentTransaction fragmentHome = getFragmentManager().beginTransaction();
                                    fragmentHome.replace(R.id.fragment_container, new SignUpFragment());
                                    fragmentHome.commit();

                                } catch (Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {

                                progressBar.setVisibility(getView().GONE);

                            }
                        }

                    });
        }
    }
    void pay(){

        String priceFormat = "200";//sportFee.getText().toString()
                //.replace("kr","")
                //.replace(",","");

        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(priceFormat),
                "SEK","Membership Fee", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,payConfig);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAYPAL_REQUEST_CODE){

            if (resultCode == RESULT_OK){

                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if (confirmation != null){

                   String state = confirmation.getProofOfPayment().getState();

                    if (state.equals("approved")){

                        try {
                            final Person person = new Person(firebaseAuth.getUid(), firstNameText,
                                    lastNameText, emailText, passwordText,
                                    phoneNumberText, position, true, format);

                            //new AttendancesUpdater(person.getPersonID());

                            databasePerson.child(FirebaseAuth.getInstance().getCurrentUser()
                                    .getUid()).setValue(person).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                        Toast.makeText(getContext(), "Done!", Toast.LENGTH_LONG).show();
                                }
                            });

                            sportFee.setText("Payment approved: Go to login");

                        }catch (Exception e){
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                        sportFee.setText("Error");

                   /*try{
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                        final Person person = new Person(firebaseAuth.getUid(), firstNameText,
                                lastNameText, emailText, passwordText,
                                phoneNumberText, position, jsonObject.getJSONObject("response").getBoolean("state"),//state from JSON
                                format);

                        //new AttendancesUpdater(person.getPersonID());

                        databasePerson.child(FirebaseAuth.getInstance().getCurrentUser()
                                .getUid()).setValue(person).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NotNull Task<Void> task) {
                                Toast.makeText(getContext(), "Done!", Toast.LENGTH_LONG).show();
                            }
                        });

                        sportFee.setText("Payment Approved");

                    }catch (JSONException e){
                        e.printStackTrace();
                    }*/
                }
                else
                    sportFee.setText("Confirmation is null");
            }
            // add else drop after
            else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(getContext(), "Payment cancel", Toast.LENGTH_SHORT).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(getContext(), "Invalid Payment", Toast.LENGTH_SHORT).show();
        }
    }

}
