package com.izv.angel.chatterbot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import chatterbot.ChatterBot;
import chatterbot.ChatterBotFactory;
import chatterbot.ChatterBotSession;
import chatterbot.ChatterBotType;


public class Principal extends Activity implements TextToSpeech.OnInitListener{

    private ChatterBotSession bot1session;
    private final static int CTE = 1;
    private final static int VOZ = 2;
    private TextToSpeech tts;
    private boolean reproductor = false;
    private TextView tv1;
    private HiloConversacion hc;
    private Locale ttsiIdioma= new Locale("es","ES");
    private String reconIdioma="es-ES";
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        tv1 = (TextView) findViewById(R.id.tvTexto);
        tv1.setMovementMethod(new ScrollingMovementMethod());
        bt= (Button)findViewById(R.id.btHablar);
        ChatterBotFactory factory = new ChatterBotFactory();
        ChatterBot bot1 = null;
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bot1session=bot1.createSession();
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            reproductor=true;
            tts.setLanguage(ttsiIdioma);
            //tts.setPitch((float) 0.5); //tono
            //tts.setSpeechRate((float) 0.5); //velocidad
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CTE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this,this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if (requestCode == VOZ){
            if (resultCode == RESULT_OK){
                ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                tv1.append("Yo:\n" + textos.get(0) + "\n");
                final Layout layout = tv1.getLayout();
                if(layout != null){
                    int scrollDelta = layout.getLineBottom(tv1.getLineCount() - 1)
                            - tv1.getScrollY() - tv1.getHeight();
                    if(scrollDelta > 0)
                        tv1.scrollBy(0, scrollDelta);
                }
                String s = textos.get(0);
                hc = new HiloConversacion();
                hc.execute(s);

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public void hablar(View view){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, reconIdioma);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent. EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        startActivityForResult(i, VOZ);
    }

    class HiloConversacion extends AsyncTask<String, Integer, String> {

        private ProgressDialog dialog;

        @Override
        protected String doInBackground(String... params) {
            String texto=params[0];
            String aux="";
            try {
                aux=bot1session.think(texto);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return aux;
        }

        @Override
        protected void onPostExecute(String texto) {
            super.onPostExecute(texto);
            if(reproductor){
                tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
                tv1.append("Bot:\n"+texto+"\n");
                final Layout layout = tv1.getLayout();
                if(layout != null){
                    int scrollDelta = layout.getLineBottom(tv1.getLineCount() - 1) - tv1.getScrollY() - tv1.getHeight();
                    if(scrollDelta > 0)
                        tv1.scrollBy(0, scrollDelta);
                }
            }else{
                Toast.makeText(getBaseContext(), "No se puede reproducir", Toast.LENGTH_SHORT).show();
            }
            hc=null;
            dialog.dismiss();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog= new ProgressDialog(Principal.this);
            dialog.setMessage("ChatterBot Pensando");
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    public void english(View view){
        reconIdioma="en-GB";
        ttsiIdioma= new Locale("en","GB");
        tts.setLanguage(ttsiIdioma);
        bt.setText(R.string.speak);
    }

    public void spanish(View view){
        reconIdioma="es-ES";
        ttsiIdioma= new Locale("es","ES");
        tts.setLanguage(ttsiIdioma);
        bt.setText(R.string.hablar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
