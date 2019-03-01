package com.example.conductor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private TextView numCard;
    private TextView saldo;
    private TextView gasto;
    private TextView pagina;
    private ConstraintLayout cardContainer;
    private BarChart grafico;

    // <----- não tive tempo de setar tudo por conta da viagem, ai ficou engessado essa parte ----->
    private String mes = "10";
    private String ano = "2018";
    private String page = "1";

    private ArrayList<String> historico = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        numCard = findViewById(R.id.txt_num_card);
        saldo = findViewById(R.id.txt_valor_disponivel);
        gasto = findViewById(R.id.txt_valor_gasto);
        grafico = findViewById(R.id.grafico);

        cardContainer = findViewById(R.id.card_container);
        pagina = findViewById(R.id.txt_page);

        cardContainer.setVisibility(View.INVISIBLE);
        grafico.getDescription().setEnabled(false);

        Cartao cartao = new Cartao();

        obterNumero(cartao);
        obterSaudo(cartao);
        obterExtratos(mes, ano, page);
        obterUso(mes, cartao);
        obterGrafico();

    }

    private void setData(ArrayList<String> historico){
        ArrayList<BarEntry> valores = new ArrayList<>();

        for (int i=0; i<historico.size(); i++){
            valores.add(new BarEntry(i, Float.parseFloat(historico.get(i))));
        }

        BarDataSet set = new BarDataSet(valores, "Meses");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setDrawValues(true);

        BarData data = new BarData(set);

        grafico.setData(data);
        grafico.invalidate();
        grafico.animateY(500);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_grafico:
                    cardContainer.setVisibility(View.INVISIBLE);
                    grafico.setVisibility(View.VISIBLE);
                    return true;
                case R.id.menu_cartao:
                    grafico.setVisibility(View.INVISIBLE);
                    cardContainer.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    private void obterNumero(final Cartao cartao) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = "https://2hm1e5siv9.execute-api.us-east-1.amazonaws.com/dev/users/profile";

        // METODO GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(final String response) {
                try {

                    JSONObject resposta = new JSONObject(response);
                    cartao.setNumero(resposta.getString("cardNumber"));
                    numCard.setText("XXXX XXXX XXXX " + cartao.getNumero().substring(12,16));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erro: " + error.toString());

            }
        });
        requestQueue.add(stringRequest);
    }

    private void obterSaudo(final Cartao cartao) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = "https://2hm1e5siv9.execute-api.us-east-1.amazonaws.com/dev/resume";

        // METODO GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(final String response) {
                try {

                    JSONObject resposta = new JSONObject(response);
                    cartao.setSaldo(resposta.getString("balance"));
                    saldo.setText(cartao.getSaldo());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erro: " + error.toString());

            }
        });
        requestQueue.add(stringRequest);
    }

    private void obterUso(String mes, final Cartao cartao) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = "https://2hm1e5siv9.execute-api.us-east-1.amazonaws.com/dev/card-usage";

        final int numMes = Integer.parseInt(mes) - 1;

        // METODO GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                try {
                    final ArrayList<CartaoExtrato> extratos = new ArrayList<>();
                    JSONArray resposta = new JSONArray(response);

                    cartao.setGasto(resposta.getJSONObject(numMes).getString("value"));
                    gasto.setText(cartao.getGasto());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erro: " + error.toString());

            }
        });
        requestQueue.add(stringRequest);
    }

    private void obterExtratos(String mes, String ano, String page) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = "https://2hm1e5siv9.execute-api.us-east-1.amazonaws.com/dev/card-statement?month=" + mes + "&year=" + ano + "&page=" + page;

        final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        final SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM");

        pagina.setText("Pagina " + page);

        // METODO GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                try {
                    final ArrayList<CartaoExtrato> extratos = new ArrayList<>();
                    JSONObject resposta = new JSONObject(response);
                    JSONArray valores = new JSONArray(resposta.get("purchases").toString());

                    for(int i=0; i< valores.length(); i++){
                        CartaoExtrato extrato = new CartaoExtrato();


                        Date date = inputFormat.parse(valores.getJSONObject(i).getString("date"));
                        String formattedDate = outputFormat.format(date);

                        extrato.setData(formattedDate);
                        extrato.setTipo(valores.getJSONObject(i).getString("description"));
                        extrato.setValor(valores.getJSONObject(i).getString("value"));

                        extratos.add(extrato);
                    }

                    // LISTA DE QUADRINHOS DISPONIVEIS
                    ListView listView = findViewById(R.id.list_compras);
                    ArrayAdapter adapter = new ExtratoAdapter(MainActivity.this, extratos);
                    listView.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erro: " + error.toString());

            }
        });
        requestQueue.add(stringRequest);
    }

    private void obterGrafico() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = "https://2hm1e5siv9.execute-api.us-east-1.amazonaws.com/dev/card-usage";

        // METODO GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                try {

                    JSONArray resposta = new JSONArray(response);

                    for (int i=0; i<resposta.length(); i++){
                        historico.add(resposta.getJSONObject(i).getString("value"));
                        setData(historico);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erro: " + error.toString());

            }
        });
        requestQueue.add(stringRequest);
    }

}
