package com.app.quizoofinal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    ImageButton signup_next;
    AlertDialog dialog;
    TextInputLayout Username, Phonenum, Password;
    FirebaseDatabase database;
    FirebaseAuth auth;
    SignInButton Google;
    ProgressBar pbotp;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    TextView phonetxt, Signin,Resend;
    EditText inputcode1, inputcode2, inputcode3, inputcode4, inputcode5, inputcode6;
    Button closebutton;
    String user, phone, pass;
    DatabaseReference ref;
    GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private static int RC_SIGN_IN = 100;
    float v = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        CardView signupcard = findViewById(R.id.signup_card);
        signupcard.setBackgroundResource(R.drawable.cardback_login);

        Username = findViewById(R.id.name_textinput);
        Phonenum = findViewById(R.id.phone_textinput);
        Password = findViewById(R.id.pass_textinput);

        ///////////// Alert View //////////////////////////////
        final View customlayout = getLayoutInflater().inflate(R.layout.custom_alert_view, null);

        phonetxt = customlayout.findViewById(R.id.phonetxt);

        inputcode1 = customlayout.findViewById(R.id.inputcode1);
        inputcode2 = customlayout.findViewById(R.id.inputcode2);
        inputcode3 = customlayout.findViewById(R.id.inputcode3);
        inputcode4 = customlayout.findViewById(R.id.inputcode4);
        inputcode5 = customlayout.findViewById(R.id.inputcode5);
        inputcode6 = customlayout.findViewById(R.id.inputcode6);
        setupOTPinputs();

        Resend= customlayout.findViewById(R.id.resend);

        pbotp = customlayout.findViewById(R.id.pbotp);
        closebutton = customlayout.findViewById(R.id.close);


        signup_next = findViewById(R.id.Signup_next);
        signup_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Username.getEditText().getText().toString().isEmpty() == false && Phonenum.getEditText().getText().toString().isEmpty() == false && Password.getEditText().getText().toString().isEmpty() == false) {
                        phone = Phonenum.getEditText().getText().toString();
                        phonetxt.setText(phone);
                        sendVerificationCode(phone);
                        Resend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendVerificationCode(phone);
                                pbotp.setVisibility(View.VISIBLE);
                            }
                        });

                        dialog = new AlertDialog.Builder(SignupActivity.this, R.style.AlertdialogTheme)
                                .setView(customlayout)
                                .setCancelable(false)
                                .show();


                        closebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                ViewGroup parent = (ViewGroup) customlayout.getParent();
                                parent.removeAllViews();
                            }
                        });

                        Window window = dialog.getWindow();
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        ImageButton verify = customlayout.findViewById(R.id.Verify_next);
                        verify.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pbotp.setVisibility(View.VISIBLE);
                                if (inputcode2.getText().toString().trim().isEmpty() || inputcode1.getText().toString().trim().isEmpty() || inputcode3.getText().toString().trim().isEmpty() || inputcode4.getText().toString().trim().isEmpty() || inputcode5.getText().toString().trim().isEmpty() || inputcode6.getText().toString().trim().isEmpty()) {
                                    Toast.makeText(SignupActivity.this, "Please Enter valid OTP", Toast.LENGTH_SHORT).show();
                                    pbotp.setVisibility(View.GONE);
                                    return;
                                }

                                String code = inputcode1.getText().toString() +
                                        inputcode2.getText().toString() +
                                        inputcode3.getText().toString() +
                                        inputcode4.getText().toString() +
                                        inputcode5.getText().toString() +
                                        inputcode6.getText().toString();

                                String Verificationcode = getIntent().getStringExtra("Verification");

                                if (Verificationcode != null) {
                                    verify.setVisibility(View.INVISIBLE);

                                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(Verificationcode, code);
                                    auth.signInWithCredential(credential)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    verify.setVisibility(View.VISIBLE);
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(SignupActivity.this, HomeScreen.class);
                                                        intent.putExtra("usernamelogin", Username.getEditText().getText().toString());
                                                        startActivity(intent);
                                                        ////Firebase Data store ////////////////
                                                        database = FirebaseDatabase.getInstance();
                                                        ref = database.getReference("user");
                                                        user = Username.getEditText().getText().toString();
                                                        pass = Password.getEditText().getText().toString();
                                                        UserHelper helper = new UserHelper(user, phone, pass);
                                                        ref.child(phone).setValue(helper);
                                                        finish();

                                                    } else {
                                                        Toast.makeText(SignupActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                                                        pbotp.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                    else{
                        if (Phonenum.getEditText().getText().toString().isEmpty() == true) {
                            Phonenum.setError("Please Enter Your Phone Number , It's Required");
                        }
                        if (Password.getEditText().getText().toString().isEmpty() == true) {
                            Password.setError("Please Enter Your Password, It's Required");
                        }
                        if (Username.getEditText().getText().toString().isEmpty() == true) {
                            Username.setError("Please Enter Your Username, It's Required");
                        }

                        Username.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (Username.getEditText().getText().toString().isEmpty() == true) {
                                    Username.setError("Please Enter Your Username, It's Required");
                                } else {
                                    Username.setError(null);
                                    Username.setErrorEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });

                        Phonenum.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (Phonenum.getEditText().getText().toString().isEmpty() == true) {
                                    Phonenum.setError("Please Enter Your Phone Number, It's Required");
                                } else {
                                    Phonenum.setError(null);
                                    Phonenum.setErrorEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        Password.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (Password.getEditText().getText().toString().isEmpty() == true) {
                                    Password.setError("Please Enter Your Password, It's Required");
                                } else {
                                    Password.setError(null);
                                    Password.setErrorEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });

                }
            }
        });

        /// Social Media Animation //////////////////
        Google = findViewById(R.id.googleup);
        Google.setSize(SignInButton.SIZE_WIDE);
        Google.setTranslationY(500);
        Google.setAlpha(v);
        Google.animate().translationY(0).alpha(1).setDuration(2000).setStartDelay(100).start();


        // Google Sign up /////////////////////////////////////////////////////////////////////////

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.googleup:
                        signIn();
                        break;
                    // ...
                }
            }
        });

        //////////////////////////////////////////////////

        Signin = findViewById(R.id.signin);
        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    ////////// Google Functions /////////////////////////////////////////
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

            String personName = null;

            if (acct != null) {
                personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();
                Toast.makeText(this, "Person Name= " + personName, Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(SignupActivity.this, HomeScreen.class);
            intent.putExtra("usernamelogin", personName);
            startActivity(intent);

        } catch (ApiException e) {
            Log.d("Message", e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////
    ////////////////////////////// OTP FUNCTION ////////////////////////////////

    private void setupOTPinputs() {
        inputcode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputcode1.getText().toString().length() == 1) {
                    inputcode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputcode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputcode2.getText().toString().length() == 1) {
                    inputcode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputcode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputcode3.getText().toString().length() == 1) {
                    inputcode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputcode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputcode4.getText().toString().length() == 1) {
                    inputcode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputcode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputcode5.getText().toString().length() == 1) {
                    inputcode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }


    private void sendVerificationCode(String PhoneNo) {

        StartFirebaseLogin();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + PhoneNo,
                60,
                TimeUnit.SECONDS,
                this,
                mCallback
        );
    }

    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // pb.setVisibility(View.GONE);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //   pb.setVisibility(View.GONE);
                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Toast.makeText(SignupActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
                pbotp.setVisibility(View.GONE);
                Intent intent = getIntent();
                intent.putExtra("Verification", s);
            }

        };
    }


}