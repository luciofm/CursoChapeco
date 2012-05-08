package curso.despesas;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.kroz.activerecord.ActiveRecordBase;
import org.kroz.activerecord.ActiveRecordException;
import org.kroz.activerecord.EntitiesHelper;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import curso.despesas.DAO.Despesas;
import curso.despesas.DAO.Usuario;
import curso.despesas.DTO.DespesasDTO;
import curso.utils.Networking;

public class ListaDespesasActivity extends ListActivity {

	ActiveRecordBase db;
	private int user_id;
	double valor_total = 0.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.despesas);
		setTitle("Despesas: " + getIntent().getStringExtra("NOME"));
		super.onCreate(savedInstanceState);
		db = ((DespesasApp) getApplication()).getDatabase();

		user_id = getIntent().getIntExtra("ID", 1);
		String url = getString(R.string.base_url) + user_id
				+ getString(R.string.metodo_despesas);
		task.execute(url);
	}

	AsyncTask<String, Void, ArrayList<Despesas>> task = new AsyncTask<String, Void, ArrayList<Despesas>>() {

		@Override
		protected void onPostExecute(ArrayList<Despesas> result) {
			if (result != null)
				setListAdapter(new DespesasAdapter(ListaDespesasActivity.this,
						0, result));
			Resources res = getResources();
			TextView text = (TextView) findViewById(R.id.textTotal);
			text.setText(String.valueOf(valor_total));
			if (valor_total > 0)
				text.setTextColor(res.getColor(R.color.verde));
			else
				text.setTextColor(res.getColor(R.color.vermelho));
		}

		@Override
		protected ArrayList<Despesas> doInBackground(String... params) {
			String url = params[0];
			String resposta;

			try {
				resposta = Networking.getHttpRequest(url);
			} catch (IOException e) {
				e.printStackTrace();
				try {
					return (ArrayList<Despesas>) db.find(Despesas.class,
							"userid = ?",
							new String[] { String.valueOf(user_id) });
				} catch (ActiveRecordException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				}
			}

			try {
				db.delete(Despesas.class, "userid = ?",
								new String[] { String.valueOf(user_id) });
			} catch (ActiveRecordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Type type = new TypeToken<ArrayList<DespesasJson>>(){}.getType();
			Gson gson = new GsonBuilder().create();
			ArrayList<DespesasJson> temp = gson.fromJson(resposta, type);
			for (DespesasJson d : temp) {
				try {
					Despesas despesa = db.newEntity(Despesas.class);
					EntitiesHelper.copyFieldsWithoutID(despesa, d.getDespesa());
					despesa.setUser_id(user_id);
					despesa.save();
					valor_total += despesa.getValor();
				} catch (ActiveRecordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				return (ArrayList<Despesas>) db.find(Despesas.class, "userid = ?",
									new String[] { String.valueOf(user_id) });
			} catch (ActiveRecordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	};

	public class DespesasAdapter extends ArrayAdapter<Despesas> {
		LayoutInflater inflater;
		Resources res;
		
		public DespesasAdapter(Context context, int textViewResourceId,
				List<Despesas> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
			res = context.getResources();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null)
				v = inflater.inflate(android.R.layout.simple_list_item_2,
						parent, false);
			Despesas d = getItem(position);
			TextView text = (TextView) v.findViewById(android.R.id.text1);
			text.setText(d.getDescricao());

			text = (TextView) v.findViewById(android.R.id.text2);
			text.setText(String.valueOf(d.getValor()));
			if (d.getValor() > 0)
				text.setTextColor(res.getColor(R.color.verde));
			else
				text.setTextColor(res.getColor(R.color.vermelho));

			return v;
		}	
	}
	
	public static class DespesasJson {
		public DespesasDTO despesa;

		public DespesasJson() {
			
		}

		public DespesasDTO getDespesa() {
			return this.despesa;
		}
	}
}
