package com.ratatouille;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ratatouille.models.Chef;
import com.ratatouille.models.Cliente;
import com.ratatouille.models.Direccion;
import com.ratatouille.models.Herramienta;
import com.ratatouille.models.Receta;
import com.ratatouille.models.Usuario;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    Button buttonSiguiente;
    FirebaseAuth mAuth;
    EditText edMail;
    EditText edPass;
    EditText edPassAgain;
    EditText edPhoneNumber;
    EditText edName;
    EditText edIdNumber;
    TextView tvNacimiento;
    CheckBox cbChef;
    CheckBox cbCliente;
    DatabaseReference mDatabaseChefs;
    DatabaseReference mDatabaseClientes;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseChefs = FirebaseDatabase.getInstance().getReference("chefs");
        mDatabaseClientes = FirebaseDatabase.getInstance().getReference("clientes");
        buttonSiguiente = findViewById(R.id.buttonSiguienteImagen);
        edMail = findViewById(R.id.editTextCorreo);
        edPass = findViewById(R.id.editTextContraseña);
        edPassAgain = findViewById(R.id.editTextContraseña4);
        edPhoneNumber = findViewById(R.id.editTextTelefono);
        edName = findViewById(R.id.editTextNombre);
        cbChef = findViewById(R.id.checkBoxChef);
        cbCliente = findViewById(R.id.checkBoxComensal);
        tvNacimiento = findViewById(R.id.editTextNacimiento);
        edIdNumber = findViewById(R.id.editTextCC);
        tvNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                Log.d("DATEPICKER", "onDateSet: " + dayOfMonth + " " + month + " " + year);
                String date = dayOfMonth + "/" + month + "/" + year;
                tvNacimiento.setText(date);
            }
        };
        buttonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    if (validacionTipo()) {
                        Intent intent = new Intent(v.getContext(), ImagenActivity.class);

                        String nomAux = edName.getText().toString();
                        double calAux = 10;
                        String correoAux = edMail.getText().toString();
                        Long docAux = Long.parseLong(edIdNumber.getText().toString());
                        String claveAux = "";
                        int telAux = Integer.parseInt(edPhoneNumber.getText().toString());
                        String birthdayAux = tvNacimiento.getText().toString();
                        String fotoAux = "";
                        int credAux = 10;
                        Direccion dirAux = new Direccion(0, "", "", 0, 0);
                        List<Herramienta> herramientasAux = new ArrayList<Herramienta>();

                        if (cbChef.isChecked()) {
                            intent.putExtra("tipo", "chef");
                            Boolean estAux = false;
                            List<Receta> recetasAux = new ArrayList<Receta>();
                            Chef chefAux = new Chef(nomAux, calAux, correoAux, docAux, claveAux, telAux, birthdayAux, fotoAux, credAux, dirAux, herramientasAux, estAux, recetasAux);
                            String id = mDatabaseChefs.push().getKey();
                            mDatabaseChefs.child(id).setValue(chefAux);
                        }
                        if (cbCliente.isChecked()) {
                            intent.putExtra("tipo", "cliente");
                            Boolean primeAux = false;
                            Cliente clienteAux = new Cliente(nomAux, calAux, correoAux, docAux, claveAux, telAux, birthdayAux, fotoAux, credAux, dirAux, herramientasAux, primeAux);
                            String id = mDatabaseClientes.push().getKey();
                            mDatabaseClientes.child(id).setValue(clienteAux);
                        }
                        registerUser(edMail.getText().toString(), edPassAgain.getText().toString());
                        startActivity(intent);
                    }
                }

            }
        });
    }


    private boolean validacionTipo() {
        if (cbChef.isChecked() && cbCliente.isChecked()) {
            Toast.makeText(RegisterActivity.this, "Debe seleccionar solo uno", Toast.LENGTH_LONG).show();
            return false;
        } else if (!cbChef.isChecked() && !cbCliente.isChecked()) {
            Toast.makeText(RegisterActivity.this, "Debe seleccionar solo uno", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private void registerUser(String email, String password) {
        if (validateForm()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d("NEW USER", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        if (user != null) {
                            UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                            upcrb.setDisplayName(edName.getText().toString());
                            upcrb.setPhotoUri(Uri.parse("path/to/pic"));
                            user.updateProfile(upcrb.build());
                            updateUI(user);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("NEW USER", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // ...
                }
            });
        }
    }


    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(getBaseContext(), EscogerTipoActivity.class);
            intent.putExtra("user", currentUser.getEmail());
            startActivity(intent);
        }
    }


    private boolean validateForm() {
        boolean valid = true;
        String email = edMail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            edMail.setError("Requerido");
            valid = false;
        }
        String password = edPass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            edPass.setError("Requerido");
            valid = false;
        }
        if (!edPass.getText().toString().matches(edPassAgain.getText().toString())) {
            Toast.makeText(RegisterActivity.this, "La contraseñas ingresadas no coinciden", Toast.LENGTH_LONG).show();
            edPass.setText("");
            edPassAgain.setText("");
            return false;
        }
        return valid;
    }
}
