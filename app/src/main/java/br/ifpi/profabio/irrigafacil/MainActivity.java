package br.ifpi.profabio.irrigafacil;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView item1;
    private SharedPreferences preferences;
    private SeekBar seek;
    static TextView statusMessage;
    static TextView counterMessage;
    ConnectionThread connect;
    static TextView txt_dados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("app", MODE_PRIVATE);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        txt_dados = (TextView) findViewById(R.id.txt_dados);
        counterMessage = (TextView)findViewById(R.id.txt_counter);
        statusMessage = (TextView) findViewById(R.id.txt_status);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        /*if (btAdapter == null) {
            counterMessage.setText("Hardware Bluetooth não está funcionando!");
        } else {
            counterMessage.setText("Ótimo! Hardware Bluetooth está funcionando!");
        }*/
        btAdapter.enable();

        connect = new ConnectionThread("20:14:12:03:24:46");
        connect.start();

        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            /* Esse método é invocado na Activity principal
                sempre que a thread de conexão Bluetooth recebe
                uma mensagem.
             */
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("temp");
            String dataString = new String(data);

            /* Aqui ocorre a decisão de ação, baseada na string
                recebida. Caso a string corresponda à uma das
                mensagens de status de conexão (iniciadas com --),
                atualizamos o status da conexão conforme o código.
             */
            if(dataString.equals("---N"))
                statusMessage.setText("Status: Erro na conexão bluetooth");
            else if(dataString.equals("---S"))
                statusMessage.setText("Status: Conectado");
            else {

                /* Se a mensagem não for um código de status,
                    então ela deve ser tratada pelo aplicativo
                    como uma mensagem vinda diretamente do outro
                    lado da conexão. Nesse caso, simplesmente
                    atualizamos o valor contido no TextView do
                    contador.
                 */
                txt_dados.setText(dataString);
                //txt_umidade.setText(dataUmid);
                //txt_irrigado.setText(dataIrrigado);

            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {

            final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
            final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

            final View Viewlayout = inflater.inflate(R.layout.dialog,
                    (ViewGroup) findViewById(R.id.layout_dialog));

            item1 = (TextView)Viewlayout.findViewById(R.id.txtItem1);

            popDialog.setIcon(android.R.drawable.ic_menu_info_details);
            popDialog.setTitle(R.string.dialog_title);
            popDialog.setView(Viewlayout);
            popDialog.setMessage(R.string.dialog_message);

            seek = (SeekBar) Viewlayout.findViewById(R.id.seekBar1);

            int umid = preferences.getInt("umidade", 0);
            preferences.
            if (umid != 0){
                seek.setProgress(umid);
            }
            item1.setText("Umidade: "+umid+" %");

            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

                    item1.setText("Umidade: "+progress+" %");

                }

                public void onStartTrackingTouch(SeekBar arg0) {
                    // TODO Auto-generated method stub

                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }
            });

            popDialog.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });

            popDialog.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();
                            int umidade = seek.getProgress();
                            editor.putInt("umidade", umidade);
                            editor.apply();
                            String data = String.valueOf("i"+umidade+"t");
                            connect.write(data.getBytes());
                            Toast.makeText(getApplicationContext(), "Indíce de umidade configurado para "+String.valueOf(umidade)+" % ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                    });


            popDialog.create();
            popDialog.show();

        }

        if (id == R.id.action_exit){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
